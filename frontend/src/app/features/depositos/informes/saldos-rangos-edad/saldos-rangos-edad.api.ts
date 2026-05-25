import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface SaldosRangosEdadRango {
  nombreRango: string;
  edadInicial: number;
  edadFinal: number;
}

export interface SaldosRangosEdadRequest {
  fechaCorte: string;
  idAgencia: number;
  rangos: SaldosRangosEdadRango[];
}

export interface SaldosRangosEdadResumen {
  nombreRango: string;
  edadInicial: number;
  edadFinal: number;
  cantidadAsociados: number;
  saldoTotalAportes: number;
  saldoPromedioAportes: number;
  salarioPromedio: number;
}

export interface SaldosRangosEdadItem {
  idAgencia: number;
  nombreAgencia: string;
  documento: string;
  nombreCompleto: string;
  edad: number;
  nombreRango: string;
  ocupacion: string;
  sectorEconomico: string;
  salario: number;
  otrosIngresos: number;
  totalIngresos: number;
  ciudad: string;
  celular: string;
  correo: string;
  saldoAportes: number;
}

export interface SaldosRangosEdadResponse {
  resumen: SaldosRangosEdadResumen[];
  itemsExcel: SaldosRangosEdadItem[];
}

@Injectable({
  providedIn: 'root'
})
export class SaldosRangosEdadApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/saldos-rangos-edad`;

  consultar(
    filtros: SaldosRangosEdadRequest
  ): Promise<SaldosRangosEdadResponse> {

    return firstValueFrom(
      this.http.post<SaldosRangosEdadResponse>(
        this.base,
        filtros
      )
    );
  }

}
