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
  CentralRiesgoCatalogo,
  SolicitudCentralRiesgo,
  SolicitudCentralRiesgoDetalle,
  SolicitudCentralRiesgoGuardarRequest
} from './originacion-central-riesgo.models';


@Injectable({
  providedIn: 'root'
})
export class OriginacionCentralRiesgoApi {

  // =========================================================
  // URLS
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/originacion/central-riesgo`;

  private readonly catalogoUrl =
    `${environment.apiUrl}/cartera/catalogos/centrales-riesgo`;


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly http: HttpClient
  ) {}


  // =========================================================
  // LISTAR INFORMACIÓN POR SOLICITUD
  // =========================================================

  listarPorSolicitud(
    idSolicitudCredito: number
  ): Observable<SolicitudCentralRiesgo[]> {

    return this.http.get<SolicitudCentralRiesgo[]>(
      `${this.baseUrl}/solicitud/${idSolicitudCredito}`
    );
  }


  // =========================================================
  // LISTAR INFORMACIÓN POR DEUDOR
  // =========================================================

  listarPorDeudor(
    idSolicitudDeudor: number
  ): Observable<SolicitudCentralRiesgo[]> {

    return this.http.get<SolicitudCentralRiesgo[]>(
      `${this.baseUrl}/deudor/${idSolicitudDeudor}`
    );
  }


  // =========================================================
  // CONSULTAR DETALLE
  // =========================================================

  buscarPorId(
    idSolicitudDeudorCentral: number
  ): Observable<SolicitudCentralRiesgoDetalle> {

    return this.http.get<SolicitudCentralRiesgoDetalle>(
      `${this.baseUrl}/${idSolicitudDeudorCentral}`
    );
  }


  // =========================================================
  // GUARDAR / ACTUALIZAR
  // =========================================================

  guardar(
    request: SolicitudCentralRiesgoGuardarRequest
  ): Observable<SolicitudCentralRiesgoDetalle> {

    return this.http.post<SolicitudCentralRiesgoDetalle>(
      `${this.baseUrl}/guardar`,
      request
    );
  }


  // =========================================================
  // CATÁLOGO DE CENTRALES DE RIESGO
  // =========================================================

  listarCentralesRiesgo(): Observable<CentralRiesgoCatalogo[]> {

    return this.http.get<CentralRiesgoCatalogo[]>(
      this.catalogoUrl
    );
  }
}
