package edu.eci.dosw.oficioya.search.client;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import edu.eci.dosw.oficioya.search.domain.InterpretacionIA;
import edu.eci.dosw.oficioya.search.dto.OficioDTO;
import edu.eci.dosw.oficioya.search.exception.IntegracionException;
import edu.eci.dosw.oficioya.search.service.InterpretadorLenguajeNatural;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Intérprete que consulta la IA (Gemini) a través del Orchestrator (modo {@code oficioya.ia.modo=orquestador}).
 * La IA solo debe identificar el oficio del catálogo; si no puede, Search pide más detalle al contratante.
 * Un fallo de la IA nunca afecta a otros dominios: se responde como "error" y se registra en el log.
 */
@Component
@ConditionalOnProperty(name = "oficioya.ia.modo", havingValue = "orquestador")
public class InterpretadorIAViaOrquestador implements InterpretadorLenguajeNatural {

    private static final Logger log = LoggerFactory.getLogger(InterpretadorIAViaOrquestador.class);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final OrquestadorClient orquestadorClient;

    public InterpretadorIAViaOrquestador(OrquestadorClient orquestadorClient) {
        this.orquestadorClient = orquestadorClient;
    }

    @Override
    public InterpretacionIA interpretar(String texto, List<OficioDTO> catalogo) {
        try {
            String respuesta = orquestadorClient.solicitarInterpretacionIA(construirPrompt(texto, catalogo));
            return convertirRespuesta(respuesta, catalogo);
        } catch (IntegracionException e) {
            log.error("Falló la consulta a la IA a través del Orchestrator: {}", e.getMessage());
            return InterpretacionIA.error();
        }
    }

    String construirPrompt(String texto, List<OficioDTO> catalogo) {
        String oficios = catalogo.stream()
                .map(o -> o.id() + ": " + o.nombre())
                .collect(Collectors.joining("\n"));
        return """
                Eres el asistente de búsqueda de OficioYa. Identifica ÚNICAMENTE el oficio que necesita la persona,
                eligiendo uno de este catálogo (id: nombre):
                %s

                Responde solo con JSON, sin texto adicional, con este formato:
                {"oficioId": <id o null>, "oficio": "<nombre o null>", "confianza": <número entre 0 y 1>}
                Si no es posible identificar un único oficio, usa oficioId null.

                Descripción de la persona: "%s"
                """.formatted(oficios, texto.replace("\"", "'"));
    }

    InterpretacionIA convertirRespuesta(String respuesta, List<OficioDTO> catalogo) {
        try {
            JsonNode json = JSON.readTree(extraerJson(respuesta));
            JsonNode oficioId = json.path("oficioId");
            if (!oficioId.isNumber()) {
                return InterpretacionIA.requiereMasDetalle();
            }
            long id = oficioId.asLong();
            double confianza = json.path("confianza").asDouble(0);
            return catalogo.stream()
                    .filter(o -> o.id() == id)
                    .findFirst()
                    .map(o -> InterpretacionIA.identificado(o.id(), o.nombre(), confianza))
                    .orElseGet(InterpretacionIA::requiereMasDetalle);
        } catch (JacksonException | IllegalArgumentException e) {
            log.error("La IA devolvió una respuesta que no se pudo interpretar: {}", e.getMessage());
            return InterpretacionIA.error();
        }
    }

    private static String extraerJson(String respuesta) {
        int inicio = respuesta == null ? -1 : respuesta.indexOf('{');
        int fin = respuesta == null ? -1 : respuesta.lastIndexOf('}');
        if (inicio < 0 || fin < inicio) {
            throw new IllegalArgumentException("La respuesta no contiene JSON");
        }
        return respuesta.substring(inicio, fin + 1);
    }
}
