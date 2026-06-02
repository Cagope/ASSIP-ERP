import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

export interface ConvenioRecaudo {
  idConvenio?: number;

  idAgencia?: number;
  codigoAgencia?: string;
  nombreAgencia?: string;

  codigoConvenio?: string;
  nombreConvenio?: string;

  idCuentaAhorro?: number;
  codigoCuenta?: string;
  codigoForma?: string;
  nombreForma?: string;

  idDatosPersonal?: number;
  documento?: string;
  nombreTitular?: string;

  saldoActual?: number;

  estado?: string;
}

export interface ConvenioRecaudoCuenta {
  idCuentaAhorro?: number;

  idAgencia?: number;
  codigoAgencia?: string;
  nombreAgencia?: string;

  idDatosPersonal?: number;
  documento?: string;
  nombreTitular?: string;

  codigoCuenta?: string;
  codigoForma?: string;
  nombreForma?: string;

  saldoActual?: number;

  estadoCuenta?: string;
  estadoOperativo?: boolean;
  mensajeOperativo?: string;
}

export interface ConvenioRecaudoRequest {
  idAgencia?: number;
  codigoConvenio?: string;
  nombreConvenio?: string;
  idCuentaAhorro?: number;
  estado?: string;
}

export interface ConvenioRecaudoBusqueda {
  valido?: boolean;
  mensaje?: string;

  documento?: string;
  nombreTitular?: string;

  codigoCuentaAportes?: string;
  saldoAportes?: number;
  fechaAperturaAportes?: string;

  cuentas?: ConvenioRecaudoCuenta[];
}

@Injectable({
  providedIn: 'root'
})
export class ConveniosRecaudoApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cajas/convenios_recaudo`;

  constructor(
    private http: HttpClient
  ) {
  }

  listar(): Observable<ConvenioRecaudo[]> {
    return this.http.get<ConvenioRecaudo[]>(this.baseUrl);
  }

  obtener(idConvenio: number): Observable<ConvenioRecaudo> {
    return this.http.get<ConvenioRecaudo>(
      `${this.baseUrl}/${idConvenio}`
    );
  }

  buscarCuentas(
    idAgencia: number,
    documento: string
  ): Observable<ConvenioRecaudoBusqueda> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia)
      .set('documento', documento);

    return this.http.get<ConvenioRecaudoBusqueda>(
      `${this.baseUrl}/buscar-cuentas`,
      { params }
    );
  }

  crear(
    request: ConvenioRecaudoRequest
  ): Observable<ConvenioRecaudo> {
    return this.http.post<ConvenioRecaudo>(
      this.baseUrl,
      request
    );
  }

  actualizar(
    idConvenio: number,
    request: ConvenioRecaudoRequest
  ): Observable<ConvenioRecaudo> {
    return this.http.put<ConvenioRecaudo>(
      `${this.baseUrl}/${idConvenio}`,
      request
    );
  }

  eliminar(
    idConvenio: number
  ): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/${idConvenio}`
    );
  }
}
