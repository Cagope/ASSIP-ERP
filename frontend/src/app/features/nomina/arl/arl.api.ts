import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs
   ========================================================= */

export interface ArlListDTO {
  idArl: number;
  nombreArl: string;
  idDatosPersonal: number | null;
  documento: string | null;
  activo: boolean;
}

export interface ArlFormDTO {
  idArl?: number;
  nombreArl: string;
  idDatosPersonal: number | null;
  activo: boolean;
}

export interface ArlSaveDTO extends ArlFormDTO {}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class ArlApi {

  // ✔ Ruta alineada al backend
  private readonly base = `${environment.apiUrl}/nomina/arl`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // LISTAR
  // =========================================================
  listar(): Observable<ArlListDTO[]> {
    return this.http.get<ArlListDTO[]>(this.base);
  }

  // =========================================================
  // OBTENER (FORM)
  // =========================================================
  obtener(id: number): Observable<ArlFormDTO> {
    return this.http.get<ArlFormDTO>(`${this.base}/${id}`);
  }

  // =========================================================
  // CREAR
  // =========================================================
  crear(data: ArlSaveDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  // =========================================================
  // ACTUALIZAR
  // =========================================================
  actualizar(id: number, data: ArlSaveDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  // =========================================================
  // ELIMINAR
  // =========================================================
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
