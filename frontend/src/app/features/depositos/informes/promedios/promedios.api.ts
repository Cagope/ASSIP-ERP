import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface PromediosRequest {
  fechaCorte: string;
  idAgencia: number;
  codigoForma: string;
  limitePantalla: number;
}

export interface PromediosResumen {
  totalCuentas: number;
  totalSaldos: number;
  saldoPromedio: number;
  saldoMayor: number;
  saldoMenor: number;
}

export interface PromediosForma {
  codigoForma: string;
  nombreForma: string;

  totalCuentas: number;
  totalSaldos: number;

  saldoPromedio: number;
  saldoMayor: number;
  saldoMenor: number;
}

export interface PromediosItem {
  codigoForma: string;
  nombreForma: string;

  codigoCuenta: string;

  documento: string;
  nombreCompleto: string;

  saldoCorte: number;
}

export interface PromediosResponse {
  resumen: PromediosResumen;
  formas: PromediosForma[];

  mayores: PromediosItem[];
  menores: PromediosItem[];

  itemsExcel: PromediosItem[];
}

@Injectable({
  providedIn: 'root'
})
export class PromediosApi {

  private readonly http =
    inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/promedios`;

  consultar(
    filtros: PromediosRequest
  ): Promise<PromediosResponse> {

    return firstValueFrom(
      this.http.post<PromediosResponse>(
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
