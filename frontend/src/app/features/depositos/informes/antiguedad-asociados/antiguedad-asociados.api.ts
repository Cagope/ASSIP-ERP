import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface AntiguedadAsociadosRequest {
  fechaCorte: string;
  idAgencia: number;
  limitePantalla: number;
}

export interface AntiguedadAsociadosResumen {
  rangoAntiguedad: string;

  cantidadAsociados: number;

  saldoTotalAportes: number;

  saldoPromedioAportes: number;

  edadPromedio: number;
}

export interface AntiguedadAsociadosItem {

  idAgencia: number;
  nombreAgencia: string;

  codigoCuenta: string;

  documento: string;

  nombreCompleto: string;

  fechaVinculacion: string;

  aniosAsociado: number;

  edad: number;

  saldoAportes: number;

  ciudad: string;
  departamento: string;

  celular: string;

  correo: string;

  rangoAntiguedad: string;

}

export interface AntiguedadAsociadosResponse {

  resumen: AntiguedadAsociadosResumen[];

  mayoresAntiguedad: AntiguedadAsociadosItem[];

  itemsExcel: AntiguedadAsociadosItem[];

}

@Injectable({
  providedIn: 'root'
})
export class AntiguedadAsociadosApi {

  private readonly http =
    inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/antiguedad-asociados`;

  consultar(
    filtros: AntiguedadAsociadosRequest
  ): Promise<AntiguedadAsociadosResponse> {

    return firstValueFrom(
      this.http.post<AntiguedadAsociadosResponse>(
        this.base,
        filtros
      )
    );

  }

}
