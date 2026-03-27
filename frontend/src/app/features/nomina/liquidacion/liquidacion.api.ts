import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTO REQUEST
   ========================================================= */

export interface LiquidacionRequestDTO {
  idPeriodoNomina?: number; // opcional (backend decide)
  fkAgencia: number;
}

/* =========================================================
   DTO PERÍODO OPERATIVO (VIENE DEL BACKEND)
   ========================================================= */

export interface PeriodoOperativoDTO {
  idPeriodo: number;
  anio: number;
  mes: number;
  numeroPeriodo?: number;
  tipoPeriodo?: string;
}

/* =========================================================
   DTO PREVIEW (estructura backend)
   ========================================================= */

export interface LiquidacionPreviewContratoDTO {

  // 🔑 NUEVO: período operativo resuelto por backend
  periodo: PeriodoOperativoDTO;

  // contrato liquidado
  contrato: any; // EmpleadoContratoDTO (tal cual backend)

  ibc: number;

  totales: {
    totalDevengados: number;
    totalDeducciones: number;
    totalProvisiones: number;
    netoPagar: number;
  };

  detalles: LiquidacionDetalleDTO[];
}

export interface LiquidacionDetalleDTO {

  // clave real del concepto (varchar)
  codigoConcepto: string;

  tipo: 'DEVENGADO' | 'DEDUCCION' | 'PROVISION';
  origen: 'AUTOMATICO' | 'NOVEDAD';

  cantidad: number;
  valorUnitario: number;
  valorTotal: number;
  baseCalculo: number;

  tipoCalculo?: string;
  multiplicador?: number;

  idNovedadNomina?: number;
}

/* =========================================================
   DTO PREVIEW EXCEL (PLANO)
   ========================================================= */

export interface LiquidacionPreviewExcelDTO {

  idEmpleado: number;
  documentoEmpleado?: string;
  nombreEmpleado?: string;

  idContrato: number;
  fechaInicioContrato?: string;
  fechaFinContrato?: string;
  contratoActivo?: boolean;

  idAgencia: number;
  nombreAgencia?: string;

  idSeccion?: number;
  nombreSeccion?: string;

  idCargo?: number;
  nombreCargo?: string;

  idPeriodoNomina: number;
  fechaInicioPeriodo?: string;
  fechaFinPeriodo?: string;

  // concepto por código (varchar)
  codigoConcepto?: string;
  nombreConcepto?: string;

  tipoConcepto: string; // DEVENGADO | DEDUCCION | PROVISION
  origen: string;       // AUTOMATICO | NOVEDAD

  tipoCalculo?: string;
  baseCalculo?: string;
  multiplicador?: number;

  cantidad: number;
  valorUnitario: number;
  baseCalculoValor: number;
  valorTotal: number;

  idNovedadNomina?: number;
  observacionNovedad?: string;

  salarioBase: number;
  ibc: number;

  totalDevengados: number;
  totalDeducciones: number;
  totalProvisiones: number;
  netoPagar: number;

  estadoLiquidacion: string; // PREVIEW
}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class LiquidacionApi {

  private readonly base =
    `${environment.apiUrl}/nomina/liquidacion`;

  private readonly preview =
    `${environment.apiUrl}/nomina/liquidacion/preview`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // 👁️ PREVIEW EN PANTALLA
  // =========================================================
  previewPeriodo(
    dto: LiquidacionRequestDTO
  ): Observable<LiquidacionPreviewContratoDTO[]> {

    return this.http.post<LiquidacionPreviewContratoDTO[]>(
      this.preview,
      dto
    );
  }

  // =========================================================
  // 📤 PREVIEW EXCEL (PLANO)
  // =========================================================
  previewPeriodoExcel(
    dto: LiquidacionRequestDTO
  ): Observable<LiquidacionPreviewExcelDTO[]> {

    return this.http.post<LiquidacionPreviewExcelDTO[]>(
      `${this.preview}/excel`,
      dto
    );
  }

  // =========================================================
  // ▶️ EJECUTAR LIQUIDACIÓN (GUARDA + CIERRA PERÍODO)
  // =========================================================
  ejecutarLiquidacion(
    dto: LiquidacionRequestDTO
  ): Observable<void> {

    return this.http.post<void>(
      `${this.base}/ejecutar`,
      dto
    );
  }
}
