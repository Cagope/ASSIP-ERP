import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../../environments/environment';

import {
  FechasCdatItem,
  FechasCdatRequest,
  FechasCdatResponse,
  FechasCdatResumen
} from './fechas-cdat.models';

@Injectable({
  providedIn: 'root'
})
export class FechasCdatApi {

  private readonly url =
    `${environment.apiUrl}/cdat/informes/fechas`;

  constructor(
    private http: HttpClient
  ) {
  }

  consultar(
    request: FechasCdatRequest
  ): Observable<FechasCdatResponse> {

    return this.http
      .post<FechasCdatResponse>(`${this.url}/consultar`, request)
      .pipe(
        map(response => this.normalizarResponse(response))
      );
  }

  private normalizarResponse(
    response: FechasCdatResponse | null | undefined
  ): FechasCdatResponse {

    const resumen: FechasCdatResumen = {
      cantidad: Number(response?.resumen?.cantidad ?? 0),
      valorTotal: Number(response?.resumen?.valorTotal ?? 0),
      promedioTasa: Number(response?.resumen?.promedioTasa ?? 0),
      promedioPlazo: Number(response?.resumen?.promedioPlazo ?? 0)
    };

    const resultados: FechasCdatItem[] =
      (response?.resultados ?? []).map(item => ({
        idCuentaCdat: Number(item.idCuentaCdat ?? 0),
        codigoCdat: item.codigoCdat ?? '',
        documento: item.documento ?? '',
        nombreCompleto: item.nombreCompleto ?? '',
        agencia: item.agencia ?? '',
        fechaApertura: item.fechaApertura ?? '',
        fechaVencimiento: item.fechaVencimiento ?? '',
        fechaCancelacion: item.fechaCancelacion ?? null,
        fechaRenovacion: item.fechaRenovacion ?? null,
        plazoMeses: Number(item.plazoMeses ?? 0),
        tasa: Number(item.tasa ?? 0),
        valor: Number(item.valor ?? 0),
        estado: item.estado ?? ''
      }));

    return {
      resumen,
      resultados
    };
  }

}
