package edu.eci.dosw.oficioya.search.dto;

/**
 * Reputación de un trabajador tal como la entrega Reputation a través del Orchestrator.
 */
public record ReputacionDTO(Long trabajadorId, Double reputacion, Integer totalResenas) {
}
