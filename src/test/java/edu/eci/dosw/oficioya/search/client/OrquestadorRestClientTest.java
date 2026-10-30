package edu.eci.dosw.oficioya.search.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import edu.eci.dosw.oficioya.search.dto.OficioDTO;
import edu.eci.dosw.oficioya.search.dto.ReputacionDTO;
import edu.eci.dosw.oficioya.search.dto.TrabajadorDTO;
import edu.eci.dosw.oficioya.search.exception.IntegracionException;

class OrquestadorRestClientTest {

    private static final String BASE = "http://orquestador";

    private MockRestServiceServer servidor;
    private OrquestadorRestClient cliente;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new OrquestadorRestClient(builder, BASE, "/api/workers/trabajadores/activos", "/api/workers/oficios",
                "/api/reputation/reputaciones", "/api/ia/interpretaciones");
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void consultaTrabajadoresActivosPorElOrquestadorYReenviaElJwt() {
        Jwt jwt = Jwt.withTokenValue("token-del-usuario").header("alg", "HS256").subject("contratante-1").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        servidor.expect(requestTo(BASE + "/api/workers/trabajadores/activos?oficioId=1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token-del-usuario"))
                .andRespond(withSuccess("""
                        [{"id": 101, "nombreCompleto": "Carlos", "oficioPrincipalId": 1, "oficioPrincipal": "Plomería",
                          "correo": "no-debe-llegar@correo.com", "tarifaAproximada": 45000, "disponibleAhora": true,
                          "disponibilidad": [{"diaSemana": "MONDAY", "horaInicio": "08:00", "horaFin": "17:00"}],
                          "trabajosCompletados": 12}]
                        """, MediaType.APPLICATION_JSON));

        List<TrabajadorDTO> trabajadores = cliente.buscarTrabajadoresActivos(1L);

        assertThat(trabajadores).hasSize(1);
        assertThat(trabajadores.get(0).nombreCompleto()).isEqualTo("Carlos");
        assertThat(trabajadores.get(0).disponibilidad().get(0).diaSemana()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(trabajadores.get(0).disponibilidad().get(0).horaInicio()).isEqualTo(LocalTime.of(8, 0));
        servidor.verify();
    }

    @Test
    void consultaElCatalogoDeOficios() {
        servidor.expect(requestTo(BASE + "/api/workers/oficios"))
                .andRespond(withSuccess("[{\"id\": 1, \"nombre\": \"Plomería\"}]", MediaType.APPLICATION_JSON));
        assertThat(cliente.obtenerOficios()).containsExactly(new OficioDTO(1L, "Plomería", null));
    }

    @Test
    void consultaReputacionesYLasIndexaPorTrabajador() {
        servidor.expect(requestTo(BASE + "/api/reputation/reputaciones?trabajadorIds=101&trabajadorIds=102"))
                .andRespond(withSuccess("""
                        [{"trabajadorId": 101, "reputacion": 4.9, "totalResenas": 128},
                         {"trabajadorId": 102, "reputacion": 4.1, "totalResenas": 7}]
                        """, MediaType.APPLICATION_JSON));
        Map<Long, ReputacionDTO> reputaciones = cliente.obtenerReputaciones(List.of(101L, 102L));
        assertThat(reputaciones).containsKeys(101L, 102L);
        assertThat(reputaciones.get(101L).reputacion()).isEqualTo(4.9);
    }

    @Test
    void sinTrabajadoresNoConsultaReputaciones() {
        assertThat(cliente.obtenerReputaciones(List.of())).isEmpty();
        servidor.verify();
    }

    @Test
    void enviaElPromptALaIAPorElOrquestador() {
        servidor.expect(requestTo(BASE + "/api/ia/interpretaciones"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"respuesta\": \"{\\\"oficioId\\\": 1}\"}", MediaType.APPLICATION_JSON));
        assertThat(cliente.solicitarInterpretacionIA("prompt")).isEqualTo("{\"oficioId\": 1}");
    }

    @Test
    void respuestasVaciasSeConviertenEnListasVacias() {
        servidor.expect(requestTo(BASE + "/api/workers/trabajadores/activos?oficioId=1")).andRespond(withSuccess());
        servidor.expect(requestTo(BASE + "/api/workers/oficios")).andRespond(withSuccess());
        servidor.expect(requestTo(BASE + "/api/reputation/reputaciones?trabajadorIds=1")).andRespond(withSuccess());
        servidor.expect(requestTo(BASE + "/api/ia/interpretaciones")).andRespond(withSuccess());
        assertThat(cliente.buscarTrabajadoresActivos(1L)).isEmpty();
        assertThat(cliente.obtenerOficios()).isEmpty();
        assertThat(cliente.obtenerReputaciones(List.of(1L))).isEmpty();
        assertThat(cliente.solicitarInterpretacionIA("p")).isEmpty();
    }

    @Test
    void losErroresDelOrquestadorSeConviertenEnIntegracionException() {
        servidor.expect(requestTo(BASE + "/api/workers/trabajadores/activos?oficioId=1")).andRespond(withServerError());
        servidor.expect(requestTo(BASE + "/api/workers/oficios")).andRespond(withServerError());
        servidor.expect(requestTo(BASE + "/api/reputation/reputaciones?trabajadorIds=1")).andRespond(withServerError());
        servidor.expect(requestTo(BASE + "/api/ia/interpretaciones")).andRespond(withServerError());
        assertThatThrownBy(() -> cliente.buscarTrabajadoresActivos(1L)).isInstanceOf(IntegracionException.class);
        assertThatThrownBy(() -> cliente.obtenerOficios()).isInstanceOf(IntegracionException.class);
        assertThatThrownBy(() -> cliente.obtenerReputaciones(List.of(1L))).isInstanceOf(IntegracionException.class);
        assertThatThrownBy(() -> cliente.solicitarInterpretacionIA("p")).isInstanceOf(IntegracionException.class);
    }
}
