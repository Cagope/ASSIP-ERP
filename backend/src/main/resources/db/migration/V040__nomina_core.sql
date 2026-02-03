-- ============================================================
-- V040__nomina_core.sql
-- ASSIP-ERP - Módulo Nómina (Colombia) + Salud (Hoja de Vida)
--
-- Auditoría estándar (según hoja_vida.datos_personales):
--   fk_seguridad_creacion, fecha_creacion
--   fk_seguridad_edicion,  fecha_edicion
--
-- Dependencias:
--  - general.datos_agencias
--  - hoja_vida.datos_personales (PK: id_datos_personal)
--  - catalogos.parentescos (PK: codigo_parentesco VARCHAR(2))
-- ============================================================

-- ============================================================
-- 0) ESQUEMA
-- ============================================================

CREATE SCHEMA IF NOT EXISTS nomina;

-- ============================================================
-- 1) HOJA DE VIDA - SALUD / EMERGENCIAS
-- ============================================================

CREATE TABLE IF NOT EXISTS hoja_vida.datos_personales_salud (

    -- PK = FK (relación 1 a 1 con la persona)
    id_datos_personal                     INTEGER PRIMARY KEY,

    -- Tipo de sangre
    tipo_sangre                           VARCHAR(5) NULL,

    -- Contacto de emergencia
    nombre_contacto_emergencia            VARCHAR(200) NULL,
    telefono_contacto_emergencia          VARCHAR(30)  NULL,

    -- Parentesco (catálogo)
    codigo_parentesco_contacto_emergencia VARCHAR(2)   NULL,

    -- Alertas básicas
    tiene_alergias                        BOOLEAN NOT NULL DEFAULT FALSE,
    alergias                              VARCHAR(300) NULL,

    tiene_restricciones_medicas           BOOLEAN NOT NULL DEFAULT FALSE,
    restricciones_medicas                 VARCHAR(300) NULL,

    -- Salud ocupacional
    fecha_examen_ingreso                  DATE NULL,
    fecha_proximo_examen                  DATE NULL,
    apto_laboral                          BOOLEAN NULL,
    observaciones_examen                  VARCHAR(400) NULL,

    -- Auditoría estándar
    fk_seguridad_creacion                 INTEGER NOT NULL,
    fecha_creacion                        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion                  INTEGER NOT NULL,
    fecha_edicion                         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_salud_datos_personales
        FOREIGN KEY (id_datos_personal)
        REFERENCES hoja_vida.datos_personales(id_datos_personal)
        ON DELETE CASCADE,

    CONSTRAINT fk_salud_parentesco
        FOREIGN KEY (codigo_parentesco_contacto_emergencia)
        REFERENCES catalogos.parentescos(codigo_parentesco)
);

CREATE INDEX IF NOT EXISTS idx_salud_tipo_sangre
    ON hoja_vida.datos_personales_salud(tipo_sangre);

CREATE INDEX IF NOT EXISTS idx_salud_proximo_examen
    ON hoja_vida.datos_personales_salud(fecha_proximo_examen);

-- ============================================================
-- 2) CATÁLOGOS NOMINA
-- ============================================================

-- 2.1 Secciones / grupos nómina
CREATE TABLE IF NOT EXISTS nomina.secciones_nomina (
    id_seccion            SERIAL PRIMARY KEY,
    codigo                VARCHAR(30) NOT NULL,
    nombre_seccion        VARCHAR(150) NOT NULL,
    activo                BOOLEAN NOT NULL DEFAULT TRUE,

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion  INTEGER NOT NULL,
    fecha_edicion         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_seccion_codigo UNIQUE (codigo),
    CONSTRAINT uq_seccion_nombre UNIQUE (nombre_seccion)
);

-- 2.2 Cargos
CREATE TABLE IF NOT EXISTS nomina.cargos (
    id_cargo              SERIAL PRIMARY KEY,
    nombre_cargo          VARCHAR(200) NOT NULL,
    activo                BOOLEAN NOT NULL DEFAULT TRUE,

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion  INTEGER NOT NULL,
    fecha_edicion         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_cargos_nombre UNIQUE (nombre_cargo)
);

-- 2.3 Tipos de contrato
CREATE TABLE IF NOT EXISTS nomina.tipos_contrato (
    id_tipo_contrato      SERIAL PRIMARY KEY,
    codigo                VARCHAR(30) NOT NULL,
    nombre                VARCHAR(120) NOT NULL,
    activo                BOOLEAN NOT NULL DEFAULT TRUE,

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion  INTEGER NOT NULL,
    fecha_edicion         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_tipo_contrato_codigo UNIQUE (codigo),
    CONSTRAINT uq_tipo_contrato_nombre UNIQUE (nombre)
);

-- ============================================================
-- 2.4 Entidades Seguridad Social (con tercero id_datos_personal)
-- ============================================================

CREATE TABLE IF NOT EXISTS nomina.entidades_eps (
    id_eps                SERIAL PRIMARY KEY,
    nombre_eps            VARCHAR(200) NOT NULL,
    id_datos_personal     INTEGER NOT NULL,
    activo                BOOLEAN NOT NULL DEFAULT TRUE,

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion  INTEGER NOT NULL,
    fecha_edicion         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_eps_nombre UNIQUE (nombre_eps),
    CONSTRAINT uq_eps_tercero UNIQUE (id_datos_personal),

    CONSTRAINT fk_eps_tercero
        FOREIGN KEY (id_datos_personal)
        REFERENCES hoja_vida.datos_personales(id_datos_personal)
);

CREATE TABLE IF NOT EXISTS nomina.entidades_afp (
    id_afp                SERIAL PRIMARY KEY,
    nombre_afp            VARCHAR(200) NOT NULL,
    id_datos_personal     INTEGER NOT NULL,
    activo                BOOLEAN NOT NULL DEFAULT TRUE,

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion  INTEGER NOT NULL,
    fecha_edicion         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_afp_nombre UNIQUE (nombre_afp),
    CONSTRAINT uq_afp_tercero UNIQUE (id_datos_personal),

    CONSTRAINT fk_afp_tercero
        FOREIGN KEY (id_datos_personal)
        REFERENCES hoja_vida.datos_personales(id_datos_personal)
);

CREATE TABLE IF NOT EXISTS nomina.entidades_cesantias (
    id_cesantias          SERIAL PRIMARY KEY,
    nombre_cesantias      VARCHAR(200) NOT NULL,
    id_datos_personal     INTEGER NOT NULL,
    activo                BOOLEAN NOT NULL DEFAULT TRUE,

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion  INTEGER NOT NULL,
    fecha_edicion         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_cesantias_nombre UNIQUE (nombre_cesantias),
    CONSTRAINT uq_cesantias_tercero UNIQUE (id_datos_personal),

    CONSTRAINT fk_cesantias_tercero
        FOREIGN KEY (id_datos_personal)
        REFERENCES hoja_vida.datos_personales(id_datos_personal)
);

CREATE TABLE IF NOT EXISTS nomina.entidades_arl (
    id_arl                SERIAL PRIMARY KEY,
    nombre_arl            VARCHAR(200) NOT NULL,
    id_datos_personal     INTEGER NOT NULL,
    activo                BOOLEAN NOT NULL DEFAULT TRUE,

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion  INTEGER NOT NULL,
    fecha_edicion         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_arl_nombre UNIQUE (nombre_arl),
    CONSTRAINT uq_arl_tercero UNIQUE (id_datos_personal),

    CONSTRAINT fk_arl_tercero
        FOREIGN KEY (id_datos_personal)
        REFERENCES hoja_vida.datos_personales(id_datos_personal)
);

CREATE TABLE IF NOT EXISTS nomina.entidades_caja_compensacion (
    id_caja               SERIAL PRIMARY KEY,
    nombre_caja           VARCHAR(200) NOT NULL,
    id_datos_personal     INTEGER NOT NULL,
    activo                BOOLEAN NOT NULL DEFAULT TRUE,

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion  INTEGER NOT NULL,
    fecha_edicion         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_caja_nombre UNIQUE (nombre_caja),
    CONSTRAINT uq_caja_tercero UNIQUE (id_datos_personal),

    CONSTRAINT fk_caja_tercero
        FOREIGN KEY (id_datos_personal)
        REFERENCES hoja_vida.datos_personales(id_datos_personal)
);

-- ============================================================
-- 3) CONCEPTOS NÓMINA
-- ============================================================

CREATE TABLE IF NOT EXISTS nomina.conceptos_nomina (
    codigo               VARCHAR(40) PRIMARY KEY,
    nombre               VARCHAR(150) NOT NULL,
    tipo                 VARCHAR(20) NOT NULL
        CHECK (tipo IN ('DEVENGADO', 'DEDUCCION', 'APORTE', 'PROVISION')),
    es_fijo              BOOLEAN NOT NULL DEFAULT FALSE,
    activo               BOOLEAN NOT NULL DEFAULT TRUE,

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion  INTEGER NOT NULL,
    fecha_edicion         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_conceptos_nomina_tipo
    ON nomina.conceptos_nomina(tipo);

-- ============================================================
-- 4) VARIABLES LEGALES POR VIGENCIA (rango)
-- ============================================================

CREATE TABLE IF NOT EXISTS nomina.variables_vigencia (
    id_variable               SERIAL PRIMARY KEY,

    fecha_inicial             DATE NOT NULL,
    fecha_final               DATE NOT NULL,

    smmlv                     NUMERIC(18,2) NOT NULL,
    aux_transporte            NUMERIC(18,2) NOT NULL,

    porc_salud_empleado       NUMERIC(10,6) NOT NULL,
    porc_salud_empleador      NUMERIC(10,6) NOT NULL,
    porc_pension_empleado     NUMERIC(10,6) NOT NULL,
    porc_pension_empleador    NUMERIC(10,6) NOT NULL,

    porc_caja_compensacion    NUMERIC(10,6) NOT NULL,
    porc_sena                 NUMERIC(10,6) NOT NULL,
    porc_icbf                 NUMERIC(10,6) NOT NULL,

    por_provision_prima              NUMERIC(10,6) NOT NULL,
    por_provision_vacaciones         NUMERIC(10,6) NOT NULL,
    por_provision_cesantias          NUMERIC(10,6) NOT NULL,
    por_provision_interes_cesantias  NUMERIC(10,6) NOT NULL,

    tope_ibc_min_smmlv        NUMERIC(10,2) NOT NULL DEFAULT 1,
    tope_ibc_max_smmlv        NUMERIC(10,2) NOT NULL DEFAULT 25,

    exonerado_salud           BOOLEAN NOT NULL DEFAULT FALSE,
    exonerado_parafiscales    BOOLEAN NOT NULL DEFAULT FALSE,

    activo                    BOOLEAN NOT NULL DEFAULT TRUE,

    fk_seguridad_creacion     INTEGER NOT NULL,
    fecha_creacion            TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion      INTEGER NOT NULL,
    fecha_edicion             TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_variables_rango UNIQUE (fecha_inicial, fecha_final),
    CONSTRAINT ck_variables_fechas CHECK (fecha_final >= fecha_inicial)
);

-- ============================================================
-- 5) EMPLEADOS (Agencia + Persona)
-- ============================================================

CREATE TABLE IF NOT EXISTS nomina.empleados (
    id_empleado            SERIAL PRIMARY KEY,
    id_agencia             INTEGER NOT NULL,
    id_datos_personal      INTEGER NOT NULL,
    activo                 BOOLEAN NOT NULL DEFAULT TRUE,

    fk_seguridad_creacion  INTEGER NOT NULL,
    fecha_creacion         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion   INTEGER NOT NULL,
    fecha_edicion          TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_empleados_agencia
        FOREIGN KEY (id_agencia)
        REFERENCES general.datos_agencias(id_agencia),

    CONSTRAINT fk_empleados_persona
        FOREIGN KEY (id_datos_personal)
        REFERENCES hoja_vida.datos_personales(id_datos_personal),

    CONSTRAINT uq_empleados_agencia_persona
        UNIQUE (id_agencia, id_datos_personal)
);

-- ============================================================
-- 6) CONTRATOS
-- ============================================================

CREATE TABLE IF NOT EXISTS nomina.empleado_contratos (
    id_contrato                 SERIAL PRIMARY KEY,
    id_empleado                 INTEGER NOT NULL,

    id_seccion                  INTEGER NULL,

    fecha_inicio                DATE NOT NULL,
    fecha_fin                   DATE NULL,

    id_tipo_contrato            INTEGER NOT NULL,
    id_cargo                    INTEGER NULL,

    salario_base                NUMERIC(18,2) NOT NULL DEFAULT 0,
    salario_integral            BOOLEAN NOT NULL DEFAULT FALSE,

    periodo_pago                VARCHAR(15) NOT NULL DEFAULT 'MENSUAL'
        CHECK (periodo_pago IN ('MENSUAL', 'QUINCENAL')),

    id_eps                      INTEGER NULL,
    id_afp                      INTEGER NULL,
    id_cesantias                INTEGER NULL,
    id_arl                      INTEGER NULL,
    id_caja_compensacion        INTEGER NULL,

    cuenta_nomina_display       VARCHAR(30) NULL,
    id_cuenta_ahorro_nomina     BIGINT NULL,

    fecha_envio_nota_renovacion DATE NULL,

    clase_riesgo_arl            SMALLINT NOT NULL DEFAULT 1,
    porcentaje_arl              NUMERIC(10,6) NOT NULL DEFAULT 0,

    activo                      BOOLEAN NOT NULL DEFAULT TRUE,

    fk_seguridad_creacion       INTEGER NOT NULL,
    fecha_creacion              TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion        INTEGER NOT NULL,
    fecha_edicion               TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_contratos_empleado
        FOREIGN KEY (id_empleado)
        REFERENCES nomina.empleados(id_empleado),

    CONSTRAINT fk_contratos_seccion
        FOREIGN KEY (id_seccion)
        REFERENCES nomina.secciones_nomina(id_seccion),

    CONSTRAINT fk_contratos_tipo
        FOREIGN KEY (id_tipo_contrato)
        REFERENCES nomina.tipos_contrato(id_tipo_contrato),

    CONSTRAINT fk_contratos_cargo
        FOREIGN KEY (id_cargo)
        REFERENCES nomina.cargos(id_cargo),

    CONSTRAINT fk_contratos_eps
        FOREIGN KEY (id_eps)
        REFERENCES nomina.entidades_eps(id_eps),

    CONSTRAINT fk_contratos_afp
        FOREIGN KEY (id_afp)
        REFERENCES nomina.entidades_afp(id_afp),

    CONSTRAINT fk_contratos_cesantias
        FOREIGN KEY (id_cesantias)
        REFERENCES nomina.entidades_cesantias(id_cesantias),

    CONSTRAINT fk_contratos_arl
        FOREIGN KEY (id_arl)
        REFERENCES nomina.entidades_arl(id_arl),

    CONSTRAINT fk_contratos_caja
        FOREIGN KEY (id_caja_compensacion)
        REFERENCES nomina.entidades_caja_compensacion(id_caja),

    CONSTRAINT ck_contratos_riesgo
        CHECK (clase_riesgo_arl BETWEEN 1 AND 5),

    CONSTRAINT ck_contratos_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

-- ============================================================
-- 7) PERIODOS NÓMINA
-- ============================================================

CREATE TABLE IF NOT EXISTS nomina.periodos_nomina (
    id_periodo            SERIAL PRIMARY KEY,

    id_agencia            INTEGER NOT NULL,
    anio                  INTEGER NOT NULL,
    mes                   INTEGER NOT NULL CHECK (mes BETWEEN 1 AND 12),

    tipo_periodo          VARCHAR(15) NOT NULL DEFAULT 'MENSUAL'
        CHECK (tipo_periodo IN ('MENSUAL', 'QUINCENAL')),

    fecha_inicio          DATE NOT NULL,
    fecha_fin             DATE NOT NULL,

    estado                VARCHAR(20) NOT NULL DEFAULT 'ABIERTO'
        CHECK (estado IN ('ABIERTO', 'LIQUIDADO', 'CONTABILIZADO', 'CERRADO')),

    fecha_liquidacion     TIMESTAMP NULL,
    fk_seguridad_liquidacion INTEGER NULL,

    fecha_contabiliza     TIMESTAMP NULL,
    fk_seguridad_contabiliza INTEGER NULL,

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion  INTEGER NOT NULL,
    fecha_edicion         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_periodos_agencia
        FOREIGN KEY (id_agencia)
        REFERENCES general.datos_agencias(id_agencia),

    CONSTRAINT uq_periodo_agencia_rango
        UNIQUE (id_agencia, fecha_inicio, fecha_fin),

    CONSTRAINT ck_periodo_fechas
        CHECK (fecha_fin >= fecha_inicio)
);

-- ============================================================
-- 8) NOVEDADES
-- ============================================================

CREATE TABLE IF NOT EXISTS nomina.novedades_nomina (
    id_novedad            SERIAL PRIMARY KEY,

    id_periodo            INTEGER NOT NULL,
    id_empleado           INTEGER NOT NULL,

    codigo_concepto       VARCHAR(40) NOT NULL,

    fecha_inicial         DATE NOT NULL,
    fecha_final           DATE NOT NULL,

    cantidad              NUMERIC(18,4) NOT NULL DEFAULT 0,
    valor                 NUMERIC(18,2) NOT NULL DEFAULT 0,

    observacion           VARCHAR(400),

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion  INTEGER NOT NULL,
    fecha_edicion         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_novedades_periodo
        FOREIGN KEY (id_periodo)
        REFERENCES nomina.periodos_nomina(id_periodo),

    CONSTRAINT fk_novedades_empleado
        FOREIGN KEY (id_empleado)
        REFERENCES nomina.empleados(id_empleado),

    CONSTRAINT fk_novedades_concepto
        FOREIGN KEY (codigo_concepto)
        REFERENCES nomina.conceptos_nomina(codigo),

    CONSTRAINT ck_novedades_fechas
        CHECK (fecha_final >= fecha_inicial)
);

-- ============================================================
-- 9) LIQUIDACIONES
-- ============================================================

CREATE TABLE IF NOT EXISTS nomina.liquidaciones (
    id_liquidacion        SERIAL PRIMARY KEY,

    id_periodo            INTEGER NOT NULL,
    id_empleado           INTEGER NOT NULL,

    ibc                   NUMERIC(18,2) NOT NULL DEFAULT 0,

    total_devengado       NUMERIC(18,2) NOT NULL DEFAULT 0,
    total_deducciones     NUMERIC(18,2) NOT NULL DEFAULT 0,
    neto_pagar            NUMERIC(18,2) NOT NULL DEFAULT 0,

    estado                VARCHAR(20) NOT NULL DEFAULT 'EJECUTADO'
        CHECK (estado IN ('PREVIEW', 'EJECUTADO')),

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_liq_periodo
        FOREIGN KEY (id_periodo)
        REFERENCES nomina.periodos_nomina(id_periodo),

    CONSTRAINT fk_liq_empleado
        FOREIGN KEY (id_empleado)
        REFERENCES nomina.empleados(id_empleado),

    CONSTRAINT uq_liq_periodo_empleado
        UNIQUE (id_periodo, id_empleado)
);

-- ============================================================
-- 10) LIQUIDACIÓN DETALLE
-- ============================================================

CREATE TABLE IF NOT EXISTS nomina.liquidacion_detalle (
    id_detalle           SERIAL PRIMARY KEY,

    id_liquidacion       INTEGER NOT NULL,
    codigo_concepto      VARCHAR(40) NOT NULL,

    tipo                 VARCHAR(20) NOT NULL
        CHECK (tipo IN ('DEVENGADO', 'DEDUCCION', 'APORTE', 'PROVISION')),

    base                 NUMERIC(18,2) NOT NULL DEFAULT 0,
    cantidad             NUMERIC(18,4) NOT NULL DEFAULT 0,
    tarifa               NUMERIC(10,6) NOT NULL DEFAULT 0,
    valor                NUMERIC(18,2) NOT NULL DEFAULT 0,

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_detalle_liquidacion
        FOREIGN KEY (id_liquidacion)
        REFERENCES nomina.liquidaciones(id_liquidacion)
        ON DELETE CASCADE,

    CONSTRAINT fk_detalle_concepto
        FOREIGN KEY (codigo_concepto)
        REFERENCES nomina.conceptos_nomina(codigo)
);

-- ============================================================
-- 11) MAPEO CONTABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS nomina.concepto_cuentas_contables (
    id_mapeo             SERIAL PRIMARY KEY,

    codigo_concepto      VARCHAR(40) NOT NULL,
    id_agencia           INTEGER NOT NULL,

    id_cuenta_debito     INTEGER NOT NULL,
    id_cuenta_credito    INTEGER NOT NULL,

    activo               BOOLEAN NOT NULL DEFAULT TRUE,

    fk_seguridad_creacion INTEGER NOT NULL,
    fecha_creacion        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion  INTEGER NOT NULL,
    fecha_edicion         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cc_concepto
        FOREIGN KEY (codigo_concepto)
        REFERENCES nomina.conceptos_nomina(codigo),

    CONSTRAINT fk_cc_agencia
        FOREIGN KEY (id_agencia)
        REFERENCES general.datos_agencias(id_agencia),

    CONSTRAINT uq_cc_agencia_concepto
        UNIQUE (id_agencia, codigo_concepto)
);
