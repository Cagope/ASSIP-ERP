import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface ResumenTipoMovimientoRequest {
  fechaInicial: string;
  fechaFinal: string;
  idAgencia: number;
  codigoForma: string;
  codigoMovimiento: string;
}

export interface ResumenTipoMovimientoItem {

  codigoMovimiento: string;
  nombreMovimiento: string;

  cantidadMovimientos: number;

  totalDebitos: number;
  totalCreditos: number;
  neto: number;

  idExtractoCuentaAhorro?: number;

  fechaMovimiento?: string;
  horaMovimiento?: string;

  codigoAgencia?: string;
  nombreAgencia?: string;

  codigoForma?: string;
  nombreForma?: string;

  codigoCuenta?: string;
  documento?: string;
  nombreCompleto?: string;

  debito?: number;
  credito?: number;

}

export interface ResumenTipoMovimientoResumen {

  totalTipos: number;
  totalMovimientos: number;

  totalDebitos: number;
  totalCreditos: number;
  totalNeto: number;

}

export interface ResumenTipoMovimientoResponse {

  resumen: ResumenTipoMovimientoResumen;

  items: ResumenTipoMovimientoItem[];

}

@Injectable({
  providedIn: 'root'
})
export class ResumenTipoMovimientoApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/resumen-tipo-movimiento`;

  consultar(
    filtros: ResumenTipoMovimientoRequest
  ): Promise<ResumenTipoMovimientoResponse> {

    return firstValueFrom(
      this.http.post<ResumenTipoMovimientoResponse>(
        this.base,
        filtros
      )
    );
  }

  detalle(
    filtros: ResumenTipoMovimientoRequest
  ): Promise<ResumenTipoMovimientoResponse> {

    return firstValueFrom(
      this.http.post<ResumenTipoMovimientoResponse>(
        `${this.base}/detalle`,
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
