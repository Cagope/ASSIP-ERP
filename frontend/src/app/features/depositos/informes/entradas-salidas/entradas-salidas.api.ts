import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface EntradasSalidasRequest {
  fechaInicial: string;
  fechaFinal: string;
  idAgencia: number;
  codigoForma: string;
}

export interface EntradasSalidasItem {
  fechaMovimiento: string;

  codigoForma?: string;
  nombreForma?: string;

  codigoCuenta?: string;
  documento?: string;
  nombreCompleto?: string;

  codigoMovimiento?: string;
  nombreMovimiento?: string;
  numeroMovimiento?: string;

  debito?: number;
  credito?: number;

  cantidadMovimientos: number;
  entradas: number;
  salidas: number;
  neto: number;
}

export interface EntradasSalidasResumen {
  totalDias: number;
  totalMovimientos: number;
  totalEntradas: number;
  totalSalidas: number;
  totalNeto: number;
}

export interface EntradasSalidasResponse {
  resumen: EntradasSalidasResumen;
  items: EntradasSalidasItem[];
}

@Injectable({
  providedIn: 'root'
})
export class EntradasSalidasApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/entradas-salidas`;

  consultar(
    filtros: EntradasSalidasRequest
  ): Promise<EntradasSalidasResponse> {

    return firstValueFrom(
      this.http.post<EntradasSalidasResponse>(
        this.base,
        filtros
      )
    );
  }

  resumenPorForma(
    filtros: EntradasSalidasRequest
  ): Promise<EntradasSalidasResponse> {

    return firstValueFrom(
      this.http.post<EntradasSalidasResponse>(
        `${this.base}/resumen-forma`,
        filtros
      )
    );
  }

  detalleMovimientos(
    filtros: EntradasSalidasRequest
  ): Promise<EntradasSalidasResponse> {

    return firstValueFrom(
      this.http.post<EntradasSalidasResponse>(
        `${this.base}/detalle`,
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
