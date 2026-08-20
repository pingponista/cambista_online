# 🏛️ Plan de Transición: Arquitectura Hexagonal → Clean Architecture

## Proyecto: CambistaOnline

> **Propósito del documento**: Este documento es una **referencia técnica comparativa** que describe los cambios que experimentaría el proyecto CambistaOnline al pasar de una **Arquitectura Hexagonal (Ports & Adapters)** a **Clean Architecture**, propuesta por Robert C. Martin ("Uncle Bob") en su libro _Clean Architecture: A Craftsman's Guide to Software Structure and Design_ (2017). Su objetivo es entender qué elementos, conceptos y archivos cambian al adoptar este nuevo estilo arquitectónico, **sin implicar que uno sea mejor o peor que el otro**.

---

## 📚 1. ¿Qué es Clean Architecture?

### 1.1 Fundamento Conceptual

**Clean Architecture** organiza el código en **capas concéntricas** donde la regla fundamental es que **las dependencias solo pueden apuntar hacia adentro**, es decir, hacia las capas más abstractas y estables. Comparte la misma premisa filosófica que la Arquitectura Hexagonal: aislar el núcleo de negocio de los detalles técnicos.

```
           ┌──────────────────────────────────────┐
           │         Frameworks & Drivers          │  ← Capa más externa (Web, DB, UI)
           │   ┌──────────────────────────────┐   │
           │   │     Interface Adapters        │   │  ← Controllers, Gateways, Presenters
           │   │  ┌────────────────────────┐  │   │
           │   │  │      Use Cases         │  │   │  ← Interactors + Boundaries
           │   │  │   (Interactors)        │  │   │
           │   │  │  ┌──────────────────┐  │  │   │
           │   │  │  │    Entities      │  │  │   │  ← Entidades de Dominio (Core)
           │   │  │  │  (Domain Rules)  │  │  │   │
           │   │  │  └──────────────────┘  │  │   │
           │   │  └────────────────────────┘  │   │
           │   └──────────────────────────────┘   │
           └──────────────────────────────────────┘

   Dirección de dependencia: siempre hacia el centro → → →
```

Las **4 capas** de Clean Architecture son:

| Capa         | Nombre                      | Responsabilidad                                                                                |
| ------------ | --------------------------- | ---------------------------------------------------------------------------------------------- |
| 1 (Centro)   | **Entities**                | Reglas de negocio empresariales, Value Objects, entidades de dominio puras                     |
| 2            | **Use Cases** (Interactors) | Reglas de negocio de la aplicación, orquestación de casos de uso                               |
| 3            | **Interface Adapters**      | Conversión de datos entre casos de uso y el mundo exterior (Controllers, Presenters, Gateways) |
| 4 (Exterior) | **Frameworks & Drivers**    | Spring Boot, MongoDB, JWT, Swagger (detalles tecnológicos)                                     |

### 1.2 ¿En qué se diferencia de Hexagonal?

Ambas arquitecturas comparten el mismo objetivo: proteger el núcleo de negocio de los detalles de infraestructura. La diferencia está en la **terminología, la granularidad de los conceptos y algunos patrones adicionales** que Clean Architecture introduce de forma explícita:

| Aspecto                                | Arquitectura Hexagonal                     | Clean Architecture                                               |
| -------------------------------------- | ------------------------------------------ | ---------------------------------------------------------------- |
| **Núcleo de negocio**                  | `domain/model`                             | **Entities**                                                     |
| **Orquestador del caso de uso**        | `Service` (ej: `RegisterUserService`)      | **Interactor** (ej: `RegisterUserInteractor`)                    |
| **Interfaz de entrada al caso de uso** | Puerto Inbound (`UseCase` interface)       | **Input Boundary**                                               |
| **Interfaz de salida del caso de uso** | Puerto Outbound (`Port`)                   | **Output Boundary** + **Gateway**                                |
| **Entrega del resultado al cliente**   | El servicio retorna el objeto directamente | **Presenter** (recibe el resultado del Interactor y lo formatea) |
| **Contratos con infraestructura**      | `Port` / `RepositoryPort`                  | **Gateway**                                                      |
| **Adaptadores de persistencia**        | `PersistenceAdapter`                       | **Gateway Adapter**                                              |
| **Datos de entrada al caso de uso**    | `Command` / `Dto`                          | **Request Model**                                                |
| **Datos de salida del caso de uso**    | `Dto` / `ResponseDto`                      | **Response Model** → **ViewModel**                               |
| **Capa de framework**                  | `infrastructure`                           | **Frameworks & Drivers**                                         |
| **Tests de gobierno**                  | ArchUnit sobre capas hexagonales           | ArchUnit sobre 4 capas concéntricas                              |

### 1.3 El concepto exclusivo de Clean Architecture: el Presenter

La diferencia más notable que introduce Clean Architecture respecto a Hexagonal es el patrón **Presenter** y el **Output Boundary**. En Hexagonal, el servicio retorna el objeto de dominio y el controlador lo formatea. En Clean Architecture:

1. El **Interactor** (caso de uso) nunca retorna datos directamente.
2. Llama al **Output Boundary** (interfaz) con un **Response Model**.
3. El **Presenter** implementa ese Output Boundary y prepara el **ViewModel** listo para el Controller.
4. El **Controller** simplemente recoge el ViewModel preparado por el Presenter.

Esto garantiza que el caso de uso no sabe nada del formato HTTP, JSON ni del framework web.

---

## 🗺️ 2. Comparativa de Estructura de Paquetes

### 2.1 Estructura Actual (Arquitectura Hexagonal)

```
backend/src/main/java/com/cambistaonline/
├── CambistaApplication.java
│
├── auth/
│   ├── adapters/
│   │   └── outbound/
│   │       └── mongodb/
│   │           ├── MongoUserPersistenceAdapter.java
│   │           ├── SpringDataMongoUserRepository.java
│   │           └── UserDocument.java
│   ├── application/
│   │   ├── dto/
│   │   │   ├── RegisterRequestDto.java
│   │   │   ├── LoginRequestDto.java
│   │   │   └── UserResponseDto.java
│   │   ├── mappers/
│   │   │   └── UserMapper.java
│   │   ├── ports/
│   │   │   ├── inbound/
│   │   │   │   ├── AuthenticateUserUseCase.java
│   │   │   │   ├── GetCurrentUserUseCase.java
│   │   │   │   └── RegisterUserUseCase.java
│   │   │   └── outbound/
│   │   │       ├── JwtTokenPort.java
│   │   │       ├── PasswordEncoderPort.java
│   │   │       └── UserPersistencePort.java
│   │   ├── service/
│   │   │   ├── AuthenticateUserService.java
│   │   │   ├── GetCurrentUserService.java
│   │   │   └── RegisterUserService.java
│   │   └── usecases/
│   │       ├── AuthenticateUserUseCase.java
│   │       ├── GetCurrentUserUseCase.java
│   │       └── RegisterUserUseCase.java
│   ├── domain/
│   │   ├── events/
│   │   ├── exceptions/
│   │   │   └── UserAlreadyExistsException.java
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   ├── UserRole.java
│   │   │   └── UserStatus.java
│   │   ├── ports/
│   │   │   ├── JwtTokenPort.java
│   │   │   ├── PasswordEncoderPort.java
│   │   │   └── UserRepositoryPort.java
│   │   └── valueobjects/
│   │       ├── Dni.java
│   │       ├── Email.java
│   │       ├── Password.java
│   │       └── Ruc.java
│   ├── infrastructure/
│   │   ├── config/
│   │   │   ├── BeanConfig.java
│   │   │   └── OpenApiConfig.java
│   │   ├── persistence/
│   │   ├── rest/
│   │   │   ├── AuthController.java
│   │   │   ├── request/
│   │   │   └── response/
│   │   └── security/
│   │       ├── BcryptPasswordEncoderAdapter.java
│   │       ├── CustomUserDetailsService.java
│   │       ├── JwtAuthenticationFilter.java
│   │       ├── JwtProviderAdapter.java
│   │       └── SecurityConfig.java
│   └── shared/
│       ├── ApiResponse.java
│       ├── ErrorResponse.java
│       ├── GlobalExceptionHandler.java
│       └── SecurityConstants.java
│
├── order/
│   ├── adapters/outbound/mongodb/
│   │   ├── MongoOrderPersistenceAdapter.java
│   │   ├── OrderDocument.java
│   │   └── SpringDataMongoOrderRepository.java
│   ├── application/
│   │   ├── dto/
│   │   ├── ports/
│   │   │   ├── inbound/
│   │   │   └── outbound/
│   │   ├── service/
│   │   │   ├── ConfirmTransferService.java
│   │   │   ├── CreateExchangeOrderService.java
│   │   │   └── GetMyOrdersService.java
│   │   └── usecases/
│   │       ├── ConfirmTransferUseCase.java
│   │       ├── CreateExchangeOrderUseCase.java
│   │       └── GetMyOrdersUseCase.java
│   ├── domain/
│   │   ├── model/
│   │   │   ├── ExchangeOrder.java
│   │   │   └── OrderStatus.java
│   │   └── ports/
│   │       └── ExchangeOrderRepositoryPort.java
│   └── infrastructure/
│       ├── config/
│       ├── persistence/
│       └── rest/
│
└── engine/
    ├── adapters/outbound/mongodb/
    │   ├── MongoEnginePersistenceAdapter.java
    │   ├── SpringDataMongoUserPointsRepository.java
    │   └── UserPointsDocument.java
    ├── application/
    │   ├── dto/
    │   ├── ports/
    │   ├── service/
    │   └── usecases/
    │       └── CalculateExchangeRateUseCase.java
    ├── domain/
    │   ├── model/
    │   │   ├── CalculationContext.java
    │   │   ├── CalculationDetailItem.java
    │   │   ├── CurrencyType.java
    │   │   ├── CustomerLevel.java
    │   │   ├── Money.java
    │   │   └── OperationType.java
    │   ├── ports/
    │   │   ├── ExchangeRateRepositoryPort.java
    │   │   ├── ExchangeRuleRepositoryPort.java
    │   │   └── UserPointsRepositoryPort.java
    │   └── strategy/
    └── infrastructure/
```

### 2.2 Estructura en Clean Architecture

```
backend/src/main/java/com/cambistaonline/
├── CambistaApplication.java
│
├── shared/                                          ← [NUEVO] Módulo global compartido
│   ├── entities/
│   │   └── valueobjects/
│   │       ├── Email.java                           ← [MOVIDO desde auth/domain/valueobjects]
│   │       ├── Password.java                        ← [MOVIDO desde auth/domain/valueobjects]
│   │       ├── Dni.java                             ← [MOVIDO desde auth/domain/valueobjects]
│   │       └── Ruc.java                             ← [MOVIDO desde auth/domain/valueobjects]
│   ├── usecases/
│   │   └── boundary/
│   │       ├── RequestModel.java                    ← [NUEVO] Modelo base de entrada
│   │       └── ResponseModel.java                   ← [NUEVO] Modelo base de salida
│   └── adapters/
│       ├── presenter/
│       │   ├── ApiResponse.java                     ← [MOVIDO desde auth/shared]
│       │   ├── ErrorResponse.java                   ← [MOVIDO desde auth/shared]
│       │   └── GlobalExceptionHandler.java          ← [MOVIDO desde auth/shared]
│       └── security/
│           └── SecurityConstants.java               ← [MOVIDO desde auth/shared]
│
├── auth/
│   ├── entities/                                    ← [RENOMBRADO desde auth/domain]
│   │   ├── User.java                                ← [SIN CAMBIOS DE CÓDIGO]
│   │   ├── UserRole.java                            ← [SIN CAMBIOS DE CÓDIGO]
│   │   ├── UserStatus.java                          ← [SIN CAMBIOS DE CÓDIGO]
│   │   └── exceptions/
│   │       └── UserAlreadyExistsException.java      ← [SIN CAMBIOS DE CÓDIGO]
│   │
│   ├── usecases/                                    ← [RENOMBRADO desde auth/application]
│   │   ├── boundaries/
│   │   │   ├── input/
│   │   │   │   ├── RegisterUserInputBoundary.java   ← [RENOMBRADO de RegisterUserUseCase]
│   │   │   │   ├── AuthenticateUserInputBoundary.java ← [RENOMBRADO de AuthenticateUserUseCase]
│   │   │   │   └── GetCurrentUserInputBoundary.java ← [RENOMBRADO de GetCurrentUserUseCase]
│   │   │   └── output/
│   │   │       ├── RegisterUserOutputBoundary.java  ← [NUEVO] Interfaz del Presenter
│   │   │       ├── AuthOutputBoundary.java          ← [NUEVO] Interfaz del Presenter
│   │   │       └── GetUserOutputBoundary.java       ← [NUEVO] Interfaz del Presenter
│   │   ├── gateways/
│   │   │   ├── UserGateway.java                     ← [UNIFICACIÓN de puertos de auth]
│   │   │   ├── PasswordEncoderGateway.java          ← [RENOMBRADO de PasswordEncoderPort]
│   │   │   └── JwtTokenGateway.java                 ← [RENOMBRADO de JwtTokenPort]
│   │   ├── models/
│   │   │   ├── RegisterRequestModel.java            ← [RENOMBRADO de RegisterRequestDto]
│   │   │   ├── LoginRequestModel.java               ← [RENOMBRADO de LoginRequestDto]
│   │   │   └── UserResponseModel.java               ← [RENOMBRADO de UserResponseDto]
│   │   └── interactors/                             ← [RENOMBRADO desde auth/application/service]
│   │       ├── RegisterUserInteractor.java          ← [RENOMBRADO de RegisterUserService]
│   │       ├── AuthenticateUserInteractor.java      ← [RENOMBRADO de AuthenticateUserService]
│   │       └── GetCurrentUserInteractor.java        ← [RENOMBRADO de GetCurrentUserService]
│   │
│   ├── adapters/
│   │   ├── controllers/
│   │   │   └── AuthController.java                  ← [MOVIDO desde auth/infrastructure/rest]
│   │   ├── presenters/
│   │   │   ├── RegisterUserPresenter.java           ← [NUEVO] Implementa RegisterUserOutputBoundary
│   │   │   ├── AuthPresenter.java                   ← [NUEVO] Implementa AuthOutputBoundary
│   │   │   └── GetUserPresenter.java                ← [NUEVO] Implementa GetUserOutputBoundary
│   │   └── gateways/
│   │       └── mongodb/
│   │           ├── MongoUserGatewayAdapter.java     ← [RENOMBRADO de MongoUserPersistenceAdapter]
│   │           ├── SpringDataMongoUserRepository.java ← [SIN CAMBIOS]
│   │           └── UserDocument.java                ← [SIN CAMBIOS]
│   │
│   └── frameworks/                                  ← [RENOMBRADO desde auth/infrastructure]
│       ├── config/
│       │   ├── BeanConfig.java                      ← [SIN CAMBIOS]
│       │   └── OpenApiConfig.java                   ← [SIN CAMBIOS]
│       └── security/
│           ├── BcryptPasswordEncoderAdapter.java    ← [SIN CAMBIOS]
│           ├── CustomUserDetailsService.java        ← [SIN CAMBIOS]
│           ├── JwtAuthenticationFilter.java         ← [SIN CAMBIOS]
│           ├── JwtProviderAdapter.java              ← [SIN CAMBIOS]
│           └── SecurityConfig.java                  ← [SIN CAMBIOS]
│
├── order/
│   ├── entities/                                    ← [RENOMBRADO desde order/domain]
│   │   ├── ExchangeOrder.java                       ← [SIN CAMBIOS DE CÓDIGO]
│   │   └── OrderStatus.java                         ← [SIN CAMBIOS DE CÓDIGO]
│   │
│   ├── usecases/
│   │   ├── boundaries/
│   │   │   ├── input/
│   │   │   │   ├── CreateOrderInputBoundary.java    ← [RENOMBRADO de CreateExchangeOrderUseCase]
│   │   │   │   ├── GetMyOrdersInputBoundary.java    ← [RENOMBRADO de GetMyOrdersUseCase]
│   │   │   │   └── ConfirmTransferInputBoundary.java ← [RENOMBRADO de ConfirmTransferUseCase]
│   │   │   └── output/
│   │   │       ├── CreateOrderOutputBoundary.java   ← [NUEVO]
│   │   │       ├── GetMyOrdersOutputBoundary.java   ← [NUEVO]
│   │   │       └── ConfirmTransferOutputBoundary.java ← [NUEVO]
│   │   ├── gateways/
│   │   │   └── ExchangeOrderGateway.java            ← [UNIFICACIÓN de puertos de order]
│   │   ├── models/
│   │   │   ├── CreateOrderRequestModel.java         ← [RENOMBRADO de CreateOrderDto]
│   │   │   └── OrderResponseModel.java              ← [RENOMBRADO de OrderResponseDto]
│   │   └── interactors/
│   │       ├── CreateExchangeOrderInteractor.java   ← [RENOMBRADO de CreateExchangeOrderService]
│   │       ├── GetMyOrdersInteractor.java           ← [RENOMBRADO de GetMyOrdersService]
│   │       └── ConfirmTransferInteractor.java       ← [RENOMBRADO de ConfirmTransferService]
│   │
│   ├── adapters/
│   │   ├── controllers/
│   │   │   └── ExchangeOrderController.java         ← [MOVIDO desde order/infrastructure/rest]
│   │   ├── presenters/
│   │   │   ├── CreateOrderPresenter.java            ← [NUEVO]
│   │   │   ├── GetMyOrdersPresenter.java            ← [NUEVO]
│   │   │   └── ConfirmTransferPresenter.java        ← [NUEVO]
│   │   └── gateways/
│   │       └── mongodb/
│   │           ├── MongoExchangeOrderGatewayAdapter.java ← [RENOMBRADO de MongoOrderPersistenceAdapter]
│   │           ├── OrderDocument.java               ← [SIN CAMBIOS]
│   │           └── SpringDataMongoOrderRepository.java ← [SIN CAMBIOS]
│   │
│   └── frameworks/                                  ← [RENOMBRADO desde order/infrastructure]
│       └── config/
│
└── engine/
    ├── entities/                                    ← [RENOMBRADO desde engine/domain]
    │   ├── CalculationContext.java                  ← [SIN CAMBIOS DE CÓDIGO]
    │   ├── CalculationDetailItem.java               ← [SIN CAMBIOS DE CÓDIGO]
    │   ├── CurrencyType.java                        ← [SIN CAMBIOS DE CÓDIGO]
    │   ├── CustomerLevel.java                       ← [SIN CAMBIOS DE CÓDIGO]
    │   ├── Money.java                               ← [SIN CAMBIOS DE CÓDIGO]
    │   ├── OperationType.java                       ← [SIN CAMBIOS DE CÓDIGO]
    │   └── strategy/                               ← [SIN CAMBIOS DE CÓDIGO]
    │
    ├── usecases/
    │   ├── boundaries/
    │   │   ├── input/
    │   │   │   └── CalculateRateInputBoundary.java  ← [RENOMBRADO de CalculateExchangeRateUseCase]
    │   │   └── output/
    │   │       └── CalculateRateOutputBoundary.java ← [NUEVO]
    │   ├── gateways/
    │   │   ├── ExchangeRateGateway.java             ← [RENOMBRADO de ExchangeRateRepositoryPort]
    │   │   ├── ExchangeRuleGateway.java             ← [RENOMBRADO de ExchangeRuleRepositoryPort]
    │   │   └── UserPointsGateway.java               ← [RENOMBRADO de UserPointsRepositoryPort]
    │   └── interactors/
    │       └── CalculateExchangeRateInteractor.java ← [RENOMBRADO de CalculateExchangeRateService]
    │
    ├── adapters/
    │   ├── controllers/
    │   │   └── ExchangeEngineController.java        ← [MOVIDO desde engine/infrastructure/rest]
    │   ├── presenters/
    │   │   └── CalculateRatePresenter.java          ← [NUEVO]
    │   └── gateways/
    │       └── mongodb/
    │           ├── MongoEngineGatewayAdapter.java   ← [RENOMBRADO de MongoEnginePersistenceAdapter]
    │           ├── UserPointsDocument.java          ← [SIN CAMBIOS]
    │           └── SpringDataMongoUserPointsRepository.java ← [SIN CAMBIOS]
    │
    └── frameworks/                                  ← [RENOMBRADO desde engine/infrastructure]
```

---

## 🔧 3. Catálogo Detallado de Cambios por Archivo

### 3.1 Leyenda

| Símbolo                        | Significado                                                              |
| ------------------------------ | ------------------------------------------------------------------------ |
| 🟢 **[NUEVO]**                 | Archivo nuevo que aparece en Clean Architecture y no existe en Hexagonal |
| 🟡 **[RENOMBRADO / MOVIDO]**   | Archivo que cambia de nombre, paquete, o ambos                           |
| 🔵 **[SIN CAMBIOS DE CÓDIGO]** | Archivo que solo necesita actualizar su declaración `package`            |
| 🟠 **[CÓDIGO MODIFICADO]**     | Archivo cuya lógica interna cambia                                       |
| ⚫ **[CONSOLIDADO]**           | Dos archivos de Hexagonal se fusionan en uno en Clean Architecture       |

---

### 3.2 Módulo `auth`

#### Capa 1: Entities (en Hexagonal: `domain`)

| Archivo en Hexagonal                                     | Archivo en Clean Architecture                              | Tipo                     | Detalle del cambio                                                                                                                                               |
| -------------------------------------------------------- | ---------------------------------------------------------- | ------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `auth/domain/model/User.java`                            | `auth/entities/User.java`                                  | 🔵 SIN CAMBIOS DE CÓDIGO | La capa `domain/model` pasa a llamarse `entities`. Solo cambia el `package` declaration.                                                                         |
| `auth/domain/model/UserRole.java`                        | `auth/entities/UserRole.java`                              | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                                                                                                             |
| `auth/domain/model/UserStatus.java`                      | `auth/entities/UserStatus.java`                            | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                                                                                                             |
| `auth/domain/exceptions/UserAlreadyExistsException.java` | `auth/entities/exceptions/UserAlreadyExistsException.java` | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                                                                                                             |
| `auth/domain/valueobjects/Email.java`                    | `shared/entities/valueobjects/Email.java`                  | 🟡 MOVIDO A SHARED       | En Clean Architecture los Value Objects son considerados parte de las Entities globales del sistema, no de un módulo específico. Se mueven a un módulo `shared`. |
| `auth/domain/valueobjects/Password.java`                 | `shared/entities/valueobjects/Password.java`               | 🟡 MOVIDO A SHARED       | Idem                                                                                                                                                             |
| `auth/domain/valueobjects/Dni.java`                      | `shared/entities/valueobjects/Dni.java`                    | 🟡 MOVIDO A SHARED       | Idem                                                                                                                                                             |
| `auth/domain/valueobjects/Ruc.java`                      | `shared/entities/valueobjects/Ruc.java`                    | 🟡 MOVIDO A SHARED       | Idem                                                                                                                                                             |
| `auth/domain/ports/UserRepositoryPort.java`              | _(fusionado en UserGateway)_                               | ⚫ CONSOLIDADO           | En Clean Architecture los contratos de salida se denominan **Gateways** y viven en la capa Use Cases. Ambos puertos de `auth` se consolidan en `UserGateway`.    |
| `auth/domain/ports/JwtTokenPort.java`                    | `auth/usecases/gateways/JwtTokenGateway.java`              | 🟡 RENOMBRADO            | Pasa de `Port` a `Gateway` y de la capa `domain` a la capa `usecases`.                                                                                           |
| `auth/domain/ports/PasswordEncoderPort.java`             | `auth/usecases/gateways/PasswordEncoderGateway.java`       | 🟡 RENOMBRADO            | Idem                                                                                                                                                             |

#### Capa 2: Use Cases (en Hexagonal: `application`)

| Archivo en Hexagonal                                          | Archivo en Clean Architecture                                       | Tipo                 | Detalle del cambio                                                                                                                                                    |
| ------------------------------------------------------------- | ------------------------------------------------------------------- | -------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `auth/application/ports/inbound/RegisterUserUseCase.java`     | `auth/usecases/boundaries/input/RegisterUserInputBoundary.java`     | 🟡 RENOMBRADO        | La interfaz de entrada al caso de uso adopta el nombre **Input Boundary**. La firma del método es la misma.                                                           |
| `auth/application/ports/inbound/AuthenticateUserUseCase.java` | `auth/usecases/boundaries/input/AuthenticateUserInputBoundary.java` | 🟡 RENOMBRADO        | Idem                                                                                                                                                                  |
| `auth/application/ports/inbound/GetCurrentUserUseCase.java`   | `auth/usecases/boundaries/input/GetCurrentUserInputBoundary.java`   | 🟡 RENOMBRADO        | Idem                                                                                                                                                                  |
| `auth/application/usecases/RegisterUserUseCase.java`          | `auth/usecases/boundaries/input/RegisterUserInputBoundary.java`     | ⚫ CONSOLIDADO       | Ambas definiciones del UseCase (ports/inbound y usecases) se consolidan en un único Input Boundary.                                                                   |
| `auth/application/usecases/AuthenticateUserUseCase.java`      | _(fusionado en AuthenticateUserInputBoundary)_                      | ⚫ CONSOLIDADO       | Idem                                                                                                                                                                  |
| `auth/application/usecases/GetCurrentUserUseCase.java`        | _(fusionado en GetCurrentUserInputBoundary)_                        | ⚫ CONSOLIDADO       | Idem                                                                                                                                                                  |
| `auth/application/ports/outbound/UserPersistencePort.java`    | `auth/usecases/gateways/UserGateway.java`                           | ⚫ CONSOLIDADO       | Se unifica con `UserRepositoryPort` del dominio en un único **Gateway**, que vive en la capa Use Cases.                                                               |
| `auth/application/ports/outbound/JwtTokenPort.java`           | _(consolidado en JwtTokenGateway)_                                  | ⚫ CONSOLIDADO       | Idem                                                                                                                                                                  |
| `auth/application/ports/outbound/PasswordEncoderPort.java`    | _(consolidado en PasswordEncoderGateway)_                           | ⚫ CONSOLIDADO       | Idem                                                                                                                                                                  |
| `auth/application/service/RegisterUserService.java`           | `auth/usecases/interactors/RegisterUserInteractor.java`             | 🟠 CÓDIGO MODIFICADO | Cambia el nombre de `Service` a `Interactor`. Internamente, el método pasa de **retornar** el resultado a **llamar al Output Boundary** (Presenter). Ver sección 4.1. |
| `auth/application/service/AuthenticateUserService.java`       | `auth/usecases/interactors/AuthenticateUserInteractor.java`         | 🟠 CÓDIGO MODIFICADO | Idem                                                                                                                                                                  |
| `auth/application/service/GetCurrentUserService.java`         | `auth/usecases/interactors/GetCurrentUserInteractor.java`           | 🟠 CÓDIGO MODIFICADO | Idem                                                                                                                                                                  |
| `auth/application/dto/RegisterRequestDto.java`                | `auth/usecases/models/RegisterRequestModel.java`                    | 🟡 RENOMBRADO        | El DTO de entrada pasa a llamarse **Request Model** y se ubica dentro de `usecases/models`.                                                                           |
| `auth/application/dto/LoginRequestDto.java`                   | `auth/usecases/models/LoginRequestModel.java`                       | 🟡 RENOMBRADO        | Idem                                                                                                                                                                  |
| `auth/application/dto/UserResponseDto.java`                   | `auth/usecases/models/UserResponseModel.java`                       | 🟡 RENOMBRADO        | El DTO de salida pasa a llamarse **Response Model**. El formato final para HTTP lo decide el **Presenter**.                                                           |
| `auth/application/mappers/UserMapper.java`                    | _(internalizado en los Presenters)_                                 | ⚫ CONSOLIDADO       | La lógica de mapeo de dominio → DTO pasa a los **Presenters** de la capa Interface Adapters.                                                                          |
| _(no existe en Hexagonal)_                                    | `auth/usecases/boundaries/output/RegisterUserOutputBoundary.java`   | 🟢 NUEVO             | Interfaz que el Interactor usa para entregar su resultado al Presenter. Desacopla el caso de uso del formato de la respuesta.                                         |
| _(no existe en Hexagonal)_                                    | `auth/usecases/boundaries/output/AuthOutputBoundary.java`           | 🟢 NUEVO             | Idem para autenticación                                                                                                                                               |
| _(no existe en Hexagonal)_                                    | `auth/usecases/boundaries/output/GetUserOutputBoundary.java`        | 🟢 NUEVO             | Idem para obtener usuario actual                                                                                                                                      |

#### Capa 3: Interface Adapters (en Hexagonal: `infrastructure/rest` + `adapters/outbound`)

| Archivo en Hexagonal                                                | Archivo en Clean Architecture                                       | Tipo                     | Detalle del cambio                                                                                                                                                            |
| ------------------------------------------------------------------- | ------------------------------------------------------------------- | ------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `auth/infrastructure/rest/AuthController.java`                      | `auth/adapters/controllers/AuthController.java`                     | 🟠 CÓDIGO MODIFICADO     | Se mueve de `infrastructure` a `adapters`. Internamente ya no retorna el resultado del Interactor directamente: ahora recoge el **ViewModel** del Presenter. Ver sección 4.2. |
| _(no existe en Hexagonal)_                                          | `auth/adapters/presenters/RegisterUserPresenter.java`               | 🟢 NUEVO                 | Implementa `RegisterUserOutputBoundary`. Recibe el `UserResponseModel` del Interactor y lo transforma al ViewModel HTTP.                                                      |
| _(no existe en Hexagonal)_                                          | `auth/adapters/presenters/AuthPresenter.java`                       | 🟢 NUEVO                 | Idem para autenticación                                                                                                                                                       |
| _(no existe en Hexagonal)_                                          | `auth/adapters/presenters/GetUserPresenter.java`                    | 🟢 NUEVO                 | Idem para obtener usuario actual                                                                                                                                              |
| `auth/adapters/outbound/mongodb/MongoUserPersistenceAdapter.java`   | `auth/adapters/gateways/mongodb/MongoUserGatewayAdapter.java`       | 🟡 RENOMBRADO            | De `PersistenceAdapter` a `GatewayAdapter`. Pasa a implementar `UserGateway`. La lógica interna no cambia.                                                                    |
| `auth/adapters/outbound/mongodb/SpringDataMongoUserRepository.java` | `auth/adapters/gateways/mongodb/SpringDataMongoUserRepository.java` | 🔵 SIN CAMBIOS DE CÓDIGO | Solo cambio de `package`                                                                                                                                                      |
| `auth/adapters/outbound/mongodb/UserDocument.java`                  | `auth/adapters/gateways/mongodb/UserDocument.java`                  | 🔵 SIN CAMBIOS DE CÓDIGO | Solo cambio de `package`                                                                                                                                                      |

#### Capa 4: Frameworks & Drivers (en Hexagonal: `infrastructure`)

| Archivo en Hexagonal                                             | Archivo en Clean Architecture                                | Tipo                     | Detalle del cambio                                                               |
| ---------------------------------------------------------------- | ------------------------------------------------------------ | ------------------------ | -------------------------------------------------------------------------------- |
| `auth/infrastructure/security/BcryptPasswordEncoderAdapter.java` | `auth/frameworks/security/BcryptPasswordEncoderAdapter.java` | 🔵 SIN CAMBIOS DE CÓDIGO | La capa `infrastructure` pasa a llamarse `frameworks`. Solo cambia el `package`. |
| `auth/infrastructure/security/JwtProviderAdapter.java`           | `auth/frameworks/security/JwtProviderAdapter.java`           | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                             |
| `auth/infrastructure/security/CustomUserDetailsService.java`     | `auth/frameworks/security/CustomUserDetailsService.java`     | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                             |
| `auth/infrastructure/security/JwtAuthenticationFilter.java`      | `auth/frameworks/security/JwtAuthenticationFilter.java`      | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                             |
| `auth/infrastructure/security/SecurityConfig.java`               | `auth/frameworks/security/SecurityConfig.java`               | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                             |
| `auth/infrastructure/config/BeanConfig.java`                     | `auth/frameworks/config/BeanConfig.java`                     | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                             |
| `auth/infrastructure/config/OpenApiConfig.java`                  | `auth/frameworks/config/OpenApiConfig.java`                  | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                             |

#### Módulo `shared`

| Archivo en Hexagonal                      | Archivo en Clean Architecture                           | Tipo      | Detalle del cambio                                                                                            |
| ----------------------------------------- | ------------------------------------------------------- | --------- | ------------------------------------------------------------------------------------------------------------- |
| `auth/shared/ApiResponse.java`            | `shared/adapters/presenter/ApiResponse.java`            | 🟡 MOVIDO | En Clean Architecture este objeto de presentación HTTP es un componente de la capa Interface Adapters global. |
| `auth/shared/ErrorResponse.java`          | `shared/adapters/presenter/ErrorResponse.java`          | 🟡 MOVIDO | Idem                                                                                                          |
| `auth/shared/GlobalExceptionHandler.java` | `shared/adapters/presenter/GlobalExceptionHandler.java` | 🟡 MOVIDO | Se trata como un Presenter global de errores, en la capa Interface Adapters compartida.                       |
| `auth/shared/SecurityConstants.java`      | `shared/frameworks/security/SecurityConstants.java`     | 🟡 MOVIDO | Pertenece a la capa Frameworks & Drivers al ser una constante de Spring Security.                             |

---

### 3.3 Módulo `order`

| Archivo en Hexagonal                                                  | Archivo en Clean Architecture                                           | Tipo                     | Detalle del cambio                                                    |
| --------------------------------------------------------------------- | ----------------------------------------------------------------------- | ------------------------ | --------------------------------------------------------------------- |
| `order/domain/model/ExchangeOrder.java`                               | `order/entities/ExchangeOrder.java`                                     | 🔵 SIN CAMBIOS DE CÓDIGO | Solo cambio de `package`                                              |
| `order/domain/model/OrderStatus.java`                                 | `order/entities/OrderStatus.java`                                       | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                  |
| `order/domain/ports/ExchangeOrderRepositoryPort.java`                 | _(fusionado en ExchangeOrderGateway)_                                   | ⚫ CONSOLIDADO           | Se une con el puerto de application en un único Gateway               |
| `order/application/ports/outbound/ExchangeOrderPersistencePort.java`  | `order/usecases/gateways/ExchangeOrderGateway.java`                     | ⚫ CONSOLIDADO           | Unificación de ambos puertos en un único Gateway                      |
| `order/application/usecases/CreateExchangeOrderUseCase.java`          | `order/usecases/boundaries/input/CreateOrderInputBoundary.java`         | 🟡 RENOMBRADO            | Solo cambia nombre y paquete                                          |
| `order/application/usecases/GetMyOrdersUseCase.java`                  | `order/usecases/boundaries/input/GetMyOrdersInputBoundary.java`         | 🟡 RENOMBRADO            | Idem                                                                  |
| `order/application/usecases/ConfirmTransferUseCase.java`              | `order/usecases/boundaries/input/ConfirmTransferInputBoundary.java`     | 🟡 RENOMBRADO            | Idem                                                                  |
| `order/application/service/CreateExchangeOrderService.java`           | `order/usecases/interactors/CreateExchangeOrderInteractor.java`         | 🟠 CÓDIGO MODIFICADO     | Usa Output Boundary en lugar de retorno directo                       |
| `order/application/service/GetMyOrdersService.java`                   | `order/usecases/interactors/GetMyOrdersInteractor.java`                 | 🟠 CÓDIGO MODIFICADO     | Idem                                                                  |
| `order/application/service/ConfirmTransferService.java`               | `order/usecases/interactors/ConfirmTransferInteractor.java`             | 🟠 CÓDIGO MODIFICADO     | Idem                                                                  |
| `order/adapters/outbound/mongodb/MongoOrderPersistenceAdapter.java`   | `order/adapters/gateways/mongodb/MongoExchangeOrderGatewayAdapter.java` | 🟡 RENOMBRADO            | Solo nombre y paquete                                                 |
| `order/adapters/outbound/mongodb/OrderDocument.java`                  | `order/adapters/gateways/mongodb/OrderDocument.java`                    | 🔵 SIN CAMBIOS DE CÓDIGO | Solo cambio de `package`                                              |
| `order/adapters/outbound/mongodb/SpringDataMongoOrderRepository.java` | `order/adapters/gateways/mongodb/SpringDataMongoOrderRepository.java`   | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                  |
| `order/infrastructure/rest/ExchangeOrderController.java`              | `order/adapters/controllers/ExchangeOrderController.java`               | 🟠 CÓDIGO MODIFICADO     | Pasa de `infrastructure` a `adapters` e incorpora el patrón Presenter |
| _(no existe en Hexagonal)_                                            | `order/adapters/presenters/CreateOrderPresenter.java`                   | 🟢 NUEVO                 | Implementa `CreateOrderOutputBoundary`                                |
| _(no existe en Hexagonal)_                                            | `order/adapters/presenters/GetMyOrdersPresenter.java`                   | 🟢 NUEVO                 | Implementa `GetMyOrdersOutputBoundary`                                |
| _(no existe en Hexagonal)_                                            | `order/adapters/presenters/ConfirmTransferPresenter.java`               | 🟢 NUEVO                 | Implementa `ConfirmTransferOutputBoundary`                            |

---

### 3.4 Módulo `engine`

| Archivo en Hexagonal                                                  | Archivo en Clean Architecture                                      | Tipo                     | Detalle del cambio                                                           |
| --------------------------------------------------------------------- | ------------------------------------------------------------------ | ------------------------ | ---------------------------------------------------------------------------- |
| `engine/domain/model/CalculationContext.java`                         | `engine/entities/CalculationContext.java`                          | 🔵 SIN CAMBIOS DE CÓDIGO | Solo cambio de `package`                                                     |
| `engine/domain/model/CurrencyType.java`                               | `engine/entities/CurrencyType.java`                                | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                         |
| `engine/domain/model/Money.java`                                      | `engine/entities/Money.java`                                       | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                         |
| `engine/domain/model/OperationType.java`                              | `engine/entities/OperationType.java`                               | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                         |
| `engine/domain/model/CustomerLevel.java`                              | `engine/entities/CustomerLevel.java`                               | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                         |
| `engine/domain/model/CalculationDetailItem.java`                      | `engine/entities/CalculationDetailItem.java`                       | 🔵 SIN CAMBIOS DE CÓDIGO | Idem                                                                         |
| `engine/domain/strategy/`                                             | `engine/entities/strategy/`                                        | 🔵 SIN CAMBIOS DE CÓDIGO | Solo cambio de `package` para todos los archivos de la estrategia de cálculo |
| `engine/domain/ports/ExchangeRateRepositoryPort.java`                 | `engine/usecases/gateways/ExchangeRateGateway.java`                | 🟡 RENOMBRADO            | Los Gateways en Clean Architecture viven en la capa Use Cases                |
| `engine/domain/ports/ExchangeRuleRepositoryPort.java`                 | `engine/usecases/gateways/ExchangeRuleGateway.java`                | 🟡 RENOMBRADO            | Idem                                                                         |
| `engine/domain/ports/UserPointsRepositoryPort.java`                   | `engine/usecases/gateways/UserPointsGateway.java`                  | 🟡 RENOMBRADO            | Idem                                                                         |
| `engine/application/usecases/CalculateExchangeRateUseCase.java`       | `engine/usecases/boundaries/input/CalculateRateInputBoundary.java` | 🟡 RENOMBRADO            | Idem                                                                         |
| `engine/application/service/CalculateExchangeRateService.java`        | `engine/usecases/interactors/CalculateExchangeRateInteractor.java` | 🟠 CÓDIGO MODIFICADO     | Usa Output Boundary                                                          |
| `engine/adapters/outbound/mongodb/MongoEnginePersistenceAdapter.java` | `engine/adapters/gateways/mongodb/MongoEngineGatewayAdapter.java`  | 🟡 RENOMBRADO            | Solo nombre y paquete                                                        |
| `engine/adapters/outbound/mongodb/UserPointsDocument.java`            | `engine/adapters/gateways/mongodb/UserPointsDocument.java`         | 🔵 SIN CAMBIOS DE CÓDIGO | Solo cambio de `package`                                                     |
| `engine/infrastructure/rest/ExchangeEngineController.java`            | `engine/adapters/controllers/ExchangeEngineController.java`        | 🟠 CÓDIGO MODIFICADO     | Incorpora el patrón Presenter                                                |
| _(no existe en Hexagonal)_                                            | `engine/adapters/presenters/CalculateRatePresenter.java`           | 🟢 NUEVO                 | Implementa `CalculateRateOutputBoundary`                                     |

---

### 3.5 Pruebas (Tests)

| Archivo en Hexagonal                                       | Archivo en Clean Architecture                              | Tipo                 | Detalle del cambio                                                                                                                                                         |
| ---------------------------------------------------------- | ---------------------------------------------------------- | -------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `architecture/HexagonalArchitectureTest.java`              | `architecture/CleanArchitectureTest.java`                  | 🟠 CÓDIGO MODIFICADO | Las reglas ArchUnit cambian para verificar las 4 capas concéntricas (`entities`, `usecases`, `adapters`, `frameworks`) en lugar de las capas hexagonales. Ver sección 4.3. |
| `auth/application/RegisterUserUseCaseTest.java`            | `auth/usecases/RegisterUserInteractorTest.java`            | 🟡 RENOMBRADO        | El Mock del puerto de salida pasa a llamarse `UserGateway`. Lógica de prueba idéntica.                                                                                     |
| `auth/application/AuthenticateUserUseCaseTest.java`        | `auth/usecases/AuthenticateUserInteractorTest.java`        | 🟡 RENOMBRADO        | Idem                                                                                                                                                                       |
| `engine/application/CalculateExchangeRateUseCaseTest.java` | `engine/usecases/CalculateExchangeRateInteractorTest.java` | 🟡 RENOMBRADO        | Idem                                                                                                                                                                       |
| `order/application/CreateExchangeOrderUseCaseTest.java`    | `order/usecases/CreateExchangeOrderInteractorTest.java`    | 🟡 RENOMBRADO        | Idem                                                                                                                                                                       |

---

## 💻 4. Comparativa de Código: Cambios Internos Clave

### 4.1 El Interactor y el Output Boundary: la diferencia más característica

En Hexagonal, el servicio retorna el resultado directamente al Controller. En Clean Architecture, el Interactor no retorna nada: llama al **Output Boundary**, que el **Presenter** implementa para preparar la respuesta.

#### Hexagonal — El Service retorna el resultado directamente

```java
// auth/application/service/RegisterUserService.java (Hexagonal)
@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoderPort passwordEncoderPort;

    @Override
    public User execute(RegisterCommand command) {   // ← retorna User
        User user = User.builder()
                .email(command.getEmail())
                .password(passwordEncoderPort.encode(command.getPassword()))
                // ...
                .build();
        return userPersistencePort.save(user);        // ← el Controller recibe esto directamente
    }
}
```

#### Clean Architecture — El Interactor llama al Presenter vía Output Boundary

```java
// auth/usecases/interactors/RegisterUserInteractor.java (Clean Architecture)
@Service
public class RegisterUserInteractor implements RegisterUserInputBoundary {

    private final UserGateway userGateway;
    private final PasswordEncoderGateway passwordEncoderGateway;
    private final RegisterUserOutputBoundary outputBoundary;   // ← inyecta el Presenter

    @Override
    public void execute(RegisterRequestModel requestModel) {   // ← void, no retorna nada
        User user = User.builder()
                .email(requestModel.getEmail())
                .password(passwordEncoderGateway.encode(requestModel.getPassword()))
                // ...
                .build();
        User savedUser = userGateway.save(user);

        UserResponseModel responseModel = UserResponseModel.from(savedUser);
        outputBoundary.presentSuccess(responseModel);   // ← delega al Presenter
    }
}
```

#### Nueva interfaz Output Boundary (no existe en Hexagonal)

```java
// auth/usecases/boundaries/output/RegisterUserOutputBoundary.java (Clean Architecture - NUEVO)
public interface RegisterUserOutputBoundary {
    void presentSuccess(UserResponseModel responseModel);
    void presentError(String errorMessage);
}
```

#### Nuevo Presenter en Interface Adapters (no existe en Hexagonal)

```java
// auth/adapters/presenters/RegisterUserPresenter.java (Clean Architecture - NUEVO)
@Component
@RequestScope   // un Presenter por solicitud HTTP para evitar colisiones de concurrencia
public class RegisterUserPresenter implements RegisterUserOutputBoundary {

    private ApiResponse<UserResponseDto> viewModel;

    @Override
    public void presentSuccess(UserResponseModel model) {
        UserResponseDto dto = UserResponseDto.fromResponseModel(model);
        this.viewModel = ApiResponse.success("Registrado exitosamente", dto);
    }

    @Override
    public void presentError(String errorMessage) {
        this.viewModel = ApiResponse.error(errorMessage);
    }

    public ApiResponse<UserResponseDto> getViewModel() {
        return viewModel;
    }
}
```

#### El Controller ahora recoge el ViewModel del Presenter

```java
// auth/adapters/controllers/AuthController.java (Clean Architecture)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserInputBoundary registerInteractor;
    private final RegisterUserPresenter registerPresenter;   // ← inyecta el Presenter

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto request) {

        RegisterRequestModel requestModel = RegisterRequestModel.from(request);
        registerInteractor.execute(requestModel);   // ← Interactor llama internamente al Presenter

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registerPresenter.getViewModel());   // ← recoge el ViewModel listo
    }
}
```

---

### 4.2 Gateway: Consolidación de los contratos de salida

En Hexagonal, el proyecto mantiene los contratos de salida tanto en `domain/ports` como en `application/ports/outbound`. En Clean Architecture, todos los contratos de salida se denominan **Gateways** y viven en la capa Use Cases.

#### Hexagonal — Dos interfaces para el mismo concepto de usuario

```java
// auth/domain/ports/UserRepositoryPort.java (Hexagonal)
public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

// auth/application/ports/outbound/UserPersistencePort.java (Hexagonal)
public interface UserPersistencePort {
    User save(User user);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

#### Clean Architecture — Un único Gateway unificado

```java
// auth/usecases/gateways/UserGateway.java (Clean Architecture)
public interface UserGateway {
    User save(User user);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findById(UUID id);
}
```

---

### 4.3 ArchUnit: Las reglas de gobernanza reflejan las 4 capas concéntricas

Las pruebas de arquitectura cambian para verificar el modelo de capas de Clean Architecture en lugar del modelo hexagonal.

#### Hexagonal — Verifica la separación dominio/adaptadores

```java
// HexagonalArchitectureTest.java (Hexagonal)
@AnalyzeClasses(packages = "com.cambistaonline")
public class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_adapters =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapters..");

    @ArchTest
    static final ArchRule adapters_should_not_depend_on_each_other =
        noClasses()
            .that().resideInAPackage("..adapters..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapters..");
}
```

#### Clean Architecture — Verifica las 4 capas concéntricas

```java
// CleanArchitectureTest.java (Clean Architecture)
@AnalyzeClasses(packages = "com.cambistaonline")
public class CleanArchitectureTest {

    // Regla 1: Entities no dependen de ninguna capa exterior
    @ArchTest
    static final ArchRule entities_are_independent =
        noClasses()
            .that().resideInAPackage("..entities..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..usecases..", "..adapters..", "..frameworks..");

    // Regla 2: Use Cases solo conocen Entities (no adapters ni frameworks)
    @ArchTest
    static final ArchRule usecases_do_not_reach_outer_layers =
        noClasses()
            .that().resideInAPackage("..usecases..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "..adapters..", "..frameworks..",
                "org.springframework.web..",
                "org.springframework.data.mongodb..",
                "org.springframework.security.."
            );

    // Regla 3: Adapters no acceden a la capa Frameworks directamente
    @ArchTest
    static final ArchRule adapters_do_not_depend_on_frameworks =
        noClasses()
            .that().resideInAPackage("..adapters..")
            .should().dependOnClassesThat()
            .resideInAPackage("..frameworks..");

    // Regla 4: Interactors implementan Input Boundaries
    @ArchTest
    static final ArchRule interactors_implement_input_boundaries =
        classes()
            .that().haveSimpleNameEndingWith("Interactor")
            .should().implement(JavaClass.Predicates.simpleNameEndingWith("InputBoundary"));

    // Regla 5: Presenters implementan Output Boundaries
    @ArchTest
    static final ArchRule presenters_implement_output_boundaries =
        classes()
            .that().haveSimpleNameEndingWith("Presenter")
            .should().implement(JavaClass.Predicates.simpleNameEndingWith("OutputBoundary"));
}
```

---

## 📊 5. Resumen Estadístico de la Transición

| Categoría                                                     | Cantidad |
| ------------------------------------------------------------- | -------- |
| 🟢 Archivos **nuevos** en Clean Architecture                  | **18**   |
| 🟡 Archivos **renombrados o movidos** (sin cambios de código) | **32**   |
| 🟠 Archivos con **cambios de código** internos                | **12**   |
| ⚫ Archivos **consolidados** (dos pasan a ser uno)            | **11**   |
| 🔵 Archivos con **solo cambio de `package`**                  | **28**   |
| **Total de archivos involucrados**                            | **~73**  |

> **Nota**: La gran mayoría de archivos que representan lógica de negocio (entidades, estrategias, documentos MongoDB) **no requieren cambios en su código fuente**. Esto confirma que ambas arquitecturas comparten el mismo principio de proteger el núcleo de negocio.

---

## ⚖️ 6. Tabla de Equivalencias de Terminología

| Concepto                               | Hexagonal                    | Clean Architecture                         |
| -------------------------------------- | ---------------------------- | ------------------------------------------ |
| Lógica de negocio central              | `domain/model`               | **entities**                               |
| Orquestador del negocio                | `Service`                    | **Interactor**                             |
| Interfaz de entrada                    | Puerto Inbound (`UseCase`)   | **Input Boundary**                         |
| Interfaz de salida hacia persistencia  | Puerto Outbound (`Port`)     | **Gateway**                                |
| Contrato para entrega del resultado    | _(no existe explícitamente)_ | **Output Boundary**                        |
| Objeto que formatea la respuesta       | _(no existe explícitamente)_ | **Presenter**                              |
| Adaptador de persistencia              | `PersistenceAdapter`         | **Gateway Adapter**                        |
| Datos de entrada al caso de uso        | `Command` / `RequestDto`     | **Request Model**                          |
| Datos de salida del caso de uso        | `ResponseDto`                | **Response Model**                         |
| Datos listos para presentar al cliente | `ResponseDto` (directo)      | **ViewModel** (preparado por el Presenter) |
| Capa de frameworks externos            | `infrastructure`             | **frameworks**                             |
| Tests de gobernanza                    | `HexagonalArchitectureTest`  | `CleanArchitectureTest`                    |

---

## 🗓️ 7. Orden Sugerido de Transición

```
Fase 1: Crear rama de trabajo
  └── feature/clean-architecture

Fase 2: Módulo shared (base para todo el proyecto)
  ├── Crear shared/entities/valueobjects/ (mover Email, Password, Dni, Ruc)
  └── Crear shared/adapters/presenter/ (mover ApiResponse, ErrorResponse, GlobalExceptionHandler)

Fase 3: Módulo engine (menor número de dependencias externas)
  ├── Renombrar domain → entities
  ├── Crear usecases/gateways/ (consolidar ports)
  ├── Crear usecases/boundaries/input/ y output/
  ├── Renombrar service → interactors + adaptar a Output Boundary
  ├── Crear adapters/presenters/
  └── Mover controllers a adapters/controllers/

Fase 4: Módulo order
  └── (mismo flujo que engine)

Fase 5: Módulo auth (más dependencias por Spring Security)
  └── (mismo flujo + cuidado con SecurityConfig y BeanConfig)

Fase 6: Tests
  ├── Renombrar HexagonalArchitectureTest → CleanArchitectureTest
  ├── Actualizar reglas ArchUnit
  └── Renombrar tests de UseCase → Interactor

Fase 7: Verificación
  └── mvn test (todos los tests deben pasar)
```
