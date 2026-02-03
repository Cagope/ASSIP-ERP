import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs
   ========================================================= */

export interface ConceptoNominaListDTO {
  codigo: string;
  nombre: string;
  tipo: string;
  esFijo: boolean;
  activo: boolean;
}

export interface ConceptoNominaFormDTO {
  codigo: string;
  nombre: string;
  tipo: string;
  esFijo: boolean;
  activo: boolean;
}

export interface ConceptoNominaSaveDTO extends ConceptoNominaFormDTO {}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class ConceptosNominaApi {

  private readonly base = `${environment.apiUrl}/nomina/conceptos-nomina`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // LISTAR
  // =========================================================
  listar(): Observable<ConceptoNominaListDTO[]> {
    return this.http.get<ConceptoNominaListDTO[]>(this.base);
  }

  // =========================================================
  // OBTENER (FORM)
  // =========================================================
  obtener(codigo: string): Observable<ConceptoNominaFormDTO> {
    return this.http.get<ConceptoNominaFormDTO>(`${this.base}/${codigo}`);
  }

  // =========================================================
  // CREAR
  // =========================================================
  crear(data: ConceptoNominaSaveDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  // =========================================================
  // ACTUALIZAR
  // =========================================================
  actualizar(codigo: string, data: ConceptoNominaSaveDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${codigo}`, data);
  }

  // =========================================================
  // ELIMINAR
  // =========================================================
  eliminar(codigo: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${codigo}`);
  }
}
