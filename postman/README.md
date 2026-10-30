# Colección de Postman: Search

`OficioYa-Search.postman_collection.json` reúne los **casos de aceptación** del sprint 3 de Search, más una verificación de su documentación Swagger.

| Carpeta | Peticiones | Qué demuestra |
|---|---|---|
| Search | 7 | Catálogo, búsqueda por oficio ordenada por reputación y sin datos de contacto, filtros combinados, lenguaje natural (identifica el oficio o pide más detalle), validación y 401. |
| Documentación (Swagger) | 1 | `/v3/api-docs` publicado, con el esquema JWT. |

Cada petición tiene pruebas (`pm.test`) que verifican el código HTTP y el contenido.

## Tokens
No hay que pegar tokens a mano. El *pre-request* de la colección genera un JWT HS256 para cada rol:
- `token_contratante`
- `token_trabajador`
- `token_admin`
- `token_sistema`

Para firmarlos usa la variable `jwtSecret`, que es el mismo secreto de desarrollo de `application.yml`.

## Opción 1: Postman (app)
1. Importar `OficioYa-Search.postman_collection.json`.
2. Levantar el servicio: `./mvnw spring-boot:run -Dspring-boot.run.profiles=demo` (perfil `demo`: datos simulados, sin depender de otros servicios).
3. Hacer clic derecho en la colección y elegir **Run collection**.

Para apuntar a otro entorno, cambia las variables `searchUrl` y `jwtSecret` de la colección.

## Opción 2: todo automático con Docker y Newman
Primero se compila el proyecto con `./mvnw verify`. Luego, desde la raíz del proyecto:

```bash
bash postman/ejecutar-coleccion.sh
```

El script hace lo siguiente:
1. Levanta el servicio (perfil `demo`) en contenedores.
2. Ejecuta la colección con Newman.
3. Muestra el resultado de cada petición.
4. Borra los contenedores.

Resultado esperado: **8 peticiones y 16 aserciones, 0 fallidas**.
