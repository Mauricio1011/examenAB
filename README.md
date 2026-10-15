# Huertos urbanos — microservicios de parcelas

Dos microservicios con el mismo contrato HTTP. Uno persiste en MongoDB y el otro en PostgreSQL. Ambos validan la entrada, sanitizan el texto, lanzan excepciones propias y responden con mensajes de error en español.

| Servicio | Puerto | Base de datos |
| --- | --- | --- |
| `parcel-mongo-service` | 8081 | MongoDB `huertos` |
| `parcel-postgres-service` | 8082 | PostgreSQL `parcels` |

## Arranque con Docker Compose

Requiere Docker.

```bash
docker compose up --build
```

Las credenciales de PostgreSQL (`parcels` / `parcels`) son solo para este entorno local.

## Arranque local

Requiere Java 17 y Maven. Hay que tener MongoDB en `localhost:27017` o PostgreSQL en `localhost:5432` con la base `parcels`, usuario `parcels` y contraseña `parcels`.

```bash
mvn -f parcel-mongo-service/pom.xml spring-boot:run
mvn -f parcel-postgres-service/pom.xml spring-boot:run
```

Pruebas:

```bash
mvn test
```

## API

- `POST /api/v1/parcels` registra una parcela. El estado inicial es `ACTIVA` y `createdAt` lo asigna el servidor.
- `GET /api/v1/parcels` lista las parcelas.
- `GET /api/v1/parcels/{id}` devuelve el detalle y el estado actual (`status`).

En MongoDB el `id` es un ObjectId. En PostgreSQL es un UUID.

Ejemplo:

```json
{
  "name": "Huerto Norte",
  "location": { "latitude": 19.4326, "longitude": -99.1332 },
  "soilType": "ARCILLOSO",
  "ownerOrLead": "Ana Ruiz"
}
```

`soilType` acepta `ARCILLOSO`, `LIMOSO` o `ARENOSO` (también en minúsculas). `status` puede ser `ACTIVA`, `EN_ALERTA`, `EN_REGENERACION` o `INACTIVA`; al crear queda en `ACTIVA`.

## Validación, sanitización y errores

Antes de validar, el nombre y el responsable se normalizan: se recortan espacios, se unifican espacios internos y se eliminan caracteres de control e invisibles. No se aceptan etiquetas HTML, `javascript:` ni texto fuera de letras, números, espacios, apóstrofos, comas, puntos y guiones. La latitud debe estar entre -90 y 90, y la longitud entre -180 y 180. El nombre no puede repetirse, sin distinguir mayúsculas.

Las respuestas de error tienen esta forma:

```json
{
  "timestamp": "2026-10-14T18:00:00Z",
  "status": 400,
  "error": "Solicitud inválida",
  "message": "La parcela contiene datos inválidos",
  "path": "/api/v1/parcels",
  "fieldErrors": [
    { "field": "name", "message": "El nombre de la parcela es obligatorio" }
  ]
}
```

| Situación | HTTP | Excepción |
| --- | --- | --- |
| Datos inválidos o texto malicioso | 400 | `InvalidParcelDataException` y Bean Validation |
| Identificador con formato incorrecto | 400 | `InvalidParcelDataException` |
| Parcela inexistente | 404 | `ParcelNotFoundException` |
| Nombre duplicado | 409 | `DuplicateParcelException` |
| Error no controlado | 500 | mensaje genérico, sin detalle interno |
