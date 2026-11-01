import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import { ComprobanteResumenFiltro, ComprobanteResumenResponse } from './comprobante-resumen.models';

@Injectable({ providedIn: 'root' })
export class ComprobanteResumenService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/contabilidad/informes/especiales/comprobante-resumen`;

  /** Consulta el resumen consolidado de la agencia y período seleccionados. */
  consultar(filtro: ComprobanteResumenFiltro): Observable<ComprobanteResumenResponse> {
    const params = new HttpParams()
      .set('idAgencia', filtro.idAgencia)
      .set('fechaInicial', filtro.fechaInicial)
      .set('fechaFinal', filtro.fechaFinal);
    return this.http.get<ComprobanteResumenResponse>(this.url, { params });
  }
}
