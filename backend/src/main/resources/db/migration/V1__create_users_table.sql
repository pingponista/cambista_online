-- V1__create_users_table.sql
-- Creación de la tabla users recomendada para PostgreSQL / Neon

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,

    first_name VARCHAR(100),
    last_name VARCHAR(100),
    dni VARCHAR(20),

    company_name VARCHAR(200),
    ruc VARCHAR(20),
    legal_representative_name VARCHAR(200),

    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
