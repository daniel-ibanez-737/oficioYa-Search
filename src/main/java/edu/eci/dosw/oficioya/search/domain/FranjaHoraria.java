package edu.eci.dosw.oficioya.search.domain;

/**
 * Franjas de 6 horas en las que se divide el día para filtrar por disponibilidad (horario militar).
 */
public enum FranjaHoraria {
    MADRUGADA(0, 6),
    MANANA(6, 12),
    TARDE(12, 18),
    NOCHE(18, 24);

    private final int horaInicio;
    private final int horaFin;

    FranjaHoraria(int horaInicio, int horaFin) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    /**
     * Minuto del día en que empieza la franja (incluido).
     */
    public int minutoInicio() {
        return horaInicio * 60;
    }

    /**
     * Minuto del día en que termina la franja (excluido).
     */
    public int minutoFin() {
        return horaFin * 60;
    }
}
