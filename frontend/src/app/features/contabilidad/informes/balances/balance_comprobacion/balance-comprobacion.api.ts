
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import {
  BalanceComprobacionFiltro,
  BalanceComprobacionRespuesta,
  BalanceMovimientosFiltro,
  BalanceMovimientosRespuesta
} from './balance-comprobacion.models';

/** Centraliza las solicitudes HTTP del Balance de Comprobación. */
@Injectable({ providedIn: 'root' })
export class BalanceComprobacionApi {
  private readonly http = inject(HttpClient);
  private readonly url =
    `${environment.apiUrl}/contabilidad/informes/balances/balance-comprobacion`;

  /** Consulta el balance y sus resúmenes financieros. */
  consultar(
    filtro: BalanceComprobacionFiltro
  ): Observable<BalanceComprobacionRespuesta> {
    return this.http.post<BalanceComprobacionRespuesta>(
      `${this.url}/consultar`,
      filtro
    );
  }

  /** Consulta los movimientos paginados de una cuenta auxiliar operable. */
  consultarMovimientos(
    filtro: BalanceMovimientosFiltro
  ): Observable<BalanceMovimientosRespuesta> {
    return this.http.post<BalanceMovimientosRespuesta>(
      `${this.url}/movimientos`,
      filtro
    );
  }

  /** Descarga el informe PDF o Excel generado por el backend. */
  descargar(
    filtro: BalanceComprobacionFiltro,
    tipo: 'pdf' | 'excel'
  ): Observable<Blob> {
    return this.http.post(
      `${this.url}/${tipo}`,
      filtro,
      { responseType: 'blob' }
    );
  }
}
