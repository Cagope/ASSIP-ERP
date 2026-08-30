import {
  HttpClient
} from '@angular/common/http';

import {
  Injectable,
  inject
} from '@angular/core';

import {
  Observable
} from 'rxjs';

import {
  environment
} from '../../../../../environments/environment';

import {
  CuadreCierre
} from './cuadre-cierre.models';


@Injectable({
  providedIn: 'root'
})
export class CuadreCierreApi {

  private readonly http =
    inject(HttpClient);


  // =========================================================
  // BASE URL
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/cierre-mensual/cuadre`;


  // =========================================================
  // CONSULTAR CUADRE CONSOLIDADO
  //
  // GET
  // /api/v1/cartera/cierre-mensual/cuadre/{id}
  //
  // SOLO LECTURA
  // =========================================================

  consultar(
    idCierreCartera: number
  ): Observable<CuadreCierre> {

    return this.http.get<CuadreCierre>(
      `${this.baseUrl}/${idCierreCartera}`
    );
  }
}
