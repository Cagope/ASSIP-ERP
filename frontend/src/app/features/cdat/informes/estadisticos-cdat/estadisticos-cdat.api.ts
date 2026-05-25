import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';

import {
  EstadisticosCdatDetalle,
  EstadisticosCdatDetalleRequest,
  EstadisticosCdatRequest,
  EstadisticosCdatResponse
} from './estadisticos-cdat.models';

@Injectable({
  providedIn: 'root'
})
export class EstadisticosCdatApi {

  private readonly url =
    `${environment.apiUrl}/cdat/informes/estadisticos-cdat`;

  constructor(
    private http: HttpClient
  ) {
  }

  consultar(
    req: EstadisticosCdatRequest
  ): Observable<EstadisticosCdatResponse> {

    return this.http.post<EstadisticosCdatResponse>(
      `${this.url}/consultar`,
      req
    );
  }

  detalle(
    req: EstadisticosCdatDetalleRequest
  ): Observable<EstadisticosCdatDetalle[]> {

    return this.http.post<EstadisticosCdatDetalle[]>(
      `${this.url}/detalle`,
      req
    );
  }
}
