import { Injectable, inject } from '@angular/core';
import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../../../../environments/environment';

import {
  CentralRiesgoResultadoImportacion,
  CentralRiesgoResultadoDato
} from './central-riesgo-resultado.models';

@Injectable({
  providedIn: 'root'
})
export class CentralRiesgoResultadoApi {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/centrales-riesgo/resultados`;

  importar(
    idCentralRiesgo: number,
    fechaCorte: string,
    archivo: File,
    reemplazar = false
  ): Observable<CentralRiesgoResultadoImportacion> {

    const formData = new FormData();

    formData.append(
      'archivo',
      archivo,
      archivo.name
    );

    const params = new HttpParams()
      .set(
        'idCentralRiesgo',
        idCentralRiesgo
      )
      .set(
        'fechaCorte',
        fechaCorte
      )
      .set(
        'reemplazar',
        reemplazar
      );

    return this.http.post<CentralRiesgoResultadoImportacion>(
      `${this.baseUrl}/importar`,
      formData,
      {
        params
      }
    );
  }

  buscarArchivo(
    idCentralArchivo: number
  ): Observable<CentralRiesgoResultadoImportacion> {

    return this.http.get<CentralRiesgoResultadoImportacion>(
      `${this.baseUrl}/archivo/${idCentralArchivo}`
    );

  }

  listarDatos(
    idCentralArchivo: number
  ): Observable<CentralRiesgoResultadoDato[]> {

    return this.http.get<CentralRiesgoResultadoDato[]>(
      `${this.baseUrl}/archivo/${idCentralArchivo}/datos`
    );

  }

}
