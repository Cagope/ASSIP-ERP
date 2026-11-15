import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PagoCuotasCreditoResumen } from '../pago-cuotas_v1/pago-cuotas_v1.models';
import { ConfirmarReversionProrrogaRequest, ProrrogaResumen, ResultadoReversionProrroga, VistaPreviaReversionProrroga } from './reversion-prorrogas.models';

/** Contratos HTTP específicos de Reversión de Prórrogas V1. */
@Injectable({ providedIn: 'root' })
export class ReversionProrrogasApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/cartera/reversion-prorrogas`;

  /** Lista créditos del asociado; el backend aplica el permiso funcional. */
  listarCreditos(idAsociado: number): Observable<PagoCuotasCreditoResumen[]> {
    return this.http.get<PagoCuotasCreditoResumen[]>(`${this.baseUrl}/asociados/${idAsociado}/creditos`);
  }

  /** Lista prórrogas de un crédito autorizado. */
  listarProrrogas(idCredito: number): Observable<ProrrogaResumen[]> {
    return this.http.get<ProrrogaResumen[]>(`${this.baseUrl}/creditos/${idCredito}/prorrogas`);
  }

  /** Consulta el impacto financiero sin ejecutar la reversión. */
  consultarVistaPrevia(idCredito: number, idProrroga: number): Observable<VistaPreviaReversionProrroga> {
    return this.http.get<VistaPreviaReversionProrroga>(
      `${this.baseUrl}/creditos/${idCredito}/prorrogas/${idProrroga}/vista-previa`);
  }

  /** Confirma la reversión con fecha y justificación; las validaciones son del backend. */
  confirmar(idCredito: number, idProrroga: number, solicitud: ConfirmarReversionProrrogaRequest): Observable<ResultadoReversionProrroga> {
    return this.http.post<ResultadoReversionProrroga>(
      `${this.baseUrl}/creditos/${idCredito}/prorrogas/${idProrroga}/confirmar`, solicitud);
  }
}
