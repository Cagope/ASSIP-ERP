/* ================================================================
📘 Migración: V052__depositos_views.sql
Esquema: depositos
Autor: Carlos González Pérez — ERP ASSIP SOLIDARIA Y FINANCIERA
Fecha: 2025-11-09
--------------------------------------------------------------------
🔹 Descripción:
Define las vistas principales del esquema DEPÓSITOS
para consultas operativas, contables y financieras.
Incluye vínculos con general, contabilidad y catálogos,
sin duplicar datos de hoja_vida (se cruza con reporting).
================================================================ */

SET search_path = depositos, general, contabilidad, catalogos, seguridad, public;

/* ================================================================
1️⃣ VW — FORMAS DE AHORRO
================================================================ */
DROP VIEW IF EXISTS depositos.vw_depositos_formas_ahorro_total;
CREATE OR REPLACE VIEW depositos.vw_depositos_formas_ahorro_total AS
SELECT
    f.id_forma_ahorro,
    f.codigo_forma,
    f.nombre_forma,
    f.tipo_captacion_forma,
    tc.descripcion_captacion AS nombre_tipo_captacion,
    f.tiempo_liquidacion,
    f.tasa_interes_forma,
    f.valor_minimo,
    f.autorizado_forma,
    f.fecha_ultima_liquidacion,
    f.documento_forma,
    ds.descripcion_soporte AS nombre_documento_soporte,
    f.cuenta_forma_corto,
    cc1.nombre_cuenta AS nombre_cuenta_corto,
    f.cuenta_forma_largo,
    cc2.nombre_cuenta AS nombre_cuenta_largo,
    f.cuenta_gasto,
    cc3.nombre_cuenta AS nombre_cuenta_gasto,
    f.cuenta_cxp_forma,
    cc4.nombre_cuenta AS nombre_cuenta_cxp,
    f.cuenta_gmf_forma,
    cc5.nombre_cuenta AS nombre_cuenta_gmf,
    f.fk_seguridad_creacion,
    f.fecha_creacion,
    f.fk_seguridad_edicion,
    f.fecha_edicion
FROM depositos.formas_ahorro f
    LEFT JOIN depositos.tipos_captaciones tc ON f.tipo_captacion_forma = tc.codigo_captacion
    LEFT JOIN depositos.tipos_documentos_soporte ds ON f.documento_forma = ds.codigo_soporte
    LEFT JOIN contabilidad.catalogo_cuentas cc1 ON f.cuenta_forma_corto = cc1.id_catalogo_cuenta
    LEFT JOIN contabilidad.catalogo_cuentas cc2 ON f.cuenta_forma_largo = cc2.id_catalogo_cuenta
    LEFT JOIN contabilidad.catalogo_cuentas cc3 ON f.cuenta_gasto = cc3.id_catalogo_cuenta
    LEFT JOIN contabilidad.catalogo_cuentas cc4 ON f.cuenta_cxp_forma = cc4.id_catalogo_cuenta
    LEFT JOIN contabilidad.catalogo_cuentas cc5 ON f.cuenta_gmf_forma = cc5.id_catalogo_cuenta;
COMMENT ON VIEW depositos.vw_depositos_formas_ahorro_total IS
'Vista depositos: formas de ahorro con vínculos contables y catálogos.';

/* ================================================================
2️⃣ VW — CUENTAS DE AHORRO (detalle general)
================================================================ */
DROP VIEW IF EXISTS depositos.vw_depositos_cuentas_ahorro_detalle;
CREATE OR REPLACE VIEW depositos.vw_depositos_cuentas_ahorro_detalle AS
SELECT
    c.id_cuenta_ahorro,
    c.codigo_cuenta,
    c.id_datos_personal,
    c.codigo_agencia,
    a.nombre_agencia,
    c.codigo_forma,
    f.nombre_forma AS nombre_forma_ahorro,
    c.fecha_apertura_cuenta,
    c.saldo_inicial_cuenta,
    c.saldo_actual_cuenta,
    c.estado_cuenta_cuenta,
    ea.descripcion_estado_ahorro AS nombre_estado_cuenta,
    c.fecha_estado_cuenta,
    c.gmf_cuenta_cuenta,
    tg.descripcion_tipos_gmf AS nombre_tipo_gmf,
    c.libranza_cuenta,
    c.libranzatiempo_pago,
    c.cuota_mensual_cuenta,
    c.retencion_fuente_cuenta,
    c.plazo_cuenta,
    c.fecha_final_cuenta,
    c.cuenta_activa,
    c.cuenta_conjunta,
    c.accion_conjunta,
    acc.descripcion_accion AS nombre_accion_conjunta,
    c.fk_seguridad_creacion,
    c.fecha_creacion,
    c.fk_seguridad_edicion,
    c.fecha_edicion
FROM depositos.cuentas_ahorro c
    LEFT JOIN depositos.formas_ahorro f ON c.codigo_forma = f.id_forma_ahorro
    LEFT JOIN general.datos_agencias a ON c.codigo_agencia = a.id_agencia
    LEFT JOIN depositos.estados_ahorros ea ON c.estado_cuenta_cuenta = ea.codigo_estado_ahorro
    LEFT JOIN depositos.tipos_gmf tg ON c.gmf_cuenta_cuenta = tg.codigo_tipo_gmf
    LEFT JOIN depositos.acciones_cuentas_conjuntas acc ON c.accion_conjunta = acc.codigo_accion;
COMMENT ON VIEW depositos.vw_depositos_cuentas_ahorro_detalle IS
'Vista depositos: cuentas de ahorro con información de agencia, forma, estado y GMF.';

/* ================================================================
3️⃣ VW — CUENTAS CONJUNTAS
================================================================ */
DROP VIEW IF EXISTS depositos.vw_depositos_cuentas_conjuntas_detalle;
CREATE OR REPLACE VIEW depositos.vw_depositos_cuentas_conjuntas_detalle AS
SELECT
    cc.id_cuenta_conjunta,
    cc.id_cuenta_ahorro,
    cc.id_datos_personal,
    c.codigo_cuenta,
    f.nombre_forma,
    cc.fk_seguridad_creacion,
    cc.fecha_creacion,
    cc.fk_seguridad_edicion,
    cc.fecha_edicion
FROM depositos.cuentas_ahorro_conjuntas cc
    LEFT JOIN depositos.cuentas_ahorro c ON cc.id_cuenta_ahorro = c.id_cuenta_ahorro
    LEFT JOIN depositos.formas_ahorro f ON c.codigo_forma = f.id_forma_ahorro;
COMMENT ON VIEW depositos.vw_depositos_cuentas_conjuntas_detalle IS
'Vista depositos: cotitulares o apoderados de cuentas de ahorro.';

/* ================================================================
4️⃣ VW — EXTRACTOS Y MOVIMIENTOS
================================================================ */
DROP VIEW IF EXISTS depositos.vw_depositos_extractos_cuentas_detalle;
CREATE OR REPLACE VIEW depositos.vw_depositos_extractos_cuentas_detalle AS
SELECT
    e.id_extracto_cuenta_ahorro,
    e.id_cuenta_ahorro,
    c.codigo_cuenta,
    e.fecha_movimiento,
    e.hora_movimiento,
    e.tipo_comprobante,
    e.numero_comprobante,
    e.tipo_movimiento,
    tm.descripcion AS nombre_tipo_movimiento,
    tm.accion_movimiento,
    tm.genera_gmf,
    e.valor_debito,
    e.valor_credito,
    e.modulo,
    m.descripcion_modulo AS nombre_modulo,
    e.tarjeta,
    e.establecimiento,
    e.fk_seguridad_creacion,
    e.fecha_creacion,
    e.fk_seguridad_edicion,
    e.fecha_edicion
FROM depositos.extractos_cuentas_ahorros e
    LEFT JOIN depositos.cuentas_ahorro c ON e.id_cuenta_ahorro = c.id_cuenta_ahorro
    LEFT JOIN depositos.tipo_movimiento tm ON e.tipo_movimiento = tm.codigo_movimiento
    LEFT JOIN depositos.modulos m ON e.modulo = m.codigo_modulo;
COMMENT ON VIEW depositos.vw_depositos_extractos_cuentas_detalle IS
'Vista depositos: extractos y movimientos con detalle de tipo, módulo y valores.';

/* ================================================================
5️⃣ VW — BENEFICIARIOS
================================================================ */
DROP VIEW IF EXISTS depositos.vw_depositos_beneficiarios_total;
CREATE OR REPLACE VIEW depositos.vw_depositos_beneficiarios_total AS
SELECT
    b.id_beneficiario,
    b.id_cuenta_ahorro,
    c.codigo_cuenta,
    b.documento_beneficiario,
    b.nombre_beneficiario,
    b.telefono_beneficiario,
    b.celular_beneficiario,
    b.tipo_parentesco,
    p.nombre_parentesco,
    b.fk_seguridad_creacion,
    b.fecha_creacion,
    b.fk_seguridad_edicion,
    b.fecha_edicion
FROM depositos.beneficiarios_cuenta_ahorros b
    LEFT JOIN depositos.cuentas_ahorro c ON b.id_cuenta_ahorro = c.id_cuenta_ahorro
    LEFT JOIN catalogos.parentescos p ON b.tipo_parentesco = p.codigo_parentesco;
COMMENT ON VIEW depositos.vw_depositos_beneficiarios_total IS
'Vista depositos: beneficiarios por cuenta con parentesco y auditoría.';

/* ================================================================
6️⃣ VW — CANJES
================================================================ */
DROP VIEW IF EXISTS depositos.vw_depositos_canjes_total;
CREATE OR REPLACE VIEW depositos.vw_depositos_canjes_total AS
SELECT
    ca.id_canje_cuenta_ahorro,
    ca.id_cuenta_ahorro,
    c.codigo_cuenta,
    ca.fecha_ingreso,
    ca.valor_canje,
    ca.numero_cheque,
    ca.estado_canje,
    ca.valor_liberado,
    ca.fk_seguridad_creacion,
    ca.fecha_creacion,
    ca.fk_seguridad_edicion,
    ca.fecha_edicion
FROM depositos.canjes_cuentas_ahorros ca
    LEFT JOIN depositos.cuentas_ahorro c ON ca.id_cuenta_ahorro = c.id_cuenta_ahorro;
COMMENT ON VIEW depositos.vw_depositos_canjes_total IS
'Vista depositos: canjes en proceso y liberados asociados a cuentas.';

/* ================================================================
7️⃣ VW — DOCUMENTOS SOPORTE
================================================================ */
DROP VIEW IF EXISTS depositos.vw_depositos_documentos_soporte_total;
CREATE OR REPLACE VIEW depositos.vw_depositos_documentos_soporte_total AS
SELECT
    ds.id_documento_soporte,
    ds.id_cuenta_ahorro,
    c.codigo_cuenta,
    ds.tipo_documento_soporte,
    tds.descripcion_soporte AS nombre_tipo_documento,
    ds.numero_inicial,
    ds.numero_final,
    ds.fecha_entrega,
    ds.estado_documento,
    eds.descripcion_codigo_documento_soporte AS nombre_estado_documento,
    ds.fecha_estado,
    ds.fk_seguridad_creacion,
    ds.fecha_creacion,
    ds.fk_seguridad_edicion,
    ds.fecha_edicion
FROM depositos.documentos_soporte ds
    LEFT JOIN depositos.cuentas_ahorro c ON ds.id_cuenta_ahorro = c.id_cuenta_ahorro
    LEFT JOIN depositos.tipos_documentos_soporte tds ON ds.tipo_documento_soporte = tds.codigo_soporte
    LEFT JOIN depositos.estados_documentos_soporte eds ON ds.estado_documento = eds.codigo_estado_documento_soporte;
COMMENT ON VIEW depositos.vw_depositos_documentos_soporte_total IS
'Vista depositos: documentos soporte (chequeras/libretas) por cuenta de ahorro.';

/* ================================================================
8️⃣ VW — EMPRESAS LIBRANZA
================================================================ */
DROP VIEW IF EXISTS depositos.vw_depositos_empresas_libranza_total;
CREATE OR REPLACE VIEW depositos.vw_depositos_empresas_libranza_total AS
SELECT
    el.id_empresa_libranza,
    el.id_datos_personal,
    el.documento,
    el.cuenta_deudora,
    cc.nombre_cuenta AS nombre_cuenta_deudora,
    el.fk_seguridad_creacion,
    el.fecha_creacion,
    el.fk_seguridad_edicion,
    el.fecha_edicion
FROM depositos.empresas_libranza el
    LEFT JOIN contabilidad.catalogo_cuentas cc ON el.cuenta_deudora = cc.id_catalogo_cuenta;
COMMENT ON VIEW depositos.vw_depositos_empresas_libranza_total IS
'Vista depositos: empresas de libranza con su cuenta contable asociada.';

/* ================================================================
9️⃣ VW — GENERAL TOTAL
================================================================ */
DROP VIEW IF EXISTS depositos.vw_depositos_general_total;
CREATE OR REPLACE VIEW depositos.vw_depositos_general_total AS
SELECT
    c.id_cuenta_ahorro,
    c.codigo_cuenta,
    c.id_datos_personal,
    a.nombre_agencia,
    f.nombre_forma AS forma_ahorro,
    ea.descripcion_estado_ahorro AS estado_ahorro,
    c.saldo_actual_cuenta,
    c.fecha_apertura_cuenta,
    c.gmf_cuenta_cuenta,
    tg.descripcion_tipos_gmf AS tipo_gmf,
    COUNT(e.id_extracto_cuenta_ahorro) AS total_movimientos,
    SUM(e.valor_credito - e.valor_debito) AS neto_movimientos,
    c.fk_seguridad_creacion,
    c.fecha_creacion,
    c.fk_seguridad_edicion,
    c.fecha_edicion
FROM depositos.cuentas_ahorro c
    LEFT JOIN general.datos_agencias a ON c.codigo_agencia = a.id_agencia
    LEFT JOIN depositos.formas_ahorro f ON c.codigo_forma = f.id_forma_ahorro
    LEFT JOIN depositos.estados_ahorros ea ON c.estado_cuenta_cuenta = ea.codigo_estado_ahorro
    LEFT JOIN depositos.tipos_gmf tg ON c.gmf_cuenta_cuenta = tg.codigo_tipo_gmf
    LEFT JOIN depositos.extractos_cuentas_ahorros e ON c.id_cuenta_ahorro = e.id_cuenta_ahorro
GROUP BY
    c.id_cuenta_ahorro, c.codigo_cuenta, c.id_datos_personal, a.nombre_agencia,
    f.nombre_forma, ea.descripcion_estado_ahorro, c.saldo_actual_cuenta,
    c.fecha_apertura_cuenta, c.gmf_cuenta_cuenta, tg.descripcion_tipos_gmf,
    c.fk_seguridad_creacion, c.fecha_creacion, c.fk_seguridad_edicion, c.fecha_edicion;
COMMENT ON VIEW depositos.vw_depositos_general_total IS
'Vista maestra de depósitos: cuentas de ahorro con su forma, estado, saldo y resumen de movimientos.';

/* ================================================================
✅ FIN DE MIGRACIÓN
================================================================ */
