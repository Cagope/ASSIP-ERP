import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface SaldosMenoresRequest {
  fechaCorte: string;
  idAgencia: number;
  codigoForma: string;
  valorMaximo: number;
}

export interface SaldosMenoresResumen {
  totalCuentas: number;
  totalSaldos: number;
  saldoPromedio: number;
  valorMaximo: number;
}

export interface SaldosMenoresItem {
  idAgencia: number;
  nombreAgencia: string;

  codigoForma: string;
  nombreForma: string;

  codigoCuenta: string;

  documento: string;
  nombreCompleto: string;

  fechaApertura: string;
  estadoCuenta: string;

  saldoCorte: number;
}

export interface SaldosMenoresResponse {
  resumen: SaldosMenoresResumen;
  items: SaldosMenoresItem[];
}

@Injectable({
  providedIn: 'root'
})
export class SaldosMenoresApi {

  private readonly http =
    inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/saldos-menores`;

  consultar(
    filtros: SaldosMenoresRequest
  ): Promise<SaldosMenoresResponse> {

    return firstValueFrom(
      this.http.post<SaldosMenoresResponse>(
        this.base,
        filtros
      )
    );

  }

  listarFormasAhorro() {

    return this.http
      .get<any[]>(
        `${environment.apiUrl}/depositos/formas-ahorro`
      )
      .toPromise();

  }

}
