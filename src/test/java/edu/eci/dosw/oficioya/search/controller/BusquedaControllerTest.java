package edu.eci.dosw.oficioya.search.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.eci.dosw.oficioya.search.config.SecurityConfig;
import edu.eci.dosw.oficioya.search.domain.CriteriosBusqueda;
import edu.eci.dosw.oficioya.search.domain.EstadoInterpretacion;
import edu.eci.dosw.oficioya.search.domain.TipoBusqueda;
import edu.eci.dosw.oficioya.search.dto.BusquedaResponse;
import edu.eci.dosw.oficioya.search.dto.OficioDTO;
import edu.eci.dosw.oficioya.search.dto.ResultadoBusquedaResponse;
import edu.eci.dosw.oficioya.search.exception.IntegracionException;
import edu.eci.dosw.oficioya.search.exception.SolicitudInvalidaException;
import edu.eci.dosw.oficioya.search.service.BusquedaService;

@WebMvcTest(BusquedaController.class)
@Import(SecurityConfig.class)
class BusquedaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BusquedaService servicio;

    private static BusquedaResponse respuestaConUnResultado() {
        ResultadoBusquedaResponse r = new ResultadoBusquedaResponse(1, 101L, "Carlos", null, "Plomería", "Chapinero",
                BigDecimal.valueOf(45000), 4.9, 128, 50, true);
        return new BusquedaResponse(TipoBusqueda.OFICIO, null, null, null, 1, List.of(r));
    }

    // ---------- Seguridad ----------

    @Test
    void sinJwtResponde401() throws Exception {
        mvc.perform(get("/api/search/oficios")).andExpect(status().isUnauthorized());
    }

    @Test
    void conJwtValidoResponde200() throws Exception {
        when(servicio.consultarOficios()).thenReturn(List.of(new OficioDTO(1L, "Plomería", null)));
        mvc.perform(get("/api/search/oficios").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Plomería"));
    }

    // ---------- Búsqueda por oficio ----------

    @Test
    void buscarPorOficioConFiltrosDevuelveElListadoSinDatosDeContacto() throws Exception {
        when(servicio.buscarPorOficio(eq("contratante-1"), any(CriteriosBusqueda.class)))
                .thenReturn(respuestaConUnResultado());

        mvc.perform(get("/api/search/oficios/1/trabajadores")
                        .param("precioMinimo", "30000").param("precioMaximo", "60000")
                        .param("diaSemana", "MONDAY").param("hora", "09:00")
                        .param("soloDisponibleAhora", "true").param("calificacionMinima", "4")
                        .param("barrioIds", "10", "11")
                        .with(jwt().jwt(j -> j.subject("contratante-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.resultados[0].nombreCompleto").value("Carlos"))
                .andExpect(jsonPath("$.resultados[0].correo").doesNotExist())
                .andExpect(jsonPath("$.resultados[0].telefono").doesNotExist());
    }

    @Test
    void calificacionMinimaFueraDeRangoResponde400() throws Exception {
        mvc.perform(get("/api/search/oficios/1/trabajadores").param("calificacionMinima", "7").with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criteriosIncoherentesResponden400() throws Exception {
        when(servicio.buscarPorOficio(any(), any())).thenThrow(new SolicitudInvalidaException("rango inválido"));
        mvc.perform(get("/api/search/oficios/1/trabajadores").with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("rango inválido"));
    }

    @Test
    void falloDeIntegracionResponde502() throws Exception {
        when(servicio.buscarPorOficio(any(), any())).thenThrow(new IntegracionException("caída", new RuntimeException()));
        mvc.perform(get("/api/search/oficios/1/trabajadores").with(jwt()))
                .andExpect(status().isBadGateway());
    }

    // ---------- Lenguaje natural ----------

    @Test
    void buscarEnLenguajeNatural() throws Exception {
        BusquedaResponse respuesta = new BusquedaResponse(TipoBusqueda.LENGUAJE_NATURAL,
                EstadoInterpretacion.OFICIO_IDENTIFICADO, "Plomería", null, 0, List.of());
        when(servicio.buscarPorLenguajeNatural(eq("contratante-1"), eq("arreglar una gotera"), any()))
                .thenReturn(respuesta);

        mvc.perform(post("/api/search/lenguaje-natural")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\": \"arreglar una gotera\", \"criterios\": {\"soloDisponibleAhora\": true}}")
                        .with(jwt().jwt(j -> j.subject("contratante-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("OFICIO_IDENTIFICADO"))
                .andExpect(jsonPath("$.oficioIdentificado").value("Plomería"));
    }

    @Test
    void lenguajeNaturalSinCriteriosTambienFunciona() throws Exception {
        when(servicio.buscarPorLenguajeNatural(any(), any(), any())).thenReturn(new BusquedaResponse(
                TipoBusqueda.LENGUAJE_NATURAL, EstadoInterpretacion.REQUIERE_MAS_DETALLE, null, "más detalle", 0,
                List.of()));
        mvc.perform(post("/api/search/lenguaje-natural")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\": \"hola amigos\"}")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REQUIERE_MAS_DETALLE"));
    }

    @Test
    void textoVacioResponde400() throws Exception {
        mvc.perform(post("/api/search/lenguaje-natural")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\": \"\"}")
                        .with(jwt()))
                .andExpect(status().isBadRequest());
    }
}
