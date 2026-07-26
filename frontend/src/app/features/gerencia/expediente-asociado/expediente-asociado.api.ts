import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ExpedienteAsociado } from './expediente-asociado.dto';

@Injectable({
  providedIn: 'root'
})
export class ExpedienteAsociadoApi {

  private readonly baseUrl =
    `${environment.apiUrl}/gerencia/expediente-asociado`;

  constructor(
    private readonly http: HttpClient
  ) {
  }

  consultarPorIdDatosPersonal(
    idDatosPersonal: number
  ): Observable<ExpedienteAsociado> {

    if (
      idDatosPersonal === null ||
      idDatosPersonal === undefined ||
      !Number.isInteger(idDatosPersonal) ||
      idDatosPersonal <= 0
    ) {
      throw new Error(
        'El idDatosPersonal debe ser un número entero mayor que cero.'
      );
    }

    return this.http.get<ExpedienteAsociado>(
      `${this.baseUrl}/${idDatosPersonal}`
    );
  }
}
