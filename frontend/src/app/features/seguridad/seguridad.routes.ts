// src/app/features/seguridad/seguridad.routes.ts

import { Routes } from '@angular/router';

import { permisoGuard } from '../../core/auth/permiso.guard';

import { USUARIOS_ROUTES } from './usuarios/usuarios.routes';
import { ROLES_ROUTES } from './roles/roles.routes';
import { PERMISOS_ROUTES } from './permisos/permisos.routes';

export const SEGURIDAD_ROUTES: Routes = [

  // =========================================================
  // USUARIOS
  // =========================================================

  {
    path: 'usuarios',
    canActivate: [permisoGuard],
    data: {
      permiso: 'USUARIOS_VIEW'
    },
    children: USUARIOS_ROUTES
  },

  // =========================================================
  // ROLES
  // =========================================================

  {
    path: 'roles',
    children: ROLES_ROUTES
  },

  // =========================================================
  // PERMISOS
  // =========================================================

  {
    path: 'permisos',
    children: PERMISOS_ROUTES
  }

];
