package edu.eci.dosw.oficioya.search.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.oficioya.search.domain.filtro.FiltroCompuesto;
import edu.eci.dosw.oficioya.search.exception.SolicitudInvalidaException;

class CriteriosYOrdenamientoTest {

    @Test
    void sinCriteriosNoSeAgreganFiltros() {
        FiltroCompuesto filtro = (FiltroCompuesto) CriteriosBusqueda.soloOficio(1L).construirFiltro();
        assertThat(filtro.cantidad()).isZero();
    }

    @Test
    void conTodosLosCriteriosSeAgreganCincoFiltros() {
        CriteriosBusqueda criterios = new CriteriosBusqueda(1L, List.of(10L), BigDecimal.ONE, BigDecimal.TEN,
                DayOfWeek.MONDAY, LocalTime.NOON, true, 4.0);
        FiltroCompuesto filtro = (FiltroCompuesto) criterios.construirFiltro();
        assertThat(filtro.cantidad()).isEqualTo(5);
    }

    @Test
    void conOficioConservaLosDemasCriterios() {
        CriteriosBusqueda base = new CriteriosBusqueda(null, List.of(10L), null, BigDecimal.TEN, null, null, true, 3.5);
        CriteriosBusqueda conOficio = base.conOficio(7L);
        assertThat(conOficio.oficioId()).isEqualTo(7L);
        assertThat(conOficio.barrioIds()).containsExactly(10L);
        assertThat(conOficio.soloDisponibleAhora()).isTrue();
        assertThat(conOficio.calificacionMinima()).isEqualTo(3.5);
    }

    @Test
    void rechazaRangoDePrecioInvertido() {
        assertThatThrownBy(() -> new CriteriosBusqueda(1L, null, BigDecimal.TEN, BigDecimal.ONE, null, null, false, null))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("precio mínimo");
    }

    @Test
    void rechazaHoraSinDia() {
        assertThatThrownBy(() -> new CriteriosBusqueda(1L, null, null, null, null, LocalTime.NOON, false, null))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    private static ResultadoBusqueda resultado(long id, double calificacion, int trabajos, int resenas) {
        return new ResultadoBusqueda(id, "T" + id, null, "Plomería", "Zona", null, BigDecimal.ONE, calificacion,
                resenas, trabajos, false, null);
    }

    @Test
    void ordenaPorCalificacionLuegoTrabajosLuegoResenas() {
        ResultadoBusqueda a = resultado(1, 4.5, 10, 5);
        ResultadoBusqueda b = resultado(2, 4.9, 1, 1);
        ResultadoBusqueda c = resultado(3, 4.5, 20, 1);
        ResultadoBusqueda d = resultado(4, 4.5, 20, 9);

        List<ResultadoBusqueda> ordenados = new OrdenamientoPorReputacion().ordenar(List.of(a, b, c, d));

        assertThat(ordenados).extracting(ResultadoBusqueda::getTrabajadorId).containsExactly(2L, 4L, 3L, 1L);
        assertThat(ordenados).extracting(ResultadoBusqueda::getPosicion).containsExactly(1, 2, 3, 4);
    }

    @Test
    void interpretacionSoloEsConfiableSiSeIdentificoConSuficienteConfianza() {
        assertThat(InterpretacionIA.identificado(1L, "Plomería", 0.8).esConfiable(0.6)).isTrue();
        assertThat(InterpretacionIA.identificado(1L, "Plomería", 0.5).esConfiable(0.6)).isFalse();
        assertThat(InterpretacionIA.requiereMasDetalle().esConfiable(0.0)).isFalse();
        assertThat(InterpretacionIA.error().estado()).isEqualTo(EstadoInterpretacion.ERROR);
        assertThat(InterpretacionIA.requiereMasDetalle().mensajeAclaracion()).isNotBlank();
    }

    @Test
    void elResumenDelLogNoIncluyeElTextoDelContratante() {
        Busqueda busqueda = new Busqueda("contratante-1", TipoBusqueda.LENGUAJE_NATURAL, 1L, 3, 12, null);
        assertThat(busqueda.resumen()).contains("tipo=LENGUAJE_NATURAL", "usuario=contratante-1", "resultados=3");
    }
}
