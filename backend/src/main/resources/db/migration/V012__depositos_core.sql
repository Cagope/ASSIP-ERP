/*---------------------------------------------------------------------
 📘 Migración: V012__depositos_core.sql
 Esquema: depositos
 Autor: Carlos González Pérez — ERP ASSIP SOLIDARIA Y FINANCIERA
 Fecha: 2025-11-06
 ----------------------------------------------------------------------
 🔹 Descripción:
   Define toda la estructura del módulo DEPÓSITOS, incluyendo:
   - Catálogos base de tipos de captaciones, GMF, acciones, estados, etc.
   - Tablas operativas con CRUD (formas, cuentas, extractos, beneficiarios…)
   - Campos de auditoría y control de integridad
 ----------------------------------------------------------------------*/

SET search_path = depositos, seguridad, contabilidad, catalogos, public;
SET LOCAL SCHEMA 'depositos';

----------------------------------------------------------------------
-- 📚 2️⃣ Catálogos de solo lectura
----------------------------------------------------------------------

-- 2.1 Tipos de documentos soporte
CREATE TABLE IF NOT EXISTS depositos.tipos_documentos_soporte (
    codigo_soporte CHAR(1) PRIMARY KEY,
    descripcion_soporte VARCHAR(100) NOT NULL,
    cantidad_soporte INTEGER
);
COMMENT ON TABLE depositos.tipos_documentos_soporte IS 'Tipos de documentos soporte (chequeras, libretas, etc.)';

-- 2.2 Tipos de captaciones
CREATE TABLE IF NOT EXISTS depositos.tipos_captaciones (
    codigo_captacion CHAR(1) PRIMARY KEY,
    descripcion_captacion VARCHAR(100) NOT NULL,
    corto_largo_captacion BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE depositos.tipos_captaciones IS 'Catálogo de tipos de captaciones (Aportes, A la vista, Contractual, Permanente)';

-- 2.3 Tipos de GMF
CREATE TABLE IF NOT EXISTS depositos.tipos_gmf (
    codigo_tipo_gmf CHAR(1) PRIMARY KEY,
    descripcion_tipos_gmf VARCHAR(50) NOT NULL,
    observaciones_tipos_gmf VARCHAR(200)
);
COMMENT ON TABLE depositos.tipos_gmf IS 'Catálogo de tipos de GMF aplicables a movimientos de ahorro';

-- 2.4 Acciones de cuentas conjuntas
CREATE TABLE IF NOT EXISTS depositos.acciones_cuentas_conjuntas (
    codigo_accion CHAR(1) PRIMARY KEY,
    descripcion_accion VARCHAR(50) NOT NULL
);
COMMENT ON TABLE depositos.acciones_cuentas_conjuntas IS 'Catálogo de tipos de acción para cuentas conjuntas';

-- 2.5 Estados de cuentas de ahorro
CREATE TABLE IF NOT EXISTS depositos.estados_ahorros (
    codigo_estado_ahorro CHAR(1) PRIMARY KEY,
    descripcion_estado_ahorro VARCHAR(20) NOT NULL,
    observaciones_estado_ahorro VARCHAR(200)
);
COMMENT ON TABLE depositos.estados_ahorros IS 'Catálogo de estados de cuentas de ahorro (A=Activa, I=Inactiva, B=Bloqueada)';

-- 2.6 Módulos de origen
CREATE TABLE IF NOT EXISTS depositos.modulos (
    codigo_modulo CHAR(2) PRIMARY KEY,
    descripcion_modulo VARCHAR(20) NOT NULL
);
COMMENT ON TABLE depositos.modulos IS 'Catálogo de módulos que generan movimientos en extractos de cuentas';

-- 2.7 Estados de documentos soporte
CREATE TABLE IF NOT EXISTS depositos.estados_documentos_soporte (
    codigo_estado_documento_soporte CHAR(1) PRIMARY KEY,
    descripcion_codigo_documento_soporte VARCHAR(50)
);
COMMENT ON TABLE depositos.estados_documentos_soporte IS 'Estados posibles para documentos soporte (Activa, Anulada, Cerrada)';

----------------------------------------------------------------------
-- 🧾 3️⃣ Tablas con CRUD completo
----------------------------------------------------------------------

-- 3.1 Formas de ahorro
CREATE TABLE IF NOT EXISTS depositos.formas_ahorro (
    id_forma_ahorro INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo_forma CHAR(2) UNIQUE NOT NULL,
    nombre_forma VARCHAR(50) NOT NULL,
    consecutivo_forma INTEGER,
    tipo_captacion_forma CHAR(1) REFERENCES depositos.tipos_captaciones(codigo_captacion),
    tiempo_liquidacion INTEGER,
    cuenta_forma_corto INTEGER REFERENCES contabilidad.catalogo_cuentas(id_catalogo_cuenta),
    cuenta_forma_largo INTEGER REFERENCES contabilidad.catalogo_cuentas(id_catalogo_cuenta),
    cuenta_gasto INTEGER REFERENCES contabilidad.catalogo_cuentas(id_catalogo_cuenta),
    cuenta_cxp_forma INTEGER REFERENCES contabilidad.catalogo_cuentas(id_catalogo_cuenta),
    cuenta_gmf_forma INTEGER REFERENCES contabilidad.catalogo_cuentas(id_catalogo_cuenta),
    tipo_interes_forma INTEGER,
    fecha_ultima_liquidacion DATE,
    autorizado_forma BOOLEAN DEFAULT FALSE,
    documento_forma CHAR(1) REFERENCES depositos.tipos_documentos_soporte(codigo_soporte),
    periodo_gracia INTEGER,
    valor_minimo DOUBLE PRECISION,
    tasa_interes_forma DOUBLE PRECISION,

    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE depositos.formas_ahorro IS 'Catálogo de formas o productos de ahorro (aportes, contractual, permanente)';

-- 3.2 Cuentas de ahorro
CREATE TABLE IF NOT EXISTS depositos.cuentas_ahorro (
    id_cuenta_ahorro INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo_agencia INTEGER REFERENCES general.datos_agencias(id_agencia),
    codigo_forma INTEGER REFERENCES depositos.formas_ahorro(id_forma_ahorro),
    codigo_cuenta CHAR(10) UNIQUE NOT NULL,
    id_datos_personal INTEGER REFERENCES hoja_vida.datos_personales(id_datos_personal),
    fecha_apertura_cuenta DATE DEFAULT CURRENT_DATE,
    saldo_inicial_cuenta DOUBLE PRECISION DEFAULT 0,
    saldo_actual_cuenta DOUBLE PRECISION DEFAULT 0,
    estado_cuenta_cuenta CHAR(1) REFERENCES depositos.estados_ahorros(codigo_estado_ahorro),
    fecha_estado_cuenta DATE,
    gmf_cuenta_cuenta CHAR(1) REFERENCES depositos.tipos_gmf(codigo_tipo_gmf),
    fecha_gmf_cuenta DATE,
    libranza_cuenta BOOLEAN DEFAULT FALSE,
    libranzatiempo_pago CHAR(1) DEFAULT 'M' CHECK (libranzatiempo_pago IN ('M','Q')),
    cuota_mensual_cuenta DOUBLE PRECISION DEFAULT 0,
    retencion_fuente_cuenta BOOLEAN DEFAULT FALSE,
    plazo_cuenta INTEGER DEFAULT 0,
    fecha_final_cuenta DATE,
    cuenta_activa CHAR(1) DEFAULT 'A' CHECK (cuenta_activa IN ('A','I')),
    cuenta_conjunta CHAR(1),
    accion_conjunta CHAR(1) REFERENCES depositos.acciones_cuentas_conjuntas(codigo_accion),

    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE depositos.cuentas_ahorro IS 'Tabla principal de cuentas de ahorro de los asociados';

-- 3.3 Empresas libranza
CREATE TABLE IF NOT EXISTS depositos.empresas_libranza (
    id_empresa_libranza INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_datos_personal INTEGER REFERENCES hoja_vida.datos_personales(id_datos_personal),
    documento CHAR(20),
    cuenta_deudora INTEGER REFERENCES contabilidad.catalogo_cuentas(id_catalogo_cuenta),

    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE depositos.empresas_libranza IS 'Empresas con convenio de libranza para descuento por nómina';

-- 3.4 Cuentas ahorro conjuntas
CREATE TABLE IF NOT EXISTS depositos.cuentas_ahorro_conjuntas (
    id_cuenta_conjunta INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cuenta_ahorro INTEGER REFERENCES depositos.cuentas_ahorro(id_cuenta_ahorro),
    id_datos_personal INTEGER REFERENCES hoja_vida.datos_personales(id_datos_personal),

    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE depositos.cuentas_ahorro_conjuntas IS 'Asociados adicionales vinculados como cotitulares o apoderados';

-- 3.10 Tipo de movimiento
CREATE TABLE IF NOT EXISTS depositos.tipo_movimiento (
    codigo_movimiento CHAR(3) PRIMARY KEY,
    descripcion VARCHAR(100) NOT NULL,
    accion_movimiento CHAR(1) CHECK (accion_movimiento IN ('S','R')),
    contabilizacion_diaria BOOLEAN DEFAULT TRUE,
    genera_gmf BOOLEAN DEFAULT FALSE,

    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE depositos.tipo_movimiento IS 'Tipos de movimiento que afectan el saldo de las cuentas de ahorro';


-- 3.5 Extractos de cuentas de ahorro
CREATE TABLE IF NOT EXISTS depositos.extractos_cuentas_ahorros (
    id_extracto_cuenta_ahorro INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cuenta_ahorro INTEGER REFERENCES depositos.cuentas_ahorro(id_cuenta_ahorro),
    fecha_movimiento DATE,
    hora_movimiento TIME,
    tipo_comprobante CHAR(2),
    numero_comprobante CHAR(10),
    tipo_movimiento CHAR(3) REFERENCES depositos.tipo_movimiento(codigo_movimiento),
    valor_debito DOUBLE PRECISION DEFAULT 0,
    valor_credito DOUBLE PRECISION DEFAULT 0,
    modulo CHAR(2) REFERENCES depositos.modulos(codigo_modulo),
    tarjeta CHAR(1) DEFAULT 'N',
    establecimiento VARCHAR(100),

    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE depositos.extractos_cuentas_ahorros IS 'Extractos y movimientos de cuentas de ahorro';

-- 3.6 Documentos soporte
CREATE TABLE IF NOT EXISTS depositos.documentos_soporte (
    id_documento_soporte INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cuenta_ahorro INTEGER REFERENCES depositos.cuentas_ahorro(id_cuenta_ahorro),
    tipo_documento_soporte CHAR(1) REFERENCES depositos.tipos_documentos_soporte(codigo_soporte),
    numero_inicial DOUBLE PRECISION,
    numero_final DOUBLE PRECISION,
    fecha_entrega DATE,
    estado_documento CHAR(1) REFERENCES depositos.estados_documentos_soporte(codigo_estado_documento_soporte),
    fecha_estado DATE,

    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE depositos.documentos_soporte IS 'Chequeras o libretas emitidas para las cuentas de ahorro';

-- 3.7 Canjes
CREATE TABLE IF NOT EXISTS depositos.canjes_cuentas_ahorros (
    id_canje_cuenta_ahorro INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cuenta_ahorro INTEGER REFERENCES depositos.cuentas_ahorro(id_cuenta_ahorro),
    fecha_ingreso DATE,
    valor_canje DOUBLE PRECISION,
    numero_cheque CHAR(20),
    estado_canje CHAR(1) DEFAULT 'A' CHECK (estado_canje IN ('A','C')),
    valor_liberado DOUBLE PRECISION DEFAULT 0,

    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE depositos.canjes_cuentas_ahorros IS 'Canjes en proceso de confirmación para abono en cuenta';

-- 3.8 Beneficiarios
CREATE TABLE IF NOT EXISTS depositos.beneficiarios_cuenta_ahorros (
    id_beneficiario INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cuenta_ahorro INTEGER REFERENCES depositos.cuentas_ahorro(id_cuenta_ahorro),
    documento_beneficiario VARCHAR(20),
    nombre_beneficiario VARCHAR(100) NOT NULL,
    telefono_beneficiario VARCHAR(7),
    celular_beneficiario VARCHAR(10),
    tipo_parentesco CHAR(2) REFERENCES catalogos.parentescos(codigo_parentesco),

    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE depositos.beneficiarios_cuenta_ahorros IS 'Beneficiarios registrados para cada cuenta de ahorro';

-- 3.9 Poderes
CREATE TABLE IF NOT EXISTS depositos.poderes_cuentas_ahorro (
    id_poder INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cuenta INTEGER REFERENCES depositos.cuentas_ahorro(id_cuenta_ahorro),
    documento_poder VARCHAR(20) NOT NULL,
    nombre_poder VARCHAR(100) NOT NULL,
    telefono_poder VARCHAR(7),
    celular_poder VARCHAR(10),

    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fk_seguridad_edicion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_edicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE depositos.poderes_cuentas_ahorro IS 'Apoderados autorizados para el manejo de cuentas de ahorro';


-- 3.11 Histórico documentos soporte
CREATE TABLE IF NOT EXISTS depositos.historicos_documentos_soporte (
    id_historico_documento_soporte INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cuenta_ahorro INTEGER REFERENCES depositos.cuentas_ahorro(id_cuenta_ahorro),
    tipo_documento_soporte CHAR(1) REFERENCES depositos.tipos_documentos_soporte(codigo_soporte),
    numero_inicial DOUBLE PRECISION,
    numero_final DOUBLE PRECISION,
    fecha_entrega DATE,
    estado_documento CHAR(1) REFERENCES depositos.estados_documentos_soporte(codigo_estado_documento_soporte),
    fecha_estado DATE,
    fecha_actualizacion_documento DATE DEFAULT CURRENT_DATE,

    fk_seguridad_creacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE depositos.historicos_documentos_soporte IS 'Histórico de emisión y control de talonarios / chequeras entregadas';

----------------------------------------------------------------------
-- ✅ FIN DE MIGRACIÓN
----------------------------------------------------------------------
