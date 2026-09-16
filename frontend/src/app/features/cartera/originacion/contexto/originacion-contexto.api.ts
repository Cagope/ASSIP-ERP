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
} from '../../../../../environments/environment';

import {
  OriginacionCartera,
  OriginacionCodeuda,
  OriginacionContexto,
  OriginacionVectorDetalle
} from './originacion-contexto.models';


@Injectable({
  providedIn: 'root'
})
export class OriginacionContextoApi {

  // =========================================================
  // URLS
  // =========================================================

  private readonly contextoUrl =
    `${environment.apiUrl}/cartera/originacion/contexto`;

  private readonly vectorUrl =
    `${environment.apiUrl}/cartera/analisis/vector-comportamiento/actual`;


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly http: HttpClient
  ) {
  }


  // =========================================================
  // CONTEXTO PRINCIPAL
  // =========================================================

  consultar(
    idDatosPersonal: number,
    idAgencia: number
  ): Observable<OriginacionContexto> {

    const params =
      this.crearParametrosAgencia(
        idAgencia
      );

    return this.http.get<OriginacionContexto>(
      `${this.contextoUrl}/${idDatosPersonal}`,
      {
        params
      }
    );
  }


  // =========================================================
  // CARTERA HISTÓRICA
  // =========================================================

  listarCarteraHistorica(
    idDatosPersonal: number,
    idAgencia: number
  ): Observable<OriginacionCartera[]> {

    const params =
      this.crearParametrosAgencia(
        idAgencia
      );

    return this.http.get<OriginacionCartera[]>(
      `${this.contextoUrl}/${idDatosPersonal}/cartera/historico`,
      {
        params
      }
    );
  }


  // =========================================================
  // CODEUDAS HISTÓRICAS
  // =========================================================

  listarCodeudasHistoricas(
    idDatosPersonal: number,
    idAgencia: number
  ): Observable<OriginacionCodeuda[]> {

    const params =
      this.crearParametrosAgencia(
        idAgencia
      );

    return this.http.get<OriginacionCodeuda[]>(
      `${this.contextoUrl}/${idDatosPersonal}/codeudas/historico`,
      {
        params
      }
    );
  }


  // =========================================================
  // VECTOR DETALLADO
  // =========================================================

  listarVectorDetalle(
    idDatosPersonal: number
  ): Observable<OriginacionVectorDetalle[]> {

    return this.http.get<
      OriginacionVectorDetalle[]
    >(
      `${this.vectorUrl}/persona/${idDatosPersonal}/detalle`
    );
  }


  // =========================================================
  // APOYO
  // =========================================================

  private crearParametrosAgencia(
    idAgencia: number
  ): HttpParams {

    return new HttpParams()
      .set(
        'idAgencia',
        idAgencia.toString()
      );
  }

}
