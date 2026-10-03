import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AuthRol {
  idRol: number;
  nombreRol: string;
}

export interface AuthAgencia {
  id_agencia?: number;
  codigo_agencia?: string;
  nombre_agencia?: string;

  idAgencia?: number;
  codigoAgencia?: string;
  nombreAgencia?: string;
}

/**
 * Respuesta inicial del login.
 */
export interface LoginResponse {
  token: string;
  username: string;
  requiereCambioPassword: boolean;

  estadoVigenciaPassword:
    | 'VIGENTE'
    | 'PROXIMA_VENCER'
    | 'VENCE_HOY';

  diasRestantesPassword: number;
  fechaVencimientoPassword: string;
  mostrarAvisoVencimientoPassword: boolean;
}

/**
 * Contexto funcional del usuario autenticado.
 */
export interface MeResponse {
  idUsuario: number;
  username: string;
  roles: AuthRol[];
  agencias: AuthAgencia[];
  permisos: string[];
}

/**
 * Estado de acceso previo a la autenticación.
 *
 * Las propiedades definitivas se verificarán
 * contra AuthService.java.
 */
export interface EstadoAccesoResponse {
  idUsuario: number;
  username: string;
  estado: 'PENDIENTE_ACTIVACION' | 'ACTIVO';
  longitudMinimaPassword: number;
}

/**
 * Solicitud de activación inicial.
 */
export interface ActivarCuentaRequest {
  username: string;
  codigo: string;
  passwordNueva: string;
}

/**
 * Respuesta de activación.
 */
export interface ActivarCuentaResponse {
  mensaje: string;
  username: string;
}

/**
 * Solicitud de recuperación de contraseña.
 */
export interface RecuperarPasswordRequest {
  username: string;
  codigo: string;
  passwordNueva: string;
}

/**
 * Respuesta de recuperación de contraseña.
 */
export interface RecuperarPasswordResponse {
  mensaje: string;
  username: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthApi {

  private readonly baseUrl =
    'http://localhost:8080/api/v1/auth';

  constructor(
    private readonly http: HttpClient
  ) {}

  // =========================================================
  // ESTADO DE ACCESO
  // =========================================================

  consultarEstadoAcceso(
    username: string
  ): Observable<EstadoAccesoResponse> {

    return this.http.post<EstadoAccesoResponse>(
      `${this.baseUrl}/estado-acceso`,
      { username }
    );
  }

  // =========================================================
  // ACTIVACIÓN INICIAL
  // =========================================================

  activarCuenta(
    datos: ActivarCuentaRequest
  ): Observable<ActivarCuentaResponse> {

    return this.http.post<ActivarCuentaResponse>(
      `${this.baseUrl}/activar`,
      datos
    );
  }

  // =========================================================
  // RECUPERACIÓN DE CONTRASEÑA
  // =========================================================

  recuperarPassword(
    datos: RecuperarPasswordRequest
  ): Observable<RecuperarPasswordResponse> {

    return this.http.post<RecuperarPasswordResponse>(
      `${this.baseUrl}/recuperar-password`,
      datos
    );
  }

  // =========================================================
  // AUTENTICACIÓN
  // =========================================================

  login(
    username: string,
    password: string
  ): Observable<LoginResponse> {

    return this.http.post<LoginResponse>(
      `${this.baseUrl}/login`,
      {
        username,
        password
      }
    );
  }

  // =========================================================
  // CONTEXTO DE SESIÓN
  // =========================================================

  me(): Observable<MeResponse> {

    return this.http.get<MeResponse>(
      `${this.baseUrl}/me`
    );
  }

  // =========================================================
  // CAMBIO DE CONTRASEÑA
  // =========================================================

  cambiarPassword(
    passwordActual: string,
    passwordNueva: string
  ): Observable<{ mensaje: string }> {

    return this.http.post<{ mensaje: string }>(
      `${this.baseUrl}/cambiar-password`,
      {
        passwordActual,
        passwordNueva
      }
    );
  }

  // =========================================================
  // PRIMER INGRESO EXISTENTE
  // =========================================================

  establecerPasswordPrimerIngreso(
    passwordNueva: string
  ): Observable<{ mensaje: string }> {

    return this.http.post<{ mensaje: string }>(
      `${this.baseUrl}/primer-ingreso/password`,
      {
        passwordNueva
      }
    );
  }

}
