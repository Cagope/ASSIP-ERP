import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs
   ========================================================= */

export interface AfpListDTO {
  idAfp: number;
  nombreAfp: string;
  idDatosPersonal: number | null;
  documento: string | null;
  activo: boolean;
}

export interface AfpFormDTO {
  idAfp?: number;
  nombreAfp: string;
  idDatosPersonal: number | null;
  activo: boolean;
}

export interface AfpSaveDTO extends AfpFormDTO {}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class AfpApi {

  // ✔ Ruta alineada al backend
  private readonly base = `${environment.apiUrl}/nomina/afp`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // LISTAR
  // =========================================================
  listar(): Observable<AfpListDTO[]> {
    return this.http.get<AfpListDTO[]>(this.base);
  }

  // =========================================================
  // OBTENER (FORM)
  // =========================================================
  obtener(id: number): Observable<AfpFormDTO> {
    return this.http.get<AfpFormDTO>(`${this.base}/${id}`);
  }

  // =========================================================
  // CREAR
  // =========================================================
  crear(data: AfpSaveDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  // =========================================================
  // ACTUALIZAR
  // =========================================================
  actualizar(id: number, data: AfpSaveDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  // =========================================================
  // ELIMINAR
  // =========================================================
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
