import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpParams
} from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { CajaOperacionPendiente } from './operaciones-pendientes.models';

@Injectable({
  providedIn: 'root'
})
export class OperacionesPendientesApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cajas/operaciones-pendientes`;

  constructor(
    private readonly http: HttpClient
  ) {}


  // =========================================================
  // LISTAR
  // =========================================================

  listarPendientes(
    idCaja: number,
    fechaContable: string
  ): Observable<CajaOperacionPendiente[]> {

    const params = new HttpParams()
      .set(
        'idCaja',
        idCaja.toString()
      )
      .set(
        'fechaContable',
        fechaContable
      );

    return this.http.get<CajaOperacionPendiente[]>(
      this.baseUrl,
      { params }
    );
  }


  // =========================================================
  // CONFIRMAR
  // =========================================================

  confirmar(
    idMovimientoCaja: number,
    idCaja: number
  ): Observable<void> {

    const params = new HttpParams()
      .set(
        'idCaja',
        idCaja.toString()
      );

    return this.http.post<void>(
      `${this.baseUrl}/${idMovimientoCaja}/confirmar`,
      null,
      { params }
    );
  }
}
