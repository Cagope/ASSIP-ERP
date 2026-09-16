import {
  Injectable,
  inject
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
  TasasCondicionesCdatCondicion,
  TasasCondicionesCdatCorte,
  TasasCondicionesCdatDetalle,
  TasasCondicionesCdatFiltros,
  TasasCondicionesCdatRangoSaldo,
  TasasCondicionesCdatResumen
} from './tasas-condiciones-cdat.models';


@Injectable({
  providedIn: 'root'
})
export class TasasCondicionesCdatApi {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/cdat/analisis/tasas-condiciones`;


  // =========================================================
  // CORTES DISPONIBLES
  // =========================================================

  listarCortes():
    Observable<TasasCondicionesCdatCorte[]> {

    return this.http.get<TasasCondicionesCdatCorte[]>(
      `${this.baseUrl}/cortes`
    );
  }


  // =========================================================
  // RESUMEN
  // =========================================================

  obtenerResumen(
    filtros: TasasCondicionesCdatFiltros
  ): Observable<TasasCondicionesCdatResumen> {

    return this.http.get<TasasCondicionesCdatResumen>(
      `${this.baseUrl}/resumen`,
      {
        params: this.construirParametros(filtros)
      }
    );
  }


  // =========================================================
  // CONDICIONES
  // =========================================================

  obtenerCondiciones(
    filtros: TasasCondicionesCdatFiltros
  ): Observable<TasasCondicionesCdatCondicion[]> {

    return this.http.get<TasasCondicionesCdatCondicion[]>(
      `${this.baseUrl}/condiciones`,
      {
        params: this.construirParametros(filtros)
      }
    );
  }


  // =========================================================
  // RANGOS DE SALDO
  // =========================================================

  obtenerRangosSaldo(
    filtros: TasasCondicionesCdatFiltros
  ): Observable<TasasCondicionesCdatRangoSaldo[]> {

    return this.http.get<TasasCondicionesCdatRangoSaldo[]>(
      `${this.baseUrl}/rangos-saldo`,
      {
        params: this.construirParametros(filtros)
      }
    );
  }


  // =========================================================
  // DETALLE
  // =========================================================

  obtenerDetalle(
    filtros: TasasCondicionesCdatFiltros
  ): Observable<TasasCondicionesCdatDetalle[]> {

    return this.http.get<TasasCondicionesCdatDetalle[]>(
      `${this.baseUrl}/detalle`,
      {
        params: this.construirParametros(filtros)
      }
    );
  }


  // =========================================================
  // PARÁMETROS
  // =========================================================

  private construirParametros(
    filtros: TasasCondicionesCdatFiltros
  ): HttpParams {

    let params =
      new HttpParams();

    // Fecha de corte obligatoria para el backend
    if (filtros.fechaCorte) {

      params =
        params.set(
          'fechaCorte',
          filtros.fechaCorte
        );
    }

    if (filtros.idAgencia > 0) {

      params =
        params.set(
          'idAgencia',
          filtros.idAgencia
        );
    }

    if (filtros.plazoMeses > 0) {

      params =
        params.set(
          'plazoMeses',
          filtros.plazoMeses
        );
    }

    if (filtros.amortizacion) {

      params =
        params.set(
          'amortizacion',
          filtros.amortizacion
        );
    }

    if (filtros.rangoSaldo) {

      params =
        params.set(
          'rangoSaldo',
          filtros.rangoSaldo
        );
    }

    return params;
  }

}
