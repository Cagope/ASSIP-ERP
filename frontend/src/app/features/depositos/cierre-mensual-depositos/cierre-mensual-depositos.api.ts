import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable, firstValueFrom } from 'rxjs';
import { environment } from '../../../../environments/environment';


// =========================================================
// REQUEST
//
// El cierre mensual es centralizado.
//
// Una fecha de corte genera una sola fotografía
// para todas las agencias.
// =========================================================

export interface CierreMensualDepositosRequest {
  fechaCierre: string;
}


// =========================================================
// RESUMEN GENERAL DE LA ENTIDAD
// =========================================================

export interface CierreMensualDepositosResumen {
  totalCuentas: number;
  saldoTotal: number;
  totalDebitos: number;
  totalCreditos: number;

  hombres: number;
  mujeres: number;
  juridicas: number;
}


// =========================================================
// RESUMEN POR AGENCIA
// =========================================================

export interface CierreMensualDepositosResumenAgencia {
  idAgencia: number;

  totalCuentas: number;

  saldoTotal: number;
  totalDebitos: number;
  totalCreditos: number;

  hombres: number;
  mujeres: number;
  juridicas: number;
}


// =========================================================
// RESUMEN POR AGENCIA + FORMA DE AHORRO
// =========================================================

export interface CierreMensualDepositosResumenForma {
  idAgencia: number;

  idFormaAhorro: number;
  codigoForma: string;
  nombreForma: string;

  cantidadCuentas: number;

  saldoTotal: number;
  totalDebitos: number;
  totalCreditos: number;

  hombres: number;
  mujeres: number;
  juridicas: number;
}


// =========================================================
// DETALLE DE LA FOTOGRAFÍA
//
// Solo cuentas cuyo saldo al corte sea diferente de cero.
// =========================================================

export interface CierreMensualDepositosDetalle {
  idAgencia: number;

  idFormaAhorro: number;
  codigoForma: string;
  nombreForma: string;

  idCuentaAhorro: number;
  codigoCuenta: string;

  idDatosPersonal: number;
  documento: string;
  nombreCompleto: string;

  tipoPersona: string;
  codigoGenero: string;
  nombreGenero: string;

  estadoCuenta: string;
  fechaAperturaCuenta: string | null;
  fechaEstadoCuenta: string | null;

  saldoCierre: number;
  totalDebitos: number;
  totalCreditos: number;

  gmfCuenta: string;
  fechaGmf: string | null;

  plazo: number | null;
  cuotaMensual: number | null;
  fechaFinal: string | null;
  tasa: number | null;

  cuentaActiva: string;
  cuentaConjunta: string;
  accionConjunta: string;
}


// =========================================================
// PREVIEW / CIERRE CONSULTADO
// =========================================================

export interface CierreMensualDepositosPreview {

  // -------------------------------------------------------
  // CABECERA
  // -------------------------------------------------------

  idCierreMensual?: number;

  fechaCierre?: string;

  anio?: number;
  mes?: number;

  estado?: string;

  totalCuentas?: number;

  saldoTotal?: number;
  totalDebitos?: number;
  totalCreditos?: number;


  // -------------------------------------------------------
  // RESULTADOS
  // -------------------------------------------------------

  resumen: CierreMensualDepositosResumen;

  resumenAgencias: CierreMensualDepositosResumenAgencia[];

  resumenFormas: CierreMensualDepositosResumenForma[];

  detalle: CierreMensualDepositosDetalle[];
}


// =========================================================
// RESPUESTA DE GENERAR / REGENERAR / CERRAR
// =========================================================

export interface CierreMensualDepositosApplyResponse {
  idCierreMensual: number;
  mensaje: string;
}


// =========================================================
// API
// =========================================================

@Injectable({
  providedIn: 'root'
})
export class CierreMensualDepositosApi {

  private readonly url =
    `${environment.apiUrl}/depositos/cierre-mensual-depositos`;

  constructor(
    private http: HttpClient
  ) {
  }


  // =======================================================
  // PREVIEW
  //
  // No persiste información.
  // =======================================================

  preview(
    request: CierreMensualDepositosRequest
  ): Observable<CierreMensualDepositosPreview> {

    return this.http.post<CierreMensualDepositosPreview>(
      `${this.url}/preview`,
      request
    );
  }


  // =======================================================
  // GENERAR FOTOGRAFÍA
  //
  // Estado resultante:
  //
  // P = En proceso
  // =======================================================

  generar(
    request: CierreMensualDepositosRequest
  ): Observable<CierreMensualDepositosApplyResponse> {

    return this.http.post<CierreMensualDepositosApplyResponse>(
      `${this.url}/generar`,
      request
    );
  }


  // =======================================================
  // REGENERAR FOTOGRAFÍA
  //
  // Solo estado P.
  // =======================================================

  regenerar(
    idCierreMensual: number
  ): Observable<CierreMensualDepositosApplyResponse> {

    return this.http.post<CierreMensualDepositosApplyResponse>(
      `${this.url}/${idCierreMensual}/regenerar`,
      null
    );
  }


  // =======================================================
  // CERRAR FOTOGRAFÍA EN FIRME
  //
  // P -> C
  // =======================================================

  cerrar(
    idCierreMensual: number
  ): Observable<CierreMensualDepositosApplyResponse> {

    return this.http.post<CierreMensualDepositosApplyResponse>(
      `${this.url}/${idCierreMensual}/cerrar`,
      null
    );
  }


  // =======================================================
  // LISTAR CIERRES
  // =======================================================

  listar(): Promise<CierreMensualDepositosPreview[]> {

    return firstValueFrom(
      this.http.get<CierreMensualDepositosPreview[]>(
        this.url
      )
    );
  }


  // =======================================================
  // OBTENER CIERRE POR ID
  // =======================================================

  obtenerPorId(
    idCierreMensual: number
  ): Promise<CierreMensualDepositosPreview> {

    return firstValueFrom(
      this.http.get<CierreMensualDepositosPreview>(
        `${this.url}/${idCierreMensual}`
      )
    );
  }


  // =======================================================
  // ELIMINAR PRECierre
  //
  // Solo estado P.
  // =======================================================

  eliminar(
    idCierreMensual: number
  ): Promise<void> {

    return firstValueFrom(
      this.http.delete<void>(
        `${this.url}/${idCierreMensual}`
      )
    );
  }
}
