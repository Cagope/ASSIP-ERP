import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import {
  IndicadorMoraTemprana,
  MoraTempranaControl,
  MoraTempranaCosecha,
  MoraTempranaDetalle,
  MoraTempranaPrimeraMora,
  MoraTempranaResumen,
  MoraTempranaSegmento
} from './mora-temprana.models';

@Injectable({ providedIn: 'root' })
export class MoraTempranaApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/cartera/analisis/mora-temprana`;

  control(): Observable<MoraTempranaControl> {
    return this.http.get<MoraTempranaControl>(`${this.baseUrl}/control`);
  }

  resumen(cosechaDesde: string, cosechaHasta: string, idAgencia?: number | null, idLineaCredito?: number | null): Observable<MoraTempranaResumen> {
    return this.http.get<MoraTempranaResumen>(`${this.baseUrl}/resumen`, {
      params: this.paramsBase(cosechaDesde, cosechaHasta, idAgencia, idLineaCredito)
    });
  }

  cosechas(cosechaDesde: string, cosechaHasta: string, idAgencia?: number | null, idLineaCredito?: number | null): Observable<MoraTempranaCosecha[]> {
    return this.http.get<MoraTempranaCosecha[]>(`${this.baseUrl}/cosechas`, {
      params: this.paramsBase(cosechaDesde, cosechaHasta, idAgencia, idLineaCredito)
    });
  }

  agencias(cosechaDesde: string, cosechaHasta: string, idAgencia?: number | null, idLineaCredito?: number | null): Observable<MoraTempranaSegmento[]> {
    return this.http.get<MoraTempranaSegmento[]>(`${this.baseUrl}/agencias`, {
      params: this.paramsBase(cosechaDesde, cosechaHasta, idAgencia, idLineaCredito)
    });
  }

  lineas(cosechaDesde: string, cosechaHasta: string, idAgencia?: number | null, idLineaCredito?: number | null): Observable<MoraTempranaSegmento[]> {
    return this.http.get<MoraTempranaSegmento[]>(`${this.baseUrl}/lineas`, {
      params: this.paramsBase(cosechaDesde, cosechaHasta, idAgencia, idLineaCredito)
    });
  }

  primeraMora(cosechaDesde: string, cosechaHasta: string, idAgencia?: number | null, idLineaCredito?: number | null): Observable<MoraTempranaPrimeraMora[]> {
    return this.http.get<MoraTempranaPrimeraMora[]>(`${this.baseUrl}/primera-mora`, {
      params: this.paramsBase(cosechaDesde, cosechaHasta, idAgencia, idLineaCredito)
    });
  }

  detalle(cosechaDesde: string, cosechaHasta: string, indicador: IndicadorMoraTemprana, idAgencia?: number | null, idLineaCredito?: number | null): Observable<MoraTempranaDetalle[]> {
    let params = this.paramsBase(cosechaDesde, cosechaHasta, idAgencia, idLineaCredito);
    params = params.set('indicador', indicador);
    return this.http.get<MoraTempranaDetalle[]>(`${this.baseUrl}/detalle`, { params });
  }

  private paramsBase(cosechaDesde: string, cosechaHasta: string, idAgencia?: number | null, idLineaCredito?: number | null): HttpParams {
    let params = new HttpParams()
      .set('cosechaDesde', cosechaDesde)
      .set('cosechaHasta', cosechaHasta);

    if (idAgencia != null) params = params.set('idAgencia', String(idAgencia));
    if (idLineaCredito != null) params = params.set('idLineaCredito', String(idLineaCredito));
    return params;
  }
}
