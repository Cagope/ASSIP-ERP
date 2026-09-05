import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import {
  RollForwardCorte,
  RollForwardDetalle,
  RollForwardLinea,
  RollForwardResumen
} from './roll-forward.models';

@Injectable({ providedIn: 'root' })
export class RollForwardApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/cartera/analisis/roll-forward`;

  cortes(): Observable<RollForwardCorte[]> {
    return this.http.get<RollForwardCorte[]>(`${this.baseUrl}/cortes`);
  }

  resumen(fechaCorte: string): Observable<RollForwardResumen> {
    return this.http.get<RollForwardResumen>(
      `${this.baseUrl}/resumen`,
      { params: this.paramsFecha(fechaCorte) }
    );
  }

  historico(): Observable<RollForwardResumen[]> {
    return this.http.get<RollForwardResumen[]>(`${this.baseUrl}/resumen/historico`);
  }

  lineas(fechaCorte: string): Observable<RollForwardLinea[]> {
    return this.http.get<RollForwardLinea[]>(
      `${this.baseUrl}/lineas`,
      { params: this.paramsFecha(fechaCorte) }
    );
  }

  tiposMovimiento(fechaCorte: string): Observable<string[]> {
    return this.http.get<string[]>(
      `${this.baseUrl}/tipos-movimiento`,
      { params: this.paramsFecha(fechaCorte) }
    );
  }

  detalle(
    fechaCorte: string,
    tipoMovimiento?: string | null,
    idLineaCredito?: number | null
  ): Observable<RollForwardDetalle[]> {
    let params = this.paramsFecha(fechaCorte);

    if (tipoMovimiento) {
      params = params.set('tipoMovimiento', tipoMovimiento);
    }

    if (idLineaCredito != null) {
      params = params.set('idLineaCredito', String(idLineaCredito));
    }

    return this.http.get<RollForwardDetalle[]>(
      `${this.baseUrl}/detalle`,
      { params }
    );
  }

  private paramsFecha(fechaCorte: string): HttpParams {
    return new HttpParams().set('fechaCorte', fechaCorte);
  }
}
