export interface TablaAmortizacionRequest {
  valorCredito: number;
  tasaColocacion: number;
  plazoMeses: number;
  amortizacionCapitalMeses: number;
  periodoInteresMeses: number;
  tipoModalidadInteres: string;
  codigoTipoCuota: string;
  fechaDesembolso: string;
  fechaPrimeraCuotaCapital: string;
  fechaPrimeraCuotaInteres: string;
  porcentajeSeguroCredito: number;
  porcentajeSeguroEntidad: number;
}

export interface TablaAmortizacionDetalle {
  numeroCuota: number;
  fechaPago: string;
  abonoCapital: number;
  valorIntereses: number;
  valorSeguros: number;
  saldoCredito: number;
  valorCuota: number;
}

export interface TablaAmortizacionResponse {
  valorCredito: number;
  tasaColocacion: number;
  plazoMeses: number;
  amortizacionCapitalMeses: number;
  periodoInteresMeses: number;
  tipoModalidadInteres: string;
  codigoTipoCuota: string;
  fechaDesembolso: string;
  fechaPrimeraCuotaCapital: string;
  fechaPrimeraCuotaInteres: string;
  cantidadCuotas: number;
  valorCuotaProyectada: number;
  totalCapital: number;
  totalIntereses: number;
  totalSeguros: number;
  totalPagado: number;
  detalle: TablaAmortizacionDetalle[];
}
