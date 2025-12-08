import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LoginResponse {
  token: string;
  username: string;
  rol: string | null;
  agencias?: Array<{
    id_agencia: number;
    codigo_agencia: string;
    nombre_agencia: string;
  }>;
  permisos?: string[];  // ← opcional
}

export interface MeResponse {
  idUsuario: number;
  username: string;
  rol: string | null;
  agencias: Array<{
    id_agencia: number;
    codigo_agencia: string;
    nombre_agencia: string;
  }>;
  permisos: string[];   // ← ✔ AGREGADO
}


@Injectable({ providedIn: 'root' })
export class AuthApi {
  private baseUrl = 'http://localhost:8080/api/v1/auth';

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<LoginResponse> {
    const body = { username, password };
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, body);
  }

  me(): Observable<MeResponse> {
    return this.http.get<MeResponse>(`${this.baseUrl}/me`);
  }
}
