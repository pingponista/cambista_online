# 💱 CambistaOnline - Plataforma Fintech de Cambio de Divisas

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5-purple.svg)](https://vitejs.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon.tech-blue.svg)](https://neon.tech/)
[![Architecture](https://img.shields.io/badge/Architecture-Onion%20%2F%20Package--by--Feature-darkgreen.svg)]()

**CambistaOnline** es una aplicación web financiera de grado empresarial diseñada para la cotización e intercambio de divisas en tiempo real entre **Dólares (USD)**, **Euros (EUR)** y **Soles (PEN)**. Ofrece soporte integral tanto para **Persona Natural** (DNI/CE) como para **Persona Jurídica** (RUC 10/20, Razón Social y Representante Legal).

Construida sobre una estructura de **Monorepo Ligero** con separación estricta entre el Frontend (`/frontend`) y el Backend (`/backend`).

---

## 🏛️ Estrategia de Monorepo & Arquitectura del Proyecto

```text
cambista_online/
├── backend/                  # API REST Enterprise en Java 17 + Spring Boot 3 (Onion Architecture)
│   ├── src/main/java/        # Código fuente estructurado por Feature (com.cambistaonline.auth.*)
│   ├── src/main/resources/   # application.yml, Logback y Migraciones SQL Flyway (V1, V2, V3)
│   └── src/test/java/        # Pruebas Unitarias y Validación de Arquitectura con ArchUnit
│
└── frontend/                 # Aplicación Web React + Vite + CSS Modules + Zustand
    ├── src/components/       # UI Library (Button, Card, Badge, CurrencySelector, TimerBadge)
    ├── src/features/         # Módulos Funcionales (calculator, exchange, auth, dashboard)
    ├── src/store/            # Estado Global con Zustand (useAuthStore, useFxStore, useOrderStore)
    └── src/services/         # Cliente API Axios estandarizado (apiClient.js, apiAuth.js)
```

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

## 🛠️ Requisitos Previos e Instalación

Asegúrate de tener instalado el siguiente software en tu entorno local:

- **Java JDK 17** (Eclipse Temurin u OpenJDK 17) -> [Descargar](https://adoptium.net/)
- **Apache Maven 3.9+** -> [Descargar](https://maven.apache.org/download.cgi)
- **Node.js 18+ & npm** -> [Descargar](https://nodejs.org/)
- **Git**
- **Cuenta en Neon.tech** (Serverless PostgreSQL) o una base de datos PostgreSQL 16 local.

---

## 🚀 Guía de Despliegue en Entorno Local

### Paso 1: Clonar el Repositorio

```bash
git clone https://github.com/TU_USUARIO/cambista_online.git
cd cambista_online
```

---

### Paso 2: Configurar y Levantar el Backend (Spring Boot 3 + PostgreSQL/Neon)

1. Abre el archivo de configuración en `backend/src/main/resources/application.yml`.
2. Asigna las credenciales de tu base de datos PostgreSQL o **Neon.tech**:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://<TU_HOST_NEON>/neondb?sslmode=require
    username: <TU_USUARIO_NEON>
    password: <TU_PASSWORD_NEON>
    driver-class-name: org.postgresql.Driver
```

3. Ingresa a la carpeta `/backend` y ejecuta la aplicación:

```bash
cd backend
mvn spring-boot:run
```

> ℹ️ **Migración Automática**: Al iniciar, **Flyway** creará automáticamente en tu base de datos la tabla `users`, los índices optimizados y los datos semilla (`V1`, `V2`, `V3`).

4. **Verificar Documentación REST (Swagger UI)**:
   Accede en tu navegador a: 👉 **`http://localhost:8080/swagger-ui.html`**

---

### Paso 3: Configurar y Levantar el Frontend (React + Vite)

1. En una nueva terminal, navega a la carpeta `/frontend`:

```bash
cd frontend
```

2. Instala las dependencias de Node:

```bash
npm install
```

3. Inicia el servidor de desarrollo de Vite:

```bash
npm run dev
```

4. Abre tu navegador en: 👉 **`http://localhost:3000`**

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

- **Domain Layer (`POJO Puro`)**: Cero dependencias de Spring Boot o JPA. Contiene Entidades (`User`), Objetos de Valor (`Email`, `Password`, `Dni`, `Ruc`), Excepciones de Negocio y **Puertos** (interfaces).
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
