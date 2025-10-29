-- ============================================================
-- 🧱 V008__catalogos_globales.sql
-- ============================================================
-- Creación de vistas transversales de catálogos y tablas generales
-- Esquema: shared
-- Autor: ERP ASSIP SOLIDARIA Y FINANCIERA
-- Fecha: 2025-10-24
-- ============================================================

CREATE SCHEMA IF NOT EXISTS shared;

-- ============================================================
-- 1️⃣ CATÁLOGOS BASE
-- ============================================================

CREATE OR REPLACE VIEW shared.vw_catalogo_paises AS
SELECT id_pais AS id, codigo_pais AS codigo, nombre_pais AS nombre, TRUE AS activo
FROM catalogos.paises;

CREATE OR REPLACE VIEW shared.vw_catalogo_departamentos AS
SELECT id_departamento AS id, codigo_departamento AS codigo, nombre_departamento AS nombre, TRUE AS activo
FROM catalogos.departamentos;

CREATE OR REPLACE VIEW shared.vw_catalogo_ciudades AS
SELECT id_ciudad AS id, codigo_ciudad AS codigo, nombre_ciudad AS nombre, id_departamento, TRUE AS activo
FROM catalogos.ciudades;

CREATE OR REPLACE VIEW shared.vw_catalogo_generos AS
SELECT codigo_genero AS codigo, nombre_genero AS nombre, TRUE AS activo
FROM catalogos.generos;

CREATE OR REPLACE VIEW shared.vw_catalogo_estados_civiles AS
SELECT codigo_estado_civil AS codigo, nombre_estado_civil AS nombre, TRUE AS activo
FROM catalogos.estados_civiles;

CREATE OR REPLACE VIEW shared.vw_catalogo_niveles_escolares AS
SELECT codigo_escolaridad AS codigo, nombre_escolaridad AS nombre, TRUE AS activo
FROM catalogos.niveles_escolares;

CREATE OR REPLACE VIEW shared.vw_catalogo_ocupaciones AS
SELECT codigo_ocupacion AS codigo, nombre_ocupacion AS nombre, TRUE AS activo
FROM catalogos.ocupaciones;

CREATE OR REPLACE VIEW shared.vw_catalogo_tipos_contratos AS
SELECT codigo_tipo_contrato AS codigo, nombre_tipo_contrato AS nombre, TRUE AS activo
FROM catalogos.tipos_contratos;

CREATE OR REPLACE VIEW shared.vw_catalogo_tipos_empresas AS
SELECT codigo_tipo_empresa AS codigo, nombre_tipo_empresa AS nombre, TRUE AS activo
FROM catalogos.tipos_empresas;

CREATE OR REPLACE VIEW shared.vw_catalogo_sectores_economicos AS
SELECT codigo_sector_economico AS codigo, nombre_sector_economico AS nombre, TRUE AS activo
FROM catalogos.sectores_economicos;

CREATE OR REPLACE VIEW shared.vw_catalogo_actividades_economicas_ses AS
SELECT codigo_actividad_ses AS codigo, nombre_actividad_ses AS nombre, TRUE AS activo
FROM catalogos.actividades_economicas_ses;

CREATE OR REPLACE VIEW shared.vw_catalogo_actividades_economicas_dian AS
SELECT codigo_actividad_dian AS codigo, nombre_actividad_dian AS nombre, TRUE AS activo
FROM catalogos.actividades_economicas_dian;

CREATE OR REPLACE VIEW shared.vw_catalogo_tipos_peps AS
SELECT tipo_peps AS codigo, nombre_tipo_peps AS nombre, TRUE AS activo
FROM catalogos.tipos_peps;

CREATE OR REPLACE VIEW shared.vw_catalogo_parentescos AS
SELECT codigo_parentesco AS codigo, nombre_parentesco AS nombre, TRUE AS activo
FROM catalogos.parentescos;

CREATE OR REPLACE VIEW shared.vw_catalogo_tipos_bienes AS
SELECT codigo_tipo_bien AS codigo, descripcion_tipo_bien AS nombre, TRUE AS activo
FROM catalogos.tipos_bienes;

CREATE OR REPLACE VIEW shared.vw_catalogo_niveles_ingresos AS
SELECT codigo_nivel_ingreso AS codigo, valor1_natural, valor2_natural, valor1_juridico, valor2_juridico, TRUE AS activo
FROM catalogos.niveles_ingresos;

CREATE OR REPLACE VIEW shared.vw_catalogo_tipos_regimen AS
SELECT codigo_regimen AS codigo, nombre_regimen AS nombre, porcentaje_retencion, base_retencion, TRUE AS activo
FROM catalogos.tipos_regimen;

CREATE OR REPLACE VIEW shared.vw_catalogo_tipos_viviendas AS
SELECT codigo_tipo_vivienda AS codigo, nombre_tipo_vivienda AS nombre, TRUE AS activo
FROM catalogos.tipos_viviendas;

CREATE OR REPLACE VIEW shared.vw_catalogo_jornadas_laborales AS
SELECT codigo_jornada AS codigo, nombre_jornada AS nombre, TRUE AS activo
FROM catalogos.jornadas_laborales;

CREATE OR REPLACE VIEW shared.vw_catalogo_tipos_directivos AS
SELECT codigo_tipo_directivo AS codigo, nombre_tipo_directivo AS nombre, TRUE AS activo
FROM catalogos.tipos_directivos;

-- ============================================================
-- 2️⃣ TABLAS GENERALES
-- ============================================================

CREATE OR REPLACE VIEW shared.vw_general_empresas AS
SELECT razon_social AS nombre, tipo_documento, documento_empresa AS documento, sigla_empresa, correo_corporativo, telefono, celular, sitio_web, TRUE AS activo
FROM general.empresas;

CREATE OR REPLACE VIEW shared.vw_general_agencias AS
SELECT id_agencia AS id, codigo_agencia AS codigo, nombre_agencia AS nombre, sigla_agencia, direccion_agencia, id_departamento, id_ciudad, correo_agencia, celular_agencia, telefono_agencia, TRUE AS activo
FROM general.datos_agencias;

CREATE OR REPLACE VIEW shared.vw_general_zonas AS
SELECT id_zona AS id, codigo_zona AS codigo, nombre_zona AS nombre, comentario_zona, TRUE AS activo
FROM general.zonas;

CREATE OR REPLACE VIEW shared.vw_general_sub_zonas AS
SELECT id_sub_zona AS id, codigo_sub_zona AS codigo, nombre_sub_zona AS nombre, id_zona, comentario_sub_zona, TRUE AS activo
FROM general.sub_zonas;

CREATE OR REPLACE VIEW shared.vw_general_parametros AS
SELECT id_parametro AS id, codigo_parametro AS codigo, nombre_parametro AS nombre, valor_parametro, id_agencia, TRUE AS activo
FROM general.parametros;

-- ============================================================
-- 3️⃣ ÍNDICES SUGERIDOS
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_shared_paises_codigo    ON catalogos.paises (codigo_pais);
CREATE INDEX IF NOT EXISTS idx_shared_departamentos_cod ON catalogos.departamentos (codigo_departamento);
CREATE INDEX IF NOT EXISTS idx_shared_ciudades_cod      ON catalogos.ciudades (codigo_ciudad);
CREATE INDEX IF NOT EXISTS idx_shared_generos_cod       ON catalogos.generos (codigo_genero);
CREATE INDEX IF NOT EXISTS idx_shared_estadocivil_cod   ON catalogos.estados_civiles (codigo_estado_civil);
CREATE INDEX IF NOT EXISTS idx_shared_ocupaciones_cod   ON catalogos.ocupaciones (codigo_ocupacion);
CREATE INDEX IF NOT EXISTS idx_shared_parentescos_cod   ON catalogos.parentescos (codigo_parentesco);

-- ============================================================
-- 4️⃣ VALIDACIÓN Y PRUEBA
-- ============================================================

COMMENT ON SCHEMA shared IS 'Vistas transversales de catálogos y tablas generales. Uso global para formularios, reportes y autocompletados.';

-- Verificación rápida:
-- SELECT * FROM shared.vw_catalogo_paises LIMIT 5;
-- SELECT * FROM shared.vw_general_agencias LIMIT 5;
-- SELECT * FROM shared.vw_catalogo_estados_civiles;
