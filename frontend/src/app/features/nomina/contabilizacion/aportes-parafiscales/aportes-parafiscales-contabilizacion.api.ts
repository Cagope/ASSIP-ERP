import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { LiquidacionMovimientoContableDTO } from '../liquidacion/liquidacion-contabilizacion.api';

export interface AportesParafiscalesComprobanteGeneradoDTO {
  idAgencia: number;
  tipoComprobante: string;
  numeroComprobante: string;
  conceptoComprobante: string;
  fechaComprobante: string;
  cantidadMovimientos: number;
  totalDebito: number;
  totalCredito: number;
}

export interface AportesParafiscalesContabilizacionResultadoDTO {
  idPeriodoNomina: number;
  cantidadAgencias: number;
  comprobantes: AportesParafiscalesComprobanteGeneradoDTO[];
}

@Injectable({
  providedIn: 'root'
})
export class AportesParafiscalesContabilizacionApi {

  private baseUrl =
    `${environment.apiUrl}/nomina/contabilizacion/aportes-parafiscales`;

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

  ejecutar(idPeriodo: number): Observable<AportesParafiscalesContabilizacionResultadoDTO> {
    return this.http.post<AportesParafiscalesContabilizacionResultadoDTO>(
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
