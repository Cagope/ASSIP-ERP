
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import {
  SecuencialComprobantesFiltro,
  SecuencialComprobantesResponse
} from './secuencial-comprobantes.models';

@Injectable({
  providedIn: 'root'
})
export class SecuencialComprobantesService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl =
    `${environment.apiUrl}/contabilidad/informes/especiales/secuencial-comprobantes`;

  /**
   * Consulta el secuencial de comprobantes de una agencia y período.
   * Envía los filtros al backend, donde se validan los permisos
   * y el acceso del usuario a la agencia solicitada.
   */
  consultar(
    filtros: SecuencialComprobantesFiltro
  ): Observable<SecuencialComprobantesResponse> {
    const params = new HttpParams()
      .set('idAgencia', filtros.idAgencia.toString())
      .set('fechaInicial', filtros.fechaInicial)
      .set('fechaFinal', filtros.fechaFinal);

    return this.http.get<SecuencialComprobantesResponse>(
      this.apiUrl,
      { params }
    );
  }
}
