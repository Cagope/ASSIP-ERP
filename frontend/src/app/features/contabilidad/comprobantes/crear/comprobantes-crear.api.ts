
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';

import {
  ComprobanteCrearRequest,
  ComprobanteCrearResponse,
  ComprobanteTipoControlOpcion
} from './comprobantes-crear.models';

/**
 * Servicio HTTP para la creación manual de comprobantes contables.
 *
 * Centraliza el registro del comprobante y la consulta
 * de los tipos de control documental.
 */
@Injectable({
  providedIn: 'root'
})
export class ComprobantesCrearService {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/contabilidad/comprobantes`;

  /**
   * Registra un comprobante manual.
   *
   * El backend valida permisos, agencia, período contable,
   * movimientos, controles documentales y cuadre.
   */
  crear(
    solicitud: ComprobanteCrearRequest
  ): Observable<ComprobanteCrearResponse> {

    return this.http.post<ComprobanteCrearResponse>(
      this.baseUrl,
      solicitud
    );
  }

  /**
   * Consulta los tipos de control documental configurados
   * en PostgreSQL, incluyendo la obligatoriedad del documento.
   */
  listarTiposControl(): Observable<ComprobanteTipoControlOpcion[]> {

    return this.http.get<ComprobanteTipoControlOpcion[]>(
      `${this.baseUrl}/tipos-control`
    );
  }
}
