import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import { BalanceComparadoMensualFiltro, BalanceComparadoMensualRespuesta } from './balance-comparado-mensual.models';

/** Centraliza las solicitudes de consulta y exportación autorizadas por el backend. */
@Injectable({ providedIn: 'root' })
export class BalanceComparadoMensualApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/contabilidad/informes/balances/balance-comparado-mensual`;

  consultar(filtro: BalanceComparadoMensualFiltro): Observable<BalanceComparadoMensualRespuesta> {
    return this.http.post<BalanceComparadoMensualRespuesta>(`${this.url}/consultar`, filtro);
  }

  descargar(filtro: BalanceComparadoMensualFiltro, tipo: 'pdf' | 'excel'): Observable<Blob> {
    return this.http.post(`${this.url}/${tipo}`, filtro, { responseType: 'blob' });
  }
}
