import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { CapturaState } from './captura-state.service';

// Guard para el paso "resumen": exige que el paso 0 (datos personales) sea válido.
export const CapturaGuard: CanActivateFn = (): boolean | UrlTree => {
  const state = inject(CapturaState);
  const router = inject(Router);
  const ok = !!state.stepValid()[0];
  return ok ? true : router.createUrlTree(['/hoja-vida/captura', 'datos-personales']);
};
