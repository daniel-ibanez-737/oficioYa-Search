package edu.eci.dosw.oficioya.search.client;

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

    static final long PLOMERIA = 1L;
    static final long ELECTRICIDAD = 2L;
    static final long CARPINTERIA = 3L;
    static final long CLASES = 4L;
    static final long MASCOTAS = 5L;
    static final long TECNICO = 6L;

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
            trabajador(101L, "Carlos Martínez", PLOMERIA, "Plomería", List.of(TECNICO), List.of(CHAPINERO, USAQUEN),
                    "Chapinero", 45_000, true, ENTRE_SEMANA, 128),
            trabajador(102L, "Lucía Fernández", ELECTRICIDAD, "Electricidad", List.of(), List.of(CHAPINERO),
                    "Chapinero", 50_000, true, ENTRE_SEMANA, 96),
            trabajador(103L, "Miguel Torres", CARPINTERIA, "Carpintería", List.of(), List.of(SUBA), "Suba",
                    60_000, false, FIN_DE_SEMANA, 84),
            trabajador(104L, "Andrea Gómez", PLOMERIA, "Plomería", List.of(), List.of(KENNEDY), "Kennedy",
                    38_000, false, FIN_DE_SEMANA, 41),
            trabajador(105L, "Jorge Rodríguez", TECNICO, "Técnico de electrodomésticos", List.of(PLOMERIA, ELECTRICIDAD),
                    List.of(USAQUEN, SUBA), "Usaquén", 70_000, true, ENTRE_SEMANA_Y_NOCHES, 63),
            trabajador(106L, "Paula Ramírez", CLASES, "Profesor particular", List.of(), List.of(CHAPINERO, KENNEDY),
                    "Chapinero", 35_000, true, FIN_DE_SEMANA, 22),
            trabajador(107L, "Sofía Herrera", MASCOTAS, "Cuidado de mascotas", List.of(), List.of(USAQUEN),
                    "Usaquén", 25_000, true, FIN_DE_SEMANA, 57));

    private static final Map<Long, ReputacionDTO> REPUTACIONES = List.of(
                    new ReputacionDTO(101L, 4.9, 128), new ReputacionDTO(102L, 4.9, 96),
                    new ReputacionDTO(103L, 4.8, 84), new ReputacionDTO(104L, 4.2, 41),
                    new ReputacionDTO(105L, 4.6, 63), new ReputacionDTO(106L, 4.7, 22),
                    new ReputacionDTO(107L, 4.9, 57))
            .stream().collect(Collectors.toMap(ReputacionDTO::trabajadorId, Function.identity()));

    @SuppressWarnings("java:S107")
    private static TrabajadorDTO trabajador(Long id, String nombre, long oficioId, String oficio, List<Long> secundarios,
                                            List<Long> barrios, String zona, int tarifa, boolean disponibleAhora,
                                            List<DisponibilidadDTO> disponibilidad, int trabajos) {
        return new TrabajadorDTO(id, nombre, "https://picsum.photos/seed/" + id + "/200", oficioId, oficio,
                secundarios, barrios, zona, BigDecimal.valueOf(tarifa), disponibleAhora, disponibilidad, trabajos);
    }

    @Override
    public List<TrabajadorDTO> buscarTrabajadoresActivos(Long oficioId) {
        return TRABAJADORES.stream().filter(t -> t.ofrece(oficioId)).toList();
    }

    @Override
    public List<OficioDTO> obtenerOficios() {
        return OFICIOS;
    }

    @Override
    public Map<Long, ReputacionDTO> obtenerReputaciones(Collection<Long> trabajadorIds) {
        return trabajadorIds.stream().filter(REPUTACIONES::containsKey)
                .collect(Collectors.toMap(Function.identity(), REPUTACIONES::get));
    }

    @Override
    public String solicitarInterpretacionIA(String prompt) {
        // En modo demo se usa el intérprete simulado; si se configura la IA vía Orchestrator, se pide más detalle.
        return "{\"oficioId\": null, \"oficio\": null, \"confianza\": 0}";
    }
}
