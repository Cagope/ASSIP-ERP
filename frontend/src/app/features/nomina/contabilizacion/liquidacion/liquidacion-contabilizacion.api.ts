import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

// ======================================================
// DTO MOVIMIENTOS CONTABLES
// ======================================================

export interface LiquidacionMovimientoContableDTO {

  idAgencia: number;

  idCatalogoCuenta: number;
  codigoCuenta: string;
  nombreCuenta: string;

  idTercero: number | null;
  nombreTercero: string | null;
  documentoTercero?: string;

  idEmpleadoReferencia: number | null;
  nombreEmpleadoReferencia: string | null;

  debito: number;
  credito: number;
  valorBase?: number;

}

// ======================================================
// DTO COMPROBANTE GENERADO
// ======================================================

export interface LiquidacionComprobanteGeneradoDTO {

  idAgencia: number;

  tipoComprobante: string;
  numeroComprobante: string;

  conceptoComprobante: string;
  fechaComprobante: string;

  cantidadMovimientos: number;

  totalDebito: number;
  totalCredito: number;

}

// ======================================================
// DTO RESULTADO CONTABILIZACIÓN
// ======================================================

export interface LiquidacionContabilizacionResultadoDTO {

  idPeriodoNomina: number;

  cantidadAgencias: number;

  comprobantes: LiquidacionComprobanteGeneradoDTO[];

}

// ======================================================
// API
// ======================================================

@Injectable({ providedIn: 'root' })
export class LiquidacionContabilizacionApi {

  private readonly baseUrl =
    `${environment.apiUrl}/nomina/contabilizacion/liquidacion`;

  constructor(private http: HttpClient) {}

  // ======================================================
  // PREVIEW
  // ======================================================

  preview(idPeriodoNomina: number): Observable<LiquidacionMovimientoContableDTO[]> {

    return this.http.get<LiquidacionMovimientoContableDTO[]>(
      `${this.baseUrl}/preview/${idPeriodoNomina}`
    );

  }

  // ======================================================
  // EJECUTAR CONTABILIZACIÓN
  // ======================================================

  ejecutar(idPeriodoNomina: number): Observable<LiquidacionContabilizacionResultadoDTO> {

    return this.http.post<LiquidacionContabilizacionResultadoDTO>(
      `${this.baseUrl}/ejecutar/${idPeriodoNomina}`,
      {}
    );

  }

  // ======================================================
  // REVERSAR CONTABILIZACIÓN
  // ======================================================

  reversar(idPeriodoNomina: number): Observable<void> {

    return this.http.post<void>(
      `${this.baseUrl}/reversar/${idPeriodoNomina}`,
      {}
    );

  }

  obtenerComprobante(idPeriodo: number) {
    return this.http.get(
      `/nomina/contabilizacion/liquidacion/comprobante/${idPeriodo}`,
      { responseType: 'text' }
    );
  }

}
