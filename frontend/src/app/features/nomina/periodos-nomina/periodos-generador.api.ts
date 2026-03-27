import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* ============================================
   DTOs
   ============================================ */

export interface PeriodosGenerarRequestDTO {
  idAgencia: number | null;
  anio: number | null;
  tipoPeriodo: string;
}

export interface PeriodoGeneradoDTO {
  anio: number;
  mes: number;
  fechaInicio: string;
  fechaFin: string;
  descripcion: string;
}

/* ============================================
   API
   ============================================ */

@Injectable({ providedIn: 'root' })
export class PeriodosGeneradorApi {

  private readonly base =
    `${environment.apiUrl}/nomina/periodos-nomina/generar`;

  constructor(private http: HttpClient) {}

  generar(
    data: PeriodosGenerarRequestDTO
  ): Observable<PeriodoGeneradoDTO[]> {

    return this.http.post<PeriodoGeneradoDTO[]>(
      this.base,
      data
    );
  }
}
