import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable, firstValueFrom } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface CierreMensualDepositosRequest {
  idAgencia: number;
  fechaCierre: string;
}

export interface CierreMensualDepositosResumen {
  totalCuentas: number;
  saldoTotal: number;
  totalDebitos: number;
  totalCreditos: number;
  totalFormas: number;
  hombres: number;
  mujeres: number;
  juridicas: number;
}

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
  fechaAperturaCuenta: string;
  fechaEstadoCuenta: string;
  saldoCierre: number;
  totalDebitos: number;
  totalCreditos: number;
  gmfCuenta: string;
  fechaGmf: string;
  plazo: number;
  cuotaMensual: number;
  fechaFinal: string;
  tasa: number;
  cuentaActiva: string;
  cuentaConjunta: string;
  accionConjunta: string;
}

export interface CierreMensualDepositosPreview {
  idCierreMensual?: number;
  idAgencia?: number;
  fechaCierre?: string;
  anio?: number;
  mes?: number;
  estado?: string;
  totalCuentas?: number;
  saldoTotal?: number;
  totalDebitos?: number;
  totalCreditos?: number;

  resumen: CierreMensualDepositosResumen;
  resumenFormas: CierreMensualDepositosResumenForma[];
  detalle: CierreMensualDepositosDetalle[];
}

export interface CierreMensualDepositosApplyResponse {
  idCierreMensual: number;
  mensaje: string;
}

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

  preview(
    request: CierreMensualDepositosRequest
  ): Observable<CierreMensualDepositosPreview> {

    return this.http.post<CierreMensualDepositosPreview>(
      `${this.url}/preview`,
      request
    );
  }

  aplicar(
    request: CierreMensualDepositosRequest
  ): Observable<CierreMensualDepositosApplyResponse> {

    return this.http.post<CierreMensualDepositosApplyResponse>(
      `${this.url}/aplicar`,
      request
    );
  }

  listar(): Promise<CierreMensualDepositosPreview[]> {
    return firstValueFrom(
      this.http.get<CierreMensualDepositosPreview[]>(
        this.url
      )
    );
  }

  obtenerPorId(
    idCierreMensual: number
  ): Promise<CierreMensualDepositosPreview> {

    return firstValueFrom(
      this.http.get<CierreMensualDepositosPreview>(
        `${this.url}/${idCierreMensual}`
      )
    );
  }

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
