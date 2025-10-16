// src/app/core/guards/auth.guard.ts
import { CanMatchFn, Router, UrlSegment } from '@angular/router';
import { inject } from '@angular/core';
import { SessionService } from '../auth/session.service';

/** Construye la URL que el usuario intentó abrir, a partir de los segmentos. */
function buildAttemptedUrl(segments: UrlSegment[]): string {
  const path = '/' + segments.map(s => s.path).join('/');
  return path || '/';
}

export const authGuard: CanMatchFn = (route, segments) => {
  const router  = inject(Router);
  const session = inject(SessionService);

  // Considera logueado si hay token (ajústalo si tienes expiración/validación)
  const isLoggedIn = !!session.getToken();

  if (isLoggedIn) return true;

  const returnUrl = buildAttemptedUrl(segments);
  return router.createUrlTree(['/login'], { queryParams: { returnUrl } });
};
