import {
  Injectable
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  environment
} from '../../../../../environments/environment';

import {
  SolicitudFinanciero,
  SolicitudFinancieroDetalle,
  SolicitudFinancieroGuardarRequest
} from './originacion-financiero.models';


@Injectable({
  providedIn: 'root'
})
export class OriginacionFinancieroApi {

  // =========================================================
  // URL
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/originacion/financiero`;


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly http: HttpClient
  ) {}


  // =========================================================
  // LISTAR FINANCIEROS DE LA SOLICITUD
  // =========================================================

  listarPorSolicitud(
    idSolicitudCredito: number
  ): Observable<SolicitudFinanciero[]> {

    return this.http.get<SolicitudFinanciero[]>(
      `${this.baseUrl}/solicitud/${idSolicitudCredito}`
    );
  }


  // =========================================================
  // CONSULTAR FINANCIERO DE UN DEUDOR
  // =========================================================

  buscarPorDeudor(
    idSolicitudDeudor: number
  ): Observable<SolicitudFinancieroDetalle> {

    return this.http.get<SolicitudFinancieroDetalle>(
      `${this.baseUrl}/deudor/${idSolicitudDeudor}`
    );
  }


  // =========================================================
  // GUARDAR / ACTUALIZAR INFORMACIÓN FINANCIERA
  // =========================================================

  guardar(
    request: SolicitudFinancieroGuardarRequest
  ): Observable<SolicitudFinancieroDetalle> {

    return this.http.post<SolicitudFinancieroDetalle>(
      `${this.baseUrl}/guardar`,
      request
    );
  }
}
