package edu.eci.dosw.oficioya.search.domain.filtro;

import java.util.Collection;
import java.util.Set;

import edu.eci.dosw.oficioya.search.domain.ResultadoBusqueda;

/**
 * El trabajador debe cubrir al menos uno de los barrios indicados.
 */
public class FiltroZona implements FiltroBusqueda {

    private final Set<Long> barrioIds;

    public FiltroZona(Collection<Long> barrioIds) {
        this.barrioIds = Set.copyOf(barrioIds);
    }

    @Override
    public boolean cumple(ResultadoBusqueda resultado) {
        return resultado.getBarrioIds().stream().anyMatch(barrioIds::contains);
    }
}
