-- =====================================================================
-- 🚀 V053__depositos_vistas_cruzadas.sql
-- ---------------------------------------------------------------------
-- Vista consolidada de Cuentas de Ahorro (Datos personales + Depósitos)
--
-- Integra la información de las cuentas de ahorro con los datos del
-- titular, agencia, forma de ahorro y estado, para reportes y consultas.
--
-- Esquema: depositos
-- Vista:   vw_depositos_cuentas_ahorro_total
-- ---------------------------------------------------------------------
-- Autor: Carlos González Pérez
-- Empresa: ERP ASSIP SOLIDARIA Y FINANCIERA
-- Fecha: 2025-11-09
-- =====================================================================

CREATE OR REPLACE VIEW depositos.vw_depositos_cuentas_ahorro_total AS
SELECT
    -- 🔹 Identificadores principales
    ca.id_cuenta_ahorro,
    ca.codigo_cuenta,
    ca.id_datos_personal,
    dp.tipo_documento,
    dp.documento,
    dp.nombres,
    dp.primer_apellido,
    dp.segundo_apellido,
    CONCAT(dp.nombres, ' ', dp.primer_apellido, ' ', COALESCE(dp.segundo_apellido, '')) AS nombre_completo,

    -- 🔹 Datos de agencia
    ca.codigo_agencia,
    ca.nombre_agencia,

    -- 🔹 Forma y tipo de ahorro
    ca.codigo_forma,
    ca.nombre_forma_ahorro,
    ca.plazo_cuenta,
    ca.fecha_apertura_cuenta,
    ca.fecha_final_cuenta,

    -- 🔹 Estados y condiciones
    ca.estado_cuenta_cuenta AS codigo_estado,
    ca.nombre_estado_cuenta AS nombre_estado,
    ca.cuenta_activa,
    ca.cuenta_conjunta,
    ca.accion_conjunta,
    ca.nombre_accion_conjunta,

    -- 🔹 Libranza / retención / GMF
    ca.libranza_cuenta,
    ca.libranzatiempo_pago,
    ca.cuota_mensual_cuenta,
    ca.retencion_fuente_cuenta,
    ca.gmf_cuenta_cuenta,
    ca.nombre_tipo_gmf,

    -- 🔹 Saldos
    ca.saldo_inicial_cuenta,
    ca.saldo_actual_cuenta,

    -- 🔹 Auditoría y trazabilidad
    ca.fk_seguridad_creacion,
    ca.fecha_creacion,
    ca.fk_seguridad_edicion,
    ca.fecha_edicion

FROM depositos.vw_depositos_cuentas_ahorro_detalle ca
LEFT JOIN hoja_vida.datos_personales dp
    ON dp.id_datos_personal = ca.id_datos_personal;

COMMENT ON VIEW depositos.vw_depositos_cuentas_ahorro_total IS
'Vista consolidada: cuentas de ahorro + datos personales del titular (nombre, documento, agencia, forma, saldo, estado, etc.).';
