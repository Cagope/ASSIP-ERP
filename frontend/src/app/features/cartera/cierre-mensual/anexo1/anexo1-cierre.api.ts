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
  CierreMensualCartera
} from '../cierre-mensual-cartera.api';


// =========================================================
// RESULTADO ANEXO 1
// =========================================================

export interface ResultadoAnexo1 {

  edadesContables: number;

  deteriorosCapital: number;

  deteriorosIntereses: number;
}


// =========================================================
// API
// =========================================================

@Injectable({
  providedIn: 'root'
})
export class Anexo1CierreApi {

  private readonly http =
    inject(HttpClient);


  // =========================================================
  // URL BASE
  //
  // Backend:
  // /api/v1/cartera/anexo1
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/anexo1`;


  // =========================================================
  // CALCULAR SOLO EDAD CONTABLE
  // =========================================================

  calcularEdadContable(
    idCierreCartera: number
  ): Observable<number> {

    return this.http.post<number>(
      `${this.baseUrl}/${idCierreCartera}/edad-contable`,
      {}
    );
  }


  // =========================================================
  // PROCESAR ANEXO 1 COMPLETO
  // =========================================================

  procesar(
    idCierreCartera: number
  ): Observable<ResultadoAnexo1> {

    return this.http.post<ResultadoAnexo1>(
      `${this.baseUrl}/${idCierreCartera}/procesar`,
      {}
    );
  }


  // =========================================================
  // CERRAR ANEXO 1 EN FIRME
  //
  // POST
  //
  // /api/v1/cartera/anexo1/
  // {idCierreCartera}/cerrar
  // =========================================================

  cerrar(
    idCierreCartera: number
  ): Observable<CierreMensualCartera> {

    return this.http.post<CierreMensualCartera>(
      `${this.baseUrl}/${idCierreCartera}/cerrar`,
      {}
    );
  }

}
