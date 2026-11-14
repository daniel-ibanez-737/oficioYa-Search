package edu.eci.dosw.oficioya.search.dto;

import java.util.UUID;

import java.math.BigDecimal;

import edu.eci.dosw.oficioya.search.domain.ResultadoBusqueda;

/**
 * Trabajador en el listado de resultados. Search solo devuelve el listado (no el perfil completo)
 * y nunca expone correo ni teléfono.
 */
public record ResultadoBusquedaResponse(int posicion, UUID trabajadorId, String nombreCompleto, String fotoPerfilUrl,
                                        String oficio, String zona, BigDecimal tarifaAproximada,
                                        double calificacionPromedio, int totalResenas, int trabajosCompletados,
                                        boolean disponibleAhora) {

    public static ResultadoBusquedaResponse desde(ResultadoBusqueda r) {
        return new ResultadoBusquedaResponse(r.getPosicion(), r.getTrabajadorId(), r.getNombreCompleto(),
                r.getFotoPerfilUrl(), r.getOficio(), r.getZona(), r.getTarifaAproximada(),
                r.getCalificacionPromedio(), r.getTotalResenas(), r.getTrabajosCompletados(), r.isDisponibleAhora());
    }
}
