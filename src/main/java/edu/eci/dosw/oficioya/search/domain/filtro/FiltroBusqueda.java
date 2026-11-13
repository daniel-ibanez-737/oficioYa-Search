package edu.eci.dosw.oficioya.search.domain.filtro;

import edu.eci.dosw.oficioya.search.domain.ResultadoBusqueda;

/**
 * Criterio que un trabajador debe cumplir para aparecer en los resultados (patrón Strategy).
 */
@FunctionalInterface
public interface FiltroBusqueda {

    boolean cumple(ResultadoBusqueda resultado);
}
