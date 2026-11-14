package edu.eci.dosw.oficioya.search.domain;

import java.util.UUID;

import java.time.LocalDateTime;

/**
 * Registro de una búsqueda realizada. No se guarda en base de datos (Search no almacena información):
 * solo se usa para dejar el log de cada búsqueda.
 */
public record Busqueda(String usuario, TipoBusqueda tipo, UUID oficioId, int totalResultados, long tiempoMs,
                       LocalDateTime fechaHora) {

    /**
     * Texto para el log. No incluye el texto libre del contratante ni datos personales de los trabajadores.
     */
    public String resumen() {
        return "tipo=%s usuario=%s oficioId=%s resultados=%d tiempoMs=%d"
                .formatted(tipo, usuario, oficioId, totalResultados, tiempoMs);
    }
}
