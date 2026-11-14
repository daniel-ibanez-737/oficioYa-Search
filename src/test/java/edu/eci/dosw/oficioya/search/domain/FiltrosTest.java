package edu.eci.dosw.oficioya.search.domain;


import static edu.eci.dosw.oficioya.search.IdsDePrueba.id;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.oficioya.search.domain.filtro.FiltroCalificacionMinima;
import edu.eci.dosw.oficioya.search.domain.filtro.FiltroCompuesto;
import edu.eci.dosw.oficioya.search.domain.filtro.FiltroDisponibilidadSemanal;
import edu.eci.dosw.oficioya.search.domain.filtro.FiltroDisponibleAhora;
import edu.eci.dosw.oficioya.search.domain.filtro.FiltroFranjaHoraria;
import edu.eci.dosw.oficioya.search.domain.filtro.FiltroRangoPrecio;
import edu.eci.dosw.oficioya.search.domain.filtro.FiltroZona;

class FiltrosTest {

    private static ResultadoBusqueda trabajador(Set<Long> barrios, String tarifa, double calificacion,
                                                boolean disponibleAhora) {
        List<FranjaDisponibilidad> lunes = List.of(
                new FranjaDisponibilidad(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)));
        return new ResultadoBusqueda(id(1), "Ana", null, "Plomería", "Chapinero", barrios,
                tarifa == null ? null : new BigDecimal(tarifa), calificacion, 10, 5, disponibleAhora, lunes);
    }

    @Test
    void filtroZonaCumpleSiCubreAlgunBarrio() {
        ResultadoBusqueda r = trabajador(Set.of(10L, 11L), "40000", 4.5, true);
        assertThat(new FiltroZona(List.of(11L, 99L)).cumple(r)).isTrue();
        assertThat(new FiltroZona(List.of(99L)).cumple(r)).isFalse();
    }

    @Test
    void filtroRangoPrecioRespetaLimites() {
        ResultadoBusqueda r = trabajador(Set.of(), "40000", 4.5, true);
        assertThat(new FiltroRangoPrecio(new BigDecimal("30000"), new BigDecimal("50000")).cumple(r)).isTrue();
        assertThat(new FiltroRangoPrecio(new BigDecimal("40000"), new BigDecimal("40000")).cumple(r)).isTrue();
        assertThat(new FiltroRangoPrecio(new BigDecimal("45000"), null).cumple(r)).isFalse();
        assertThat(new FiltroRangoPrecio(null, new BigDecimal("35000")).cumple(r)).isFalse();
        assertThat(new FiltroRangoPrecio(null, null).cumple(r)).isTrue();
    }

    @Test
    void filtroRangoPrecioDescartaTrabajadoresSinTarifa() {
        ResultadoBusqueda sinTarifa = trabajador(Set.of(), null, 4.5, true);
        assertThat(new FiltroRangoPrecio(BigDecimal.ONE, null).cumple(sinTarifa)).isFalse();
    }

    @Test
    void filtroDisponibleAhora() {
        assertThat(new FiltroDisponibleAhora().cumple(trabajador(Set.of(), "1", 4, true))).isTrue();
        assertThat(new FiltroDisponibleAhora().cumple(trabajador(Set.of(), "1", 4, false))).isFalse();
    }

    @Test
    void filtroDisponibilidadSemanalPorDiaYHora() {
        ResultadoBusqueda r = trabajador(Set.of(), "1", 4, false);
        assertThat(new FiltroDisponibilidadSemanal(DayOfWeek.MONDAY, null).cumple(r)).isTrue();
        assertThat(new FiltroDisponibilidadSemanal(DayOfWeek.MONDAY, LocalTime.of(9, 30)).cumple(r)).isTrue();
        assertThat(new FiltroDisponibilidadSemanal(DayOfWeek.MONDAY, LocalTime.of(12, 0)).cumple(r)).isFalse();
        assertThat(new FiltroDisponibilidadSemanal(DayOfWeek.MONDAY, LocalTime.of(7, 59)).cumple(r)).isFalse();
        assertThat(new FiltroDisponibilidadSemanal(DayOfWeek.TUESDAY, null).cumple(r)).isFalse();
    }

    @Test
    void filtroFranjaHorariaBuscaCruceConLaDisponibilidad() {
        ResultadoBusqueda r = trabajador(Set.of(), "1", 4, false);
        assertThat(new FiltroFranjaHoraria(null, FranjaHoraria.MANANA).cumple(r)).isTrue();
        assertThat(new FiltroFranjaHoraria(DayOfWeek.MONDAY, FranjaHoraria.MANANA).cumple(r)).isTrue();
        assertThat(new FiltroFranjaHoraria(DayOfWeek.TUESDAY, FranjaHoraria.MANANA).cumple(r)).isFalse();
        assertThat(new FiltroFranjaHoraria(null, FranjaHoraria.TARDE).cumple(r)).isFalse();
        assertThat(new FiltroFranjaHoraria(null, FranjaHoraria.MADRUGADA).cumple(r)).isFalse();
    }

    @Test
    void franjaQueTerminaALaMedianocheCubreLaNoche() {
        FranjaDisponibilidad tarde = new FranjaDisponibilidad(DayOfWeek.FRIDAY, LocalTime.of(17, 0), LocalTime.of(19, 0));
        assertThat(tarde.seCruzaCon(null, FranjaHoraria.NOCHE)).isTrue();
        assertThat(tarde.seCruzaCon(DayOfWeek.FRIDAY, FranjaHoraria.TARDE)).isTrue();
        assertThat(tarde.seCruzaCon(DayOfWeek.FRIDAY, FranjaHoraria.MANANA)).isFalse();
    }

    @Test
    void filtroCalificacionMinimaEsIgualOSuperior() {
        ResultadoBusqueda r = trabajador(Set.of(), "1", 4.0, false);
        assertThat(new FiltroCalificacionMinima(4.0).cumple(r)).isTrue();
        assertThat(new FiltroCalificacionMinima(4.1).cumple(r)).isFalse();
    }

    @Test
    void filtroCompuestoExigeCumplirTodos() {
        ResultadoBusqueda r = trabajador(Set.of(10L), "40000", 4.5, true);
        FiltroCompuesto todosCumplen = new FiltroCompuesto()
                .agregar(new FiltroZona(List.of(10L)))
                .agregar(new FiltroDisponibleAhora());
        FiltroCompuesto unoFalla = new FiltroCompuesto()
                .agregar(new FiltroZona(List.of(10L)))
                .agregar(new FiltroCalificacionMinima(4.8));
        assertThat(todosCumplen.cumple(r)).isTrue();
        assertThat(unoFalla.cumple(r)).isFalse();
        assertThat(new FiltroCompuesto().cumple(r)).isTrue();
        assertThat(todosCumplen.cantidad()).isEqualTo(2);
    }
}
