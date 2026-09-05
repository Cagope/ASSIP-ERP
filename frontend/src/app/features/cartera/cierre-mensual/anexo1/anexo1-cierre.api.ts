import {
  HttpClient
} from '@angular/common/http';

import {
  Injectable,
  inject
} from '@angular/core';

import {
  Observable
} from 'rxjs';

import {
  environment
} from '../../../../../environments/environment';

import {
  CierreMensualCartera
} from '../cierre-mensual-cartera.api';


// =========================================================
// RESULTADO ANEXO 1
// =========================================================

export interface ResultadoAnexo1 {

  edadesContables: number;

  causacionesIntereses: number;

  causacionesContingentes: number;

  interesesConsolidados: number;

  deteriorosCapital: number;

  deteriorosIntereses: number;
}


// =========================================================
// DETALLE ANEXO 1
// =========================================================

export interface DetalleAnexo1 {

  // =======================================================
  // CIERRE
  // =======================================================

  idCierreCartera: number;

  fechaCorte: string;


  // =======================================================
  // IDENTIFICACIÓN
  // =======================================================

  idCarteraCredito: number;

  idCierreCarteraCredito: number;

  idCierreCarteraResultado: number;

  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;

  idDatosPersonal: number;

  pagareCartera: string;

  tipoDocumento: string;

  documento: string;

  nombres: string;

  primerApellido: string;

  segundoApellido: string;

  nombreCompleto: string;


  // =======================================================
  // LÍNEA
  // =======================================================

  idLineaCredito: number;

  codigoLineaCredito: string;

  nombreLineaCredito: string;


  // =======================================================
  // CLASIFICACIÓN DEL CRÉDITO
  // =======================================================

  codigoClasificacionCredito: string;

  descripcionClasificacionCredito: string;


  // =======================================================
  // GARANTÍA DEL CRÉDITO
  // =======================================================

  codigoGarantiaCredito: string;

  descripcionGarantiaCredito: string;

  tipoGarantia: string;


  // =======================================================
  // TIPO DE PERSONA
  // =======================================================

  tipoPersona: string;


  // =======================================================
  // MORA Y EDADES
  // =======================================================

  diasMora: number;

  edadRiesgoInicial: string;

  edadDeMora: string;

  edadDeRiesgo: string;

  edadContable: string;


  // =======================================================
  // SALDOS DEL CRÉDITO
  // =======================================================

  saldoCapital: number;

  tasaNominalAnual: number;


  // =======================================================
  // CAUSACIÓN DE INTERESES
  // =======================================================

  valorInteresesCausadosMes: number;

  saldoInteresesCausados: number;

  valorInteresesContingentesMes: number;

  saldoInteresesContingentes: number;


  // =======================================================
  // APORTES
  // =======================================================

  saldoAportesFechaCorte: number;

  porcentajeAportesCredito: number;

  valorAportesCredito: number;


  // =======================================================
  // GARANTÍAS PRORRATEADAS
  // =======================================================

  cantidadBienesGarantia: number;

  valorGarantiasTotal: number;

  porcentajeGarantiasCredito: number;

  valorGarantiasCredito: number;


  // =======================================================
  // GARANTÍAS CALCULADAS PARA ANEXO 1
  // =======================================================

  porcentajeAplicacionGarantia: number;

  valorGarantiaReconocida: number;


  // =======================================================
  // DETERIORO DE CAPITAL - ANEXO 1
  // =======================================================

  porcentajeDeterioroCapital: number;

  baseDeterioroCapital: number;

  deterioroCapital: number;


  // =======================================================
  // DETERIORO DE INTERESES - ANEXO 1
  // =======================================================

  porcentajeDeterioroIntereses: number;

  deterioroIntereses: number;


  // =======================================================
  // CONTROL
  // =======================================================

  codigoMetodoCalculo: string;
}


// =========================================================
// DETALLE POR EDAD CONTABLE
// =========================================================

export interface EdadAnexo1 {

  edadContable: string;

  cantidadCreditos: number;

  saldoCapital: number;

  valorAportesAplicados: number;

  valorGarantiasAsignadas: number;

  valorGarantiasReconocidas: number;

  cantidadCreditosConInteresesCausados: number;

  valorInteresesCausadosMes: number;

  cantidadCreditosConInteresesContingentes: number;

  valorInteresesContingentesMes: number;

  baseDeterioroCapital: number;

  deterioroCapital: number;

  deterioroIntereses: number;

  deterioroTotal: number;
}


// =========================================================
// RESUMEN ANEXO 1
// =========================================================

export interface ResumenAnexo1 {

  idCierreCartera: number;

  fechaCorte: string;

  cantidadCreditos: number;

  saldoCapital: number;

  edades: EdadAnexo1[];
}


// =========================================================
// API
// =========================================================

@Injectable({
  providedIn: 'root'
})
export class Anexo1CierreApi {

  private readonly http =
    inject(HttpClient);


  // =========================================================
  // URL BASE
  //
  // Backend:
  // /api/v1/cartera/anexo1
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/anexo1`;


  // =========================================================
  // CALCULAR SOLO EDAD CONTABLE
  // =========================================================

  calcularEdadContable(
    idCierreCartera: number
  ): Observable<number> {

    return this.http.post<number>(
      `${this.baseUrl}/${idCierreCartera}/edad-contable`,
      {}
    );
  }


  // =========================================================
  // PROCESAR ANEXO 1 COMPLETO
  // =========================================================

  procesar(
    idCierreCartera: number
  ): Observable<ResultadoAnexo1> {

    return this.http.post<ResultadoAnexo1>(
      `${this.baseUrl}/${idCierreCartera}/procesar`,
      {}
    );
  }


  // =========================================================
  // CONSULTAR RESUMEN
  // =========================================================

  obtenerResumen(
    idCierreCartera: number
  ): Observable<ResumenAnexo1> {

    return this.http.get<ResumenAnexo1>(
      `${this.baseUrl}/${idCierreCartera}/resumen`
    );
  }


  // =========================================================
  // CONSULTAR DETALLE
  // =========================================================

  obtenerDetalle(
    idCierreCartera: number
  ): Observable<DetalleAnexo1[]> {

    return this.http.get<DetalleAnexo1[]>(
      `${this.baseUrl}/${idCierreCartera}/detalle`
    );
  }


  // =========================================================
  // CERRAR ANEXO 1 EN FIRME
  // =========================================================

  cerrar(
    idCierreCartera: number
  ): Observable<CierreMensualCartera> {

    return this.http.post<CierreMensualCartera>(
      `${this.baseUrl}/${idCierreCartera}/cerrar`,
      {}
    );
  }

}
