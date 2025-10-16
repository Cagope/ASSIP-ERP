-- =========================
-- ESQUEMAS
-- =========================
CREATE SCHEMA IF NOT EXISTS catalogos;
CREATE SCHEMA IF NOT EXISTS general;
CREATE SCHEMA IF NOT EXISTS hoja_vida;
CREATE SCHEMA IF NOT EXISTS contabilidad;
CREATE SCHEMA IF NOT EXISTS depositos;
CREATE SCHEMA IF NOT EXISTS cdats;
CREATE SCHEMA IF NOT EXISTS cartera;
CREATE SCHEMA IF NOT EXISTS cajas;
CREATE SCHEMA IF NOT EXISTS activos_fijos;
CREATE SCHEMA IF NOT EXISTS nomina;
CREATE SCHEMA IF NOT EXISTS tesoreria;
CREATE SCHEMA IF NOT EXISTS tarjetas;

-- =========================
-- CATÁLOGOS (solo lectura)
-- =========================
CREATE TABLE IF NOT EXISTS catalogos.tipos_documentos (
  tipo_documento         VARCHAR(2) PRIMARY KEY,
  nombre_tipo_documento  VARCHAR(50),
  tipo_dv                BOOLEAN,
  tipo_documento_dian    VARCHAR(2)
);

CREATE TABLE IF NOT EXISTS catalogos.paises (
  id_pais       INTEGER PRIMARY KEY,
  codigo_pais   VARCHAR(5) UNIQUE,
  codigo_dos    VARCHAR(5),
  nombre_pais   VARCHAR(100) NOT NULL
);

-- No depende de país.
CREATE TABLE IF NOT EXISTS catalogos.departamentos (
  id_departamento       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  codigo_departamento   VARCHAR(5) UNIQUE,
  nombre_departamento   VARCHAR(100) NOT NULL
);

-- Depende de departamento. Captura en cascada.
CREATE TABLE IF NOT EXISTS catalogos.ciudades (
  id_ciudad        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_departamento  INTEGER NOT NULL REFERENCES catalogos.departamentos(id_departamento),
  codigo_ciudad    VARCHAR(5) NOT NULL,
  nombre_ciudad    VARCHAR(100),
  CONSTRAINT uk_ciudades UNIQUE (id_departamento, codigo_ciudad)
);

CREATE TABLE IF NOT EXISTS catalogos.generos (
  codigo_genero  VARCHAR(1) PRIMARY KEY,
  nombre_genero  VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS catalogos.estados_civiles (
  codigo_estado_civil  VARCHAR(1) PRIMARY KEY,
  nombre_estado_civil  VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS catalogos.niveles_escolares (
  codigo_escolaridad  VARCHAR(2) PRIMARY KEY,
  nombre_escolaridad  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS catalogos.ocupaciones (
  codigo_ocupacion  VARCHAR(2) PRIMARY KEY,
  nombre_ocupacion  VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS catalogos.tipos_contratos (
  codigo_tipo_contrato  VARCHAR(2) PRIMARY KEY,
  nombre_tipo_contrato  VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS catalogos.tipos_empresas (
  codigo_tipo_empresa  VARCHAR(2) PRIMARY KEY,
  nombre_tipo_empresa  VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS catalogos.sectores_economicos (
  codigo_sector_economico  VARCHAR(3) PRIMARY KEY,
  nombre_sector_economico  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS catalogos.actividades_economicas_ses (
  codigo_actividad_ses  VARCHAR(4) PRIMARY KEY,
  nombre_actividad_ses  VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS catalogos.actividades_economicas_dian (
  codigo_actividad_dian  VARCHAR(4) PRIMARY KEY,
  nombre_actividad_dian  VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS catalogos.tipos_peps (
  tipo_peps        VARCHAR(5) PRIMARY KEY,
  nombre_tipo_peps VARCHAR(250)
);

CREATE TABLE IF NOT EXISTS catalogos.parentescos (
  codigo_parentesco  VARCHAR(2) PRIMARY KEY,
  nombre_parentesco  VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS catalogos.tipos_bienes (
  codigo_tipo_bien       VARCHAR(2) PRIMARY KEY,
  descripcion_tipo_bien  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS catalogos.niveles_ingresos (
  codigo_nivel_ingreso  VARCHAR(2) PRIMARY KEY,
  valor1_natural        DECIMAL(18,2),
  valor2_natural        DECIMAL(18,2),
  valor1_juridico       DECIMAL(18,2),
  valor2_juridico       DECIMAL(18,2)
);

-- Para la lógica: base_retencion = true sobre el valor del pago, false sobre el valor del IVA.
CREATE TABLE IF NOT EXISTS catalogos.tipos_regimen (
  codigo_regimen         VARCHAR(2) PRIMARY KEY,
  nombre_regimen         VARCHAR(50) NOT NULL,
  porcentaje_retencion   DECIMAL(18,2) DEFAULT 0,
  base_retencion         BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS catalogos.tipos_viviendas (
  codigo_tipo_vivienda  VARCHAR(2) PRIMARY KEY,
  nombre_tipo_vivienda  VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS catalogos.jornadas_laborales (
  codigo_jornada  VARCHAR(2) PRIMARY KEY,
  nombre_jornada  VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS catalogos.tipos_directivos (
  codigo_tipo_directivo  VARCHAR(2) PRIMARY KEY,
  nombre_tipo_directivo  VARCHAR(50)
);

-- =========================
-- GENERAL (núcleo)
-- =========================
CREATE TABLE IF NOT EXISTS general.empresas (
  tipo_documento              VARCHAR(2) NOT NULL REFERENCES catalogos.tipos_documentos(tipo_documento),
  documento_empresa           VARCHAR(20) NOT NULL CHECK (documento_empresa ~ '^[0-9]+$'),
  digito_verificacion         VARCHAR(2) CHECK (digito_verificacion ~ '^[0-9]{1,2}$'),
  razon_social                VARCHAR(150) NOT NULL,
  sigla_empresa               VARCHAR(100) NOT NULL,
  fecha_constitucion          DATE,
  id_pais_documento           INTEGER REFERENCES catalogos.paises(id_pais),
  id_departamento             INTEGER REFERENCES catalogos.departamentos(id_departamento),
  id_ciudad                   INTEGER REFERENCES catalogos.ciudades(id_ciudad),
  correo_corporativo          VARCHAR(100),
  telefono                    VARCHAR(7)  CHECK (telefono ~ '^[0-9]{7}$'),
  celular                     VARCHAR(10) CHECK (celular  ~ '^[0-9]{10}$'),
  sitio_web                   VARCHAR(150),
  logo_url                    VARCHAR(200),
  FK_seguridad_creacion       INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion  INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_empresa_nit UNIQUE (tipo_documento, documento_empresa),
  CHECK (correo_corporativo IS NULL OR correo_corporativo ~ '^[^@]+@[^@]+\.[^@]+$')
);

-- Independiente de general.empresas
CREATE TABLE IF NOT EXISTS general.datos_agencias (
  id_agencia                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  codigo_agencia              VARCHAR(2) NOT NULL,
  nombre_agencia              VARCHAR(100) NOT NULL,
  sigla_agencia               VARCHAR(100) NOT NULL,
  direccion_agencia           VARCHAR(100) NOT NULL,
  id_departamento             INTEGER REFERENCES catalogos.departamentos(id_departamento),
  id_ciudad                   INTEGER REFERENCES catalogos.ciudades(id_ciudad),
  correo_agencia              VARCHAR(100),
  celular_agencia             VARCHAR(10) CHECK (celular_agencia ~ '^[0-9]{10}$'),
  telefono_agencia            VARCHAR(7)  CHECK (telefono_agencia  ~ '^[0-9]{7}$'),
  FK_seguridad_creacion       INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion  INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CHECK (correo_agencia IS NULL OR correo_agencia ~ '^[^@]+@[^@]+\.[^@]+$')
);

CREATE TABLE IF NOT EXISTS general.zonas (
  id_zona                     INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  codigo_zona                 VARCHAR(3),
  nombre_zona                 VARCHAR(100),
  comentario_zona             VARCHAR(100),
  FK_seguridad_creacion       INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion  INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Depende de zonas, en cascada
CREATE TABLE IF NOT EXISTS general.sub_zonas (
  id_sub_zona                 INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_zona                     INTEGER NOT NULL REFERENCES general.zonas(id_zona) ON DELETE CASCADE,
  codigo_sub_zona             VARCHAR(3) NOT NULL,
  nombre_sub_zona             VARCHAR(100) NOT NULL,
  comentario_sub_zona         VARCHAR(100) NOT NULL,
  FK_seguridad_creacion       INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion  INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_sub_zonas UNIQUE (id_zona, codigo_sub_zona)
);

CREATE TABLE IF NOT EXISTS general.parametros (
  id_parametro                INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_agencia                  INTEGER NOT NULL REFERENCES general.datos_agencias(id_agencia),
  codigo_parametro            INTEGER NOT NULL,
  nombre_parametro            VARCHAR(100) NOT NULL,
  valor_parametro             DECIMAL(18,2) DEFAULT 0,
  tipo_valor                  BOOLEAN NOT NULL,  -- true=valor, false=porcentaje
  FK_seguridad_creacion       INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion  INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_parametros UNIQUE (id_agencia, codigo_parametro)
);

-- =========================
-- HOJA DE VIDA (núcleo personas)
-- =========================
CREATE TABLE IF NOT EXISTS hoja_vida.datos_personales (
  id_datos_personal             INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  tipo_documento                VARCHAR(2) REFERENCES catalogos.tipos_documentos(tipo_documento),
  documento                     VARCHAR(20) NOT NULL CHECK (documento ~ '^[0-9]+$'),
  digito_verificacion           VARCHAR(2) CHECK (digito_verificacion ~ '^[0-9]{1,2}$'),
  tipo_persona                  CHAR(1) NOT NULL CHECK (tipo_persona IN ('1','2')), -- 1=Natural, 2=Jurídica
  fecha_documento               DATE,
  id_pais_documento             INTEGER REFERENCES catalogos.paises(id_pais),
  id_departamento_expedicion    INTEGER REFERENCES catalogos.departamentos(id_departamento),
  id_ciudad_expedicion          INTEGER REFERENCES catalogos.ciudades(id_ciudad),
  nombres                       VARCHAR(100) NOT NULL,
  primer_apellido               VARCHAR(50)  NOT NULL,
  segundo_apellido              VARCHAR(50),
  fecha_nacimiento              DATE,
  id_pais_nacimiento            INTEGER REFERENCES catalogos.paises(id_pais),
  id_departamento_nacimiento    INTEGER REFERENCES catalogos.departamentos(id_departamento),
  id_ciudad_nacimiento          INTEGER REFERENCES catalogos.ciudades(id_ciudad),
  fecha_apertura                DATE DEFAULT CURRENT_DATE,
  fecha_actualiza               DATE DEFAULT CURRENT_DATE,
  codigo_genero                 VARCHAR(1) REFERENCES catalogos.generos(codigo_genero),
  codigo_estado_civil           VARCHAR(1) REFERENCES catalogos.estados_civiles(codigo_estado_civil),
  codigo_escolaridad            VARCHAR(2) REFERENCES catalogos.niveles_escolares(codigo_escolaridad),
  cabeza_familia                CHAR(1) CHECK (cabeza_familia IN ('0','1')),
  estrato_social                INTEGER CHECK (estrato_social BETWEEN 0 AND 6),
  codigo_tipo_vivienda          VARCHAR(2) REFERENCES catalogos.tipos_viviendas(codigo_tipo_vivienda),
  numero_hijos                  INTEGER CHECK (numero_hijos BETWEEN 0 AND 99),
  codigo_ocupacion              VARCHAR(2) REFERENCES catalogos.ocupaciones(codigo_ocupacion),
  codigo_sector_economico       VARCHAR(3) REFERENCES catalogos.sectores_economicos(codigo_sector_economico),
  codigo_actividad_ses          VARCHAR(4) REFERENCES catalogos.actividades_economicas_ses(codigo_actividad_ses),
  codigo_actividad_dian         VARCHAR(4) REFERENCES catalogos.actividades_economicas_dian(codigo_actividad_dian),
  codigo_retencion              VARCHAR(2) REFERENCES catalogos.tipos_regimen(codigo_regimen),
  comentario                    VARCHAR(250) NOT NULL,
  foto                          VARCHAR(100),
  firma_uno                     VARCHAR(100),
  firma_dos                     VARCHAR(100),
  FK_seguridad_creacion         INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion    INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hoja_vida.datos_familiares (
  id_datos_familiares            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_datos_personal              INTEGER NOT NULL REFERENCES hoja_vida.datos_personales(id_datos_personal) ON DELETE CASCADE,
  codigo_parentesco              VARCHAR(2)   REFERENCES catalogos.parentescos(codigo_parentesco),
  nombre_datos_familiar          VARCHAR(100) NOT NULL,
  documento_datos_familiar       VARCHAR(20)  NOT NULL CHECK (documento_datos_familiar ~ '^[0-9]+$'),
  telefono_datos_familiar        VARCHAR(7)   CHECK (telefono_datos_familiar  ~ '^[0-9]{7}$'),
  celular_datos_familiar         VARCHAR(10)  CHECK (celular_datos_familiar   ~ '^[0-9]{10}$'),
  direccion_datos_familiar       VARCHAR(100) NOT NULL,
  id_departamento                INTEGER REFERENCES catalogos.departamentos(id_departamento),
  id_ciudad                      INTEGER REFERENCES catalogos.ciudades(id_ciudad),
  ingresos_datos_familiar        DECIMAL(18,2) DEFAULT 0,
  egresos_datos_familiar         DECIMAL(18,2) DEFAULT 0,
  referencia_familiar            BOOLEAN NOT NULL DEFAULT FALSE,
  FK_seguridad_creacion          INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion                 TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion     INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  -- Múltiples filas por persona (sin UNIQUE)
);

CREATE TABLE IF NOT EXISTS hoja_vida.referencias_personales (
  id_referencia_personal         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_datos_personal              INTEGER NOT NULL REFERENCES hoja_vida.datos_personales(id_datos_personal) ON DELETE CASCADE,
  nombre_referencia_personal     VARCHAR(100) NOT NULL,
  direccion_referencia_personal  VARCHAR(100) NOT NULL,
  id_departamento                INTEGER REFERENCES catalogos.departamentos(id_departamento),
  id_ciudad                      INTEGER REFERENCES catalogos.ciudades(id_ciudad),
  telefono_referencia_personal   VARCHAR(7)  CHECK (telefono_referencia_personal ~ '^[0-9]{7}$'),
  celular_referencia_personal    VARCHAR(10) CHECK (celular_referencia_personal  ~ '^[0-9]{10}$'),
  FK_seguridad_creacion          INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion                 TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion     INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hoja_vida.referencias_comerciales (
  id_referencia_comercial          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_datos_personal                INTEGER NOT NULL REFERENCES hoja_vida.datos_personales(id_datos_personal) ON DELETE CASCADE,
  nombre_referencia_comercial      VARCHAR(100) NOT NULL,
  direccion_referencia_comercial   VARCHAR(100) NOT NULL,
  id_departamento                  INTEGER REFERENCES catalogos.departamentos(id_departamento),
  id_ciudad                        INTEGER REFERENCES catalogos.ciudades(id_ciudad),
  telefono_referencia_comercial    VARCHAR(7)  CHECK (telefono_referencia_comercial ~ '^[0-9]{7}$'),
  celular_referencia_comercial     VARCHAR(10) CHECK (celular_referencia_comercial  ~ '^[0-9]{10}$'),
  comentario_referencia_comercial  VARCHAR(100) NOT NULL,
  FK_seguridad_creacion            INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion                   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion       INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hoja_vida.bienes_inmuebles (
  id_bienes_inmuebles        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_datos_personal          INTEGER NOT NULL REFERENCES hoja_vida.datos_personales(id_datos_personal) ON DELETE CASCADE,
  codigo_tipo_bien           VARCHAR(2) NOT NULL REFERENCES catalogos.tipos_bienes(codigo_tipo_bien),
  descripcion_bien_inmueble  VARCHAR(200) NOT NULL,
  direccion_bien             VARCHAR(100) NOT NULL,
  id_departamento            INTEGER REFERENCES catalogos.departamentos(id_departamento),
  id_ciudad                  INTEGER REFERENCES catalogos.ciudades(id_ciudad),
  fecha_adquisicion          DATE,
  valor_comercial            DECIMAL(18,2) DEFAULT 0,
  valor_hipoteca             DECIMAL(18,2) DEFAULT 0,
  hipoteca_entidad           VARCHAR(100) NOT NULL,
  codigo_garantia            VARCHAR(20),
  fecha_avaluo               DATE,
  valor_asegurado            DECIMAL(18,2) DEFAULT 0,
  fecha_vencimiento_seguro   DATE,
  FK_seguridad_creacion      INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hoja_vida.ubicaciones (
  id_ubicacion               INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_datos_personal          INTEGER NOT NULL REFERENCES hoja_vida.datos_personales(id_datos_personal) ON DELETE CASCADE,
  direccion                  VARCHAR(100) NOT NULL,
  barrio                     VARCHAR(100) NOT NULL,
  telefono                   VARCHAR(7)  CHECK (telefono ~ '^[0-9]{7}$'),
  celular_uno                VARCHAR(10) CHECK (celular_uno  ~ '^[0-9]{10}$'),
  celular_dos                VARCHAR(10) CHECK (celular_dos  ~ '^[0-9]{10}$'),
  correo                     VARCHAR(100),
  id_pais                    INTEGER REFERENCES catalogos.paises(id_pais),
  id_departamento            INTEGER REFERENCES catalogos.departamentos(id_departamento),
  id_ciudad                  INTEGER REFERENCES catalogos.ciudades(id_ciudad),
  id_sub_zona                INTEGER REFERENCES general.sub_zonas(id_sub_zona),
  FK_seguridad_creacion      INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_ubicacion_persona UNIQUE (id_datos_personal),
  CHECK (correo IS NULL OR correo ~ '^[^@]+@[^@]+\.[^@]+$')
);

CREATE TABLE IF NOT EXISTS hoja_vida.permisos_especiales (
  id_permiso_especial         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_datos_personal           INTEGER NOT NULL REFERENCES hoja_vida.datos_personales(id_datos_personal) ON DELETE CASCADE,
  recibe_llamadas             BOOLEAN NOT NULL DEFAULT FALSE,
  recibe_msm                  BOOLEAN NOT NULL DEFAULT FALSE,
  recibe_emails               BOOLEAN NOT NULL DEFAULT FALSE,
  recibe_cartas               BOOLEAN NOT NULL DEFAULT FALSE,
  recibe_redes_sociales       BOOLEAN NOT NULL DEFAULT FALSE,
  FK_seguridad_creacion       INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion  INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_permiso_persona UNIQUE (id_datos_personal)
);

CREATE TABLE IF NOT EXISTS hoja_vida.financieros (
  id_financiero               INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_datos_personal           INTEGER NOT NULL REFERENCES hoja_vida.datos_personales(id_datos_personal) ON DELETE CASCADE,
  valor_salario               DECIMAL(18,2) DEFAULT 0,
  valor_pension               DECIMAL(18,2) DEFAULT 0,
  ingresos_arriendo           DECIMAL(18,2) DEFAULT 0,
  ingresos_comisiones         DECIMAL(18,2) DEFAULT 0,
  otros_ingresos              DECIMAL(18,2) DEFAULT 0,
  comentario_otros_ingresos   VARCHAR(100) NOT NULL,
  egresos_familiares          DECIMAL(18,2) DEFAULT 0,
  egresos_arriendo            DECIMAL(18,2) DEFAULT 0,
  egresos_credito             DECIMAL(18,2) DEFAULT 0,
  otros_egresos               DECIMAL(18,2) DEFAULT 0,
  comentario_otros_egresos    VARCHAR(100) NOT NULL,
  total_activos               DECIMAL(18,2) DEFAULT 0,
  total_pasivos               DECIMAL(18,2) DEFAULT 0,
  origen_fondos               VARCHAR(100) NOT NULL,
  relacion_financiera         VARCHAR(100) NOT NULL,
  deuda_relacion_financiera   DECIMAL(18,2) DEFAULT 0,
  FK_seguridad_creacion       INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion  INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_financiero_persona UNIQUE (id_datos_personal)
);

CREATE TABLE IF NOT EXISTS hoja_vida.laborales (
  id_laboral                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_datos_personal           INTEGER NOT NULL REFERENCES hoja_vida.datos_personales(id_datos_personal) ON DELETE CASCADE,
  nombre_empresa              VARCHAR(100) NOT NULL,
  direccion                   VARCHAR(100) NOT NULL,
  id_pais                     INTEGER REFERENCES catalogos.paises(id_pais),
  id_departamento             INTEGER REFERENCES catalogos.departamentos(id_departamento),
  id_ciudad                   INTEGER REFERENCES catalogos.ciudades(id_ciudad),
  telefono_empresa            VARCHAR(7)  CHECK (telefono_empresa  ~ '^[0-9]{7}$'),
  celular_empresa             VARCHAR(10) CHECK (celular_empresa   ~ '^[0-9]{10}$'),
  correo_empresa              VARCHAR(100),
  codigo_tipo_empresa         VARCHAR(2) REFERENCES catalogos.tipos_empresas(codigo_tipo_empresa),
  empleado_entidad            BOOLEAN NOT NULL DEFAULT FALSE,
  codigo_tipo_contrato        VARCHAR(2) REFERENCES catalogos.tipos_contratos(codigo_tipo_contrato),
  codigo_jornada              VARCHAR(2) REFERENCES catalogos.jornadas_laborales(codigo_jornada),
  nombre_contacto             VARCHAR(100) NOT NULL,
  celular_contacto            VARCHAR(10) CHECK (celular_contacto   ~ '^[0-9]{10}$'),
  fecha_vinculacion           DATE,
  FK_seguridad_creacion       INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion  INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_laboral_persona UNIQUE (id_datos_personal),
  CHECK (correo_empresa IS NULL OR correo_empresa ~ '^[^@]+@[^@]+\.[^@]+$')
);

CREATE TABLE IF NOT EXISTS hoja_vida.sarlaft (
  id_sarlaft                    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_datos_personal             INTEGER NOT NULL REFERENCES hoja_vida.datos_personales(id_datos_personal) ON DELETE CASCADE,
  exoneracion_uiaf              BOOLEAN,
  fecha_exoneracion             DATE,
  asociado_peps                 BOOLEAN,
  tipo_peps                     VARCHAR(5) REFERENCES catalogos.tipos_peps(tipo_peps),
  observaciones_peps            VARCHAR(300) NOT NULL,
  fecha_inicial_peps            DATE,
  fecha_final_peps              DATE,
  familia_peps                  BOOLEAN,
  tipo_familia_peps             VARCHAR(5) REFERENCES catalogos.tipos_peps(tipo_peps),
  cedula_familia_peps           VARCHAR(20) CHECK (cedula_familia_peps ~ '^[0-9]+$'),
  codigo_parentesco             VARCHAR(2) REFERENCES catalogos.parentescos(codigo_parentesco),
  nombre_familia_peps           VARCHAR(100) NOT NULL,
  moneda_extranjera             BOOLEAN,
  observacion_moneda_extranjera VARCHAR(200),
  cuenta_extranjero             BOOLEAN,
  tipo_moneda_extranjera        VARCHAR(20) NOT NULL,
  numero_cuenta_extranjero      VARCHAR(30) NOT NULL,
  nombre_banco_extranjero       VARCHAR(100) NOT NULL,
  ciudad_cuenta_extranjero      VARCHAR(50) NOT NULL,
  pais_cuenta_extranjero        VARCHAR(50) NOT NULL,
  FK_seguridad_creacion         INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion    INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_sarlaft_persona UNIQUE (id_datos_personal),
  CHECK (fecha_inicial_peps IS NULL OR fecha_final_peps IS NULL OR fecha_inicial_peps <= fecha_final_peps)
);

-- =========================
-- DIRECTIVOS (por empresa)
-- =========================
CREATE TABLE IF NOT EXISTS general.directivos (
  id_directivo                INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_datos_personal           INTEGER NOT NULL REFERENCES hoja_vida.datos_personales(id_datos_personal),
  codigo_tipo_directivo       VARCHAR(2) NOT NULL REFERENCES catalogos.tipos_directivos(codigo_tipo_directivo),
  calidad_directivo           VARCHAR(1) NOT NULL,  -- '1'=Principal, '2'=Suplente
  estado_directivo            VARCHAR(1) NOT NULL,  -- '1'=Nombrado, '2'=Retirado, '3'=Excluido
  acta_asamblea               VARCHAR(10) NOT NULL,
  fecha_asamblea              DATE,
  resolucion_ses              VARCHAR(10) NOT NULL,
  fecha_resolucion            DATE,
  fecha_retiro                DATE,
  periodos_vigencia           INTEGER DEFAULT 0,
  FK_seguridad_creacion       INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion  INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CHECK (calidad_directivo IN ('1','2')),
  CHECK (estado_directivo  IN ('1','2','3')),
  CHECK (periodos_vigencia IS NULL OR periodos_vigencia >= 0)
);

CREATE TABLE IF NOT EXISTS general.privilegiados (
  id_privilegiado             INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_directivo                INTEGER NOT NULL REFERENCES general.directivos(id_directivo),
  codigo_parentesco           VARCHAR(2) NOT NULL REFERENCES catalogos.parentescos(codigo_parentesco),
  FK_seguridad_creacion       INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_creacion              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FK_seguridad_actualizacion  INTEGER NOT NULL REFERENCES seguridad.usuarios(id_usuario),
  fecha_actualizacion         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para listar privilegiados por directivo
CREATE INDEX IF NOT EXISTS idx_privilegiados_directivo
  ON general.privilegiados(id_directivo);

-- =========================================================
-- TRIGGER: actualizar fecha_actualiza en hoja_vida.datos_personales
-- =========================================================
CREATE OR REPLACE FUNCTION hoja_vida.tg_set_fecha_actualiza()
RETURNS trigger AS
$$
BEGIN
  -- Como el campo es DATE, guardamos solo la fecha del cambio
  NEW.fecha_actualiza := CURRENT_DATE;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_fecha_actualiza ON hoja_vida.datos_personales;

CREATE TRIGGER set_fecha_actualiza
BEFORE UPDATE ON hoja_vida.datos_personales
FOR EACH ROW
EXECUTE FUNCTION hoja_vida.tg_set_fecha_actualiza();

-- =========================================================
-- ÍNDICES SUGERIDOS (FKs y consultas frecuentes)
-- =========================================================

-- CATALOGOS
CREATE INDEX IF NOT EXISTS idx_ciudades_dep
  ON catalogos.ciudades(id_departamento);

-- GENERAL
CREATE INDEX IF NOT EXISTS idx_agencias_dep
  ON general.datos_agencias(id_departamento);

CREATE INDEX IF NOT EXISTS idx_agencias_ciu
  ON general.datos_agencias(id_ciudad);

CREATE INDEX IF NOT EXISTS idx_sub_zonas_zona
  ON general.sub_zonas(id_zona);

CREATE INDEX IF NOT EXISTS idx_param_agencia
  ON general.parametros(id_agencia);

-- HOJA DE VIDA: FKs a datos_personales (para cascada y joins)
CREATE INDEX IF NOT EXISTS idx_familiares_dp
  ON hoja_vida.datos_familiares(id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_refpers_dp
  ON hoja_vida.referencias_personales(id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_refcom_dp
  ON hoja_vida.referencias_comerciales(id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_bienes_dp
  ON hoja_vida.bienes_inmuebles(id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_ubica_dp
  ON hoja_vida.ubicaciones(id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_permisos_dp
  ON hoja_vida.permisos_especiales(id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_financ_dp
  ON hoja_vida.financieros(id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_laboral_dp
  ON hoja_vida.laborales(id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_sarlaft_dp
  ON hoja_vida.sarlaft(id_datos_personal);

-- HOJA DE VIDA: geográficos frecuentes (búsquedas por ciudad)
CREATE INDEX IF NOT EXISTS idx_ciudad_ubica
  ON hoja_vida.ubicaciones(id_ciudad);

CREATE INDEX IF NOT EXISTS idx_ciudad_bienes
  ON hoja_vida.bienes_inmuebles(id_ciudad);

CREATE INDEX IF NOT EXISTS idx_ciudad_refpers
  ON hoja_vida.referencias_personales(id_ciudad);

CREATE INDEX IF NOT EXISTS idx_ciudad_refcom
  ON hoja_vida.referencias_comerciales(id_ciudad);
