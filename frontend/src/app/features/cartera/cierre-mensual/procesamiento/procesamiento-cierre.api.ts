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
  ResultadoProcesamientoCierre
} from './procesamiento-cierre.models';


@Injectable({
  providedIn: 'root'
})
export class ProcesamientoCierreApi {

  private readonly http =
    inject(HttpClient);


  // =========================================================
  // BASE URL
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/cierre-mensual`;


  // =========================================================
  // PROCESAR CÁLCULOS DEL CIERRE
  //
  // POST
  //
  // /api/v1/cartera/cierre-mensual/
  // {idCierreCartera}/procesar-calculos
  //
  // Ejecuta:
  //
  // - intereses
  // - seguros
  // - alivios
  // - cálculos previos
  // - Anexo 1
  // - Anexo 2 / PE
  // - validaciones finales
  // - cuadre consolidado
  //
  // NO recibe idUsuario.
  //
  // =========================================================

  procesar(
    idCierreCartera: number
  ): Observable<ResultadoProcesamientoCierre> {

    return this.http.post<ResultadoProcesamientoCierre>(
      `${this.baseUrl}/${idCierreCartera}/procesar-calculos`,
      {}
    );
  }
}
