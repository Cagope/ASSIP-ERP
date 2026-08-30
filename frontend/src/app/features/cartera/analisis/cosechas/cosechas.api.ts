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
  CosechaCatalogo,
  CosechaConsulta,
  CosechaCorte,
  CosechaDetalle,
  CosechaDetalleConsulta,
  CosechaResumen
} from './cosechas.models';


@Injectable({
  providedIn: 'root'
})
export class CosechasApi {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/analisis/cosechas`;


  // =========================================================
  // CORTES
  // =========================================================

  listarCortes():
  Observable<CosechaCorte[]> {

    return this.http.get<CosechaCorte[]>(
      `${this.baseUrl}/cortes`
    );
  }


  // =========================================================
  // AGENCIAS
  // =========================================================

  listarAgencias():
  Observable<CosechaCatalogo[]> {

    return this.http.get<CosechaCatalogo[]>(
      `${this.baseUrl}/agencias`
    );
  }


  // =========================================================
  // LÍNEAS
  // =========================================================

  listarLineas():
  Observable<CosechaCatalogo[]> {

    return this.http.get<CosechaCatalogo[]>(
      `${this.baseUrl}/lineas`
    );
  }


  // =========================================================
  // ANALIZAR
  // =========================================================

  analizar(
    consulta: CosechaConsulta
  ): Observable<CosechaResumen> {

    let params =
      new HttpParams()
        .set(
          'cosechaDesde',
          consulta.cosechaDesde
        )
        .set(
          'cosechaHasta',
          consulta.cosechaHasta
        );

    if (
      consulta.hastaCorte
    ) {

      params =
        params.set(
          'hastaCorte',
          consulta.hastaCorte
        );
    }

    if (
      consulta.idAgencia != null
    ) {

      params =
        params.set(
          'idAgencia',
          consulta.idAgencia.toString()
        );
    }

    if (
      consulta.idLineaCredito != null
    ) {

      params =
        params.set(
          'idLineaCredito',
          consulta.idLineaCredito.toString()
        );
    }

    return this.http.get<CosechaResumen>(
      `${this.baseUrl}/analizar`,
      {
        params
      }
    );
  }


  // =========================================================
  // DETALLE
  // =========================================================

  listarDetalle(
    consulta: CosechaDetalleConsulta
  ): Observable<CosechaDetalle[]> {

    let params =
      new HttpParams()
        .set(
          'cosecha',
          consulta.cosecha
        )
        .set(
          'fechaCorte',
          consulta.fechaCorte
        )
        .set(
          'indicador',
          consulta.indicador
        );

    if (
      consulta.idAgencia != null
    ) {

      params =
        params.set(
          'idAgencia',
          consulta.idAgencia.toString()
        );
    }

    if (
      consulta.idLineaCredito != null
    ) {

      params =
        params.set(
          'idLineaCredito',
          consulta.idLineaCredito.toString()
        );
    }

    return this.http.get<CosechaDetalle[]>(
      `${this.baseUrl}/detalle`,
      {
        params
      }
    );
  }

}
