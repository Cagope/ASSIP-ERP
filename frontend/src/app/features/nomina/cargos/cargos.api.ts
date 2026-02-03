import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs
   ========================================================= */

export interface CargoListDTO {
  idCargo: number;
  nombreCargo: string;
  activo: boolean;
}

export interface CargoFormDTO {
  idCargo?: number;
  nombreCargo: string;
  activo: boolean;
}

export interface CargoSaveDTO extends CargoFormDTO {}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class CargoApi {

  // ✔ Ruta alineada al backend
  private readonly base = `${environment.apiUrl}/nomina/cargos`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // LISTAR
  // =========================================================
  listar(): Observable<CargoListDTO[]> {
    return this.http.get<CargoListDTO[]>(this.base);
  }

  // =========================================================
  // OBTENER (FORM)
  // =========================================================
  obtener(id: number): Observable<CargoFormDTO> {
    return this.http.get<CargoFormDTO>(`${this.base}/${id}`);
  }

  // =========================================================
  // CREAR
  // =========================================================
  crear(data: CargoSaveDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  // =========================================================
  // ACTUALIZAR
  // =========================================================
  actualizar(id: number, data: CargoSaveDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  // =========================================================
  // ELIMINAR
  // =========================================================
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
