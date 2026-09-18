import {
  Injectable
} from '@angular/core';

import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  environment
} from '../../../../../../environments/environment';

import {
  SolicitudListado,
  SolicitudListadoFiltros
} from './solicitud-list.models';


@Injectable({
  providedIn: 'root'
})
export class SolicitudListApi {

  // =========================================================
  // URL
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/originacion/solicitudes/list`;


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly http: HttpClient
  ) {}


  // =========================================================
  // LISTAR SOLICITUDES
  // =========================================================

  listar(
    filtros?: Partial<SolicitudListadoFiltros>
  ): Observable<SolicitudListado[]> {

    let params =
      new HttpParams();

    if (filtros?.idAgencia != null) {

      params = params.set(
        'idAgencia',
        filtros.idAgencia.toString()
      );
    }

    const numeroSolicitud =
      filtros?.numeroSolicitud?.trim() ?? '';

    if (numeroSolicitud) {

      params = params.set(
        'numeroSolicitud',
        numeroSolicitud
      );
    }

    const documento =
      filtros?.documento?.trim() ?? '';

    if (documento) {

      params = params.set(
        'documento',
        documento
      );
    }

    const nombreSolicitante =
      filtros?.nombreSolicitante?.trim() ?? '';

    if (nombreSolicitante) {

      params = params.set(
        'nombreSolicitante',
        nombreSolicitante
      );
    }

    if (filtros?.idAsesor != null) {

      params = params.set(
        'idAsesor',
        filtros.idAsesor.toString()
      );
    }

    if (filtros?.idSolicitudProceso != null) {

      params = params.set(
        'idSolicitudProceso',
        filtros.idSolicitudProceso.toString()
      );
    }

    if (filtros?.idSolicitudResultado != null) {

      params = params.set(
        'idSolicitudResultado',
        filtros.idSolicitudResultado.toString()
      );
    }

    return this.http.get<SolicitudListado[]>(
      this.baseUrl,
      {
        params
      }
    );
  }
}
