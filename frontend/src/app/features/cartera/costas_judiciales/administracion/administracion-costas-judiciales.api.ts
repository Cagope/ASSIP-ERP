
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';

import {
  CostasJudicialesRequest,
  CostasJudicialesVistaPrevia,
  CostasJudicialesResultado
} from './administracion-costas-judiciales.models';

/**
 * Comunicación con el backend de Costas Judiciales.
 * El backend valida agencia, permisos, parámetros y contabilización.
 */
@Injectable({
  providedIn: 'root'
})
export class CostasJudicialesApi {
  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/costas-judiciales`;

  /** Obtiene el comprobante propuesto sin registrar movimientos. */
  previsualizar(
    solicitud: CostasJudicialesRequest
  ): Observable<CostasJudicialesVistaPrevia> {
    return this.http.post<CostasJudicialesVistaPrevia>(
      `${this.baseUrl}/comprobante/preview`,
      solicitud
    );
  }

  /** Confirma el cargo y registra el comprobante contable. */
  confirmar(
    solicitud: CostasJudicialesRequest
  ): Observable<CostasJudicialesResultado> {
    return this.http.post<CostasJudicialesResultado>(
      `${this.baseUrl}/confirmar`,
      solicitud
    );
  }
}
