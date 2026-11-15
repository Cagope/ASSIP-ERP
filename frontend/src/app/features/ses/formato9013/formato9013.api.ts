
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

/**
 * Registro del Formato 9013 - Aportes Sociales.
 *
 * Los valores monetarios se reciben como números.
 * Los campos pendientes de parametrización pueden
 * llegar como null desde el backend.
 */
export interface Formato9013DTO {

  // Identificación del reporte
  codigoEntidad: string;
  fechaCorte: string;
  consecutivo: number;

  // Identificación del asociado
  tipoIdentificacion: string;
  numeroIdentificacion: string;
  primerApellido: string | null;
  segundoApellido: string | null;
  nombres: string | null;

  // Información financiera
  saldoAportes: number;
  valorAporteMensual: number | null;
  aportesOrdinarios: number | null;
  aportesExtraordinarios: number | null;
  valorRevalorizacion: number | null;
  montoPromedio: number | null;
  ultimaFecha: string | null;
  contribuciones: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class Formato9013Api {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/ses/formatos/9013`;

  /**
   * Consulta los aportes sociales a la fecha de corte.
   *
   * El backend valida que exista un cierre mensual
   * definitivo de Depósitos para el período solicitado.
   *
   * No modifica información financiera.
   */
  consultar(
    fechaCorte: string
  ): Observable<Formato9013DTO[]> {

    const params = new HttpParams()
      .set('fechaCorte', fechaCorte);

    return this.http.get<Formato9013DTO[]>(
      this.baseUrl,
      { params }
    );
  }
}
