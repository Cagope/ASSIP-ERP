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

  constructor(
    private http: HttpClient
  ) {
  }

  // ======================================================
  // BÚSQUEDA DE CUENTAS DE AHORRO
  // ======================================================

  buscarCuentas(
    filtros: SimuladorBusquedaFiltros
  ): Observable<SimuladorAsociado[]> {

    return this.http.get<SimuladorAsociado[]>(
      `${this.simuladorUrl}/cuentas-ahorro`,
      {
        params: {
          documento: filtros.documento?.trim() || '',
          nombres: filtros.nombres?.trim() || '',
          primerApellido: filtros.primer_apellido?.trim() || '',
          segundoApellido: filtros.segundo_apellido?.trim() || ''
        }
      }
    );
  }

  // ======================================================
  // PREVIEW DEL SIMULADOR
  // ======================================================

  generarPreview(
    entrada: SimuladorCdatEntrada
  ): Observable<SimuladorCdatPreview> {

    return this.http.post<SimuladorCdatPreview>(
      `${this.simuladorUrl}/preview`,
      entrada
    );
  }

}
