
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../../../environments/environment';

import {
  LibroDiarioResumidoFiltro,
  LibroDiarioResumidoResponse
} from './libro-diario-resumido.models';

/**
 * Servicio de consulta del informe Libro diario resumido.
 * Envía los filtros al backend y recibe los movimientos
 * agrupados y los totales calculados.
 */
@Injectable({
  providedIn: 'root'
})
export class LibroDiarioResumidoService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    `${environment.apiUrl}/contabilidad/informes/especiales/libro-diario-resumido`;

  /**
   * Consulta el Libro diario resumido para la agencia,
   * el período y la modalidad seleccionados.
   */
  consultar(
    filtros: LibroDiarioResumidoFiltro
  ): Observable<LibroDiarioResumidoResponse> {

    const params = new HttpParams()
      .set('idAgencia', filtros.idAgencia.toString())
      .set('fechaInicial', filtros.fechaInicial)
      .set('fechaFinal', filtros.fechaFinal)
      .set('modalidad', filtros.modalidad);

    return this.http.get<LibroDiarioResumidoResponse>(
      this.apiUrl,
      { params }
    );
  }
}
