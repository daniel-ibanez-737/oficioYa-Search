package edu.eci.dosw.oficioya.search.client.worker;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import edu.eci.dosw.oficioya.search.dto.OficioDTO;

/** Oficio del catálogo de Worker (Squad 3), en {@code GET /api/v1/trades}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OficioWorkerDTO(UUID id, String name, String description, boolean active) {

    public OficioDTO aOficio() {
        return new OficioDTO(id, name, description);
    }
}
