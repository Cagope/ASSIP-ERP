-- ============================================
-- V016 - REBUILD COMPLETO DEL SISTEMA SARLAFT
-- Tablas limpias con auditoría unificada
-- ============================================

-- ============================================
-- 0. ELIMINAR TABLAS EXISTENTES
-- ============================================

DROP TABLE IF EXISTS general.alertas_gestion CASCADE;
DROP TABLE IF EXISTS general.alertas CASCADE;
DROP TABLE IF EXISTS general.reglas_sarlaft CASCADE;


-- ============================================
-- 1. TABLA PRINCIPAL: general.alertas
-- ============================================

CREATE TABLE general.alertas (
    id_alerta BIGSERIAL PRIMARY KEY,

    -- Persona asociada (hoja_vida.datos_personales)
    id_datos_personal BIGINT NULL,

    -- Agencia responsable
    id_agencia INTEGER NULL,

    -- Código módulo origen (01=Contab, 02=Depósitos, etc.)
    codigo_modulo VARCHAR(3) NOT NULL,

    -- Severidad: VERDE / AMARILLO / ROJO
    severidad VARCHAR(10) NOT NULL,

    -- Descripción detallada del motivo
    descripcion TEXT NOT NULL,

    -- Acción automática del sistema
    accion_sistema TEXT NULL,

    -- Auditoría unificada
    fk_seguridad_creacion INTEGER NULL REFERENCES seguridad.usuarios(id_usuario) ON DELETE SET NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NULL REFERENCES seguridad.usuarios(id_usuario) ON DELETE SET NULL,
    fecha_edicion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices
CREATE INDEX idx_alertas_persona     ON general.alertas(id_datos_personal);
CREATE INDEX idx_alertas_modulo      ON general.alertas(codigo_modulo);
CREATE INDEX idx_alertas_severidad   ON general.alertas(severidad);
CREATE INDEX idx_alertas_fecha       ON general.alertas(fecha_creacion);

-- Relaciones externas
ALTER TABLE general.alertas
  ADD CONSTRAINT fk_alertas_persona
  FOREIGN KEY (id_datos_personal)
  REFERENCES hoja_vida.datos_personales(id_datos_personal)
  ON DELETE SET NULL;

ALTER TABLE general.alertas
  ADD CONSTRAINT fk_alertas_agencia
  FOREIGN KEY (id_agencia)
  REFERENCES general.datos_agencias(id_agencia)
  ON DELETE SET NULL;


-- ============================================
-- 2. TABLA: general.reglas_sarlaft
-- ============================================

CREATE TABLE general.reglas_sarlaft (
    id_regla BIGSERIAL PRIMARY KEY,

    codigo VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT NOT NULL,

    valor_numerico NUMERIC NULL,
    valor_texto VARCHAR(200) NULL,

    severidad VARCHAR(10) NOT NULL DEFAULT 'VERDE',
    codigo_modulo VARCHAR(3) NULL,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    -- Auditoría
    fk_seguridad_creacion INTEGER NULL REFERENCES seguridad.usuarios(id_usuario) ON DELETE SET NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NULL REFERENCES seguridad.usuarios(id_usuario) ON DELETE SET NULL,
    fecha_edicion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reglas_codigo ON general.reglas_sarlaft(codigo);
CREATE INDEX idx_reglas_modulo ON general.reglas_sarlaft(codigo_modulo);


-- ============================================
-- 3. TABLA: general.alertas_gestion
-- ============================================

CREATE TABLE general.alertas_gestion (
    id_gestion BIGSERIAL PRIMARY KEY,

    id_alerta BIGINT NOT NULL,
    comentario TEXT NOT NULL,

    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario BIGINT NULL REFERENCES seguridad.usuarios(id_usuario) ON DELETE SET NULL
);

ALTER TABLE general.alertas_gestion
  ADD CONSTRAINT fk_gestion_alerta
  FOREIGN KEY (id_alerta)
  REFERENCES general.alertas(id_alerta)
  ON DELETE CASCADE;

CREATE INDEX idx_gestion_alerta ON general.alertas_gestion(id_alerta);


-- ============================================
-- FIN DEL ARCHIVO V016
-- ============================================
