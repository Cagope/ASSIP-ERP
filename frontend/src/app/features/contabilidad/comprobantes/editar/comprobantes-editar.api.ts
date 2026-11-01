import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { ComprobanteEditadoResponse, SolicitudEdicion } from '../consulta/comprobantes-consulta.models';

/** Envía actualizaciones de comprobantes manuales al backend transaccional. */
@Injectable({ providedIn: 'root' })
export class ComprobantesEditarService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/contabilidad/comprobantes`;

  /** Edita sin cambiar agencia, tipo, número ni fecha del comprobante. */
  editar(agencia: number, tipo: string, numero: string, datos: SolicitudEdicion): Observable<ComprobanteEditadoResponse> {
    return this.http.put<ComprobanteEditadoResponse>(
      `${this.base}/${agencia}/${encodeURIComponent(tipo)}/${encodeURIComponent(numero)}`, datos
    );
  }
}
