package edu.eci.dosw.oficioya.search.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Franja de disponibilidad semanal de un trabajador.
 */
public record FranjaDisponibilidad(DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFin) {

    /**
     * Indica si la franja cubre el día y la hora indicados. Si la hora es {@code null}, basta con el día.
     */
    public boolean cubre(DayOfWeek dia, LocalTime hora) {
        if (diaSemana != dia) {
            return false;
        }
        if (hora == null) {
            return true;
        }
        return !hora.isBefore(horaInicio) && hora.isBefore(horaFin);
    }
}
