# 💱 CambistaOnline - Plataforma Fintech de Cambio de Divisas

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot 3.3.1](https://img.shields.io/badge/Spring%20Boot-3.3.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React 18](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Vite 5](https://img.shields.io/badge/Vite-5-purple.svg)](https://vitejs.dev/)
[![PostgreSQL Neon](https://img.shields.io/badge/PostgreSQL-Neon.tech-blue.svg)](https://neon.tech/)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)
[![Architecture](https://img.shields.io/badge/Architecture-Onion%20%2F%20Hexagonal-darkgreen.svg)]()

**CambistaOnline** es una plataforma web financiera de grado empresarial diseñada para la cotización e intercambio de divisas en tiempo real entre **Dólares (USD)**, **Euros (EUR)** y **Soles (PEN)**. Ofrece soporte integral para **Persona Natural** (DNI/CE) y **Persona Jurídica** (RUC 10/20, Razón Social y Representante Legal), respaldada por una arquitectura robusta, escalable y mantenible basada en **Onion Architecture** (Arquitectura Cebolla).

---

## 🏛️ Descripción Detallada de la Arquitectura ONION (Arquitectura Cebolla)

En plataformas bancarias y fintech de alto volumen, la lógica de negocio y las reglas del motor de tipo de cambio deben **permanecer completamente aisladas de detalles de infraestructura** como frameworks web, librerías ORM o proveedores de base de datos.

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

### 1. Capa de Dominio (`Domain Layer`)
Es el corazón de la aplicación. Contiene objetos POJO puros en Java 17 sin dependencias de Spring Boot, Hibernate o JPA.

- **Entidades de Dominio**: `User`, `ExchangeOrder`, `OrderStatus`, `OperationType`, `CurrencyType`, `CustomerLevel`, `CalculationContext`.
- **Objetos de Valor (Value Objects)**: `Email`, `Password`, `Dni`, `Ruc`.
- **Puertos de Dominio (Domain Ports / Interfaces)**:
  - `UserRepositoryPort`: Contrato para la búsqueda y guardado de usuarios.
  - `ExchangeOrderRepositoryPort`: Contrato para la creación y consulta de órdenes de cambio `TRX-XXXXXX`.
  - `ExchangeRateRepositoryPort`: Contrato para obtener la tasa base SBS.
  - `ExchangeRuleRepositoryPort`: Contrato para reglas de spread, horario y estacionalidad.
  - `UserPointsRepositoryPort`: Contrato para consulta y actualización del saldo de **CambiPuntos**.

### 2. Capa de Aplicación (`Application Layer`)
Orquesta el flujo de datos hacia y desde las entidades de dominio y ejecuta los casos de uso del negocio.

- **Casos de Uso (Use Cases)**:
  - `RegisterUserUseCase`: Registro validado de Persona Natural ("N") y Persona Jurídica ("J").
  - `AuthenticateUserUseCase`: Autenticación de credenciales y generación del token JWT.
  - `CalculateExchangeRateUseCase`: Ejecución del pipeline dinámico de cálculo de tipo de cambio.
  - `CreateExchangeOrderUseCase`: Creación de la orden de cambio, expiración a 15 minutos y gestión del saldo de puntos.
  - `GetMyOrdersUseCase`: Consulta del historial de operaciones del usuario autenticado.
  - `ConfirmTransferUseCase`: Confirmación de transferencia bancaria y actualización a estado `PAYMENT_UPLOADED`.
- **Estrategias y Pipeline de Cálculo (`Strategy Pattern`)**:
  - `ExchangeRateCalculationPipeline`: Orquestador de pasos.
  - `BaseRateStep`, `SpreadStep`, `HourlyRuleStep`, `SeasonalRuleStep`, `PointsRedemptionStep`.
- **Objetos de Transferencia de Datos (DTOs)**:
  - `CalculateRateRequest`, `CalculateRateResponse`, `CreateOrderRequest`, `CreateOrderResponse`, `FxBreakdownResponse`, `OrderSummaryDto`.

### 3. Capa de Infraestructura (`Infrastructure Layer`)
Contiene los adaptadores tecnológicos de entrada (REST Controllers) y salida (JPA Repositories, Spring Security).

- **Adaptadores REST de Entrada (REST Controllers & OpenAPI 3)**:
  - `AuthController` (`/api/v1/auth`): Endpoints de autenticación y perfil.
  - `ExchangeEngineController` (`/api/v1/exchange`, `/api/v1/rates`): Endpoints de cálculo dinámico y desglose.
  - `ExchangeOrderController` (`/api/v1/orders`): Endpoints de gestión de transacciones.
- **Adaptadores de Persistencia de Salida (JPA Adapters & Neon DB)**:
  - `JpaUserRepositoryAdapter`, `ExchangeEnginePersistenceAdapter`, `JpaExchangeOrderRepositoryAdapter`.
  - Repositorios Spring Data JPA (`SpringDataJpaUserRepository`, `SpringDataJpaOrderRepository`, etc.).
- **Seguridad e Identidad**:
  - `SecurityConfig`, `JwtAuthenticationFilter`, `JwtTokenProvider`, `CustomUserDetailsService` con cifrado BCrypt.
- **Migraciones de Base de Datos Flyway**:
  - Scripts SQL versionados desde `V1` hasta `V7` (`tb_operacion`, `tb_usuario_puntos`, `tb_tasa_base`, `tb_spread_nivel`, `tb_horario`, `tb_estacionalidad`, `tb_puntos_config`, `users`).

---

## 📌 Catálogo de Endpoints REST Creados

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
| `POST` | `/api/v1/orders/{orderNumber}/confirm-transfer` | Registra el número de operación bancaria y adjunta la constancia de pago | ✅ |

---

## 🛠️ Tecnologías Aplicadas

### Backend Enterprise (Java 17 + Spring Boot 3)
- **Java 17 LTS**: Uso de *Records*, *Sealed Classes*, *Switch Expressions* y programación orientada a objetos inmutable.
- **Spring Boot 3.3.1**: Framework empresarial para microservicios RESTful.
- **Spring Security 6 + JWT**: Autenticación sin estado (*Stateless*) con tokens firmados HMAC SHA-256 y contraseñas cifradas con BCrypt.
- **Spring Data JPA & Hibernate**: Persistencia relacional orientada a objetos.
- **Flyway Database Migrations**: Control de versiones de base de datos desde `V1` a `V7`.
- **ArchUnit 1.3**: Validación de arquitectura Onion en cada compilación.

### Frontend Moderno (React 18 + Vite + Vanilla CSS)
- **React 18 & Vite 5**: UI interactiva con compilador de producción ultrarrápido.
- **Zustand**: Gestión de estado global persistente (`useAuthStore`, `useFxStore`, `useOrderStore`).
- **Axios**: Cliente HTTP estandarizado con interceptores automáticos Bearer Token.
- **CSS Modules & Variables**: Estilizado limpio con soporte para diseño responsivo horizontal y modo oscuro/claro.

### Base de Datos & Infraestructura
- **PostgreSQL Serverless en Neon Cloud**: Base de datos en la nube con soporte SSL activado.
- **Docker & Docker Compose**: Orquestación de contenedores multi-etapa (`eclipse-temurin:17-jre-alpine` + `nginx:alpine`).

---

## 🚀 Requisitos e Instalación en Desarrollo

### Paso 1: Clonar el Repositorio
```bash
git clone https://github.com/TU_USUARIO/cambista_online.git
cd cambista_online
```

### Paso 2: Configurar las Variables de Entorno (`backend/.env`)
Crea o edita el archivo `backend/.env` con tus credenciales de Neon PostgreSQL:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-young-tooth-ac5b010f-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=tu_usuario_neondb
SPRING_DATASOURCE_PASSWORD=tu_password_de_neon
JWT_SECRET=tu_jwt_secret
JWT_EXPIRATION_MS=3600000
```

---

### Paso 3: Levantar los Contenedores con Docker Compose (Recomendado)

Desde la raíz del repositorio, ejecuta:

```bash
docker compose up --build -d
```

> ℹ️ **Migración Automática**: Al iniciar el contenedor Backend, **Flyway** creará automáticamente las tablas e insertará los datos semilla (`V1` a `V7`).

#### 🌐 URLs de Acceso:
- **Aplicación Web Frontend (React + Nginx)**: 👉 **`http://localhost:3000`**
- **Documentación REST Backend (Swagger UI)**: 👉 **`http://localhost:8080/swagger-ui.html`**

---

## 🔑 Credenciales Demo para Pruebas Rápidas

Para ingresar rápidamente al Cotizador y Flujo de Cambio sin registrar una cuenta manualmente:

- **Correo electrónico**: `demo@cambistaonline.pe`
- **Contraseña**: `demo1234`
- *(En la pantalla de Login encontrarás el botón de 1-clic: `⚡ Ingresar directamente con Demo`)*.

---

## 🧪 Pruebas Automatizadas

### Pruebas de Backend y Arquitectura Onion (ArchUnit + JUnit 5)
```bash
cd backend
mvn test
```

### Validación del Frontend
```bash
cd frontend
npm run build
```
