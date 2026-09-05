import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  environment
} from '../../../../../environments/environment';

export interface ResumenEdadMora {
  codigoClasificacion: string;
  nombreClasificacion: string;
  edadMora: string;
  cantidadCreditos: number;
  saldoCapital: number;
  porcentajeCantidad: number;
  porcentajeSaldo: number;
}

export interface ResumenControlesCalculos {
  cantidadResultados: number;
  sinEdadMora: number;
  diasMoraNegativos: number;
  edadesRiesgoInvalidas: number;
  edadesReestructuracionInvalidas: number;
  noReestructuradosInconsistentes: number;
  cantidadReestructurados: number;
  cantidadUnaSolaCuota: number;
  procesoConsistente: boolean;
}

export interface ResumenAportesGarantias {

  // =========================================================
  // APORTES
  // =========================================================

  creditosConAportes: number;
  creditosSinAportes: number;
  saldoAportesDisponible: number;
  valorAportesProrrateado: number;

  // =========================================================
  // GARANTÍAS
  // =========================================================

  creditosConGarantia: number;
  creditosSinGarantia: number;
  cantidadBienesGarantia: number;
  valorBienesGarantia: number;
  valorGarantiasAsignado: number;
}

export interface DetalleCalculosCierre {

  idCierreCartera: number;
  fechaCorte: string;

  idCarteraCredito: number;
  idCierreCarteraCredito: number;
  idCierreCarteraResultado: number;
  idAgencia: number;

  pagareCartera: string;

  idDatosPersonal: number;
  tipoDocumento: string;
  documento: string;
  nombres: string;
  primerApellido: string;
  segundoApellido: string;
  nombreCompleto: string;

  idLineaCredito: number;
  codigoLineaCredito: string;
  nombreLineaCredito: string;

  codigoClasificacionCredito: string;
  descripcionClasificacionCredito: string;

  saldoFotografia: number;

  diasMora: number;
  edadMoraCalculada: string;

  cantidadCreditosAsociado: number;
  saldoTotalCreditosAsociado: number;
  saldoAportesFechaCorte: number;
  porcentajeAportesCredito: number;
  valorAportesCredito: number;

  cantidadBienesGarantia: number;
  valorGarantiasTotal: number;
  porcentajeGarantiasCredito: number;
  valorGarantiasCredito: number;
}

export interface CierreCalculosRespuesta {

  idCierreCartera: number;
  fechaCorte: string;

  estadoCierre: string;

  estadoFotografia: string;
  fechaFotografiaFirme: string | null;

  estadoCalculos: string;
  fechaCalculosInicio: string | null;
  fechaCalculosFirme: string | null;

  estadoAnexo1: string;
  fechaAnexo1Inicio: string | null;
  fechaAnexo1Firme: string | null;

  estadoAnexo2: string;
  fechaAnexo2Inicio: string | null;
  fechaAnexo2Firme: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class CalculosCierreApi {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/calculos-previos`;

  // =========================================================
  // EJECUTAR CÁLCULOS PREVIOS
  // =========================================================

  ejecutar(
    idCierreCartera: number
  ): Observable<number> {

    return this.http.post<number>(
      `${this.baseUrl}/${idCierreCartera}/ejecutar`,
      {}
    );

  }

  // =========================================================
  // CERRAR CÁLCULOS EN FIRME
  // =========================================================

  cerrarCalculos(
    idCierreCartera: number
  ): Observable<CierreCalculosRespuesta> {

    return this.http.post<CierreCalculosRespuesta>(
      `${this.baseUrl}/${idCierreCartera}/cerrar`,
      {}
    );

  }

  // =========================================================
  // RESUMEN POR CLASIFICACIÓN Y EDAD DE MORA
  // =========================================================

  obtenerResumenEdadMora(
    idCierreCartera: number
  ): Observable<ResumenEdadMora[]> {

    return this.http.get<ResumenEdadMora[]>(
      `${this.baseUrl}/${idCierreCartera}/resumen-edad-mora`
    );

  }

  // =========================================================
  // RESUMEN DE CONTROLES DE CÁLCULOS
  // =========================================================

  obtenerResumenControles(
    idCierreCartera: number
  ): Observable<ResumenControlesCalculos> {

    return this.http.get<ResumenControlesCalculos>(
      `${this.baseUrl}/${idCierreCartera}/resumen-controles`
    );

  }

  // =========================================================
  // RESUMEN DE APORTES Y GARANTÍAS
  // =========================================================

  obtenerResumenAportesGarantias(
    idCierreCartera: number
  ): Observable<ResumenAportesGarantias> {

    return this.http.get<ResumenAportesGarantias>(
      `${this.baseUrl}/${idCierreCartera}/resumen-aportes-garantias`
    );

  }

  // =========================================================
  // DETALLE DE CÁLCULOS PARA EXCEL
  // =========================================================

  obtenerDetalleCalculos(
    idCierreCartera: number
  ): Observable<DetalleCalculosCierre[]> {

    return this.http.get<DetalleCalculosCierre[]>(
      `${this.baseUrl}/${idCierreCartera}/detalle-calculos`
    );

  }

}
