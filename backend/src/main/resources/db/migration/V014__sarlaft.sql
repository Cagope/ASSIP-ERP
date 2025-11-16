-- ============================================
-- V014 - Sistema SARLAFT / Riesgos
-- Estructura base del sistema de alertas
-- ============================================

-- ============================================
-- 1. Tabla principal: general.alertas
-- Registra alertas ROJAS generadas por cualquier módulo.
-- ============================================

CREATE TABLE IF NOT EXISTS general.alertas (
    id_alerta BIGSERIAL PRIMARY KEY,

    -- Persona asociada a la alerta (puede ser NULL)
    id_datos_personal BIGINT NULL,

    -- Agencia donde ocurrió la alerta
    id_agencia INTEGER NULL,

    -- Código del módulo que la generó (01=Contab, 02=Depósitos, etc.)
    codigo_modulo VARCHAR(3) NOT NULL,

    -- Severidad: VERDE / AMARILLO / ROJO
    severidad VARCHAR(10) NOT NULL,

    -- Descripción detallada
    descripcion TEXT NOT NULL,

    -- Acción del sistema (bloqueo / solo aviso / etc.)
    accion_sistema TEXT,

    -- Auditoría
    fecha_creacion TIMESTAMP NOT NULL DEFAULT now(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT now(),
    usuario_creacion BIGINT NULL,
    usuario_actualizacion BIGINT NULL
);

-- ============================================
-- FOREIGN KEYS con ON DELETE SET NULL
-- Evitan traumas al borrar datos relacionados
-- ============================================

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

ALTER TABLE general.alertas
  ADD CONSTRAINT fk_alertas_usuario_crea
  FOREIGN KEY (usuario_creacion)
  REFERENCES seguridad.usuarios(id_usuario)
  ON DELETE SET NULL;

ALTER TABLE general.alertas
  ADD CONSTRAINT fk_alertas_usuario_actualiza
  FOREIGN KEY (usuario_actualizacion)
  REFERENCES seguridad.usuarios(id_usuario)
  ON DELETE SET NULL;

-- ============================================
-- Índices
-- ============================================

CREATE INDEX IF NOT EXISTS idx_alertas_persona
  ON general.alertas(id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_alertas_modulo
  ON general.alertas(codigo_modulo);

CREATE INDEX IF NOT EXISTS idx_alertas_severidad
  ON general.alertas(severidad);

CREATE INDEX IF NOT EXISTS idx_alertas_fecha
  ON general.alertas(fecha_creacion);



-- ============================================
-- 2. Tabla de configuración: general.reglas_sarlaft
-- Todas las reglas parametrizables por el Oficial
-- ============================================

CREATE TABLE IF NOT EXISTS general.reglas_sarlaft (
    id_regla BIGSERIAL PRIMARY KEY,

    -- Ej: "EDAD_MENOR", "DIAS_MAX_ACTUALIZACION", "ACCION_ROJA"
    codigo VARCHAR(50) NOT NULL UNIQUE,

    descripcion TEXT NOT NULL,

    -- Valor numérico de referencia (años, días, edades, límites, etc.)
    valor_numerico NUMERIC NULL,

    -- Valor de texto para reglas especiales
    valor_texto VARCHAR(200) NULL,

    -- Severidad asignada por el Oficial
    severidad VARCHAR(10) NOT NULL DEFAULT 'VERDE',

    -- Módulo al que aplica (puede ser NULL si es global)
    codigo_modulo VARCHAR(3) NULL,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL DEFAULT now(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT now(),
    usuario_creacion BIGINT NULL,
    usuario_actualizacion BIGINT NULL
);

CREATE INDEX IF NOT EXISTS idx_reglas_codigo
  ON general.reglas_sarlaft(codigo);

CREATE INDEX IF NOT EXISTS idx_reglas_modulo
  ON general.reglas_sarlaft(codigo_modulo);



-- ============================================
-- 3. Tabla de gestión oficial: general.alertas_gestion
-- Registro de lo que se hizo con la alerta
-- ============================================

CREATE TABLE IF NOT EXISTS general.alertas_gestion (
    id_gestion BIGSERIAL PRIMARY KEY,

    id_alerta BIGINT NOT NULL,

    -- Acción tomada por el Oficial (texto libre)
    comentario TEXT NOT NULL,

    fecha TIMESTAMP NOT NULL DEFAULT now(),
    usuario BIGINT NULL
);

ALTER TABLE general.alertas_gestion
  ADD CONSTRAINT fk_gestion_alerta
  FOREIGN KEY (id_alerta)
  REFERENCES general.alertas(id_alerta)
  ON DELETE CASCADE;

ALTER TABLE general.alertas_gestion
  ADD CONSTRAINT fk_gestion_usuario
  FOREIGN KEY (usuario)
  REFERENCES seguridad.usuarios(id_usuario)
  ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_gestion_alerta
  ON general.alertas_gestion(id_alerta);


-- ============================================
-- FIN DEL ARCHIVO V014
-- ============================================
