package edu.eci.dosw.oficioya.search.dto;

import java.util.UUID;

/**
 * Oficio del catálogo de Worker.
 */
public record OficioDTO(UUID id, String nombre, String descripcion) {
}
