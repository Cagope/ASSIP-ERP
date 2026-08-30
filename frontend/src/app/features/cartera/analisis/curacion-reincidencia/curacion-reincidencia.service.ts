import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

import {
  ApiListResponse,
  CuracionReincidenciaControl,
  CuracionReincidenciaDetalle,
  CuracionReincidenciaDistribucionCura,
  CuracionReincidenciaEdadEntrada,
  CuracionReincidenciaPeriodo,
  CuracionReincidenciaPrimeraReincidencia,
  CuracionReincidenciaResumen,
  CuracionReincidenciaSegmento,
  IndicadorDetalle
} from './curacion-reincidencia.models';

@Injectable({
  providedIn: 'root'
})
export class CuracionReincidenciaService {

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/analisis/curacion-reincidencia`;

  constructor(private readonly http: HttpClient) {}

  control(): Observable<CuracionReincidenciaControl> {
    return this.http.get<CuracionReincidenciaControl>(`${this.baseUrl}/control`);
  }

  resumen(
    periodoDesde: string,
    periodoHasta: string,
    idAgencia?: number | null,
    idLineaCredito?: number | null,
    edadEntrada?: string | null
  ): Observable<CuracionReincidenciaResumen> {
    return this.http.get<CuracionReincidenciaResumen>(
      `${this.baseUrl}/resumen`,
      { params: this.params(periodoDesde, periodoHasta, idAgencia, idLineaCredito, edadEntrada) }
    );
  }

  periodos(
    periodoDesde: string,
    periodoHasta: string,
    idAgencia?: number | null,
    idLineaCredito?: number | null,
    edadEntrada?: string | null
  ): Observable<ApiListResponse<CuracionReincidenciaPeriodo>> {
    return this.http.get<ApiListResponse<CuracionReincidenciaPeriodo>>(
      `${this.baseUrl}/periodos`,
      { params: this.params(periodoDesde, periodoHasta, idAgencia, idLineaCredito, edadEntrada) }
    );
  }

  agencias(
    periodoDesde: string,
    periodoHasta: string,
    idAgencia?: number | null,
    idLineaCredito?: number | null,
    edadEntrada?: string | null
  ): Observable<ApiListResponse<CuracionReincidenciaSegmento>> {
    return this.http.get<ApiListResponse<CuracionReincidenciaSegmento>>(
      `${this.baseUrl}/agencias`,
      { params: this.params(periodoDesde, periodoHasta, idAgencia, idLineaCredito, edadEntrada) }
    );
  }

  lineas(
    periodoDesde: string,
    periodoHasta: string,
    idAgencia?: number | null,
    idLineaCredito?: number | null,
    edadEntrada?: string | null
  ): Observable<ApiListResponse<CuracionReincidenciaSegmento>> {
    return this.http.get<ApiListResponse<CuracionReincidenciaSegmento>>(
      `${this.baseUrl}/lineas`,
      { params: this.params(periodoDesde, periodoHasta, idAgencia, idLineaCredito, edadEntrada) }
    );
  }

  edadesEntrada(
    periodoDesde: string,
    periodoHasta: string,
    idAgencia?: number | null,
    idLineaCredito?: number | null,
    edadEntrada?: string | null
  ): Observable<ApiListResponse<CuracionReincidenciaEdadEntrada>> {
    return this.http.get<ApiListResponse<CuracionReincidenciaEdadEntrada>>(
      `${this.baseUrl}/edades-entrada`,
      { params: this.params(periodoDesde, periodoHasta, idAgencia, idLineaCredito, edadEntrada) }
    );
  }

  distribucionCura(
    periodoDesde: string,
    periodoHasta: string,
    idAgencia?: number | null,
    idLineaCredito?: number | null,
    edadEntrada?: string | null
  ): Observable<ApiListResponse<CuracionReincidenciaDistribucionCura>> {
    return this.http.get<ApiListResponse<CuracionReincidenciaDistribucionCura>>(
      `${this.baseUrl}/distribucion-cura`,
      { params: this.params(periodoDesde, periodoHasta, idAgencia, idLineaCredito, edadEntrada) }
    );
  }

  primeraReincidencia(
    periodoDesde: string,
    periodoHasta: string,
    idAgencia?: number | null,
    idLineaCredito?: number | null,
    edadEntrada?: string | null
  ): Observable<ApiListResponse<CuracionReincidenciaPrimeraReincidencia>> {
    return this.http.get<ApiListResponse<CuracionReincidenciaPrimeraReincidencia>>(
      `${this.baseUrl}/primera-reincidencia`,
      { params: this.params(periodoDesde, periodoHasta, idAgencia, idLineaCredito, edadEntrada) }
    );
  }

  detalle(
    periodoDesde: string,
    periodoHasta: string,
    indicador: IndicadorDetalle,
    idAgencia?: number | null,
    idLineaCredito?: number | null,
    edadEntrada?: string | null
  ): Observable<ApiListResponse<CuracionReincidenciaDetalle>> {
    let params = this.params(periodoDesde, periodoHasta, idAgencia, idLineaCredito, edadEntrada);
    params = params.set('indicador', indicador);

    return this.http.get<ApiListResponse<CuracionReincidenciaDetalle>>(
      `${this.baseUrl}/detalle`,
      { params }
    );
  }

  private params(
    periodoDesde: string,
    periodoHasta: string,
    idAgencia?: number | null,
    idLineaCredito?: number | null,
    edadEntrada?: string | null
  ): HttpParams {
    let params = new HttpParams()
      .set('periodoDesde', periodoDesde)
      .set('periodoHasta', periodoHasta);

    if (idAgencia != null) {
      params = params.set('idAgencia', String(idAgencia));
    }

    if (idLineaCredito != null) {
      params = params.set('idLineaCredito', String(idLineaCredito));
    }

    if (edadEntrada) {
      params = params.set('edadEntrada', edadEntrada);
    }

    return params;
  }
}
