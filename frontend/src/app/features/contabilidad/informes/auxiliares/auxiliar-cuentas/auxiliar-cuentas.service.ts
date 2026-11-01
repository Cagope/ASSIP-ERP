
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import {
  AuxiliarCuentasFiltro,
  AuxiliarCuentasResponse
} from './auxiliar-cuentas.models';

/**
 * Servicio de consulta del informe de auxiliares contables por cuentas.
 * Envía los filtros al backend y recibe los saldos y movimientos calculados.
 */
@Injectable({
  providedIn: 'root'
})
export class AuxiliarCuentasService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/contabilidad/informes/auxiliares/cuentas`;

  /**
   * Consulta las cuentas operables y sus movimientos para la agencia,
   * el rango de cuentas y el período seleccionados.
   */
  consultar(filtros: AuxiliarCuentasFiltro): Observable<AuxiliarCuentasResponse> {
    const params = new HttpParams()
      .set('idAgencia', filtros.idAgencia.toString())
      .set('cuentaInicial', filtros.cuentaInicial.trim())
      .set('cuentaFinal', filtros.cuentaFinal.trim())
      .set('fechaInicial', filtros.fechaInicial)
      .set('fechaFinal', filtros.fechaFinal);

    return this.http.get<AuxiliarCuentasResponse>(this.apiUrl, { params });
  }
}
