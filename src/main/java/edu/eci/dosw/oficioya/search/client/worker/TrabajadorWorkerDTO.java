package edu.eci.dosw.oficioya.search.client.worker;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import edu.eci.dosw.oficioya.search.dto.DisponibilidadDTO;
import edu.eci.dosw.oficioya.search.dto.TrabajadorDTO;

/**
 * Trabajador tal como lo entrega Worker (Squad 3) en {@code GET /api/v1/workers}. Se convierte al modelo de Search
 * con {@link #aTrabajador()}.
 * PENDIENTE con otros dominios: Worker no entrega el nombre ni la foto de perfil (son de Users) ni los trabajos
 * completados (son de Service); por ahora quedan vacíos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrabajadorWorkerDTO(UUID id, UUID userId, String status, boolean availableNow, Set<Long> zoneIds,
                                  OficioDelTrabajadorDTO primaryTrade, List<OficioDelTrabajadorDTO> secondaryTrades,
                                  List<FranjaWorkerDTO> slots) {

    /** Oficio de un trabajador con su tarifa. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OficioDelTrabajadorDTO(UUID id, BigDecimal approxRate, String rateUnit, OficioWorkerDTO trade) {
    }

    /** Franja de disponibilidad. Solo se usan las semanales (día de la semana con hora de inicio y fin). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FranjaWorkerDTO(String recurrenceType, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
    }

    public TrabajadorDTO aTrabajador() {
        OficioWorkerDTO principal = primaryTrade == null ? null : primaryTrade.trade();
        List<UUID> secundarios = secondaryTrades == null ? List.of() : secondaryTrades.stream()
                .map(OficioDelTrabajadorDTO::trade).filter(Objects::nonNull).map(OficioWorkerDTO::id).toList();
        List<DisponibilidadDTO> disponibilidad = slots == null ? List.of() : slots.stream()
                .filter(f -> f.dayOfWeek() != null && f.startTime() != null && f.endTime() != null)
                .map(f -> new DisponibilidadDTO(f.dayOfWeek(), f.startTime(), f.endTime()))
                .toList();
        return new TrabajadorDTO(id, null, null,
                principal == null ? null : principal.id(), principal == null ? null : principal.name(),
                secundarios, zoneIds == null ? List.of() : List.copyOf(zoneIds), null,
                primaryTrade == null ? null : primaryTrade.approxRate(), availableNow, disponibilidad, null);
    }
}
