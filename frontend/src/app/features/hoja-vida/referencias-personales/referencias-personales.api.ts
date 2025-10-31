import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';
import { ReferenciaPersonal } from '../../../shared/models/referencia-personal.model';

/**
 * 👥 API Hoja de Vida — Referencias Personales
 * ------------------------------------------------------------
 * Gestiona las referencias personales asociadas a una persona.
 * Permite listar, crear, editar y eliminar registros.
 */
@Injectable({ providedIn: 'root' })
export class ReferenciasPersonalesApi {
  private readonly base = `${environment.apiUrl}/hoja-vida/referencias-personales`;

  constructor(private http: HttpClient) {}

  /** 🔹 Listar todas las referencias personales */
  listar(): Observable<ReferenciaPersonal[]> {
    return this.http.get<ReferenciaPersonal[]>(this.base);
  }

  /** 🔹 Obtener una referencia personal por ID */
  obtener(id: number): Observable<ReferenciaPersonal> {
    return this.http.get<ReferenciaPersonal>(`${this.base}/${id}`);
  }

  /** 🔹 Crear nueva referencia personal */
  crear(data: ReferenciaPersonal): Observable<ReferenciaPersonal> {
    return this.http.post<ReferenciaPersonal>(this.base, data);
  }

  /** 🔹 Actualizar una referencia personal existente */
  actualizar(id: number, data: ReferenciaPersonal): Observable<ReferenciaPersonal> {
    return this.http.put<ReferenciaPersonal>(`${this.base}/${id}`, data);
  }

  /** 🔹 Eliminar una referencia personal */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /** 🔸 Listar referencias personales por persona */
  listarPorPersona(idDatosPersonal: number): Observable<ReferenciaPersonal[]> {
    return this.http.get<ReferenciaPersonal[]>(`${this.base}/persona/${idDatosPersonal}`);
  }
}
