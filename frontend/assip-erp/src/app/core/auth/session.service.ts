// src/app/core/auth/session.service.ts
import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, tap } from 'rxjs';

export interface MeResponse {
  idUsuario: number;
  username: string;
  // Si tu backend lo envía:
  debeCambiarPassword?: boolean;
  // Agrega aquí campos extra que devuelva /auth/me
}

@Injectable({ providedIn: 'root' })
export class SessionService {
  private _me = signal<MeResponse | null>(null);

  constructor(private http: HttpClient) {}

  // ===== Estado de sesión (perfil) =====
  me(): MeResponse | null {
    return this._me();
  }
  setMeLocal(val: MeResponse | null): void {
    this._me.set(val);
  }

  // ===== Token en localStorage =====
  getToken(): string | null {
    return localStorage.getItem('token');
  }
  setToken(token: string): void {
    localStorage.setItem('token', token);
  }
  clearToken(): void {
    localStorage.removeItem('token');
  }

  // ===== Cerrar sesión =====
  logout(): void {
    this.clearToken();
    this.setMeLocal(null);
  }

  // ===== Cargar /auth/me =====
  loadMe(): Observable<MeResponse> {
    const url = `${environment.apiBase}/auth/me`;
    return this.http.get<MeResponse>(url).pipe(
      tap((me) => this.setMeLocal(me))
    );
    // NOTA: El interceptor agregará Authorization si hay token
  }
}
