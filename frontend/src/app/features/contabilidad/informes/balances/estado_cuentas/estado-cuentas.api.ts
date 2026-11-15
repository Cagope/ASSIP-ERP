import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import {
  EstadoCuentasFiltro, EstadoCuentasRespuesta, CuentaPrincipal,
  EstadoCuentasMovimientosFiltro, EstadoCuentasMovimientosRespuesta
} from './estado-cuentas.models';

/** Centraliza las llamadas HTTP del informe. */
@Injectable({ providedIn: 'root' })
export class EstadoCuentasApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/contabilidad/informes/balances/estado-cuentas`;

  consultar(filtro: EstadoCuentasFiltro): Observable<EstadoCuentasRespuesta> {
    return this.http.post<EstadoCuentasRespuesta>(`${this.url}/consultar`, filtro);
  }

  listarCuentas(idAgencia: number): Observable<CuentaPrincipal[]> {
    return this.http.get<CuentaPrincipal[]>(`${this.url}/cuentas-principales`, {
      params: new HttpParams().set('idAgencia', String(idAgencia))
    });
  }

  consultarMovimientos(
    filtro: EstadoCuentasMovimientosFiltro
  ): Observable<EstadoCuentasMovimientosRespuesta> {
    return this.http.post<EstadoCuentasMovimientosRespuesta>(
      `${this.url}/movimientos`, filtro
    );
  }

  descargar(filtro: EstadoCuentasFiltro, tipo: 'pdf' | 'excel'): Observable<Blob> {
    return this.http.post(`${this.url}/${tipo}`, filtro, { responseType: 'blob' });
  }
}
