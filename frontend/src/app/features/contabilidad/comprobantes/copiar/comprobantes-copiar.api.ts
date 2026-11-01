import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { ComprobanteCopiarRequest, ComprobanteCopiadoResponse } from './comprobantes-copiar.models';
/** Envía al backend la copia transaccional del comprobante. */
@Injectable({ providedIn: 'root' })
export class ComprobantesCopiarService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/contabilidad/comprobantes/copiar`;
  /** Registra un nuevo comprobante sin alterar el origen. */
  copiar(solicitud: ComprobanteCopiarRequest): Observable<ComprobanteCopiadoResponse> {
    return this.http.post<ComprobanteCopiadoResponse>(this.url, solicitud);
  }
}
