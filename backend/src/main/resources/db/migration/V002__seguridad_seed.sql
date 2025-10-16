-- =========================================================
-- Proyecto : ASSIP_ERP
-- Migración: V002__seguridad_seed.sql
-- Objetivo : Seed mínimo del esquema seguridad:
--            - Usuario superusuario SOLO de seguridad (admin)
--            - Catálogo de permisos de seguridad (para armar roles luego)
--            NOTA: Sin creación de roles ni asignaciones a agencias
-- =========================================================

SET search_path TO seguridad, public;

-- 1) Usuario superusuario de seguridad (admin)
--    Password: 'Assip2025!' (bcrypt). Obligará cambio por la bandera por defecto.
--    Puedes cambiar el hash más adelante desde backend (cuando implementemos cambio de clave).
--    Hash generado con bcrypt-10 (ejemplo).
WITH upsert_admin AS (
  INSERT INTO usuarios (username, password_hash, is_superuser_seguridad, activo)
  VALUES (
    'admin',
    '$2a$10$WZ9lq9lJXk3m4oZF9jQJeeRr4lA6pH6f7o9q1o2Y3ZyXy1wqJ8C2e', -- bcrypt('Assip2025!')
    TRUE,
    TRUE
  )
  ON CONFLICT (username) DO UPDATE
    SET activo = EXCLUDED.activo,
        is_superuser_seguridad = EXCLUDED.is_superuser_seguridad
  RETURNING id_usuario
)
SELECT 1;

-- 2) Catálogo de permisos del módulo SEGURIDAD
--    (Para que luego puedas crear Roles y asignar permisos desde la UI)
--    Idempotente: ON CONFLICT por 'codigo'
INSERT INTO permisos (codigo, descripcion, dominio, creado_por)
VALUES
  -- Usuarios
  ('seg.usuarios.view',           'Ver listado/detalle de usuarios',          'seg', 'seed'),
  ('seg.usuarios.create',         'Crear usuarios',                            'seg', 'seed'),
  ('seg.usuarios.edit',           'Editar usuarios',                           'seg', 'seed'),
  ('seg.usuarios.disable',        'Activar/Desactivar usuarios',               'seg', 'seed'),
  ('seg.usuarios.reset_password', 'Resetear contraseña de usuario',            'seg', 'seed'),
  ('seg.usuarios.force_pw_change','Forzar cambio de contraseña en login',      'seg', 'seed'),

  -- Roles
  ('seg.roles.view',              'Ver roles',                                  'seg', 'seed'),
  ('seg.roles.create',            'Crear roles',                                 'seg', 'seed'),
  ('seg.roles.edit',              'Editar roles',                                'seg', 'seed'),
  ('seg.roles.disable',           'Activar/Desactivar roles',                    'seg', 'seed'),
  ('seg.roles.assign',            'Asignar permisos a roles',                    'seg', 'seed'),

  -- Asignaciones Usuario↔Agencia
  ('seg.asignaciones.view',       'Ver asignaciones de usuario↔agencia',         'seg', 'seed'),
  ('seg.asignaciones.create',     'Crear asignación usuario↔agencia',            'seg', 'seed'),
  ('seg.asignaciones.change_role','Cambiar rol en una agencia',                  'seg', 'seed'),
  ('seg.asignaciones.mark_default','Marcar agencia por defecto de usuario',      'seg', 'seed'),
  ('seg.asignaciones.disable',    'Desactivar asignación',                       'seg', 'seed'),
  ('seg.asignaciones.delete',     'Eliminar asignación',                         'seg', 'seed'),

  -- Políticas de seguridad
  ('seg.politicas.password_update','Actualizar política de contraseñas',        'seg', 'seed'),
  ('seg.politicas.mfa_update',     'Actualizar política MFA',                   'seg', 'seed'),
  ('seg.politicas.sesion_update',  'Actualizar política de sesiones',           'seg', 'seed'),

  -- Auditoría (consultas/export)
  ('seg.auditoria.view',          'Consultar vistas de auditoría de seguridad', 'seg', 'seed'),
  ('seg.auditoria.export',        'Exportar resultados de auditoría',           'seg', 'seed')
ON CONFLICT (codigo) DO NOTHING;

-- 3) Registrar en log_eventos (opcional informativo)
INSERT INTO log_eventos (accion, resultado, entidad, username, motivo, extra_json)
VALUES
  ('SEG.USUARIOS.CREAR_OK', 'OK', 'usuario', 'admin', 'Seed de superusuario de seguridad',
   '{"nota":"is_superuser_seguridad=true, debe_cambiar_password=true por defecto"}'::jsonb),
  ('SEG.PERMISOS.SEED_OK',  'OK', 'permiso', 'admin', 'Seed de catálogo de permisos de seguridad',
   '{"count_aproximado": 22}'::jsonb);
