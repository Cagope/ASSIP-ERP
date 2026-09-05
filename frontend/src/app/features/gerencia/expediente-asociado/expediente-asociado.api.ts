import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import {
  ExpedienteAsociado,
  ExpedientePersonaBusqueda
} from './expediente-asociado.dto';

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

  buscarPersonas(
    documento: string,
    nombres: string,
    primerApellido: string,
    segundoApellido: string
  ): Observable<ExpedientePersonaBusqueda[]> {

    let params = new HttpParams();

    const documentoFiltro =
      String(documento ?? '').trim();

    const nombresFiltro =
      String(nombres ?? '').trim();

    const primerApellidoFiltro =
      String(primerApellido ?? '').trim();

    const segundoApellidoFiltro =
      String(segundoApellido ?? '').trim();

    if (documentoFiltro) {
      params = params.set(
        'documento',
        documentoFiltro
      );
    }

    if (nombresFiltro) {
      params = params.set(
        'nombres',
        nombresFiltro
      );
    }

    if (primerApellidoFiltro) {
      params = params.set(
        'primerApellido',
        primerApellidoFiltro
      );
    }

    if (segundoApellidoFiltro) {
      params = params.set(
        'segundoApellido',
        segundoApellidoFiltro
      );
    }

    return this.http.get<ExpedientePersonaBusqueda[]>(
      `${this.baseUrl}/buscar`,
      { params }
    );
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
