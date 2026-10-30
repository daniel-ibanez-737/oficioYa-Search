package edu.eci.dosw.oficioya.search.domain.filtro;

import java.time.DayOfWeek;
import java.time.LocalTime;

import edu.eci.dosw.oficioya.search.domain.ResultadoBusqueda;

/**
 * El trabajador debe atender el día indicado (y, si se da, a la hora indicada) según su disponibilidad semanal.
 */
public class FiltroDisponibilidadSemanal implements FiltroBusqueda {

    private final DayOfWeek diaSemana;
    private final LocalTime hora;

    public FiltroDisponibilidadSemanal(DayOfWeek diaSemana, LocalTime hora) {
        this.diaSemana = diaSemana;
        this.hora = hora;
    }

    @Override
    public boolean cumple(ResultadoBusqueda resultado) {
        return resultado.getDisponibilidad().stream().anyMatch(franja -> franja.cubre(diaSemana, hora));
    }
}
