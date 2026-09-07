import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../../environments/environment';

import {
  DashboardCdatResponse,
  DashboardCdatVencimientoDetalle
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

  consultar(): Observable<DashboardCdatResponse> {

    return this.http
      .get<DashboardCdatResponse>(`${this.url}/consultar`)
      .pipe(
        map(response => this.normalizar(response))
      );
  }

  consultarDetalleVencimientos(
    rango: string
  ): Observable<DashboardCdatVencimientoDetalle[]> {

    return this.http.get<DashboardCdatVencimientoDetalle[]>(
      `${this.url}/vencimientos/detalle`,
      {
        params: {
          rango
        }
      }
    );
  }

  private normalizar(
    response: DashboardCdatResponse | null | undefined
  ): DashboardCdatResponse {

    return {
      resumen: {
        totalCdats:
          Number(response?.resumen?.totalCdats ?? 0),

        valorTotalCaptado:
          Number(response?.resumen?.valorTotalCaptado ?? 0),

        promedioTasa:
          Number(response?.resumen?.promedioTasa ?? 0),

        promedioPlazo:
          Number(response?.resumen?.promedioPlazo ?? 0),

        vencen30Dias:
          Number(response?.resumen?.vencen30Dias ?? 0),

        totalAsociados:
          Number(response?.resumen?.totalAsociados ?? 0)
      },

      agencias:
        response?.agencias ?? [],

      plazos:
        response?.plazos ?? [],

      tasas:
        response?.tasas ?? [],

      vencimientos:
        response?.vencimientos ?? [],

      tendencia:
        (response?.tendencia ?? []).map(item => ({
          fechaCorte:
            item.fechaCorte ?? null,

          periodo:
            item.periodo ?? '',

          cantidadCdats:
            Number(item.cantidadCdats ?? 0),

          valorCaptado:
            Number(item.valorCaptado ?? 0)
        }))
    };
  }
}
