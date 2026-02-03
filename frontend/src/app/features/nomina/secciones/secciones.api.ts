import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTO ÚNICO (CRUD pequeño)
   ========================================================= */
export interface SeccionNominaDTO {
  idSeccion?: number;
  codigo: string;
  nombreSeccion: string;
  activo: boolean;
}

/* =========================================================
   API
   ========================================================= */
@Injectable({ providedIn: 'root' })
export class SeccionesNominaApi {

  private readonly base = `${environment.apiUrl}/nomina/secciones`;

  constructor(private http: HttpClient) {}

  // LISTAR
  listar(): Observable<SeccionNominaDTO[]> {
    return this.http.get<SeccionNominaDTO[]>(this.base);
  }

  // OBTENER (form)
  obtener(id: number): Observable<SeccionNominaDTO> {
    return this.http.get<SeccionNominaDTO>(`${this.base}/${id}`);
  }

  // CREAR
  crear(data: SeccionNominaDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  // ACTUALIZAR
  actualizar(id: number, data: SeccionNominaDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  // ELIMINAR
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
