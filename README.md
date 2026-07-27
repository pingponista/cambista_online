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

## 🏛️ ¿Por qué Arquitectura ONION (Arquitectura Cebolla)?

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

### Principios y Beneficios Clave de la Arquitectura:

1. **Dominio Inmutable y Puro (`Domain Layer`)**:
   El núcleo de la aplicación (`com.cambistaonline.engine.domain`, `com.cambistaonline.order.domain`, `com.cambistaonline.auth.domain`) contiene las reglas de negocio puras (POJOs en Java 17), entidades, objetos de valor y las interfaces (**Puertos**). **No importa ni conoce Spring Boot, Hibernate o JPA**.
2. **Inversión de Dependencias (DIP)**:
   Las dependencias apuntan siempre hacia el centro (Dominio). La capa de Infraestructura implementa los **Puertos** definidos por el dominio mediante **Adaptadores** (`ExchangeEnginePersistenceAdapter`, `ExchangeOrderController`).
3. **Mantenibilidad y Prueba Independiente**:
   Es posible probar el 100% de los casos de uso y la lógica de negocio utilizando mocks en milisegundos mediante JUnit 5 y Mockito, sin necesidad de levantar contenedores de bases de datos o el contexto completo de Spring.
4. **Validación Mecánica con ArchUnit**:
   Se incluyen pruebas arquitectónicas automatizadas (`OnionArchitectureTest`, `EngineOnionArchitectureTest`, `OrderOnionArchitectureTest`) que verifican mecánicamente en cada `mvn test` que ninguna clase de Dominio o Aplicación importe paquetes de Infraestructura.

---

## 🛠️ Tecnologías Aplicadas

### Backend Enterprise (Java 17 + Spring Boot 3)
- **Java 17 LTS**: Uso extensivo de *Records*, *Sealed Classes*, *Switch Expressions* y programación orientada a objetos inmutable.
- **Spring Boot 3.3.1**: Framework empresarial para la creación de microservicios y APIs RESTful.
- **Spring Security 6 + JWT**: Autenticación sin estado (*Stateless*) con tokens firmados encriptados con algoritmo HMAC SHA-256 y contraseñas cifradas mediante BCrypt.
- **Spring Data JPA & Hibernate**: Persistencia relacional orientada a objetos.
- **Flyway Database Migrations**: Control de versiones de esquema de base de datos desde `V1` hasta `V7` (`tb_operacion`, `tb_usuario_puntos`, `tb_tasa_base`, `tb_spread_nivel`, `tb_horario`, `tb_estacionalidad`, `tb_puntos_config`, `users`).
- **ArchUnit 1.3**: Pruebas automatizadas de arquitectura y gobierno de código.
- **JaCoCo**: Cobertura de código y métricas de calidad de pruebas unitarias.

### Frontend Moderno (React 18 + Vite + Vanilla CSS)
- **React 18**: Biblioteca de interfaz de usuario basada en componentes reutilizables.
- **Vite 5**: Compilador y empaquetador ultrarrápido para producción.
- **Zustand**: Gestión de estado global ligero y persistente (`useAuthStore`, `useFxStore`, `useOrderStore`).
- **Axios**: Cliente HTTP estandarizado con interceptores automáticos de cabecera JWT Bearer y manejo de errores 401 Unauthorized.
- **CSS Modules & Variables**: Estilizado moderno sin dependencias pesadas de terceros, con soporte para modo oscuro/claro, sombras de vidrio (*glassmorphism*) y diseño responsivo.
- **Lucide React**: Biblioteca de iconografía moderna.

### Base de Datos Cloud & Infraestructura
- **PostgreSQL Serverless en Neon Cloud**: Base de datos PostgreSQL alojada en la nube con soporte SSL activado.
- **Docker & Docker Compose**: Construcción multi-etapa (*Multi-stage build*) con imágenes livianas (`eclipse-temurin:17-jre-alpine` para backend y `node:20-slim` -> `nginx:alpine` para el frontend).

---

## 🌟 Funcionalidades Completadas del Sistema

1. **Motor Dinámico de Tipo de Cambio**:
   - Pipeline de reglas: Tasa base SBS, Spread por rol/nivel preferente, Ajuste horario, Ajuste estacional por demanda y Bonificación por puntos.
2. **Persistencia Real de Órdenes (`TRX-XXXXXX`)**:
   - Creación y actualización de transacciones en la tabla `tb_operacion` de Neon PostgreSQL.
3. **Ciclo de Vida de CambiPuntos ⭐**:
   - Descuento automático de puntos canjeados durante la transacción y acreditación instantánea de **+10 puntos** de recompensa por cada operación realizada.
4. **Persistencia de Sesión de Usuario**:
   - Restauración de datos de perfil (`Alex Meza`) y mantenimiento de la sesión activa entre navegación y recargas.
5. **Navegación Inteligente y Vista "Nosotros"**:
   - Ocultamiento de la pestaña "Empresas" para Persona Natural logueada.
   - Nueva pantalla institucional `/nosotros` con acreditación SBS, propuestas de valor y central de soporte y contacto.
6. **Diseño Responsivo Horizontal**:
   - Reorganización de la calculadora a un diseño fluido en 2 columnas paralelas para computadoras y tablets (`≥ 768px`) y formato vertical en dispositivos móviles (`< 768px`).

---

## 🚀 Cómo Levantar el Proyecto en Desarrollo

### Requisitos Previos:
- **Docker Desktop** (Recomendado) o **Java 17 JDK** + **Maven 3.9+** + **Node.js 20+**.

---

### Opción A: Despliegue con Docker Compose (Recomendado)

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/TU_USUARIO/cambista_online.git
   cd cambista_online
   ```

2. **Verificar variables de entorno (`backend/.env`)**:
   Asegúrate de contar con el archivo `backend/.env` configurado con tus credenciales de Neon PostgreSQL:
   ```env
   SPRING_DATASOURCE_URL=jdbc:postgresql://ep-young-tooth-ac5b010f-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require
   SPRING_DATASOURCE_USERNAME=neondb_owner
   SPRING_DATASOURCE_PASSWORD=tu_password_de_neon
   JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
   JWT_EXPIRATION_MS=3600000
   ```

3. **Compilar y levantar contenedores**:
   ```bash
   docker compose up --build -d
   ```

4. **Acceder a la aplicación**:
   - 🌐 **Frontend (Web App)**: [http://localhost:3000](http://localhost:3000)
   - 📑 **Documentación REST (Swagger UI)**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

### Opción B: Ejecución Local en Desarrollo (Sin Docker)

#### 1. Levantar el Backend (Java 17 / Spring Boot)
```bash
cd backend
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
mvn spring-boot:run
```

#### 2. Levantar el Frontend (React / Vite)
```bash
cd frontend
npm install
npm run dev
```
Accede al entorno de desarrollo en [http://localhost:5173](http://localhost:5173).

---

## 🧪 Pruebas Automatizadas

### Ejecutar Pruebas Backend (JUnit 5 + Mockito + ArchUnit)
```bash
cd backend
mvn test
```

### Validar Compilación Frontend
```bash
cd frontend
npm run build
```

---

## 🔑 Credenciales de Prueba

- **Usuario**: `alex.meza@smartbricks.cl`
- **Contraseña**: `Pingp0nist@`
- **Usuario Demo**: `demo@cambistaonline.pe` / `demo1234`
