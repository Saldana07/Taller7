-- Flyway: versión 1 del esquema de la base de datos "demo"
CREATE TABLE IF NOT EXISTS student (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    age        INTEGER,
    state      BOOLEAN DEFAULT TRUE
);
