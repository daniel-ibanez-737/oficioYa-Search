package edu.eci.dosw.oficioya.search.domain;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import edu.eci.dosw.oficioya.search.domain.filtro.FiltroBusqueda;
import edu.eci.dosw.oficioya.search.domain.filtro.FiltroCalificacionMinima;
import edu.eci.dosw.oficioya.search.domain.filtro.FiltroCompuesto;
import edu.eci.dosw.oficioya.search.domain.filtro.FiltroDisponibilidadSemanal;
import edu.eci.dosw.oficioya.search.domain.filtro.FiltroDisponibleAhora;
import edu.eci.dosw.oficioya.search.domain.filtro.FiltroRangoPrecio;
import edu.eci.dosw.oficioya.search.domain.filtro.FiltroZona;
import edu.eci.dosw.oficioya.search.exception.SolicitudInvalidaException;

/**
 * Criterios de una búsqueda. Todos los filtros son opcionales y se combinan (el trabajador debe cumplirlos todos).
 */
public record CriteriosBusqueda(Long oficioId, List<Long> barrioIds, BigDecimal precioMinimo, BigDecimal precioMaximo,
                                DayOfWeek diaSemana, LocalTime hora, boolean soloDisponibleAhora,
                                Double calificacionMinima) {

    public CriteriosBusqueda {
        barrioIds = barrioIds == null ? List.of() : List.copyOf(barrioIds);
        if (precioMinimo != null && precioMaximo != null && precioMinimo.compareTo(precioMaximo) > 0) {
            throw new SolicitudInvalidaException("El precio mínimo no puede ser mayor que el precio máximo.");
        }
        if (hora != null && diaSemana == null) {
            throw new SolicitudInvalidaException("Para filtrar por hora también debes indicar el día de la semana.");
        }
    }

    public static CriteriosBusqueda soloOficio(Long oficioId) {
        return new CriteriosBusqueda(oficioId, List.of(), null, null, null, null, false, null);
    }

    /**
     * Copia de los criterios para otro oficio (se usa cuando la IA identifica el oficio).
     */
    public CriteriosBusqueda conOficio(Long nuevoOficioId) {
        return new CriteriosBusqueda(nuevoOficioId, barrioIds, precioMinimo, precioMaximo, diaSemana, hora,
                soloDisponibleAhora, calificacionMinima);
    }

    /**
     * Construye el filtro combinado con los criterios que se hayan indicado.
     */
    public FiltroBusqueda construirFiltro() {
        FiltroCompuesto filtro = new FiltroCompuesto();
        if (!barrioIds.isEmpty()) {
            filtro.agregar(new FiltroZona(barrioIds));
        }
        if (precioMinimo != null || precioMaximo != null) {
            filtro.agregar(new FiltroRangoPrecio(precioMinimo, precioMaximo));
        }
        if (diaSemana != null) {
            filtro.agregar(new FiltroDisponibilidadSemanal(diaSemana, hora));
        }
        if (soloDisponibleAhora) {
            filtro.agregar(new FiltroDisponibleAhora());
        }
        if (calificacionMinima != null) {
            filtro.agregar(new FiltroCalificacionMinima(calificacionMinima));
        }
        return filtro;
    }
}
