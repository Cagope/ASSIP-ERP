import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';

import { OriginacionSolicitudApi } from '../solicitud/originacion-solicitud.api';

import {
  FormaPago,
  ModalidadInteres,
  TipoCuota
} from '../solicitud/originacion-solicitud.models';

import {
  SolicitudFormalizacionDetalle,
  SolicitudFormalizacionGuardarRequest
} from './originacion-formalizacion.models';


// =========================================================
// RESPUESTA DE VALIDACIÓN DE FORMALIZACIÓN
//
// Backend:
// SolicitudFormalizacionValidacionService
// .ResultadoValidacionFormalizacion
// =========================================================

export interface ResultadoValidacionFormalizacion {
  puedeContinuar: boolean;
  bloqueos: string[];

  // El backend devuelve también el detalle del cálculo
  // de tasa. Lo conservamos para uso posterior.
  tasa: {
    tasaEfectivaAnual: number;
    [campo: string]: unknown;
  } | null;

  [campo: string]: unknown;
}


@Injectable({
  providedIn: 'root'
})
export class OriginacionFormalizacionApi {

  private readonly http = inject(HttpClient);

  private readonly solicitudApi = inject(OriginacionSolicitudApi);

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/originacion/formalizacion`;


  // =======================================================
  // CONSULTAR FORMALIZACIÓN
  // =======================================================

  consultar(
    idSolicitudCredito: number
  ): Observable<SolicitudFormalizacionDetalle> {

    return this.http.get<SolicitudFormalizacionDetalle>(
      `${this.baseUrl}/${idSolicitudCredito}`
    );
  }


  // =======================================================
  // VALIDAR CONDICIONES DIGITADAS
  //
  // No guarda las condiciones financieras.
  // No genera pagaré.
  // =======================================================

  validar(
    idSolicitudCredito: number,
    request: SolicitudFormalizacionGuardarRequest
  ): Observable<ResultadoValidacionFormalizacion> {

    return this.http.post<ResultadoValidacionFormalizacion>(
      `${this.baseUrl}/${idSolicitudCredito}/validar`,
      request
    );
  }


  // =======================================================
  // GUARDAR CONDICIONES DEFINITIVAS
  // =======================================================

  guardar(
    idSolicitudCredito: number,
    request: SolicitudFormalizacionGuardarRequest
  ): Observable<SolicitudFormalizacionDetalle> {

    return this.http.put<SolicitudFormalizacionDetalle>(
      `${this.baseUrl}/${idSolicitudCredito}`,
      request
    );
  }


  // =======================================================
  // GENERAR PAGARÉ
  //
  // Revalida las condiciones guardadas.
  // Constituye el crédito en estado P.
  // Vincula el crédito con la solicitud.
  //
  // No recibe condiciones digitadas ni consume
  // un consecutivo durante la impresión.
  // =======================================================

  generarPagare(
    idSolicitudCredito: number
  ): Observable<SolicitudFormalizacionDetalle> {

    return this.http.post<SolicitudFormalizacionDetalle>(
      `${this.baseUrl}/${idSolicitudCredito}/generar-pagare`,
      null
    );
  }


  // =======================================================
  // FINALIZAR FORMALIZACIÓN
  //
  // Proceso 4 → Proceso 5.
  // Requiere crédito vinculado en estado P.
  // =======================================================

  finalizar(
    idSolicitudCredito: number
  ): Observable<SolicitudFormalizacionDetalle> {

    return this.http.post<SolicitudFormalizacionDetalle>(
      `${this.baseUrl}/${idSolicitudCredito}/finalizar`,
      null
    );
  }


  // =======================================================
  // CATÁLOGOS EXISTENTES
  // =======================================================

  listarFormasPago(): Observable<FormaPago[]> {

    return this.solicitudApi.listarFormasPago();
  }

  listarModalidadesInteres(): Observable<ModalidadInteres[]> {

    return this.solicitudApi.listarModalidadesInteres();
  }

  listarTiposCuota(): Observable<TipoCuota[]> {

    return this.solicitudApi.listarTiposCuota();
  }

}
