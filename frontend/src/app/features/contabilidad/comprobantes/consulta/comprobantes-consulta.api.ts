import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { ComprobanteCompleto, FiltrosComprobantes, ResumenComprobante, TipoControl } from './comprobantes-consulta.models';

/** API de consulta de comprobantes manuales, compartida con la edición. */
@Injectable({ providedIn: 'root' })
export class ComprobantesConsultaService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/contabilidad/comprobantes`;

  /** Busca hasta cien comprobantes según filtros y agencias autorizadas en backend. */
  buscar(filtros: FiltrosComprobantes): Observable<ResumenComprobante[]> {
    let params = new HttpParams();
    for (const [clave, valor] of Object.entries(filtros)) {
      if (valor !== undefined && valor !== null && String(valor).trim() !== '') {
        params = params.set(clave, String(valor).trim());
      }
    }
    return this.http.get<ResumenComprobante[]>(`${this.base}/buscar`, { params });
  }

  /** Recupera cabecera y auxiliares activos sin modificar datos. */
  consultar(idAgencia: number, tipo: string, numero: string): Observable<ComprobanteCompleto> {
    return this.http.get<ComprobanteCompleto>(
      `${this.base}/${idAgencia}/${encodeURIComponent(tipo)}/${encodeURIComponent(numero)}`
    );
  }

  /** Consulta la parametrización documental compartida con Crear. */
  tiposControl(): Observable<TipoControl[]> {
    return this.http.get<TipoControl[]>(`${this.base}/tipos-control`);
  }
}
