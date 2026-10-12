import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import {
  CajaEfectivoDisponible,
  ChequeDisponible,
  ConsignacionBancariaPreviewResponse,
  ConsignacionBancariaRequest,
  ConsignacionBancariaResponse,
  CuentaBancariaConsignacion
} from './consignaciones-bancarias.models';

@Injectable({
  providedIn: 'root'
})
export class ConsignacionesBancariasApi {

  private readonly baseUrl =
    `${environment.apiUrl}/tesoreria/consignaciones-bancarias`;

  constructor(
    private readonly http: HttpClient
  ) {
  }


  // =========================================================
  // CUENTAS BANCARIAS
  // =========================================================

  listarCuentasBancarias(
    idAgencia: number
  ): Observable<CuentaBancariaConsignacion[]> {

    const params = new HttpParams()
      .set(
        'idAgencia',
        idAgencia.toString()
      );

    return this.http.get<CuentaBancariaConsignacion[]>(
      `${this.baseUrl}/cuentas-bancarias`,
      { params }
    );
  }


  // =========================================================
  // CHEQUES DISPONIBLES
  // =========================================================

  listarCheques(
    idAgencia: number
  ): Observable<ChequeDisponible[]> {

    const params = new HttpParams()
      .set(
        'idAgencia',
        idAgencia.toString()
      );

    return this.http.get<ChequeDisponible[]>(
      `${this.baseUrl}/cheques`,
      { params }
    );
  }


  // =========================================================
  // CAJAS DISPONIBLES
  // =========================================================

  listarCajas(
    idAgencia: number,
    fechaConsignacion: string
  ): Observable<CajaEfectivoDisponible[]> {

    const params = new HttpParams()
      .set(
        'idAgencia',
        idAgencia.toString()
      )
      .set(
        'fechaConsignacion',
        fechaConsignacion
      );

    return this.http.get<CajaEfectivoDisponible[]>(
      `${this.baseUrl}/cajas`,
      { params }
    );
  }


  // =========================================================
  // PREVIEW CONTABLE
  // =========================================================

  previewContable(
    request: ConsignacionBancariaRequest
  ): Observable<ConsignacionBancariaPreviewResponse> {

    return this.http.post<ConsignacionBancariaPreviewResponse>(
      `${this.baseUrl}/preview-contable`,
      request
    );
  }


  // =========================================================
  // APLICAR CONSIGNACIÓN
  // =========================================================

  aplicar(
    request: ConsignacionBancariaRequest
  ): Observable<ConsignacionBancariaResponse> {

    return this.http.post<ConsignacionBancariaResponse>(
      this.baseUrl,
      request
    );
  }

}
