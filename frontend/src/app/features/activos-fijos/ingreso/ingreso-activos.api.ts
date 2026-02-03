import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs - HEADER
   ========================================================= */

export interface IngresoActivoHeaderDTO {

  /** Fecha del comprobante */
  fechaInclusion: string; // yyyy-MM-dd

  /** Tipo de comprobante contable */
  tipoComprobante: string;

  /**
   * Número de comprobante:
   * - Puede venir vacío → backend genera consecutivo
   * - Si viene, solo números (se normaliza a 10)
   */
  numeroComprobante?: string | null;

  /** Detalle del comprobante (obligatorio, max 100) */
  detalle: string;

  /** Proveedor principal */
  idProveedor: number;

  /** Cuenta contable de la factura / contrapartida */
  idCuentaFactura: number;
}

/* =========================================================
   DTOs - ITEM
   ========================================================= */

export interface IngresoActivoItemDTO {

  placaActivo: string;
  nombreActivo: string;

  fechaGarantia?: string | null;

  idFormaDepreciacion: number;
  mesesDepreciacion: number;

  valorHistorico: number;
  valorIva?: number | null;
  valorRetencion?: number | null;

  idEstadoActivo: number;
  idTipoAdquisicion: number;

  idUbicacion: number;
  idBloque?: number | null;

  /** Responsable del activo */
  idResponsable?: number | null;

  /** Proveedor por ítem (opcional, sobrescribe header) */
  idProveedor?: number | null;

  /** Cuentas contables */
  idCuentaActivo: number;
  idCuentaDepreciacion: number;
  idCuentaGasto: number;
  idCuentaControl: number;
  idCuentaIvaActivo?: number | null;
  idCuentaRetencion?: number | null;
}

/* =========================================================
   REQUEST
   ========================================================= */

export interface IngresoActivosRequestDTO {
  header: IngresoActivoHeaderDTO;
  items: IngresoActivoItemDTO[];
}

/* =========================================================
   RESPONSE
   ========================================================= */

export interface IngresoActivosResponseDTO {

  /** En tu modelo contable no hay id_comprobante */
  idComprobante?: number | null;

  tipoComprobante: string;
  numeroComprobante: string;
  fechaInclusion: string;

  cantidadActivos: number;

  totalDebito: number;
  totalCredito: number;

  idsActivosCreados: number[];
}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class IngresoActivosApi {

  /** Backend ya incluye /api/v1 en environment */
  private readonly base = `${environment.apiUrl}/activos-fijos/ingreso`;

  constructor(private http: HttpClient) {}

  /**
   * Ingreso de activos fijos por comprobante contable
   *
   * - Agencia sale del token
   * - Un request = un comprobante
   * - N activos
   */
  ingresar(
    request: IngresoActivosRequestDTO
  ): Observable<IngresoActivosResponseDTO> {

    return this.http.post<IngresoActivosResponseDTO>(
      this.base,
      request
    );
  }
}
