import {
  inject
} from '@angular/core';

import {
  CanActivateFn,
  Router
} from '@angular/router';

import {
  SessionService
} from './session.service';

/**
 * Protege rutas que requieren un permiso funcional.
 *
 * El permiso requerido se declara en:
 *
 * data: {
 *   permiso: 'CODIGO_PERMISO'
 * }
 *
 * ADMIN conserva acceso total mediante
 * SessionService.tienePermiso().
 */
export const permisoGuard: CanActivateFn = (
  route
) => {

  const router =
    inject(Router);

  const session =
    inject(SessionService);

  /*
   * Primero debe existir una sesión autenticada.
   */
  if (!session.isAuthenticated()) {

    return router.createUrlTree([
      '/login'
    ]);
  }

  const permisoRequerido =
    route.data?.['permiso'] as
      string | undefined;

  /*
   * Si la ruta no declara un permiso específico,
   * este guard no agrega restricciones adicionales.
   */
  if (!permisoRequerido) {
    return true;
  }

  /*
   * La resolución de permisos pertenece al
   * SessionService.
   *
   * Esto incluye el acceso total de ADMIN.
   */
  if (
    session.tienePermiso(
      permisoRequerido
    )
  ) {
    return true;
  }

  return router.createUrlTree([
    '/acceso-denegado'
  ]);
};
