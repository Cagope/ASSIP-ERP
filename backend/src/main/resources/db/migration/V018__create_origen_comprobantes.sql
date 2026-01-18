-- ============================================================
-- V018__create_origen_comprobantes.sql
-- ------------------------------------------------------------
-- Tabla para trazabilidad del ORIGEN de un comprobante contable
-- (automático o manual), sin contaminar auxiliares_contables.
-- ============================================================

CREATE TABLE IF NOT EXISTS contabilidad.origen_comprobantes (
    id_origen_comprobante       SERIAL PRIMARY KEY,

    -- Identificación del comprobante (llave lógica)
    id_agencia                  INTEGER NOT NULL,
    tipo_comprobante            CHAR(2) NOT NULL,
    numero_comprobante          CHAR(10) NOT NULL,

    -- Origen del comprobante
    origen_tipo                 VARCHAR(10) NOT NULL,   -- AUTO | MANUAL
    modulo_origen               VARCHAR(80) NOT NULL,   -- texto descriptivo (ej: ACTIVOS_FIJOS)
    proceso_origen              VARCHAR(80) NULL,       -- ej: DEPRECIACION, APERTURA_CUENTA
    tabla_origen                VARCHAR(120) NULL,      -- ej: activos_fijos.depreciacion_activos_control
    id_origen                   BIGINT NULL,            -- id del registro origen en la tabla (ej: id_control)

    -- Auditoría
    fk_seguridad_creacion       INTEGER NOT NULL,
    fecha_creacion              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion        INTEGER NOT NULL,
    fecha_edicion               TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Restricción: un comprobante debe tener UN origen (por agencia)
-- ============================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_origen_comprobantes_comprobante'
    ) THEN
        ALTER TABLE contabilidad.origen_comprobantes
        ADD CONSTRAINT uq_origen_comprobantes_comprobante
        UNIQUE (id_agencia, tipo_comprobante, numero_comprobante);
    END IF;
END $$;

-- ============================================================
-- Validación del tipo de origen
-- ============================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_origen_comprobantes_origen_tipo'
    ) THEN
        ALTER TABLE contabilidad.origen_comprobantes
        ADD CONSTRAINT ck_origen_comprobantes_origen_tipo
        CHECK (origen_tipo IN ('AUTO', 'MANUAL'));
    END IF;
END $$;

-- ============================================================
-- Índices útiles para búsquedas
-- ============================================================
CREATE INDEX IF NOT EXISTS ix_origen_comprobantes_modulo
    ON contabilidad.origen_comprobantes (modulo_origen);

CREATE INDEX IF NOT EXISTS ix_origen_comprobantes_proceso
    ON contabilidad.origen_comprobantes (proceso_origen);

CREATE INDEX IF NOT EXISTS ix_origen_comprobantes_tabla_id
    ON contabilidad.origen_comprobantes (tabla_origen, id_origen);
