import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs (ALINEADOS 100% CON EL BACKEND ACTUALIZADO)
   ========================================================= */

export interface ConceptoNominaListDTO {
  codigoConcepto: string;
  nombreConcepto: string;
  tipoConcepto: string;

  esFijo: boolean;
  activo: boolean;

  tipoCalculo: string;
  baseCalculo?: string | null;
  multiplicador?: number | null;

  afectaIbc: boolean;
  afectaBaseCesantias: boolean;
  afectaBasePrimaLegal: boolean;
  afectaBaseVacaciones: boolean;
  afectaBasePrimaSemestral: boolean;
  afectaBaseArl: boolean;
  afectaBaseParafiscales: boolean;

  smmlvDesde?: number | null;
  smmlvHasta?: number | null;
}

export interface ConceptoNominaFormDTO {
  codigoConcepto: string;
  nombreConcepto: string;
  tipoConcepto: string;

  esFijo: boolean;
  activo: boolean;

  tipoCalculo: string;
  baseCalculo?: string | null;
  multiplicador?: number | null;

  afectaIbc: boolean;
  afectaBaseCesantias: boolean;
  afectaBasePrimaLegal: boolean;
  afectaBaseVacaciones: boolean;
  afectaBasePrimaSemestral: boolean;
  afectaBaseArl: boolean;
  afectaBaseParafiscales: boolean;

  smmlvDesde?: number | null;
  smmlvHasta?: number | null;
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
  // OBTENER
  // =========================================================
  obtener(codigoConcepto: string): Observable<ConceptoNominaFormDTO> {
    return this.http.get<ConceptoNominaFormDTO>(
      `${this.base}/${codigoConcepto}`
    );
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
  actualizar(
    codigoConcepto: string,
    data: ConceptoNominaSaveDTO
  ): Observable<void> {
    return this.http.put<void>(
      `${this.base}/${codigoConcepto}`,
      data
    );
  }

  // =========================================================
  // ELIMINAR
  // =========================================================
  eliminar(codigoConcepto: string): Observable<void> {
    return this.http.delete<void>(
      `${this.base}/${codigoConcepto}`
    );
  }
}
