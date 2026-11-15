import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import {
  BalancesVigenciasFiltro, BalancesVigenciasRespuesta, CuentaPrincipal,
  BalancesVigenciasMovimientosFiltro, BalancesVigenciasMovimientosRespuesta
} from './balances-vigencias.models';

/** Centraliza las llamadas HTTP del informe. */
@Injectable({ providedIn: 'root' })
export class BalancesVigenciasApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/contabilidad/informes/balances/balances-vigencias`;

  consultar(filtro: BalancesVigenciasFiltro): Observable<BalancesVigenciasRespuesta> {
    return this.http.post<BalancesVigenciasRespuesta>(`${this.url}/consultar`, filtro);
  }

  listarCuentas(idAgencia: number): Observable<CuentaPrincipal[]> {
    return this.http.get<CuentaPrincipal[]>(`${this.url}/cuentas-principales`, {
      params: new HttpParams().set('idAgencia', String(idAgencia))
    });
  }

  consultarMovimientos(
    filtro: BalancesVigenciasMovimientosFiltro
  ): Observable<BalancesVigenciasMovimientosRespuesta> {
    return this.http.post<BalancesVigenciasMovimientosRespuesta>(
      `${this.url}/movimientos`, filtro
    );
  }

  descargar(filtro: BalancesVigenciasFiltro, tipo: 'pdf' | 'excel'): Observable<Blob> {
    return this.http.post(`${this.url}/${tipo}`, filtro, { responseType: 'blob' });
  }
}
