
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';

import {
  CalcularDesembolsoRequest,
  ComprobanteDesembolsoPreview,
  CreditoPendienteDesembolso,
  DistribucionDesembolsoRequest,
  EjecutarDesembolsoRequest,
  LiquidacionDesembolso,
  PreparacionDesembolso,
  ResultadoEjecucionDesembolso,
  ResultadoValidacionDistribucion,
  VerificarComprobantesRequest
} from './originacion-desembolso.models';


@Injectable({
  providedIn: 'root'
})
export class OriginacionDesembolsoApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/originacion/desembolso`;


  constructor(
    private readonly http: HttpClient
  ) {
  }


  // =========================================================
  // BANDEJA DE PENDIENTES
  //
  // GET
  // /cartera/originacion/desembolso/pendientes
  // =========================================================

  listarPendientes():
    Observable<CreditoPendienteDesembolso[]> {

    return this.http.get<CreditoPendienteDesembolso[]>(
      `${this.baseUrl}/pendientes`
    );
  }


  // =========================================================
  // PREPARAR DESEMBOLSO
  //
  // GET
  // /cartera/originacion/desembolso/{id}/preparar
  //
  // No genera movimientos.
  // =========================================================

  preparar(
    idCarteraCredito: number
  ): Observable<PreparacionDesembolso> {

    return this.http.get<PreparacionDesembolso>(
      `${this.baseUrl}/${idCarteraCredito}/preparar`
    );
  }


  // =========================================================
  // RECALCULAR LIQUIDACIÓN
  //
  // POST
  // /cartera/originacion/desembolso/{id}/calcular
  //
  // El frontend únicamente modifica la cantidad de consultas
  // a Central de Riesgo.
  // =========================================================

  calcular(
    idCarteraCredito: number,
    cantidadCentral: number
  ): Observable<LiquidacionDesembolso> {

    const request: CalcularDesembolsoRequest = {
      cantidadCentral
    };

    return this.http.post<LiquidacionDesembolso>(
      `${this.baseUrl}/${idCarteraCredito}/calcular`,
      request
    );
  }


  // =========================================================
  // VALIDAR DISTRIBUCIÓN
  //
  // POST
  // /cartera/originacion/desembolso/{id}/validar-distribucion
  //
  // No contabiliza ni genera movimientos.
  // =========================================================

  validarDistribucion(
    idCarteraCredito: number,
    request: DistribucionDesembolsoRequest
  ): Observable<ResultadoValidacionDistribucion> {

    return this.http.post<ResultadoValidacionDistribucion>(
      `${this.baseUrl}/${idCarteraCredito}/validar-distribucion`,
      request
    );
  }


  // =========================================================
  // VERIFICAR COMPROBANTES CONTABLES
  //
  // POST
  // /cartera/originacion/desembolso/{id}/verificar-comprobantes
  //
  // Construye la vista previa de los comprobantes de
  // desembolso y cobros, incluyendo movimientos y cuadre.
  //
  // No contabiliza ni genera consecutivos.
  // =========================================================

  verificarComprobantes(
    idCarteraCredito: number,
    request: VerificarComprobantesRequest
  ): Observable<ComprobanteDesembolsoPreview[]> {

    return this.http.post<ComprobanteDesembolsoPreview[]>(
      `${this.baseUrl}/${idCarteraCredito}/verificar-comprobantes`,
      request
    );
  }


  // =========================================================
  // EJECUTAR DESEMBOLSO
  //
  // POST
  // /cartera/originacion/desembolso/{id}/ejecutar
  //
  // Operación definitiva.
  //
  // El backend:
  // - recalcula
  // - valida distribución
  // - contabiliza
  // - registra Caja
  // - registra Depósitos
  // - registra Aportes
  // - activa el crédito
  // =========================================================

  ejecutar(
    idCarteraCredito: number,
    request: EjecutarDesembolsoRequest
  ): Observable<ResultadoEjecucionDesembolso> {

    return this.http.post<ResultadoEjecucionDesembolso>(
      `${this.baseUrl}/${idCarteraCredito}/ejecutar`,
      request
    );
  }


  // =========================================================
  // CONSULTAR CUENTAS DE AHORRO DEL ASOCIADO
  // =========================================================

  buscarCuentasAsociado(
    documento: string
  ): Observable<any> {

    const request = {
      schema: 'depositos',
      view: 'vw_depositos_cuentas_ahorro_total',
      filters: {
        documento,
        codigo_estado: 'A'
      }
    };

    return this.http.post<any>(
      `${environment.apiUrl}/reporting/query`,
      request
    );
  }

}
