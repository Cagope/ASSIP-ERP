import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

/**
 * Representa una agencia (tabla general.datos_agencias)
 */
export interface Agencia {
  idAgencia?: number;
  codigoAgencia: string;
  nombreAgencia: string;
  siglaAgencia?: string;
  direccionAgencia?: string;
  idDepartamento?: number;
  idCiudad?: number;
  correoAgencia?: string;
  celularAgencia?: string;
  telefonoAgencia?: string;

  // 🔹 Campos opcionales para vistas y exportaciones
  nombreDepartamento?: string;
  nombreCiudad?: string;
  fechaCreacion?: string | Date;
  fechaEdicion?: string | Date;
}

/**
 * Servicio para consumir la API de agencias.
 */
@Injectable({ providedIn: 'root' })
export class AgenciasApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/general/agencias`;

  listar() {
    return this.http.get<Agencia[]>(this.baseUrl);
  }

  obtener(id: number) {
    return this.http.get<Agencia>(`${this.baseUrl}/${id}`);
  }

  crear(data: Agencia) {
    return this.http.post<Agencia>(this.baseUrl, data);
  }

  actualizar(id: number, data: Agencia) {
    return this.http.put<Agencia>(`${this.baseUrl}/${id}`, data);
  }

  eliminar(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
