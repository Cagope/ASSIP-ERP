import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface MovimientosDiariosRequest {
  fechaInicial: string;
  fechaFinal: string;
  idAgencia: number;
  codigoForma: string;
  codigoMovimiento: string;
}

export interface MovimientosDiariosItem {
  idExtractoCuentaAhorro: number;
  fechaMovimiento: string;
  horaMovimiento: string;

  codigoAgencia: string;
  nombreAgencia: string;

  codigoCuenta: string;
  documento: string;
  nombreCompleto: string;

  codigoForma: string;
  nombreForma: string;

  codigoMovimiento: string;
  nombreMovimiento: string;

  debito: number;
  credito: number;
}

export interface MovimientosDiariosResumen {
  totalMovimientos: number;
  totalDebitos: number;
  totalCreditos: number;
}

export interface MovimientosDiariosResponse {
  resumen: MovimientosDiariosResumen;
  items: MovimientosDiariosItem[];
}

@Injectable({
  providedIn: 'root'
})
export class MovimientosDiariosApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/movimientos-diarios`;

  consultar(
    filtros: MovimientosDiariosRequest
  ): Promise<MovimientosDiariosResponse> {

    return firstValueFrom(
      this.http.post<MovimientosDiariosResponse>(
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

  listarTiposMovimiento(): Promise<any[]> {
    return firstValueFrom(
      this.http.get<any[]>(
        `${environment.apiUrl}/depositos/informes/movimientos-diarios/tipos-movimiento`
      )
    );
  }

}
