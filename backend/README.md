# CambistaOnline - Enterprise Auth Microservice (Java 17 + Spring Boot 3 + PostgreSQL/Neon)

Este microservicio empresarial de Autenticación y Gestión de Usuarios para **CambistaOnline** ha sido desarrollado bajo **Onion Architecture estricta**, patrones **SOLID** y estructura **Package-by-Feature**.

---

## 🏛️ Arquitectura & Principios de Diseño

```text
               +---------------------------------------------------+
               |               Infrastructure Layer                |
               |  (Spring Security, JPA, PostgreSQL, REST, Flyway) |
               |   +-------------------------------------------+   |
               |   |            Application Layer              |   |
               |   |   (Use Cases, Commands, DTOs, Mappers)    |   |
               |   |   +-----------------------------------+   |   |
               |   |   |           Domain Layer            |   |   |
               |   |   |  (Entities, Value Objects,        |   |   |
               |   |   |   Domain Services, Ports)         |   |   |
               |   |   +-----------------------------------+   |   |
               |   +-------------------------------------------+   |
               +---------------------------------------------------+
```

- **Aislamiento Total del Dominio**: La capa `domain` contiene POJOs puros en Java 17 con **cero dependencias de Spring Boot**. Se verifica automáticamente en cada build mediante **ArchUnit**.
- **Inversión de Dependencias (DIP)**: Los Casos de Uso interactúan exclusivamente a través de interfaces (**Puertos**), los cuales son implementados en la capa de `infrastructure` (**Adaptadores**).
- **Flyway Migrations**: Esquema DDL en PostgreSQL versionado sin uso de `ddl-auto=create`.

---

## 🛠️ Stack Tecnológico

- **Lenguaje**: Java 17 LTS
- **Framework**: Spring Boot 3.3.1
- **Seguridad**: Spring Security 6 + JJWT 0.12.5 (Stateless Authentication)
- **Base de Datos**: PostgreSQL 16 / **Neon Serverless PostgreSQL**
- **Migraciones**: Flyway
- **Documentación**: OpenAPI 3 + Swagger UI
- **Pruebas**: JUnit 5, Mockito, ArchUnit, JaCoCo

---

## 🌐 Configuración e Integración con Neon (Serverless PostgreSQL)

Para conectar este servicio a tu base de datos cloud en **Neon**:

1. Ingresa a tu consola de [Neon.tech](https://neon.tech) y copia tu **Connection String** PostgreSQL.
2. Define las siguientes variables de entorno en tu sistema o archivo `.env`:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://<neon-hostname>/cambista_db?sslmode=require"
export SPRING_DATASOURCE_USERNAME="<tu-usuario-neon>"
export SPRING_DATASOURCE_PASSWORD="<tu-password-neon>"
```

Alternativamente, puedes modificar estas claves directamente en `src/main/resources/application.yml`.

Al iniciar la aplicación, **Flyway ejecutará automáticamente** las migraciones `V1`, `V2` y `V3` en Neon creando las tablas, llaves primarias UUID e índices necesarios.

---

## 🚀 Ejecución de la Aplicación

### Opción A: Ejecución Local con Maven
```bash
cd backend
mvn clean spring-boot:run
```
La aplicación iniciará en `http://localhost:8080`.

### Opción B: Ejecución con Docker Compose
```bash
docker-compose up --build -d
```

---

## 📌 Documentación de Endpoints REST (Swagger UI)

Una vez iniciada la aplicación, accede a la documentación interactiva en:
👉 **`http://localhost:8080/swagger-ui.html`**

### 1. Registrar Usuario (Persona Natural / Persona Jurídica)
- **POST** `/api/v1/auth/register`
- **Body Request (Persona Natural)**:
```json
{
  "email": "juan@gmail.com",
  "password": "Password123*",
  "firstName": "Juan",
  "lastName": "Perez",
  "dni": "12345678",
  "companyName": null,
  "ruc": null,
  "legalRepresentativeName": null,
  "role": "N"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "Se registro exitosamente"
}
```

### 2. Autenticar Usuario & Obtener JWT Token
- **POST** `/api/v1/auth/login`
- **Body Request**:
```json
{
  "email": "juan@gmail.com",
  "password": "Password123*"
}
```
- **Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### 3. Obtener Usuario Autenticado
- **GET** `/api/v1/auth/me`
- **Header**: `Authorization: Bearer <accessToken>`

---

## 🧪 Pruebas Automatizadas & Arquitectura

Para ejecutar la suite completa de pruebas de unidad, integración y validación mecánica de Onion Architecture (ArchUnit):

```bash
mvn test
```
