import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Financiero } from '../../../shared/models/financiero.model';

/**
 * 🧩 Servicio API — Financieros (Hoja de Vida)
 * ------------------------------------------------------------
 * Gestiona las operaciones CRUD sobre la información económica
 * y patrimonial de los afiliados.
 *
 * Endpoints backend: /hoja-vida/financieros
 */
@Injectable({ providedIn: 'root' })
export class FinancierosApi {
  private readonly base = `${environment.apiUrl}/hoja-vida/financieros`;

  constructor(private http: HttpClient) {}

  /** 🔹 Listar todos los registros financieros */
  listar(): Observable<Financiero[]> {
    return this.http.get<Financiero[]>(this.base);
  }

  /** 🔹 Obtener un registro financiero por su ID */
  obtener(id: number): Observable<Financiero> {
    return this.http.get<Financiero>(`${this.base}/${id}`);
  }

  /** 🔹 Crear un nuevo registro financiero */
  crear(data: Financiero): Observable<Financiero> {
    return this.http.post<Financiero>(this.base, data);
  }

  /** 🔹 Actualizar un registro financiero existente */
  actualizar(id: number, data: Financiero): Observable<Financiero> {
    return this.http.put<Financiero>(`${this.base}/${id}`, data);
  }

  /** 🔹 Eliminar un registro financiero */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /** 🔹 Obtener el registro financiero asociado a una persona */
  obtenerPorPersona(idDatosPersonales: number): Observable<Financiero> {
    return this.http.get<Financiero>(`${this.base}/persona/${idDatosPersonales}`);
  }
}
