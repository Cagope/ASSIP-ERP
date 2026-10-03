// src/app/features/seguridad/roles/roles.api.ts

import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../../../environments/environment';
import { PermisoRol, Rol } from './roles.models';

/**
 * Servicio para consumir la API de Roles
 * y administrar sus permisos.
 */
@Injectable({ providedIn: 'root' })
export class RolesApi {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/seguridad/roles`;

  private readonly rolPermisosUrl =
    `${environment.apiUrl}/seguridad/rol-permisos`;

  // =========================================================
  // ROLES
  // =========================================================

  listar() {
    return this.http.get<Rol[]>(
      this.baseUrl
    );
  }

  obtener(idRol: number) {
    return this.http.get<Rol>(
      `${this.baseUrl}/${idRol}`
    );
  }

  crear(data: Rol) {
    return this.http.post<Rol>(
      this.baseUrl,
      data
    );
  }

  actualizar(
    idRol: number,
    data: Rol
  ) {
    return this.http.put<Rol>(
      `${this.baseUrl}/${idRol}`,
      data
    );
  }

  eliminar(idRol: number) {
    return this.http.delete<void>(
      `${this.baseUrl}/${idRol}`
    );
  }

  // =========================================================
  // PERMISOS DEL ROL
  // =========================================================

  listarPermisosRol(idRol: number) {
    return this.http.get<PermisoRol[]>(
      `${this.rolPermisosUrl}/${idRol}`
    );
  }

  actualizarPermisosRol(
    idRol: number,
    idsPermisos: number[]
  ) {
    return this.http.put<void>(
      `${this.rolPermisosUrl}/${idRol}`,
      idsPermisos
    );
  }
}
