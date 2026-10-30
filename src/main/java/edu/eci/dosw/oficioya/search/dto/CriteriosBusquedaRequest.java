package edu.eci.dosw.oficioya.search.dto;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import edu.eci.dosw.oficioya.search.domain.CriteriosBusqueda;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Filtros opcionales de una búsqueda. Se combinan: el trabajador debe cumplirlos todos.
 *
 * @param barrioIds           barrios de la zona deseada
 * @param precioMinimo        límite inferior del rango de precio
 * @param precioMaximo        límite superior del rango de precio
 * @param diaSemana           día de la disponibilidad semanal (MONDAY … SUNDAY)
 * @param hora                hora deseada (HH:mm); requiere el día
 * @param soloDisponibleAhora solo trabajadores marcados como "disponible ahora"
 * @param calificacionMinima  calificación promedio mínima (0 a 5)
 */
public record CriteriosBusquedaRequest(
        List<Long> barrioIds,
        @PositiveOrZero BigDecimal precioMinimo,
        @PositiveOrZero BigDecimal precioMaximo,
        DayOfWeek diaSemana,
        @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
        Boolean soloDisponibleAhora,
        @DecimalMin("0.0") @DecimalMax("5.0") Double calificacionMinima) {

    public CriteriosBusqueda aCriterios(Long oficioId) {
        return new CriteriosBusqueda(oficioId, barrioIds, precioMinimo, precioMaximo, diaSemana, hora,
                Boolean.TRUE.equals(soloDisponibleAhora), calificacionMinima);
    }
}
