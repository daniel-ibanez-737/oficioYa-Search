package edu.eci.dosw.oficioya.search.domain;

/**
 * Resultado de interpretar con IA la descripción escrita por el contratante.
 */
public enum EstadoInterpretacion {
    /** Se identificó el oficio con suficiente confianza. */
    OFICIO_IDENTIFICADO,
    /** No se identificó el oficio: hay que pedirle más detalle al contratante. */
    REQUIERE_MAS_DETALLE,
    /** Falló la integración con la IA. */
    ERROR
}
