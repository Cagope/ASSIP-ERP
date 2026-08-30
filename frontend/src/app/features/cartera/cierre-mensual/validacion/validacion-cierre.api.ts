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
  ResultadoValidacionCierre
} from './validacion-cierre.models';


@Injectable({
  providedIn: 'root'
})
export class ValidacionCierreApi {

  private readonly http =
    inject(HttpClient);


  // =========================================================
  // BASE URL
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/cierre-mensual/validacion`;


  // =========================================================
  // VALIDAR CIERRE
  //
  // GET
  // /api/v1/cartera/cierre-mensual/validacion/{id}
  //
  // IMPORTANTE:
  //
  // - solo consulta
  // - no recalcula
  // - no modifica resultados
  // - incluye cuadre consolidado final
  //
  // =========================================================

  consultar(
    idCierreCartera: number
  ): Observable<ResultadoValidacionCierre> {

    return this.http.get<ResultadoValidacionCierre>(
      `${this.baseUrl}/${idCierreCartera}`
    );
  }
}
