import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import {
  CajaVentanillaEstado,
  CajaVentanillaMovimiento
} from './cajas-ventanilla.models';


@Injectable({
  providedIn: 'root'
})
export class CajasVentanillaApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cajas/ventanilla`;

  constructor(
    private readonly http: HttpClient
  ) {}


  // =========================================================
  // ESTADO ACTUAL DEL USUARIO
  // =========================================================

  obtenerEstadoActual(): Observable<CajaVentanillaEstado> {

    return this.http.get<CajaVentanillaEstado>(
      `${this.baseUrl}/actual`
    );
  }


  // =========================================================
  // TRASLADO CAJA ↔ VENTANILLA
  // =========================================================

  trasladar(
    movimiento: CajaVentanillaMovimiento
  ): Observable<CajaVentanillaEstado> {

    return this.http.post<CajaVentanillaEstado>(
      `${this.baseUrl}/trasladar`,
      movimiento
    );
  }
}
