BEGIN;

ALTER TABLE hoja_vida.datos_personales
  ALTER COLUMN tipo_persona    TYPE VARCHAR(1) USING TRIM(tipo_persona),
  ALTER COLUMN cabeza_familia  TYPE VARCHAR(1) USING TRIM(cabeza_familia);

COMMIT;
