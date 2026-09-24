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
  SolicitudFormalizacionBandeja,
  SolicitudFormalizacionDetalle,
  SolicitudFormalizacionGuardarRequest,
  SolicitudFormalizacionSimulacion
} from './originacion-formalizacion.models';


// =========================================================
// BLOQUEOS Y ALERTAS DE FORMALIZACIÓN
//
// Backend:
// SolicitudFormalizacionValidacionService
// =========================================================

export interface BloqueoFormalizacion {
  tipo: string;
  mensaje: string;
}

export interface AlertaFormalizacion {
  tipo: string;
  mensaje: string;
}


// =========================================================
// RESPUESTA DE VALIDACIÓN DE FORMALIZACIÓN
//
// Backend:
// ResultadoValidacionFormalizacion
// =========================================================

export interface ResultadoValidacionFormalizacion {

  idSolicitudCredito: number;
  numeroSolicitud: string | null;

  idAgencia: number;
  idDatosPersonal: number;

  puedeContinuar: boolean;
  mensajeGeneral: string;

  cantidadBloqueos: number;
  cantidadAlertas: number;

  bloqueos: BloqueoFormalizacion[];
  alertas: AlertaFormalizacion[];

  // Detalles específicos devueltos por el backend.
  // Conservamos sus estructuras para uso posterior.

  bloqueosMora: unknown[];

  aportes: unknown | null;

  tasa: {
    tasaNominal: number | null;
    tasaEfectivaAnual: number | null;
    tasaMaximaLegal: number | null;
    cumple: boolean;
  } | null;

}

 // =========================================================
 // PROPUESTA PARA GENERACIÓN DEL PAGARÉ
 // =========================================================

 export interface PropuestaPagare {
   pagareProvisional: string;
   fechaSugerida: string;       // yyyy-MM-dd
   nombreCompleto: string;
   valorFormalizado: number;
 }

 export interface GenerarPagareRequest {
   fechaPagare: string;         // yyyy-MM-dd
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
  // BANDEJA DE FORMALIZACIÓN
  // =======================================================

  listarBandeja(): Observable<SolicitudFormalizacionBandeja[]> {

    return this.http.get<SolicitudFormalizacionBandeja[]>(
      `${this.baseUrl}/bandeja`
    );
  }


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
  // SIMULAR CONDICIONES FINANCIERAS
  //
  // Calcula TEA y cuota con las condiciones digitadas.
  // No guarda información.
  // =======================================================

  simular(
    idSolicitudCredito: number,
    request: SolicitudFormalizacionGuardarRequest
  ): Observable<SolicitudFormalizacionSimulacion> {

    return this.http.post<SolicitudFormalizacionSimulacion>(
      `${this.baseUrl}/${idSolicitudCredito}/simular`,
      request
    );
  }


  // =======================================================
  // VALIDAR CONDICIONES DIGITADAS
  //
  // Valida bloqueos y alertas de formalización.
  // No guarda las condiciones financieras.
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
    // CONSULTAR PROPUESTA DEL PAGARÉ
    //
    // Consulta el consecutivo provisional del parámetro 605.
    // No reserva ni incrementa el consecutivo.
    // =======================================================

    consultarPropuestaPagare(
      idSolicitudCredito: number
    ): Observable<PropuestaPagare> {

      return this.http.get<PropuestaPagare>(
        `${this.baseUrl}/${idSolicitudCredito}/propuesta-pagare`
      );
    }


    // =======================================================
    // GENERAR PAGARÉ
    //
    // Envía la fecha seleccionada por el asesor.
    // El backend asigna el consecutivo definitivo y registra
    // la fecha en cartera.carteras_creditos.fecha_inclusion_sistema.
    // =======================================================

    generarPagare(
      idSolicitudCredito: number,
      request: GenerarPagareRequest
    ): Observable<SolicitudFormalizacionDetalle> {

      return this.http.post<SolicitudFormalizacionDetalle>(
        `${this.baseUrl}/${idSolicitudCredito}/generar-pagare`,
        request
      );
    }


  // =======================================================
  // FINALIZAR FORMALIZACIÓN
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
