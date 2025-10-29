-- ============================================================
-- 🧱 V009__catalogos_enriquecidos.sql
-- ============================================================
-- Vistas extendidas y combinadas con relaciones cruzadas
-- Autor: ERP ASSIP SOLIDARIA Y FINANCIERA
-- Fecha: 2025-10-24
-- ============================================================

CREATE SCHEMA IF NOT EXISTS shared;

-- ============================================================
-- 1️⃣ VISTAS CON RELACIONES JERÁRQUICAS
-- ============================================================

CREATE OR REPLACE VIEW shared.vw_catalogo_departamentos_paises AS
SELECT d.id_departamento, d.codigo_departamento, d.nombre_departamento,
       p.id_pais, p.nombre_pais, TRUE AS activo
FROM catalogos.departamentos d
LEFT JOIN catalogos.paises p ON p.id_pais = d.id_departamento; -- si tuvieras relación explícita

CREATE OR REPLACE VIEW shared.vw_catalogo_ciudades_departamentos AS
SELECT c.id_ciudad, c.codigo_ciudad, c.nombre_ciudad,
       d.id_departamento, d.nombre_departamento, TRUE AS activo
FROM catalogos.ciudades c
LEFT JOIN catalogos.departamentos d ON c.id_departamento = d.id_departamento;

CREATE OR REPLACE VIEW shared.vw_catalogo_sub_zonas_zonas AS
SELECT sz.id_sub_zona, sz.codigo_sub_zona, sz.nombre_sub_zona,
       z.id_zona, z.nombre_zona, TRUE AS activo
FROM general.sub_zonas sz
LEFT JOIN general.zonas z ON sz.id_zona = z.id_zona;

CREATE OR REPLACE VIEW shared.vw_general_agencias_ciudades AS
SELECT a.id_agencia, a.codigo_agencia, a.nombre_agencia, a.sigla_agencia,
       c.nombre_ciudad, d.nombre_departamento, TRUE AS activo
FROM general.datos_agencias a
LEFT JOIN catalogos.ciudades c ON a.id_ciudad = c.id_ciudad
LEFT JOIN catalogos.departamentos d ON a.id_departamento = d.id_departamento;

CREATE OR REPLACE VIEW shared.vw_general_parametros_agencia AS
SELECT p.id_parametro, p.codigo_parametro, p.nombre_parametro, p.valor_parametro,
       a.nombre_agencia, a.codigo_agencia, TRUE AS activo
FROM general.parametros p
LEFT JOIN general.datos_agencias a ON p.id_agencia = a.id_agencia;

-- ============================================================
-- 2️⃣ VISTA DE PERSONAS RESUMEN
-- ============================================================

CREATE OR REPLACE VIEW shared.vw_personas_resumen AS
SELECT dp.id_datos_personal,
       dp.tipo_documento, dp.documento,
       dp.nombres, dp.primer_apellido, dp.segundo_apellido,
       dp.fecha_apertura, dp.fecha_actualizacion,
       g.nombre_genero AS genero,
       ec.nombre_estado_civil AS estado_civil,
       ne.nombre_escolaridad AS escolaridad,
       oc.nombre_ocupacion AS ocupacion,
       se.nombre_sector_economico AS sector_economico,
       tv.nombre_tipo_vivienda AS tipo_vivienda,
       ub.direccion, ub.celular_uno, ub.correo,
       c.nombre_ciudad AS ciudad, d.nombre_departamento AS departamento,
       sz.nombre_sub_zona AS sub_zona, z.nombre_zona AS zona,
       f.valor_salario, f.total_activos, f.total_pasivos,
       TRUE AS activo
FROM hoja_vida.datos_personales dp
LEFT JOIN hoja_vida.ubicaciones ub ON dp.id_datos_personal = ub.id_datos_personal
LEFT JOIN catalogos.generos g ON dp.codigo_genero = g.codigo_genero
LEFT JOIN catalogos.estados_civiles ec ON dp.codigo_estado_civil = ec.codigo_estado_civil
LEFT JOIN catalogos.niveles_escolares ne ON dp.codigo_escolaridad = ne.codigo_escolaridad
LEFT JOIN catalogos.ocupaciones oc ON dp.codigo_ocupacion = oc.codigo_ocupacion
LEFT JOIN catalogos.sectores_economicos se ON dp.codigo_sector_economico = se.codigo_sector_economico
LEFT JOIN catalogos.tipos_viviendas tv ON dp.codigo_tipo_vivienda = tv.codigo_tipo_vivienda
LEFT JOIN catalogos.ciudades c ON ub.id_ciudad = c.id_ciudad
LEFT JOIN catalogos.departamentos d ON ub.id_departamento = d.id_departamento
LEFT JOIN general.sub_zonas sz ON ub.id_sub_zona = sz.id_sub_zona
LEFT JOIN general.zonas z ON sz.id_zona = z.id_zona
LEFT JOIN hoja_vida.financieros f ON dp.id_datos_personal = f.id_datos_personal;

-- ============================================================
-- 3️⃣ VALIDACIÓN
-- ============================================================

COMMENT ON SCHEMA shared IS 'Vistas globales enriquecidas: asociaciones y resúmenes transversales para reportes.';

-- SELECT * FROM shared.vw_catalogo_ciudades_departamentos LIMIT 5;
-- SELECT * FROM shared.vw_personas_resumen LIMIT 5;
