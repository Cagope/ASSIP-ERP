-- ===========================================================
-- ERP ASSIP SOLIDARIA Y FINANCIERA
-- MIGRACIÓN V011 — CONTABILIDAD CORE
-- Fecha: 2025-11-04
-- ===========================================================

SET search_path TO contabilidad;

-- ===========================================================
-- 1️⃣ TIPOS ESPECIALES DE CUENTAS
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.tipos_especiales_cuentas (
    id_tipo_especial_cuenta INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    descripcion VARCHAR(50) NOT NULL,
    porcentaje DECIMAL(18,2) DEFAULT 0,
    lleva_base BOOLEAN DEFAULT FALSE,
    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================================
-- 2️⃣ CATÁLOGO DE CUENTAS
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.catalogo_cuentas (
    id_catalogo_cuenta INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_agencia INTEGER REFERENCES general.datos_agencias(id_agencia),
    codigo_cuenta CHAR(11) NOT NULL CHECK (LENGTH(codigo_cuenta) IN (1,2,4,6,11)),
    nombre_cuenta VARCHAR(100) NOT NULL,
    naturaleza_cuenta CHAR(1) CHECK (naturaleza_cuenta IN ('D','C')),
    nivel_cuenta INTEGER NOT NULL,
    cuenta_operable BOOLEAN DEFAULT FALSE,
    id_tipo_especial_cuenta INTEGER REFERENCES contabilidad.tipos_especiales_cuentas(id_tipo_especial_cuenta),
    control_entrada_salida BOOLEAN DEFAULT FALSE,
    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_catalogo_cuentas UNIQUE (id_agencia, codigo_cuenta)
);

-- ===========================================================
-- 3️⃣ TIPOS DE COMPROBANTES
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.tipos_comprobantes (
    tipo_comprobante CHAR(2) PRIMARY KEY,
    id_agencia INTEGER REFERENCES general.datos_agencias(id_agencia),
    nombre_tipo_comprobante VARCHAR(50) NOT NULL,
    csc_comprobante INTEGER DEFAULT 0,
    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================================
-- 4️⃣ ESTADOS DE MOVIMIENTOS (SOLO LECTURA)
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.estados_movimientos (
    codigo_estado_movimiento CHAR(1) PRIMARY KEY,
    descripcion_estado_movimiento VARCHAR(50),
    suma BOOLEAN DEFAULT TRUE
);

-- ===========================================================
-- 5️⃣ AUXILIARES CONTABLES
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.auxiliares_contables (
    id_auxiliar_contable INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_catalogo_cuenta INTEGER NOT NULL REFERENCES contabilidad.catalogo_cuentas(id_catalogo_cuenta),
    id_agencia INTEGER NOT NULL REFERENCES general.datos_agencias(id_agencia),
    id_datos_personal INTEGER REFERENCES hoja_vida.datos_personales(id_datos_personal),
    fecha_auxiliar DATE NOT NULL,
    tipo_comprobante CHAR(2) REFERENCES contabilidad.tipos_comprobantes(tipo_comprobante),
    numero_comprobante CHAR(10),
    detalle_movimiento VARCHAR(100),
    estado_movimiento CHAR(1) REFERENCES contabilidad.estados_movimientos(codigo_estado_movimiento),
    valor_debito DECIMAL(18,2) DEFAULT 0,
    valor_credito DECIMAL(18,2) DEFAULT 0,
    valor_base DECIMAL(18,2) DEFAULT 0,
    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================================
-- 6️⃣ TIPOS DE CONTROL DE ENTRADA/SALIDA
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.tipos_control_ent_sal (
    id_tipos_control INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    descripcion VARCHAR(50) NOT NULL,
    pide_documento BOOLEAN DEFAULT FALSE,
    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================================
-- 7️⃣ CONTROLES AUXILIARES
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.controles_auxiliares (
    id_controles_auxiliares INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_tipos_control INTEGER REFERENCES contabilidad.tipos_control_ent_sal(id_tipos_control),
    id_auxiliar_contable INTEGER REFERENCES contabilidad.auxiliares_contables(id_auxiliar_contable),
    numero_documento VARCHAR(20),
    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================================
-- 8️⃣ CONCEPTOS CONTABLES
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.conceptos_contables (
    id_conceptos_contables INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo_comprobante CHAR(2) REFERENCES contabilidad.tipos_comprobantes(tipo_comprobante),
    numero_comprobante CHAR(10),
    concepto_comprobante VARCHAR(250) NOT NULL,
    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_conceptos UNIQUE (tipo_comprobante, numero_comprobante)
);

-- ===========================================================
-- 9️⃣ SALDOS DE TERCEROS
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.saldos_terceros (
    id_saldo_tercero INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_agencia INTEGER REFERENCES general.datos_agencias(id_agencia),
    id_catalogo_cuenta INTEGER REFERENCES contabilidad.catalogo_cuentas(id_catalogo_cuenta),
    id_datos_personal INTEGER REFERENCES hoja_vida.datos_personales(id_datos_personal),
    valor_debito DECIMAL(18,2) DEFAULT 0,
    valor_credito DECIMAL(18,2) DEFAULT 0,
    valor_saldo DECIMAL(18,2) DEFAULT 0,
    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================================
-- 🔟 MESES CERRADOS
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.meses_cerrados (
    id_agencia INTEGER REFERENCES general.datos_agencias(id_agencia),
    ano INTEGER NOT NULL CHECK (ano >= 2000),
    mes INTEGER NOT NULL CHECK (mes BETWEEN 1 AND 12),
    estado CHAR(1) NOT NULL CHECK (estado IN ('A','C')),
    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_agencia, ano, mes)
);

-- ===========================================================
-- 11️⃣ AÑOS CERRADOS
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.anos_cerrados (
    id_agencia INTEGER REFERENCES general.datos_agencias(id_agencia),
    ano INTEGER NOT NULL CHECK (ano >= 2000),
    estado CHAR(1) NOT NULL CHECK (estado IN ('A','C')),
    tipo_comprobante CHAR(2) REFERENCES contabilidad.tipos_comprobantes(tipo_comprobante),
    numero_comprobante CHAR(10),
    observaciones TEXT,
    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_agencia, ano)
);

-- ===========================================================
-- 12️⃣ CIERRES MENSUALES
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.cierres_mensuales (
    id_cierre_mensual INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_agencia INTEGER REFERENCES general.datos_agencias(id_agencia),
    id_catalogo_cuenta INTEGER REFERENCES contabilidad.catalogo_cuentas(id_catalogo_cuenta),
    ano INTEGER,
    mes INTEGER,
    valor_debito DECIMAL(18,2) DEFAULT 0,
    valor_credito DECIMAL(18,2) DEFAULT 0,
    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================================
-- 13️⃣ CIERRES ANUALES
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.cierres_anuales (
    id_cierre_anual INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_agencia INTEGER REFERENCES general.datos_agencias(id_agencia),
    id_catalogo_cuenta INTEGER REFERENCES contabilidad.catalogo_cuentas(id_catalogo_cuenta),
    ano INTEGER,
    valor_debito DECIMAL(18,2) DEFAULT 0,
    valor_credito DECIMAL(18,2) DEFAULT 0,
    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================================
-- 14️⃣ FIRMAS CONTABLES
-- ===========================================================
CREATE TABLE IF NOT EXISTS contabilidad.firmas_contables (
    id_agencia INTEGER REFERENCES general.datos_agencias(id_agencia),
    titulo_1 VARCHAR(100),
    nombre_titulo_1 VARCHAR(100),
    tarjeta_titulo_1 CHAR(20),
    titulo_2 VARCHAR(100),
    nombre_titulo_2 VARCHAR(100),
    tarjeta_titulo_2 CHAR(20),
    titulo_3 VARCHAR(100),
    nombre_titulo_3 VARCHAR(100),
    tarjeta_titulo_3 CHAR(20),
    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_agencia)
);
