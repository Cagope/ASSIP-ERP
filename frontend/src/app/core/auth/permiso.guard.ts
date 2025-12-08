import { Injectable } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from './session.service';

@Injectable({ providedIn: 'root' })
export class PermisoGuard {

  constructor(
    private session: SessionService,
    private router: Router
  ) {}

  canActivate: CanActivateFn = (route, state) => {
    const permisosRequeridos = route.data?.['permiso'] as string | undefined;

    // Si la ruta no exige permiso → permitir
    if (!permisosRequeridos) return true;

    // Si no está logueado → fuera
    if (!this.session.isAuthenticated()) {
      this.router.navigate(['/login']);
      return false;
    }

    // Validar permiso
    if (!this.session.tienePermiso(permisosRequeridos)) {
      this.router.navigate(['/acceso-denegado']); // luego creamos esta pantalla
      return false;
    }

    return true;
  };
}
