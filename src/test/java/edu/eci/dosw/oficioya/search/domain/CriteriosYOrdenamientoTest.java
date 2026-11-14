package edu.eci.dosw.oficioya.search.domain;

import java.util.UUID;

import static edu.eci.dosw.oficioya.search.IdsDePrueba.id;

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
        FiltroCompuesto filtro = (FiltroCompuesto) CriteriosBusqueda.soloOficio(id(1)).construirFiltro();
        assertThat(filtro.cantidad()).isZero();
    }

    @Test
    void conTodosLosCriteriosSeAgreganCincoFiltros() {
        CriteriosBusqueda criterios = new CriteriosBusqueda(id(1), List.of(10L), BigDecimal.ONE, BigDecimal.TEN,
                DayOfWeek.MONDAY, LocalTime.NOON, true, 4.0);
        FiltroCompuesto filtro = (FiltroCompuesto) criterios.construirFiltro();
        assertThat(filtro.cantidad()).isEqualTo(5);
    }

    @Test
    void conOficioConservaLosDemasCriterios() {
        CriteriosBusqueda base = new CriteriosBusqueda(null, List.of(10L), null, BigDecimal.TEN, null, null, true, 3.5);
        CriteriosBusqueda conOficio = base.conOficio(id(7));
        assertThat(conOficio.oficioId()).isEqualTo(id(7));
        assertThat(conOficio.barrioIds()).containsExactly(10L);
        assertThat(conOficio.soloDisponibleAhora()).isTrue();
        assertThat(conOficio.calificacionMinima()).isEqualTo(3.5);
    }

    @Test
    void rechazaRangoDePrecioInvertido() {
        UUID id1 = id(1);
        assertThatThrownBy(() -> new CriteriosBusqueda(id1, null, BigDecimal.TEN, BigDecimal.ONE, null, null, false, null))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("precio mínimo");
    }

    @Test
    void rechazaHoraSinDia() {
        UUID id1 = id(1);
        assertThatThrownBy(() -> new CriteriosBusqueda(id1, null, null, null, null, LocalTime.NOON, false, null))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    private static ResultadoBusqueda resultado(long numero, double calificacion, int trabajos, int resenas) {
        return new ResultadoBusqueda(id(numero), "T" + numero, null, "Plomería", "Zona", null, BigDecimal.ONE, calificacion,
                resenas, trabajos, false, null);
    }

    @Test
    void ordenaPorCalificacionLuegoTrabajosLuegoResenas() {
        ResultadoBusqueda a = resultado(1, 4.5, 10, 5);
        ResultadoBusqueda b = resultado(2, 4.9, 1, 1);
        ResultadoBusqueda c = resultado(3, 4.5, 20, 1);
        ResultadoBusqueda d = resultado(4, 4.5, 20, 9);

        List<ResultadoBusqueda> ordenados = new OrdenamientoPorReputacion().ordenar(List.of(a, b, c, d));

        assertThat(ordenados).extracting(ResultadoBusqueda::getTrabajadorId).containsExactly(id(2), id(4), id(3), id(1));
        assertThat(ordenados).extracting(ResultadoBusqueda::getPosicion).containsExactly(1, 2, 3, 4);
    }

    @Test
    void ordenaDeMenorAMayorCalificacionConElMismoDesempate() {
        ResultadoBusqueda a = resultado(1, 4.5, 10, 5);
        ResultadoBusqueda b = resultado(2, 4.9, 1, 1);
        ResultadoBusqueda c = resultado(3, 4.5, 20, 1);
        ResultadoBusqueda d = resultado(4, 3.8, 2, 2);

        List<ResultadoBusqueda> ordenados = new OrdenamientoPorReputacion(OrdenCalificacion.MENOR_A_MAYOR)
                .ordenar(List.of(a, b, c, d));

        assertThat(ordenados).extracting(ResultadoBusqueda::getTrabajadorId).containsExactly(id(4), id(3), id(1), id(2));
    }

    @Test
    void sinOrdenSeUsaDeMayorAMenorYLaFranjaAgregaUnFiltro() {
        CriteriosBusqueda criterios = new CriteriosBusqueda(id(1), null, null, null, null, null, false, null,
                FranjaHoraria.TARDE, null);
        assertThat(criterios.orden()).isEqualTo(OrdenCalificacion.MAYOR_A_MENOR);
        assertThat(((FiltroCompuesto) criterios.construirFiltro()).cantidad()).isEqualTo(1);
        assertThat(criterios.conOficio(id(2)).franjaHoraria()).isEqualTo(FranjaHoraria.TARDE);
    }

    @Test
    void interpretacionSoloEsConfiableSiSeIdentificoConSuficienteConfianza() {
        assertThat(InterpretacionIA.identificado(id(1), "Plomería", 0.8).esConfiable(0.6)).isTrue();
        assertThat(InterpretacionIA.identificado(id(1), "Plomería", 0.5).esConfiable(0.6)).isFalse();
        assertThat(InterpretacionIA.requiereMasDetalle().esConfiable(0.0)).isFalse();
        assertThat(InterpretacionIA.error().estado()).isEqualTo(EstadoInterpretacion.ERROR);
        assertThat(InterpretacionIA.requiereMasDetalle().mensajeAclaracion()).isNotBlank();
    }

    @Test
    void elResumenDelLogNoIncluyeElTextoDelContratante() {
        Busqueda busqueda = new Busqueda("contratante-1", TipoBusqueda.LENGUAJE_NATURAL, id(1), 3, 12, null);
        assertThat(busqueda.resumen()).contains("tipo=LENGUAJE_NATURAL", "usuario=contratante-1", "resultados=3");
    }
}
