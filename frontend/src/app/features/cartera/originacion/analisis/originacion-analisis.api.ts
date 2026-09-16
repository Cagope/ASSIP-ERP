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


@Injectable({
  providedIn: 'root'
})
export class OriginacionAnalisisApi {

  // =========================================================
  // URL
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/originacion/analisis`;


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
}
