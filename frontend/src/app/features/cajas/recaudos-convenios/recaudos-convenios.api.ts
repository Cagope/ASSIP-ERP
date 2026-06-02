import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

export interface RecaudoConvenioConvenio {
  idConvenio: number;

  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;

  codigoConvenio: string;
  nombreConvenio: string;

  idCuentaAhorro: number;
  codigoCuenta: string;

  documentoTitular: string;
  nombreTitular: string;
}

export interface RecaudoConvenio {
  idRecaudoConvenio: number;

  idProvision: number;
  idCaja: number;
  idAgencia: number;
  fechaRecaudo: string;

  idConvenio: number;
  codigoConvenio: string;
  nombreConvenio: string;

  idCuentaAhorro: number;
  codigoCuenta: string;

  documentoSoporte: string;
  valorRecaudo: number;

  estadoRecaudo: string;
}

export interface RecaudoConvenioRequest {
  idProvision: number;
  idConvenio: number;
  documentoSoporte: string;
  valorRecaudo: number;
}

@Injectable({
  providedIn: 'root'
})
export class RecaudosConveniosApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cajas/recaudos_convenios`;

  constructor(
    private http: HttpClient
  ) {
  }

  listarConveniosActivos(
    idAgencia: number
  ): Observable<RecaudoConvenioConvenio[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia);

    return this.http.get<RecaudoConvenioConvenio[]>(
      `${this.baseUrl}/convenios-activos`,
      { params }
    );
  }

  listarPorProvision(
    idProvision: number
  ): Observable<RecaudoConvenio[]> {

    const params = new HttpParams()
      .set('idProvision', idProvision);

    return this.http.get<RecaudoConvenio[]>(
      this.baseUrl,
      { params }
    );
  }

  aplicar(
    request: RecaudoConvenioRequest
  ): Observable<RecaudoConvenio> {

    return this.http.post<RecaudoConvenio>(
      `${this.baseUrl}/aplicar`,
      request
    );
  }
}
