// =========================================================
// Filtros enviados al backend
// =========================================================

export interface DashboardCarteraFiltro {
  fechaCorte: string;
  fechaDesde: string;
  fechaHasta: string;

  idAgencia: number | null;
  idLineaCredito: number | null;

  edadRiesgo: string | null;
  edadMora: string | null;

  codigoEstadoCartera: string | null;
  codigoEstadoJuridico: string | null;
  codigoClasificacionCredito: string | null;
  codigoGarantiaCredito: string | null;
}

// =========================================================
// Respuesta completa del dashboard
// =========================================================

export interface DashboardCarteraResponse {
  fechaCorte: string;
  fechaDesde: string;
  fechaHasta: string;

  resumen: DashboardCarteraResumen;
  riesgos: DashboardCarteraDistribucion[];
  moras: DashboardCarteraDistribucion[];
  lineas: DashboardCarteraLinea[];
  agencias: DashboardCarteraAgencia[];
  recaudos: DashboardCarteraRecaudo[];
  alertas: DashboardCarteraAlerta[];
}

// =========================================================
// Resumen ejecutivo
// =========================================================

export interface DashboardCarteraResumen {
  saldoCartera: number;
  saldoNetoPendiente: number;
  saldoCreditosMora: number;
  indiceMora: number;

  cantidadCreditosConSaldo: number;
  cantidadAsociados: number;
  cantidadCreditosMora: number;
  cantidadCreditosCriticos: number;

  recaudoPeriodo: number;
  capitalRecaudado: number;
  interesesRecaudados: number;
  interesesMoraRecaudados: number;

  cantidadDeteriorados: number;
  cantidadMejorados: number;
  cantidadReestructurados: number;
  cantidadJuridicos: number;
}

// =========================================================
// Distribución por riesgo y mora
// =========================================================

export interface DashboardCarteraDistribucion {
  codigo: string;
  descripcion: string;
  orden: number;

  cantidadCreditos: number;
  cantidadAsociados: number;

  saldoCartera: number;
  saldoCreditosMora: number;
  porcentajeParticipacion: number;
}

// =========================================================
// Distribución por línea de crédito
// =========================================================

export interface DashboardCarteraLinea {
  idLineaCredito: number;
  codigoLineaCredito: string;
  nombreLineaCredito: string;

  cantidadCreditos: number;
  cantidadAsociados: number;

  valorDesembolsado: number;
  saldoCartera: number;
  saldoNetoPendiente: number;
  saldoCreditosMora: number;

  indiceMora: number;
  porcentajeParticipacion: number;
}

// =========================================================
// Distribución por agencia
// =========================================================

export interface DashboardCarteraAgencia {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;

  cantidadCreditos: number;
  cantidadAsociados: number;

  valorDesembolsado: number;
  saldoCartera: number;
  saldoNetoPendiente: number;
  saldoCreditosMora: number;
  recaudoPeriodo: number;

  indiceMora: number;
  porcentajeParticipacion: number;
}

// =========================================================
// Composición del recaudo
// =========================================================

export interface DashboardCarteraRecaudo {
  codigo: string;
  descripcion: string;
  orden: number;

  cantidadMovimientos: number;
  valorRecaudado: number;
  porcentajeParticipacion: number;
}

// =========================================================
// Alertas gerenciales
// =========================================================

export type DashboardCarteraNivelAlerta =
  | 'CRITICO'
  | 'ADVERTENCIA'
  | 'INFORMACION';

export interface DashboardCarteraAlerta {
  codigo: string;
  descripcion: string;
  nivel: DashboardCarteraNivelAlerta;
  orden: number;

  cantidadCreditos: number;
  cantidadAsociados: number;

  saldoCartera: number;
  saldoCreditosMora: number;
}

// =========================================================
// Modelos auxiliares para gráficos
// =========================================================

export interface DashboardCarteraGraficoItem {
  codigo: string;
  etiqueta: string;
  valor: number;
  porcentaje?: number;
}

export interface DashboardCarteraSerieItem {
  codigo: string;
  etiqueta: string;
  saldoCartera: number;
  saldoCreditosMora: number;
  indiceMora: number;
}

// =========================================================
// Valores iniciales
// =========================================================

export function crearDashboardCarteraFiltroInicial(
  fechaActual: Date = new Date()
): DashboardCarteraFiltro {
  const fechaCorte = formatearFecha(fechaActual);

  const primerDiaMes = new Date(
    fechaActual.getFullYear(),
    fechaActual.getMonth(),
    1
  );

  return {
    fechaCorte,
    fechaDesde: formatearFecha(primerDiaMes),
    fechaHasta: fechaCorte,

    idAgencia: null,
    idLineaCredito: null,

    edadRiesgo: null,
    edadMora: null,

    codigoEstadoCartera: null,
    codigoEstadoJuridico: null,
    codigoClasificacionCredito: null,
    codigoGarantiaCredito: null
  };
}

export function crearDashboardCarteraResumenVacio():
  DashboardCarteraResumen {
  return {
    saldoCartera: 0,
    saldoNetoPendiente: 0,
    saldoCreditosMora: 0,
    indiceMora: 0,

    cantidadCreditosConSaldo: 0,
    cantidadAsociados: 0,
    cantidadCreditosMora: 0,
    cantidadCreditosCriticos: 0,

    recaudoPeriodo: 0,
    capitalRecaudado: 0,
    interesesRecaudados: 0,
    interesesMoraRecaudados: 0,

    cantidadDeteriorados: 0,
    cantidadMejorados: 0,
    cantidadReestructurados: 0,
    cantidadJuridicos: 0
  };
}

export function crearDashboardCarteraResponseVacio():
  DashboardCarteraResponse {
  const filtro = crearDashboardCarteraFiltroInicial();

  return {
    fechaCorte: filtro.fechaCorte,
    fechaDesde: filtro.fechaDesde,
    fechaHasta: filtro.fechaHasta,

    resumen: crearDashboardCarteraResumenVacio(),
    riesgos: [],
    moras: [],
    lineas: [],
    agencias: [],
    recaudos: [],
    alertas: []
  };
}

// =========================================================
// Utilidades internas
// =========================================================

function formatearFecha(fecha: Date): string {
  const anio = fecha.getFullYear();
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');

  return `${anio}-${mes}-${dia}`;
}
