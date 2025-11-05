/* ================================================================
V051__reporting_views.sql
Bloque 1 — Vistas Reporting para Hoja de Vida
Incluye:
  - datos_personales
  - ubicaciones
  - datos_familiares
================================================================ */

/* 🔹 Asegurar esquema */
CREATE SCHEMA IF NOT EXISTS reporting;

/* ================================================================
1️⃣ VW — DATOS PERSONALES
Descripción:
Vista principal que consolida información básica de cada persona,
decodificando catálogos (género, estado civil, escolaridad, vivienda,
ocupación, sector, actividad, etc.)
================================================================ */
DROP VIEW IF EXISTS reporting.vw_hoja_vida_datos_personales_total;
CREATE OR REPLACE VIEW reporting.vw_hoja_vida_datos_personales_total AS
SELECT
    dp.id_datos_personal,
    dp.tipo_documento,
    td.nombre_tipo_documento,
    dp.documento,
    dp.tipo_persona,
    dp.tiene_rut,
    dp.digito_verificacion,
    dp.fecha_documento,
    dp.id_pais_documento,
    pdoc.nombre_pais AS nombre_pais_documento,
    dp.id_departamento_expedicion,
    dexp.nombre_departamento AS nombre_departamento_expedicion,
    dp.id_ciudad_expedicion,
    cexp.nombre_ciudad AS nombre_ciudad_expedicion,
    dp.nombres,
    dp.primer_apellido,
    dp.segundo_apellido,
    dp.fecha_nacimiento,
    dp.id_pais_nacimiento,
    pnac.nombre_pais AS nombre_pais_nacimiento,
    dp.id_departamento_nacimiento,
    dnac.nombre_departamento AS nombre_departamento_nacimiento,
    dp.id_ciudad_nacimiento,
    cnac.nombre_ciudad AS nombre_ciudad_nacimiento,
    dp.fecha_apertura,
    dp.fecha_actualizacion,
    dp.codigo_genero,
    g.nombre_genero,
    dp.codigo_estado_civil,
    ec.nombre_estado_civil,
    dp.codigo_escolaridad,
    ne.nombre_escolaridad,
    dp.cabeza_familia,
    dp.estrato_social,
    dp.codigo_tipo_vivienda,
    tv.nombre_tipo_vivienda,
    dp.numero_hijos,
    dp.codigo_ocupacion,
    o.nombre_ocupacion,
    dp.codigo_sector_economico,
    se.nombre_sector_economico,
    dp.codigo_actividad_ses,
    ase.nombre_actividad_ses,
    dp.codigo_actividad_dian,
    adian.nombre_actividad_dian,
    dp.comentario,
    dp.foto,
    dp.firma_uno,
    dp.firma_dos,
    dp.fk_seguridad_creacion,
    dp.fecha_creacion,
    dp.fk_seguridad_edicion,
    dp.fecha_edicion
FROM hoja_vida.datos_personales dp
    LEFT JOIN catalogos.tipos_documentos td ON dp.tipo_documento = td.tipo_documento
    LEFT JOIN catalogos.paises pdoc ON dp.id_pais_documento = pdoc.id_pais
    LEFT JOIN catalogos.departamentos dexp ON dp.id_departamento_expedicion = dexp.id_departamento
    LEFT JOIN catalogos.ciudades cexp ON dp.id_ciudad_expedicion = cexp.id_ciudad
    LEFT JOIN catalogos.paises pnac ON dp.id_pais_nacimiento = pnac.id_pais
    LEFT JOIN catalogos.departamentos dnac ON dp.id_departamento_nacimiento = dnac.id_departamento
    LEFT JOIN catalogos.ciudades cnac ON dp.id_ciudad_nacimiento = cnac.id_ciudad
    LEFT JOIN catalogos.generos g ON dp.codigo_genero = g.codigo_genero
    LEFT JOIN catalogos.estados_civiles ec ON dp.codigo_estado_civil = ec.codigo_estado_civil
    LEFT JOIN catalogos.niveles_escolares ne ON dp.codigo_escolaridad = ne.codigo_escolaridad
    LEFT JOIN catalogos.tipos_viviendas tv ON dp.codigo_tipo_vivienda = tv.codigo_tipo_vivienda
    LEFT JOIN catalogos.ocupaciones o ON dp.codigo_ocupacion = o.codigo_ocupacion
    LEFT JOIN catalogos.sectores_economicos se ON dp.codigo_sector_economico = se.codigo_sector_economico
    LEFT JOIN catalogos.actividades_economicas_ses ase ON dp.codigo_actividad_ses = ase.codigo_actividad_ses
    LEFT JOIN catalogos.actividades_economicas_dian adian ON dp.codigo_actividad_dian = adian.codigo_actividad_dian;
COMMENT ON VIEW reporting.vw_hoja_vida_datos_personales_total IS
'Vista reporting: datos personales con decodificación de catálogos y campos de auditoría.';

/* ================================================================
2️⃣ VW — UBICACIONES
================================================================ */
DROP VIEW IF EXISTS reporting.vw_hoja_vida_ubicaciones_total;
CREATE OR REPLACE VIEW reporting.vw_hoja_vida_ubicaciones_total AS
SELECT
    u.id_ubicacion,
    u.id_datos_personal,
    u.direccion,
    u.barrio,
    u.telefono,
    u.celular_uno,
    u.celular_dos,
    u.correo,
    u.id_pais,
    p.nombre_pais,
    u.id_departamento,
    d.nombre_departamento,
    u.id_ciudad,
    c.nombre_ciudad,
    u.id_zona,
    z.nombre_zona,
    u.id_sub_zona,
    sz.nombre_sub_zona,
    u.fk_seguridad_creacion,
    u.fecha_creacion,
    u.fk_seguridad_edicion,
    u.fecha_edicion
FROM hoja_vida.ubicaciones u
    LEFT JOIN catalogos.paises p ON u.id_pais = p.id_pais
    LEFT JOIN catalogos.departamentos d ON u.id_departamento = d.id_departamento
    LEFT JOIN catalogos.ciudades c ON u.id_ciudad = c.id_ciudad
    LEFT JOIN general.zonas z ON u.id_zona = z.id_zona
    LEFT JOIN general.sub_zonas sz ON u.id_sub_zona = sz.id_sub_zona;
COMMENT ON VIEW reporting.vw_hoja_vida_ubicaciones_total IS
'Vista reporting: direcciones y contactos, con decodificación geográfica y auditoría.';

/* ================================================================
3️⃣ VW — DATOS FAMILIARES
================================================================ */
DROP VIEW IF EXISTS reporting.vw_hoja_vida_datos_familiares_total;
CREATE OR REPLACE VIEW reporting.vw_hoja_vida_datos_familiares_total AS
SELECT
    f.id_datos_familiares,
    f.id_datos_personal,
    f.codigo_parentesco,
    par.nombre_parentesco,
    f.nombre_datos_familiar,
    f.documento_datos_familiar,
    f.telefono_datos_familiar,
    f.celular_datos_familiar,
    f.direccion_datos_familiar,
    f.id_departamento,
    d.nombre_departamento,
    f.id_ciudad,
    c.nombre_ciudad,
    f.ingresos_datos_familiar,
    f.egresos_datos_familiar,
    f.referencia_familiar,
    f.fk_seguridad_creacion,
    f.fecha_creacion,
    f.fk_seguridad_edicion,
    f.fecha_edicion
FROM hoja_vida.datos_familiares f
    LEFT JOIN catalogos.parentescos par ON f.codigo_parentesco = par.codigo_parentesco
    LEFT JOIN catalogos.departamentos d ON f.id_departamento = d.id_departamento
    LEFT JOIN catalogos.ciudades c ON f.id_ciudad = c.id_ciudad;
COMMENT ON VIEW reporting.vw_hoja_vida_datos_familiares_total IS
'Vista reporting: datos familiares con parentescos, ingresos, egresos y auditoría.';

/* ================================================================
4️⃣ VW — LABORALES
================================================================ */
DROP VIEW IF EXISTS reporting.vw_hoja_vida_laborales_total;
CREATE OR REPLACE VIEW reporting.vw_hoja_vida_laborales_total AS
SELECT
    l.id_laboral,
    l.id_datos_personal,
    l.nombre_empresa,
    l.direccion,
    l.id_pais,
    p.nombre_pais,
    l.id_departamento,
    d.nombre_departamento,
    l.id_ciudad,
    c.nombre_ciudad,
    l.telefono_empresa,
    l.celular_empresa,
    l.correo_empresa,
    l.codigo_tipo_empresa,
    te.nombre_tipo_empresa,
    l.empleado_entidad,
    l.codigo_tipo_contrato,
    tc.nombre_tipo_contrato,
    l.codigo_jornada,
    j.nombre_jornada,
    l.nombre_contacto,
    l.celular_contacto,
    l.fecha_vinculacion,
    l.fk_seguridad_creacion,
    l.fecha_creacion,
    l.fk_seguridad_edicion,
    l.fecha_edicion
FROM hoja_vida.laborales l
    LEFT JOIN catalogos.paises p ON l.id_pais = p.id_pais
    LEFT JOIN catalogos.departamentos d ON l.id_departamento = d.id_departamento
    LEFT JOIN catalogos.ciudades c ON l.id_ciudad = c.id_ciudad
    LEFT JOIN catalogos.tipos_empresas te ON l.codigo_tipo_empresa = te.codigo_tipo_empresa
    LEFT JOIN catalogos.tipos_contratos tc ON l.codigo_tipo_contrato = tc.codigo_tipo_contrato
    LEFT JOIN catalogos.jornadas_laborales j ON l.codigo_jornada = j.codigo_jornada;
COMMENT ON VIEW reporting.vw_hoja_vida_laborales_total IS
'Vista reporting: información laboral detallada, con decodificación de ubicación, empresa, contrato y jornada.';

/* ================================================================
5️⃣ VW — FINANCIEROS
================================================================ */
DROP VIEW IF EXISTS reporting.vw_hoja_vida_financieros_total;
CREATE OR REPLACE VIEW reporting.vw_hoja_vida_financieros_total AS
SELECT
    f.id_financiero,
    f.id_datos_personal,
    f.valor_salario,
    f.valor_pension,
    f.ingresos_arriendo,
    f.ingresos_comisiones,
    f.otros_ingresos,
    f.comentario_otros_ingresos,
    f.egresos_familiares,
    f.egresos_arriendo,
    f.egresos_credito,
    f.otros_egresos,
    f.comentario_otros_egresos,
    f.total_activos,
    f.total_pasivos,
    f.origen_fondos,
    f.relacion_financiera,
    f.deuda_relacion_financiera,
    f.fk_seguridad_creacion,
    f.fecha_creacion,
    f.fk_seguridad_edicion,
    f.fecha_edicion
FROM hoja_vida.financieros f;
COMMENT ON VIEW reporting.vw_hoja_vida_financieros_total IS
'Vista reporting: información financiera y económica, lista para reportes de ingresos, egresos y patrimonio.';

/* ================================================================
6️⃣ VW — REFERENCIAS PERSONALES
================================================================ */
DROP VIEW IF EXISTS reporting.vw_hoja_vida_referencias_personales_total;
CREATE OR REPLACE VIEW reporting.vw_hoja_vida_referencias_personales_total AS
SELECT
    rp.id_referencia_personal,
    rp.id_datos_personal,
    rp.nombre_referencia_personal,
    rp.direccion_referencia_personal,
    rp.id_departamento,
    d.nombre_departamento,
    rp.id_ciudad,
    c.nombre_ciudad,
    rp.telefono_referencia_personal,
    rp.celular_referencia_personal,
    rp.fk_seguridad_creacion,
    rp.fecha_creacion,
    rp.fk_seguridad_edicion,
    rp.fecha_edicion
FROM hoja_vida.referencias_personales rp
    LEFT JOIN catalogos.departamentos d ON rp.id_departamento = d.id_departamento
    LEFT JOIN catalogos.ciudades c ON rp.id_ciudad = c.id_ciudad;
COMMENT ON VIEW reporting.vw_hoja_vida_referencias_personales_total IS
'Vista reporting: referencias personales con ubicación y campos de auditoría.';

/* ================================================================
7️⃣ VW — REFERENCIAS COMERCIALES
================================================================ */
DROP VIEW IF EXISTS reporting.vw_hoja_vida_referencias_comerciales_total;
CREATE OR REPLACE VIEW reporting.vw_hoja_vida_referencias_comerciales_total AS
SELECT
    rc.id_referencia_comercial,
    rc.id_datos_personal,
    rc.nombre_referencia_comercial,
    rc.direccion_referencia_comercial,
    rc.id_departamento,
    d.nombre_departamento,
    rc.id_ciudad,
    c.nombre_ciudad,
    rc.telefono_referencia_comercial,
    rc.celular_referencia_comercial,
    rc.comentario_referencia_comercial,
    rc.fk_seguridad_creacion,
    rc.fecha_creacion,
    rc.fk_seguridad_edicion,
    rc.fecha_edicion
FROM hoja_vida.referencias_comerciales rc
    LEFT JOIN catalogos.departamentos d ON rc.id_departamento = d.id_departamento
    LEFT JOIN catalogos.ciudades c ON rc.id_ciudad = c.id_ciudad;
COMMENT ON VIEW reporting.vw_hoja_vida_referencias_comerciales_total IS
'Vista reporting: referencias comerciales con decodificación de ubicación y observaciones.';

/* ================================================================
8️⃣ VW — SARLAFT
================================================================ */
DROP VIEW IF EXISTS reporting.vw_hoja_vida_sarlaft_total;
CREATE OR REPLACE VIEW reporting.vw_hoja_vida_sarlaft_total AS
SELECT
    s.id_sarlaft,
    s.id_datos_personal,
    s.exoneracion_uiaf,
    s.fecha_exoneracion,
    s.asociado_peps,
    s.tipo_peps,
    tp.nombre_tipo_peps,
    s.observaciones_peps,
    s.fecha_inicial_peps,
    s.fecha_final_peps,
    s.familia_peps,
    s.tipo_familia_peps,
    tfp.nombre_tipo_peps AS nombre_tipo_familia_peps,
    s.cedula_familia_peps,
    s.codigo_parentesco,
    par.nombre_parentesco,
    s.nombre_familia_peps,
    s.moneda_extranjera,
    s.observacion_moneda_extranjera,
    s.cuenta_extranjero,
    s.tipo_moneda_extranjera,
    s.numero_cuenta_extranjero,
    s.nombre_banco_extranjero,
    s.ciudad_cuenta_extranjero,
    s.pais_cuenta_extranjero,
    s.fk_seguridad_creacion,
    s.fecha_creacion,
    s.fk_seguridad_edicion,
    s.fecha_edicion
FROM hoja_vida.sarlaft s
    LEFT JOIN catalogos.tipos_peps tp ON s.tipo_peps = tp.tipo_peps
    LEFT JOIN catalogos.tipos_peps tfp ON s.tipo_familia_peps = tfp.tipo_peps
    LEFT JOIN catalogos.parentescos par ON s.codigo_parentesco = par.codigo_parentesco;
COMMENT ON VIEW reporting.vw_hoja_vida_sarlaft_total IS
'Vista reporting: información SARLAFT con detalle de PEPs, parentesco y operaciones internacionales.';

/* ================================================================
9️⃣ VW — PERMISOS ESPECIALES
================================================================ */
DROP VIEW IF EXISTS reporting.vw_hoja_vida_permisos_especiales_total;
CREATE OR REPLACE VIEW reporting.vw_hoja_vida_permisos_especiales_total AS
SELECT
    pe.id_permiso_especial,
    pe.id_datos_personal,
    pe.recibe_llamadas,
    pe.recibe_msm,
    pe.recibe_emails,
    pe.recibe_cartas,
    pe.recibe_redes_sociales,
    pe.fecha_llamadas,
    pe.fecha_sms,
    pe.fecha_emails,
    pe.fecha_cartas,
    pe.fecha_redes,
    pe.fk_seguridad_creacion,
    pe.fecha_creacion,
    pe.fk_seguridad_edicion,
    pe.fecha_edicion
FROM hoja_vida.permisos_especiales pe;
COMMENT ON VIEW reporting.vw_hoja_vida_permisos_especiales_total IS
'Vista reporting: permisos de contacto y comunicación (SARLAFT y privacidad de datos).';

/* ================================================================
🔟 VW — BIENES INMUEBLES
================================================================ */
DROP VIEW IF EXISTS reporting.vw_hoja_vida_bienes_inmuebles_total;
CREATE OR REPLACE VIEW reporting.vw_hoja_vida_bienes_inmuebles_total AS
SELECT
    b.id_bienes_inmuebles,
    b.id_datos_personal,
    b.codigo_tipo_bien,
    tb.descripcion_tipo_bien AS nombre_tipo_bien,
    b.descripcion_bien_inmueble,
    b.direccion_bien,
    b.id_departamento,
    d.nombre_departamento,
    b.id_ciudad,
    c.nombre_ciudad,
    b.fecha_adquisicion,
    b.valor_comercial,
    b.valor_hipoteca,
    b.hipoteca_entidad,
    b.codigo_garantia,
    b.fecha_avaluo,
    b.valor_asegurado,
    b.fecha_vencimiento_seguro,
    b.fk_seguridad_creacion,
    b.fecha_creacion,
    b.fk_seguridad_edicion,
    b.fecha_edicion
FROM hoja_vida.bienes_inmuebles b
    LEFT JOIN catalogos.tipos_bienes tb ON b.codigo_tipo_bien = tb.codigo_tipo_bien
    LEFT JOIN catalogos.departamentos d ON b.id_departamento = d.id_departamento
    LEFT JOIN catalogos.ciudades c ON b.id_ciudad = c.id_ciudad;
COMMENT ON VIEW reporting.vw_hoja_vida_bienes_inmuebles_total IS
'Vista reporting: bienes inmuebles declarados, con información geográfica, avalúos y auditoría.';

DROP VIEW IF EXISTS reporting.vw_hoja_vida_general_total;
CREATE OR REPLACE VIEW reporting.vw_hoja_vida_general_total AS
SELECT
    dp.id_datos_personal,
    dp.tipo_documento,
    dp.nombre_tipo_documento,
    dp.documento,
    dp.nombres,
    dp.primer_apellido,
    dp.segundo_apellido,
    dp.fecha_nacimiento,
    dp.nombre_genero,
    dp.nombre_estado_civil,
    dp.nombre_escolaridad,
    dp.nombre_tipo_vivienda,
    dp.nombre_ocupacion,
    dp.nombre_sector_economico,
    dp.nombre_actividad_ses,
    dp.nombre_actividad_dian,
    dp.estrato_social,
    dp.numero_hijos,
    dp.comentario,
    -- 🏠 Dirección
    u.direccion AS direccion_residencia,
    u.barrio AS barrio_residencia,
    u.nombre_pais AS pais_residencia,
    u.nombre_departamento AS departamento_residencia,
    u.nombre_ciudad AS ciudad_residencia,
    u.nombre_zona AS zona_residencia,
    u.nombre_sub_zona AS sub_zona_residencia,
    u.celular_uno,
    u.celular_dos,
    u.correo AS correo_principal,
    -- 💼 Información laboral
    l.nombre_empresa,
    l.nombre_tipo_contrato,
    l.nombre_tipo_empresa,
    l.nombre_jornada,
    l.fecha_vinculacion,
    l.empleado_entidad,
    -- 💰 Información financiera
    f.valor_salario,
    f.valor_pension,
    f.total_activos,
    f.total_pasivos,
    f.ingresos_arriendo,
    f.egresos_credito,
    f.relacion_financiera,
    -- 🏡 Bienes
    b.descripcion_bien_inmueble,
    b.valor_comercial,
    b.hipoteca_entidad,
    b.valor_asegurado,
    -- 🔐 SARLAFT
    s.asociado_peps,
    s.nombre_tipo_peps,
    s.nombre_familia_peps,
    s.moneda_extranjera,
    s.cuenta_extranjero,
    -- 📞 Permisos especiales
    pe.recibe_llamadas,
    pe.recibe_msm,
    pe.recibe_emails,
    pe.recibe_cartas,
    pe.recibe_redes_sociales,
    -- 🕓 Auditoría
    dp.fecha_creacion AS fecha_creacion_datos,
    dp.fecha_edicion AS fecha_edicion_datos
FROM reporting.vw_hoja_vida_datos_personales_total dp
    LEFT JOIN reporting.vw_hoja_vida_ubicaciones_total u ON dp.id_datos_personal = u.id_datos_personal
    LEFT JOIN reporting.vw_hoja_vida_laborales_total l ON dp.id_datos_personal = l.id_datos_personal
    LEFT JOIN reporting.vw_hoja_vida_financieros_total f ON dp.id_datos_personal = f.id_datos_personal
    LEFT JOIN reporting.vw_hoja_vida_bienes_inmuebles_total b ON dp.id_datos_personal = b.id_datos_personal
    LEFT JOIN reporting.vw_hoja_vida_sarlaft_total s ON dp.id_datos_personal = s.id_datos_personal
    LEFT JOIN reporting.vw_hoja_vida_permisos_especiales_total pe ON dp.id_datos_personal = pe.id_datos_personal;
COMMENT ON VIEW reporting.vw_hoja_vida_general_total IS
'Vista maestra consolidada de Hoja de Vida: integra información personal, ubicación, laboral, financiera, SARLAFT y permisos especiales. Ideal para formularios, reportes y exportaciones.';
