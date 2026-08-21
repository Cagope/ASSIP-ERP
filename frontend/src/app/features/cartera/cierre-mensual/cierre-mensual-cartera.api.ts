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
} from '../../../../environments/environment';


// =========================================================
// DTO / MODELO DEL CIERRE MENSUAL
// =========================================================

export interface CierreMensualCartera {

  idCierreCartera: number;

  fechaCorte: string;

  estadoCierre: string;

  saldoCarteraMaestro: number;

  saldoCarteraContable: number;

  diferenciaCuadre: number;

  cantidadCreditos: number;

  fechaInicio: string | null;

  fechaCuadre: string | null;

  fechaFotografia: string | null;

  fechaFinal: string | null;

  observaciones: string | null;

  fkSeguridadCreacion: number;

  fechaCreacion: string | null;

  fkSeguridadEdicion: number;

  fechaEdicion: string | null;
}


// =========================================================
// API
// =========================================================

@Injectable({
  providedIn: 'root'
})
export class CierreMensualCarteraApi {

  // =========================================================
  // URL BASE
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/cierre-mensual`;


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly http: HttpClient
  ) {}


  // =========================================================
  // LISTAR CIERRES
  // =========================================================

  listar(): Observable<CierreMensualCartera[]> {

    return this.http.get<CierreMensualCartera[]>(
      this.baseUrl
    );

  }


  // =========================================================
  // CONSULTAR CIERRE POR ID
  // =========================================================

  buscarPorId(
    idCierreCartera: number
  ): Observable<CierreMensualCartera> {

    return this.http.get<CierreMensualCartera>(
      `${this.baseUrl}/${idCierreCartera}`
    );

  }


  // =========================================================
  // EJECUTAR CIERRE / GENERAR FOTO
  //
  // El cierre es GENERAL.
  // No se envía agencia.
  // =========================================================

  ejecutar(
    fechaCorte: string
  ): Observable<CierreMensualCartera> {

    const params =
      new HttpParams()
        .set(
          'fechaCorte',
          fechaCorte
        );

    return this.http.post<CierreMensualCartera>(
      `${this.baseUrl}/ejecutar`,
      null,
      {
        params
      }
    );

  }


  // =========================================================
  // REGENERAR FOTO
  //
  // Solo aplica para cierres en estado P.
  //
  // Conserva:
  // - id_cierre_cartera
  // - fecha de corte
  //
  // Regenera:
  // - foto de créditos
  // - base de cálculos
  // - cantidad de créditos
  // - saldo maestro
  // - fecha de fotografía
  // =========================================================

  regenerar(
    idCierreCartera: number
  ): Observable<CierreMensualCartera> {

    return this.http.post<CierreMensualCartera>(
      `${this.baseUrl}/${idCierreCartera}/regenerar`,
      null
    );

  }


  // =========================================================
  // CONSULTAR SI EL CIERRE YA TIENE FOTO
  // =========================================================

  existeFoto(
    idCierreCartera: number
  ): Observable<boolean> {

    return this.http.get<boolean>(
      `${this.baseUrl}/${idCierreCartera}/foto/existe`
    );

  }

}
