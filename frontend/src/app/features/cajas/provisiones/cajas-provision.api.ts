import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import {
  CajaApertura,
  CajaAperturaRequest,
  CajaDisponible,
  CajaEstado,
  CajaProvisionActiva,
  CajaUsuario,
  CajaUsuarioVincularRequest
} from './cajas-provision.models';


@Injectable({
  providedIn: 'root'
})
export class CajasProvisionApi {

  private readonly vinculacionUrl =
    `${environment.apiUrl}/cajas/vinculacion`;

  private readonly provisionesUrl =
    `${environment.apiUrl}/cajas/provisiones`;


  constructor(
    private readonly http: HttpClient
  ) {}


  // =========================================================
  // VINCULACIÓN - CAJAS DISPONIBLES
  // =========================================================

  listarDisponibles(): Observable<CajaDisponible[]> {

    return this.http.get<CajaDisponible[]>(
      `${this.vinculacionUrl}/disponibles`
    );
  }


  listarDisponiblesCambio(): Observable<CajaDisponible[]> {

    return this.http.get<CajaDisponible[]>(
      `${this.vinculacionUrl}/disponibles-cambio`
    );
  }


  // =========================================================
  // VINCULACIÓN - CAJA ACTIVA DEL USUARIO
  // =========================================================

  obtenerVinculacionActiva(): Observable<CajaUsuario | null> {

    return this.http.get<CajaUsuario | null>(
      `${this.vinculacionUrl}/activa`
    );
  }


  obtenerVinculacionActivaPorCaja(
    idCaja: number
  ): Observable<CajaUsuario> {

    return this.http.get<CajaUsuario>(
      `${this.vinculacionUrl}/caja/${idCaja}`
    );
  }


  // =========================================================
  // VINCULACIÓN - SELECCIONAR CAJA
  // =========================================================

  vincularCaja(
    idCaja: number
  ): Observable<CajaUsuario> {

    const request: CajaUsuarioVincularRequest = {
      idCaja
    };

    return this.http.post<CajaUsuario>(
      this.vinculacionUrl,
      request
    );
  }


  // =========================================================
  // VINCULACIÓN - CORREGIR CAJA
  // =========================================================

  cambiarCaja(
    idCaja: number
  ): Observable<CajaUsuario> {

    const request: CajaUsuarioVincularRequest = {
      idCaja
    };

    return this.http.put<CajaUsuario>(
      `${this.vinculacionUrl}/cambiar`,
      request
    );
  }


  // =========================================================
  // PROVISIÓN - PREPARAR APERTURA
  // =========================================================

  prepararApertura(): Observable<CajaApertura> {

    return this.http.get<CajaApertura>(
      `${this.provisionesUrl}/preparar-apertura`
    );
  }


  // =========================================================
  // PROVISIÓN - ABRIR CAJA
  // =========================================================

  abrirCaja(
    request: CajaAperturaRequest
  ): Observable<number> {

    return this.http.post<number>(
      `${this.provisionesUrl}/abrir`,
      request
    );
  }


  // =========================================================
  // PROVISIÓN ACTIVA DEL USUARIO
  //
  // Este método será compartido por:
  // - Captura de depósitos
  // - Recaudos
  // - Convenios
  // - Movimientos interagencia
  // - Cierre de recaudos/convenios
  // =========================================================

  obtenerProvisionActivaUsuario():
    Observable<CajaProvisionActiva> {

    return this.http.get<CajaProvisionActiva>(
      `${this.provisionesUrl}/activa-usuario`
    );
  }


  // =========================================================
  // ESTADO DE CAJAS
  // =========================================================

  listarEstadoCajas(
    idAgencia: number,
    fechaContable: string
  ): Observable<CajaEstado[]> {

    return this.http.get<CajaEstado[]>(
      `${this.provisionesUrl}/estado-cajas`,
      {
        params: {
          idAgencia: idAgencia.toString(),
          fechaContable
        }
      }
    );
  }
}
