import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SaldosCorteApi {

  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/depositos/informes/saldos`;

  // ======================================================
  // 🔍 Consulta principal (FUNCIONA Y NO SE TOCA)
  // ======================================================
  consultar(filtros: { agencia: number; fechaCorte: string }) {
    return this.http
      .post<any>(`${this.base}/corte`, filtros)
      .toPromise();
  }

  // ======================================================
  // 🆕 Resumen por agencia y forma
  // ======================================================
  resumenPorAgencia(
    fechaCorte: string,
    agencia: number
  ) {
    return this.http
      .get<any[]>(`${this.base}/resumen-agencia`, {
        params: {
          fechaCorte,
          agencia
        }
      })
      .toPromise();
  }
}
