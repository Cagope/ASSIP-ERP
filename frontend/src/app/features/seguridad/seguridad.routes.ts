// src/app/features/seguridad/seguridad.routes.ts

import { Routes } from '@angular/router';

// 🧑‍💼 Submódulo: Usuarios
import { USUARIOS_ROUTES } from './usuarios/usuarios.routes';
import { PermisoGuard } from '../../core/auth/permiso.guard';   // ⭐ NUEVO

export const SEGURIDAD_ROUTES: Routes = [

  // 👤 Gestión de usuarios
  {
    path: 'usuarios',
    canActivate: [PermisoGuard],               // ⭐ PROTEGE TODAS LAS RUTAS DE USUARIOS
    data: { permiso: 'USUARIOS_VIEW' },        // ⭐ PERMISO REQUERIDO
    children: USUARIOS_ROUTES
  }

  // En el futuro:
  // {
  //   path: 'roles',
  //   children: rolesRoutes
  // },
  // {
  //   path: 'permisos',
  //   children: permisosRoutes
  // }
];
