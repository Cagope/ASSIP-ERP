import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

/**
 * Registro del Formato SES 9026 - Ahorros.
 *
 * Consolida información histórica de Depósitos y CDAT.
 *
 * Los importes y tasas se reciben como números.
 * Los campos regulatorios pendientes de parametrización
 * pueden llegar como null desde el backend.
 */
export interface Formato9026DTO {

  // Identificación del reporte.
  codigoEntidad: string | null;
  fechaCorte: string;
  consecutivo: number;

  // Identificación del titular.
  tipoIdentificacion: string | null;
  nit: string | null;

  // Clasificación del depósito.
  codigoContable: string | null;
  nombreDeposito: string;
  tipoAhorro: number;
  amortizacion: string | null;

  // Condiciones del depósito.
  fechaApertura: string | null;
  plazo: number;
  fechaVencimiento: string | null;
  modalidad: string | null;

  // Tasas e información financiera.
  tasaInteresNominal: number;
  tasaInteresEfectiva: number;
  interesesCausados: number;
  saldo: number;
  depositoInicial: number;

  // Identificación de la cuenta.
  numeroCuenta: string;

  // GMF y características de la cuenta.
  exentaGmf: string | null;
  fechaAceptacionGmf: string | null;
  estado: string | null;
  ctaBajoMonto: string | null;
  cotitulares: number | null;
  conjuntaColectivo: string | null;
  paso: string | null;

  // Información complementaria para pantalla y Excel.
  // Estos campos no pertenecen al TXT oficial.
  moduloOrigen: string;
  idAgencia: number;
  nombreTitular: string | null;
}

/**
 * API del Formato SES 9026 - Ahorros.
 *
 * Centraliza las solicitudes HTTP del formato.
 * No contiene cálculos financieros ni lógica de exportación.
 */
@Injectable({
  providedIn: 'root'
})
export class Formato9026Api {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/ses/formatos/9026`;

  /**
   * Consulta los registros del Formato 9026.
   *
   * Parámetro:
   * - fechaCorte: fecha en formato yyyy-MM-dd.
   *
   * El backend:
   * - Valida la fecha de corte.
   * - Comprueba los cierres mensuales de Depósitos y CDAT.
   * - Consulta los detalles históricos de ambos módulos.
   * - Excluye Aportes Sociales.
   * - Devuelve un consecutivo único para los registros.
   *
   * Es una operación de solo lectura.
   *
   * @param fechaCorte Fecha de corte del informe.
   * @returns Registros consolidados de Depósitos y CDAT.
   */
  consultar(
    fechaCorte: string
  ): Observable<Formato9026DTO[]> {

    const params = new HttpParams()
      .set('fechaCorte', fechaCorte);

    return this.http.get<Formato9026DTO[]>(
      this.baseUrl,
      { params }
    );
  }
}
