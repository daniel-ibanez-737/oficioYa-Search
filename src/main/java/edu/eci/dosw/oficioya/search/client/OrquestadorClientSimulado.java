package edu.eci.dosw.oficioya.search.client;

import java.util.UUID;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import edu.eci.dosw.oficioya.search.dto.DisponibilidadDTO;
import edu.eci.dosw.oficioya.search.dto.OficioDTO;
import edu.eci.dosw.oficioya.search.dto.ReputacionDTO;
import edu.eci.dosw.oficioya.search.dto.TrabajadorDTO;

/**
 * Orchestrator simulado con datos de ejemplo (perfil {@code demo}).
 * Permite probar y mostrar Search en la review sin depender de que Worker, Reputation y el Orchestrator
 * de los otros squads estén disponibles. En este perfil se usa el intérprete de IA simulado.
 */
@Component
@Profile("demo")
public class OrquestadorClientSimulado implements OrquestadorClient {

    static final UUID PLOMERIA = id(1);
    static final UUID ELECTRICIDAD = id(2);
    static final UUID CARPINTERIA = id(3);
    static final UUID CLASES = id(4);
    static final UUID MASCOTAS = id(5);
    static final UUID TECNICO = id(6);

    private static final long CHAPINERO = 10L;
    private static final long USAQUEN = 11L;
    private static final long SUBA = 12L;
    private static final long KENNEDY = 13L;

    private static final List<OficioDTO> OFICIOS = List.of(
            new OficioDTO(PLOMERIA, "Plomería", "Fugas, tuberías, griferías y sanitarios"),
            new OficioDTO(ELECTRICIDAD, "Electricidad", "Instalaciones eléctricas, tomas e iluminación"),
            new OficioDTO(CARPINTERIA, "Carpintería", "Muebles, puertas y trabajos en madera"),
            new OficioDTO(CLASES, "Profesor particular", "Clases y refuerzo escolar"),
            new OficioDTO(MASCOTAS, "Cuidado de mascotas", "Paseo y cuidado de mascotas"),
            new OficioDTO(TECNICO, "Técnico de electrodomésticos", "Reparación de neveras, lavadoras y más"));

    private static final List<DisponibilidadDTO> ENTRE_SEMANA = List.of(
            new DisponibilidadDTO(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(17, 0)),
            new DisponibilidadDTO(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(17, 0)),
            new DisponibilidadDTO(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(17, 0)));
    // Entre semana y además los martes y jueves en la noche, para probar el filtro por franja horaria.
    private static final List<DisponibilidadDTO> ENTRE_SEMANA_Y_NOCHES = List.of(
            ENTRE_SEMANA.get(0), ENTRE_SEMANA.get(1), ENTRE_SEMANA.get(2),
            new DisponibilidadDTO(DayOfWeek.TUESDAY, LocalTime.of(18, 0), LocalTime.of(22, 0)),
            new DisponibilidadDTO(DayOfWeek.THURSDAY, LocalTime.of(18, 0), LocalTime.of(22, 0)));
    private static final List<DisponibilidadDTO> FIN_DE_SEMANA = List.of(
            new DisponibilidadDTO(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(14, 0)),
            new DisponibilidadDTO(DayOfWeek.SUNDAY, LocalTime.of(9, 0), LocalTime.of(13, 0)));

    private static final List<TrabajadorDTO> TRABAJADORES = List.of(
            trabajador(id(101), "Carlos Martínez", PLOMERIA, "Plomería", List.of(TECNICO), List.of(CHAPINERO, USAQUEN),
                    "Chapinero", 45_000, true, ENTRE_SEMANA, 128),
            trabajador(id(102), "Lucía Fernández", ELECTRICIDAD, "Electricidad", List.of(), List.of(CHAPINERO),
                    "Chapinero", 50_000, true, ENTRE_SEMANA, 96),
            trabajador(id(103), "Miguel Torres", CARPINTERIA, "Carpintería", List.of(), List.of(SUBA), "Suba",
                    60_000, false, FIN_DE_SEMANA, 84),
            trabajador(id(104), "Andrea Gómez", PLOMERIA, "Plomería", List.of(), List.of(KENNEDY), "Kennedy",
                    38_000, false, FIN_DE_SEMANA, 41),
            trabajador(id(105), "Jorge Rodríguez", TECNICO, "Técnico de electrodomésticos", List.of(PLOMERIA, ELECTRICIDAD),
                    List.of(USAQUEN, SUBA), "Usaquén", 70_000, true, ENTRE_SEMANA_Y_NOCHES, 63),
            trabajador(id(106), "Paula Ramírez", CLASES, "Profesor particular", List.of(), List.of(CHAPINERO, KENNEDY),
                    "Chapinero", 35_000, true, FIN_DE_SEMANA, 22),
            trabajador(id(107), "Sofía Herrera", MASCOTAS, "Cuidado de mascotas", List.of(), List.of(USAQUEN),
                    "Usaquén", 25_000, true, FIN_DE_SEMANA, 57));

    private static final Map<UUID, ReputacionDTO> REPUTACIONES = List.of(
                    new ReputacionDTO(id(101), 4.9, 128), new ReputacionDTO(id(102), 4.9, 96),
                    new ReputacionDTO(id(103), 4.8, 84), new ReputacionDTO(id(104), 4.2, 41),
                    new ReputacionDTO(id(105), 4.6, 63), new ReputacionDTO(id(106), 4.7, 22),
                    new ReputacionDTO(id(107), 4.9, 57))
            .stream().collect(Collectors.toMap(ReputacionDTO::trabajadorId, Function.identity()));

    /** Ids de demo legibles: id(101) = 00000000-0000-0000-0000-000000000101, igual que en Reputation. */
    static UUID id(long numero) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(numero));
    }

    @SuppressWarnings("java:S107")
    private static TrabajadorDTO trabajador(UUID id, String nombre, UUID oficioId, String oficio, List<UUID> secundarios,
                                            List<Long> barrios, String zona, int tarifa, boolean disponibleAhora,
                                            List<DisponibilidadDTO> disponibilidad, int trabajos) {
        return new TrabajadorDTO(id, nombre, "https://picsum.photos/seed/" + id.getLeastSignificantBits() + "/200", oficioId, oficio,
                secundarios, barrios, zona, BigDecimal.valueOf(tarifa), disponibleAhora, disponibilidad, trabajos);
    }

    @Override
    public List<TrabajadorDTO> buscarTrabajadoresActivos(UUID oficioId) {
        return TRABAJADORES.stream().filter(t -> t.ofrece(oficioId)).toList();
    }

    @Override
    public List<OficioDTO> obtenerOficios() {
        return OFICIOS;
    }

    @Override
    public Map<UUID, ReputacionDTO> obtenerReputaciones(Collection<UUID> trabajadorIds) {
        return trabajadorIds.stream().filter(REPUTACIONES::containsKey)
                .collect(Collectors.toMap(Function.identity(), REPUTACIONES::get));
    }

    @Override
    public String solicitarInterpretacionIA(String prompt) {
        // En modo demo se usa el intérprete simulado; si se configura la IA vía Orchestrator, se pide más detalle.
        return "{\"oficioId\": null, \"oficio\": null, \"confianza\": 0}";
    }
}
