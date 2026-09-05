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


// =========================================================
// RESULTADO PREPARACIÓN ANEXO 2
// =========================================================

export interface ResultadoPreparacionAnexo2 {

  idPeProceso: number;

  idCierreCartera: number;

  poblacion: number;

  moras: number;

  morasEsperadas: number;

  periodos: number;

  maxPeriodo: number;

  cortesHistoricos: number;

  corte40Valido: boolean;

  variables: number;

  modelosCalculados: number;

  deteriorosCalculados: number;

  veaCalculados: number;

  perdidasCalculadas: number;

  homologados: number;

  resultadosPersistidos: number;
}


// =========================================================
// RESULTADO
// =========================================================

export interface DetalleAnexo2 {

  idCierreCartera: number;
  fechaCorte: string;

  idCarteraCredito: number;
  idCierreCarteraCredito: number;
  idCierreCarteraResultado: number;

  idAgencia: number;
  idDatosPersonal: number;

  pagareCartera: string;

  tipoDocumento: string;
  documento: string;

  nombres: string;
  primerApellido: string;
  segundoApellido: string;
  nombreCompleto: string;

  idModeloPe: number;
  codigoModeloPe: string;
  nombreModeloPe: string;

  idLineaCredito: number;
  codigoLineaCredito: string;
  nombreLineaCredito: string;

  codigoClasificacionCredito: string;
  descripcionClasificacionCredito: string;

  tipoPersona: string;

  codigoGarantiaCredito: string;
  descripcionGarantiaCredito: string;
  tipoGarantia: string;

  valorGarantiasCredito: number;
  porcentajeGarantiasCredito: number;

  fechaDesembolso: string;
  fechaVencimiento: string;

  saldoCapital: number;
  saldoIntereses: number;
  saldoOtrosConceptos: number;

  saldoAportes: number;
  valorAportesAplicados: number;

  vea: number;
  pi: number;
  pdi: number;

  perdidaEsperada: number;
  porcentajePerdidaEsperada: number;

  deterioroCapital: number;
  deterioroIntereses: number;
  deterioroOtros: number;

  diasMora: number;

  edadRiesgoInicial: string;
  edadDeMora: string;
  edadDeRiesgo: string;
  edadPe: string;
  edadHomologada: string;
  edadContable: string;

  codigoFormaPago: string;
  codigoEstadoJuridico: string;

  esLibranza: boolean;
  esReestructurado: boolean;

  codigoMetodoCalculo: string;
}


// =========================================================
// HOJA DE TRABAJO
// =========================================================

export interface TrabajoAnexo2 {

  idCierreCartera: number;
  fechaCorte: string;

  idCarteraCredito: number;
  idCierreCarteraCredito: number;
  idDatosPersonal: number;

  pagareCartera: string;
  documento: string;
  nombreCompleto: string;

  idModeloPe: number;
  nombreModeloPe: string;

  codigoClasificacionCredito: string;
  codigoFormaPago: string;

  idEmpresaLibranza: number | null;

  tipoPersona: string;
  codigoGarantiaCredito: string;

  fechaDesembolso: string;

  diasMoraActual: number;

  edadMoraEntradaPe: string;
  edadRiesgoEntradaPe: string;

  saldoActual: number;
  saldoInteresesCausados: number;
  saldoAportesFechaCorte: number;
  valorAportesCredito: number;
  valorCostasJudiciales: number;
  valorOtrosConceptos: number;

  moraMax3m: number;
  moraMax12m: number;
  moraMax24m: number;
  moraMax36m: number;
  cantidadMora3160_3m: number;

  ea: number;
  eaContenido: number;

  fe: number;
  feContenido: string;

  valcuota: number;
  valcuotaContenido: number;

  fondplazo: number;
  fondplazoContenido: number;

  mora1230: number;
  mora1230Contenido: number;

  mora1260: number;
  mora1260Contenido: number;

  sinmora: number;
  sinmoraContenido: number;

  mora2430n: number;
  mora2430nContenido: number;

  mora315: number;
  mora315Contenido: number;

  mortrim: number;
  mortrimContenido: number;

  mora3660: number;
  mora3660MoraMax36m: number;
  mora3660MoraMax24m: number;

  betaIntercepto: number;
  betaEa: number;
  betaFe: number;
  betaValcuota: number;
  betaFondplazo: number;
  betaMora1230: number;
  betaMora1260: number;
  betaSinmora: number;
  betaMora2430n: number;
  betaMora315: number;
  betaMortrim: number;
  betaMora3660: number;

  aporteZIntercepto: number;
  aporteZEa: number;
  aporteZFe: number;
  aporteZValcuota: number;
  aporteZFondplazo: number;
  aporteZMora1230: number;
  aporteZMora1260: number;
  aporteZSinmora: number;
  aporteZMora2430n: number;
  aporteZMora315: number;
  aporteZMortrim: number;
  aporteZMora3660: number;

  z: number;
  puntaje: number;

  calificacionModelo: string;

  diasDefaultModelo: number;
  defaultPe: number;

  calificacionPe: string;
  edadDeterioro: string;

  tipoEntidadPe: number;

  calificacionBasePi: string;

  pi: number;

  baseVeaCapital: number;
  baseVeaIntereses: number;
  baseVeaCostasJudiciales: number;
  baseVeaOtros: number;
  baseVeaAportes: number;
  baseVeaAhorroPermanente: number;

  veaBruto: number;
  veaDeducciones: number;
  vea: number;

  codigoGarantiaPdi: string;
  nombreGarantiaPdi: string;

  valorGarantia: number;
  porcentajeGarantiaReconocido: number;
  valorGarantiaReconocido: number;

  diasMoraPdi: number;
  tramoPdi: number;
  diasDesdePdi: number;
  diasHastaPdi: number;

  pdi: number;

  perdidaEsperada: number;
  porcentajePerdida: number;

  baseDeterioroCapital: number;
  baseDeterioroIntereses: number;
  baseDeterioroOtros: number;
  baseDeterioroTotal: number;

  porcentajeDeterioroCapital: number;
  porcentajeDeterioroIntereses: number;
  porcentajeDeterioroOtros: number;

  valorPerdidaCapital: number;
  valorPerdidaIntereses: number;
  valorPerdidaOtros: number;

  deterioroCapitalPe: number;
  deterioroInteresesPe: number;
  deterioroOtrosPe: number;
  deterioroTotalPe: number;

  diasMoraHomologacion: number;

  edadHomologadaIndividual: string;
  edadContablePe: string;
}


// =========================================================
// MORA
// =========================================================

export interface MoraAnexo2 {

  idCierreCartera: number;
  fechaCorte: string;

  idCarteraCredito: number;
  idCierreCarteraCredito: number;

  pagareCartera: string;
  documento: string;
  nombreCompleto: string;

  idModeloPe: number;
  nombreModeloPe: string;

  periodo: number;
  fechaReferencia: string;

  diasMora: number;
}


// =========================================================
// RESUMEN
// =========================================================

export interface ResumenAnexo2 {

  idCierreCartera: number;
  fechaCorte: string;

  idModeloPe: number;
  nombreModeloPe: string;

  calificacion: string;

  cantidadCreditos: number;
  porcentajeCantidad: number;

  saldoCapital: number;
  porcentajeSaldo: number;

  perdidaEsperada: number;
  porcentajePerdidaEsperada: number;
}


// =========================================================
// API
// =========================================================

@Injectable({
  providedIn: 'root'
})
export class Anexo2CierreApi {

  private readonly http =
    inject(HttpClient);


  // =========================================================
  // URL BASE
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/anexo2`;


  // =========================================================
  // PREPARAR / PROCESAR ANEXO 2
  // =========================================================

  preparar(
    idCierreCartera: number
  ): Observable<ResultadoPreparacionAnexo2> {

    return this.http.post<ResultadoPreparacionAnexo2>(
      `${this.baseUrl}/${idCierreCartera}/preparar`,
      {}
    );
  }


  // =========================================================
  // MODELOS DEL CIERRE
  // =========================================================

  obtenerModelos(
    idCierreCartera: number
  ): Observable<number[]> {

    return this.http.get<number[]>(
      `${this.baseUrl}/${idCierreCartera}/modelos`
    );
  }


  // =========================================================
  // RESULTADO
  // =========================================================

  obtenerDetalle(
    idCierreCartera: number,
    idModeloPe: number
  ): Observable<DetalleAnexo2[]> {

    return this.http.get<DetalleAnexo2[]>(
      `${this.baseUrl}/${idCierreCartera}/modelos/${idModeloPe}/detalle`
    );
  }


  // =========================================================
  // HOJA DE TRABAJO
  // =========================================================

  obtenerTrabajo(
    idCierreCartera: number,
    idModeloPe: number
  ): Observable<TrabajoAnexo2[]> {

    return this.http.get<TrabajoAnexo2[]>(
      `${this.baseUrl}/${idCierreCartera}/modelos/${idModeloPe}/trabajo`
    );
  }


  // =========================================================
  // MORA
  // =========================================================

  obtenerMora(
    idCierreCartera: number,
    idModeloPe: number
  ): Observable<MoraAnexo2[]> {

    return this.http.get<MoraAnexo2[]>(
      `${this.baseUrl}/${idCierreCartera}/modelos/${idModeloPe}/mora`
    );
  }


  // =========================================================
  // RESUMEN
  // =========================================================

  obtenerResumen(
    idCierreCartera: number,
    idModeloPe: number
  ): Observable<ResumenAnexo2[]> {

    return this.http.get<ResumenAnexo2[]>(
      `${this.baseUrl}/${idCierreCartera}/modelos/${idModeloPe}/resumen`
    );
  }

}
