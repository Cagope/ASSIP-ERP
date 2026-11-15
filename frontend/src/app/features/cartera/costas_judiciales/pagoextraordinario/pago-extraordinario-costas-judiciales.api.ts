import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { ConsultaCreditosApi } from '../../consulta-creditos/consulta-creditos.api';
import { ConsultaCreditoResumen, ConsultaCreditoDetalle } from '../../consulta-creditos/consulta-creditos.models';
import { PagoCuotasCajaRecaudo } from '../../pago-cuotas_v1/pago-cuotas_v1.models';
import { PagoCostasCargo, PagoCostasCuentaAhorro, PagoCostasResultado, PagoCostasSolicitud, PagoCostasVistaPrevia } from './pago-extraordinario-costas-judiciales.models';

/** Utiliza los endpoints reales de Costass y la consulta de créditos ya existente. */
@Injectable({ providedIn: 'root' })
export class PagoExtraordinarioCostasJudicialesApi {
  private readonly baseUrl = `${environment.apiUrl}/cartera/costas-judiciales/pago-extraordinario`;
  constructor(private readonly http: HttpClient, private readonly creditos: ConsultaCreditosApi) {}

  listarCreditosPorAsociado(idDatosPersonal: number): Observable<ConsultaCreditoResumen[]> {
    return this.creditos.listarPorPersona(idDatosPersonal);
  }
  consultarDetalleCredito(idCredito: number): Observable<ConsultaCreditoDetalle> {
    return this.creditos.buscarPorId(idCredito);
  }
  /** Consulta el selector de cajas ya existente en Pago de Cuotas V1. */
  listarCajasRecaudo(idAgencia: number): Observable<PagoCuotasCajaRecaudo[]> {
    return this.http.get<PagoCuotasCajaRecaudo[]>(
      `${environment.apiUrl}/cartera/pago-cuotas-v1/medios-recaudo/cajas`,
      { params: { idAgenciaReceptora: String(idAgencia) } }
    );
  }
  consultarSaldo(idCredito: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/creditos/${idCredito}/saldo`);
  }
  consultarCuentasAhorro(idCredito: number): Observable<PagoCostasCuentaAhorro[]> {
    return this.http.get<PagoCostasCuentaAhorro[]>(`${this.baseUrl}/creditos/${idCredito}/cuentas-ahorro`);
  }
  consultarCargos(idCredito: number): Observable<PagoCostasCargo[]> {
    return this.http.get<PagoCostasCargo[]>(`${this.baseUrl}/creditos/${idCredito}/cargos`);
  }
  previsualizar(solicitud: PagoCostasSolicitud): Observable<PagoCostasVistaPrevia> {
    return this.http.post<PagoCostasVistaPrevia>(`${this.baseUrl}/vista-previa`, solicitud);
  }
  confirmar(solicitud: PagoCostasSolicitud): Observable<PagoCostasResultado> {
    return this.http.post<PagoCostasResultado>(`${this.baseUrl}/confirmar`, solicitud);
  }
}
