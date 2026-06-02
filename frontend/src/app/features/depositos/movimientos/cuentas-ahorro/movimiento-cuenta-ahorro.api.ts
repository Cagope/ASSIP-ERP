import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface EvaluacionSarlaftDTO {
  alerta: boolean;
  severidad: string;
  descripcion: string;
  idAlerta?: number;
  nombreRegla?: string;
  bloqueaOperacion?: boolean;
  accionRecomendada?: string;
  alertas?: EvaluacionSarlaftDTO[];
}

export interface CuentaMovimientoDTO {
  idCuentaAhorro: number;
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;

  idFormaAhorro: number;
  codigoForma: string;
  nombreForma: string;
  codigoCuenta: string;

  idDatosPersonal: number;
  documento: string;
  nombreAsociado: string;

  saldoActualCuenta: number;
  valorEnCanje: number;

  estadoCuenta: string;
  descripcionEstadoCuenta: string;
  estadoOperativo: boolean;
  cuentaActiva: string;
  gmfCuenta: string;

  tipoDocumentoSoporte: string;
  numeroInicialLibreta: string;
  numeroFinalLibreta: string;

  documentoPoder: string;
  nombrePoder: string;
  telefonoPoder: string;
  celularPoder: string;

  cuentaConjuntaReal: boolean;
  conjuntos: string;

  mensajeOperativo: string;
  fechaAperturaCuenta: string;
}

export interface TipoMovimientoDTO {
  codigoMovimiento: string;
  descripcion: string;
  accionMovimiento: string;
  contabilizacionDiaria: boolean;
  generaGmf: boolean;
  permiteInclusionManual: boolean;
}

export interface MovimientoCuentaRequestDTO {
  idCuentaAhorro: number;
  idAgencia: number;
  fechaMovimiento: string;
  tipoMovimiento: string;
  valorMovimiento: number;
  tipoComprobante?: string;
  numeroComprobante?: string;
  detalle?: string;
}

export interface MovimientoCuentaPreviewDTO {
  idCuentaAhorro: number;

  codigoCuenta: string;
  documento: string;
  nombreAsociado: string;

  codigoForma: string;
  nombreForma: string;

  tipoMovimiento: string;
  descripcionMovimiento: string;
  accionMovimiento: string;

  fechaMovimiento: string;

  saldoActual: number;
  valorMovimiento: number;
  valorGmf: number;
  saldoFinal: number;

  contabilizacionDiaria: boolean;
  generaGmf: boolean;

  sarlaft?: EvaluacionSarlaftDTO;
}

export interface MovimientoCuentaResponseDTO {
  idCuentaAhorro: number;
  codigoCuenta: string;
  documento: string;
  nombreAsociado: string;
  tipoMovimiento: string;
  descripcionMovimiento: string;
  fechaMovimiento: string;
  valorMovimiento: number;
  valorGmf: number;
  saldoAnterior: number;
  saldoFinal: number;
  tipoComprobante: string;
  numeroComprobante: string;
  mensaje: string;
}

export interface MovimientoCuentaFiltros {
  idAgencia: number;
  documento?: string;
  nombres?: string;
  primerApellido?: string;
  segundoApellido?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MovimientoCuentaAhorroApi {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/depositos/movimientos/cuentas-ahorro`;

  buscarCuentas(
    filtros: MovimientoCuentaFiltros
  ): Observable<CuentaMovimientoDTO[]> {

    let params = new HttpParams()
      .set('idAgencia', String(filtros.idAgencia));

    params = this.setParam(params, 'documento', filtros.documento);
    params = this.setParam(params, 'nombres', filtros.nombres);
    params = this.setParam(params, 'primerApellido', filtros.primerApellido);
    params = this.setParam(params, 'segundoApellido', filtros.segundoApellido);

    return this.http.get<CuentaMovimientoDTO[]>(
      `${this.baseUrl}/buscar-cuentas`,
      { params }
    );
  }

  listarTiposMovimiento(): Observable<TipoMovimientoDTO[]> {
    return this.http.get<TipoMovimientoDTO[]>(
      `${this.baseUrl}/tipos-movimiento`
    );
  }

  preview(
    request: MovimientoCuentaRequestDTO
  ): Observable<MovimientoCuentaPreviewDTO> {
    return this.http.post<MovimientoCuentaPreviewDTO>(
      `${this.baseUrl}/preview`,
      this.normalizarRequest(request)
    );
  }

  aplicar(
    request: MovimientoCuentaRequestDTO
  ): Observable<MovimientoCuentaResponseDTO> {
    return this.http.post<MovimientoCuentaResponseDTO>(
      `${this.baseUrl}/aplicar`,
      this.normalizarRequest(request)
    );
  }

  private setParam(
    params: HttpParams,
    key: string,
    value?: string
  ): HttpParams {
    const limpio = this.clean(value);

    return limpio
      ? params.set(key, limpio)
      : params;
  }

  private normalizarRequest(
    request: MovimientoCuentaRequestDTO
  ): MovimientoCuentaRequestDTO {
    return {
      ...request,
      tipoMovimiento: this.clean(request.tipoMovimiento) || '',
      tipoComprobante: this.clean(request.tipoComprobante),
      numeroComprobante: this.clean(request.numeroComprobante),
      detalle: this.clean(request.detalle)
    };
  }

  private clean(value?: string): string | undefined {
    const limpio = value?.trim();

    return limpio && limpio.length > 0
      ? limpio
      : undefined;
  }
}

