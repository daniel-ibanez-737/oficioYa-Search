package edu.eci.dosw.oficioya.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio oficioya-search.
 * Búsqueda de trabajadores en lenguaje natural (IA) y por oficio, filtros y orden por reputación. No guarda información: consulta otros dominios a través del Orchestrator.
 */
@SpringBootApplication
public class OficioyaSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(OficioyaSearchApplication.class, args);
    }
}
