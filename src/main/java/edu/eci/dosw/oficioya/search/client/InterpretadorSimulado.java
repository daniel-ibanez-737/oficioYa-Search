package edu.eci.dosw.oficioya.search.client;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import edu.eci.dosw.oficioya.search.domain.InterpretacionIA;
import edu.eci.dosw.oficioya.search.dto.OficioDTO;
import edu.eci.dosw.oficioya.search.service.InterpretadorLenguajeNatural;

/**
 * Intérprete simulado por palabras clave (modo {@code oficioya.ia.modo=simulado}, por defecto en el sprint 3).
 * Identifica el oficio buscando el nombre del oficio o palabras asociadas en el texto. Se reemplaza por
 * {@link InterpretadorIAViaOrquestador} cuando el Orchestrator exponga la ruta hacia la IA (sprint 4).
 */
@Component
@ConditionalOnProperty(name = "oficioya.ia.modo", havingValue = "simulado", matchIfMissing = true)
public class InterpretadorSimulado implements InterpretadorLenguajeNatural {

    static final double CONFIANZA_NOMBRE = 0.9;
    static final double CONFIANZA_PALABRA_CLAVE = 0.75;

    /** Raíz del nombre del oficio → palabras con las que la gente suele describir esa necesidad. */
    private static final Map<String, List<String>> PALABRAS_CLAVE = new LinkedHashMap<>();

    static {
        PALABRAS_CLAVE.put("plomer", List.of("gotera", "fuga", "tuberia", "tubo", "lavamanos", "inodoro", "sanitario",
                "ducha", "grifo", "desague", "lavaplatos", "plomero"));
        PALABRAS_CLAVE.put("electric", List.of("electricista", "luz", "enchufe", "toma", "cable", "corto", "breaker",
                "bombillo", "lampara", "interruptor"));
        PALABRAS_CLAVE.put("carpinter", List.of("carpintero", "mueble", "madera", "puerta", "closet", "armario",
                "repisa"));
        PALABRAS_CLAVE.put("profesor", List.of("profesor", "clase", "tarea", "matematicas", "ingles", "fisica",
                "quimica", "tutor"));
        PALABRAS_CLAVE.put("mascota", List.of("mascota", "perro", "gato", "pasear", "cuidador", "guarderia"));
        PALABRAS_CLAVE.put("tecnic", List.of("tecnico", "nevera", "lavadora", "televisor", "computador", "horno",
                "microondas", "electrodomestico"));
    }

    @Override
    public InterpretacionIA interpretar(String texto, List<OficioDTO> catalogo) {
        String normalizado = normalizar(texto);
        Map<OficioDTO, Double> coincidencias = new LinkedHashMap<>();
        for (OficioDTO oficio : catalogo) {
            double confianza = confianza(normalizado, normalizar(oficio.nombre()));
            if (confianza > 0) {
                coincidencias.put(oficio, confianza);
            }
        }
        if (coincidencias.size() != 1) {
            // Ningún oficio o varios a la vez: la descripción es ambigua, se pide más detalle.
            return InterpretacionIA.requiereMasDetalle();
        }
        Map.Entry<OficioDTO, Double> unica = coincidencias.entrySet().iterator().next();
        return InterpretacionIA.identificado(unica.getKey().id(), unica.getKey().nombre(), unica.getValue());
    }

    private double confianza(String texto, String nombreOficio) {
        Set<String> palabrasDelTexto = new LinkedHashSet<>(List.of(texto.split("\\W+")));
        for (Map.Entry<String, List<String>> entrada : PALABRAS_CLAVE.entrySet()) {
            String raiz = entrada.getKey();
            if (!nombreOficio.contains(raiz)) {
                continue;
            }
            if (palabrasDelTexto.stream().anyMatch(p -> p.startsWith(raiz))) {
                return CONFIANZA_NOMBRE;
            }
            if (entrada.getValue().stream().anyMatch(clave -> palabrasDelTexto.stream().anyMatch(p -> p.startsWith(clave)))) {
                return CONFIANZA_PALABRA_CLAVE;
            }
        }
        return 0;
    }

    static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }
}
