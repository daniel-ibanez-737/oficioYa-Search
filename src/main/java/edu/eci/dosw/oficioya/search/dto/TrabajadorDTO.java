package edu.eci.dosw.oficioya.search.dto;

import java.util.UUID;

import java.math.BigDecimal;
import java.util.List;

/**
 * Trabajador ACTIVO tal como lo entrega Worker a través del Orchestrator.
 * No incluye correo ni teléfono: si Worker los enviara, se ignoran.
 */
public record TrabajadorDTO(UUID id, String nombreCompleto, String fotoPerfilUrl, UUID oficioPrincipalId,
                            String oficioPrincipal, List<UUID> oficiosSecundariosIds, List<Long> barrioIds,
                            String zona, BigDecimal tarifaAproximada, Boolean disponibleAhora,
                            List<DisponibilidadDTO> disponibilidad, Integer trabajosCompletados) {

    /**
     * Indica si el trabajador ofrece el oficio como principal o como secundario.
     */
    public boolean ofrece(UUID oficioId) {
        if (oficioId == null) {
            return false;
        }
        return oficioId.equals(oficioPrincipalId)
                || (oficiosSecundariosIds != null && oficiosSecundariosIds.contains(oficioId));
    }
}
