import {
  inject
} from '@angular/core';
import { map } from 'rxjs';

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

  return session.validarContexto().pipe(
    map(valido => {
      if (!valido) {
        return router.createUrlTree([
          session.isAuthenticated() ? '/acceso-denegado' : '/login'
        ]);
      }
      if (!permisoRequerido || session.tienePermiso(permisoRequerido)) {
        return true;
      }
      return router.createUrlTree(['/acceso-denegado']);
    })
  );
};
