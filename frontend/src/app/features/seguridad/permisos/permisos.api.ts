// src/app/features/seguridad/permisos/permisos.api.ts

import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../../../environments/environment';
import { Permiso } from './permisos.models';

@Injectable({ providedIn: 'root' })
export class PermisosApi {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/seguridad/permisos`;

  // =========================================================
  // CONSULTAS
  // =========================================================

  listar() {
    return this.http.get<Permiso[]>(
      this.baseUrl
    );
  }

  obtener(idPermiso: number) {
    return this.http.get<Permiso>(
      `${this.baseUrl}/${idPermiso}`
    );
  }

  // =========================================================
  // CREACIÓN
  // =========================================================

  crear(data: Permiso) {
    return this.http.post<Permiso>(
      this.baseUrl,
      data
    );
  }

  // =========================================================
  // ACTUALIZACIÓN
  // =========================================================

  actualizar(
    idPermiso: number,
    data: Permiso
  ) {
    return this.http.put<Permiso>(
      `${this.baseUrl}/${idPermiso}`,
      data
    );
  }

  // =========================================================
  // ESTADO
  // =========================================================

  cambiarEstado(
    idPermiso: number,
    activo: boolean
  ) {
    return this.http.patch<Permiso>(
      `${this.baseUrl}/${idPermiso}/estado`,
      null,
      {
        params: {
          activo
        }
      }
    );
  }
}
