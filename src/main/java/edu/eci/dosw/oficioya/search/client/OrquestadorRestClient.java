package edu.eci.dosw.oficioya.search.client;

import java.util.UUID;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import edu.eci.dosw.oficioya.search.client.worker.OficioWorkerDTO;
import edu.eci.dosw.oficioya.search.client.worker.TrabajadorWorkerDTO;
import edu.eci.dosw.oficioya.search.dto.OficioDTO;
import edu.eci.dosw.oficioya.search.dto.ReputacionDTO;
import edu.eci.dosw.oficioya.search.dto.RespuestaIADTO;
import edu.eci.dosw.oficioya.search.dto.SolicitudIADTO;
import edu.eci.dosw.oficioya.search.dto.TrabajadorDTO;
import edu.eci.dosw.oficioya.search.exception.IntegracionException;

/**
 * Implementación REST de {@link OrquestadorClient}: todas las llamadas van al Orchestrator, que las reenvía
 * al dominio correspondiente. Las rutas son configurables. Los formatos de Worker son los de su API real
 * ({@code GET /api/v1/workers} y {@code GET /api/v1/trades}).
 */
@Component
@Profile("!demo")
public class OrquestadorRestClient implements OrquestadorClient {

    private final RestClient restClient;
    private final String rutaTrabajadores;
    private final String rutaOficios;
    private final String rutaReputaciones;
    private final String rutaInterpretacionIA;

    @SuppressWarnings("java:S107")
    public OrquestadorRestClient(RestClient.Builder builder,
                                 @Value("${oficioya.orquestador.base-url}") String baseUrl,
                                 @Value("${oficioya.orquestador.rutas.trabajadores-activos}") String rutaTrabajadores,
                                 @Value("${oficioya.orquestador.rutas.oficios}") String rutaOficios,
                                 @Value("${oficioya.orquestador.rutas.reputaciones}") String rutaReputaciones,
                                 @Value("${oficioya.orquestador.rutas.interpretacion-ia}") String rutaInterpretacionIA) {
        this.restClient = builder.baseUrl(baseUrl).requestInterceptor(new PropagacionTokenInterceptor()).build();
        this.rutaTrabajadores = rutaTrabajadores;
        this.rutaOficios = rutaOficios;
        this.rutaReputaciones = rutaReputaciones;
        this.rutaInterpretacionIA = rutaInterpretacionIA;
    }

    @Override
    public List<TrabajadorDTO> buscarTrabajadoresActivos(UUID oficioId) {
        try {
            // Worker ya devuelve solo los trabajadores ACTIVOS; tradeId filtra por oficio principal o secundario.
            List<TrabajadorWorkerDTO> trabajadores = restClient.get()
                    .uri(uri -> uri.path(rutaTrabajadores).queryParam("tradeId", oficioId).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<TrabajadorWorkerDTO>>() { });
            return trabajadores == null ? List.of() : trabajadores.stream().map(TrabajadorWorkerDTO::aTrabajador).toList();
        } catch (RestClientException e) {
            throw new IntegracionException("No fue posible consultar los trabajadores en Worker", e);
        }
    }

    @Override
    public List<OficioDTO> obtenerOficios() {
        try {
            List<OficioWorkerDTO> oficios = restClient.get()
                    .uri(uri -> uri.path(rutaOficios).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<OficioWorkerDTO>>() { });
            return oficios == null ? List.of()
                    : oficios.stream().filter(OficioWorkerDTO::active).map(OficioWorkerDTO::aOficio).toList();
        } catch (RestClientException e) {
            throw new IntegracionException("No fue posible consultar el catálogo de oficios en Worker", e);
        }
    }

    @Override
    public Map<UUID, ReputacionDTO> obtenerReputaciones(Collection<UUID> trabajadorIds) {
        if (trabajadorIds.isEmpty()) {
            return Map.of();
        }
        try {
            List<ReputacionDTO> reputaciones = restClient.get()
                    .uri(uri -> uri.path(rutaReputaciones).queryParam("trabajadorIds", trabajadorIds.toArray()).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ReputacionDTO>>() { });
            if (reputaciones == null) {
                return Map.of();
            }
            return reputaciones.stream()
                    .collect(Collectors.toMap(ReputacionDTO::trabajadorId, Function.identity(), (a, b) -> a));
        } catch (RestClientException e) {
            throw new IntegracionException("No fue posible consultar las reputaciones en Reputation", e);
        }
    }

    @Override
    public String solicitarInterpretacionIA(String prompt) {
        try {
            RespuestaIADTO respuesta = restClient.post()
                    .uri(uri -> uri.path(rutaInterpretacionIA).build())
                    .body(new SolicitudIADTO(prompt))
                    .retrieve()
                    .body(RespuestaIADTO.class);
            return respuesta == null ? "" : respuesta.respuesta();
        } catch (RestClientException e) {
            throw new IntegracionException("No fue posible consultar la IA", e);
        }
    }
}
