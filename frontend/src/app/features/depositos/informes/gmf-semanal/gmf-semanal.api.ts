import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface GmfSemanalRequest {
  fechaInicial: string;
  fechaFinal: string;
  idAgencia: number;
  numeroSemana: number;
  codigoForma: string;
}

export interface GmfSemanalItem {
  fechaMovimiento: string;
  codigoCuenta: string;
  tipoMovimiento: string;
  descripcionMovimiento: string;
  documentoSoporte: string;
  documentoAsociado: string;
  nombreAsociado: string;
  codigoForma: string;
  nombreForma: string;
  estadoGmfCuenta: string;

  baseAsumido: number;
  gmfAsumido: number;

  baseAsociado: number;
  gmfAsociado: number;

  baseRetiro: number;
  gmfRetiro: number;
  valorExento: number;

  baseChequeAsumido: number;
  gmfChequeAsumido: number;
  baseChequeExento: number;
  gmfChequeExento: number;
}

export interface GmfSemanalResumen {
  totalBaseAsumido: number;
  totalGmfAsumido: number;

  totalBaseAsociado: number;
  totalGmfAsociado: number;

  totalBaseRetiro: number;
  totalGmfRetiro: number;
  totalValorExento: number;

  totalBaseChequeAsumido: number;
  totalGmfChequeAsumido: number;
  totalBaseChequeExento: number;
  totalGmfChequeExento: number;

  totalBaseGravada: number;
  totalGmf: number;
}

export interface GmfSemanalResponse {
  fechaInicial: string;
  fechaFinal: string;
  idAgencia: number;
  numeroSemana: number;
  codigoForma: string;
  resumen: GmfSemanalResumen;
  items: GmfSemanalItem[];
}

@Injectable({
  providedIn: 'root'
})
export class GmfSemanalApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/gmf-semanal`;

  consultar(
    filtros: GmfSemanalRequest
  ): Promise<GmfSemanalResponse> {

    return firstValueFrom(
      this.http.post<GmfSemanalResponse>(
        this.base,
        filtros
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
