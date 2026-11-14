package edu.eci.dosw.oficioya.search.domain;

import java.util.UUID;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Trabajador encontrado en una búsqueda, con la información necesaria para filtrar, ordenar y mostrar.
 * Nunca contiene correo ni teléfono (privacidad). Search no guarda estos datos: se arman en cada búsqueda.
 */
public class ResultadoBusqueda {

    private final UUID trabajadorId;
    private final String nombreCompleto;
    private final String fotoPerfilUrl;
    private final String oficio;
    private final String zona;
    private final Set<Long> barrioIds;
    private final BigDecimal tarifaAproximada;
    private final double calificacionPromedio;
    private final int totalResenas;
    private final int trabajosCompletados;
    private final boolean disponibleAhora;
    private final List<FranjaDisponibilidad> disponibilidad;
    private int posicion;

    @SuppressWarnings("java:S107") // el resultado agrupa los datos que se muestran en el listado
    public ResultadoBusqueda(UUID trabajadorId, String nombreCompleto, String fotoPerfilUrl, String oficio, String zona,
                             Set<Long> barrioIds, BigDecimal tarifaAproximada, double calificacionPromedio,
                             int totalResenas, int trabajosCompletados, boolean disponibleAhora,
                             List<FranjaDisponibilidad> disponibilidad) {
        this.trabajadorId = trabajadorId;
        this.nombreCompleto = nombreCompleto;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.oficio = oficio;
        this.zona = zona;
        this.barrioIds = barrioIds == null ? Set.of() : Set.copyOf(barrioIds);
        this.tarifaAproximada = tarifaAproximada;
        this.calificacionPromedio = calificacionPromedio;
        this.totalResenas = totalResenas;
        this.trabajosCompletados = trabajosCompletados;
        this.disponibleAhora = disponibleAhora;
        this.disponibilidad = disponibilidad == null ? List.of() : List.copyOf(disponibilidad);
    }

    public void asignarPosicion(int posicion) {
        this.posicion = posicion;
    }

    public UUID getTrabajadorId() {
        return trabajadorId;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public String getOficio() {
        return oficio;
    }

    public String getZona() {
        return zona;
    }

    public Set<Long> getBarrioIds() {
        return barrioIds;
    }

    public BigDecimal getTarifaAproximada() {
        return tarifaAproximada;
    }

    public double getCalificacionPromedio() {
        return calificacionPromedio;
    }

    public int getTotalResenas() {
        return totalResenas;
    }

    public int getTrabajosCompletados() {
        return trabajosCompletados;
    }

    public boolean isDisponibleAhora() {
        return disponibleAhora;
    }

    public List<FranjaDisponibilidad> getDisponibilidad() {
        return disponibilidad;
    }

    public int getPosicion() {
        return posicion;
    }
}
