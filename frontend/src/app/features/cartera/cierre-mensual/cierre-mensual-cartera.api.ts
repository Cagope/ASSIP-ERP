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

  // =======================================================
  // ESTADO GENERAL DEL CIERRE
  // =======================================================

  estadoCierre: string;

  // =======================================================
  // VALORES DEL CIERRE
  // =======================================================

  saldoCarteraMaestro: number;

  saldoCarteraContable: number;

  diferenciaCuadre: number;

  cantidadCreditos: number;

  // =======================================================
  // FECHAS GENERALES
  // =======================================================

  fechaInicio: string | null;

  fechaCuadre: string | null;

  fechaFotografia: string | null;

  fechaFinalizacion: string | null;

  // =======================================================
  // FOTOGRAFÍA
  // =======================================================

  estadoFotografia: string;

  fechaFotografiaFirme: string | null;

  // =======================================================
  // CÁLCULOS
  // =======================================================

  estadoCalculos: string;

  fechaCalculosInicio: string | null;

  fechaCalculosFirme: string | null;

  // =======================================================
  // ANEXO 1
  // =======================================================

  estadoAnexo1: string;

  fechaAnexo1Inicio: string | null;

  fechaAnexo1Firme: string | null;

  // =======================================================
  // ANEXO 2
  // =======================================================

  estadoAnexo2: string;

  fechaAnexo2Inicio: string | null;

  fechaAnexo2Firme: string | null;

  // =======================================================
  // OTROS
  // =======================================================

  observaciones: string | null;

  fkSeguridadCreacion: number;

  fechaCreacion: string | null;

  fkSeguridadEdicion: number | null;

  fechaEdicion: string | null;
}


// =========================================================
// API
// =========================================================

@Injectable({
  providedIn: 'root'
})
export class CierreMensualCarteraApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/cierre-mensual`;

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
  // Permitido mientras la fotografía no esté en firme.
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
  // CERRAR FOTOGRAFÍA EN FIRME
  // =========================================================

  cerrarFotografia(
    idCierreCartera: number
  ): Observable<CierreMensualCartera> {

    return this.http.post<CierreMensualCartera>(
      `${this.baseUrl}/${idCierreCartera}/cerrar-fotografia`,
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
