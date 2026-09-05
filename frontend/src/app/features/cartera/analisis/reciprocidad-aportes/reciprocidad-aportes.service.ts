import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';

import {
  ReciprocidadAportesDetalle,
  ReciprocidadAportesPersona,
  ReciprocidadAportesResumen
} from './reciprocidad-aportes.models';

@Injectable({
  providedIn: 'root'
})
export class ReciprocidadAportesService {

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/analisis/reciprocidad-aportes`;

  constructor(
    private readonly http: HttpClient
  ) {}

  listarCortes(): Observable<string[]> {
    return this.http.get<string[]>(
      `${this.baseUrl}/cortes`
    );
  }

  obtenerResumen(
    fechaCorte: string
  ): Observable<ReciprocidadAportesResumen> {
    return this.http.get<ReciprocidadAportesResumen>(
      `${this.baseUrl}/resumen`,
      {
        params: this.params(fechaCorte)
      }
    );
  }

  listarPersonas(
    fechaCorte: string
  ): Observable<ReciprocidadAportesPersona[]> {
    return this.http.get<ReciprocidadAportesPersona[]>(
      `${this.baseUrl}/personas`,
      {
        params: this.params(fechaCorte)
      }
    );
  }

  listarDetalle(
    fechaCorte: string
  ): Observable<ReciprocidadAportesDetalle[]> {
    return this.http.get<ReciprocidadAportesDetalle[]>(
      `${this.baseUrl}/detalle`,
      {
        params: this.params(fechaCorte)
      }
    );
  }

  listarDetallePersona(
    idDatosPersonal: number,
    fechaCorte: string
  ): Observable<ReciprocidadAportesDetalle[]> {
    return this.http.get<ReciprocidadAportesDetalle[]>(
      `${this.baseUrl}/detalle/persona/${idDatosPersonal}`,
      {
        params: this.params(fechaCorte)
      }
    );
  }

  private params(
    fechaCorte: string
  ): HttpParams {
    return new HttpParams()
      .set('fechaCorte', fechaCorte);
  }
}
