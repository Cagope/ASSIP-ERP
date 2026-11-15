import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import {
  CierreDiarioInforme,
  CierreDiarioMensual,
  CierreDiarioRequest,
  CierreDiarioResumen
} from './cierre-diario.models';

/** Consume los endpoints de cierre diario de Contabilidad. */
@Injectable({ providedIn: 'root' })
export class CierreDiarioApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/contabilidad/cierres/diario`;

  /** Consulta todos los días calendario del mes seleccionado. */
  consultarMes(
    idAgencia: number,
    ano: number,
    mes: number
  ): Observable<CierreDiarioMensual> {
    const params = new HttpParams()
      .set('idAgencia', String(idAgencia))
      .set('ano', String(ano))
      .set('mes', String(mes));

    return this.http.get<CierreDiarioMensual>(
      `${this.url}/mes`,
      { params }
    );
  }

  /** Consulta el estado y los movimientos de una fecha contable. */
  consultar(
    idAgencia: number,
    fecha: string
  ): Observable<CierreDiarioResumen> {
    const params = new HttpParams()
      .set('idAgencia', String(idAgencia))
      .set('fecha', fecha);

    return this.http.get<CierreDiarioResumen>(
      this.url,
      { params }
    );
  }

  /** Consulta los datos del informe diario para impresión y Excel. */
  consultarInforme(
    idAgencia: number,
    fecha: string
  ): Observable<CierreDiarioInforme> {
    const params = new HttpParams()
      .set('idAgencia', String(idAgencia))
      .set('fecha', fecha);

    return this.http.get<CierreDiarioInforme>(
      `${this.url}/informe`,
      { params }
    );
  }

  /** Ejecuta el cierre del día seleccionado. */
  cerrar(
    request: CierreDiarioRequest
  ): Observable<CierreDiarioResumen> {
    return this.http.post<CierreDiarioResumen>(
      `${this.url}/cerrar`,
      request
    );
  }

  /** Reabre un día cerrado con justificación obligatoria. */
  reabrir(
    request: CierreDiarioRequest
  ): Observable<CierreDiarioResumen> {
    return this.http.post<CierreDiarioResumen>(
      `${this.url}/reabrir`,
      request
    );
  }
}
