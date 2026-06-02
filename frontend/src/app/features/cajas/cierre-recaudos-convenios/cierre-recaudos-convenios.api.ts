import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

export interface CierreRecaudosConveniosRequest {
  idCaja?: number;
  fechaContable?: string;
  idConvenio?: number;
  documentoSoporte?: string;
}

export interface CierreRecaudosConveniosItem {
  idRecaudoConvenio?: number;
  fechaRecaudo?: string;
  documentoSoporte?: string;
  valorRecaudo?: number;
}

export interface CierreRecaudosConveniosPreview {
  idProvision?: number;
  idCaja?: number;
  idAgencia?: number;
  fechaContable?: string;

  idConvenio?: number;
  codigoConvenio?: string;
  nombreConvenio?: string;

  idCuentaAhorro?: number;
  codigoCuenta?: string;
  documentoTitular?: string;
  nombreTitular?: string;

  documentoSoporte?: string;

  cantidadRecaudos?: number;
  valorTotal?: number;

  permiteAplicar?: boolean;
  mensaje?: string;
  errores?: string[];

  items?: CierreRecaudosConveniosItem[];
}

export interface CierreRecaudosConveniosResponse {
  idProvision?: number;
  idCaja?: number;
  idAgencia?: number;
  fechaContable?: string;

  idConvenio?: number;
  codigoConvenio?: string;
  nombreConvenio?: string;

  idCuentaAhorro?: number;
  codigoCuenta?: string;

  documentoSoporte?: string;

  cantidadRecaudos?: number;
  valorTotal?: number;

  idMovimientoCaja?: number;

  mensaje?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CierreRecaudosConveniosApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cajas/cierre_recaudos_convenios`;

  constructor(
    private http: HttpClient
  ) {
  }

  preview(
    request: CierreRecaudosConveniosRequest
  ): Observable<CierreRecaudosConveniosPreview> {
    return this.http.post<CierreRecaudosConveniosPreview>(
      `${this.baseUrl}/preview`,
      request
    );
  }

  aplicar(
    request: CierreRecaudosConveniosRequest
  ): Observable<CierreRecaudosConveniosResponse> {
    return this.http.post<CierreRecaudosConveniosResponse>(
      `${this.baseUrl}/aplicar`,
      request
    );
  }
}
