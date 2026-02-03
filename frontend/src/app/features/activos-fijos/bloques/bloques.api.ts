import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs
   ========================================================= */

export interface BloqueListDTO {
  idBloque: number;
  codigoBloque: string;
  nombreBloque: string;
  mesesDepreciacionDefecto: number;
}

export interface BloqueFormDTO {
  idBloque?: number;
  codigoBloque: string;
  nombreBloque: string;
  mesesDepreciacionDefecto: number;
}

export interface BloqueSaveDTO extends BloqueFormDTO {}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class BloquesApi {

  // ✔ Ruta alineada al backend (sin repetir /api/v1)
  private readonly base = `${environment.apiUrl}/activos-fijos/bloques`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // LISTADO
  // =========================================================
  listar(): Observable<BloqueListDTO[]> {
    return this.http.get<BloqueListDTO[]>(this.base);
  }

  // =========================================================
  // FORMULARIO (detalle)
  // =========================================================
  obtener(id: number): Observable<BloqueFormDTO> {
    return this.http.get<BloqueFormDTO>(`${this.base}/${id}`);
  }

  // =========================================================
  // CREAR
  // =========================================================
  crear(data: BloqueSaveDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  // =========================================================
  // ACTUALIZAR
  // =========================================================
  actualizar(id: number, data: BloqueSaveDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  // =========================================================
  // ELIMINAR
  // =========================================================
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
