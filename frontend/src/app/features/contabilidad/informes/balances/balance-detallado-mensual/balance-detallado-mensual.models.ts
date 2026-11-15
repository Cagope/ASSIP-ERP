
/** Contratos del Balance 1, correspondientes a los DTO del backend. */
export interface BalanceDetalladoMensualFiltro {
  idAgencia: number;
  ano: number;
  mes: number;
  nivel: number;
  soloBalance: boolean;
  compararAnoAnterior: boolean;
  incluirFirmas: boolean;
}

export interface BalanceFila {
  codigoCuenta: string;
  nombreCuenta: string;
  nivelCuenta: number;
  naturalezaCuenta: string;
  cuentaOperable: boolean;
  saldoAnterior: number;
  valorDebitoMes: number;
  valorCreditoMes: number;
  saldoFinal: number;
  saldoMismoMesAnoAnterior: number | null;
  variacionValor: number | null;
  variacionPorcentaje: number | null;
}

export interface BalanceTotales {
  saldoAnterior: number;
  valorDebitoMes: number;
  valorCreditoMes: number;
  saldoFinal: number;
  saldoMismoMesAnoAnterior: number | null;
  variacionValor: number | null;
  variacionPorcentaje: number | null;
}

export interface BalanceFirmante {
  titulo: string;
  nombre: string;
  tarjetaProfesional: string | null;
}

export interface BalanceFirmas {
  firmante1: BalanceFirmante | null;
  firmante2: BalanceFirmante | null;
  firmante3: BalanceFirmante | null;
}

/** Valores del mes y acumulados de una categoría financiera. */
export interface BalanceIndicador {
  valorMes: number;
  valorAcumulado: number;
}

/** Indicadores calculados a partir de las clases contables. */
export interface BalanceIndicadoresFinancieros {
  activo: BalanceIndicador;
  pasivo: BalanceIndicador;
  patrimonio: BalanceIndicador;
  ingresos: BalanceIndicador;
  costosGastos: BalanceIndicador;
  gananciasPerdidas: BalanceIndicador;
}

/** Débitos y créditos de una etapa del resumen financiero. */
export interface BalanceMovimientoResumen {
  debitos: number;
  creditos: number;
}

/** Control financiero del período y sus diferencias contables. */
export interface BalanceResumenFinanciero {
  acumuladoAnterior: BalanceMovimientoResumen;
  movimientosMes: BalanceMovimientoResumen;
  acumuladoFinal: BalanceMovimientoResumen;
  diferenciaMes: number;
  diferenciaAcumulada: number;
}

/** Respuesta del Balance 1 individual o consolidado. */
export interface BalanceDetalladoMensualRespuesta
  extends BalanceDetalladoMensualFiltro {
  codigoAgencia: string;
  nombreAgencia: string;
  anoComparativo: number | null;
  mesComparativo: number | null;
  filas: BalanceFila[];
  totales: BalanceTotales;
  firmas: BalanceFirmas | null;
  indicadoresFinancieros: BalanceIndicadoresFinancieros | null;
  resumenFinanciero: BalanceResumenFinanciero | null;
}


/** Parámetros para consultar movimientos de una cuenta auxiliar. */
export interface BalanceMovimientosFiltro {
  idAgencia: number;
  ano: number;
  mes: number;
  codigoCuenta: string;
  pagina: number;
  tamanoPagina: number;
}

/** Movimiento contable asociado a una cuenta auxiliar operable. */
export interface BalanceMovimientoCuenta {
  idAuxiliarContable: number;
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
  idCatalogoCuenta: number;
  fechaAuxiliar: string;
  tipoComprobante: string;
  numeroComprobante: string;
  idDatosPersonal: number | null;
  detalleMovimiento: string;
  valorDebito: number;
  valorCredito: number;
}

/** Respuesta paginada de los movimientos del período seleccionado. */
export interface BalanceMovimientosRespuesta {
  codigoCuenta: string;
  ano: number;
  mes: number;
  pagina: number;
  tamanoPagina: number;
  hayMas: boolean;
  movimientos: BalanceMovimientoCuenta[];
}
