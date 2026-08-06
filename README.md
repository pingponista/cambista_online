# 💱 CambistaOnline - Plataforma Fintech de Cambio de Divisas (Arquitectura Hexagonal)

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot 3.3.1](https://img.shields.io/badge/Spring%20Boot-3.3.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React 18](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Vite 5](https://img.shields.io/badge/Vite-5-purple.svg)](https://vitejs.dev/)
[![MongoDB Atlas](https://img.shields.io/badge/MongoDB-Atlas%20NoSQL-green.svg)](https://www.mongodb.com/cloud/atlas)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)
[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%2F%20Ports%20%26%20Adapters-darkgreen.svg)]()

**CambistaOnline** es una plataforma web financiera de grado empresarial diseñada para la cotización e intercambio de divisas en tiempo real entre **Dólares (USD)**, **Euros (EUR)** y **Soles (PEN)**. Soporta **Persona Natural** (DNI/CE) y **Persona Jurídica** (RUC 10/20, Razón Social y Representante Legal), desarrollada bajo **Arquitectura Hexagonal (Puertos y Adaptadores / Ports & Adapters Architecture)** y persistida en **MongoDB Atlas NoSQL**.

---

## 🏛️ Explicación Detallada de la Arquitectura Hexagonal (Ports & Adapters)

Para elevar la mantenibilidad, desacoplamiento y testabilidad a estándares bancarios internacionales, la aplicación fue estructurada bajo **Arquitectura Hexagonal pura** (patrón propuesto por Alistair Cockburn).

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
               |  (MongoDB Atlas Adapters, Spring Security JWT, BCrypt)            |
               +-------------------------------------------------------------------+
```

---

### 🧱 Las 3 Capas Fundamentales del Hexágono

```mermaid
graph TD
    subgraph DrivingAdapters ["1. Adaptadores Primarios / Conductores (Adapters Inbound)"]
        REST["REST Controllers (AuthController, OrderController)"]
    end

    subgraph CoreApplication ["2. Capa de Aplicación y Puertos (Application)"]
        InboundPorts["Puertos de Entrada (Inbound Ports / UseCases)"]
        AppServices["Servicios de Aplicación (Use Case Implementations)"]
        OutboundPorts["Puertos de Salida (Outbound Ports / Persistence Interfaces)"]
    end

    subgraph CoreDomain ["3. Núcleo de Dominio (Domain Core - Pure POJOs)"]
        Entities["Entidades de Dominio (User, ExchangeOrder)"]
        ValueObjects["Objetos de Valor (Email, Dni, Ruc, Password)"]
        DomainServices["Servicios de Dominio (RateCalculationStrategy)"]
    end

    subgraph DrivenAdapters ["4. Adaptadores Secundarios / Conducidos (Adapters Outbound)"]
        MongoAdapters["Adaptadores MongoDB (MongoUserPersistenceAdapter, MongoOrderAdapter)"]
        SecurityAdapters["Adaptadores de Seguridad (BcryptAdapter, JwtProviderAdapter)"]
    end

    REST --> InboundPorts
    InboundPorts --> AppServices
    AppServices --> CoreDomain
    AppServices --> OutboundPorts
    MongoAdapters --> OutboundPorts
    SecurityAdapters --> OutboundPorts
```

---

### 1. Dominio Puro (`com.cambistaonline.*.domain`)
Es el centro del hexágono. Contiene los modelos de negocio y reglas bancarias esenciales sin ninguna dependencia de frameworks externos (sin anotaciones de Spring, sin JPA, sin MongoDB, sin Jackson):

- **Entidades de Dominio**: `User`, `ExchangeOrder`.
- **Objetos de Valor (Value Objects)**: `Email`, `Password`, `Dni`, `Ruc`, `OrderNumber`.
- **Servicios de Dominio**: Estrategias de cálculo de tipo de cambio y nivel de cliente.

### 2. Capa de Aplicación y Puertos (`com.cambistaonline.*.application`)
Orquesta los flujos de uso de la aplicación mediante contratos independientes de la infraestructura:

- **Puertos de Entrada (Inbound Ports / Primary)**: Interfaces que definen lo que la aplicación puede hacer desde el exterior (`RegisterUserUseCase`, `AuthenticateUserUseCase`, `CalculateExchangeRateUseCase`, `CreateExchangeOrderUseCase`).
- **Servicios de Aplicación (`application.service`)**: Implementan los puertos de entrada coordinando la lógica de negocio (`RegisterUserService`, `CreateExchangeOrderService`).
- **Puertos de Salida (Outbound Ports / Secondary)**: Interfaces de persistencia y servicios que la aplicación requiere del exterior (`UserPersistencePort`, `ExchangeOrderPersistencePort`, `UserPointsPersistencePort`, `PasswordEncoderPort`, `JwtTokenPort`).

### 3. Adaptadores Primarios y Secundarios (`com.cambistaonline.*.adapters`)
Conectan la aplicación con tecnologías del mundo exterior:

- **Adaptadores de Entrada (Driving / Inbound)**: Controladores REST HTTP (`AuthController`, `ExchangeEngineController`, `ExchangeOrderController`) que traducen las solicitudes del cliente Web a llamadas a los Puertos de Entrada.
- **Adaptadores de Salida (Driven / Outbound - MongoDB)**: Implementan los Puertos de Salida conectándose a las colecciones de **MongoDB Atlas NoSQL**:
  - `MongoUserPersistenceAdapter` & `SpringDataMongoUserRepository`: Persistencia para la colección `users`.
  - `MongoOrderPersistenceAdapter` & `SpringDataMongoOrderRepository`: Persistencia para la colección `operacion`.
  - `MongoEnginePersistenceAdapter` & `SpringDataMongoUserPointsRepository`: Persistencia para la colección `usuarios_puntos`.
  - `BcryptPasswordEncoderAdapter`: Implementa el hash de contraseñas.
  - `JwtProviderAdapter`: Implementa la generación y validación de JSON Web Tokens (JWT).

---

## 🛡️ Resiliencia y Mapeo Dual NoSQL (MongoDB Atlas)

Para garantizar compatibilidad completa tanto con datos relacionales previa migración como con transacciones creadas de forma nativa en MongoDB:

1. **Mapeo Dual de Nombres de Campos**:
   - `OrderDocument` y `SpringDataMongoOrderRepository` soportan búsquedas y lecturas por ambos formatos de nombres:
     - **Migrados (SQL)**: `correo_user`, `nro_orden`, `tipo_operacion`, `monto_origen`, `monto_destino`, `tasa_final`, `estado`.
     - **Nativos (NoSQL)**: `user_email`, `order_number`, `operation_type`, `amount_sent`, `amount_received`, `exchange_rate`, `status`.
2. **Formateador Resiliente de Fechas**:
   - Mapea de forma segura fechas con espacio (`YYYY-MM-DD HH:MM:SS`), ISO-8601 (`YYYY-MM-DDTHH:MM:SS`) o timestamps de Mongo BSON.

---

## 🧪 Pruebas de Arquitectura Automatizadas (`ArchUnit 1.3`)

Se implementó la suite **`HexagonalArchitectureTest.java`** que verifica automáticamente en cada compilación `mvn test` que no existan violaciones de acoplamiento:

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

## 🐳 Guía de Despliegue con Docker y Docker Compose

Toda la aplicación (Backend Java Spring Boot + Frontend React Nginx) se despliega mediante contenedores Docker listos para producción.

### 📋 Requisitos Previos:
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (con Docker Engine y Docker Compose instalados).
- Instancia activa de **MongoDB Atlas** (o MongoDB local).

---

### Paso 1: Configurar las Variables de Entorno (`backend/.env`)

Edita o crea el archivo **`backend/.env`** con tu URI de MongoDB Atlas y tu clave secreta JWT:

```env
SPRING_DATA_MONGODB_URI=mongodb+srv://admin:admin@cambistaonilne.vgbiyuj.mongodb.net/sample_mflix?retryWrites=true&w=majority&appName=cambistaOnilne
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-young-tooth-ac5b010f-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=npg_9vFz6gRJaCHP
JWT_SECRET=tu_jwt_secret
JWT_EXPIRATION_MS=3600000
```

---

### Paso 2: Desplegar la Aplicación con Docker Compose

Ejecuta el siguiente comando en la raíz del repositorio:

```bash
docker compose up --build -d
```

Este comando:
1. Compilará la imagen de Docker del backend Java 17 (`cambista-backend-app`).
2. Compilará los assets de producción de React + Vite y configurará el servidor web Nginx (`cambista-frontend-app`).
3. Iniciará ambos contenedores en segundo plano.

---

### Paso 3: Verificar el Estado de los Contenedores

```bash
# Ver estado de los servicios
docker compose ps

# Ver logs en tiempo real del backend
docker logs cambista-backend-app -f

# Ver logs en tiempo real del frontend
docker logs cambista-frontend-app -f
```

---

### 🌐 URLs de Acceso a la Aplicación:

- **Frontend Web App (React 18)**: 👉 `http://localhost:3000`
- **Backend REST API**: 👉 `http://localhost:8080`
- **Documentación Swagger / OpenAPI**: 👉 `http://localhost:8080/swagger-ui.html`

---

### 🛑 Detener y Limpiar Contenedores:

```bash
docker compose down
```

---

## 📡 Endpoints Principales de la API REST

### Auth API (`/api/v1/auth`)
- **`POST /api/v1/auth/register`**: Registro de usuarios Persona Natural / Jurídica.
- **`POST /api/v1/auth/login`**: Autenticación y emisión de token JWT.
- **`GET /api/v1/auth/me`**: Obtención del perfil del usuario autenticado.

### Exchange Engine API (`/api/v1/exchange`)
- **`POST /api/v1/exchange/calculate`**: Cálculo dinámico de tasa de cambio (con descuentos por CambiPuntos, hora pico y nivel de cliente).

### Exchange Order API (`/api/v1/orders`)
- **`POST /api/v1/orders`**: Creación de nueva orden de cambio de divisas.
- **`GET /api/v1/orders`**: Obtención del historial de operaciones del usuario.
- **`POST /api/v1/orders/confirm`**: Confirmación de transferencia bancaria realizada por el cliente.
