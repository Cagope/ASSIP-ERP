import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

type LoginRequest = { username: string; password: string };
type LoginResponse = { token: string };

export type CambiarPasswordRequest = {
  passwordActual: string;
  passwordNueva: string;
  passwordConfirmar?: string; // opcional
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  // apiBase YA incluye /api/v1
  private base = `${environment.apiBase}/auth`;

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/login`, payload);
  }

  cambiarPassword(payload: CambiarPasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/cambiar-password`, payload);
  }
}
