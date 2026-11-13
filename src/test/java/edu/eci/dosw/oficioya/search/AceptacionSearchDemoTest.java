package edu.eci.dosw.oficioya.search;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de aceptación de Search de punta a punta con el perfil {@code demo}
 * (Orchestrator simulado con datos de ejemplo e intérprete de IA simulado).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class AceptacionSearchDemoTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void rf1Rf2DescribirEnLenguajeNaturalSugiereLosMejoresPlomeros() throws Exception {
        mvc.perform(post("/api/v1/search/lenguaje-natural")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\": \"Necesito alguien que arregle una gotera\"}")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("OFICIO_IDENTIFICADO"))
                .andExpect(jsonPath("$.oficioIdentificado").value("Plomería"))
                // Carlos (4.9, oficio principal), Jorge (4.6, plomería como oficio secundario), Andrea (4.2)
                .andExpect(jsonPath("$.resultados[0].nombreCompleto").value("Carlos Martínez"))
                .andExpect(jsonPath("$.resultados[1].nombreCompleto").value("Jorge Rodríguez"))
                .andExpect(jsonPath("$.resultados[2].nombreCompleto").value("Andrea Gómez"));
    }

    @Test
    void rf2SiNoSeEntiendeLaDescripcionSePideMasDetalle() throws Exception {
        mvc.perform(post("/api/v1/search/lenguaje-natural")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\": \"necesito ayuda con algo\"}")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REQUIERE_MAS_DETALLE"))
                .andExpect(jsonPath("$.mensaje").isNotEmpty())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void rf3BuscarPorOficioIncluyeOficiosSecundarios() throws Exception {
        mvc.perform(get("/api/v1/search/oficios/2/trabajadores").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.resultados[0].nombreCompleto").value("Lucía Fernández"))
                .andExpect(jsonPath("$.resultados[1].nombreCompleto").value("Jorge Rodríguez"));
    }

    @Test
    void rf4Rf5Rf6FiltrosCombinados() throws Exception {
        mvc.perform(get("/api/v1/search/oficios/1/trabajadores")
                        .param("precioMaximo", "50000")
                        .param("soloDisponibleAhora", "true")
                        .param("diaSemana", "MONDAY").param("hora", "10:00")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.resultados[0].nombreCompleto").value("Carlos Martínez"));
    }

    @Test
    void ordenaPorCalificacionDeMenorAMayor() throws Exception {
        mvc.perform(get("/api/v1/search/oficios/1/trabajadores").param("orden", "MENOR_A_MAYOR").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultados[0].nombreCompleto").value("Andrea Gómez"))
                .andExpect(jsonPath("$.resultados[1].nombreCompleto").value("Jorge Rodríguez"))
                .andExpect(jsonPath("$.resultados[2].nombreCompleto").value("Carlos Martínez"))
                .andExpect(jsonPath("$.resultados[0].posicion").value(1));
    }

    @Test
    void filtraPorFranjaHoraria() throws Exception {
        mvc.perform(get("/api/v1/search/oficios/1/trabajadores").param("franjaHoraria", "NOCHE").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.resultados[0].nombreCompleto").value("Jorge Rodríguez"));
        mvc.perform(get("/api/v1/search/oficios/1/trabajadores").param("franjaHoraria", "MADRUGADA").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void franjaHorariaInvalidaResponde400() throws Exception {
        mvc.perform(get("/api/v1/search/oficios/1/trabajadores").param("franjaHoraria", "MEDIODIA").with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rf3ListarOficios() throws Exception {
        mvc.perform(get("/api/v1/search/oficios").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void sinJwtNoSePuedeBuscar() throws Exception {
        mvc.perform(get("/api/v1/search/oficios")).andExpect(status().isUnauthorized());
    }
}
