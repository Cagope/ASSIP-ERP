import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface LiquidacionV2RequestDTO {
  fkAgencia: number | null;
  idPeriodoNomina: number | null;
}

export interface LiquidacionV2PeriodoDisponibleDTO {
  idPeriodo: number;
  anio: number;
  mes: number;
  numeroPeriodo: number;
  tipoPeriodo: string;
}

export interface LiquidacionV2DetalleDTO {
  tipo: string;
  codigoConcepto: string;
  nombreConcepto?: string | null;
  cantidad: number;
  valorUnitario: number;
  valorTotal: number;
  baseCalculo: number;
  origen?: string | null;
  idNovedad?: number | null;
}

export interface LiquidacionV2PreviewItemDTO {
  idContrato: number;
  idEmpleado: number;
  documentoEmpleado?: string | null;
  nombreEmpleado?: string | null;
  salarioBase: number;
  diasLaborados: number;
  ibc: number;
  totalDevengados: number;
  totalDeducciones: number;
  totalProvisiones: number;
  netoPagar: number;
  detalle: LiquidacionV2DetalleDTO[];
}

export interface LiquidacionV2PreviewResponseDTO {
  idPeriodoNomina: number;
  fkAgencia: number;
  totalContratos: number;
  totalDevengados: number;
  totalDeducciones: number;
  totalProvisiones: number;
  totalNetoPagar: number;
  items: LiquidacionV2PreviewItemDTO[];
}

@Injectable({ providedIn: 'root' })
export class LiquidacionV2Api {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/nomina/liquidacion-v2`;

  primerPeriodoDisponible(idAgencia: number): Observable<LiquidacionV2PeriodoDisponibleDTO | null> {
    return this.http.get<LiquidacionV2PeriodoDisponibleDTO | null>(
      `${this.baseUrl}/periodo-disponible/${idAgencia}`
    );
  }

  preview(body: LiquidacionV2RequestDTO): Observable<LiquidacionV2PreviewResponseDTO> {
    return this.http.post<LiquidacionV2PreviewResponseDTO>(
      `${this.baseUrl}/preview`,
      body
    );
  }

  ejecutar(body: LiquidacionV2RequestDTO): Observable<void> {
    return this.http.post<void>(
      this.baseUrl,
      body
    );
  }
}
