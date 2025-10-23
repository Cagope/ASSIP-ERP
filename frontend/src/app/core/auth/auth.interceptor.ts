import { HttpInterceptorFn, HttpErrorResponse, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { SessionService } from './session.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  const router = inject(Router);

  // ✅ Obtener token actual
  const token = session.getToken();

  // 🚫 No modificar las peticiones al login
  const isAuthRequest =
    req.url.includes('/auth/login');

  let modifiedReq: HttpRequest<any> = req;

  // ✅ Agregar encabezado Authorization a todo excepto /login
  if (!isAuthRequest && token) {
    modifiedReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  // ⚙️ Manejar errores globalmente (token expirado)
  return next(modifiedReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        session.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
