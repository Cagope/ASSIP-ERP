import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

export interface DepreciacionRequestDTO {
  idAgencia: number;             // ✅ OBLIGATORIO

  fechaPeriodo: string;          // yyyy-MM-dd
  fechaContabilizacion: string;  // yyyy-MM-dd
  tipoComprobante: string;       // VARCHAR(2)
  numeroComprobante: string;     // VARCHAR(10)
  concepto: string;              // VARCHAR(200)
}

export interface DepreciacionPreviewDTO {
  idActivoFijo: number;
  placaActivo: string;
  nombreActivo: string;

  // ✅ proveedor (tercero contable)
  idDatosPersonalProveedor: number | null;

  valorMensual: number;
  depreciacionAcumulada: number;
  saldoPendiente: number;
  valorPeriodo: number;

  idCuentaGasto: number | null;
  idCuentaDepreciacion: number | null;
}


export interface DepreciacionPreviewResult {
  detalle: DepreciacionPreviewDTO[];
  totalDebito: number;
  totalCredito: number;
}

export interface DepreciacionEjecucionResult {
  activosProcesados: number;
  totalDebito: number;
  totalCredito: number;
}

@Injectable({ providedIn: 'root' })
export class DepreciacionApi {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/activos-fijos/depreciacion`;

  preview(dto: DepreciacionRequestDTO) {
    return this.http.post<DepreciacionPreviewResult>(
      `${this.baseUrl}/preview`,
      dto
    );
  }

  ejecutar(dto: DepreciacionRequestDTO) {
    return this.http.post<DepreciacionEjecucionResult>(
      `${this.baseUrl}/ejecutar`,
      dto
    );
  }
}
