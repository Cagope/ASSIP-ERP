
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { PersonaBusquedaDTO } from '../../../../shared/personas/personas-busqueda.dto';
import { environment } from '../../../../../environments/environment';

import {
  RetiroAsociadoCuenta,
  RetiroAsociadoEntrada,
  RetiroAsociadoPreview
} from './retiro-asociado.models';

export interface CajaAbiertaRetiro {
  idCaja: number;
  codigoCaja: string;
  descripcionCaja: string;
}

/**
 * Alias de compatibilidad para los componentes
 * que utilizan la vista previa contable.
 *
 * Los comprobantes y movimientos están definidos
 * en RetiroAsociadoPreview.
 */
export type RetiroPreviewContable = RetiroAsociadoPreview;

@Injectable({ providedIn: 'root' })
export class RetiroAsociadoApi {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/depositos/retiro-asociado`;

  buscarAsociados(filtros: {
    documento: string;
    nombres: string;
    primerApellido: string;
    segundoApellido: string;
  }): Observable<PersonaBusquedaDTO[]> {

    let params = new HttpParams();

    for (const [clave, valor] of Object.entries(filtros)) {
      if (valor.trim()) {
        params = params.set(clave, valor.trim());
      }
    }

    return this.http.get<PersonaBusquedaDTO[]>(
      `${this.baseUrl}/buscar-asociados`,
      { params }
    );
  }

  consultarCuentas(
    idDatosPersonal: number
  ): Observable<RetiroAsociadoCuenta[]> {

    return this.http.get<RetiroAsociadoCuenta[]>(
      `${this.baseUrl}/cuentas/${idDatosPersonal}`
    );
  }

  preview(
    entrada: RetiroAsociadoEntrada
  ): Observable<RetiroPreviewContable> {

    return this.http.post<RetiroPreviewContable>(
      `${this.baseUrl}/preview`,
      entrada
    );
  }

  aplicar(
    entrada: RetiroAsociadoEntrada
  ): Observable<void> {

    return this.http.post<void>(
      `${this.baseUrl}/aplicar`,
      entrada
    );
  }

  obtenerCajasAbiertas(
    idAgencia: number,
    fecha: string
  ): Observable<CajaAbiertaRetiro[]> {

    return this.http.get<CajaAbiertaRetiro[]>(
      `${environment.apiUrl}/cajas/provisiones/abiertas`,
      {
        params: {
          idAgencia,
          fecha
        }
      }
    );
  }
}
