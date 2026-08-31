# project_interview

API REST que expone información de mascotas consumiendo la API pública de [Petstore (Swagger)](https://petstore.swagger.io/v2), desarrollada como prueba técnica.

## Stack

- Java 17
- Spring Boot 4.1.1 (ver nota de versión más abajo)
- Gradle (Groovy DSL)
- `RestClient` (Spring 6.1+) para el consumo de Petstore

## Cómo correr el proyecto

```bash
./gradlew bootRun
```

La app levanta en `http://localhost:8080`.

## Endpoints

### `GET /api/pet/{petId}`

Consulta un pet en Petstore y regresa solo los campos relevantes.

```bash
curl http://localhost:8080/api/pet/123456789
```
```json
{ "id": 123456789, "name": "kitty", "status": "available" }
```

### `POST /api/pet`

Crea un pet en Petstore. `transactionId` (UUID v4) y `dateCreated` se generan en la capa Service, no vienen de Petstore.

```bash
curl -X POST http://localhost:8080/api/pet \
  -H "Content-Type: application/json" \
  -d '{"id": 990011, "name": "testingPet1", "status": "available"}'
```
```json
{
  "transactionId": "9b4a2a27-f6cc-4877-9821-f0fd94cce0b1",
  "dateCreated": "2026-08-30T20:48:30.684951",
  "status": "available",
  "name": "testingPet1"
}
```

## Manejo de errores

| Escenario | HTTP status | Body |
|---|---|---|
| Pet inexistente (GET) | 404 | `{"message": "No se encontró el pet con id X"}` |
| `petId` no numérico | 400 | `{"message": "El parámetro 'petId' tiene un formato inválido"}` |
| Body del POST inválido (campo vacío/faltante) | 400 | `{"message": "<campo> es obligatorio"}` |
| JSON malformado | 400 | `{"message": "El body de la petición no es un JSON válido"}` |
| Petstore caído / timeout / error 5xx | 502 | `{"message": "El servicio de Petstore no está disponible"}` |

## Arquitectura

```
controller/  → PetController: define los endpoints REST
service/     → PetService: lógica de negocio, mapeo, logging, UUID/fecha
client/      → PetstoreClient (interfaz) + PetstoreClientImpl (RestClient),
               PetstoreProperties, PetstoreRestClientCustomizer,
               PetstorePet (contrato EXTERNO de Petstore)
dto/         → contratos de NUESTRA API pública (GetPetResponse, CreatePetRequest, CreatePetResponse)
exception/   → excepciones propias + ApiExceptionHandler (@RestControllerAdvice)
```

El contrato externo de Petstore (`PetstorePet`) y el contrato público de esta API (`dto/*`) están deliberadamente separados, aunque hoy compartan los mismos campos — así un cambio en el schema de Petstore no se filtra directo a los consumidores de esta API.

## Configuración

En `application.properties`:

```properties
petstore.base-url=https://petstore.swagger.io/v2
petstore.connect-timeout=3s
petstore.read-timeout=5s
```

Se puede sobreescribir en runtime, por ejemplo:
```bash
./gradlew bootRun --args="--petstore.base-url=http://localhost:9999"
```

## Tests

```bash
./gradlew test
```

25 tests: unitarios de `PetService` (mocks de `PetstoreClient`), de `PetstoreClientImpl` (`MockRestServiceServer`, sin red real), de `PetController` (`@WebMvcTest`), y un test de integración end-to-end (`@SpringBootTest` + un servidor HTTP de prueba embebido) que valida el wiring completo de Spring.

## Decisiones de alcance (a propósito no implementado)

- **Sin `spring-boot-starter-validation`**: la validación del POST se hace a mano en `PetService` (footprint de dependencias mínimo para 2 endpoints).
- **Sin base de datos, cache, ni cola de mensajes**: no hay requerimiento que los justifique.
- **Sin circuit breaker / retry framework**: el timeout + traducción de errores es suficiente para el alcance actual; ver justificación completa en la sección de preguntas de la presentación.
- **`status` del pet es un `String` libre**, no un enum — Petstore lo trata como string en su propio contrato.

## Nota sobre versiones

El enunciado original pedía Spring Boot 3.2.7 vía Spring Initializr. Esa versión ya es EOL y fue retirada del catálogo de `start.spring.io` (que hoy solo ofrece 4.0.8+), así que el proyecto usa **Spring Boot 4.1.1** (la versión GA vigente al momento de generarlo). La arquitectura, capas y decisiones de diseño son independientes de esta versión menor de Boot.
