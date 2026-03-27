import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

// ===============================
// DTO LISTADO
// ===============================
export interface PeriodoNominaListDTO {

  idPeriodo: number;

  idAgencia: number;
  nombreAgencia: string;

  anio: number;
  mes: number;
  numeroPeriodo: number;

  tipoPeriodo: string;
  descripcion?: string;

  fechaInicio: string;
  fechaFin: string;

  estado: string;

  fechaLiquidacion?: string | null;
  fkSeguridadLiquidacion?: number | null;

  fechaContabiliza?: string | null;
  fkSeguridadContabiliza?: number | null;
}

// ===============================
// DTO ACCIÓN (SOLO REVERSAR)
// ===============================
export interface PeriodoAccionDTO {
  idPeriodo: number;
  accion: 'ABRIR';   // 🔒 ÚNICA ACCIÓN PERMITIDA
  observacion?: string | null;
}

@Injectable({ providedIn: 'root' })
export class PeriodosNominaApi {

  private readonly base = `${environment.apiUrl}/nomina/periodos-nomina`;

  constructor(private http: HttpClient) {}

  // ===============================
  // LISTAR
  // ===============================
  listar(params?: {
    agencia?: number | null;
    anio?: number | null;
  }): Observable<PeriodoNominaListDTO[]> {

    const q: string[] = [];

    if (params?.agencia != null)
      q.push(`agencia=${params.agencia}`);

    if (params?.anio != null)
      q.push(`anio=${params.anio}`);

    const url =
      q.length
        ? `${this.base}?${q.join('&')}`
        : this.base;

    return this.http.get<PeriodoNominaListDTO[]>(url);
  }

  // ===============================
  // 🔓 REVERSAR PERÍODO (ABRIR)
  // ===============================
  abrir(idPeriodo: number, observacion?: string | null): Observable<void> {

    const payload: PeriodoAccionDTO = {
      idPeriodo,
      accion: 'ABRIR',
      observacion: observacion ?? null
    };

    return this.http.post<void>(`${this.base}/accion`, payload);
  }

  // ======================================================
  // 🔎 PERÍODOS DISPONIBLES PARA CONTABILIZAR
  // ======================================================
  listarParaContabilizacion(): Observable<PeriodoNominaListDTO[]> {

    return this.http.get<PeriodoNominaListDTO[]>(
      `${environment.apiUrl}/nomina/periodos-nomina/para-contabilizacion`
    );

  }

}
