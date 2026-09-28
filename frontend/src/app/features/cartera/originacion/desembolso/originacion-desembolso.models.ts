// =========================================================
// ORIGINACIÓN DE CARTERA - DESEMBOLSO
// Modelos del frontend
// =========================================================


// =========================================================
// BANDEJA DE CRÉDITOS PENDIENTES
// =========================================================

export interface CreditoPendienteDesembolso {
  idCarteraCredito: number;
  idSolicitudCredito: number;

  idAgencia: number;
  idLineaCredito: number;
  idDatosPersonal: number;

  pagareCartera: string;

  tipoDocumento: string | null;
  documento: string;
  nombreCompleto: string;

  codigoLineaCredito: string;
  nombreLineaCredito: string;

  valorCredito: number;
  plazo: number;

  fechaInclusionSistema: string | null;
  fechaPrimeraCuota: string | null;
  fechaFinal: string | null;

  codigoEstadoCartera: string;
}


// =========================================================
// PREPARACIÓN DEL DESEMBOLSO
// =========================================================

export interface PreparacionDesembolso {
  idCarteraCredito: number;
  idSolicitudCredito: number;

  idAgencia: number;
  idLineaCredito: number;
  idDatosPersonal: number;
  idCuentaAportes: number | null;

  pagareCartera: string;

  documento: string;
  nombreCompleto: string;

  codigoLineaCredito: string;
  nombreLineaCredito: string;

  valorCredito: number;

  porcentajeFondo: number;
  valorFondo: number;

  porcentajeIva: number;
  ivaFondo: number;

  totalComprobante1: number;
  valorDistribuir: number;

  cantidadCentralSugerida: number;
  valorUnitarioCentral: number;
  valorCentralSugerido: number;

  valorFijoPapeleria: number;
  porcentajePapeleria: number;
  valorPapeleria: number;
  ivaPapeleria: number;

  valorFijoAportes: number;
  porcentajeAportes: number;
  valorAportes: number;

  totalComprobante2: number;
  contrapartidaCaja: number;

  fechaPrimeraCuota: string | null;
  fechaFinal: string | null;
}


// =========================================================
// RECÁLCULO DE LIQUIDACIÓN
// =========================================================

export interface CalcularDesembolsoRequest {
  cantidadCentral: number;
}

export interface LiquidacionDesembolso {
  idCarteraCredito: number;

  valorCredito: number;

  porcentajeFondo: number;
  valorFondo: number;

  porcentajeIva: number;
  ivaFondo: number;

  totalComprobante1: number;
  valorDistribuir: number;

  cantidadCentral: number;
  valorUnitarioCentral: number;
  valorCentral: number;

  valorPapeleria: number;
  ivaPapeleria: number;

  valorAportes: number;

  totalComprobante2: number;
  contrapartidaCaja: number;
}


// =========================================================
// DISTRIBUCIÓN DEL DESEMBOLSO
// =========================================================

export interface DepositoDesembolso {
  idCuentaAhorro: number;
  valor: number;
}

export interface DistribucionDesembolsoRequest {
  idCaja: number;
  valorEfectivo: number;
  depositos: DepositoDesembolso[];
  valorCuentaPorPagar: number;
}


// =========================================================
// RESULTADO DE VALIDACIÓN DE DISTRIBUCIÓN
// =========================================================

export interface ResultadoValidacionDistribucion {
  idCarteraCredito: number;

  valorDistribuir: number;

  idCaja: number;

  valorEfectivo: number;
  totalDepositos: number;
  valorCuentaPorPagar: number;

  totalDistribuido: number;
  diferencia: number;

  cuadra: boolean;
  mensaje: string;
}


// =========================================================
// EJECUCIÓN DEFINITIVA
// =========================================================

export interface EjecutarDesembolsoRequest {
  idCaja: number;

  fechaContable: string;
  fechaDesembolso: string;

  tipoComprobanteDesembolso: string;
  tipoComprobanteCobros: string | null;

  cantidadCentral: number;

  valorEfectivo: number;
  depositos: DepositoDesembolso[];
  valorCuentaPorPagar: number;
}


// =========================================================
// RESULTADO DE LA EJECUCIÓN
// =========================================================

export interface ResultadoEjecucionDesembolso {
  idCarteraCredito: number;
  pagareCartera: string;

  valorCredito: number;
  valorDistribuido: number;
  totalCobros: number;

  tipoComprobanteDesembolso: string;
  numeroComprobanteDesembolso: string;

  tipoComprobanteCobros: string | null;
  numeroComprobanteCobros: string | null;

  fechaContable: string;
  fechaDesembolso: string;

  edadRiesgoInicial: string;

  codigoModificacionCredito: string;
  numeroNovaciones: number;
}
