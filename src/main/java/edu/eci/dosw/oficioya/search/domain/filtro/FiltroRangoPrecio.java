package edu.eci.dosw.oficioya.search.domain.filtro;

import java.math.BigDecimal;

import edu.eci.dosw.oficioya.search.domain.ResultadoBusqueda;

/**
 * La tarifa aproximada del trabajador debe estar dentro del rango (decisión de la profesora: rango de precios).
 * Cualquiera de los dos límites puede omitirse.
 */
public class FiltroRangoPrecio implements FiltroBusqueda {

    private final BigDecimal precioMinimo;
    private final BigDecimal precioMaximo;

    public FiltroRangoPrecio(BigDecimal precioMinimo, BigDecimal precioMaximo) {
        this.precioMinimo = precioMinimo;
        this.precioMaximo = precioMaximo;
    }

    @Override
    public boolean cumple(ResultadoBusqueda resultado) {
        BigDecimal tarifa = resultado.getTarifaAproximada();
        if (tarifa == null) {
            return false;
        }
        boolean sobreMinimo = precioMinimo == null || tarifa.compareTo(precioMinimo) >= 0;
        boolean bajoMaximo = precioMaximo == null || tarifa.compareTo(precioMaximo) <= 0;
        return sobreMinimo && bajoMaximo;
    }
}
