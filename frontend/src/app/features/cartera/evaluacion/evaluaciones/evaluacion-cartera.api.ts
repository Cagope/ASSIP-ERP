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
} from '../../../../../environments/environment';

import {
  EvaluacionCartera,
  EvaluacionCarteraGuardar,
  EvaluacionCreditoResultado
} from './evaluacion-cartera.models';


@Injectable({
  providedIn: 'root'
})
export class EvaluacionCarteraApi {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/evaluaciones`;

  // =========================================================
  // LISTAR
  // =========================================================

  listar(): Observable<EvaluacionCartera[]> {

    return this.http.get<EvaluacionCartera[]>(
      this.baseUrl
    );
  }

  // =========================================================
  // BUSCAR POR ID
  // =========================================================

  buscarPorId(
    idEvaluacionCartera: number
  ): Observable<EvaluacionCartera> {

    return this.http.get<EvaluacionCartera>(
      `${this.baseUrl}/${idEvaluacionCartera}`
    );
  }

  // =========================================================
  // BUSCAR POR FECHA DE CORTE
  // =========================================================

  buscarPorFechaCorte(
    fechaCorte: string
  ): Observable<EvaluacionCartera> {

    return this.http.get<EvaluacionCartera>(
      `${this.baseUrl}/fecha-corte/${fechaCorte}`
    );
  }

  // =========================================================
  // CREAR
  // =========================================================

  crear(
    dto: EvaluacionCarteraGuardar
  ): Observable<EvaluacionCartera> {

    return this.http.post<EvaluacionCartera>(
      this.baseUrl,
      dto
    );
  }

  // =========================================================
  // ACTUALIZAR
  // =========================================================

  actualizar(
    idEvaluacionCartera: number,
    dto: EvaluacionCarteraGuardar
  ): Observable<EvaluacionCartera> {

    return this.http.put<EvaluacionCartera>(
      `${this.baseUrl}/${idEvaluacionCartera}`,
      dto
    );
  }

  // =========================================================
  // EJECUTAR EVALUACIÓN
  // =========================================================

  ejecutar(
    idEvaluacionCartera: number
  ): Observable<EvaluacionCreditoResultado[]> {

    return this.http.post<EvaluacionCreditoResultado[]>(
      `${this.baseUrl}/${idEvaluacionCartera}/ejecutar`,
      null
    );
  }

  // =========================================================
  // MARCAR DEFINITIVA
  // =========================================================

   marcarDefinitiva(
     idEvaluacionCartera: number
   ): Observable<EvaluacionCartera> {

     return this.http.put<EvaluacionCartera>(
       `${this.baseUrl}/${idEvaluacionCartera}/definitiva`,
       null
     );
   }

}
