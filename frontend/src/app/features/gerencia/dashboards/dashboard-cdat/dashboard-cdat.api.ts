import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../../environments/environment';

import {
  DashboardCdatRequest,
  DashboardCdatResponse
} from './dashboard-cdat.models';

@Injectable({
  providedIn: 'root'
})
export class DashboardCdatApi {

  private readonly url =
    `${environment.apiUrl}/gerencia/dashboard-cdat`;

  constructor(
    private http: HttpClient
  ) {
  }

  consultar(
    request: DashboardCdatRequest
  ): Observable<DashboardCdatResponse> {

    return this.http
      .post<DashboardCdatResponse>(`${this.url}/consultar`, request)
      .pipe(
        map(response => this.normalizar(response))
      );
  }

  private normalizar(
    response: DashboardCdatResponse | null | undefined
  ): DashboardCdatResponse {

    return {
      resumen: {
        totalCdats: Number(response?.resumen?.totalCdats ?? 0),
        valorTotalCaptado: Number(response?.resumen?.valorTotalCaptado ?? 0),
        promedioTasa: Number(response?.resumen?.promedioTasa ?? 0),
        promedioPlazo: Number(response?.resumen?.promedioPlazo ?? 0),
        vencen30Dias: Number(response?.resumen?.vencen30Dias ?? 0),
        renovacionesMes: Number(response?.resumen?.renovacionesMes ?? 0),
        cancelacionesMes: Number(response?.resumen?.cancelacionesMes ?? 0)
      },
      agencias: response?.agencias ?? [],
      plazos: response?.plazos ?? [],
      tasas: response?.tasas ?? [],
      tendencia: response?.tendencia ?? [],
      vencimientos: response?.vencimientos ?? []
    };
  }

}
