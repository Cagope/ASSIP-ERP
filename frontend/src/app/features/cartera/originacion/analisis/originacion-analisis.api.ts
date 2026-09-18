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
  SolicitudAnalisisComponente,
  SolicitudAnalisisDeudor,
  SolicitudAnalisisPersistencia,
  SolicitudAnalisisResultado
} from './originacion-analisis.models';

import {
  SolicitudEnviarAprobacionResponse,
  SolicitudValidacionAprobacion
} from '../solicitud/originacion-solicitud.models';

@Injectable({
  providedIn: 'root'
})
export class OriginacionAnalisisApi {

  // =========================================================
  // URL
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/originacion/analisis`;

  private readonly solicitudesUrl =
    `${environment.apiUrl}/cartera/originacion/solicitudes`;


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly http: HttpClient
  ) {}


  // =========================================================
  // RESULTADO GENERAL
  // =========================================================

  obtenerResultado(
    idSolicitudCredito: number
  ): Observable<SolicitudAnalisisResultado> {

    return this.http.get<SolicitudAnalisisResultado>(
      `${this.baseUrl}/${idSolicitudCredito}/resultado`
    );
  }


  // =========================================================
  // RESULTADOS POR DEUDOR
  // =========================================================

  listarDeudores(
    idSolicitudCredito: number
  ): Observable<SolicitudAnalisisDeudor[]> {

    return this.http.get<SolicitudAnalisisDeudor[]>(
      `${this.baseUrl}/${idSolicitudCredito}/deudores`
    );
  }


  // =========================================================
  // COMPONENTES
  // =========================================================

  listarComponentes(
    idSolicitudCredito: number
  ): Observable<SolicitudAnalisisComponente[]> {

    return this.http.get<SolicitudAnalisisComponente[]>(
      `${this.baseUrl}/${idSolicitudCredito}/componentes`
    );
  }


  // =========================================================
  // GUARDAR ANÁLISIS
  // =========================================================

  persistir(
    idSolicitudCredito: number
  ): Observable<SolicitudAnalisisPersistencia> {

    return this.http.post<SolicitudAnalisisPersistencia>(
      `${this.baseUrl}/${idSolicitudCredito}/persistir`,
      {}
    );
  }

  // =========================================================
  // VALIDAR PARA APROBACIÓN
  // =========================================================

  validarParaAprobacion(
    idSolicitudCredito: number
  ): Observable<SolicitudValidacionAprobacion> {

    return this.http.get<SolicitudValidacionAprobacion>(
      `${this.solicitudesUrl}/${idSolicitudCredito}/validar-aprobacion`
    );
  }

  // =========================================================
  // ENVIAR A APROBACIÓN
  // =========================================================

  enviarAprobacion(
    idSolicitudCredito: number,
    conceptoAsesorAprobacion: string
  ): Observable<SolicitudEnviarAprobacionResponse> {

    return this.http.put<SolicitudEnviarAprobacionResponse>(
      `${this.solicitudesUrl}/${idSolicitudCredito}/enviar-aprobacion`,
      {
        conceptoAsesorAprobacion:
          conceptoAsesorAprobacion.trim()
      }
    );
  }
}
