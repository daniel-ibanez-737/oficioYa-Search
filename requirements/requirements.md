# 📄 Requerimientos del Sistema

## 1. Lista general de requerimientos

El sistema de Search (OficioYa) debe tener los siguientes requerimientos:

### 1.1 Requerimientos funcionales

1. Permitir al contratante ingresar una descripción en lenguaje natural del servicio que necesita.
2. Interpretar, mediante un componente de Inteligencia Artificial, la descripción para identificar el oficio y los criterios de búsqueda.
3. Permitir filtrar los resultados por zona, precio aproximado, disponibilidad y calificación mínima.
4. Ordenar los resultados por reputación del trabajador, sin que el pago influya en el orden.
5. Permitir consultar únicamente los trabajadores marcados como "disponible ahora".
6. Permitir realizar búsquedas de trabajadores por categoría u oficio.

### 1.2 Requerimientos no funcionales

1. Cobertura de pruebas unitarias mínima del 80%.
2. La interfaz de búsqueda debe ser responsive.
3. El sistema debe registrar logs de cada búsqueda realizada.

## 2. Diagramas de caso de uso

### 2.1 Requerimiento Funcional 1

| Campo | Descripción |
|------|-------------|
| **ID** | RF-01 |
| **Nombre del requerimiento** | Descripción de la necesidad en lenguaje natural |
| **Descripción** | *El sistema debe permitir a un contratante describir en lenguaje natural el servicio que necesita* |
| **Precondiciones** | *El contratante debe estar autenticado* |
| **Actor** | *Contratante* |
| **Flujo principal** | 1. El contratante accede a la opción de búsqueda.<br>2. Escribe una descripción libre.<br>3. El sistema envía el texto al caso de uso de interpretación. |
| **Poscondiciones** | *La descripción queda lista para ser interpretada por el componente de IA.* |

### 2.2 Requerimiento Funcional 2

| Campo | Descripción |
|------|-------------|
| **ID** | RF-02 |
| **Nombre del requerimiento** | Interpretación de la descripción mediante IA |
| **Descripción** | *El sistema debe interpretar mediante IA (Gemini) la descripción en lenguaje natural para identificar el oficio y criterios de búsqueda* |
| **Precondiciones** | *Debe existir una descripción ingresada (RF-01)* |
| **Actor** | *Contratante (indirectamente, vía el sistema)* |
| **Flujo principal** | 1. El sistema envía la descripción a Gemini.<br>2. Gemini identifica oficio y palabras clave.<br>3. El sistema traduce esa interpretación en criterios de búsqueda. |
| **Poscondiciones** | *Se generan los criterios de búsqueda listos para ejecutar la consulta.* |

### 2.3 Requerimiento Funcional 3

| Campo | Descripción |
|------|-------------|
| **ID** | RF-03 |
| **Nombre del requerimiento** | Filtrado de resultados de búsqueda |
| **Descripción** | *El sistema debe permitir filtrar resultados por zona, precio, disponibilidad y calificación mínima* |
| **Precondiciones** | *Debe existir una búsqueda previa ejecutada* |
| **Actor** | *Contratante* |
| **Flujo principal** | 1. El contratante aplica uno o más filtros.<br>2. El sistema recalcula la lista de trabajadores que cumplen los filtros. |
| **Poscondiciones** | *El contratante visualiza una lista filtrada.* |

### 2.4 Requerimiento Funcional 4

| Campo | Descripción |
|------|-------------|
| **ID** | RF-04 |
| **Nombre del requerimiento** | Ordenamiento por reputación |
| **Descripción** | *El sistema debe ordenar los resultados por reputación del trabajador, sin que el pago influya* |
| **Precondiciones** | *Debe existir una lista de resultados* |
| **Actor** | *Contratante* |
| **Flujo principal** | 1. El sistema toma la lista de resultados.<br>2. Ordena de mayor a menor calificación promedio.<br>3. Ignora cualquier parámetro de pago. |
| **Poscondiciones** | *El contratante ve los resultados ordenados exclusivamente por reputación.* |

### 2.5 Requerimiento Funcional 5

| Campo | Descripción |
|------|-------------|
| **ID** | RF-05 |
| **Nombre del requerimiento** | Consulta de disponibilidad inmediata |
| **Descripción** | *El sistema debe permitir consultar únicamente trabajadores marcados como "disponible ahora"* |
| **Precondiciones** | *El contratante debe estar autenticado* |
| **Actor** | *Contratante* |
| **Flujo principal** | 1. El contratante activa el filtro "disponible ahora".<br>2. El sistema consulta solo trabajadores en ese estado en tiempo real. |
| **Poscondiciones** | *Se muestra únicamente la lista de trabajadores disponibles en el momento de la consulta.* |

### 2.6 Requerimiento Funcional 6 

| Campo | Descripción |
|------|-------------|
| **ID** | RF-06 |
| **Nombre del requerimiento** | Búsqueda por categoría u oficio |
| **Descripción** | *El sistema debe permitir al contratante buscar trabajadores mediante la selección de una categoría u oficio específico"* |
| **Precondiciones** | *El contratante debe estar autenticado* |
| **Actor** | *Contratante* |
| **Flujo principal** | 1. El contratante accede a la opción de búsqueda. <br>2. Selecciona una categoría u oficio de la lista disponible. <br>3. El sistema consulta los trabajadores asociados a la categoría u oficio seleccionada. <br>4. El sistema muestra los resultados encontrados. |
| **Poscondiciones** | *Se presenta una lista de trabajadores correspondiente a la categoría u oficio seleccionado.* |
