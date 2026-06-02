import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import {
  CajaCapturaDepositosCuenta,
  CajaCapturaDepositosPreview,
  CajaCapturaDepositosRequest,
  CajaCapturaDepositosResponse,
  TipoMovimientoDeposito
} from './captura-depositos.models';

@Injectable({
  providedIn: 'root'
})
export class CapturaDepositosApi {

  private readonly url =
    `${environment.apiUrl}/cajas/captura_depositos`;

  constructor(
    private http: HttpClient
  ) {
  }

  buscarCuentas(filtros: {
    idAgencia: number;
    documento?: string;
    nombres?: string;
    primerApellido?: string;
    segundoApellido?: string;
  }): Observable<CajaCapturaDepositosCuenta[]> {

    let params = new HttpParams()
      .set('idAgencia', String(filtros.idAgencia));

    if (filtros.documento?.trim()) {
      params = params.set('documento', filtros.documento.trim());
    }

    if (filtros.nombres?.trim()) {
      params = params.set('nombres', filtros.nombres.trim());
    }

    if (filtros.primerApellido?.trim()) {
      params = params.set('primerApellido', filtros.primerApellido.trim());
    }

    if (filtros.segundoApellido?.trim()) {
      params = params.set('segundoApellido', filtros.segundoApellido.trim());
    }

    return this.http.get<CajaCapturaDepositosCuenta[]>(
      `${this.url}/buscar-cuentas`,
      { params }
    );
  }

  obtenerCuenta(
    idCuentaAhorro: number
  ): Observable<CajaCapturaDepositosCuenta> {
    return this.http.get<CajaCapturaDepositosCuenta>(
      `${this.url}/cuenta/${idCuentaAhorro}`
    );
  }

  listarTiposMovimiento(): Observable<TipoMovimientoDeposito[]> {
    return this.http.get<TipoMovimientoDeposito[]>(
      `${this.url}/tipos_movimiento`
    );
  }

  preview(
    request: CajaCapturaDepositosRequest
  ): Observable<CajaCapturaDepositosPreview> {
    return this.http.post<CajaCapturaDepositosPreview>(
      `${this.url}/preview`,
      request
    );
  }

  aplicar(
    request: CajaCapturaDepositosRequest
  ): Observable<CajaCapturaDepositosResponse> {
    return this.http.post<CajaCapturaDepositosResponse>(
      `${this.url}/aplicar`,
      request
    );
  }
}
