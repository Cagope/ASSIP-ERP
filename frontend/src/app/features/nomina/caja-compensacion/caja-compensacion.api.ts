import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs
   ========================================================= */

export interface CajaCompensacionListDTO {
  idCaja: number;
  nombreCaja: string;
  idDatosPersonal: number | null;
  documento: string | null;
  activo: boolean;
}

export interface CajaCompensacionFormDTO {
  idCaja?: number;
  nombreCaja: string;
  idDatosPersonal: number | null;
  activo: boolean;
}

export interface CajaCompensacionSaveDTO extends CajaCompensacionFormDTO {}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class CajaCompensacionApi {

  // ✔ Ruta alineada al backend
  private readonly base = `${environment.apiUrl}/nomina/caja-compensacion`;

  constructor(private http: HttpClient) {}

  listar(): Observable<CajaCompensacionListDTO[]> {
    return this.http.get<CajaCompensacionListDTO[]>(this.base);
  }

  obtener(id: number): Observable<CajaCompensacionFormDTO> {
    return this.http.get<CajaCompensacionFormDTO>(`${this.base}/${id}`);
  }

  crear(data: CajaCompensacionSaveDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  actualizar(id: number, data: CajaCompensacionSaveDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
