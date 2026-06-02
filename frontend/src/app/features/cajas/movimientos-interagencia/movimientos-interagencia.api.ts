import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

export interface MovimientoInteragenciaCuenta {
  idCuentaAhorro?: number;

  idAgenciaCuenta?: number;
  codigoAgenciaCuenta?: string;
  nombreAgenciaCuenta?: string;

  codigoCuenta?: string;

  idDatosPersonal?: number;
  tipoDocumento?: string;
  documento?: string;
  nombreAsociado?: string;

  idFormaAhorro?: number;
  codigoForma?: string;
  nombreForma?: string;

  saldoActual?: number;
  valorCanje?: number;
  saldoDisponible?: number;

  estadoCuenta?: string;
  estadoOperativo?: boolean;
  mensajeOperativo?: string;
}

export interface MovimientoInteragenciaCheque {
  codigoBanco?: string;
  numeroCheque?: string;
  valorCheque?: number;
  observacion?: string;
}

export interface MovimientoInteragenciaRequest {
  idCaja?: number;
  idProvision?: number;
  idAgenciaCaja?: number;
  fechaContable?: string;

  idCuentaAhorro?: number;

  codigoOperacion?: string;
  tipoMovimiento?: string;
  tipoComprobante?: string;
  numeroComprobante?: string;

  valorEfectivo?: number;
  valorCheques?: number;

  concepto?: string;
  observacion?: string;

  cheques?: MovimientoInteragenciaCheque[];
}

export interface MovimientoInteragenciaPreview {
  idCaja?: number;
  idProvision?: number;

  idAgenciaCaja?: number;
  nombreAgenciaCaja?: string;

  fechaContable?: string;

  idCuentaAhorro?: number;

  idAgenciaCuenta?: number;
  codigoAgenciaCuenta?: string;
  nombreAgenciaCuenta?: string;

  codigoCuenta?: string;
  documento?: string;
  nombreAsociado?: string;

  codigoForma?: string;
  nombreForma?: string;

  codigoOperacion?: string;
  nombreOperacion?: string;

  tipoMovimiento?: string;
  nombreTipoMovimiento?: string;

  naturaleza?: string;

  tipoComprobante?: string;
  numeroComprobante?: string;

  valorEfectivo?: number;
  valorCheques?: number;
  valorTotal?: number;

  saldoAnterior?: number;
  valorCanje?: number;
  saldoDisponible?: number;
  saldoFinal?: number;

  permiteAplicar?: boolean;
  mensaje?: string;

  sarlaft?: any;

  errores?: string[];
}

export interface MovimientoInteragenciaResponse {
  idCuentaAhorro?: number;
  codigoCuenta?: string;

  idAgenciaCaja?: number;
  idAgenciaCuenta?: number;

  documento?: string;
  nombreAsociado?: string;

  fechaContable?: string;

  tipoMovimiento?: string;
  naturaleza?: string;

  valorEfectivo?: number;
  valorCheques?: number;
  valorTotal?: number;

  saldoAnterior?: number;
  saldoFinal?: number;

  movimientosCaja?: number[];

  requiereFormatoLavadoActivos?: boolean;

  mensaje?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MovimientosInteragenciaApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cajas/movimientos_interagencia`;

  constructor(
    private http: HttpClient
  ) {
  }

  buscarCuentas(
    idAgenciaCaja: number,
    documento?: string,
    nombres?: string,
    primerApellido?: string,
    segundoApellido?: string
  ): Observable<MovimientoInteragenciaCuenta[]> {

    let params = new HttpParams()
      .set('idAgenciaCaja', String(idAgenciaCaja));

    if (documento?.trim()) {
      params = params.set('documento', documento.trim());
    }

    if (nombres?.trim()) {
      params = params.set('nombres', nombres.trim());
    }

    if (primerApellido?.trim()) {
      params = params.set('primerApellido', primerApellido.trim());
    }

    if (segundoApellido?.trim()) {
      params = params.set('segundoApellido', segundoApellido.trim());
    }

    return this.http.get<MovimientoInteragenciaCuenta[]>(
      `${this.baseUrl}/buscar-cuentas`,
      { params }
    );
  }

  preview(
    request: MovimientoInteragenciaRequest
  ): Observable<MovimientoInteragenciaPreview> {

    return this.http.post<MovimientoInteragenciaPreview>(
      `${this.baseUrl}/preview`,
      request
    );
  }

  aplicar(
    request: MovimientoInteragenciaRequest
  ): Observable<MovimientoInteragenciaResponse> {

    return this.http.post<MovimientoInteragenciaResponse>(
      `${this.baseUrl}/aplicar`,
      request
    );
  }
}
