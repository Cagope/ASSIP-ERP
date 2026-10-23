import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { CarteraAsociadoBusqueda } from '../consulta-creditos/consulta-creditos.models';
import { ConsultaCreditosApi } from '../consulta-creditos/consulta-creditos.api';
import {
  PagoCuotasCreditoResumen,
  PagoCuotasCreditoDetalle,
  PagoCuotasDatosPrevios,
  PagoCuotasLiquidacion,
  PagoCuotasSimulacion,
  PagoCuotasParametrosLiquidacion,
  PagoCuotasParametrosSimulacion,
  PagoCuotasPagoRealizado
} from './pago-cuotas_v1.models';

/**
 * Consultas y simulaciones de Pago de Cuotas V1.
 * Ninguna operación de este servicio registra pagos ni modifica saldos.
 */
@Injectable({ providedIn: 'root' })
export class PagoCuotasV1Api {

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/pago-cuotas-v1`;

  constructor(
    private readonly http: HttpClient,
    private readonly consultaCreditosApi: ConsultaCreditosApi
  ) {}

  /** Misma búsqueda por identificación y nombres de Consulta de Créditos. */
  buscarAsociados(
    documento: string,
    nombres: string,
    primerApellido: string,
    segundoApellido: string
  ): Observable<CarteraAsociadoBusqueda[]> {
    return this.consultaCreditosApi.buscarAsociados(
      documento, nombres, primerApellido, segundoApellido
    );
  }

  listarCreditosPorAsociado(
    idDatosPersonal: number
  ): Observable<PagoCuotasCreditoResumen[]> {
    return this.http.get<PagoCuotasCreditoResumen[]>(
      `${this.baseUrl}/asociados/${idDatosPersonal}/creditos`
    );
  }

  consultarCredito(
    idCarteraCredito: number
  ): Observable<PagoCuotasCreditoDetalle> {
    return this.http.get<PagoCuotasCreditoDetalle>(
      `${this.baseUrl}/creditos/${idCarteraCredito}`
    );
  }

  consultarDatosPrevios(
    idCarteraCredito: number,
    fechaPago: string
  ): Observable<PagoCuotasDatosPrevios> {
    return this.http.get<PagoCuotasDatosPrevios>(
      `${this.baseUrl}/creditos/${idCarteraCredito}/datos-previos`,
      { params: new HttpParams().set('fechaPago', fechaPago) }
    );
  }

  liquidar(
    parametros: PagoCuotasParametrosLiquidacion
  ): Observable<PagoCuotasLiquidacion> {
    return this.http.get<PagoCuotasLiquidacion>(
      `${this.baseUrl}/creditos/${parametros.idCarteraCredito}/liquidacion`,
      { params: this.parametrosLiquidacion(parametros) }
    );
  }

  simularPago(
    parametros: PagoCuotasParametrosSimulacion
  ): Observable<PagoCuotasSimulacion> {
    return this.http.get<PagoCuotasSimulacion>(
      `${this.baseUrl}/creditos/${parametros.idCarteraCredito}/simulacion-pago`,
      {
        params: this.parametrosLiquidacion(parametros)
          .set('valorPago', parametros.valorPago.toString())
      }
    );
  }

  /** Pagos consolidados por comprobante, consultados en Pago de Cuotas V1. */
  listarPagosRealizados(
    idCarteraCredito: number
  ): Observable<PagoCuotasPagoRealizado[]> {
    return this.http.get<PagoCuotasPagoRealizado[]>(
      `${this.baseUrl}/creditos/${idCarteraCredito}/pagos-realizados`
    );
  }

  private parametrosLiquidacion(
    parametros: PagoCuotasParametrosLiquidacion
  ): HttpParams {
    let httpParams = new HttpParams()
      .set('fechaPago', parametros.fechaPago)
      .set('periodoInteresMeses', parametros.periodoInteresMeses.toString());

    if (parametros.cuotasACancelar != null) {
      httpParams = httpParams.set(
        'cuotasACancelar', parametros.cuotasACancelar.toString()
      );
    }

    return httpParams;
  }
}
