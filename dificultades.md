# 🚨 Registro de Dificultades y Retos Técnicos: Migración de CambistaOnline

Este documento describe en detalle las dificultades, retos de diseño y trampas de implementación a las que se enfrenta un desarrollador al transformar la aplicación **CambistaOnline**:
1. De **Arquitectura ONION** a **Arquitectura HEXAGONAL (Ports & Adapters)**.
2. De la infraestructura relacional **Neon PostgreSQL (JPA)** a la base de datos NoSQL **MongoDB Atlas**.
3. Los desafíos adicionales que surgirían si la aplicación escalara hacia un entorno bancario/fintech completo.

---

## 🏛️ PARTE 1: Dificultades en la Transición de Arquitectura ONION a HEXAGONAL

Aunque la Arquitectura ONION y la Arquitectura HEXAGONAL comparten la premisa de mantener el negocio en el centro, la transición práctica en un proyecto real con **Spring Boot** presenta fricciones estructurales importantes:

### 1.1. Purificación del Dominio (Eliminar Anotaciones y Frameworks)
- **El Problema**: En la arquitectura ONION original, era común ver anotaciones de Spring (`@Component`, `@Autowired`), anotaciones de JPA (`@Entity`, `@Table`, `@Column`, `@Id`) o de Jackson (`@JsonProperty`) directamente en las clases de Dominio.
- **La Dificultad**: Convertir el dominio a **POJOs y Objetos de Valor puros** exige eliminar completamente cualquier importación de Spring, Hibernate, JPA o Jackson.
- **El Impacto**: Al quitar JPA del dominio, perdemos utilidades automáticas como la generación automática de IDs, el manejo automático de fechas (`@CreationTimestamp`, `@PrePersist`) y las relaciones Lazy/Eager. Todo esto tuvo que trasladarse explícitamente a los **Adaptadores de Salida**.

### 1.2. El Doble Contrato de Puertos (Inbound vs Outbound)
- **El Problema**: En ONION se suele utilizar una interfaz de Servicio (`UserService`) que actúa a la vez como contrato para los controladores y como punto de inyección de infraestructura.
- **La Dificultad en Hexagonal**: Obliga a separar estrictamente:
  - **Inbound Ports (Puertos de Entrada / Use Cases)**: Interfaces orientadas al usuario o cliente REST (ej. `RegisterUserUseCase`, `CalculateExchangeRateUseCase`).
  - **Outbound Ports (Puertos de Salida / SPIs)**: Interfaces orientadas a la infraestructura (ej. `UserPersistencePort`, `ExchangeOrderPersistencePort`, `PasswordEncoderPort`).
- **Complicación**: Un desarrollador puede confundirse fácilmente sobre dónde colocar cada interfaz y cómo inyectar las implementaciones sin romper la regla de dependencia hacia adentro.

### 1.3. Cumplimiento de las Pruebas de Gobierno con ArchUnit
- **La Dificultad**: La suite de pruebas de arquitectura `HexagonalArchitectureTest.java` falla si cualquier clase del paquete `..domain..` o `..application..` importa clases de `..adapters..`, `..infrastructure..` o `org.springframework..`.
- **El Reto**: Mantener los Casos de Uso (`application.service`) 100% limpios de anotaciones de persistencia o transaccionales externas de Spring (`@Transactional` de Spring en el dominio).

---

## 🍃 PARTE 2: Dificultades en la Migración de Persistencia (Neon PostgreSQL a MongoDB Atlas)

Esta fue la fase con mayor cantidad de anomalías reales descubiertas durante la ejecución:

### 2.1. Incompatibilidad de Nombres de Campos (Documentos Migrados vs Nativos)
- **El Problema Real**: La base de datos de MongoDB Atlas ya contenía documentos creados previamente durante la fase de PostgreSQL que fueron importados con nombres de columnas SQL en español:
  - Campos migrados: `"correo_user"`, `"nro_orden"`, `"tipo_operacion"`, `"monto_origen"`, `"monto_destino"`, `"tasa_final"`, `"estado"`.
  - Campos nativos nuevos: `"user_email"`, `"order_number"`, `"operation_type"`, `"amount_sent"`, `"amount_received"`, `"exchange_rate"`, `"status"`.
- **El Síntoma**: Al ejecutar `findByUserEmail("alex.meza@smartbricks.cl")`, la consulta de Spring Data MongoDB buscaba por el campo `user_email`. Las operaciones migradas tenían el campo `correo_user`, por lo que el historial retornaba **0 registros** (`"data": []`) a pesar de que los datos existían en la colección `operacion`.
- **La Solución Implementada**:
  1. Se creó una consulta con operador `$or` en `SpringDataMongoOrderRepository`:
     ```json
     { "$or": [ { "user_email": ?0 }, { "correo_user": ?0 } ] }
     ```
  2. Se diseñó un **Mapeo Dual Resiliente** en `OrderDocument.java` con getters que priorizan el campo estándar y hacen fallback transparente al campo migrado en español.

### 2.2. Error de Deserialización de Fechas (Cadenas con Espacios vs ISO-8601)
- **El Problema Real**: En MongoDB Atlas, la marca de tiempo `created_at` de los usuarios migrados estaba almacenada como una cadena con formato SQL con espacios: `"2026-08-01 03:12:56.465671+00"`.
- **El Síntoma**: Al intentar deserializar el documento a `LocalDateTime`, el convertidor nativo de Spring Data MongoDB fallaba lanzando una excepción `DateTimeParseException: Text '2026-08-01 03:12:56.465671+00' could not be parsed at index 10`, causando un error HTTP 500 al iniciar sesión.
- **La Solución Implementada**: Se cambió la propiedad a `Object createdAt` en el documento NoSQL y se implementó un método privado `parseDateTime` en el adaptador que convierte espacios `' '` por `'T'`, maneja zonas horarias `OffsetDateTime` y realiza fallbacks seguros.

### 2.3. Formato de URI de Conexión (JDBC vs Drivers Nativos de MongoDB)
- **El Problema**: Las herramientas de inteligencia de negocios o SQL Connector de Mongo proveen URIs con prefijo `jdbc:mongodb://...`.
- **La Dificultad**: Spring Data MongoDB no utiliza JDBC, sino el Driver síncrono nativo de Mongo que exige el formato `mongodb+srv://<usuario>:<password>@cluster.mongodb.net/database`. Usar la URL incorrecta provocaba fallos de resolución DNS `NameNotFoundException` al iniciar la aplicación.

### 2.4. Marcas de Tiempo e Inmutabilidad Non-Null
- **El Problema**: En Neon DB (PostgreSQL), la base de datos aplicaba restricciones `NOT NULL` en `created_at` y `updated_at`.
- **La Dificultad**: En MongoDB (NoSQL), no existen esquemas rígidos por defecto. Si el adaptador de persistencia no inicializa explícitamente `createdAt` y `updatedAt` con `LocalDateTime.now()`, se generan documentos inconsistentes que rompen la lógica de ordenamiento por fecha en el frontend.

---

## 🔮 PARTE 3: Complicaciones Adicionales si la Aplicación Escalara a Nivel Bancario Real

Actualmente, **CambistaOnline** aborda las funcionalidades principales de cambio de divisas y canje de puntos. Sin embargo, si la aplicación se llevara a un entorno bancario de producción masiva, surgirían complicaciones de mayor complejidad:

### 3.1. Transacciones Distribuidas y Consistencia Eventual (ACID vs NoSQL)
- **En PostgreSQL**: Se ejecuta un bloque `@Transactional` que actualiza la tabla de operaciones, resta los puntos de la tabla `tb_usuario_puntos` y registra la auditoría en un único commit atómico.
- **El Desafío en MongoDB**: Al ser una base de datos distribuida, si el saldo de puntos y la orden están en diferentes colecciones o fragmentos (Shards), se requieren **Transacciones Multi-Documento de MongoDB** o la implementación del patrón de microservicios **Saga (Orquestación / Coreografía)** para manejar compensaciones si falla la transferencia bancaria.

### 3.2. Condiciones de Carrera en el Canje de CambiPuntos (Race Conditions)
- **La Complicación**: Si un usuario realiza 2 operaciones casi simultáneas intentando canjear sus mismos 340 CambiPuntos, dos peticiones concurrentes podrían leer el mismo saldo antes de actualizarlo.
- **Solución Necesaria**: En NoSQL se debe implementar **Bloqueo Optimista** con control de versiones (`@Version`) o bien operaciones atómicas de incremento/decremento (`$inc` / `$dec`) en MongoDB a nivel de documento.

### 3.3. Garantía de Inmutabilidad de Tasas y Bloqueo de Cotización
- **La Complicación**: En una plataforma de cambio en vivo, una cotización se le garantiza al cliente durante un tiempo límite (ej. 15 minutos).
- **El Reto**: Se necesita una caché distribuida en memoria (ej. **Redis**) con expiración TTL (Time-To-Live) para congelar la tasa calculada por el motor de divisas, evitando que la variación del mercado durante la transferencia afecte la orden del cliente.

### 3.4. Trazabilidad e Inmutabilidad de Auditoría Financiera
- **La Complicación**: Regulaciones de la SBS y UIF (Unidad de Inteligencia Financiera) exigen auditorías estrictas e inalterables de cada cambio de estado de la operación (`PENDIENTE` -> `EN_VERIFICACION` -> `COMPLETADA` / `RECHAZADA`).
- **El Reto**: Implementar un patrón de **Event Sourcing** donde la colección de operaciones sea una lista de eventos inmutables de solo lectura (Append-Only Log).

---

## 📝 Conclusión

El cambio a la **Arquitectura Hexagonal** y a **MongoDB Atlas** le otorga a **CambistaOnline** un nivel de desacoplamiento excepcional: la base de datos se convirtió en un detalle técnico intercambiable mediante un Adaptador de Salida. Sin embargo, requiere una disciplina rigurosa por parte del equipo de desarrollo para manejar la heterogeneidad de datos migrados y mantener la pureza del Dominio.
