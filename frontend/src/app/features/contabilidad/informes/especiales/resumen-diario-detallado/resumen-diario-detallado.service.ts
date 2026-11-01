
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../../../environments/environment';

import {
  ResumenDiarioDetalladoFiltro,
  ResumenDiarioDetalladoResponse
} from './resumen-diario-detallado.models';

/**
 * Consulta los movimientos individuales del Resumen diario detallado.
 * Reutiliza el endpoint de informes especiales del backend.
 */
@Injectable({
  providedIn: 'root'
})
export class ResumenDiarioDetalladoService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    `${environment.apiUrl}/contabilidad/informes/especiales/resumen-diario-detallado`;

  /** Obtiene los movimientos y totales de la agencia y período seleccionados. */
  consultar(
    filtros: ResumenDiarioDetalladoFiltro
  ): Observable<ResumenDiarioDetalladoResponse> {
    const params = new HttpParams()
      .set('idAgencia', filtros.idAgencia.toString())
      .set('fechaInicial', filtros.fechaInicial)
      .set('fechaFinal', filtros.fechaFinal);

    return this.http.get<ResumenDiarioDetalladoResponse>(
      this.apiUrl,
      { params }
    );
  }
}
