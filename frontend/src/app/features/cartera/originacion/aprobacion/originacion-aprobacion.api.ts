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
  SolicitudAprobacionActuacion,
  SolicitudAprobacionBandeja,
  SolicitudAprobacionDecision,
  SolicitudAprobacionDecisionRequest,
  SolicitudAprobacionFotos
} from './originacion-aprobacion.models';


@Injectable({
  providedIn: 'root'
})
export class OriginacionAprobacionApi {

  // =========================================================
  // URL
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/originacion/aprobacion`;


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly http: HttpClient
  ) {}


  // =========================================================
  // BANDEJA DEL USUARIO AUTENTICADO
  // =========================================================

  listarBandeja(): Observable<SolicitudAprobacionBandeja[]> {

    return this.http.get<SolicitudAprobacionBandeja[]>(
      `${this.baseUrl}/bandeja`
    );
  }


  // =========================================================
  // CATÁLOGO DE DECISIONES
  // =========================================================

  listarDecisiones(): Observable<SolicitudAprobacionDecision[]> {

    return this.http.get<SolicitudAprobacionDecision[]>(
      `${this.baseUrl}/decisiones`
    );
  }


  // =========================================================
  // HISTORIAL DE APROBACIÓN
  // =========================================================

  listarHistorial(
    idSolicitudCredito: number
  ): Observable<SolicitudAprobacionActuacion[]> {

    return this.http.get<SolicitudAprobacionActuacion[]>(
      `${this.baseUrl}/${idSolicitudCredito}/historial`
    );
  }


  // =========================================================
  // FOTOGRAFÍAS ACTUALES DE LA SOLICITUD
  // =========================================================

  obtenerFotos(
    idSolicitudCredito: number
  ): Observable<SolicitudAprobacionFotos> {

    return this.http.get<SolicitudAprobacionFotos>(
      `${this.baseUrl}/${idSolicitudCredito}/fotos`
    );
  }


  // =========================================================
  // FOTOGRAFÍAS HISTÓRICAS DE UNA ACTUACIÓN
  // =========================================================

  obtenerFotosActuacion(
    idSolicitudCredito: number,
    idSolicitudAprobacion: number
  ): Observable<SolicitudAprobacionFotos> {

    return this.http.get<SolicitudAprobacionFotos>(
      `${this.baseUrl}/${idSolicitudCredito}` +
      `/actuaciones/${idSolicitudAprobacion}/fotos`
    );
  }


  // =========================================================
  // REGISTRAR DECISIÓN DE APROBACIÓN
  // =========================================================

  registrarDecision(
    idSolicitudCredito: number,
    request: SolicitudAprobacionDecisionRequest
  ): Observable<number> {

    return this.http.post<number>(
      `${this.baseUrl}/${idSolicitudCredito}/decision`,
      request
    );
  }

}
