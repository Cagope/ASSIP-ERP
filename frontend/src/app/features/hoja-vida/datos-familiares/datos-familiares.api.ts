import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';
import { DatosFamiliar } from '../../../shared/models/datos-familiar.model';

/**
 * 👨‍👩‍👧‍👦 API Hoja de Vida — Datos Familiares
 * Gestiona la información de los familiares asociados a una persona (parentesco, contactos, referencias, etc.).
 */
@Injectable({ providedIn: 'root' })
export class DatosFamiliaresApi {
  private readonly base = `${environment.apiUrl}/hoja-vida/datos-familiares`;

  constructor(private http: HttpClient) {}

  /** 🔹 Listar todos los registros */
  listar(): Observable<DatosFamiliar[]> {
    return this.http.get<DatosFamiliar[]>(this.base);
  }

  /** 🔹 Obtener un registro por ID */
  obtener(id: number): Observable<DatosFamiliar> {
    return this.http.get<DatosFamiliar>(`${this.base}/${id}`);
  }

  /** 🔹 Crear nuevo registro */
  crear(data: DatosFamiliar): Observable<DatosFamiliar> {
    return this.http.post<DatosFamiliar>(this.base, data);
  }

  /** 🔹 Actualizar registro existente */
  actualizar(id: number, data: DatosFamiliar): Observable<DatosFamiliar> {
    return this.http.put<DatosFamiliar>(`${this.base}/${id}`, data);
  }

  /** 🔹 Eliminar registro */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  // ===========================================================
  // 🔸 Métodos de filtrado específicos
  // ===========================================================

  /** 🔸 Listar familiares por persona */
  listarPorPersona(idDatosPersonal: number): Observable<DatosFamiliar[]> {
    return this.http.get<DatosFamiliar[]>(`${this.base}/persona/${idDatosPersonal}`);
  }

  /** 🔸 Listar familiares por código de parentesco */
  listarPorParentesco(codigoParentesco: string): Observable<DatosFamiliar[]> {
    return this.http.get<DatosFamiliar[]>(`${this.base}/parentesco/${codigoParentesco}`);
  }

  /** 🔸 Listar solo referencias familiares */
  listarReferencias(): Observable<DatosFamiliar[]> {
    return this.http.get<DatosFamiliar[]>(`${this.base}/referencias`);
  }
}
