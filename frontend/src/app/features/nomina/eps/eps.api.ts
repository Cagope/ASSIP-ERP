import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs
   ========================================================= */

export interface EpsListDTO {
  idEps: number;
  nombreEps: string;
  idDatosPersonal: number | null;
  documento: string | null; // ✅ NUEVO
  activo: boolean;
}

export interface EpsFormDTO {
  idEps?: number;
  nombreEps: string;
  idDatosPersonal: number | null;
  activo: boolean;
}

export interface EpsSaveDTO extends EpsFormDTO {}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class EpsApi {

  // ✔ Ruta alineada al backend
  private readonly base = `${environment.apiUrl}/nomina/eps`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // LISTAR
  // =========================================================
  listar(): Observable<EpsListDTO[]> {
    return this.http.get<EpsListDTO[]>(this.base);
  }

  // =========================================================
  // OBTENER (FORM)
  // =========================================================
  obtener(id: number): Observable<EpsFormDTO> {
    return this.http.get<EpsFormDTO>(`${this.base}/${id}`);
  }

  // =========================================================
  // CREAR
  // =========================================================
  crear(data: EpsSaveDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  // =========================================================
  // ACTUALIZAR
  // =========================================================
  actualizar(id: number, data: EpsSaveDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  // =========================================================
  // ELIMINAR
  // =========================================================
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
