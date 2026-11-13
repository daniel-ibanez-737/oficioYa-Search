package edu.eci.dosw.oficioya.search.service;

import java.util.List;

import edu.eci.dosw.oficioya.search.domain.InterpretacionIA;
import edu.eci.dosw.oficioya.search.dto.OficioDTO;

/**
 * Interpreta la descripción en lenguaje natural para identificar el oficio que necesita el contratante.
 */
public interface InterpretadorLenguajeNatural {

    InterpretacionIA interpretar(String texto, List<OficioDTO> catalogo);
}
