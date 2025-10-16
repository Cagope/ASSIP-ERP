BEGIN;

-- 1) Si existe la constraint que forzaba solo naturales, elimínala
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_dp_tipo_persona_natural') THEN
    ALTER TABLE hoja_vida.datos_personales
      DROP CONSTRAINT ck_dp_tipo_persona_natural;
  END IF;
END$$;

-- 2) Asegura dominio de tipo_persona ∈ {'1','2'} (si tu DDL original no lo dejó)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_dp_tipo_persona_in') THEN
    ALTER TABLE hoja_vida.datos_personales
      ADD CONSTRAINT ck_dp_tipo_persona_in
      CHECK (tipo_persona IN ('1','2'));
  END IF;
END$$;

-- 3) Cabeza de familia no aplica a jurídicas:
--    Si tipo_persona='2' => cabeza_familia debe ser '0' o NULL (preferimos '0').
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_dp_cabeza_familia_seguntipo') THEN
    ALTER TABLE hoja_vida.datos_personales
      ADD CONSTRAINT ck_dp_cabeza_familia_seguntipo
      CHECK (
        tipo_persona = '1'
        OR (tipo_persona = '2' AND (cabeza_familia IS NULL OR cabeza_familia = '0'))
      );
  END IF;
END$$;

COMMIT;
