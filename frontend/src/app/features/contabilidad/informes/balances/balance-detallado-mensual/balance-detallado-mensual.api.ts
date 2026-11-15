
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import {
  BalanceDetalladoMensualFiltro,
  BalanceDetalladoMensualRespuesta,
  BalanceMovimientosFiltro,
  BalanceMovimientosRespuesta
} from './balance-detallado-mensual.models';

/** Centraliza las solicitudes HTTP del balance detallado mensual. */
@Injectable({ providedIn: 'root' })
export class BalanceDetalladoMensualApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/contabilidad/informes/balances/balance-detallado-mensual`;

  /** Consulta el balance mensual o comparativo con los filtros autorizados. */
  consultar(filtro: BalanceDetalladoMensualFiltro): Observable<BalanceDetalladoMensualRespuesta> {
    return this.http.post<BalanceDetalladoMensualRespuesta>(`${this.url}/consultar`, filtro);
  }

  /** Consulta los movimientos paginados de una cuenta auxiliar operable. */
  consultarMovimientos(filtro: BalanceMovimientosFiltro): Observable<BalanceMovimientosRespuesta> {
    return this.http.post<BalanceMovimientosRespuesta>(`${this.url}/movimientos`, filtro);
  }

  /** Descarga el PDF o Excel generado por el backend. */
  descargar(filtro: BalanceDetalladoMensualFiltro, tipo: 'pdf' | 'excel'): Observable<Blob> {
    return this.http.post(`${this.url}/${tipo}`, filtro, { responseType: 'blob' });
  }
}
