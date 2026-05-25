import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface ExtractoPorValorRequest {

  fechaInicial: string;
  fechaFinal: string;

  idAgencia: number;

  codigoForma: string;

  valor: number;

  tipoBusqueda: string;

}

export interface ExtractoPorValorItem {

  idExtractoCuentaAhorro: number;

  fechaMovimiento: string;
  horaMovimiento: string;

  codigoAgencia: string;
  nombreAgencia: string;

  codigoForma: string;
  nombreForma: string;

  codigoCuenta: string;

  documento: string;
  nombreCompleto: string;

  codigoMovimiento: string;
  nombreMovimiento: string;

  debito: number;
  credito: number;

}

export interface ExtractoPorValorResumen {

  totalMovimientos: number;

  totalDebitos: number;
  totalCreditos: number;
  totalNeto: number;

}

export interface ExtractoPorValorResponse {

  resumen: ExtractoPorValorResumen;

  items: ExtractoPorValorItem[];

}

@Injectable({
  providedIn: 'root'
})
export class ExtractoPorValorApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/extracto-por-valor`;

  consultar(
    filtros: ExtractoPorValorRequest
  ): Promise<ExtractoPorValorResponse> {

    return firstValueFrom(
      this.http.post<ExtractoPorValorResponse>(
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
