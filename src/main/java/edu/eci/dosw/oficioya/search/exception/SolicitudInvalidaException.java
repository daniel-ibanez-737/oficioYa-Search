package edu.eci.dosw.oficioya.search.exception;

/**
 * Los datos de la búsqueda no son válidos (responde 400).
 */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
