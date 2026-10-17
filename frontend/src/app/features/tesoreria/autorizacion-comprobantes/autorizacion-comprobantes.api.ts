import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import {
  ComprobanteResumen,
  ComprobanteDetalle,
  ComprobanteDecisionRequest,
  ComprobanteDecisionResponse
} from './autorizacion-comprobantes.models';

/**
 * API de autorización de comprobantes de Tesorería.
 *
 * Permite consultar comprobantes pendientes, obtener
 * su detalle y enviar decisiones al backend.
 *
 * La validación de permisos, agencia, usuario elaborador,
 * estado y contabilización corresponde al backend.
 */
@Injectable({
  providedIn: 'root'
})
export class AutorizacionComprobantesApi {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/tesoreria/autorizacion-comprobantes`;

  /**
   * Consulta los comprobantes pendientes de una agencia.
   *
   * El backend debe validar que el usuario autenticado
   * tenga acceso a la agencia solicitada.
   */
  listarPendientes(
    idAgencia: number
  ): Observable<ComprobanteResumen[]> {

    const params = new HttpParams()
      .set('idAgencia', String(idAgencia));

    return this.http.get<ComprobanteResumen[]>(
      `${this.baseUrl}/pendientes`,
      { params }
    );
  }

  /**
   * Obtiene la cabecera, las partidas contables y los
   * movimientos de caja preparados de un comprobante.
   *
   * Esta consulta no autoriza ni contabiliza movimientos.
   */
  consultarDetalle(
    idComprobante: number
  ): Observable<ComprobanteDetalle> {

    return this.http.get<ComprobanteDetalle>(
      `${this.baseUrl}/${idComprobante}`
    );
  }

  /**
   * Envía una decisión de autorización, rechazo o anulación.
   *
   * El backend ejecuta las validaciones de seguridad,
   * concurrencia y contabilización correspondientes.
   *
   * No se debe llamar directamente a otros procesos
   * de aplicación de movimientos desde el frontend.
   */
  procesarDecision(
    idComprobante: number,
    request: ComprobanteDecisionRequest
  ): Observable<ComprobanteDecisionResponse> {

    return this.http.post<ComprobanteDecisionResponse>(
      `${this.baseUrl}/${idComprobante}/decision`,
      request
    );
  }
}
