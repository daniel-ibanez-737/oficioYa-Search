package edu.eci.dosw.oficioya.search.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Búsqueda en lenguaje natural: el contratante describe lo que necesita con sus palabras.
 *
 * @param texto     descripción libre (por ejemplo: "necesito alguien que arregle una gotera")
 * @param criterios filtros opcionales que se aplican después de identificar el oficio
 */
public record BusquedaLenguajeNaturalRequest(
        @NotBlank(message = "Describe el servicio que necesitas")
        @Size(min = 3, max = 500, message = "La descripción debe tener entre 3 y 500 caracteres")
        String texto,
        @Valid CriteriosBusquedaRequest criterios) {
}
