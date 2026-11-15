
import {
  HttpClient,
  HttpParams,
  HttpResponse
} from '@angular/common/http';

import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

export interface PeriodoFormato3 { ano: number; mes: number; }
export interface RenglonFormato3 { codigoRenglon: string; descripcion: string; saldo: number; }
export interface ConsultaFormato3 { fechaCorte: string; cantidadRenglones: number; detalle: RenglonFormato3[]; }

@Injectable({
  providedIn: 'root'
})
export class Formato3Api {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/ses/contabilidad/formato-3`;

  periodos(): Observable<PeriodoFormato3[]> {
    return this.http.get<PeriodoFormato3[]>(`${this.baseUrl}/periodos`);
  }

  consultar(ano: number, mes: number): Observable<ConsultaFormato3> {
    const params = new HttpParams().set('ano', ano.toString()).set('mes', mes.toString());
    return this.http.get<ConsultaFormato3>(`${this.baseUrl}/consulta`, { params });
  }

  /**
   * Genera y obtiene el archivo plano del Formato 3.
   *
   * El backend:
   * - Valida que el período esté cerrado.
   * - Consulta los saldos consolidados.
   * - Aplica el ajuste de excedentes o pérdidas.
   * - Devuelve el TXT con su nombre oficial.
   *
   * Se conserva la respuesta HTTP completa para
   * consultar la cabecera Content-Disposition.
   *
   * No modifica información contable.
   */
  descargar(
    ano: number,
    mes: number
  ): Observable<HttpResponse<Blob>> {

    const params = new HttpParams()
      .set('ano', ano.toString())
      .set('mes', mes.toString());

    return this.http.get(this.baseUrl, {
      params,
      observe: 'response',
      responseType: 'blob'
    });
  }
}
