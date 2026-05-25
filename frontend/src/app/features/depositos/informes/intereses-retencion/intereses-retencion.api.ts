import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface InteresesRetencionRequest {

  fechaInicial: string;
  fechaFinal: string;

  idAgencia: number;

  codigoForma: string;

  codigoCuenta: string;

}

export interface InteresesRetencionItem {

  codigoAgencia: string;
  nombreAgencia: string;

  codigoForma: string;
  nombreForma: string;

  codigoCuenta: string;

  documento: string;
  nombreCompleto: string;

  intereses: number;
  retencion: number;
  neto: number;

}

export interface InteresesRetencionResumen {

  totalCuentas: number;

  totalIntereses: number;
  totalRetencion: number;
  totalNeto: number;

}

export interface InteresesRetencionResponse {

  resumen: InteresesRetencionResumen;

  items: InteresesRetencionItem[];

}

@Injectable({
  providedIn: 'root'
})
export class InteresesRetencionApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/intereses-retencion`;

  consultar(
    filtros: InteresesRetencionRequest
  ): Promise<InteresesRetencionResponse> {

    return firstValueFrom(
      this.http.post<InteresesRetencionResponse>(
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
