package edu.eci.dosw.oficioya.search.domain.filtro;

import edu.eci.dosw.oficioya.search.domain.ResultadoBusqueda;

/**
 * La calificación promedio del trabajador debe ser igual o superior al mínimo indicado.
 */
public class FiltroCalificacionMinima implements FiltroBusqueda {

    private final double calificacionMinima;

    public FiltroCalificacionMinima(double calificacionMinima) {
        this.calificacionMinima = calificacionMinima;
    }

    @Override
    public boolean cumple(ResultadoBusqueda resultado) {
        return resultado.getCalificacionPromedio() >= calificacionMinima;
    }
}
