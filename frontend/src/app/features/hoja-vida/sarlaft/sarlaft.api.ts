import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Sarlaft } from '../../../shared/models/sarlaft.model';

/**
 * 🌐 API Service — SARLAFT
 * ------------------------------------------------------------
 * Gestiona las operaciones CRUD para la información SARLAFT
 * asociada a una persona en el módulo Hoja de Vida.
 *
 * Endpoints:
 *  - GET    /hoja-vida/sarlaft
 *  - GET    /hoja-vida/sarlaft/{id}
 *  - GET    /hoja-vida/sarlaft/persona/{idDatosPersonal}
 *  - POST   /hoja-vida/sarlaft
 *  - PUT    /hoja-vida/sarlaft/{id}
 *  - DELETE /hoja-vida/sarlaft/{id}
 */
@Injectable({ providedIn: 'root' })
export class SarlaftApi {
  private readonly base = `${environment.apiUrl}/hoja-vida/sarlaft`;

  constructor(private http: HttpClient) {}

  /** 🔹 Listar todos los registros SARLAFT */
  listar(): Observable<Sarlaft[]> {
    return this.http.get<Sarlaft[]>(this.base);
  }

  /** 🔹 Obtener registro SARLAFT por ID */
  obtener(id: number): Observable<Sarlaft> {
    return this.http.get<Sarlaft>(`${this.base}/${id}`);
  }

  /** 🔹 Crear nuevo registro SARLAFT */
  crear(data: Sarlaft): Observable<Sarlaft> {
    return this.http.post<Sarlaft>(this.base, data);
  }

  /** 🔹 Actualizar un registro existente */
  actualizar(id: number, data: Sarlaft): Observable<Sarlaft> {
    return this.http.put<Sarlaft>(`${this.base}/${id}`, data);
  }

  /** 🔹 Eliminar un registro SARLAFT */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /** 🔹 Obtener registro SARLAFT por persona (idDatosPersonal) */
  obtenerPorPersona(idDatosPersonal: number): Observable<Sarlaft> {
    return this.http.get<Sarlaft>(`${this.base}/persona/${idDatosPersonal}`);
  }
}

/** ✅ Exporta el tipo para que pueda usarse desde otros servicios y componentes */
export type { Sarlaft };
