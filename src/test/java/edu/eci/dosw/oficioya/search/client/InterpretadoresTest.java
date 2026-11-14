package edu.eci.dosw.oficioya.search.client;


import static edu.eci.dosw.oficioya.search.IdsDePrueba.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.oficioya.search.domain.EstadoInterpretacion;
import edu.eci.dosw.oficioya.search.domain.InterpretacionIA;
import edu.eci.dosw.oficioya.search.dto.OficioDTO;
import edu.eci.dosw.oficioya.search.exception.IntegracionException;

class InterpretadoresTest {

    private static final List<OficioDTO> CATALOGO = List.of(
            new OficioDTO(id(1), "Plomería", null),
            new OficioDTO(id(2), "Electricidad", null),
            new OficioDTO(id(6), "Técnico de electrodomésticos", null));

    // ---------- Intérprete simulado ----------

    private final InterpretadorSimulado simulado = new InterpretadorSimulado();

    @Test
    void simuladoIdentificaElOficioPorPalabraClave() {
        InterpretacionIA r = simulado.interpretar("Necesito alguien que arregle una gotera", CATALOGO);
        assertThat(r.estado()).isEqualTo(EstadoInterpretacion.OFICIO_IDENTIFICADO);
        assertThat(r.oficioId()).isEqualTo(id(1));
        assertThat(r.nivelConfianza()).isEqualTo(InterpretadorSimulado.CONFIANZA_PALABRA_CLAVE);
    }

    @Test
    void simuladoIdentificaElOficioPorSuNombreSinImportarTildes() {
        InterpretacionIA r = simulado.interpretar("busco un PLOMERO urgente", CATALOGO);
        assertThat(r.oficioId()).isEqualTo(id(1));
        assertThat(r.nivelConfianza()).isEqualTo(InterpretadorSimulado.CONFIANZA_NOMBRE);
    }

    @Test
    void simuladoPideMasDetalleSiNoEntiende() {
        InterpretacionIA r = simulado.interpretar("necesito ayuda con algo", CATALOGO);
        assertThat(r.estado()).isEqualTo(EstadoInterpretacion.REQUIERE_MAS_DETALLE);
    }

    @Test
    void simuladoPideMasDetalleSiLaDescripcionEsAmbigua() {
        InterpretacionIA r = simulado.interpretar("tengo una fuga y además se fue la luz", CATALOGO);
        assertThat(r.estado()).isEqualTo(EstadoInterpretacion.REQUIERE_MAS_DETALLE);
    }

    @Test
    void normalizarQuitaTildesYMayusculas() {
        assertThat(InterpretadorSimulado.normalizar("Técnico ÉLÉCTRICO")).isEqualTo("tecnico electrico");
    }

    // ---------- Intérprete vía Orchestrator ----------

    private final OrquestadorClient orquestador = mock(OrquestadorClient.class);
    private final InterpretadorIAViaOrquestador viaOrquestador = new InterpretadorIAViaOrquestador(orquestador);

    @Test
    void viaOrquestadorConvierteLaRespuestaDeLaIA() {
        when(orquestador.solicitarInterpretacionIA(anyString()))
                .thenReturn("```json\n{\"oficioId\": \"00000000-0000-0000-0000-000000000002\", \"oficio\": \"Electricidad\", \"confianza\": 0.92}\n```");
        InterpretacionIA r = viaOrquestador.interpretar("se fue la luz", CATALOGO);
        assertThat(r.estado()).isEqualTo(EstadoInterpretacion.OFICIO_IDENTIFICADO);
        assertThat(r.oficioId()).isEqualTo(id(2));
        assertThat(r.oficioIdentificado()).isEqualTo("Electricidad");
        assertThat(r.nivelConfianza()).isEqualTo(0.92);
    }

    @Test
    void viaOrquestadorPideMasDetalleSiLaIANoIdentificaElOficio() {
        when(orquestador.solicitarInterpretacionIA(anyString())).thenReturn("{\"oficioId\": null}");
        assertThat(viaOrquestador.interpretar("hola", CATALOGO).estado())
                .isEqualTo(EstadoInterpretacion.REQUIERE_MAS_DETALLE);
    }

    @Test
    void viaOrquestadorIgnoraOficiosQueNoEstanEnElCatalogo() {
        when(orquestador.solicitarInterpretacionIA(anyString())).thenReturn("{\"oficioId\": \"00000000-0000-0000-0000-000000000099\", \"confianza\": 0.9}");
        assertThat(viaOrquestador.interpretar("algo", CATALOGO).estado())
                .isEqualTo(EstadoInterpretacion.REQUIERE_MAS_DETALLE);
    }

    @Test
    void viaOrquestadorDevuelveErrorSiLaRespuestaNoEsJson() {
        when(orquestador.solicitarInterpretacionIA(anyString())).thenReturn("no entendí");
        assertThat(viaOrquestador.interpretar("algo", CATALOGO).estado()).isEqualTo(EstadoInterpretacion.ERROR);
    }

    @Test
    void viaOrquestadorDevuelveErrorSiElJsonEstaMalFormado() {
        when(orquestador.solicitarInterpretacionIA(anyString())).thenReturn("{\"oficioId\": }");
        assertThat(viaOrquestador.interpretar("algo", CATALOGO).estado()).isEqualTo(EstadoInterpretacion.ERROR);
    }

    @Test
    void viaOrquestadorDevuelveErrorSiFallaLaIntegracion() {
        when(orquestador.solicitarInterpretacionIA(anyString()))
                .thenThrow(new IntegracionException("caída", new RuntimeException()));
        assertThat(viaOrquestador.interpretar("algo", CATALOGO).estado()).isEqualTo(EstadoInterpretacion.ERROR);
    }

    @Test
    void elPromptIncluyeElCatalogoYLaDescripcion() {
        String prompt = viaOrquestador.construirPrompt("arreglar \"gotera\"", CATALOGO);
        assertThat(prompt).contains(id(1) + ": Plomería", id(2) + ": Electricidad", "arreglar 'gotera'", "oficioId");
    }
}
