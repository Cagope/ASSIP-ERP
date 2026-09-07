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
  ConcentracionDepositosRequest,
  ConcentracionDepositosResponse
} from './concentracion-depositos.models';


@Injectable({
  providedIn: 'root'
})
export class ConcentracionDepositosApi {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/depositos/analisis/concentracion`;


  // =========================================================
  // ANALIZAR
  // =========================================================

  analizar(
    consulta: ConcentracionDepositosRequest
  ): Observable<ConcentracionDepositosResponse> {

    return this.http.post<ConcentracionDepositosResponse>(
      this.baseUrl,
      consulta
    );
  }

}
