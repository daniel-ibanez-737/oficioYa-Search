package edu.eci.dosw.oficioya.search.domain;

import java.util.Comparator;
import java.util.List;

/**
 * Ordena los resultados EXCLUSIVAMENTE por reputación; ningún dato de pago participa (RF-7).
 * Criterio: calificación promedio desc → trabajos completados desc → total de reseñas desc.
 */
public class OrdenamientoPorReputacion implements Comparator<ResultadoBusqueda> {

    private static final Comparator<ResultadoBusqueda> CRITERIO = Comparator
            .comparingDouble(ResultadoBusqueda::getCalificacionPromedio).reversed()
            .thenComparing(Comparator.comparingInt(ResultadoBusqueda::getTrabajosCompletados).reversed())
            .thenComparing(Comparator.comparingInt(ResultadoBusqueda::getTotalResenas).reversed());

    @Override
    public int compare(ResultadoBusqueda a, ResultadoBusqueda b) {
        return CRITERIO.compare(a, b);
    }

    /**
     * Devuelve una nueva lista ordenada y asigna la posición (desde 1) a cada resultado.
     */
    public List<ResultadoBusqueda> ordenar(List<ResultadoBusqueda> resultados) {
        List<ResultadoBusqueda> ordenados = resultados.stream().sorted(this).toList();
        for (int i = 0; i < ordenados.size(); i++) {
            ordenados.get(i).asignarPosicion(i + 1);
        }
        return ordenados;
    }
}
