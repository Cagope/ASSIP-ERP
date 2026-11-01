import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import { VerificarPartidaDobleFiltro, VerificarPartidaDobleResponse } from './verificar-partida-doble.models';

@Injectable({ providedIn: 'root' })
export class VerificarPartidaDobleService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/contabilidad/informes/especiales/verificar-partida-doble`;

  /** Solicita la verificación de la agencia y período autorizados. */
  consultar(filtro: VerificarPartidaDobleFiltro): Observable<VerificarPartidaDobleResponse> {
    const params = new HttpParams()
      .set('idAgencia', String(filtro.idAgencia))
      .set('fechaInicial', filtro.fechaInicial)
      .set('fechaFinal', filtro.fechaFinal);
    return this.http.get<VerificarPartidaDobleResponse>(this.url, { params });
  }
}
