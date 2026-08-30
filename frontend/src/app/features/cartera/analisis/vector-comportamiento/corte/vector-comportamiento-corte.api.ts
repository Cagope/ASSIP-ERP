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
} from '../../../../../../environments/environment';

import {
  VectorComportamientoCorteDetalle,
  VectorComportamientoCorteResumen
} from './vector-comportamiento-corte.models';


@Injectable({
  providedIn: 'root'
})
export class VectorComportamientoCorteApi {

  // =========================================================
  // DEPENDENCIAS
  // =========================================================

  private readonly http =
    inject(HttpClient);


  // =========================================================
  // URL
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/analisis/vector-comportamiento/corte`;


  // =========================================================
  // FECHAS DE CORTE DISPONIBLES
  // =========================================================

  listarFechasCorteDisponibles():
  Observable<string[]> {

    return this.http.get<string[]>(
      `${this.baseUrl}/fechas`
    );
  }


  // =========================================================
  // CARTERA DEL CORTE - RESUMEN
  // =========================================================

  listarResumenPorCorte(
    fechaCorte: string
  ): Observable<VectorComportamientoCorteResumen[]> {

    const params =
      new HttpParams()
        .set(
          'fechaCorte',
          fechaCorte
        );

    return this.http.get<
      VectorComportamientoCorteResumen[]
    >(
      `${this.baseUrl}/resumen`,
      {
        params
      }
    );
  }


  // =========================================================
  // CARTERA DEL CORTE - DETALLE
  // =========================================================

  listarDetallePorCorte(
    fechaCorte: string
  ): Observable<VectorComportamientoCorteDetalle[]> {

    const params =
      new HttpParams()
        .set(
          'fechaCorte',
          fechaCorte
        );

    return this.http.get<
      VectorComportamientoCorteDetalle[]
    >(
      `${this.baseUrl}/detalle`,
      {
        params
      }
    );
  }


  // =========================================================
  // ASOCIADO - RESUMEN
  // =========================================================

  listarResumenPorPersona(
    idDatosPersonal: number,
    fechaCorte: string
  ): Observable<VectorComportamientoCorteResumen[]> {

    const params =
      new HttpParams()
        .set(
          'fechaCorte',
          fechaCorte
        );

    return this.http.get<
      VectorComportamientoCorteResumen[]
    >(
      `${this.baseUrl}/persona/${idDatosPersonal}/resumen`,
      {
        params
      }
    );
  }


  // =========================================================
  // ASOCIADO - DETALLE
  // =========================================================

  listarDetallePorPersona(
    idDatosPersonal: number,
    fechaCorte: string
  ): Observable<VectorComportamientoCorteDetalle[]> {

    const params =
      new HttpParams()
        .set(
          'fechaCorte',
          fechaCorte
        );

    return this.http.get<
      VectorComportamientoCorteDetalle[]
    >(
      `${this.baseUrl}/persona/${idDatosPersonal}/detalle`,
      {
        params
      }
    );
  }


  // =========================================================
  // CRÉDITO - RESUMEN
  // =========================================================

  buscarResumenPorCredito(
    idCarteraCredito: number,
    fechaCorte: string
  ): Observable<VectorComportamientoCorteResumen | null> {

    const params =
      new HttpParams()
        .set(
          'fechaCorte',
          fechaCorte
        );

    return this.http.get<
      VectorComportamientoCorteResumen | null
    >(
      `${this.baseUrl}/credito/${idCarteraCredito}/resumen`,
      {
        params
      }
    );
  }


  // =========================================================
  // CRÉDITO - DETALLE
  // =========================================================

  listarDetallePorCredito(
    idCarteraCredito: number,
    fechaCorte: string
  ): Observable<VectorComportamientoCorteDetalle[]> {

    const params =
      new HttpParams()
        .set(
          'fechaCorte',
          fechaCorte
        );

    return this.http.get<
      VectorComportamientoCorteDetalle[]
    >(
      `${this.baseUrl}/credito/${idCarteraCredito}/detalle`,
      {
        params
      }
    );
  }


  // =========================================================
  // EXISTENCIA DEL CRÉDITO EN EL CORTE
  // =========================================================

  existeCreditoEnCorte(
    idCarteraCredito: number,
    fechaCorte: string
  ): Observable<boolean> {

    const params =
      new HttpParams()
        .set(
          'fechaCorte',
          fechaCorte
        );

    return this.http.get<boolean>(
      `${this.baseUrl}/credito/${idCarteraCredito}/existe`,
      {
        params
      }
    );
  }
}
