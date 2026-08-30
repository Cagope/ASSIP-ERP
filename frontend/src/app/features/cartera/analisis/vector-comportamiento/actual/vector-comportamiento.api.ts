import {
  Injectable,
  inject
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  environment
} from '../../../../../../environments/environment';

import {
  VectorComportamientoDetalle,
  VectorComportamientoResumen
} from './vector-comportamiento.models';


@Injectable({
  providedIn: 'root'
})
export class VectorComportamientoApi {

  // =========================================================
  // DEPENDENCIAS
  // =========================================================

  private readonly http =
    inject(HttpClient);


  // =========================================================
  // URL
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/analisis/vector-comportamiento/actual`;


  // =========================================================
  // CARTERA ACTIVA - RESUMEN
  // =========================================================

  listarResumenCarteraActiva():
  Observable<VectorComportamientoResumen[]> {

    return this.http.get<
      VectorComportamientoResumen[]
    >(
      `${this.baseUrl}/resumen`
    );
  }


  // =========================================================
  // CARTERA ACTIVA - DETALLE
  // =========================================================

  listarDetalleCarteraActiva():
  Observable<VectorComportamientoDetalle[]> {

    return this.http.get<
      VectorComportamientoDetalle[]
    >(
      `${this.baseUrl}/detalle`
    );
  }


  // =========================================================
  // ASOCIADO - RESUMEN
  // =========================================================

  listarResumenPorPersona(
    idDatosPersonal: number
  ): Observable<VectorComportamientoResumen[]> {

    return this.http.get<
      VectorComportamientoResumen[]
    >(
      `${this.baseUrl}/persona/${idDatosPersonal}/resumen`
    );
  }


  // =========================================================
  // ASOCIADO - DETALLE
  // =========================================================

  listarDetallePorPersona(
    idDatosPersonal: number
  ): Observable<VectorComportamientoDetalle[]> {

    return this.http.get<
      VectorComportamientoDetalle[]
    >(
      `${this.baseUrl}/persona/${idDatosPersonal}/detalle`
    );
  }


  // =========================================================
  // CRÉDITO - RESUMEN
  // =========================================================

  buscarResumenPorCredito(
    idCarteraCredito: number
  ): Observable<VectorComportamientoResumen | null> {

    return this.http.get<
      VectorComportamientoResumen | null
    >(
      `${this.baseUrl}/credito/${idCarteraCredito}/resumen`
    );
  }


  // =========================================================
  // CRÉDITO - DETALLE
  // =========================================================

  listarDetallePorCredito(
    idCarteraCredito: number
  ): Observable<VectorComportamientoDetalle[]> {

    return this.http.get<
      VectorComportamientoDetalle[]
    >(
      `${this.baseUrl}/credito/${idCarteraCredito}/detalle`
    );
  }


  // =========================================================
  // EXISTENCIA
  // =========================================================

  existeCredito(
    idCarteraCredito: number
  ): Observable<boolean> {

    return this.http.get<boolean>(
      `${this.baseUrl}/credito/${idCarteraCredito}/existe`
    );
  }
}
