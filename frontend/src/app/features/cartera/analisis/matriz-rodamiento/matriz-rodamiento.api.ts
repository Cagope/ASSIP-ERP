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
  MatrizRodamiento,
  MatrizRodamientoConsulta,
  MatrizRodamientoCorte,
  MatrizRodamientoDetalle,
  MatrizRodamientoDetalleConsulta
} from './matriz-rodamiento.models';


@Injectable({
  providedIn: 'root'
})
export class MatrizRodamientoApi {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/analisis/matriz-rodamiento`;


  // =========================================================
  // CORTES DISPONIBLES
  // =========================================================

  listarCortes():
  Observable<MatrizRodamientoCorte[]> {

    return this.http.get<
      MatrizRodamientoCorte[]
    >(
      `${this.baseUrl}/cortes`
    );
  }


  // =========================================================
  // CALCULAR MATRIZ
  // =========================================================

  calcular(
    consulta: MatrizRodamientoConsulta
  ): Observable<MatrizRodamiento> {

    let params =
      new HttpParams()
        .set(
          'tipoPartida',
          consulta.tipoPartida
        )
        .set(
          'fechaComparacion',
          consulta.fechaComparacion
        );

    if (
      consulta.tipoPartida === 'CORTE'
      &&
      consulta.fechaPartida
    ) {

      params =
        params.set(
          'fechaPartida',
          consulta.fechaPartida
        );
    }

    return this.http.get<MatrizRodamiento>(
      `${this.baseUrl}/calcular`,
      {
        params
      }
    );
  }


  // =========================================================
  // DETALLE DE CELDA
  // =========================================================

  listarDetalle(
    consulta:
      MatrizRodamientoDetalleConsulta
  ): Observable<MatrizRodamientoDetalle[]> {

    let params =
      new HttpParams()
        .set(
          'tipoPartida',
          consulta.tipoPartida
        )
        .set(
          'fechaComparacion',
          consulta.fechaComparacion
        )
        .set(
          'categoriaAnterior',
          consulta.categoriaAnterior
        )
        .set(
          'categoriaPartida',
          consulta.categoriaPartida
        );

    if (
      consulta.tipoPartida === 'CORTE'
      &&
      consulta.fechaPartida
    ) {

      params =
        params.set(
          'fechaPartida',
          consulta.fechaPartida
        );
    }

    return this.http.get<
      MatrizRodamientoDetalle[]
    >(
      `${this.baseUrl}/detalle`,
      {
        params
      }
    );
  }

}
