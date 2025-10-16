-- =========================================================
-- Proyecto : ASSIP_ERP
-- Migración: V001__seguridad_core.sql
-- Objetivo : Crear esquema SEGURIDAD y tablas base:
--            usuarios, roles, permisos, roles_permisos,
--            usuarios_agencias, sesiones, log_eventos.
--            Índices y restricciones clave.
-- Nota     : Sin datos semilla (ver V002__seguridad_seed.sql)
-- =========================================================

-- 1) Esquema y search_path
CREATE SCHEMA IF NOT EXISTS seguridad;
SET search_path TO seguridad, public;

-- 2) Tipos auxiliares (si se requieren en el futuro, por ahora no)
-- (Reservado para enums de seguridad)

-- 3) Tabla: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario              BIGSERIAL PRIMARY KEY,
    username                VARCHAR(64) NOT NULL, -- minúsculas, único, inmutable
    password_hash           TEXT NOT NULL,
    password_actualizada_en TIMESTAMPTZ,
    debe_cambiar_password   BOOLEAN NOT NULL DEFAULT TRUE,

    is_superuser_seguridad  BOOLEAN NOT NULL DEFAULT FALSE, -- bypass SOLO en seguridad
    activo                  BOOLEAN NOT NULL DEFAULT TRUE,

    intentos_fallidos       INTEGER NOT NULL DEFAULT 0,
    bloqueado_hasta         TIMESTAMPTZ,

    mfa_habilitado          BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret              TEXT,        -- almacenar cifrado si se usa
    mfa_activado_en         TIMESTAMPTZ,
    mfa_recovery_hashes     JSONB,       -- lista de hashes (y metadatos) de códigos de recuperación

    ultimo_login_en         TIMESTAMPTZ,
    ultimo_ip               INET,
    ultimo_user_agent       TEXT,

    creado_en               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    creado_por              VARCHAR(64),
    actualizado_en          TIMESTAMPTZ,
    actualizado_por         VARCHAR(64),

    CONSTRAINT uq_usuarios_username UNIQUE (username)
);
COMMENT ON TABLE usuarios IS 'Usuarios del esquema de seguridad (login por username).';
COMMENT ON COLUMN usuarios.username IS 'Identificador único e inmutable (minúsculas).';

-- 4) Tabla: roles
CREATE TABLE IF NOT EXISTS roles (
    id_rol       BIGSERIAL PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  TEXT,
    activo       BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    creado_por   VARCHAR(64),
    actualizado_en TIMESTAMPTZ,
    actualizado_por VARCHAR(64),

    CONSTRAINT uq_roles_nombre UNIQUE (nombre)
);
COMMENT ON TABLE roles IS 'Roles/perfiles del esquema seguridad (mapearán a permisos).';

-- 5) Tabla: permisos (catálogo granular)
CREATE TABLE IF NOT EXISTS permisos (
    id_permiso   BIGSERIAL PRIMARY KEY,
    codigo       VARCHAR(150) NOT NULL, -- ej: seg.usuarios.create
    descripcion  TEXT,
    dominio      VARCHAR(50),           -- ej: seg, hv, contab, central
    creado_en    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    creado_por   VARCHAR(64),

    CONSTRAINT uq_permisos_codigo UNIQUE (codigo)
);
COMMENT ON TABLE permisos IS 'Permisos granulares (dominio.recurso.accion).';

-- 6) Tabla puente: roles_permisos (N..N)
CREATE TABLE IF NOT EXISTS roles_permisos (
    id_rol      BIGINT NOT NULL REFERENCES roles(id_rol) ON UPDATE RESTRICT ON DELETE CASCADE,
    id_permiso  BIGINT NOT NULL REFERENCES permisos(id_permiso) ON UPDATE RESTRICT ON DELETE CASCADE,

    PRIMARY KEY (id_rol, id_permiso)
);
COMMENT ON TABLE roles_permisos IS 'Asignación de permisos a roles.';

-- 7) Tabla: usuarios_agencias (rol único por agencia + agencia por defecto)
--     NOTA: id_agencia referencia un catálogo en otro esquema (general/agencias).
--     Aquí lo dejamos como BIGINT sin FK (se agregará cuando exista la tabla de agencias).
CREATE TABLE IF NOT EXISTS usuarios_agencias (
    id_usuario      BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON UPDATE RESTRICT ON DELETE CASCADE,
    id_agencia      BIGINT NOT NULL, -- FK diferida a futuro (general.agencias)
    id_rol          BIGINT NOT NULL REFERENCES roles(id_rol) ON UPDATE RESTRICT ON DELETE RESTRICT,

    es_por_defecto  BOOLEAN NOT NULL DEFAULT FALSE,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_asignacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (id_usuario, id_agencia)
);
COMMENT ON TABLE usuarios_agencias IS 'Asignación de usuario a agencia con rol único y marca de por defecto.';

-- Restricción: solo una agencia por defecto por usuario (índice único parcial)
CREATE UNIQUE INDEX IF NOT EXISTS uq_usuario_default_agencia
    ON usuarios_agencias (id_usuario)
    WHERE es_por_defecto = TRUE;

-- 8) Tabla: sesiones (control de refresh/revocación)
CREATE TABLE IF NOT EXISTS sesiones (
    session_id     UUID PRIMARY KEY,
    id_usuario     BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON UPDATE RESTRICT ON DELETE CASCADE,
    creado_en      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expira_en      TIMESTAMPTZ,
    ultimo_uso_en  TIMESTAMPTZ,
    ultimo_ip      INET,
    ultimo_user_agent TEXT,
    revocada       BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_revocacion TEXT
);
COMMENT ON TABLE sesiones IS 'Control de sesiones (refresh tokens / revocación).';

-- 9) Tabla: log_eventos (auditoría SOLO de seguridad; auditoría general irá en otro esquema)
CREATE TABLE IF NOT EXISTS log_eventos (
    id_evento      BIGSERIAL PRIMARY KEY,
    timestamp_tz   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    request_id     UUID,
    session_id     UUID,                 -- puede ser NULL si no aplica
    user_id        BIGINT,               -- actor (puede ser NULL si fallo antes de resolver usuario)
    username       VARCHAR(64),
    agency_scope   BIGINT,               -- contexto de agencia si aplica
    ip             INET,
    user_agent     TEXT,

    modulo         VARCHAR(50) NOT NULL DEFAULT 'seguridad',
    entidad        VARCHAR(50),          -- usuario, rol, permiso, usuario_agencia, sesion, etc.
    accion         VARCHAR(80) NOT NULL, -- ej: SEG.SESION.LOGIN_OK
    resultado      VARCHAR(8)  NOT NULL, -- OK / FAIL
    status_code    INTEGER,              -- HTTP si aplica
    latencia_ms    INTEGER,

    motivo         TEXT,                 -- texto corto causa/razón
    extra_json     JSONB,                -- payload adicional
    antes_json     JSONB,                -- cambios (enmascarado)
    despues_json   JSONB,                -- cambios (enmascarado)
    hash_integridad TEXT                 -- HMAC de campos clave (se calcula en app, si aplica)
);
COMMENT ON TABLE log_eventos IS 'Bitácora de eventos del esquema seguridad.';

-- Índices de consulta rápida (por partición lógica mensual en una futura migración si se requiere)
CREATE INDEX IF NOT EXISTS idx_log_eventos_ts        ON log_eventos (timestamp_tz);
CREATE INDEX IF NOT EXISTS idx_log_eventos_username  ON log_eventos (username, timestamp_tz);
CREATE INDEX IF NOT EXISTS idx_log_eventos_accion    ON log_eventos (accion, timestamp_tz);
CREATE INDEX IF NOT EXISTS idx_log_eventos_agency    ON log_eventos (agency_scope, timestamp_tz);
CREATE INDEX IF NOT EXISTS idx_log_eventos_session   ON log_eventos (session_id);

-- 10) Comentarios y buenas prácticas
COMMENT ON COLUMN usuarios.mfa_secret IS 'Guardar cifrado. No exponer en vistas.';
COMMENT ON COLUMN log_eventos.antes_json IS 'Datos previos (PII enmascarada).';
COMMENT ON COLUMN log_eventos.despues_json IS 'Datos posteriores (PII enmascarada).';
