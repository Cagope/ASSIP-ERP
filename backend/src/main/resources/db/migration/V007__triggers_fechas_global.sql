-- ============================================================
-- 📘 ASSIP-ERP - V007__triggers_fechas_global.sql
-- ============================================================
-- Objetivo:
--   Definir funciones y triggers PL/pgSQL globales para manejo
--   estandarizado de fechas y auditoría en todos los esquemas
--   del sistema ASSIP-ERP.
--   Incluye índices recomendados para optimizar rendimiento.
-- ============================================================

-- ============================================================
-- 1️⃣ Función: fn_set_fechas_auditoria
-- ============================================================
CREATE OR REPLACE FUNCTION fn_set_fechas_auditoria()
RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    NEW.fecha_creacion := COALESCE(NEW.fecha_creacion, CURRENT_TIMESTAMP);
    NEW.fecha_edicion := COALESCE(NEW.fecha_edicion, CURRENT_TIMESTAMP);
  ELSIF TG_OP = 'UPDATE' THEN
    NEW.fecha_edicion := CURRENT_TIMESTAMP;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- 2️⃣ Función: fn_auto_fecha_edicion
-- ============================================================
CREATE OR REPLACE FUNCTION fn_auto_fecha_edicion()
RETURNS TRIGGER AS $$
BEGIN
  NEW.fecha_edicion := CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- 3️⃣ Función: fn_sync_fecha_actualizacion
-- ============================================================
CREATE OR REPLACE FUNCTION fn_sync_fecha_actualizacion()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE hoja_vida.datos_personales
  SET
    fecha_actualizacion = CURRENT_DATE,
    fecha_edicion = CURRENT_TIMESTAMP
  WHERE id_datos_personal = NEW.id_datos_personal;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- 4️⃣ Función: fn_update_fechas_relacionadas
-- ============================================================
CREATE OR REPLACE FUNCTION fn_update_fechas_relacionadas()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE general.datos_agencias
  SET fecha_edicion = CURRENT_TIMESTAMP
  WHERE id_agencia = NEW.id_agencia;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- 5️⃣ Función: fn_fix_fechas_nulas
-- ============================================================
CREATE OR REPLACE FUNCTION fn_fix_fechas_nulas()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.fecha_creacion IS NULL THEN
    NEW.fecha_creacion := CURRENT_TIMESTAMP;
  END IF;
  IF NEW.fecha_edicion IS NULL THEN
    NEW.fecha_edicion := CURRENT_TIMESTAMP;
  END IF;
  IF NEW.fecha_actualizacion IS NULL THEN
    NEW.fecha_actualizacion := CURRENT_DATE;
  END IF;
  IF NEW.fecha_apertura IS NULL THEN
    NEW.fecha_apertura := CURRENT_DATE;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- 6️⃣ Función: fn_log_cambio_fechas
-- ============================================================
CREATE OR REPLACE FUNCTION fn_log_cambio_fechas()
RETURNS TRIGGER AS $$
DECLARE
  v_usuario INTEGER;
BEGIN
  v_usuario := COALESCE(current_setting('app.user_id', true)::INTEGER, NULL);

  IF (OLD.fecha_actualizacion IS DISTINCT FROM NEW.fecha_actualizacion)
     OR (OLD.fecha_edicion IS DISTINCT FROM NEW.fecha_edicion)
     OR (OLD.fecha_apertura IS DISTINCT FROM NEW.fecha_apertura) THEN

    INSERT INTO seguridad.log_evento (
      esquema, tabla, id_registro, descripcion_evento, fecha_evento, id_usuario
    )
    VALUES (
      TG_TABLE_SCHEMA,
      TG_TABLE_NAME,
      COALESCE(NEW.id_datos_personal, NEW.id_agencia),
      'Cambio de fechas en ' || TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME,
      CURRENT_TIMESTAMP,
      v_usuario
    );
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- 7️⃣ Función: fn_propagate_fechas_agencia
-- ============================================================
CREATE OR REPLACE FUNCTION fn_propagate_fechas_agencia()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE contabilidad.comprobantes
  SET fecha_edicion = CURRENT_TIMESTAMP
  WHERE id_agencia = NEW.id_agencia;

  UPDATE depositos.cuentas
  SET fecha_edicion = CURRENT_TIMESTAMP
  WHERE id_agencia = NEW.id_agencia;

  UPDATE cartera.creditos
  SET fecha_edicion = CURRENT_TIMESTAMP
  WHERE id_agencia = NEW.id_agencia;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- 8️⃣ Índices recomendados
-- ============================================================

-- 🔹 Índices para búsquedas y ordenamientos por fechas
CREATE INDEX IF NOT EXISTS idx_personales_fecha_actualizacion
  ON hoja_vida.datos_personales (fecha_actualizacion DESC);

CREATE INDEX IF NOT EXISTS idx_personales_fecha_edicion
  ON hoja_vida.datos_personales (fecha_edicion DESC);

CREATE INDEX IF NOT EXISTS idx_agencias_fecha_edicion
  ON general.datos_agencias (fecha_edicion DESC);

CREATE INDEX IF NOT EXISTS idx_log_evento_fecha_evento
  ON seguridad.log_evento (fecha_evento DESC);

-- 🔹 Índices de relación (optimizan los triggers y joins)
CREATE INDEX IF NOT EXISTS idx_ubicaciones_id_datos_personal
  ON hoja_vida.ubicaciones (id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_familiares_id_datos_personal
  ON hoja_vida.datos_familiares (id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_laborales_id_datos_personal
  ON hoja_vida.laborales (id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_financieros_id_datos_personal
  ON hoja_vida.financieros (id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_sarlaft_id_datos_personal
  ON hoja_vida.sarlaft (id_datos_personal);

CREATE INDEX IF NOT EXISTS idx_zonas_id
  ON general.zonas (id_zona);

CREATE INDEX IF NOT EXISTS idx_sub_zonas_id_zona
  ON general.sub_zonas (id_zona);

-- ============================================================
-- 🔒 FIN DEL ARCHIVO V007__triggers_fechas_global.sql
-- ============================================================
