package edu.eci.dosw.oficioya.search.service;

import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.eci.dosw.oficioya.search.client.OrquestadorClient;
import edu.eci.dosw.oficioya.search.domain.Busqueda;
import edu.eci.dosw.oficioya.search.domain.CriteriosBusqueda;
import edu.eci.dosw.oficioya.search.domain.FranjaDisponibilidad;
import edu.eci.dosw.oficioya.search.domain.InterpretacionIA;
import edu.eci.dosw.oficioya.search.domain.OrdenCalificacion;
import edu.eci.dosw.oficioya.search.domain.OrdenamientoPorReputacion;
import edu.eci.dosw.oficioya.search.domain.ResultadoBusqueda;
import edu.eci.dosw.oficioya.search.domain.TipoBusqueda;
import edu.eci.dosw.oficioya.search.domain.filtro.FiltroBusqueda;
import edu.eci.dosw.oficioya.search.dto.BusquedaResponse;
import edu.eci.dosw.oficioya.search.dto.OficioDTO;
import edu.eci.dosw.oficioya.search.dto.ReputacionDTO;
import edu.eci.dosw.oficioya.search.dto.ResultadoBusquedaResponse;
import edu.eci.dosw.oficioya.search.dto.TrabajadorDTO;
import edu.eci.dosw.oficioya.search.exception.SolicitudInvalidaException;

/**
 * Casos de uso de Search. No guarda información: consulta Worker y Reputation a través del Orchestrator
 * y registra un log de cada búsqueda.
 */
@Service
public class BusquedaService {

    private static final Logger log = LoggerFactory.getLogger(BusquedaService.class);

    private final OrquestadorClient orquestadorClient;
    private final InterpretadorLenguajeNatural interpretador;
    private final OrdenamientoPorReputacion ordenamiento = new OrdenamientoPorReputacion();
    private final double umbralConfianza;
    private final int maxSugerencias;

    public BusquedaService(OrquestadorClient orquestadorClient, InterpretadorLenguajeNatural interpretador,
                           @Value("${oficioya.ia.umbral-confianza:0.6}") double umbralConfianza,
                           @Value("${oficioya.search.max-sugerencias:10}") int maxSugerencias) {
        this.orquestadorClient = orquestadorClient;
        this.interpretador = interpretador;
        this.umbralConfianza = umbralConfianza;
        this.maxSugerencias = maxSugerencias;
    }

    /**
     * Catálogo de oficios para la búsqueda por oficio (RF-3).
     */
    public List<OficioDTO> consultarOficios() {
        return orquestadorClient.obtenerOficios();
    }

    /**
     * Búsqueda por oficio con filtros combinados, ordenada por reputación (RF-3, RF-4, RF-5, RF-6, RF-7).
     */
    public BusquedaResponse buscarPorOficio(String usuario, CriteriosBusqueda criterios) {
        long inicio = System.currentTimeMillis();
        List<ResultadoBusqueda> resultados = enOrden(ejecutar(criterios), criterios.orden());
        registrarLog(usuario, TipoBusqueda.OFICIO, criterios.oficioId(), resultados.size(), inicio);
        return new BusquedaResponse(TipoBusqueda.OFICIO, null, null, null, resultados.size(), aRespuesta(resultados));
    }

    /**
     * Búsqueda en lenguaje natural (RF-1, RF-2): la IA identifica el oficio y se sugieren los trabajadores
     * con mayor reputación en ese oficio. Si no se identifica el oficio, se pide más detalle.
     */
    public BusquedaResponse buscarPorLenguajeNatural(String usuario, String texto, CriteriosBusqueda criterios) {
        long inicio = System.currentTimeMillis();
        String descripcion = sanear(texto);
        InterpretacionIA interpretacion = interpretador.interpretar(descripcion, orquestadorClient.obtenerOficios());
        if (!interpretacion.esConfiable(umbralConfianza)) {
            registrarLog(usuario, TipoBusqueda.LENGUAJE_NATURAL, null, 0, inicio);
            return new BusquedaResponse(TipoBusqueda.LENGUAJE_NATURAL, interpretacion.estado(), null,
                    interpretacion.mensajeAclaracion(), 0, List.of());
        }
        // Se toman las de mayor reputación y luego se presentan en el orden pedido.
        List<ResultadoBusqueda> sugerencias = enOrden(ejecutar(criterios.conOficio(interpretacion.oficioId())).stream()
                .limit(maxSugerencias)
                .toList(), criterios.orden());
        registrarLog(usuario, TipoBusqueda.LENGUAJE_NATURAL, interpretacion.oficioId(), sugerencias.size(), inicio);
        return new BusquedaResponse(TipoBusqueda.LENGUAJE_NATURAL, interpretacion.estado(),
                interpretacion.oficioIdentificado(), null, sugerencias.size(), aRespuesta(sugerencias));
    }

    private List<ResultadoBusqueda> ejecutar(CriteriosBusqueda criterios) {
        if (criterios.oficioId() == null) {
            throw new SolicitudInvalidaException("Debes indicar el oficio que buscas.");
        }
        List<TrabajadorDTO> trabajadores = orquestadorClient.buscarTrabajadoresActivos(criterios.oficioId()).stream()
                .filter(t -> t.ofrece(criterios.oficioId()))
                .toList();
        Map<UUID, ReputacionDTO> reputaciones = orquestadorClient.obtenerReputaciones(
                trabajadores.stream().map(TrabajadorDTO::id).toList());
        FiltroBusqueda filtro = criterios.construirFiltro();
        List<ResultadoBusqueda> filtrados = trabajadores.stream()
                .map(t -> aResultado(t, reputaciones.get(t.id())))
                .filter(filtro::cumple)
                .toList();
        return ordenamiento.ordenar(filtrados);
    }

    private static List<ResultadoBusqueda> enOrden(List<ResultadoBusqueda> resultados, OrdenCalificacion orden) {
        return orden == OrdenCalificacion.MAYOR_A_MENOR ? resultados : new OrdenamientoPorReputacion(orden).ordenar(resultados);
    }

    private static ResultadoBusqueda aResultado(TrabajadorDTO t, ReputacionDTO reputacion) {
        double calificacion = reputacion == null || reputacion.reputacion() == null ? 0 : reputacion.reputacion();
        int resenas = reputacion == null || reputacion.totalResenas() == null ? 0 : reputacion.totalResenas();
        List<FranjaDisponibilidad> franjas = t.disponibilidad() == null ? List.of() : t.disponibilidad().stream()
                .map(d -> new FranjaDisponibilidad(d.diaSemana(), d.horaInicio(), d.horaFin()))
                .toList();
        return new ResultadoBusqueda(t.id(), t.nombreCompleto(), t.fotoPerfilUrl(), t.oficioPrincipal(), t.zona(),
                t.barrioIds() == null ? null : Set.copyOf(t.barrioIds()), t.tarifaAproximada(),
                calificacion, resenas, t.trabajosCompletados() == null ? 0 : t.trabajosCompletados(),
                Boolean.TRUE.equals(t.disponibleAhora()), franjas);
    }

    private static List<ResultadoBusquedaResponse> aRespuesta(List<ResultadoBusqueda> resultados) {
        return resultados.stream().map(ResultadoBusquedaResponse::desde).toList();
    }

    /**
     * Valida y limpia el texto antes de enviarlo a la IA: elimina caracteres de control y espacios repetidos.
     */
    static String sanear(String texto) {
        String limpio = texto == null ? "" : texto.replaceAll("\\p{Cntrl}", " ").replaceAll("\\s+", " ").trim();
        if (limpio.length() < 3) {
            throw new SolicitudInvalidaException("Describe el servicio que necesitas con al menos 3 caracteres.");
        }
        return limpio;
    }

    private static void registrarLog(String usuario, TipoBusqueda tipo, UUID oficioId, int total, long inicio) {
        Busqueda busqueda = new Busqueda(usuario, tipo, oficioId, total, System.currentTimeMillis() - inicio,
                LocalDateTime.now());
        log.info("Búsqueda realizada: {}", busqueda.resumen());
    }
}
