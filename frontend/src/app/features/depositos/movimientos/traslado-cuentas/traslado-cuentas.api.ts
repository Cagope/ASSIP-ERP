
import {
  Injectable,
  inject
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
  PersonaBusquedaDTO
} from '../../../../shared/personas/personas-busqueda.dto';

import {
  TrasladoEntrada,
  TrasladoCuenta,
  TrasladoPreview,
  TrasladoResultado
} from './traslado-cuentas.models';


// =========================================================
// API - TRASLADO INTEGRAL DE CUENTAS ENTRE AGENCIAS
// =========================================================

@Injectable({
  providedIn: 'root'
})
export class TrasladoCuentasApi {

  // =====================================================
  // DEPENDENCIAS
  // =====================================================

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/traslado-cuentas`;


  // =====================================================
  // BUSCAR ASOCIADOS
  // =====================================================

  buscarAsociados(
    filtros: {
      documento: string;
      nombres: string;
      primerApellido: string;
      segundoApellido: string;
    }
  ): Observable<PersonaBusquedaDTO[]> {

    let params = new HttpParams();

    // ---------------------------------------------------
    // CONSTRUIR PARÁMETROS DE BÚSQUEDA
    // ---------------------------------------------------

    Object.entries(filtros).forEach(
      ([campo, valor]) => {

        const texto = valor.trim();

        if (texto) {

          params = params.set(
            campo,
            texto
          );
        }
      }
    );

    // ---------------------------------------------------
    // CONSULTAR ASOCIADOS
    // ---------------------------------------------------

    return this.http.get<PersonaBusquedaDTO[]>(
      `${this.base}/buscar-asociados`,
      {
        params
      }
    );
  }


  // =====================================================
  // CONSULTAR CUENTAS DEL ASOCIADO
  // =====================================================

  consultarCuentas(
    idDatosPersonal: number
  ): Observable<TrasladoCuenta[]> {

    return this.http.get<TrasladoCuenta[]>(
      `${this.base}/cuentas/${idDatosPersonal}`
    );
  }


  // =====================================================
  // GENERAR VISTA PREVIA DEL TRASLADO
  // =====================================================

  preview(
    entrada: TrasladoEntrada
  ): Observable<TrasladoPreview> {

    return this.http.post<TrasladoPreview>(
      `${this.base}/preview`,
      entrada
    );
  }


  // =====================================================
  // APLICAR TRASLADO INTEGRAL
  // =====================================================

  aplicar(
    entrada: TrasladoEntrada
  ): Observable<TrasladoResultado> {

    return this.http.post<TrasladoResultado>(
      `${this.base}/aplicar`,
      entrada
    );
  }

}
