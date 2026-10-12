import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import {
  CajaCierreConfirmarRequest,
  CajaCierreDisponible,
  CajaCierreResumen
} from './cajas-cierre.models';


@Injectable({
  providedIn: 'root'
})
export class CajasCierreApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cajas/cierre`;


  constructor(
    private readonly http: HttpClient
  ) {}


  preparar(
    fechaContable: string
  ): Observable<CajaCierreResumen> {

    const params = new HttpParams()
      .set(
        'fechaContable',
        fechaContable
      );

    return this.http.get<CajaCierreResumen>(
      `${this.baseUrl}/preparar`,
      { params }
    );
  }

  listarDisponibles(
    fechaContable: string
  ): Observable<CajaCierreDisponible[]> {

    const params = new HttpParams()
      .set(
        'fechaContable',
        fechaContable
      );

    return this.http.get<CajaCierreDisponible[]>(
      `${this.baseUrl}/disponibles`,
      { params }
    );
  }

  obtenerResumen(
    idProvision: number
  ): Observable<CajaCierreResumen> {

    return this.http.get<CajaCierreResumen>(
      `${this.baseUrl}/${idProvision}`
    );
  }

  listarDenominaciones(): Observable<number[]> {

    return this.http.get<number[]>(
      `${this.baseUrl}/denominaciones`
    );
  }

  confirmar(
    request: CajaCierreConfirmarRequest
  ): Observable<CajaCierreResumen> {

    return this.http.post<CajaCierreResumen>(
      `${this.baseUrl}/confirmar`,
      request
    );
  }
}
