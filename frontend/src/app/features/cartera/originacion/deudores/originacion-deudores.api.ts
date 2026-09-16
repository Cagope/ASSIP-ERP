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
  SolicitudDeudor,
  SolicitudDeudorAgregarRequest,
  SolicitudDeudorDetalle
} from './originacion-deudores.models';


@Injectable({
  providedIn: 'root'
})
export class OriginacionDeudoresApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/originacion/deudores`;

  constructor(
    private http: HttpClient
  ) {
  }


  // =========================================================
  // LISTAR POR SOLICITUD
  // =========================================================

  listarPorSolicitud(
    idSolicitudCredito: number
  ): Observable<SolicitudDeudor[]> {

    return this.http.get<SolicitudDeudor[]>(
      `${this.baseUrl}/solicitud/${idSolicitudCredito}`
    );
  }


  // =========================================================
  // BUSCAR POR ID
  // =========================================================

  buscarPorId(
    idSolicitudDeudor: number
  ): Observable<SolicitudDeudorDetalle> {

    return this.http.get<SolicitudDeudorDetalle>(
      `${this.baseUrl}/${idSolicitudDeudor}`
    );
  }


  // =========================================================
  // AGREGAR CODEUDOR
  // =========================================================

  agregarCodeudor(
    request: SolicitudDeudorAgregarRequest
  ): Observable<SolicitudDeudorDetalle> {

    return this.http.post<SolicitudDeudorDetalle>(
      `${this.baseUrl}/agregar`,
      request
    );
  }


  // =========================================================
  // RETIRAR CODEUDOR
  // =========================================================

  retirarCodeudor(
    idSolicitudDeudor: number
  ): Observable<void> {

    return this.http.delete<void>(
      `${this.baseUrl}/${idSolicitudDeudor}`
    );
  }

}
