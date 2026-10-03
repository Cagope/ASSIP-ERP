// src/app/features/seguridad/usuarios/usuarios.api.ts

import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  Usuario,
  UsuarioRol
} from './usuarios.models';

import {
  environment
} from '../../../../environments/environment';

/**
 * Datos necesarios para crear un usuario
 * con sus roles y agencias autorizadas.
 */
export interface UsuarioCreacionCompleta
  extends Omit<Usuario, 'idUsuario' | 'roles' | 'agencias'> {

  roles: number[];
  agencias: number[];
}

@Injectable({
  providedIn: 'root'
})
export class UsuariosApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/seguridad/usuarios`;

  /**
   * Lista los usuarios.
   */
  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(
      this.base
    );
  }

  /**
   * Busca un usuario por username.
   */
  buscar(username: string): Observable<Usuario> {
    return this.http.get<Usuario>(
      `${this.base}/user/${encodeURIComponent(username)}`
    );
  }

  /**
   * Busca un usuario por su identificador.
   */
  buscarPorId(idUsuario: number): Observable<Usuario> {
    return this.http.get<Usuario>(
      `${this.base}/id/${idUsuario}`
    );
  }

  /**
   * Crea un usuario con roles y agencias
   * mediante una única operación transaccional.
   */
  crearCompleto(
    datos: UsuarioCreacionCompleta
  ): Observable<Usuario> {

    return this.http.post<Usuario>(
      `${this.base}/completo`,
      datos
    );
  }

  /**
   * Crea o actualiza los datos generales del usuario.
   *
   * Los roles y agencias se administran mediante
   * endpoints independientes.
   */
  guardar(usuario: Usuario): Observable<Usuario> {
    return this.http.post<Usuario>(
      this.base,
      usuario
    );
  }

  /**
   * Obtiene las agencias asignadas al usuario.
   */
  listarAgencias(
    idUsuario: number
  ): Observable<any[]> {

    return this.http.get<any[]>(
      `${this.base}/${idUsuario}/agencias`
    );
  }

  /**
   * Reemplaza las agencias asignadas al usuario.
   *
   * Una lista vacía también debe enviarse para permitir
   * retirar todas las agencias.
   */
  asignarAgencias(
    idUsuario: number,
    agencias: number[]
  ): Observable<void> {

    return this.http.post<void>(
      `${this.base}/${idUsuario}/agencias`,
      agencias
    );
  }

  /**
   * Obtiene los roles vigentes del usuario.
   */
  listarRoles(
    idUsuario: number
  ): Observable<UsuarioRol[]> {

    return this.http.get<UsuarioRol[]>(
      `${this.base}/${idUsuario}/roles`
    );
  }

  /**
   * Asigna un rol al usuario.
   */
  asignarRol(
    idUsuario: number,
    idRol: number
  ): Observable<Usuario> {

    return this.http.post<Usuario>(
      `${this.base}/${idUsuario}/roles/${idRol}`,
      null
    );
  }

  /**
   * Retira un rol del usuario conservando
   * el histórico en backend.
   */
  retirarRol(
    idUsuario: number,
    idRol: number
  ): Observable<Usuario> {

    return this.http.delete<Usuario>(
      `${this.base}/${idUsuario}/roles/${idRol}`
    );
  }

  /**
   * Genera el documento PDF cifrado para la gestión
   * de contraseña del usuario.
   *
   * El backend determina automáticamente el tipo de proceso:
   *
   * - ACTIVACION:
   *   cuando el usuario todavía no tiene contraseña.
   *
   * - RECUPERACION:
   *   cuando el usuario ya tiene contraseña.
   *
   * El código de seguridad nunca se expone en JSON.
   */
  generarDocumentoPassword(
    idUsuario: number
  ): Observable<Blob> {

    return this.http.post(
      `${this.base}/${idUsuario}/documento-password`,
      null,
      {
        responseType: 'blob'
      }
    );
  }

}
