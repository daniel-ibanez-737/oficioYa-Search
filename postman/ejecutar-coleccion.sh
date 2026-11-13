#!/usr/bin/env bash
# Levanta Search en Docker y ejecuta su colección de Postman con Newman.
# Requisitos: Docker y haber compilado el proyecto (./mvnw verify o ./mvnw package).
# Uso, desde la raíz del proyecto (Git Bash, Linux o macOS):  bash postman/ejecutar-coleccion.sh
RAIZ="$(cd "$(dirname "$0")/.." && (pwd -W 2>/dev/null || pwd))"
PROYECTO=oficioya-search
SERVICIO=oy-search
RED=oy-search-red
export MSYS_NO_PATHCONV=1

limpiar() {
  docker rm -f "$SERVICIO" oy-search-pg >/dev/null 2>&1
  docker network rm "$RED" >/dev/null 2>&1
}
limpiar
docker network create "$RED" >/dev/null

docker run -d --rm --name "$SERVICIO" --network "$RED" -v "$RAIZ/target":/t eclipse-temurin:21-jre \
  sh -c "java -jar /t/$PROYECTO-*.jar --spring.profiles.active=demo" >/dev/null

for i in $(seq 1 60); do
  docker logs "$SERVICIO" 2>&1 | grep -q "Started Oficioya" && break
  sleep 2
done
echo "$SERVICIO: $(docker logs "$SERVICIO" 2>&1 | grep -o 'Started Oficioya[A-Za-z]* in [0-9.]* seconds' || echo 'NO ARRANCO')"

docker run --rm --network "$RED" -v "$RAIZ/postman":/etc/newman postman/newman:6.1.3-alpine run \
  OficioYa-Search.postman_collection.json \
  --env-var searchUrl=http://$SERVICIO:8081 \
  --reporters cli --color off
RESULTADO=$?

limpiar
echo "newman exit=$RESULTADO"
exit $RESULTADO
