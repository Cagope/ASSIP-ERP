import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';

import {
  DashboardCarteraFiltro,
  DashboardCarteraResponse
} from './dashboard-cartera.models';

@Injectable({
  providedIn: 'root'
})
export class DashboardCarteraApi {

  private readonly url =
    `${environment.apiUrl}/gerencia/dashboard-cartera`;

  constructor(
    private readonly http: HttpClient
  ) {
  }

  // =========================================================
  // Dashboard completo
  // =========================================================

  consultar(
    filtro: DashboardCarteraFiltro
  ): Observable<DashboardCarteraResponse> {

    return this.http.post<DashboardCarteraResponse>(
      `${this.url}/consultar`,
      this.normalizarFiltro(filtro)
    );
  }

  // =========================================================
  // Normalización del filtro
  // =========================================================

  private normalizarFiltro(
    filtro: DashboardCarteraFiltro
  ): DashboardCarteraFiltro {

    return {
      fechaCorte: filtro.fechaCorte,
      fechaDesde: filtro.fechaDesde,
      fechaHasta: filtro.fechaHasta,

      idAgencia: filtro.idAgencia,
      idLineaCredito: filtro.idLineaCredito,

      edadRiesgo: this.normalizarTexto(
        filtro.edadRiesgo
      ),

      edadMora: this.normalizarTexto(
        filtro.edadMora
      ),

      codigoEstadoCartera: this.normalizarTexto(
        filtro.codigoEstadoCartera
      ),

      codigoEstadoJuridico: this.normalizarTexto(
        filtro.codigoEstadoJuridico
      ),

      codigoClasificacionCredito: this.normalizarTexto(
        filtro.codigoClasificacionCredito
      ),

      codigoGarantiaCredito: this.normalizarTexto(
        filtro.codigoGarantiaCredito
      )
    };
  }

  private normalizarTexto(
    valor: string | null | undefined
  ): string | null {

    const texto = valor?.trim();

    return texto
      ? texto
      : null;
  }

}
