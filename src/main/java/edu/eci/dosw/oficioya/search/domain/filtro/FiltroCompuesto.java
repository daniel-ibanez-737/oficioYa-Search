package edu.eci.dosw.oficioya.search.domain.filtro;

import java.util.ArrayList;
import java.util.List;

import edu.eci.dosw.oficioya.search.domain.ResultadoBusqueda;

/**
 * Combina varios filtros: el trabajador debe cumplirlos todos (patrón Composite).
 * Sin filtros, todos los trabajadores cumplen.
 */
public class FiltroCompuesto implements FiltroBusqueda {

    private final List<FiltroBusqueda> filtros = new ArrayList<>();

    public FiltroCompuesto agregar(FiltroBusqueda filtro) {
        filtros.add(filtro);
        return this;
    }

    public int cantidad() {
        return filtros.size();
    }

    @Override
    public boolean cumple(ResultadoBusqueda resultado) {
        return filtros.stream().allMatch(filtro -> filtro.cumple(resultado));
    }
}
