import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import {
  CancelacionPrepagoControl,
  CancelacionPrepagoCredito,
  CancelacionPrepagoDetalle,
  CancelacionPrepagoLinea,
  CancelacionPrepagoResumen
} from './cancelacion-prepago.models';

@Injectable({ providedIn: 'root' })
export class CancelacionPrepagoService {
  private readonly baseUrl = `${environment.apiUrl}/cartera/analisis/cancelacion-prepago`;

  constructor(private readonly http: HttpClient) {}

  control(): Observable<CancelacionPrepagoControl> {
    return this.http.get<CancelacionPrepagoControl>(`${this.baseUrl}/control`);
  }

  listarCortes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/cortes`);
  }

  obtenerResumen(fechaCorte: string): Observable<CancelacionPrepagoResumen> {
    return this.http.get<CancelacionPrepagoResumen>(`${this.baseUrl}/resumen`, {
      params: new HttpParams().set('fechaCorte', fechaCorte)
    });
  }

  listarLineas(fechaCorte: string): Observable<CancelacionPrepagoLinea[]> {
    return this.http.get<CancelacionPrepagoLinea[]>(`${this.baseUrl}/lineas`, {
      params: new HttpParams().set('fechaCorte', fechaCorte)
    });
  }

  listarCreditos(fechaCorte: string, clasificacion?: string): Observable<CancelacionPrepagoDetalle[]> {
    let params = new HttpParams().set('fechaCorte', fechaCorte);
    if (clasificacion?.trim()) params = params.set('clasificacion', clasificacion.trim());
    return this.http.get<CancelacionPrepagoDetalle[]>(`${this.baseUrl}/creditos`, { params });
  }

  obtenerCredito(idCarteraCredito: number): Observable<CancelacionPrepagoCredito> {
    return this.http.get<CancelacionPrepagoCredito>(`${this.baseUrl}/creditos/${idCarteraCredito}`);
  }

  listarDetalle(fechaCorte: string, idLineaCredito?: number | null): Observable<CancelacionPrepagoDetalle[]> {
    let params = new HttpParams().set('fechaCorte', fechaCorte);
    if (idLineaCredito != null) params = params.set('idLineaCredito', idLineaCredito);
    return this.http.get<CancelacionPrepagoDetalle[]>(`${this.baseUrl}/detalle`, { params });
  }

  listarHistoriaCredito(idCarteraCredito: number): Observable<CancelacionPrepagoDetalle[]> {
    return this.http.get<CancelacionPrepagoDetalle[]>(`${this.baseUrl}/detalle/credito/${idCarteraCredito}`);
  }
}
