import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';

import {
  GarantiasCoberturaBien,
  GarantiasCoberturaCredito,
  GarantiasCoberturaDetalle,
  GarantiasCoberturaResumen
} from './garantias-cobertura.models';

@Injectable({
  providedIn: 'root'
})
export class GarantiasCoberturaService {

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/analisis/garantias-cobertura`;

  constructor(
    private readonly http: HttpClient
  ) {}

  // =========================================================
  // CORTES
  // =========================================================
  listarCortes(): Observable<string[]> {
    return this.http.get<string[]>(
      `${this.baseUrl}/cortes`
    );
  }

  // =========================================================
  // RESUMEN
  // =========================================================
  obtenerResumen(
    fechaCorte: string
  ): Observable<GarantiasCoberturaResumen> {

    return this.http.get<GarantiasCoberturaResumen>(
      `${this.baseUrl}/resumen`,
      {
        params: this.params(fechaCorte)
      }
    );
  }

  // =========================================================
  // BIENES
  // =========================================================
  listarBienes(
    fechaCorte: string
  ): Observable<GarantiasCoberturaBien[]> {

    return this.http.get<GarantiasCoberturaBien[]>(
      `${this.baseUrl}/bienes`,
      {
        params: this.params(fechaCorte)
      }
    );
  }

  listarBienesInsuficientes(
    fechaCorte: string
  ): Observable<GarantiasCoberturaBien[]> {

    return this.http.get<GarantiasCoberturaBien[]>(
      `${this.baseUrl}/bienes/insuficientes`,
      {
        params: this.params(fechaCorte)
      }
    );
  }

  // =========================================================
  // CRÉDITOS
  // =========================================================
  listarCreditos(
    fechaCorte: string
  ): Observable<GarantiasCoberturaCredito[]> {

    return this.http.get<GarantiasCoberturaCredito[]>(
      `${this.baseUrl}/creditos`,
      {
        params: this.params(fechaCorte)
      }
    );
  }

  listarCreditosInsuficientes(
    fechaCorte: string
  ): Observable<GarantiasCoberturaCredito[]> {

    return this.http.get<GarantiasCoberturaCredito[]>(
      `${this.baseUrl}/creditos/insuficientes`,
      {
        params: this.params(fechaCorte)
      }
    );
  }

  // =========================================================
  // DETALLE COMPLETO
  // =========================================================
  listarDetalle(
    fechaCorte: string
  ): Observable<GarantiasCoberturaDetalle[]> {

    return this.http.get<GarantiasCoberturaDetalle[]>(
      `${this.baseUrl}/detalle`,
      {
        params: this.params(fechaCorte)
      }
    );
  }

  // =========================================================
  // DETALLE POR BIEN
  // =========================================================
  listarDetallePorBien(
    fechaCorte: string,
    idBien: number
  ): Observable<GarantiasCoberturaDetalle[]> {

    return this.http.get<GarantiasCoberturaDetalle[]>(
      `${this.baseUrl}/detalle/bien/${idBien}`,
      {
        params: this.params(fechaCorte)
      }
    );
  }

  // =========================================================
  // DETALLE POR CRÉDITO
  // =========================================================
  listarDetallePorCredito(
    fechaCorte: string,
    idCarteraCredito: number
  ): Observable<GarantiasCoberturaDetalle[]> {

    return this.http.get<GarantiasCoberturaDetalle[]>(
      `${this.baseUrl}/detalle/credito/${idCarteraCredito}`,
      {
        params: this.params(fechaCorte)
      }
    );
  }

  // =========================================================
  // PARÁMETROS
  // =========================================================
  private params(
    fechaCorte: string
  ): HttpParams {

    return new HttpParams()
      .set('fechaCorte', fechaCorte);
  }
}
