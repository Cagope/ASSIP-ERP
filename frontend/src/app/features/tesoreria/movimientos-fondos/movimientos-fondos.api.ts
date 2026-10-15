
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import {
  CajaConProvision,
  CajaMovimiento,
  ChequeDisponible,
  CuentaBancariaMovimiento,
  MovimientoFondoPreview,
  MovimientoFondoResponse,
  MovimientoFondoRequest
} from './movimientos-fondos.models';

@Injectable({
  providedIn: 'root'
})
export class MovimientosFondosApi {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/tesoreria/movimientos-fondos`;

  // =========================================================
  // CUENTAS BANCARIAS
  // =========================================================

  listarCuentasBancarias(
    idAgencia: number
  ): Observable<CuentaBancariaMovimiento[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString());

    return this.http.get<CuentaBancariaMovimiento[]>(
      `${this.baseUrl}/cuentas-bancarias`,
      { params }
    );
  }

  // =========================================================
  // CHEQUES RECIBIDOS DISPONIBLES
  // =========================================================

  listarCheques(
    idAgencia: number
  ): Observable<ChequeDisponible[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString());

    return this.http.get<ChequeDisponible[]>(
      `${this.baseUrl}/cheques`,
      { params }
    );
  }

  // =========================================================
  // CAJAS CON PROVISIÓN
  // =========================================================

  listarCajasConProvision(
    idAgencia: number,
    fechaConsignacion: string
  ): Observable<CajaConProvision[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString())
      .set('fechaConsignacion', fechaConsignacion);

    return this.http.get<CajaConProvision[]>(
      `${this.baseUrl}/cajas`,
      { params }
    );
  }

  // =========================================================
  // CAJAS CONTROL Y OPERATIVAS
  // =========================================================

  listarCajasMovimiento(
    idAgencia: number
  ): Observable<CajaMovimiento[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString());

    return this.http.get<CajaMovimiento[]>(
      `${this.baseUrl}/cajas-movimiento`,
      { params }
    );
  }

  // =========================================================
  // VISTA PREVIA CONTABLE
  //
  // No registra movimientos.
  // No genera consecutivos definitivos.
  // No modifica caja ni contabilidad.
  // =========================================================

  previewContable(
    request: MovimientoFondoRequest
  ): Observable<MovimientoFondoPreview> {

    return this.http.post<MovimientoFondoPreview>(
      `${this.baseUrl}/preview-contable`,
      request
    );
  }

  // Registro definitivo, separado de la vista previa.
  aplicarMovimiento(request: MovimientoFondoRequest): Observable<MovimientoFondoResponse> {
    const endpoint = request.tipoMovimiento === 'CONSIGNACION'
      ? 'consignaciones'
      : request.tipoMovimiento === 'TRASLADO_CAJAS'
        ? 'traslados-cajas'
        : null;

    if (!endpoint) {
      throw new Error('La operación seleccionada todavía no está habilitada.');
    }

    return this.http.post<MovimientoFondoResponse>(
      `${this.baseUrl}/${endpoint}`,
      request
    );
  }

}
