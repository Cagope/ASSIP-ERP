import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export type TipoInformeMovimientosMeses =
  'RESUMEN' |
  'ASOCIADO';

export interface MovimientosPorMesesRequest {
  fechaCorte: string;
  idAgencia: number;
  codigoForma: string;
  meses: number;
  tipoInforme: TipoInformeMovimientosMeses;
}

export interface MovimientosPorMesesResumen {
  mes: string;
  cantidadMovimientos: number;
  entradas: number;
  salidas: number;
}

export interface MovimientosPorMesesAsociado {
  documento: string;
  nombreCompleto: string;
  codigoCuenta: string;
  codigoForma: string;
  nombreForma: string;
  mes: string;
  cantidadMovimientos: number;
  entradas: number;
  salidas: number;
}

export interface MovimientosPorMesesResponse {
  meses: string[];
  resumen: MovimientosPorMesesResumen[];
  detalleAsociado: MovimientosPorMesesAsociado[];
  totalMovimientos: number;
  totalEntradas: number;
  totalSalidas: number;
}

@Injectable({
  providedIn: 'root'
})
export class MovimientosPorMesesApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/movimientos-por-meses`;

  consultar(
    filtros: MovimientosPorMesesRequest
  ): Promise<MovimientosPorMesesResponse> {

    return firstValueFrom(
      this.http.post<MovimientosPorMesesResponse>(
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
