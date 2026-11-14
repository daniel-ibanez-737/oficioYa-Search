package edu.eci.dosw.oficioya.search.dto;

import java.util.UUID;

/**
 * Reputación de un trabajador tal como la entrega Reputation a través del Orchestrator.
 */
public record ReputacionDTO(UUID trabajadorId, Double reputacion, Integer totalResenas) {
}
