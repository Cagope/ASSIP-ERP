// src/app/core/http/auth.interceptor.ts
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { SessionService } from '../auth/session.service';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  const router  = inject(Router);

  const token   = session.getToken();
  const isApi   = req.url.startsWith(environment.apiBase);                  // http://localhost:8080/api/v1
  const isLogin = req.url.startsWith(`${environment.apiBase}/auth/login`); // solo login es público
  const isMe    = req.url.startsWith(`${environment.apiBase}/auth/me`);

  // Adjunta Authorization a toda la API excepto /auth/login
  let authReq = req;
  if (isApi && !isLogin && token) {
    authReq = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      // SOLO invalidar sesión si /auth/me devuelve 401
      if (isMe && err.status === 401) {
        session.logout();
        router.navigateByUrl('/login');
      }
      return throwError(() => err);
    })
  );
};
