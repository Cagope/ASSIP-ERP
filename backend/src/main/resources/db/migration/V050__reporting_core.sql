/* =====================================================================================
   🚀 MIGRACIÓN: V050__reporting_core.sql
   -------------------------------------------------------------------------------------
   📦 Módulo: REPORTING (Consultas dinámicas y metadatos)
   🧩 Proyecto: ERP ASSIP SOLIDARIA Y FINANCIERA
   📅 Fecha: 2025-11-02
   👨‍💻 Autor: Equipo de Tecnología ASSIP-ERP

   Descripción general:
   --------------------
   Crea la infraestructura base del motor de reportes dinámicos (Reporting Engine)
   para el sistema ASSIP-ERP. Permite generar consultas seguras sobre vistas SQL
   registradas, con auditoría completa y metadatos centralizados.

   Objetivos principales:
   -----------------------
   1️⃣ Centralizar la definición de reportes del sistema.
   2️⃣ Registrar cada consulta ejecutada (auditoría de uso).
   3️⃣ Permitir la extensión modular mediante vistas en otros esquemas.
   4️⃣ Ofrecer acceso seguro de solo lectura mediante roles dedicados.

   ===================================================================================== */


-- =====================================================================
-- 🧱 1. CREACIÓN DEL ESQUEMA
-- =====================================================================
CREATE SCHEMA IF NOT EXISTS reporting;

COMMENT ON SCHEMA reporting IS
'Esquema central del motor de reportes dinámicos del ERP ASSIP-ERP.';


-- =====================================================================
-- 📄 2. TABLA: reporting_metadata
-- ---------------------------------------------------------------------
-- Define los metadatos de cada reporte disponible en el sistema.
-- Se enlaza con vistas o tablas existentes en otros esquemas.
-- =====================================================================
CREATE TABLE IF NOT EXISTS reporting.reporting_metadata (
    id SERIAL PRIMARY KEY,
    schema_name     VARCHAR(50)   NOT NULL,         -- Nombre del esquema (ej. hoja_vida, contabilidad)
    view_name       VARCHAR(100)  NOT NULL,         -- Nombre de la vista o tabla asociada
    descripcion     TEXT,                           -- Descripción del reporte
    columnas        JSONB,                          -- Definición opcional de columnas visibles
    filtros         JSONB,                          -- Filtros base predefinidos (opcional)
    activo          BOOLEAN DEFAULT TRUE,           -- Permite activar/desactivar reportes
    fecha_creacion  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_creacion VARCHAR(100) DEFAULT CURRENT_USER
);

COMMENT ON TABLE reporting.reporting_metadata IS
'Tabla de metadatos que define las vistas disponibles para el motor de reporting.';

COMMENT ON COLUMN reporting.reporting_metadata.schema_name IS 'Nombre del esquema de origen del reporte.';
COMMENT ON COLUMN reporting.reporting_metadata.view_name IS 'Nombre de la vista SQL o tabla referenciada.';
COMMENT ON COLUMN reporting.reporting_metadata.columnas IS 'Definición JSON de columnas visibles o alias.';
COMMENT ON COLUMN reporting.reporting_metadata.filtros IS 'Definición JSON de filtros base.';


-- =====================================================================
-- 🧾 3. TABLA: reporting_logs
-- ---------------------------------------------------------------------
-- Registra todas las ejecuciones del módulo de reporting, permitiendo
-- trazabilidad completa y análisis de uso.
-- =====================================================================
CREATE TABLE IF NOT EXISTS reporting.reporting_logs (
    id SERIAL PRIMARY KEY,
    usuario         VARCHAR(100) NOT NULL,          -- Usuario que ejecutó el reporte
    fecha           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    schema_name     VARCHAR(50)   NOT NULL,         -- Esquema consultado
    view_name       VARCHAR(100)  NOT NULL,         -- Vista o recurso consultado
    parametros      JSONB,                          -- Parámetros y filtros enviados
    total_registros INTEGER,                        -- Total de filas devueltas
    duracion_ms     INTEGER,                        -- Duración estimada (ms)
    ip_origen       VARCHAR(100),                   -- IP del cliente o API consumer
    observacion     TEXT                            -- Observaciones adicionales
);

COMMENT ON TABLE reporting.reporting_logs IS
'Histórico de consultas ejecutadas en el módulo de reporting ASSIP-ERP.';

COMMENT ON COLUMN reporting.reporting_logs.parametros IS
'Filtros o parámetros enviados por el cliente en formato JSON.';


-- =====================================================================
-- 🔒 4. ÍNDICES Y RESTRICCIONES
-- ---------------------------------------------------------------------
-- Índices para optimizar búsqueda y consultas de auditoría.
-- =====================================================================
CREATE INDEX IF NOT EXISTS idx_reporting_metadata_schema
    ON reporting.reporting_metadata(schema_name, view_name);

CREATE INDEX IF NOT EXISTS idx_reporting_logs_fecha
    ON reporting.reporting_logs(fecha DESC);

CREATE INDEX IF NOT EXISTS idx_reporting_logs_usuario
    ON reporting.reporting_logs(usuario);


-- =====================================================================
-- 👤 5. ROLES Y SEGURIDAD
-- ---------------------------------------------------------------------
-- Se crean roles dedicados al esquema de reporting:
--   - reporting_admin : mantenimiento de metadatos
--   - reporting_reader: acceso solo lectura (frontend)
-- =====================================================================

-- 📌 5.1 Crear roles si no existen
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'reporting_admin') THEN
        CREATE ROLE reporting_admin LOGIN PASSWORD 'admin_reporting';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'reporting_reader') THEN
        CREATE ROLE reporting_reader LOGIN PASSWORD 'reader_reporting';
    END IF;
END $$;

-- 📌 5.2 Otorgar permisos a los roles
GRANT USAGE ON SCHEMA reporting TO reporting_admin, reporting_reader;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA reporting TO reporting_admin;
GRANT SELECT ON ALL TABLES IN SCHEMA reporting TO reporting_reader;

ALTER DEFAULT PRIVILEGES IN SCHEMA reporting
GRANT SELECT ON TABLES TO reporting_reader;

COMMENT ON ROLE reporting_admin IS
'Rol administrativo del esquema reporting (puede modificar metadatos).';

COMMENT ON ROLE reporting_reader IS
'Rol de solo lectura, usado por servicios frontend para consultas.';


-- =====================================================================
-- ⚙️ 6. PERMISOS FUTUROS (AUTO-GRANTS)
-- ---------------------------------------------------------------------
-- Garantiza que cualquier tabla o vista futura herede permisos de lectura.
-- =====================================================================
ALTER DEFAULT PRIVILEGES IN SCHEMA reporting
GRANT SELECT ON TABLES TO reporting_reader;


-- =====================================================================
-- 📦 7. DATOS INICIALES
-- ---------------------------------------------------------------------
-- El módulo arranca sin reportes cargados; cada esquema registrará los suyos.
-- =====================================================================
-- INSERT INTO reporting.reporting_metadata(schema_name, view_name, descripcion)
-- VALUES ('hoja_vida', 'vw_afiliados_por_genero', 'Afiliados agrupados por género');


-- =====================================================================
-- ✅ FIN DE MIGRACIÓN
-- =====================================================================
