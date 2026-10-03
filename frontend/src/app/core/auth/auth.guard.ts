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
 * Protege las rutas que requieren una sesión autenticada.
 *
 * La autorización funcional por permisos corresponde
 * exclusivamente a permisoGuard.
 */
export const authGuard: CanActivateFn = () => {

  const router =
    inject(Router);

  const session =
    inject(SessionService);

  if (session.isAuthenticated()) {
    return true;
  }

  return router.createUrlTree([
    '/login'
  ]);
};
