package edu.eci.dosw.oficioya.search.exception;

/**
 * Falló la comunicación con otro dominio o con la IA a través del Orchestrator (responde 502).
 * Un fallo de integración nunca modifica información de otros dominios: Search solo consulta.
 */
public class IntegracionException extends RuntimeException {

    public IntegracionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
