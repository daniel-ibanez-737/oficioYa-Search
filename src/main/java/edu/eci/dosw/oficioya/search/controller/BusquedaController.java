package edu.eci.dosw.oficioya.search.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import edu.eci.dosw.oficioya.search.domain.CriteriosBusqueda;
import edu.eci.dosw.oficioya.search.dto.BusquedaLenguajeNaturalRequest;
import edu.eci.dosw.oficioya.search.dto.BusquedaResponse;
import edu.eci.dosw.oficioya.search.dto.CriteriosBusquedaRequest;
import edu.eci.dosw.oficioya.search.dto.OficioDTO;
import edu.eci.dosw.oficioya.search.service.BusquedaService;
import jakarta.validation.Valid;

/**
 * API de Search. Llega siempre a través del Orchestrator y requiere un JWT válido.
 * Solo devuelve el listado de trabajadores (el perfil completo es responsabilidad de Worker).
 */
@Tag(name = "Búsqueda", description = "Búsqueda de trabajadores (RF-01 a RF-06)")
@RestController
@RequestMapping("/api/v1/search")
@Validated
public class BusquedaController {

    private final BusquedaService busquedaService;

    public BusquedaController(BusquedaService busquedaService) {
        this.busquedaService = busquedaService;
    }

    /**
     * Catálogo de oficios para buscar por oficio.
     */
    @Operation(summary = "Catálogo de oficios")
    @GetMapping("/oficios")
    public List<OficioDTO> listarOficios() {
        return busquedaService.consultarOficios();
    }

    /**
     * Búsqueda por oficio con filtros opcionales combinados.
     * Ejemplo: {@code GET /api/v1/search/oficios/1/trabajadores?precioMinimo=30000&precioMaximo=60000&soloDisponibleAhora=true}
     */
    @Operation(summary = "Buscar trabajadores por oficio (principal o secundario) con filtros")
    @GetMapping("/oficios/{oficioId}/trabajadores")
    public BusquedaResponse buscarPorOficio(@PathVariable Long oficioId,
                                            @Valid @ModelAttribute CriteriosBusquedaRequest criterios,
                                            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return busquedaService.buscarPorOficio(usuario(jwt), criterios.aCriterios(oficioId));
    }

    /**
     * Búsqueda en lenguaje natural: la IA identifica el oficio y se sugieren los trabajadores
     * con mayor reputación en ese oficio.
     */
    @Operation(summary = "Buscar describiendo la necesidad en lenguaje natural (IA)")
    @PostMapping("/lenguaje-natural")
    public BusquedaResponse buscarPorLenguajeNatural(@Valid @RequestBody BusquedaLenguajeNaturalRequest solicitud,
                                                     @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        CriteriosBusqueda criterios = solicitud.criterios() == null
                ? CriteriosBusqueda.soloOficio(null)
                : solicitud.criterios().aCriterios(null);
        return busquedaService.buscarPorLenguajeNatural(usuario(jwt), solicitud.texto(), criterios);
    }

    private static String usuario(Jwt jwt) {
        return jwt == null ? "desconocido" : jwt.getSubject();
    }
}
