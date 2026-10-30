# oficioya-search

Microservicio del dominio **Search** de OficioYa (Squad 5).

Búsqueda de trabajadores en lenguaje natural (IA) y por oficio, filtros y orden por reputación. No guarda información: consulta otros dominios a través del Orchestrator.

## Tecnología
- Java 21, Spring Boot 4.1.1, Maven
- Spring Security con JWT (validado también por el Orchestrator)
- JUnit 5 y JaCoCo (cobertura mínima del 80 %)

## Estructura por capas (`edu.eci.dosw.oficioya.search`)
| Paquete | Capa |
|---|---|
| `controller` | Presentación (API REST) |
| `service` | Aplicación (casos de uso) |
| `domain` | Dominio (modelo y reglas de negocio) |
| `dto` | Objetos de transferencia |
| `client` | Infraestructura: `OrquestadorClient`, única salida hacia otros dominios o la IA |
| `config` | Configuración (seguridad, clientes HTTP) |
| `exception` | Excepciones y manejo de errores |

Toda comunicación con otros dominios o con la IA **entra y sale por el Orchestrator**.

## Compilar y probar
Con Java 21 instalado (no hace falta Maven: el proyecto trae Maven Wrapper):

```bash
./mvnw verify          # Linux, macOS o Git Bash
mvnw.cmd verify        # Windows (CMD o PowerShell)
```

Sin Java, con Docker:

```bash
docker run --rm -v "$PWD":/app -w /app maven:3.9-eclipse-temurin-21 mvn verify
```

`mvn verify` compila, ejecuta las pruebas y valida la cobertura mínima con JaCoCo
(reporte en `target/site/jacoco/index.html`).

## Variables de entorno
| Variable | Uso | Valor por defecto (desarrollo) |
|---|---|---|
| `JWT_SECRET` | Secreto para validar el JWT | valor de desarrollo |
| `JWT_ROLES_CLAIM` | Claim del JWT con los roles | `roles` |
| `ORQUESTADOR_URL` | URL del Orchestrator | `http://localhost:8080` |
| `IA_MODO` | `simulado` (palabras clave, sin IA) u `orquestador` (IA real a través del Orchestrator) | `simulado` |

## Endpoints (todos requieren JWT)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/search/oficios` | Catálogo de oficios. |
| GET | `/api/search/oficios/{oficioId}/trabajadores` | Búsqueda por oficio (principal o secundario), ordenada por reputación. |
| POST | `/api/search/lenguaje-natural` | Búsqueda describiendo la necesidad: identifica el oficio y sugiere los trabajadores de mayor reputación. |

**Filtros opcionales** (query params en la búsqueda por oficio, o el objeto `criterios` en lenguaje natural), combinables entre sí:
`barrioIds`, `precioMinimo`, `precioMaximo`, `diaSemana` (`MONDAY`…), `hora` (`HH:mm`, requiere `diaSemana`), `soloDisponibleAhora`, `calificacionMinima` (0–5).

Ejemplo de lenguaje natural:
```json
{ "texto": "Necesito alguien que arregle una gotera", "criterios": { "soloDisponibleAhora": true } }
```
Si la IA no identifica el oficio con suficiente confianza, la respuesta tiene `estado = REQUIERE_MAS_DETALLE` y un mensaje pidiendo más detalle.

Los resultados nunca incluyen correo ni teléfono del trabajador. Search no guarda nada: cada búsqueda solo deja un registro en el log.

Errores: `400` validación, `401` sin JWT, `502` falla al consultar el Orchestrator.

## Perfil `demo`
Para probar sin el Orchestrator, con 6 oficios y 7 trabajadores de ejemplo:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

Las pruebas de aceptación (`AceptacionSearchDemoTest`) usan este perfil.


## Documentación de la API (Swagger)
Con el servicio en ejecución:
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI (JSON): `http://localhost:8081/v3/api-docs`

Para probar desde Swagger UI, se pulsa **Authorize** y se pega un JWT.
La colección de Postman del servicio (`postman/`) genera tokens de prueba para cada rol y se ejecuta sola con `bash postman/ejecutar-coleccion.sh` (ver `postman/README.md`).
La documentación es pública y se puede apagar con `SWAGGER_ENABLED=false`.
