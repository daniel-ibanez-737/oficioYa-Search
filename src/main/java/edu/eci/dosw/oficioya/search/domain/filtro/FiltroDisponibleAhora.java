package edu.eci.dosw.oficioya.search.domain.filtro;

import edu.eci.dosw.oficioya.search.domain.ResultadoBusqueda;

/**
 * Solo trabajadores marcados como "disponible ahora".
 */
public class FiltroDisponibleAhora implements FiltroBusqueda {

    @Override
    public boolean cumple(ResultadoBusqueda resultado) {
        return resultado.isDisponibleAhora();
    }
}
