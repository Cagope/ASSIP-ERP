
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';


import {
  OperacionPendiente,
  OperacionEjecucion,
  OperacionSaldo,
  CuentaBancariaParaPago
} from './operaciones-pendientes.models';

/**
 * ASSIP ERP
 * Tesorería - Operaciones Pendientes
 *
 * API para consultar obligaciones pendientes,
 * ejecuciones y saldos disponibles.
 *
 * No registra ni confirma pagos.
 */

export interface OperacionPagoRequest {
  idOperacion: number;
  lineas: Array<{
    idBeneficiario: number;
    idCuentaBancaria: number;
    medioPago: 'TRANSFERENCIA' | 'CHEQUE';
    numeroDocumento: string | null;
    valor: number;
    observaciones: string | null;
  }>;
}

export interface OperacionComprobantePreview {
  idOperacion: number;
  idAgencia: number;
  idBeneficiario: number;
  valorOriginal: number;
  valorEjecutado: number;
  saldoDisponible: number;
  totalPago: number;
  saldoRestante: number;
  totalDebito: number;
  totalCredito: number;
  cuadrado: boolean;
  movimientos: Array<{
    idCatalogoCuenta: number;
    detalleMovimiento: string;
    documentoSoporte: string | null;
    valorDebito: number;
    valorCredito: number;
  }>;
  cuentasContables: Record<number, string>;
}

export interface OperacionPagoResponse {
  idOperacion: number;
  idAgencia: number;
  tipoComprobante: string;
  numeroComprobante: string;
  fechaContabilizacion: string;
  valorPagado: number;
  valorEjecutadoAcumulado: number;
  saldoPendiente: number;
  estadoOperacion: string;
  cantidadLineas: number;
  idEjecuciones: number[];
  mensaje: string;
}

@Injectable({
  providedIn: 'root'
})
export class OperacionesPendientesApi {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/tesoreria/operaciones-pendientes`;

  // =====================================================
  // CONSULTAR OBLIGACIONES POR AGENCIA
  // =====================================================

  listarPendientes(
    idAgencia: number
  ): Observable<OperacionPendiente[]> {

    const params = new HttpParams()
      .set('idAgencia', String(idAgencia));

    return this.http.get<OperacionPendiente[]>(
      this.baseUrl,
      { params }
    );
  }

  // =====================================================
  // CONSULTAR EJECUCIONES DE UNA OBLIGACIÓN
  // =====================================================

  listarEjecuciones(
    idOperacionPendiente: number
  ): Observable<OperacionEjecucion[]> {

    return this.http.get<OperacionEjecucion[]>(
      `${this.baseUrl}/${idOperacionPendiente}/ejecuciones`
    );
  }

  // =====================================================
  // CONSULTAR SALDO DISPONIBLE
  // =====================================================

  consultarSaldo(
    idOperacionPendiente: number
  ): Observable<OperacionSaldo> {

    return this.http.get<OperacionSaldo>(
      `${this.baseUrl}/${idOperacionPendiente}/saldo`
    );
  }


  // =====================================================
  // CUENTAS BANCARIAS HABILITADAS PARA PAGOS
  // =====================================================

  listarCuentasParaPago(
    idAgencia: number
  ): Observable<CuentaBancariaParaPago[]> {

    const params = new HttpParams()
      .set('idAgencia', String(idAgencia));

    return this.http.get<CuentaBancariaParaPago[]>(
      `${this.baseUrl}/cuentas-para-pago`,
      { params }
    );
  }


  generarVistaPrevia(request: OperacionPagoRequest): Observable<OperacionComprobantePreview> {
    return this.http.post<OperacionComprobantePreview>(`${this.baseUrl}/preview`, request);
  }

  confirmarPago(request: OperacionPagoRequest): Observable<OperacionPagoResponse> {
    return this.http.post<OperacionPagoResponse>(`${this.baseUrl}/confirmar-pago`, request);
  }
}
