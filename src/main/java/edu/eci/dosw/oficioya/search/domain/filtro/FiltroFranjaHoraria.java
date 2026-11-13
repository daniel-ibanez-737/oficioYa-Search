package edu.eci.dosw.oficioya.search.domain.filtro;

import java.time.DayOfWeek;

import edu.eci.dosw.oficioya.search.domain.FranjaHoraria;
import edu.eci.dosw.oficioya.search.domain.ResultadoBusqueda;

/**
 * El trabajador debe atender en algún momento de la franja horaria. Si se indica el día, la franja debe caer ese día;
 * si no, basta con cualquier día de la semana.
 */
public class FiltroFranjaHoraria implements FiltroBusqueda {

    private final DayOfWeek diaSemana;
    private final FranjaHoraria franja;

    public FiltroFranjaHoraria(DayOfWeek diaSemana, FranjaHoraria franja) {
        this.diaSemana = diaSemana;
        this.franja = franja;
    }

    @Override
    public boolean cumple(ResultadoBusqueda resultado) {
        return resultado.getDisponibilidad().stream().anyMatch(disponibilidad -> disponibilidad.seCruzaCon(diaSemana, franja));
    }
}
