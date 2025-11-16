-- ============================================================
-- V017 — Poblar reglas SARLAFT iniciales (Estructura nueva)
-- Sistema: ASSIP-ERP
-- Esquema: general
-- Autor: Carlos G. / ChatGPT
-- Fecha: 2025-11-XX
-- ============================================================

-- Este archivo asume que las tablas ya fueron reconstruidas en V016.
-- Auditoría unificada:
--   fk_seguridad_creacion
--   fecha_creacion
--   fk_seguridad_edicion
--   fecha_edicion
-- ============================================================

INSERT INTO general.reglas_sarlaft (
    codigo,
    descripcion,
    valor_numerico,
    valor_texto,
    severidad,
    codigo_modulo,
    activo,
    fk_seguridad_creacion,
    fk_seguridad_edicion
)
VALUES
-- ============================================================
-- REGLA 1: Datos desactualizados (DÍAS)
-- ============================================================
(
    'DATOS_DESACTUALIZADOS',
    'Detecta cuando la información personal supera el límite máximo de días sin actualización.',
    360,        -- límite estándar en días
    NULL,
    'ROJO',
    NULL,       -- Global
    TRUE,
    1,          -- Usuario auditoría por defecto
    1
),

-- ============================================================
-- REGLA 2: Documento NO corresponde con edad
-- ============================================================
(
    'DOCUMENTO_NO_CORRESPONDE_EDAD',
    'Validación de coherencia entre edad y tipo de documento.',
    NULL,
    NULL,
    'ROJO',
    NULL,
    TRUE,
    1,
    1
),

-- ============================================================
-- REGLA 3: Forma 03 usada por mayores del límite permitido
-- ============================================================
(
    'FORMA_03_MAYOR_EDAD',
    'Detecta cuando la forma de ahorro 03 es usada por personas mayores al límite permitido.',
    14,         -- edad máxima permitida
    '03',
    'ROJO',
    '02',       -- módulo depósitos
    TRUE,
    1,
    1
);

-- ============================================================
-- FIN DEL ARCHIVO V017
-- ============================================================
