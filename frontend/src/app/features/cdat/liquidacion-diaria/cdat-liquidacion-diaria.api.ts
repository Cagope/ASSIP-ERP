import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../environments/environment';

// ======================================================
// DTO ENTRADA
// ======================================================

export interface CdatLiquidacionDiariaEntradaDTO {
  idAgencia: number | null;

  fechaLiquidacion: string;
  fechaContable: string;

  tipoComprobante: string;
  numeroComprobante?: string;

  confirmado?: boolean;
}

// ======================================================
// DTO ITEM
// ======================================================

export interface CdatLiquidacionDiariaItemDTO {
  idCuentaCdat: number;

  codigoCdat: string;
  documento: string;
  nombreCompleto: string;

  capitalCdat: number;
  tasaNominalAnual: number;

  interesDiario: number;
  retencion: number;
  interesNeto: number;

  trasladarADepositos: boolean;
  valorTrasladoDepositos: number;

  codigoCuentaAhorro: string;
  codigoFormaAhorro: string;
  nombreFormaAhorro: string;
}

// ======================================================
// DTO PREVIEW
// ======================================================

export interface CdatLiquidacionDiariaPreviewDTO {
  totalCdats: number;

  totalInteres: number;
  totalRetencion: number;
  totalNeto: number;

  totalTrasladadoDepositos: number;
  totalNoTrasladado: number;

  totalDebito: number;
  totalCredito: number;
  diferenciaContable: number;

  cuadrado: boolean;

  items: CdatLiquidacionDiariaItemDTO[];
  movimientosContables: any[];
}

// ======================================================
// API
// ======================================================

@Injectable({
  providedIn: 'root'
})
export class CdatLiquidacionDiariaApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cdat/liquidacion-diaria`;

  constructor(
    private http: HttpClient
  ) {
  }

  // ======================================================
  // PREVIEW
  // ======================================================

  preview(
    dto: CdatLiquidacionDiariaEntradaDTO
  ): Promise<CdatLiquidacionDiariaPreviewDTO> {

    return firstValueFrom(
      this.http.post<CdatLiquidacionDiariaPreviewDTO>(
        `${this.baseUrl}/preview`,
        dto
      )
    );
  }

  // ======================================================
  // APLICAR
  // ======================================================

  aplicar(
    dto: CdatLiquidacionDiariaEntradaDTO
  ): Promise<void> {

    return firstValueFrom(
      this.http.post<void>(
        `${this.baseUrl}/aplicar`,
        dto
      )
    );
  }

  // ======================================================
  // PRÓXIMO COMPROBANTE
  // ======================================================

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

}
