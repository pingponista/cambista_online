# 💱 CambistaOnline - Plataforma Fintech de Cambio de Divisas

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5-purple.svg)](https://vitejs.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon.tech-blue.svg)](https://neon.tech/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)
[![Architecture](https://img.shields.io/badge/Architecture-Onion%20%2F%20Package--by--Feature-darkgreen.svg)]()

**CambistaOnline** es una aplicación web financiera de grado empresarial diseñada para la cotización e intercambio de divisas en tiempo real entre **Dólares (USD)**, **Euros (EUR)** y **Soles (PEN)**. Ofrece soporte integral tanto para **Persona Natural** (DNI/CE) como para **Persona Jurídica** (RUC 10/20, Razón Social y Representante Legal).

Construida sobre una estructura de **Monorepo Ligero** con separación estricta entre el Frontend (`/frontend`) y el Backend (`/backend`).

---

## 🏛️ Estrategia de Monorepo & Arquitectura del Proyecto

```text
cambista_online/
├── docker-compose.yml        # Orquestación de contenedores Frontend y Backend
├── .dockerignore             # Exclusión de librerías locales durante el build de Docker
├── .env.example              # Plantilla segura de variables de entorno para GitHub
├── backend/                  # API REST Enterprise en Java 17 + Spring Boot 3 (Onion Architecture)
│   ├── Dockerfile            # Multi-stage build (Maven 3.9 + JRE 17 Alpine)
│   ├── src/main/java/        # Código fuente estructurado por Feature (com.cambistaonline.auth.*)
│   ├── src/main/resources/   # application.yml, Logback y Migraciones SQL Flyway (V1, V2, V3)
│   └── src/test/java/        # Pruebas Unitarias y Validación de Arquitectura con ArchUnit
│
└── frontend/                 # Aplicación Web React + Vite + CSS Modules + Zustand
    ├── Dockerfile            # Multi-stage build (Node 20 Slim -> Nginx Alpine)
    ├── nginx.conf            # Servidor Web y Reverse Proxy (/api -> backend:8080)
    ├── src/components/       # UI Library (Button, Card, Badge, CurrencySelector, TimerBadge)
    ├── src/features/         # Módulos Funcionales (calculator, exchange, auth, dashboard)
    ├── src/store/            # Estado Global con Zustand (useAuthStore, useFxStore, useOrderStore)
    └── src/services/         # Cliente API Axios estandarizado (apiClient.js, apiAuth.js)
```

---

## 🐳 Importancia del uso de Docker en esta Arquitectura

El uso de **Docker y Docker Compose** en este proyecto no es solo una herramienta de despliegue, sino una **pieza fundamental para garantizar la integridad arquitectónica y operativa del sistema**:

1. **Aislamiento Estricto de la Arquitectura Onion**:
   El núcleo de dominio en Java 17 (`Domain Layer`) no posee ninguna dependencia de framework ni del sistema operativo subyacente. Docker encapsula la JVM dentro de un contenedor inmutable (`eclipse-temurin:17-jre-alpine`), garantizando que la aplicación se comporte exactamente igual sin importar el sistema operativo del desarrollador (Windows, macOS o Linux).

2. **Paridad Total de Entornos (*Environment Parity*)**:
   Elimina el clásico problema *"en mi máquina sí funciona"*. Las versiones de Node.js (20 Slim), Nginx (Alpine), Java (17 Alpine) y compiladores están fijadas en las imágenes de Docker, asegurando que todos los desarrolladores ejecuten la misma versión de software y binarios en cualquier momento.

3. **Seguridad y Aislamiento de Red**:
   El contenedor del Frontend expone públicamente Nginx en el puerto `3000` y actúa como un **Reverse Proxy interno** hacia el puerto `8080` del contenedor Backend. Esto blinda el backend y previene la exposición directa de servicios de base de datos o puertos internos.

4. **Despliegue Cero-Configuración**:
   Un nuevo desarrollador no necesita instalar manualmente Node, Nginx, Java JDK ni Maven en su computadora. Únicamente requiere **Docker Desktop** y ejecutar `docker compose up --build -d`.

---

## 🌟 Características Principales del Sistema

### 1. Cotizador Inteligente y Desglose de Tasa de Cambio
- **Tasa en Vivo con Contador de Bloqueo**: Temporizador de 5 minutos con actualización de tasa y botón de congelamiento de precio.
- **Módulo de CambiPuntos ⭐**: Slider interactivo para canjear puntos de fidelidad y mejorar el tipo de cambio recibido en tiempo real (`+0.00XX`).
- **Desglose Transparente de Tasa**: Visualización detallada de `TC base (SBS)`, `Spread Preferencial`, `Ajuste Horario`, `Ajuste Estacional` y `Canje de Puntos`.
- **Cálculo de Ahorro**: Estimador de ahorro comparativo en tiempo real frente a los bancos tradicionales.

### 2. Flujo de Intercambio en 3 Pasos (Stepper UI)
- **Paso 1 (Cuentas)**: Selección de banco de origen y banco de destino para la transferencia.
- **Paso 2 (Transferencia)**: Copia rápida en 1-clic del número de cuenta/CCI del Cambista y carga del comprobante de pago.
- **Paso 3 (Constancia Digital)**: Rastreador de estado de la operación en tiempo real con resumen de la transacción.

### 3. Autenticación Empresarial & Roles de Usuario
- **Registro Dual**: Soporte diferenciado para **Persona Natural** (`rol: "N"`) y **Persona Jurídica** (`rol: "J"`).
- **Seguridad Stateless (Spring Security 6 + JWT)**: Autenticación por firma JWT con hash BCrypt y manejo de sesiones sin estado.
- **Modo Demo Rápido**: Credenciales de desarrollo integradas para saltearse autenticación durante pruebas.

---

## 🛠️ Requisitos Previos

- **Docker Desktop** (con soporte para WSL2 o Virtualización activo) -> [Descargar Docker Desktop](https://www.docker.com/products/docker-desktop/)
- **Git**
- *(Opcional para desarrollo local sin Docker: Java JDK 17, Apache Maven 3.9+, Node.js 18+)*.

---

## 🚀 Despliegue con Docker Compose (Opción Recomendada)

### Paso 1: Clonar el Repositorio

```bash
git clone https://github.com/TU_USUARIO/cambista_online.git
cd cambista_online
```

---

### Paso 2: Crear el archivo `.env` local

Copia la plantilla de variables de entorno hacia `/backend/.env`:

```powershell
cd backend
Copy-Item .env.example .env
```

Ingresa tus credenciales de base de datos PostgreSQL o **Neon.tech** dentro de `backend/.env`:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-young-tooth-ac5b010f-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=tu_password_de_neon
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION_MS=3600000
```

---

### Paso 3: Levantar los Contenedores con Docker Compose

Desde la raíz del repositorio (`cambista_online`), ejecuta:

```powershell
docker compose up --build -d
```

> ℹ️ **Migración Automática**: Al iniciar el contenedor Backend, **Flyway** creará automáticamente en Neon la tabla `users`, los índices optimizados y los datos semilla (`V1`, `V2`, `V3`).

#### 🌐 URLs de Acceso:
- **Aplicación Web Frontend (React + Nginx)**: 👉 **`http://localhost:3000`**
- **Documentación REST Backend (Swagger UI)**: 👉 **`http://localhost:8080/swagger-ui.html`**

---

## 🔋 Gestión de Recursos y Ahorro de Batería en Docker

Para optimizar el rendimiento y evitar el consumo innecesario de energía/batería en tu laptop:

### Pausar y Reanudar Rápidamente (Sin destruir contenedores)
- **Pausar ejecución**:
  ```powershell
  docker compose stop
  ```
- **Reanudar en 1 segundo**:
  ```powershell
  docker compose start
  ```

### Apagar los Contenedores por Completo
```powershell
docker compose down
```

### Cerrar el Servidor de Docker Desktop
1. Ve a la barra de tareas de Windows (esquina inferior derecha junto al reloj).
2. Haz clic derecho en el icono de la **ballena de Docker** 🐳.
3. Selecciona **`Quit Docker Desktop`**.

---

## 🔑 Credenciales Demo para Pruebas Rápidas

Para ingresar rápidamente al Cotizador y Flujo de Cambio sin necesidad de registrar una cuenta manualmente:

- **Correo electrónico**: `demo@cambistaonline.pe`
- **Contraseña**: `demo1234`
- *(En la pantalla de Login encontrarás el botón de 1-clic: `⚡ Ingresar directamente con Demo`)*.

---

## 📐 Diseño Arquitectónico del Backend (`Onion Architecture`)

El backend aplica **Onion Architecture estricta** e **Inversión de Dependencias (DIP)**:

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

- **Domain Layer (`POJO Puro`)**: Cero dependencias de Spring Boot o JPA. Contiene Entidades (`User`), Objetos de Valor (`Email`, `Password`, `Dni`, `Ruc`), Excepciones de Negocio y **Puertos** (interfaces). Validado mecánicamente con **ArchUnit**.
- **Application Layer**: Casos de Uso (`RegisterUserUseCase`, `AuthenticateUserUseCase`, `GetCurrentUserUseCase`), DTOs y Mappers.
- **Infrastructure Layer**: Adaptadores tecnológicos de Spring Data JPA, Spring Security 6 JWT, controladores REST OpenAPI 3 y scripts Flyway.

---

## 🧪 Pruebas Automatizadas y Calidad de Código

### Ejecución de Pruebas en Backend (JUnit 5 + Mockito + ArchUnit)
Para ejecutar la suite completa de pruebas unitarias y la **validación mecánica de la arquitectura Onion con ArchUnit**:

```bash
cd backend
mvn test
```

### Compilación y Validación del Frontend
Para verificar la compilación de producción del Frontend:

```bash
cd frontend
npm run build
```

---

## 📌 Endpoints REST Principales (`/api/v1/auth`)

| Método | Endpoint | Descripción | Requiere Auth |
| :--- | :--- | :--- | :---: |
| `POST` | `/api/v1/auth/register` | Registro de Persona Natural ("N") o Jurídica ("J") | ❌ |
| `POST` | `/api/v1/auth/login` | Autenticación y generación de JWT Access Token | ❌ |
| `GET` | `/api/v1/auth/me` | Obtiene el perfil del usuario autenticado | ✅ (Bearer JWT) |
| `GET` | `/rates` | Consulta tipo de cambio en tiempo real | ❌ |
| `GET` | `/rates/breakdown` | Obtiene desglose de tasa y saldo de CambiPuntos | ✅ (Bearer JWT) |
