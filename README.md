# 💱 CambistaOnline - Plataforma Fintech de Cambio de Divisas
## 🚀 Versión Event-Driven: Arquitectura Hexagonal con Apache Kafka & RabbitMQ

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot 3.3.1](https://img.shields.io/badge/Spring%20Boot-3.3.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React 18](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![MongoDB Atlas](https://img.shields.io/badge/MongoDB-Atlas%20NoSQL-green.svg)](https://www.mongodb.com/cloud/atlas)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-7.5.0-black.svg?logo=apachekafka)](https://kafka.apache.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange.svg?logo=rabbitmq)](https://www.rabbitmq.com/)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)
[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%2F%20EDA-darkgreen.svg)]()

**CambistaOnline** es una plataforma web fintech de grado empresarial para cotización e intercambio de divisas en tiempo real (**USD**, **EUR**, **PEN**). Soporta operaciones para **Persona Natural** (DNI/CE) y **Persona Jurídica** (RUC, Razón Social y Representante Legal).

En esta versión (`feature/messaging`), el sistema evoluciona a una **Arquitectura Guiada por Eventos (EDA - Event-Driven Architecture)** integrando **Apache Kafka** y **RabbitMQ**, manteniendo estrictamente el aislamiento de la **Arquitectura Hexagonal (Ports & Adapters)**.

---

## 📑 Tabla de Contenidos
1. [Arquitectura del Sistema](#-1-arquitectura-del-sistema)
2. [Kafka vs. RabbitMQ: Roles en CambistaOnline](#-2-kafka-vs-rabbitmq-roles-en-cambistaonline)
3. [Detalle de Cambios Realizados](#-3-detalle-de-cambios-realizados)
4. [Beneficios de esta Nueva Versión](#-4-beneficios-de-esta-nueva-versión)
5. [Dificultades y Desafíos Técnicos](#-5-dificultades-y-desafíos-técnicos)
6. [Flujo de una Transacción con Kafka: Cambio de Soles / Dólares](#-6-flujo-de-una-transacción-con-kafka-cambio-de-soles--dólares)
7. [Guía para Levantar el Proyecto en Local](#-7-guía-para-levantar-el-proyecto-en-local)
8. [Demostración y Verificación del Flujo de Mensajería](#-8-demostración-y-verificación-del-flujo-de-mensajería)
9. [Endpoints de la API](#-9-endpoints-de-la-api)

---

## 🏛️ 1. Arquitectura del Sistema

El proyecto sigue una **Arquitectura Hexagonal Pura** combinada con **Event-Driven Architecture**. El núcleo del negocio y la lógica de aplicación no tienen ninguna dependencia directa con frameworks de mensajería (sin anotaciones de Kafka ni de RabbitMQ en el dominio).

```
                      ┌───────────────────────────────────────────────────────────┐
                      │                    DRIVING ADAPTERS                       │
                      │   REST Controllers (AuthController, OrderController)      │
                      └─────────────────────────────┬─────────────────────────────┘
                                                    │
                                                    ▼
                      ┌───────────────────────────────────────────────────────────┐
                      │                 APPLICATION & INBOUND PORTS               │
                      │   RegisterUserUseCase, CreateExchangeOrderUseCase, etc.   │
                      │                                                           │
                      │   [Dominio Puro] ──▶ Entities & Value Objects             │
                      │                                                           │
                      │   Puertos de Salida (Outbound Ports):                     │
                      │   - UserPersistencePort       - OrderPersistencePort      │
                      │   - UserEventPublisherPort    - OrderEventPublisherPort   │
                      │   - UserNotificationPort      - OrderNotificationPort     │
                      └──────────────┬────────────────────────────┬───────────────┘
                                     │                            │
                     ┌───────────────┴──────────────┐   ┌─────────┴─────────────────────┐
                     ▼                              ▼   ▼                               ▼
      ┌─────────────────────────────┐                 ┌────────────────────────────────┐
      │   MongoDB Atlas Adapters    │                 │   DRIVEN MESSAGING ADAPTERS    │
      │   (Persistencia NoSQL)      │                 │                                │
      │   - MongoUserAdapter        │                 │   Kafka Adapters (Audit Log)   │
      │   - MongoOrderAdapter       │                 │   RabbitMQ Adapters (Notif)    │
      └─────────────────────────────┘                 └────────────────────────────────┘
```

---

## 🔄 2. Kafka vs. RabbitMQ: Roles en CambistaOnline

En lugar de utilizar una sola herramienta para todo, se aplicó cada tecnología según su especialidad:

| Criterio | 🪵 Apache Kafka | 📬 RabbitMQ |
| :--- | :--- | :--- |
| **Rol en el sistema** | **Event Streaming & Auditoría Inmutable** | **Message Broker & Notificaciones Asíncronas** |
| **Modelo de datos** | Log distribuido persistente (Event Sourcing / Replay) | Colas de mensajes AMQP (consume y elimina) |
| **Propósito de negocio**| Trazabilidad financiera inmutable de cada transacción y cambio de estado | Enrutamiento de alertas y simulación de envío de correos electrónicos / SMS |
| **Tópicos / Colas** | `cambista.orders.created`<br>`cambista.orders.completed`<br>`cambista.users.registered` | **Exchange**: `cambista.notifications`<br>**Queues**: `order.created.queue`, `order.completed.queue`, `user.welcome.queue` |
| **Consumidores** | `KafkaOrderEventConsumer`<br>`KafkaUserEventConsumer` (Audit Trail) | `RabbitMQOrderNotificationConsumer`<br>`RabbitMQUserNotificationConsumer` (Email/SMS simulation) |

---

## 🛠️ 3. Detalle de Cambios Realizados

### 3.1 Nuevos Eventos de Dominio (POJOs Puros)
Ubicados en `domain/events/`, no contienen dependencias de librerías ni anotaciones externas:
- **`OrderCreatedEvent`**: Contiene número de orden, usuario, montos, monedas origen/destino, tasa y timestamp.
- **`OrderCompletedEvent`**: Emite la confirmación de la transferencia y cierre de la orden.
- **`UserRegisteredEvent`**: Emite los datos del usuario recién registrado.

### 3.2 Nuevos Puertos de Salida (Outbound Ports)
Ubicados en `application/ports/outbound/`:
- `OrderEventPublisherPort`: Define el contrato para emitir eventos de órdenes hacia Kafka.
- `OrderNotificationPort`: Define el contrato para enviar notificaciones de órdenes hacia RabbitMQ.
- `UserEventPublisherPort`: Contrato para emitir eventos de registro de usuarios a Kafka.
- `UserNotificationPort`: Contrato para emitir notificaciones de bienvenida a RabbitMQ.

### 3.3 Adaptadores Kafka (Outbound Publisher + Inbound Consumer)
- **`KafkaOrderEventPublisherAdapter`**: Implementa `OrderEventPublisherPort` utilizando `KafkaTemplate`.
- **`KafkaUserEventPublisherAdapter`**: Implementa `UserEventPublisherPort`.
- **`KafkaOrderEventConsumer`**: Escucha los tópicos `cambista.orders.*` con `@KafkaListener` y genera el log de auditoría.
- **`KafkaUserEventConsumer`**: Escucha `cambista.users.registered` para auditoría de altas de usuario.

### 3.4 Adaptadores RabbitMQ (Outbound Publisher + Inbound Consumer)
- **`RabbitMQOrderNotificationAdapter`**: Implementa `OrderNotificationPort` enviando mensajes con `RabbitTemplate` al topic exchange `cambista.notifications`.
- **`RabbitMQUserNotificationAdapter`**: Implementa `UserNotificationPort`.
- **`RabbitMQOrderNotificationConsumer`**: Escucha las colas `order.created.queue` y `order.completed.queue`, formateando e imprimiendo el correo electrónico simulado.
- **`RabbitMQUserNotificationConsumer`**: Escucha `user.welcome.queue` e imprime el email de bienvenida.

### 3.5 Infraestructura y Configuración
- **`KafkaConfig.java`**: Configura `ProducerFactory`, `KafkaTemplate`, serializadores JSON y `ConcurrentKafkaListenerContainerFactory`.
- **`RabbitMQConfig.java`**: Declara programáticamente el Exchange `cambista.notifications`, las colas durables, los enlaces (bindings) por routing key (`order.created`, `order.completed`, `user.welcome`) y el conversor `Jackson2JsonMessageConverter`.
- **`BeanConfig.java` & `OrderBeanConfig.java`**: Inyectan los nuevos puertos en los servicios de aplicación.
- **`docker-compose.yml`**: Integra los contenedores de `zookeeper`, `kafka` (Confluent 7.5.0) y `rabbitmq` (3.13-management).
- **`application.yml` & `.env`**: Variables de configuración para conexión a brokers locales y en contenedor.

### 3.6 Modificación de Casos de Uso
Los servicios `CreateExchangeOrderService`, `ConfirmTransferService` y `RegisterUserService` ahora orquestan la persistencia y la publicación de eventos a través de los puertos de salida, sin conocer si el destino final es Kafka, RabbitMQ o cualquier otro servicio.

### 3.7 Pruebas Unitarias y de Arquitectura
- **`HexagonalArchitectureTest`**: Actualizado con reglas ArchUnit que impiden que `org.apache.kafka`, `org.springframework.kafka`, `org.springframework.amqp` o `com.rabbitmq` sean importados dentro de `domain` o `application`.
- **`KafkaOrderEventPublisherAdapterTest`**: Pruebas con Mockito y `ArgumentCaptor` verificando tópicos y payloads.
- **`RabbitMQOrderNotificationAdapterTest`**: Pruebas verificando exchanges y routing keys.

---

## 🌟 4. Beneficios de esta Nueva Versión

1. **Desacoplamiento Extremo**: La creación de una orden ya no bloquea la ejecución esperando procesos secundarios como el envío de emails o el registro de auditoría.
2. **Auditoría Financiera Confiable (Kafka)**: Todos los eventos quedan almacenados en orden cronológico en el log de Kafka, permitiendo reconstruir el historial o conectarlo a data lakes / dashboards de monitoreo en el futuro.
3. **Escalabilidad de Notificaciones (RabbitMQ)**: El procesamiento de emails y alertas se maneja mediante colas asíncronas independientes; si el servicio de correos se satura, los mensajes esperan de forma segura en la cola.
4. **Mantenibilidad y Limpieza**: Se respetó al 100% el diseño Hexagonal; si mañana se sustituye RabbitMQ por AWS SQS o Kafka por Redpanda, solo se crea un nuevo adaptador sin modificar una sola línea de la lógica de negocio.
5. **Observabilidad en Tiempo Real**: Consola estructurada con trazabilidad visual de eventos recibidos por ambos brokers.

---

## ⚠️ 5. Dificultades y Desafíos Técnicos

Durante el desarrollo e integración de esta versión se resolvieron los siguientes retos técnicos:

1. **Aislamiento Estricto en Arquitectura Hexagonal**:
   - *Desafío*: Evitar la tentación de usar anotaciones de Spring (`@KafkaListener`, `@RabbitListener`, `@Transactional`) o clases de brokers dentro de las entidades y servicios del dominio.
   - *Solución*: Se crearon eventos de dominio 100% agnósticos y se delegó la anotación de listeners a clases dentro del paquete `adapters/inbound/`.
2. **Serialización y Trusted Packages en Kafka/RabbitMQ**:
   - *Desafío*: Deserializar objetos JSON a través de diferentes paquetes y contenedores puede fallar si los headers de tipo (`__TypeId__`) no coinciden o no están autorizados.
   - *Solución*: Se configuró `JsonDeserializer.TRUSTED_PACKAGES = "com.cambistaonline.*"` y se estandarizó la serialización en `KafkaConfig` y `RabbitMQConfig`.
3. **Orquestación de Múltiples Servicios en Docker**:
   - *Desafío*: Kafka requiere que Zookeeper esté inicializado antes de levantar el broker, y la aplicación Spring Boot requiere que Kafka y RabbitMQ estén disponibles para abrir las conexiones.
   - *Solución*: Se agregaron dependencias estructuradas (`depends_on`) y redes internas en `docker-compose.yml` junto con listeners mapeados para acceso interno (`kafka:29092`) y externo (`localhost:9092`).
4. **Testing sin Brokers Pesados en Memoria**:
   - *Desafío*: Ejecutar `mvn test` de forma rápida y reproducible en cualquier entorno sin obligar a tener Kafka/RabbitMQ encendidos durante la fase de compilación.
   - *Solución*: Uso de pruebas unitarias con mocks (`@Mock KafkaTemplate`, `@Mock RabbitTemplate`) y validación estática de dependencias mediante ArchUnit.

---

## 🪙 6. Flujo de una Transacción con Kafka: Cambio de Soles / Dólares

En el ciclo de vida de una operación de cambio de divisas en **CambistaOnline**, Apache Kafka interviene en **dos momentos clave** para garantizar trazabilidad y auditoría inmutable:

```
Usuario (Web / App)
    │
    │  1. Solicita cambiar: 100 USD ➔ 375.50 PEN
    ▼
[ExchangeOrderController] (HTTP POST /api/v1/orders)
    │
    ▼
[CreateExchangeOrderService] (Caso de Uso)
    ├── 1. Calcula tipo de cambio con reglas SBS y beneficios
    ├── 2. Guarda la orden en MongoDB Atlas (Estado: PENDING_PAYMENT)
    │
    └── 3. 🎯 MOMENTO 1 DE KAFKA: Publica "OrderCreatedEvent"
             │
             ▼
        [KafkaOrderEventPublisherAdapter]
             │ (send to broker)
             ▼
        🪵 Tópico Kafka: 'cambista.orders.created'
             │ (partición por orderNumber)
             ▼
        [KafkaOrderEventConsumer] ──▶ Registra Log de Auditoría Inmutable
```

Posteriormente, cuando el usuario realiza la transferencia bancaria y se valida la recepción de fondos:

```
Operador / Sistema
    │
    │  2. Confirma recepción de fondos (HTTP POST /api/v1/orders/confirm)
    ▼
[ConfirmTransferService] (Caso de Uso)
    ├── 1. Actualiza estado a COMPLETED en MongoDB Atlas
    │
    └── 2. 🎯 MOMENTO 2 DE KAFKA: Publica "OrderCompletedEvent"
             │
             ▼
        [KafkaOrderEventPublisherAdapter]
             │ (send to broker)
             ▼
        🪵 Tópico Kafka: 'cambista.orders.completed'
             │
             ▼
        [KafkaOrderEventConsumer] ──▶ Registra Cierre Financiero Inmutable
```

### 📌 Momento 1: Creación de la Orden (`cambista.orders.created`)
1. El usuario envía la solicitud de cambio de moneda.
2. `CreateExchangeOrderService` calcula la tasa, guarda la orden en MongoDB Atlas con estado `PENDING_PAYMENT` e instancia el evento de dominio `OrderCreatedEvent`.
3. `KafkaOrderEventPublisherAdapter` publica el evento en el tópico `cambista.orders.created`.
4. `KafkaOrderEventConsumer` procesa el evento y deja constancia inalterable de la orden en el log de auditoría.

### 📌 Momento 2: Confirmación y Liquidación (`cambista.orders.completed`)
1. El usuario transfiere los fondos desde su banco y se confirma la recepción.
2. `ConfirmTransferService` actualiza el estado a `COMPLETED` en MongoDB Atlas y emite `OrderCompletedEvent`.
3. `KafkaOrderEventPublisherAdapter` publica el evento en el tópico `cambista.orders.completed`.
4. `KafkaOrderEventConsumer` certifica el cierre y ejecución financiera de la transacción.

### 💡 ¿Por qué Kafka en este momento financiero?
- **Auditoría inmutable (Compliance SBS)**: MongoDB almacena el estado actual mutable; Kafka conserva la historia cronológica inalterable de todos los eventos.
- **Desacoplamiento asíncrono**: La API REST responde al cliente en milisegundos (`201 Created`), mientras los procesos analíticos y de auditoría se ejecutan en segundo plano.
- **Preparado para Microservicios**: Si a futuro se integran servicios de Prevención de Lavado de Dinero (PLD), Contabilidad o Dashboards en tiempo real, solo necesitarán suscribirse a estos tópicos sin sobrecargar la base de datos operativa.

---

## 💻 7. Guía para Levantar el Proyecto en Local

### 📋 Requisitos Previos:
- **[Docker Desktop](https://www.docker.com/products/docker-desktop/)** instalado y en ejecución.
- Cuenta o cluster en **MongoDB Atlas** (o MongoDB local).
- Git.

---

### Paso 1: Clonar y situarse en la rama de mensajería

```bash
git clone https://github.com/pingponista/cambista_online.git
cd cambista_online
git checkout feature/messaging
```

---

### Paso 2: Configurar Variables de Entorno (`backend/.env`)

Crea o edita el archivo **`backend/.env`** en base al siguiente ejemplo:

```env
# Conexión a MongoDB Atlas (Reemplazar con tus credenciales)
SPRING_DATA_MONGODB_URI=mongodb+srv://username:password@your-cluster.mongodb.net/sample_mflix?retryWrites=true&w=majority

# Seguridad JWT (Clave secreta segura de al menos 256 bits)
JWT_SECRET=tu_jwt_secret_seguro_de_al_menos_256_bits_aqui
JWT_EXPIRATION_MS=3600000

# Brokers de Mensajería (Nombres de servicio en red Docker)
KAFKA_BOOTSTRAP_SERVERS=kafka:29092
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=cambista
RABBITMQ_PASSWORD=tu_password_rabbitmq
```

---

### Paso 3: Levantar todo el stack con Docker Compose

Ejecuta en la raíz del repositorio:

```bash
docker compose up --build -d
```

Este comando descargará e iniciará 5 contenedores coordinados:
1. **`cambista-zookeeper`**: Coordinador de Kafka (`puerto 2181`).
2. **`cambista-kafka`**: Broker de eventos Apache Kafka (`puerto 9092`).
3. **`cambista-rabbitmq`**: Message broker RabbitMQ con panel de administración (`puertos 5672 y 15672`).
4. **`cambista-backend-app`**: API REST Spring Boot 3.3.1 (`puerto 8080`).
5. **`cambista-frontend-app`**: Aplicación Web React 18 + Vite (`puerto 3000`).

---

### Paso 4: Verificar el estado de los contenedores

```bash
docker compose ps
```

Todos los contenedores deben figurar en estado `running` (Up).

---

## 🖥️ 8. Demostración y Verificación del Flujo de Mensajería

### 🌐 URLs de Acceso:
- **Frontend Web App**: 👉 [http://localhost:3000](http://localhost:3000)
- **Backend API & Swagger UI**: 👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **RabbitMQ Management Dashboard**: 👉 [http://localhost:15672](http://localhost:15672)
  - **Usuario**: `cambista`
  - **Contraseña**: `tu_password_rabbitmq`

---

### 🔍 Cómo ver los eventos en tiempo real:

Abre una terminal y sigue los logs del backend:

```bash
docker logs cambista-backend-app -f
```

Al registrar un usuario o realizar una operación de cambio de divisa, verás los logs emitidos por los consumidores:

#### 1. Log de Auditoría Kafka:
```text
════════════════════════════════════════════════════
[KAFKA AUDIT] ✅ ORDEN CREADA
[KAFKA AUDIT]    Número    : TRX-491823901
[KAFKA AUDIT]    Usuario   : alex@gmail.com
[KAFKA AUDIT]    Operación : COMPRA
[KAFKA AUDIT]    USD → PEN
[KAFKA AUDIT]    Monto env : 100.00 | Monto rec: 375.50
[KAFKA AUDIT]    Tasa      : 3.7550
[KAFKA AUDIT]    Timestamp : 2026-08-20T00:15:30
════════════════════════════════════════════════════
```

#### 2. Log de Notificación RabbitMQ:
```text
╔══════════════════════════════════════════════════╗
║  [RABBITMQ NOTIF] 📧 SIMULACIÓN DE EMAIL         ║
╠══════════════════════════════════════════════════╣
║  Para   : alex@gmail.com                         ║
║  Asunto : Su orden de cambio fue registrada      ║
║  ------------------------------------------------║
║  Estimado cliente,                               ║
║  Su orden TRX-491823901 ha sido creada.          ║
║  Monto enviado   : 100.00 USD                    ║
║  Monto a recibir : 375.50 PEN                    ║
║  Por favor realice su transferencia bancaria.    ║
╚══════════════════════════════════════════════════╝
```

---

### 🛑 Detener los servicios:

```bash
docker compose down
```

---

## 📡 9. Endpoints de la API

| Módulo | Método | Endpoint | Descripción |
| :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/v1/auth/register` | Registro de usuario (emite evento a Kafka y RabbitMQ). |
| **Auth** | `POST` | `/api/v1/auth/login` | Login y emisión de token JWT. |
| **Auth** | `GET` | `/api/v1/auth/me` | Obtiene el perfil del usuario autenticado. |
| **Engine** | `POST` | `/api/v1/exchange/calculate` | Cálculo de tipo de cambio con reglas de negocio. |
| **Orders** | `POST` | `/api/v1/orders` | Crea orden de cambio (emite evento a Kafka y RabbitMQ). |
| **Orders** | `GET` | `/api/v1/orders` | Historial de órdenes de cambio del usuario. |
| **Orders** | `POST` | `/api/v1/orders/confirm` | Confirma pago (emite evento de orden completada). |

