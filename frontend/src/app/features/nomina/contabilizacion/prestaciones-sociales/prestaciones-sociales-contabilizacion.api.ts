import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

import { LiquidacionMovimientoContableDTO } from '../liquidacion/liquidacion-contabilizacion.api';

export interface PrestacionesSocialesComprobanteGeneradoDTO {
  idAgencia: number;
  tipoComprobante: string;
  numeroComprobante: string;
  conceptoComprobante: string;
  fechaComprobante: string;
  cantidadMovimientos: number;
  totalDebito: number;
  totalCredito: number;
}

export interface PrestacionesSocialesContabilizacionResultadoDTO {
  idPeriodoNomina: number;
  cantidadAgencias: number;
  comprobantes: PrestacionesSocialesComprobanteGeneradoDTO[];
}

@Injectable({
  providedIn: 'root'
})
export class PrestacionesSocialesContabilizacionApi {

  private baseUrl =
    `${environment.apiUrl}/nomina/contabilizacion/prestaciones-sociales`;

  constructor(private http: HttpClient) {}

  preview(idPeriodo: number): Observable<LiquidacionMovimientoContableDTO[]> {
    return this.http.get<LiquidacionMovimientoContableDTO[]>(
      `${this.baseUrl}/preview/${idPeriodo}`
    );
  }

  obtenerComprobante(idPeriodo: number): Observable<string> {
    return this.http.get(
      `${this.baseUrl}/comprobante/${idPeriodo}`,
      { responseType: 'text' }
    );
  }

  ejecutar(idPeriodo: number): Observable<PrestacionesSocialesContabilizacionResultadoDTO> {
    return this.http.post<PrestacionesSocialesContabilizacionResultadoDTO>(
      `${this.baseUrl}/ejecutar/${idPeriodo}`,
      {}
    );
  }

  reversar(idPeriodo: number): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}/reversar/${idPeriodo}`,
      {}
    );
  }
}
