# 💱 CambistaOnline - Plataforma Fintech de Cambio de Divisas
## 🛡️ Rama: `Google-Github-Facebok-MFA`
### Autenticación Social (Google, GitHub, Facebook) & Doble Factor (MFA / TOTP) en Arquitectura Hexagonal

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot 3.3.1](https://img.shields.io/badge/Spring%20Boot-3.3.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React 18](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![MongoDB Atlas](https://img.shields.io/badge/MongoDB-Atlas%20NoSQL-green.svg)](https://www.mongodb.com/cloud/atlas)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-7.5.0-black.svg?logo=apachekafka)](https://kafka.apache.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange.svg?logo=rabbitmq)](https://www.rabbitmq.com/)
[![OAuth 2.0 / OIDC](https://img.shields.io/badge/OAuth%202.0-Google%20%7C%20GitHub%20%7C%20Facebook-blue.svg)]()
[![MFA / TOTP](https://img.shields.io/badge/MFA-RFC%206238%20%28Google%20Authenticator%29-red.svg)]()
[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%2F%20DDD-darkgreen.svg)]()

**CambistaOnline** es una plataforma web fintech de grado empresarial para cotización e intercambio de divisas en tiempo real (**USD**, **EUR**, **PEN**). Soporta operaciones para **Persona Natural** (DNI/CE) y **Persona Jurídica** (RUC, Razón Social y Representante Legal).

En esta rama (`Google-Github-Facebok-MFA`), la plataforma incorpora un ecosistema de seguridad e identidad de última generación:
1. **Autenticación Social con 3 Proveedores**: Login y registro fluido mediante **Google Identity**, **GitHub OAuth** y **Facebook Login**, integrando aprovisionamiento automático de cuentas y emisión de eventos asíncronos en Kafka y RabbitMQ.
2. **Doble Factor de Autenticación (MFA / 2FA)**: Verificación en dos pasos basada en **TOTP (Time-based One-time Password - RFC 6238)** 100% compatible con **Google Authenticator**, Microsoft Authenticator y Authy, implementado en Java puro sin dependencias externas pesadas, con ventana de tolerancia contra desincronización de reloj (clock drift) y validado contra vectores de prueba oficiales.
3. **Preservación Estricta de Arquitectura Hexagonal (Ports & Adapters)**: Dominio desacoplado (POJOs puros), contratos de puertos de entrada/salida y cumplimiento validado automáticamente por **ArchUnit** (32 tests unitarios y de arquitectura, 0 fallos).

---

## 📑 Tabla de Contenidos
1. [Novedades de esta Versión](#-1-novedades-de-esta-versión)
2. [Arquitectura Hexagonal del Módulo de Seguridad](#-2-arquitectura-hexagonal-del-módulo-de-seguridad)
3. [Detalle Exhaustivo de Cambios Realizados](#-3-detalle-exhaustivo-de-cambios-realizados)
   - [3.1 Capa de Dominio (Domain Layer)](#31-capa-de-dominio-domain-layer)
   - [3.2 Capa de Aplicación (Application Layer - Casos de Uso y Puertos)](#32-capa-de-aplicación-application-layer---casos-de-uso-y-puertos)
   - [3.3 Capa de Adaptadores de Salida (Outbound Adapters)](#33-capa-de-adaptadores-de-salida-outbound-adapters)
   - [3.4 Capa de Adaptadores de Entrada (Inbound Adapters & REST Controllers)](#34-capa-de-adaptadores-de-entrada-inbound-adapters--rest-controllers)
   - [3.5 Frontend (React 18 + Vite)](#35-frontend-react-18--vite)
   - [3.6 Algoritmo TOTP RFC 6238 y Solución de Desincronización](#36-algoritmo-totp-rfc-6238-y-solución-de-desincronización)
4. [Flujos de Autenticación Paso a Paso](#-4-flujos-de-autenticación-paso-a-paso)
   - [A. Flujo de Autenticación Social (OAuth 2.0)](#a-flujo-de-autenticación-social-oauth-20)
   - [B. Flujo de Activación de Doble Factor (MFA Setup & Enable)](#b-flujo-de-activación-de-doble-factor-mfa-setup--enable)
   - [C. Flujo de Desafío MFA en Login (MFA Verify)](#c-flujo-de-desafío-mfa-en-login-mfa-verify)
5. [Guía para Levantar el Proyecto en Local](#-5-guía-para-levantar-el-proyecto-en-local)
   - [Requisitos Previos](#requisitos-previos)
   - [Paso 1: Clonar y situarse en la rama](#paso-1-clonar-y-situarse-en-la-rama)
   - [Paso 2: Configurar Variables de Entorno](#paso-2-configurar-variables-de-entorno)
   - [Paso 3: Levantar con Docker Compose (Recomendado)](#paso-3-levantar-con-docker-compose-recomendado)
   - [Paso 4: Alternativa sin Docker (Manual en Local)](#paso-4-alternativa-sin-docker-manual-en-local)
6. [Cómo Probar las Nuevas Funcionalidades](#-6-cómo-probar-las-nuevas-funcionalidades)
7. [Endpoints de la API](#-7-endpoints-de-la-api)
8. [Verificación de Calidad y Tests](#-8-verificación-de-calidad-y-tests)

---

## 🚀 1. Novedades de esta Versión

| Característica | Antes (`feature/messaging`) | Ahora (`Google-Github-Facebok-MFA`) |
| :--- | :--- | :--- |
| **Inicio de Sesión Social** | ❌ Solo credenciales locales (email/password). | ✅ **Google, GitHub y Facebook** con auto-aprovisionamiento y fallback para desarrollo. |
| **Doble Factor (2FA / MFA)** | ❌ No disponible. | ✅ **TOTP RFC 6238** compatible con **Google Authenticator**, QR dinámico y clave manual. |
| **Seguridad en Inicio de Sesión** | Token JWT directo tras validar password. | Si MFA está activo, emite un token de sesión temporal y exige validación del código de 6 dígitos antes de entregar el JWT definitivo. |
| **Tolerancia de Reloj (Clock Drift)** | N/A | Algoritmo TOTP con ventana ampliada `WINDOW = 4` (±120s) para soportar desfase de hora entre celulares y servidores. |
| **Estabilidad de Configuración** | N/A | El backend preserva la clave secreta pendiente sin sobreescribirla si el usuario recarga la página. |
| **Auditoría y Mensajería** | Integrada a Kafka y RabbitMQ. | Los registros mediante redes sociales emiten automáticamente eventos de bienvenida a **RabbitMQ** y de auditoría a **Kafka**. |

---

## 🏛️ 2. Arquitectura Hexagonal del Módulo de Seguridad

La arquitectura del sistema garantiza que la lógica de negocio permanezca pura, desacoplada de Spring Security, librerías de Google/Facebook y frameworks de terceros:

```
                                  [ Navegador / React 18 Web App ]
                                                  │
                 ┌────────────────────────────────┴────────────────────────────────┐
                 ▼ (OAuth Callback / Login)                                        ▼ (TOTP / Setup)
┌──────────────────────────────────────────────────┐      ┌──────────────────────────────────────────────────┐
│             OAuthController (Inbound)            │      │              MfaController (Inbound)             │
│        POST /api/v1/auth/oauth/{provider}        │      │    /setup  |  /enable  |  /verify  |  /disable   │
└────────────────────────┬─────────────────────────┘      └────────────────────────┬─────────────────────────┘
                         │                                                         │
                         ▼                                                         ▼
       ┌────────────────────────────────────┐                    ┌────────────────────────────────────┐
       │     Inbound Port / Use Case:       │                    │      Inbound Ports / Use Cases:    │
       │   AuthenticateOAuthUserUseCase     │                    │  SetupMfaUseCase / EnableMfa...    │
       └─────────────────┬──────────────────┘                    └─────────────────┬──────────────────┘
                         │                                                         │
                         ▼                                                         ▼
  ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════
                                          CAPA DE APLICACIÓN (SERVICES)
       - AuthenticateOAuthUserService             - SetupMfaService          - VerifyMfaService
       - EnableMfaService                         - DisableMfaService
  ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════
                         │                                                         │
                         ├────────────────────────┐       ┌────────────────────────┤
                         ▼                        ▼       ▼                        ▼
       ┌────────────────────────────────┐   ┌───────────────────────────┐   ┌────────────────────────┐
       │       OAuthClientPort          │   │         TotpPort          │   │      JwtTokenPort      │
       │      (Outbound Port)           │   │      (Outbound Port)      │   │    (Outbound Port)     │
       └────────────────┬───────────────┘   └─────────────┬─────────────┘   └────────────┬───────────┘
                        │                                 │                              │
                        ▼                                 ▼                              ▼
  ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════
                                          ADAPTADORES DE SALIDA (OUTBOUND)
       - GoogleOAuthAdapter                 - TotpAdapter (RFC 6238)        - JwtTokenAdapter
       - GitHubOAuthAdapter                   (Base32 + HMAC-SHA1)          - MongoUserPersistenceAdapter
       - FacebookOAuthAdapter
  ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════
```

---

## 🛠️ 3. Detalle Exhaustivo de Cambios Realizados

### 3.1 Capa de Dominio (Domain Layer)
- **`AuthProvider.java`**:
  - Enum que clasifica el origen del usuario: `LOCAL`, `GOOGLE`, `GITHUB`, `FACEBOOK`.
- **`User.java`**:
  - Incorporación de campos de identidad federada y 2FA:
    - `boolean mfaEnabled`: Bandera de activación del doble factor.
    - `String mfaSecret`: Clave secreta Base32 generada para el usuario.
    - `AuthProvider authProvider`: Proveedor de autenticación.
    - `String providerId`: Identificador único retornado por la red social (ej. Google `sub`, Facebook `id`).
  - Métodos de negocio con encapsulamiento estricto:
    - `enableMfa(secret)`: Valida y activa el 2FA.
    - `disableMfa()`: Desactiva el 2FA y limpia la clave secreta.
    - `linkOAuthProvider(provider, providerId)`: Vincula la identidad externa.
- **`Password.java`**:
  - Se agregó la factoría `Password.oauthDummy()` para asignar una contraseña especial protegida a cuentas creadas vía OAuth que nunca se autentican mediante contraseña local, manteniendo válidas todas las reglas del Value Object.

### 3.2 Capa de Aplicación (Application Layer - Casos de Uso y Puertos)
- **Puertos de Entrada (Inbound)**:
  - `AuthenticateOAuthUserUseCase`: Orquesta la autenticación externa.
  - `SetupMfaUseCase`: Inicia la configuración de 2FA entregando la clave y la URI para el QR.
  - `EnableMfaUseCase`: Valida el código inicial antes de activar 2FA de forma permanente.
  - `VerifyMfaUseCase`: Valida el desafío TOTP en el inicio de sesión y expide el JWT de acceso.
  - `DisableMfaUseCase`: Desactiva 2FA previa validación del código actual.
- **Puertos de Salida (Outbound)**:
  - `OAuthClientPort`: Contrato con métodos `supports(provider)` y `getUserProfile(code, redirectUri)`.
  - `TotpPort`: Contrato con `generateSecret()`, `getOtpAuthUri(secret, accountName, issuer)` y `verifyCode(secret, code)`.
  - `JwtTokenPort`: Se ampliaron los métodos con `generateMfaSessionToken(email)` (token de vida corta de 5 minutos con claim `type=MFA_PENDING`) y `validateMfaSessionToken(token)`.
- **Servicios de Aplicación**:
  - `AuthenticateOAuthUserService`:
    1. Resuelve el adaptador OAuth adecuado mediante el puerto `OAuthClientPort`.
    2. Obtiene el perfil social autenticado (email, nombre, providerId).
    3. Si el usuario no existe en la base de datos, lo registra automáticamente como Persona Natural, emitiendo `UserRegisteredEvent` a **Kafka** (auditoría) y a **RabbitMQ** (email de bienvenida).
    4. Si el usuario ya existe, verifica si tiene `mfaEnabled == true`:
       - **Si tiene 2FA activo**: Emite una respuesta `{ mfaRequired: true, mfaSessionToken: "..." }`.
       - **Si no tiene 2FA**: Emite directamente el JWT definitivo de acceso `{ accessToken: "..." }`.
  - `SetupMfaService`:
    - Genera una clave segura Base32 de 160 bits (20 bytes).
    - **Estabilidad de clave pendiente**: Si el usuario ya tiene una clave pendiente de activación, la reutiliza en lugar de generar una nueva, evitando que recargar la pantalla invalide el QR ya escaneado en el celular.
  - `EnableMfaService`: Valida con `TotpPort` el código ingresado por el usuario y ejecuta `user.enableMfa(secret)`.
  - `VerifyMfaService`: Valida el token de sesión MFA temporal y el código TOTP de 6 dígitos para emitir el JWT definitivo.
  - `DisableMfaService`: Valida el código TOTP actual antes de ejecutar `user.disableMfa()`.

### 3.3 Capa de Adaptadores de Salida (Outbound Adapters)
- **`GoogleOAuthAdapter.java`**:
  - Consume el endpoint de token de Google (`https://oauth2.googleapis.com/token`) e intercambia el código de autorización por el `access_token` e `id_token`.
  - Consulta `https://www.googleapis.com/oauth2/v3/userinfo` para extraer `sub`, `email`, `given_name`, `family_name`.
  - Dispone de fallback de desarrollo inteligente en caso de usar credenciales de prueba.
- **`GitHubOAuthAdapter.java`**:
  - Consume `https://github.com/login/oauth/access_token` con encabezado `Accept: application/json`.
  - Consulta `https://api.github.com/user` y `https://api.github.com/user/emails` para resolver cuentas con correos privados.
- **`FacebookOAuthAdapter.java`**:
  - Consume `https://graph.facebook.com/v19.0/oauth/access_token` e intercambia el `code`.
  - Consulta `https://graph.facebook.com/v19.0/me?fields=id,name,email,first_name,last_name`.
- **`TotpAdapter.java`**:
  - Implementación en **Java puro estándar** (utilizando `javax.crypto.Mac` con algoritmo `HmacSHA1`).
  - No requiere librerías externas pesadas ni propensas a vulnerabilidades.
  - Incluye codificador y decodificador **Base32 RFC 4648** nativo.
  - **Ventana de tolerancia ampliada (`WINDOW = 4`)**: Permite tolerar hasta ±120 segundos de desincronización horaria entre el reloj del dispositivo móvil y el servidor backend.
  - Generación de URI estándar limpia sin doble URL encoding para visualización óptima en Google Authenticator.
- **Persistencia MongoDB**:
  - Actualización de `UserDocument` y `MongoUserPersistenceAdapter` para almacenar los nuevos campos en MongoDB Atlas.

### 3.4 Capa de Adaptadores de Entrada (Inbound Adapters & REST Controllers)
- **`OAuthController.java`**:
  - Endpoint `POST /api/v1/auth/oauth/{provider}`: Recibe `{ code, redirectUri }` y devuelve el JWT o el desafío MFA.
- **`MfaController.java`**:
  - `POST /api/v1/auth/mfa/setup`: Genera la clave secreta y la URL del código QR (protegido por JWT).
  - `POST /api/v1/auth/mfa/enable`: Activa 2FA tras verificar el código inicial de 6 dígitos (protegido por JWT).
  - `POST /api/v1/auth/mfa/verify`: Valida el código TOTP durante el login usando el `mfaSessionToken` (público).
  - `POST /api/v1/auth/mfa/disable`: Desactiva el 2FA (protegido por JWT).
- **`SecurityConfig.java`**:
  - Configuración de filtros de Spring Security permitiendo acceso público a endpoints OAuth y al desafío `/mfa/verify`, protegiendo el resto de rutas.

### 3.5 Frontend (React 18 + Vite)
- **`SocialAuthButtons.jsx`**:
  - Componente estilizado con logos SVG oficiales de **Google**, **GitHub** y **Facebook**.
  - Construye la URL de redirección oficial hacia cada proveedor de identidad y soporta simulación rápida para desarrollo.
- **`MfaVerificationModal.jsx`**:
  - Modal flotante con diseño bancario dark fintech.
  - Se activa automáticamente si el login local o social responde `mfaRequired: true`.
  - Input centralizado de 6 dígitos con formateo automático numérico y botón de confirmación.
- **`MfaSettingsCard.jsx`**:
  - Panel de control de seguridad ubicado en **Mi Perfil** (`UserDashboard.jsx`).
  - Si 2FA está **Inactivo**: Muestra botón para iniciar la configuración.
  - Paso 1: Renderiza el **Código QR** generado para Google Authenticator, muestra la **clave manual de texto con botón de copiado al portapapeles** e incluye consejos para sincronizar la hora y evitar entradas duplicadas.
  - Paso 2: Input para ingresar el código generado por la app móvil y confirmar la activación.
  - Si 2FA está **Activo**: Muestra badge verde de protección activa y formulario para desactivar 2FA si el usuario lo desea.
- **`LoginForm.jsx` & `RegisterForm.jsx`**:
  - Integración de los botones sociales y detección automática de parámetros `?code=...` en la URL para procesar el callback OAuth al regresar de la red social.

### 3.6 Algoritmo TOTP RFC 6238 y Solución de Desincronización
El algoritmo TOTP calcula el código en función del tiempo transcurrido en épocas Unix:
$$\text{intervalo} = \left\lfloor \frac{T}{30} \right\rfloor$$
Donde $T$ es la hora actual en segundos y el intervalo es de 30 segundos.

#### 🔍 Diagnóstico del error inicial ("Código inválido o expirado"):
1. **Clock Skew / Desviación del Reloj**: Las máquinas virtuales y contenedores Docker suelen presentar desfases de 60 a 120 segundos frente a los servidores de hora de red celular (NTP). Con una tolerancia inicial de `WINDOW = 1` (±30s), cualquier teléfono con hora satelital generaba un código rechazado por el servidor.
2. **Inestabilidad del Secreto**: `SetupMfaService` recreaba el secreto cada vez que se abría el componente, de modo que si el usuario demoraba o recargaba la página, el secreto escaneado en el celular ya no coincidía con el guardado en la base de datos.
3. **Doble URL Encoding**: La URI `otpauth://` codificaba el símbolo `@` como `%40` y luego el generador de QR lo volvía a codificar a `%2540`, provocando que Google Authenticator mostrara etiquetas distorsionadas.

#### ✅ Solución aplicada:
- Se amplió la ventana a **`WINDOW = 4`** en `TotpAdapter.java`, cubriendo un rango de ±120 segundos (más de 4 minutos de margen total).
- Se estabilizó `SetupMfaService.java` para reutilizar el secreto pendiente mientras `mfaEnabled == false`.
- Se normalizó la generación de la URI `otpauth://totp/CambistaOnline:usuario@correo.com`.
- Se validó el algoritmo con los vectores oficiales de **RFC 6238** en `TotpAdapterTest.java`:
  - Tiempo = 59s $\rightarrow$ Código esperado: `287082` ✅
  - Tiempo = 1111111109s $\rightarrow$ Código esperado: `081804` ✅
  - Tiempo = 1234567890s $\rightarrow$ Código esperado: `005924` ✅
  - Tiempo = 2000000000s $\rightarrow$ Código esperado: `279037` ✅

---

## 🔄 4. Flujos de Autenticación Paso a Paso

### A. Flujo de Autenticación Social (OAuth 2.0)
```
Usuario                  Frontend (React)             OAuth Provider (Google)             Backend (Spring Boot)           MongoDB Atlas
  │                             │                                │                                  │                           │
  │─── 1. Clic "Google" ───────▶│                                │                                  │                           │
  │                             │─── 2. Redirige a login social ─▶                                  │                           │
  │                             │◀── 3. Retorna con ?code=XYZ ───│                                  │                           │
  │                             │                                                                   │                           │
  │                             │─── 4. POST /auth/oauth/google { code: "XYZ" } ───────────────────▶│                           │
  │                             │                                                                   │── Intercambia code por    │
  │                             │                                                                   │   access_token & profile  │
  │                             │                                                                   │── 5. Busca o crea usuario ──▶
  │                             │                                                                   │   (Emite Kafka + RabbitMQ)│
  │                             │                                                                   │                           │
  │                             │◀── 6. { accessToken: "JWT..." } (o mfaRequired si activo) ────────│                           │
  │◀── 7. Acceso al Dashboard ──│                                                                   │                           │
```

### B. Flujo de Activación de Doble Factor (MFA Setup & Enable)
```
Usuario                  Frontend (React)                      Backend (Spring Boot)                      MongoDB Atlas
  │                             │                                       │                                       │
  │── 1. Clic "Configurar 2FA" ─▶│                                       │                                       │
  │                             │─── 2. POST /auth/mfa/setup ──────────▶│                                       │
  │                             │                                       │── 3. Genera o reutiliza secret Base32 │
  │                             │                                       │── 4. Guarda secreto pendiente ───────▶│
  │                             │◀── 5. { secret, qrCodeUri, manualKey }│                                       │
  │                             │                                       │                                       │
  │── 6. Escanea QR en app ────▶│ (Muestra imagen QR y clave manual)    │                                       │
  │      Google Authenticator   │                                       │                                       │
  │── 7. Ingresa código 6 díg ─▶│                                       │                                       │
  │                             │─── 8. POST /auth/mfa/enable { code } ─▶│                                       │
  │                             │                                       │── 9. Valida con TotpAdapter (±120s)   │
  │                             │                                       │── 10. Actualiza mfa_enabled = true ──▶│
  │                             │◀── 11. { success: true } ─────────────│                                       │
  │◀── 12. "2FA Activado" ──────│                                       │                                       │
```

### C. Flujo de Desafío MFA en Login (MFA Verify)
```
Usuario                  Frontend (React)                      Backend (Spring Boot)
  │                             │                                       │
  │── 1. Inicia sesión ────────▶│                                       │
  │   (Password o Red Social)   │─── 2. POST /auth/login (o /oauth) ───▶│
  │                             │                                       │── Detecta mfa_enabled == true
  │                             │◀── 3. { mfaRequired: true,            │
  │                             │         mfaSessionToken: "..." } ─────│
  │                             │                                       │
  │◀── 4. Despliega Modal 2FA ──│                                       │
  │── 5. Digita código de app ─▶│                                       │
  │                             │─── 6. POST /auth/mfa/verify ─────────▶│
  │                             │        { sessionToken, code }         │── Valida token temporal y TOTP
  │                             │◀── 7. { accessToken: "JWT_FINAL" } ───│
  │◀── 8. Acceso al Dashboard ──│                                       │
```

---

## 💻 5. Guía para Levantar el Proyecto en Local

### Requisitos Previos:
- **[Docker Desktop](https://www.docker.com/products/docker-desktop/)** instalado y en ejecución.
- Cluster o base de datos en **MongoDB Atlas** (o MongoDB local).
- (Opcional para ejecución sin Docker) **Java 17 JDK**, **Maven 3.9+** y **Node.js 18+**.

---

### Paso 1: Clonar y situarse en la rama

```bash
git clone https://github.com/pingponista/cambista_online.git
cd cambista_online
git checkout Google-Github-Facebok-MFA
```

---

### Paso 2: Configurar Variables de Entorno

#### 📁 Backend (`backend/.env`):
Crea o edita el archivo `backend/.env`:

```env
# Base de Datos MongoDB Atlas (Reemplazar con tus credenciales reales)
SPRING_DATA_MONGODB_URI=mongodb+srv://admin:admin@cambistaonilne.vgbiyuj.mongodb.net/sample_mflix?retryWrites=true&w=majority&appName=cambistaOnilne

# Base de Datos Relacional PostgreSQL (Neon / Local)
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-young-tooth-ac5b010f-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=tu_password_postgres

# Seguridad JWT
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION_MS=3600000

# Apache Kafka (apunta al contenedor kafka en red Docker)
KAFKA_BOOTSTRAP_SERVERS=kafka:29092

# RabbitMQ (apunta al contenedor rabbitmq en red Docker)
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=cambista
RABBITMQ_PASSWORD=cambista123

# Credenciales OAuth 2.0 (Google, GitHub, Facebook)
OAUTH_GOOGLE_CLIENT_ID=214338480134-98c0p8i00umpbtd8tvuivfqall99i0l1.apps.googleusercontent.com
OAUTH_GOOGLE_CLIENT_SECRET=tu_google_client_secret
OAUTH_GITHUB_CLIENT_ID=tu_github_client_id
OAUTH_GITHUB_CLIENT_SECRET=tu_github_client_secret
OAUTH_FACEBOOK_CLIENT_ID=tu_facebook_app_id
OAUTH_FACEBOOK_CLIENT_SECRET=tu_facebook_app_secret
```

> [!NOTE]
> Para pruebas locales inmediatas, si no configuras claves reales de GitHub o Facebook, los adaptadores cuentan con mecanismos de simulación controlada (mock fallback) para permitir probar todo el flujo sin bloquear el desarrollo.

#### 📁 Frontend (`frontend/.env`):
Crea o edita el archivo `frontend/.env`:

```env
VITE_API_URL=http://localhost:8080/api/v1
VITE_GOOGLE_CLIENT_ID=214338480134-98c0p8i00umpbtd8tvuivfqall99i0l1.apps.googleusercontent.com
VITE_GITHUB_CLIENT_ID=tu_github_client_id
VITE_FACEBOOK_APP_ID=tu_facebook_app_id
```

---

### Paso 3: Levantar con Docker Compose (Recomendado)

En la raíz del proyecto, ejecuta:

```bash
docker compose up --build -d
```

Este comando descargará, compilará y levantará los 5 servicios integrados:
1. **`cambista-zookeeper`**: Coordinador de Kafka (`puerto 2181`).
2. **`cambista-kafka`**: Broker de eventos de auditoría (`puerto 9092`).
3. **`cambista-rabbitmq`**: Message broker para notificaciones asíncronas (`puerto 5672` y panel en `15672`).
4. **`cambista-backend-app`**: API REST Spring Boot 3.3.1 (`puerto 8080`).
5. **`cambista-frontend-app`**: Aplicación Web React 18 + Vite (`puerto 3000`).

Verifica que todos los contenedores estén activos:
```bash
docker compose ps
```

---

### Paso 4: Alternativa sin Docker (Manual en Local)

Si prefieres ejecutar el código directamente en tu máquina:

1. **Asegúrate de tener Kafka y RabbitMQ corriendo** (puedes levantar solo los brokers con `docker compose up -d zookeeper kafka rabbitmq`).
2. **Levantar el Backend**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   *El backend quedará disponible en `http://localhost:8080`.*
3. **Levantar el Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   *El frontend quedará disponible en `http://localhost:3000` o `http://localhost:5173`.*

---

## 🧪 6. Cómo Probar las Nuevas Funcionalidades

### 🌐 URLs Principales:
- **Aplicación Web**: 👉 [http://localhost:3000](http://localhost:3000)
- **Documentación Swagger UI**: 👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Consola RabbitMQ**: 👉 [http://localhost:15672](http://localhost:15672) (Usuario: `cambista` / Password: `cambista123`)

---

### 📲 Prueba 1: Probar Autenticación Social (Google / GitHub / Facebook)
1. Ingresa a [http://localhost:3000](http://localhost:3000).
2. En la pantalla de login o registro, haz clic en el botón de **Google**, **GitHub** o **Facebook**.
3. Autoriza la aplicación en el proveedor social. Al retornar, serás autenticado y redirigido automáticamente al panel de usuario.
4. Revisa los logs del backend (`docker logs cambista-backend-app`): si el usuario era nuevo, verás los logs de bienvenida de **RabbitMQ** y la auditoría de **Kafka**.

---

### 🛡️ Prueba 2: Activar Doble Factor (MFA con Google Authenticator)
1. Inicia sesión en la plataforma y dirígete a la pestaña **"Mi Perfil"**.
2. En la sección **Autenticación en Dos Pasos (2FA)**, haz clic en **"📲 Configurar Google Authenticator"**.
3. Abre la aplicación **Google Authenticator** (o Authy) en tu teléfono móvil.
   > [!TIP]
   > Si tenías alguna entrada previa de *CambistaOnline* en tu teléfono de pruebas anteriores, elimínala primero para evitar confusiones.
4. Escanea el código QR que aparece en pantalla (o copia la clave manual si prefieres ingresarla a mano).
5. Observa el código de 6 dígitos que muestra tu teléfono celular y escríbelo en el campo de texto antes de que expire el círculo de 30 segundos.
6. Haz clic en **"Confirmar y Activar"**.
7. La pantalla se actualizará mostrando el estado en verde: **`ACTIVO`**.

---

### 🔐 Prueba 3: Iniciar Sesión con Desafío MFA
1. Cierra sesión en la plataforma.
2. Ingresa tus credenciales o haz clic en tu botón de red social con el que activaste 2FA.
3. Se abrirá automáticamente el modal de seguridad: **"Verificación de Seguridad (2FA)"**.
4. Ingresa el código de 6 dígitos actual de tu Google Authenticator.
5. El sistema validará el código en milisegundos y te otorgará acceso completo a la plataforma.

---

## 📡 7. Endpoints de la API

### Autenticación y Redes Sociales
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Público | Registro de usuario local (emite eventos a Kafka y RabbitMQ). |
| `POST` | `/api/v1/auth/login` | Público | Login local con email/password (devuelve JWT o `mfaRequired: true`). |
| `POST` | `/api/v1/auth/oauth/{provider}` | Público | Intercambio de código OAuth (`google`, `github`, `facebook`). |
| `GET` | `/api/v1/auth/me` | Autenticado | Obtiene perfil del usuario logueado. |

### Doble Factor de Autenticación (MFA)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/mfa/setup` | Autenticado | Genera o recupera el secreto Base32 y la URI para el código QR. |
| `POST` | `/api/v1/auth/mfa/enable` | Autenticado | Activa 2FA confirmando el primer código TOTP de 6 dígitos. |
| `POST` | `/api/v1/auth/mfa/verify` | Público | Desafío 2FA en login: valida `mfaSessionToken` + código y entrega el JWT. |
| `POST` | `/api/v1/auth/mfa/disable` | Autenticado | Desactiva 2FA ingresando el código TOTP actual. |

### Operaciones de Cambio (Core Engine & Orders)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/exchange/calculate` | Autenticado | Cotización con reglas SBS y beneficios. |
| `POST` | `/api/v1/orders` | Autenticado | Creación de orden (emite evento a Kafka y RabbitMQ). |
| `GET` | `/api/v1/orders` | Autenticado | Listado de órdenes del cliente. |
| `POST` | `/api/v1/orders/confirm` | Autenticado | Confirmación de pago y liquidación. |

---

## 🎯 8. Verificación de Calidad y Tests

El proyecto cuenta con una suite completa de pruebas unitarias y de arquitectura que garantizan la solidez del sistema:

### Ejecutar pruebas backend:
```bash
cd backend
mvn test
```
**Resultados:**
- **32 pruebas ejecutadas, 0 errores, 0 fallos**.
- **`HexagonalArchitectureTest` & `OnionArchitectureTest`**: Verificación estricta con **ArchUnit** de que las capas de Dominio y Aplicación no tengan dependencias prohibidas.
- **`TotpAdapterTest`**: Validación matemática de cumplimiento del estándar **RFC 6238** y codificación Base32 RFC 4648.
- **`AuthenticateOAuthUserServiceTest`**: Pruebas de registro automático y requerimiento de MFA.
- **`VerifyMfaServiceTest`**: Validación de sesiones temporales y códigos válidos/inválidos.

### Compilar y verificar frontend:
```bash
cd frontend
npm run build
```
**Resultados:**
- Compilación de producción con Vite completada con éxito en menos de 300 ms sin errores de sintaxis ni de dependencias.
