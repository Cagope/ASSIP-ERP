import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import {
  ComprobanteImportacionDatos,
  ComprobanteImportacionResponse,
  ComprobanteImportacionTipo
} from './comprobantes-importar.models';

/** Centraliza las solicitudes de validación y confirmación del Excel. */
@Injectable({ providedIn: 'root' })
export class ComprobantesImportarService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/contabilidad/comprobantes/importar`;

  /** Consulta los tipos activos disponibles para la agencia. */
  listarTipos(idAgencia: number): Observable<ComprobanteImportacionTipo[]> {
    return this.http.get<ComprobanteImportacionTipo[]>(
      `${environment.apiUrl}/contabilidad/tipos-comprobantes/por-agencia`,
      { params: new HttpParams().set('idAgencia', idAgencia) }
    );
  }

  /** Envía el Excel para validar sin contabilizar. */
  validar(idAgencia: number, archivo: File): Observable<ComprobanteImportacionResponse> {
    const datos = new FormData();
    datos.append('idAgencia', String(idAgencia));
    datos.append('archivo', archivo, archivo.name);
    return this.http.post<ComprobanteImportacionResponse>(`${this.baseUrl}/validar`, datos);
  }

  /** Reenvía el Excel original y los datos generales para contabilizar. */
  confirmar(solicitud: ComprobanteImportacionDatos): Observable<string> {
    const datos = new FormData();
    datos.append('idAgencia', String(solicitud.idAgencia));
    datos.append('tipoComprobante', solicitud.tipoComprobante);
    datos.append('fechaContable', solicitud.fechaContable);
    datos.append('conceptoComprobante', solicitud.conceptoComprobante.trim());
    datos.append('archivo', solicitud.archivo, solicitud.archivo.name);
    return this.http.post(`${this.baseUrl}/confirmar`, datos, { responseType: 'text' });
  }
}
