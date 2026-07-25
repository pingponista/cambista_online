# Cambista Online - Fintech Currency Exchange Platform

Plataforma de intercambio de divisas (USD, EUR, PEN) con soporte para Persona Natural y Persona Jurídica, desarrollada bajo arquitectura Onion en el backend y modularidad reactiva en el frontend.

## Estrategia de Repositorio: Monorepo Ligero

- `/backend`: API REST en Java 21 + Spring Boot 3 (Onion Architecture).
- `/frontend`: Web App en React + Vite + CSS Modules + Zustand.

## Arquitectura del Backend (Onion Architecture)

```text
com.cambistaonline/
├── domain/            # Core del negocio (POJOs puros, Cero dependencias externas)
│   ├── model/         # Entidades de Dominio (User, ExchangeOrder, BankAccount, Profiles)
│   ├── valueobjects/  # Money, ExchangeRate, Currency, Ruc, Dni
│   ├── exceptions/    # Excepciones de reglas de negocio
│   └── service/       # Lógica pura de cálculo e intercambio
├── application/       # Casos de uso, DTOs y Interfaces/Puertos
│   ├── usecases/      # Reglas de aplicación / Orquestación
│   ├── dto/           # Data Transfer Objects
│   └── ports/         # Puertos de entrada y salida
├── infrastructure/    # Adaptadores tecnológicos
│   ├── persistence/   # Entidades JPA, Repositorios Spring Data, Adapters
│   ├── security/      # Spring Security, JWT Token Provider, Filters
│   └── external/      # API de tipo de cambio en tiempo real y servicios externos
└── presentation/      # Capa de entrada / Delivery
    ├── controller/    # REST Controllers (@RestController)
    └── middleware/    # Global Exception Handler y Middlewares
```

## Requisitos de Ejecución

- **Java SDK 21**
- **Node.js 18+ & npm**
- **MySQL 8.0+**

## Ejecución en Desarrollo

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```
