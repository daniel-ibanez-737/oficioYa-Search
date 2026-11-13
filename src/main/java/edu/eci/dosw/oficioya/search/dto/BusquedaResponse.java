package edu.eci.dosw.oficioya.search.dto;

import java.util.List;

import edu.eci.dosw.oficioya.search.domain.EstadoInterpretacion;
import edu.eci.dosw.oficioya.search.domain.TipoBusqueda;

/**
 * Respuesta de una búsqueda.
 *
 * @param tipo               lenguaje natural u oficio
 * @param estado             en lenguaje natural: si se identificó el oficio o hay que pedir más detalle
 * @param oficioIdentificado oficio identificado por la IA (lenguaje natural)
 * @param mensaje            mensaje para el contratante (por ejemplo, pedir más detalle)
 * @param total              cantidad de resultados
 * @param resultados         trabajadores ordenados por reputación
 */
public record BusquedaResponse(TipoBusqueda tipo, EstadoInterpretacion estado, String oficioIdentificado,
                               String mensaje, int total, List<ResultadoBusquedaResponse> resultados) {
}
