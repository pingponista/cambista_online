# 💱 CambistaOnline - Plataforma Fintech de Cambio de Divisas (Arquitectura Hexagonal)

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot 3.3.1](https://img.shields.io/badge/Spring%20Boot-3.3.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React 18](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Vite 5](https://img.shields.io/badge/Vite-5-purple.svg)](https://vitejs.dev/)
[![PostgreSQL Neon](https://img.shields.io/badge/PostgreSQL-Neon.tech-blue.svg)](https://neon.tech/)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)
[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%2F%20Ports%20%26%20Adapters-darkgreen.svg)]()

**CambistaOnline** es una plataforma web financiera de grado empresarial diseñada para la cotización e intercambio de divisas en tiempo real entre **Dólares (USD)**, **Euros (EUR)** y **Soles (PEN)**. Soporta **Persona Natural** (DNI/CE) y **Persona Jurídica** (RUC 10/20, Razón Social y Representante Legal), desarrollada bajo **Arquitectura Hexagonal (Puertos y Adaptadores / Ports & Adapters Architecture)**.

---

## 🏛️ Transformación e Implementación de la Arquitectura Hexagonal (Ports & Adapters)

Para elevar la mantenibilidad, desacoplamiento y testabilidad a estándares bancarios internacionales, la aplicación fue transformada a **Arquitectura Hexagonal pura** (definida por Alistair Cockburn).

En esta arquitectura, la lógica de negocio y las reglas del dominio habitan dentro de un **Hexágono Aislado** que interactúa con el mundo exterior únicamente a través de **Puertos** (Contratos/Interfaces) y **Adaptadores** (Implementaciones tecnológicas).

```text
               +-------------------------------------------------------------------+
               |                       DRIVING ADAPTERS                            |
               |  (REST Controllers: AuthController, ExchangeEngineController)    |
               |   +-----------------------------------------------------------+   |
               |   |                  INBOUND / PRIMARY PORTS                  |   |
               |   |   (Interfaces: RegisterUserUseCase, CalculateRateUseCase) |   |
               |   |   +---------------------------------------------------+   |   |
               |   |   |                   CORE DOMAIN                     |   |   |
               |   |   |   (Entities, Value Objects, Domain Services)      |   |   |
               |   |   +---------------------------------------------------+   |   |
               |   |                  OUTBOUND / SECONDARY PORTS               |   |
               |   |   (Interfaces: UserPersistencePort, OrderPersistence)  |   |
               |   +-----------------------------------------------------------+   |
               |                       DRIVEN ADAPTERS                             |
               |  (JPA Adapters, Neon PostgreSQL, Spring Security JWT, Flyway)     |
               +-------------------------------------------------------------------+
```

---

## 🔍 Detalle de Cambios Realizados por Capas y Bounded Contexts

### 1. Puertos de Entrada / Primarios (`application.ports.inbound`)
Definen los contratos abstractos de los **Casos de Uso** consumidos por los Adaptadores de Entrada (Controladores REST).

- **Módulo `auth`**:
  - `RegisterUserUseCase`: Interfaz del caso de uso de registro.
  - `AuthenticateUserUseCase`: Interfaz para autenticación de credenciales y emisión de JWT.
  - `GetCurrentUserUseCase`: Interfaz para la obtención del perfil autenticado.
- **Módulo `engine`**:
  - `CalculateExchangeRateUseCase`: Interfaz para la cotización dinámica de tipos de cambio.
- **Módulo `order`**:
  - `CreateExchangeOrderUseCase`: Interfaz para generación de órdenes `TRX-XXXXXX`.
  - `GetMyOrdersUseCase`: Interfaz para consulta del historial de operaciones.
  - `ConfirmTransferUseCase`: Interfaz para confirmación de transferencia y cambio de estado a `COMPLETED`.

---

### 2. Servicios de Aplicación (`application.service`)
Implementan formalmente las interfaces de los Puertos Primarios y orquestan las entidades del dominio sin depender de ningún framework web o de persistencia.

- **`RegisterUserService`**: Orquesta la creación de `User`, validación de correo único y cifrado de clave.
- **`AuthenticateUserService`**: Valida credenciales contra `UserPersistencePort` y genera tokens con `JwtTokenPort`.
- **`GetCurrentUserService`**: Recupera el dominio `User` y mapea hacia DTO de respuesta.
- **`CalculateExchangeRateService`**: Ejecuta el pipeline de estrategias de cálculo dinámico (SBS, spreads, horario, estacionalidad y puntos).
- **`CreateExchangeOrderService`**: Genera número de transacción, calcula fecha de expiración a 15 min y gestiona abono/canje de CambiPuntos.
- **`GetMyOrdersService`**: Retorna el historial de transacciones en formato `OrderSummaryDto`.
- **`ConfirmTransferService`**: Marca la orden como `COMPLETED` tras la transferencia.

---

### 3. Puertos de Salida / Secundarios (`application.ports.outbound`)
Definen los contratos de salida que requiere el núcleo para interactuar con infraestructura (Bases de datos, servicios de clave, JWT).

- **`UserPersistencePort`**: Puerto para operaciones de persistencia de usuarios.
- **`ExchangeOrderPersistencePort`**: Puerto para guardado y búsqueda de órdenes de cambio.
- **`ExchangeRatePersistencePort`**: Puerto para obtención de tasas de cambio base.
- **`ExchangeRulePersistencePort`**: Puerto para recuperación de reglas comerciales.
- **`UserPointsPersistencePort`**: Puerto para la gestión de saldo de CambiPuntos.
- **`PasswordEncoderPort`**: Puerto abstracto para algoritmos de hashing.
- **`JwtTokenPort`**: Puerto abstracto para generación y validación de tokens JWT.

---

### 4. Adaptadores de Entrada / Primarios (`adapters.inbound.rest`)
Invocan a los Puertos de Entrada para procesar las peticiones HTTP externas.

- **`AuthController`** (`/api/v1/auth`): Maneja peticiones `/register`, `/login` y `/me`.
- **`ExchangeEngineController`** (`/api/v1/exchange`, `/api/v1/rates`): Expone `/calculate` y `/breakdown`.
- **`ExchangeOrderController`** (`/api/v1/orders`): Expone endpoints de creación, listado y confirmación de transferencias.

---

### 5. Adaptadores de Salida / Secundarios (`adapters.outbound`)
Implementan los Puertos de Salida secundarias para conectarse con la infraestructura real de persistencia (**MongoDB NoSQL** y **JPA / PostgreSQL**).

- **`MongoUserPersistenceAdapter`** & **`SpringDataMongoUserRepository`**: Adaptador NoSQL para la colección `users` en MongoDB Atlas / Local.
- **`MongoOrderPersistenceAdapter`** & **`SpringDataMongoOrderRepository`**: Adaptador NoSQL para la colección `operacion` en MongoDB.
- **`MongoEnginePersistenceAdapter`** & **`SpringDataMongoUserPointsRepository`**: Adaptador NoSQL para la colección `usuarios_puntos` y reglas comerciales.
- **`UserRepositoryAdapter`** & **`ExchangeOrderPersistenceAdapter`**: Adaptadores relacionales JPA (compatibilidad relacional).
- **`BcryptPasswordEncoderAdapter`**: Adaptador que encapsula BCrypt de Spring Security.
- **`JwtProviderAdapter`**: Adaptador que implementa la firma y lectura de tokens JWT HMAC-SHA256.

---

### 🛡️ Resiliencia y Mapeo en el Registro de Usuarios (`auth`)
A diferencia de la implementación previa, en el refactor Hexagonal se aplicaron mejoras de resiliencia en la capa de **Aplicación** y **Adaptadores**:

1. **Garantía de Marcas de Tiempo Non-Null (`createdAt` / `updatedAt`)**:
   - `RegisterUserService` y `UserRepositoryAdapter` aseguran la inicialización explícita de `createdAt` y `updatedAt` con `LocalDateTime.now()`, evitando excepciones de restricción de base de datos (`NOT NULL`) en Neon PostgreSQL.
2. **Sanitización de Identificadores y Alineación DTO**:
   - Sanitización de valores vacíos para `dni` y `ruc` al construir Value Objects en el dominio.
   - Normalización de propiedades `role` y `rol` en el payload JSON entre el frontend React y `AuthController`.

---

### 6. Gobierno y Reglas Arquitectónicas Auditeadas (`ArchUnit 1.3`)
Se implementó la suite **`HexagonalArchitectureTest.java`** que verifica automáticamente en cada compilación `mvn test`:

```java
@Test
void domainModelAndStrategyShouldNotDependOnOuterInfrastructureOrFrameworks() {
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..infrastructure..", "..adapters..", "org.springframework..");
}

@Test
void applicationServicesAndPortsShouldNotDependOnInfrastructureAdapters() {
    noClasses()
        .that().resideInAPackage("..application..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..infrastructure..", "..adapters..");
}
```

---

## 📌 Catálogo de Endpoints REST

### 🔐 1. Módulo de Autenticación (`/api/v1/auth`)

| Método | Endpoint | Descripción | Requiere JWT |
| :--- | :--- | :--- | :---: |
| `POST` | `/api/v1/auth/register` | Registro de usuario como Persona Natural ("N") o Jurídica ("J") | ❌ |
| `POST` | `/api/v1/auth/login` | Autenticación y retorno de JWT Access Token | ❌ |
| `GET` | `/api/v1/auth/me` | Obtiene la información del perfil del usuario autenticado | ✅ |

### 💱 2. Motor de Cotización y Desglose (`/api/v1/exchange` & `/api/v1/rates`)

| Método | Endpoint | Descripción | Requiere JWT |
| :--- | :--- | :--- | :---: |
| `POST` | `/api/v1/exchange/calculate` | Calcula el tipo de cambio dinámico aplicando el pipeline completo de reglas | ✅ |
| `GET` | `/api/v1/rates/breakdown` | Retorna el desglose de tasa (SBS, spread, horario) y el saldo real de CambiPuntos en Neon DB | ✅ |

### 🧾 3. Módulo de Transacciones u Órdenes (`/api/v1/orders`)

| Método | Endpoint | Descripción | Requiere JWT |
| :--- | :--- | :--- | :---: |
| `POST` | `/api/v1/orders` | Crea una nueva orden de cambio (`TRX-XXXXXX`), congela la tasa por 15 min y gestiona puntos | ✅ |
| `GET` | `/api/v1/orders` | Consulta el historial completo de transacciones realizadas por el usuario | ✅ |
| `POST` | `/api/v1/orders/{orderNumber}/confirm-transfer` | Marca la orden como completada (`COMPLETED`) e ingresa la operación | ✅ |

---

## 🛠️ Tecnologías Aplicadas

### Backend Enterprise (Java 17 + Spring Boot 3)
- **Java 17 LTS**: Programación orientada a objetos inmutable.
- **Spring Boot 3.3.1**: Framework empresarial RESTful.
- **Spring Security 6 + JWT**: Autenticación sin estado (*Stateless*).
- **Spring Data JPA & Hibernate**: Persistencia relacional.
- **Flyway Database Migrations**: Control de versiones de BD (V1 a V7).
- **ArchUnit 1.3**: Auditoría automatizada de Arquitectura Hexagonal.

### Frontend Moderno (React 18 + Vite + Vanilla CSS)
- **React 18 & Vite 5**: UI responsiva con compilación de producción optimizada.
- **Zustand**: Gestión de estado global persistente (`useAuthStore`, `useFxStore`, `useOrderStore`).
- **Axios**: Interceptores automáticos Bearer Token.

---

## 🚀 Requisitos e Instalación en Desarrollo

### Paso 1: Clonar el Repositorio
```bash
git clone https://github.com/TU_USUARIO/cambista_online.git
cd cambista_online
```

### Paso 2: Configurar las Variables de Entorno (`backend/.env`)
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-young-tooth-ac5b010f-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=tu_usuario_neondb
SPRING_DATASOURCE_PASSWORD=tu_password_de_neon
JWT_SECRET=tu_jwt_secret
JWT_EXPIRATION_MS=3600000
```

---

### Paso 3: Levantar los Contenedores con Docker Compose
```bash
docker compose up --build -d
```

#### 🌐 URLs de Acceso:
- **Aplicación Web Frontend**: 👉 **`http://localhost:3000`**
- **Documentación REST Backend (Swagger UI)**: 👉 **`http://localhost:8080/swagger-ui.html`**

---

## 🧪 Pruebas Automatizadas

### Pruebas de Backend y Arquitectura Hexagonal (ArchUnit + JUnit 5)
```bash
cd backend
mvn test
```
