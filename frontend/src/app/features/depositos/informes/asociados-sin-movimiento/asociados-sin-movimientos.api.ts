import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface AsociadosSinMovimientosRequest {
  fechaCorte: string;
  idAgencia: number;
  codigoForma: string;
  diasMinimos: number;
  tipoSaldo: number;
  saldoMinimo: number;
}

export interface AsociadosSinMovimientosItem {
  idCuentaAhorro: number;
  idDatosPersonal: number;

  codigoCuenta: string;

  documento: string;
  nombreCompleto: string;

  codigoForma: string;
  nombreForma: string;

  codigoAgencia: string;
  nombreAgencia: string;

  fechaAperturaCuenta: string;
  fechaUltimoMovimiento: string;

  diasSinMovimiento: number;

  saldoActual: number;

  estadoCuenta: string;
}

export interface AsociadosSinMovimientosResumen {
  totalCuentas: number;
  totalConSaldo: number;
  totalSinSaldo: number;
  saldoTotal: number;
}

export interface AsociadosSinMovimientosResponse {
  resumen: AsociadosSinMovimientosResumen;
  items: AsociadosSinMovimientosItem[];
}

@Injectable({
  providedIn: 'root'
})
export class AsociadosSinMovimientosApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/asociados_sin_movimientos`;

  consultar(
    request: AsociadosSinMovimientosRequest
  ): Promise<AsociadosSinMovimientosResponse> {

    return firstValueFrom(
      this.http.post<AsociadosSinMovimientosResponse>(
        this.base,
        request
      )
    );
  }

  listarFormasAhorro(): Promise<any[]> {

    return firstValueFrom(
      this.http.get<any[]>(
        `${environment.apiUrl}/depositos/formas-ahorro`
      )
    );
  }

}
