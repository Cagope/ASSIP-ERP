import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs
   ========================================================= */

export interface LocalizacionListDTO {
  idLocalizacion: number;
  nombre: string;
  telefono: string | null;
  idAgencia: number;
}

export interface LocalizacionFormDTO {
  idLocalizacion?: number;

  nombre: string;
  telefono?: string | null;
  idAgencia: number;
}

export interface LocalizacionSaveDTO extends LocalizacionFormDTO {}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class LocalizacionesApi {

  // ✔ Ruta alineada al backend (sin repetir /api/v1)
  private readonly base = `${environment.apiUrl}/activos-fijos/localizaciones`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // LISTADO
  // =========================================================
  listar(): Observable<LocalizacionListDTO[]> {
    return this.http.get<LocalizacionListDTO[]>(this.base);
  }

  // =========================================================
  // FORMULARIO (detalle)
  // =========================================================
  obtener(id: number): Observable<LocalizacionFormDTO> {
    return this.http.get<LocalizacionFormDTO>(`${this.base}/${id}`);
  }

  // =========================================================
  // CREAR
  // =========================================================
  crear(data: LocalizacionSaveDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  // =========================================================
  // ACTUALIZAR
  // =========================================================
  actualizar(id: number, data: LocalizacionSaveDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  // =========================================================
  // ELIMINAR
  // =========================================================
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
