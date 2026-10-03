import {
  HttpInterceptorFn,
  HttpErrorResponse,
  HttpRequest
} from '@angular/common/http';

import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { SessionService } from './session.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const session = inject(SessionService);
  const router = inject(Router);

  const token = session.getToken();

  // =========================================================
  // IDENTIFICAR PETICIONES
  // =========================================================

  const isLoginRequest =
    req.url.includes('/auth/login');

  const isCambiarPasswordRequest =
    req.url.includes('/auth/cambiar-password');

  let modifiedReq: HttpRequest<any> = req;

  // =========================================================
  // AGREGAR TOKEN
  // =========================================================

  if (!isLoginRequest && token) {

    modifiedReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

  }

  // =========================================================
  // MANEJO GLOBAL DE ERRORES
  // =========================================================

  return next(modifiedReq).pipe(

    catchError((error: HttpErrorResponse) => {

      if (error.status === 401) {

        // El formulario de cambio de contraseña
        // mostrará su propio mensaje de error.
        if (isCambiarPasswordRequest) {
          return throwError(() => error);
        }

        // Para las demás peticiones, conservar
        // el comportamiento de sesión vencida.
        session.logout();
        router.navigate(['/login']);

      }

      return throwError(() => error);

    })

  );

};
