package edu.eci.dosw.oficioya.search.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Franja de disponibilidad semanal recibida de Worker.
 */
public record DisponibilidadDTO(DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFin) {
}
