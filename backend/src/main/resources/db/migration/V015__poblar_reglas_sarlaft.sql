-- ============================================================
-- V015 — Poblar reglas SARLAFT iniciales
-- Sistema: ASSIP-ERP
-- Esquema: general
-- Autor: ChatGPT + Carlos G.
-- Fecha: 2025-11-XX
-- ============================================================

-- ============================================================
-- ⚠️ Precondición:
-- Debe existir la tabla general.reglas_sarlaft creada en V014
-- ============================================================

INSERT INTO general.reglas_sarlaft (
    codigo,
    descripcion,
    valor_numerico,
    valor_texto,
    severidad,
    codigo_modulo,
    activo
)
VALUES
-- ============================================================
-- REGLA 1: Datos desactualizados (DÍAS)
-- ============================================================
(
    'DATOS_DESACTUALIZADOS',
    'Detecta cuando la información personal supera el límite máximo de días sin actualización.',
    360,         -- límite estándar en días
    NULL,
    'ROJO',
    NULL,        -- Global, aplica en todos los módulos
    TRUE
),

-- ============================================================
-- REGLA 2: Documento NO coincide con edad
-- ============================================================
(
    'DOCUMENTO_NO_CORRESPONDE_EDAD',
    'Validación de coherencia entre edad y tipo de documento.',
    NULL,
    NULL,
    'ROJO',
    NULL,        -- Global
    TRUE
),

-- ============================================================
-- REGLA 3: Forma 03 usada por mayores del límite permitido
-- ============================================================
(
    'FORMA_03_MAYOR_EDAD',
    'Detecta cuando la forma de ahorro 03 es usada por personas mayores al límite permitido.',
    14,          -- edad máxima permitida
    '03',        -- valor referente a la forma de ahorro
    'ROJO',
    '02',        -- módulo depósitos
    TRUE
);

-- ============================================================
-- FIN DEL ARCHIVO V015
-- ============================================================
