// src/app/features/seguridad/roles/roles.api.ts

import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

/**
 * Representa un Rol del sistema
 */
export interface Rol {
  idRol: number;
  nombreRol: string;
  activo?: boolean;
}

/**
 * Servicio para consumir la API de Roles
 */
@Injectable({ providedIn: 'root' })
export class RolesApi {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/seguridad/roles`;

  listar() {
    return this.http.get<Rol[]>(this.baseUrl);
  }

  obtener(id: number) {
    return this.http.get<Rol>(`${this.baseUrl}/${id}`);
  }

  crear(data: Rol) {
    return this.http.post<Rol>(this.baseUrl, data);
  }

  actualizar(id: number, data: Rol) {
    return this.http.put<Rol>(`${this.baseUrl}/${id}`, data);
  }

  eliminar(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
