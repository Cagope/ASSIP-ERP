import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { firstValueFrom } from 'rxjs';

import { environment }
  from '../../../../../environments/environment';

export interface DashboardDepositosResumen {
  totalAsociados: number;
  totalCuentas: number;
  saldoTotal: number;
  totalDebitos: number;
  totalCreditos: number;
  totalFormas: number;
  hombres: number;
  mujeres: number;
  juridicas: number;
  saldoAportes: number;
  saldoTac: number;
}

export interface DashboardDepositosForma {

  idFormaAhorro: number;

  codigoForma: string;
  nombreForma: string;

  cuentasActuales: number;
  saldoActual: number;

  hombres: number;
  mujeres: number;
  juridicas: number;

  cuentasAnteriores: number;
  saldoAnterior: number;

  ingresosPeriodo: number;
  egresosPeriodo: number;

  variacionSaldo: number;
  variacionCuentas: number;

  porcentajeCrecimiento: number;

}

export interface DashboardDepositosGrupo {

  concepto: string;

  cuentasAportes: number;
  valorAportes: number;
  participacionAportes: number;

  cuentasAhorros: number;
  valorAhorros: number;
  participacionAhorros: number;

  ranking: number;

}

export interface DashboardDepositosTendencia {

  periodo: string;

  totalCuentasAportes: number;
  totalCuentasDepositos: number;

  saldoAportes: number;
  saldoDepositos: number;

}

export interface DashboardDepositosResponse {

  resumen: DashboardDepositosResumen;

  formas: DashboardDepositosForma[];
  agencias: DashboardDepositosGrupo[];
  tendencia: DashboardDepositosTendencia[];

  fechaCorteAnterior: string;

}

@Injectable({
  providedIn: 'root'
})
export class DashboardDepositosApi {

  private readonly url =
    `${environment.apiUrl}/gerencia/dashboard-depositos`;

  constructor(
    private http: HttpClient
  ) {
  }

  consultar(
    fechaCorte: string
  ): Promise<DashboardDepositosResponse> {

    return firstValueFrom(
      this.http.post<DashboardDepositosResponse>(
        this.url,
        {
          fechaCorte
        }
      )
    );
  }

}
