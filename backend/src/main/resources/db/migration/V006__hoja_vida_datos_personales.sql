-- V006__hoja_vida_datos_personales.sql
-- Ajustes para solo personas naturales, campos obligatorios, RUT/DV y reglas de fechas.

BEGIN;

-- 1) Campo nuevo: tiene_rut
ALTER TABLE hoja_vida.datos_personales
  ADD COLUMN IF NOT EXISTS tiene_rut BOOLEAN NOT NULL DEFAULT FALSE;

-- 2) Obligatorios (según decisiones):
--    - tipo_documento (obligatorio)
--    - fecha_documento (obligatorio)
--    - fecha_nacimiento (obligatorio)
--    - id_ciudad_expedicion (obligatorio)
--    - id_ciudad_nacimiento (obligatorio)
ALTER TABLE hoja_vida.datos_personales
  ALTER COLUMN tipo_documento SET NOT NULL,
  ALTER COLUMN fecha_documento SET NOT NULL,
  ALTER COLUMN fecha_nacimiento SET NOT NULL,
  ALTER COLUMN id_ciudad_expedicion SET NOT NULL,
  ALTER COLUMN id_ciudad_nacimiento SET NOT NULL;

-- 3) Solo personas naturales: tipo_persona = '1'
--    Aseguramos default y constraint.
ALTER TABLE hoja_vida.datos_personales
  ALTER COLUMN tipo_persona SET DEFAULT '1';

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'ck_dp_tipo_persona_natural'
  ) THEN
    ALTER TABLE hoja_vida.datos_personales
      ADD CONSTRAINT ck_dp_tipo_persona_natural
      CHECK (tipo_persona = '1');
  END IF;
END$$;

-- 4) Unicidad del identificador
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'uk_dp_tipo_doc_documento'
  ) THEN
    ALTER TABLE hoja_vida.datos_personales
      ADD CONSTRAINT uk_dp_tipo_doc_documento
      UNIQUE (tipo_documento, documento);
  END IF;
END$$;

-- 5) Reglas de fechas:
--    - fecha_documento >= fecha_nacimiento
--    - fecha_apertura >= fecha_nacimiento (si se informa; la columna ya tiene DEFAULT CURRENT_DATE)
--    - fecha_nacimiento <= CURRENT_DATE
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'ck_dp_fechas'
  ) THEN
    ALTER TABLE hoja_vida.datos_personales
      ADD CONSTRAINT ck_dp_fechas
      CHECK (
        fecha_documento >= fecha_nacimiento
        AND (fecha_apertura IS NULL OR fecha_apertura >= fecha_nacimiento)
        AND fecha_nacimiento <= CURRENT_DATE
      );
  END IF;
END$$;

-- 6) Regla RUT/DV:
--    - Si tiene_rut = FALSE -> digito_verificacion debe ser NULL
--    - Si tiene_rut = TRUE  -> digito_verificacion debe ser 1-2 dígitos
--    (La verificación de coherencia exacta del DV con el documento se hará en servicio;
--     si luego se desea en DB, se puede agregar una función y otro CHECK.)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'ck_dp_rut_dv_condicion'
  ) THEN
    ALTER TABLE hoja_vida.datos_personales
      ADD CONSTRAINT ck_dp_rut_dv_condicion
      CHECK (
        (tiene_rut = FALSE AND digito_verificacion IS NULL)
        OR
        (tiene_rut = TRUE  AND digito_verificacion ~ '^[0-9]{1,2}$')
      );
  END IF;
END$$;

-- 7) Comentarios de reglas de negocio no codificadas en DB:
COMMENT ON COLUMN hoja_vida.datos_personales.cabeza_familia IS
  'Solo se captura para género femenino. Si es masculino, debe ser "0". (Validar en capa de aplicación)';

COMMENT ON COLUMN hoja_vida.datos_personales.id_ciudad_expedicion IS
  'Ciudad de expedición obligatoria. País y departamento son independientes (opcionales).';

COMMENT ON COLUMN hoja_vida.datos_personales.id_ciudad_nacimiento IS
  'Ciudad de nacimiento obligatoria. País y departamento son independientes (opcionales).';

COMMENT ON COLUMN hoja_vida.datos_personales.tiene_rut IS
  'Si TRUE, el dígito de verificación se calcula automáticamente con una función global (servicio).';

COMMIT;
