import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import {
  CriterioConcentracion,
  DimensionRiesgo,
  RiesgoDeterioroConcentracion,
  RiesgoDeterioroDetalle,
  RiesgoDeterioroEdad,
  RiesgoDeterioroEvolucion,
  RiesgoDeterioroResumen,
  RiesgoDeterioroSegmento,
  TipoEdadRiesgo
} from './riesgo-deterioro.models';

@Injectable({ providedIn: 'root' })
export class RiesgoDeterioroApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/cartera/analisis/riesgo-deterioro`;

  listarCortes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/cortes`);
  }

  resumen(fechaCorte: string, idAgencia?: number | null, idLineaCredito?: number | null): Observable<RiesgoDeterioroResumen> {
    return this.http.get<RiesgoDeterioroResumen>(`${this.baseUrl}/resumen`, {
      params: this.paramsBase(fechaCorte, idAgencia, idLineaCredito)
    });
  }

  edades(fechaCorte: string, tipoEdad: TipoEdadRiesgo, idAgencia?: number | null, idLineaCredito?: number | null): Observable<RiesgoDeterioroEdad[]> {
    let params = this.paramsBase(fechaCorte, idAgencia, idLineaCredito);
    params = params.set('tipoEdad', tipoEdad);
    return this.http.get<RiesgoDeterioroEdad[]>(`${this.baseUrl}/edades`, { params });
  }

  segmentacion(fechaCorte: string, dimension: DimensionRiesgo, idAgencia?: number | null, idLineaCredito?: number | null): Observable<RiesgoDeterioroSegmento[]> {
    let params = this.paramsBase(fechaCorte, idAgencia, idLineaCredito);
    params = params.set('dimension', dimension);
    return this.http.get<RiesgoDeterioroSegmento[]>(`${this.baseUrl}/segmentacion`, { params });
  }

  evolucion(fechaDesde: string, fechaHasta: string, idAgencia?: number | null, idLineaCredito?: number | null): Observable<RiesgoDeterioroEvolucion[]> {
    let params = new HttpParams()
      .set('fechaDesde', fechaDesde)
      .set('fechaHasta', fechaHasta);

    if (idAgencia != null) params = params.set('idAgencia', String(idAgencia));
    if (idLineaCredito != null) params = params.set('idLineaCredito', String(idLineaCredito));

    return this.http.get<RiesgoDeterioroEvolucion[]>(`${this.baseUrl}/evolucion`, { params });
  }

  concentracion(fechaCorte: string, criterio: CriterioConcentracion, limite = 20, idAgencia?: number | null, idLineaCredito?: number | null): Observable<RiesgoDeterioroConcentracion[]> {
    let params = this.paramsBase(fechaCorte, idAgencia, idLineaCredito)
      .set('criterio', criterio)
      .set('limite', String(limite));

    return this.http.get<RiesgoDeterioroConcentracion[]>(`${this.baseUrl}/concentracion`, { params });
  }

  detalle(fechaCorte: string, idAgencia?: number | null, idLineaCredito?: number | null): Observable<RiesgoDeterioroDetalle[]> {
    return this.http.get<RiesgoDeterioroDetalle[]>(`${this.baseUrl}/detalle`, {
      params: this.paramsBase(fechaCorte, idAgencia, idLineaCredito)
    });
  }

  private paramsBase(fechaCorte: string, idAgencia?: number | null, idLineaCredito?: number | null): HttpParams {
    let params = new HttpParams().set('fechaCorte', fechaCorte);
    if (idAgencia != null) params = params.set('idAgencia', String(idAgencia));
    if (idLineaCredito != null) params = params.set('idLineaCredito', String(idLineaCredito));
    return params;
  }
}
