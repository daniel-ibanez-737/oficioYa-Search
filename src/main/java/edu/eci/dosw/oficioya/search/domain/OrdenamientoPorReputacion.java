package edu.eci.dosw.oficioya.search.domain;

import java.util.Comparator;
import java.util.List;

/**
 * Ordena los resultados EXCLUSIVAMENTE por reputación; ningún dato de pago participa (RF-7).
 * Criterio: calificación promedio (de mayor a menor por defecto, o de menor a mayor si se pide)
 * → trabajos completados desc → total de reseñas desc.
 */
public class OrdenamientoPorReputacion implements Comparator<ResultadoBusqueda> {

    private static final Comparator<ResultadoBusqueda> DESEMPATE = Comparator
            .comparingInt(ResultadoBusqueda::getTrabajosCompletados).reversed()
            .thenComparing(Comparator.comparingInt(ResultadoBusqueda::getTotalResenas).reversed());

    private final Comparator<ResultadoBusqueda> criterio;

    public OrdenamientoPorReputacion() {
        this(OrdenCalificacion.MAYOR_A_MENOR);
    }

    public OrdenamientoPorReputacion(OrdenCalificacion orden) {
        Comparator<ResultadoBusqueda> porCalificacion = Comparator.comparingDouble(ResultadoBusqueda::getCalificacionPromedio);
        if (orden != OrdenCalificacion.MENOR_A_MAYOR) {
            porCalificacion = porCalificacion.reversed();
        }
        this.criterio = porCalificacion.thenComparing(DESEMPATE);
    }

    @Override
    public int compare(ResultadoBusqueda a, ResultadoBusqueda b) {
        return criterio.compare(a, b);
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
