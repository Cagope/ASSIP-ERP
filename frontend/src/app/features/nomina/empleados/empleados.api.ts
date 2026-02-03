import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs
   ========================================================= */

export interface EmpleadoListDTO {
  idEmpleado: number;
  idAgencia: number;
  idDatosPersonal: number;
  activo: boolean;
}

export interface EmpleadoFormDTO {
  idEmpleado?: number;
  idAgencia: number | null;
  idDatosPersonal: number | null;
  activo: boolean;
}

export interface EmpleadoSaveDTO extends EmpleadoFormDTO {}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class EmpleadosApi {

  private readonly base = `${environment.apiUrl}/nomina/empleados`;

  constructor(private http: HttpClient) {}

  listar(): Observable<EmpleadoListDTO[]> {
    return this.http.get<EmpleadoListDTO[]>(this.base);
  }

  obtener(id: number): Observable<EmpleadoFormDTO> {
    return this.http.get<EmpleadoFormDTO>(`${this.base}/${id}`);
  }

  crear(data: EmpleadoSaveDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  actualizar(id: number, data: EmpleadoSaveDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
