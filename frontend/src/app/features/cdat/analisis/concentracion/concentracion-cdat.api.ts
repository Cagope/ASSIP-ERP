import {
  Injectable
} from '@angular/core';

import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  environment
} from '../../../../../environments/environment';

import {
  ConcentracionCdatDepositante,
  ConcentracionCdatDetalle,
  ConcentracionCdatFiltros,
  ConcentracionCdatResumen
} from './concentracion-cdat.models';


@Injectable({
  providedIn: 'root'
})
export class ConcentracionCdatApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cdat/analisis/concentracion`;

  constructor(
    private readonly http: HttpClient
  ) {
  }


  // =========================================================
  // CORTES
  // =========================================================

  cortes(): Observable<string[]> {

    return this.http.get<string[]>(
      `${this.baseUrl}/cortes`
    );
  }


  // =========================================================
  // RESUMEN
  // =========================================================

  resumen(
    filtros: ConcentracionCdatFiltros
  ): Observable<ConcentracionCdatResumen> {

    return this.http.get<ConcentracionCdatResumen>(
      `${this.baseUrl}/resumen`,
      {
        params: this.parametros(filtros)
      }
    );
  }


  // =========================================================
  // RANKING
  // =========================================================

  ranking(
    filtros: ConcentracionCdatFiltros
  ): Observable<ConcentracionCdatDepositante[]> {

    return this.http.get<ConcentracionCdatDepositante[]>(
      `${this.baseUrl}/ranking`,
      {
        params: this.parametros(filtros)
      }
    );
  }


  // =========================================================
  // DETALLE DEPOSITANTE
  // =========================================================

  detalle(
    filtros: ConcentracionCdatFiltros,
    idDatosPersonal: number
  ): Observable<ConcentracionCdatDetalle[]> {

    return this.http.get<ConcentracionCdatDetalle[]>(
      `${this.baseUrl}/depositantes/${idDatosPersonal}/detalle`,
      {
        params: this.parametros(filtros)
      }
    );
  }


  // =========================================================
  // PARÁMETROS
  // =========================================================

  private parametros(
    filtros: ConcentracionCdatFiltros
  ): HttpParams {

    let params =
      new HttpParams();

    if (filtros.fechaCorte) {
      params =
        params.set(
          'fechaCorte',
          filtros.fechaCorte
        );
    }

    if (filtros.idAgencia !== null) {
      params =
        params.set(
          'idAgencia',
          filtros.idAgencia
        );
    }

    return params;
  }
}
