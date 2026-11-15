import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { CierreMensualCuenta, CierreMensualResumen } from './cierre-mensual.models';

/** Consulta y confirma el cierre mensual mediante los endpoints existentes. */
@Injectable({ providedIn: 'root' })
export class CierreMensualApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/contabilidad/cierres/mensualcontable`;
  private params(idAgencia: number, ano: number, mes: number): HttpParams {
    return new HttpParams().set('idAgencia', idAgencia).set('ano', ano).set('mes', mes);
  }
  consultarResumen(idAgencia: number, ano: number, mes: number): Observable<CierreMensualResumen> {
    return this.http.get<CierreMensualResumen>(`${this.url}/resumen`, { params: this.params(idAgencia, ano, mes) });
  }
  consultarDetalle(idAgencia: number, ano: number, mes: number): Observable<CierreMensualCuenta[]> {
    return this.http.get<CierreMensualCuenta[]>(`${this.url}/detalle`, { params: this.params(idAgencia, ano, mes) });
  }
  /** Reabre el mes cerrado y envía la justificación para su historial. */
  reabrir(idAgencia: number, ano: number, mes: number, justificacion: string): Observable<CierreMensualResumen> {
    return this.http.post<CierreMensualResumen>(`${this.url}/reabrir`, null, {
      params: this.params(idAgencia, ano, mes).set('justificacion', justificacion.trim())
    });
  }
  cerrar(idAgencia: number, ano: number, mes: number): Observable<CierreMensualResumen> {
    return this.http.post<CierreMensualResumen>(`${this.url}/cerrar`, null, { params: this.params(idAgencia, ano, mes) });
  }
}
