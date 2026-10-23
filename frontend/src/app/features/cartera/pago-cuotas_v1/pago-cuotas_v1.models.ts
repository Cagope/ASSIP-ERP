// =========================================================
// CARTERA - PAGO DE CUOTAS V1
// Contratos de consulta, liquidación y simulación.
// Los importes BigDecimal del backend llegan como number en JSON.
// Las fechas LocalDate llegan como string (yyyy-MM-dd).
// =========================================================

export interface PagoCuotasCreditoResumen {
  idCarteraCredito: number;
  idAgencia: number;
  idLineaCredito: number;
  pagareCartera: string;
  idDatosPersonal: number;
  codigoEstadoCartera: string | null;
  codigoEstadoJuridico: string | null;
  codigoTipoCuota: string | null;
  fechaDesembolso: string | null;
  fechaFinal: string | null;
  proximaFechaCapital: string | null;
  valorDesembolsado: number | null;
  saldoActual: number | null;
  valorCuota: number | null;
  alturaCuota: number | null;
}

export interface PagoCuotasCreditoDetalle {
  idCarteraCredito: number;
  idAgencia: number;
  idLineaCredito: number;
  pagareCartera: string;
  idDatosPersonal: number;
  codigoEstadoCartera: string | null;
  codigoEstadoJuridico: string | null;
  codigoTipoCuota: string | null;
  codigoFormaPago: string | null;
  tipoModalidadInteres: string | null;
  periodoCodigoInteres: string | null;
  amortizacionCapital: number | null;
  plazo: number | null;
  alturaCuota: number | null;
  valorInicialCredito: number | null;
  valorBaseCalculoCuota: number | null;
  valorPrimeraCuota: number | null;
  valorCuota: number | null;
  saldoActual: number | null;
  abonosPendientes: number | null;
  tasaNominalAnual: number | null;
  tasaEfectivaAnual: number | null;
  fechaDesembolso: string | null;
  fechaPrimeraCuota: string | null;
  fechaPrimeraCuotaCapital: string | null;
  fechaPrimeraCuotaInteres: string | null;
  fechaFinal: string | null;
  ultimaFechaCapital: string | null;
  ultimaFechaInteres: string | null;
  ultimaFechaMora: string | null;
  ultimaFechaSeguro: string | null;
  ultimaFechaFondo: string | null;
  proximaFechaCapital: string | null;
  proximaFechaInteres: string | null;
  proximaFechaSeguro: string | null;
  proximaFechaFondo: string | null;
  interesesPagadosHasta: string | null;
  interesesMoraHasta: string | null;

  // Identificación comercial y condiciones especiales del crédito.
  codigoLineaCredito: string | null;
  nombreLineaCredito: string | null;
  codigoClasificacionCredito: string | null;
  descripcionClasificacionCredito: string | null;
  codigoGarantiaCredito: string | null;
  descripcionGarantiaCredito: string | null;
  creditoEvaluado: boolean | null;
  creditoReestructurado: boolean | null;
  codigoModificacionCredito: string | null;
  tieneProrrogas: boolean;
  tieneAlivios: boolean;
}

export interface PagoCuotasDatosPrevios {
  credito: PagoCuotasCreditoDetalle;
  fechaPago: string;
  tasaMoraVigente: number;
}

export interface PagoCuotasMoraCalculada {
  fechaInicioMora: string | null;
  diasMora: number;
  tasaNominalAnual: number;
  baseMora: number;
  interesesMora: number;
}

export interface PagoCuotasLiquidacion {
  idCarteraCredito: number;
  pagareCartera: string;
  fechaPago: string;
  tipoCuota: 'FIJA' | 'VARIABLE';
  cuotasConsideradas: number;
  saldoInicial: number;
  capitalExigible: number;
  interesesCorrientes: number;
  mora: PagoCuotasMoraCalculada;
  segurosPendientes: number;
  saldoAlivios: number;
  cuotaAlivios: number;
  totalLiquidacion: number;
  vencimientoFinal: boolean;
  capitalAnticipadoSugerido: number;
  valorPagoSugerido: number;
}

export interface PagoCuotasSimulacion {
  liquidacion: PagoCuotasLiquidacion;
  valorPago: number;
  pagoMinimo: number;
  pagoMaximo: number;
  capitalAplicado: number;
  interesesCorrientesAplicados: number;
  interesesMoraAplicados: number;
  segurosAplicados: number;
  aliviosAplicados: number;
  diferenciaCapitalLiquidado: number;
  saldoCapitalProyectado: number;
  abonoAdicionalCapital: boolean;
  capitalInferiorAlLiquidado: boolean;
}

export interface PagoCuotasParametrosLiquidacion {
  idCarteraCredito: number;
  fechaPago: string;
  periodoInteresMeses: number;
  cuotasACancelar?: number | null;
}

export interface PagoCuotasParametrosSimulacion
  extends PagoCuotasParametrosLiquidacion {
  valorPago: number;
}

/** Un comprobante consolidado a partir de sus movimientos activos. */
export interface PagoCuotasPagoRealizado {
  fechaPago: string;
  tipoComprobante: string;
  numeroComprobante: string;
  cantidadRegistros: number;

  capital: number;
  interesCausado: number;
  interesIngreso: number;
  interesAnticipado: number;
  mora: number;

  seguros: number;

  aportes: number;
  papeleria: number;
  fondoGarantia: number;
  otrosValores: number;

  totalComponentes: number;
  totalPagado: number;
}

// La búsqueda de asociados y la tabla de amortización reutilizan
// los contratos de sus módulos existentes.
