import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { SessionService } from './session.service';

/**
 * Protege las rutas: solo permite acceso si el usuario está autenticado.
 * Si no hay token, redirige al login.
 */
export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const session = inject(SessionService);

  if (session.isAuthenticated()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};
