
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import {
  CajasSalidaChequesCheque,
  CajasSalidaChequesPreview,
  CajasSalidaChequesRequest,
  CajasSalidaChequesResponse
} from './cajas-salida-cheques.models';

export interface CajaVinculadaUsuario {
  idCajaUsuario: number;
  fechaJornada: string;
  estado: string;
  idCaja: number;
  idAgencia: number;
  codigoCaja: string;
  descripcionCaja: string;
  idUsuario: number;
  username: string;
  nombreCompleto: string;
}

@Injectable({
  providedIn: 'root'
})
export class CajasSalidaChequesApi {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/cajas/salida-cheques`;

  // =========================================================
  // CAJA VINCULADA AL USUARIO AUTENTICADO
  // =========================================================

  obtenerCajaVinculada(): Observable<CajaVinculadaUsuario | null> {
    return this.http.get<CajaVinculadaUsuario | null>(
      `${environment.apiUrl}/cajas/vinculacion/activa`
    );
  }

  // =========================================================
  // CHEQUES DISPONIBLES
  // =========================================================

  listarCheques(): Observable<CajasSalidaChequesCheque[]> {
    return this.http.get<CajasSalidaChequesCheque[]>(
      `${this.baseUrl}/disponibles`
    );
  }

  // =========================================================
  // VISTA PREVIA DEL COMPROBANTE CONTABLE RC
  //
  // No registra movimientos ni genera consecutivo definitivo.
  // =========================================================

  previewContable(
    request: CajasSalidaChequesRequest
  ): Observable<CajasSalidaChequesPreview> {
    return this.http.post<CajasSalidaChequesPreview>(
      `${this.baseUrl}/preview-contable`,
      request
    );
  }

  // =========================================================
  // EJECUTAR SALIDA DE CHEQUES
  // =========================================================

  ejecutar(
    request: CajasSalidaChequesRequest
  ): Observable<CajasSalidaChequesResponse> {
    return this.http.post<CajasSalidaChequesResponse>(
      `${this.baseUrl}/ejecutar`,
      request
    );
  }
}
