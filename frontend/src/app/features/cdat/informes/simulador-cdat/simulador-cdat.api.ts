import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

import {
  SimuladorAsociado,
  SimuladorBusquedaFiltros,
  SimuladorCdatEntrada,
  SimuladorCdatPreview
} from './simulador-cdat.models';

@Injectable({
  providedIn: 'root'
})
export class SimuladorCdatApi {

  private readonly simuladorUrl =
    `${environment.apiUrl}/cdat/informes/simulador-cdat`;

  private readonly reportingUrl =
    `${environment.apiUrl}/reporting/query`;

  constructor(
    private http: HttpClient
  ) {
  }

  buscarAsociados(
    filtros: SimuladorBusquedaFiltros
  ): Observable<SimuladorAsociado[]> {

    const req = {
      schema: 'depositos',
      view: 'vw_depositos_cuentas_ahorro_total',
      filters: {
        documento: filtros.documento,
        nombres: filtros.nombres,
        primer_apellido: filtros.primer_apellido,
        segundo_apellido: filtros.segundo_apellido
      }
    };

    return this.http.post<SimuladorAsociado[]>(
      this.reportingUrl,
      req
    );
  }

  generarPreview(
    entrada: SimuladorCdatEntrada
  ): Observable<SimuladorCdatPreview> {

    return this.http.post<SimuladorCdatPreview>(
      `${this.simuladorUrl}/preview`,
      entrada
    );
  }

  buscarCuentas(req: any): Observable<any> {
    return this.http.post<any>(this.reportingUrl, req);
  }

}
