import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

// ======================================================
// DTO ITEM CDAT
// ======================================================

export interface CdatCancelacionItemDTO {

  idCuentaCdat: number;
  codigoCdat: string;

  idAgencia: number;
  idDatosPersonal: number;

  documento: string;
  nombreCompleto: string;

  estadoCdat: string;

  fechaAperturaCdat: string;
  fechaVencimientoCdat: string;
  fechaUltimaLiquidacion: string;

  plazoMeses: number;
  plazoDias: number;

  valorCapital: number;
  saldoActualCdat: number;

  tasaNominalAnual: number;
  tasaEfectivaAnual: number;

  idCuentaAhorro: number | null;
  retencionFuenteCdat: boolean | null;
  amortizacionDeposito: string | null;

  valorInteresCausado: number;
  valorInteresCorriente: number;
  valorRetencion: number;

  valorDisponible: number;
}

export interface CdatCancelacionFiltroDTO {
  documento: string;
  nombres: string;
  primerApellido: string;
  segundoApellido: string;
  codigoCdat: string;
}

// ======================================================
// DTO ENTRADA
// ======================================================

export interface CdatCancelacionEntradaDTO {

  idCuentaCdat: number;

  idAgencia: number;
  idDatosPersonal: number;

  fechaProceso: string;
  fechaLiquidacion: string;

  tipoComprobante: string;
  numeroComprobante: string;

  valorRenovacion: number;

  mediosPagoEntrada: any;
  mediosPagoSalida: any;

  observacion: string;

  // =========================
  // NUEVO CDAT RENOVACIÓN
  // =========================

  fechaAperturaNuevoCdat: string | null;
  fechaVencimientoNuevoCdat: string | null;

  plazoMesesNuevoCdat: number | null;
  plazoDiasNuevoCdat: number | null;

  tasaNominalAnualNuevoCdat: number | null;
  tasaEfectivaAnualNuevoCdat: number | null;

  amortizacionDepositoNuevoCdat: string | null;
  retencionFuenteNuevoCdat: boolean | null;

  idCuentaAhorroNuevoCdat: number | null;

  observacionNuevoCdat: string | null;
}

// ======================================================
// DTO PREVIEW
// ======================================================

export interface CdatCancelacionPreviewDTO {

  cdat: CdatCancelacionItemDTO;

  valorCapital: number;
  valorInteresCausado: number;
  valorInteresCorriente: number;
  valorRetencion: number;

  valorDisponible: number;
  valorRenovacion: number;
  valorDiferencia: number;

  tipoOperacionDiferencia: string;

  movimientosContables: any[];

  totalDebito: number;
  totalCredito: number;
  diferenciaContable: number;

  cuadrado: boolean;
}

// ======================================================
// API
// ======================================================

@Injectable({
  providedIn: 'root'
})
export class CdatCancelacionApi {

  private baseUrl =
    `${environment.apiUrl}/cdat/cancelacion`;

  private readonly reportingUrl =
    `${environment.apiUrl}/reporting/query`;

  constructor(
    private http: HttpClient
  ) {
  }

  // ======================================================
  // CONSULTAR CDAT
  // ======================================================

  buscar(
    filtro: CdatCancelacionFiltroDTO
  ): Promise<CdatCancelacionItemDTO[]> {

    return firstValueFrom(
      this.http.post<CdatCancelacionItemDTO[]>(
        `${this.baseUrl}/buscar`,
        filtro
      )
    );
  }

  obtenerPorId(
    idCuentaCdat: number
  ): Promise<CdatCancelacionItemDTO> {

    return firstValueFrom(
      this.http.get<CdatCancelacionItemDTO>(
        `${this.baseUrl}/${idCuentaCdat}`
      )
    );
  }

  // ======================================================
  // PREVIEW
  // ======================================================

  preview(
    dto: CdatCancelacionEntradaDTO
  ): Promise<CdatCancelacionPreviewDTO> {

    return firstValueFrom(
      this.http.post<CdatCancelacionPreviewDTO>(
        `${this.baseUrl}/preview`,
        dto
      )
    );
  }

  // ======================================================
  // APLICAR
  // ======================================================

  aplicar(
    dto: CdatCancelacionEntradaDTO
  ): Promise<void> {

    return firstValueFrom(
      this.http.post<void>(
        `${this.baseUrl}/aplicar`,
        dto
      )
    );
  }

  obtenerProximoComprobante(
    idAgencia: number,
    tipoComprobante: string
  ): Promise<any> {

    return firstValueFrom(
      this.http.get<any>(
        `${this.baseUrl}/proximo-comprobante/${idAgencia}/${tipoComprobante}`
      )
    );
  }

  buscarCuentas(req: any): Observable<any> {

    return this.http.post<any>(
      this.reportingUrl,
      req
    );
  }

  obtenerCajasAbiertas(
    idAgencia: number,
    fecha: string
  ): Observable<any[]> {

    return this.http.get<any[]>(
      `${environment.apiUrl}/cajas/provisiones/abiertas`,
      {
        params: {
          idAgencia,
          fecha
        }
      }
    );
  }

  listarAmortizaciones(): Observable<any[]> {

    return this.http.get<any[]>(
      `${environment.apiUrl}/cdat/catalogos/amortizaciones`
    );
  }

  obtenerProximoCodigoCdat(): Observable<string> {

    return this.http.get(
      `${environment.apiUrl}/cdat/cdats/proximo-codigo`,
      {
        responseType: 'text'
      }
    );
  }

}
