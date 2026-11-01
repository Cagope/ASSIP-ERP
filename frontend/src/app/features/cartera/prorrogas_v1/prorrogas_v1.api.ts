import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';
import { PagoCuotasV1Api } from '../pago-cuotas_v1/pago-cuotas_v1.api';
import { ProrrogasLiquidacion, ProrrogasCreditoDetalle, ProrrogasCreditoResumen, ProrrogasCaja, ProrrogasCuentaAhorro, ProrrogasSolicitud, ProrrogasResultado, ProrrogasMovimientoContable } from './prorrogas_v1.models';

/** Reutiliza consultas de Pago de Cuotas y expone los endpoints propios de Prórrogas. */
@Injectable({ providedIn: 'root' })
export class ProrrogasV1Api {
  private readonly baseUrl = `${environment.apiUrl}/cartera/prorrogas-v1`;
  constructor(private readonly pagosApi: PagoCuotasV1Api, private readonly http: HttpClient) {}

  /** Solicita al backend los conceptos exigibles para la fecha seleccionada. */
  liquidar(idCredito: number, fechaLiquidacion: string): Observable<ProrrogasLiquidacion> {
    return this.http.get<ProrrogasLiquidacion>(`${this.baseUrl}/creditos/${idCredito}/liquidacion`,
      { params: new HttpParams().set('fechaLiquidacion', fechaLiquidacion) });
  }

  /** Consulta la CxC parametrizada para la agencia del crédito. */
  consultarCuentaCxc(idCredito: number): Observable<{ idCuentaPorPagar: number; codigoCuenta: string }> {
    return this.http.get<{ idCuentaPorPagar: number; codigoCuenta: string }>(
      `${this.baseUrl}/creditos/${idCredito}/cuenta-cxc`);
  }

  /** Previsualiza el CC sin generar consecutivo ni aplicar la prórroga. */
  previsualizar(solicitud: ProrrogasSolicitud, fechaContable: string): Observable<ProrrogasMovimientoContable[]> {
    return this.http.post<ProrrogasMovimientoContable[]>(`${this.baseUrl}/comprobante/preview`, solicitud,
      { params: new HttpParams().set('fechaContable', fechaContable) });
  }

  /** Ejecuta una sola transacción financiera para la prórroga y su recaudo. */
  confirmar(solicitud: ProrrogasSolicitud, fechaContable: string): Observable<ProrrogasResultado> {
    return this.http.post<ProrrogasResultado>(`${this.baseUrl}/confirmar`, solicitud,
      { params: new HttpParams().set('fechaContable', fechaContable) });
  }

  listarCreditosPorAsociado(idDatosPersonal: number): Observable<ProrrogasCreditoResumen[]> {
    return this.pagosApi.listarCreditosPorAsociado(idDatosPersonal);
  }

  consultarCredito(idCarteraCredito: number): Observable<ProrrogasCreditoDetalle> {
    return this.pagosApi.consultarCredito(idCarteraCredito);
  }

  listarCuentasAhorro(idCarteraCredito: number): Observable<ProrrogasCuentaAhorro[]> {
    return this.pagosApi.listarCuentasAhorroRecaudo(idCarteraCredito);
  }

  listarCajas(idAgenciaReceptora: number): Observable<ProrrogasCaja[]> {
    return this.pagosApi.listarCajasRecaudo(idAgenciaReceptora);
  }
}
