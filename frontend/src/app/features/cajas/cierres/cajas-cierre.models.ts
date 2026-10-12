export interface CajaCierreMovimientos {
  totalIngresos: number;
  totalEgresos: number;

  entradasEfectivo: number;
  salidasEfectivo: number;

  entradasCheques: number;
  salidasCheques: number;

  cantidadMovimientos: number;
}


export interface CajaCierreMovimientoResumen {
  idDepartamentoOperativo: number;
  codigoDepartamento: string;
  nombreDepartamento: string;

  naturaleza: string;

  cantidadMovimientos: number;

  valorEfectivo: number;
  valorCheques: number;
  valorOtros: number;

  valorTotal: number;
}


export interface CajaCierreComprobante {
  tipo: string;
  numero: string;
}


export interface CajaCierreComprobantesResumen {
  idDepartamentoOperativo: number;
  codigoDepartamento: string;
  nombreDepartamento: string;

  documentosOrigen: CajaCierreComprobante[];
  comprobantesCaja: CajaCierreComprobante[];
}


export interface CajaCierreVentanilla {
  cajaAVentanilla: number;
  ventanillaACaja: number;

  entradasOperacionesEfectivo: number;
  salidasOperacionesEfectivo: number;

  saldoVentanilla: number;
  saldoCajaInterna: number;
}

export interface CajaCierreDisponible {
  idProvision: number;

  idCaja: number;
  codigoCaja: string;
  descripcionCaja: string;

  fechaContable: string;

  estado: string;

  efectivoInicio: number;
  efectivoFin: number;

  chequesInicio: number;
  chequesFin: number;
}

export interface CajaCierreResumen {
  idProvision: number;
  idCaja: number;
  idAgencia: number;

  codigoCaja: string;
  descripcionCaja: string;
  tipoCaja: string;

  fechaContable: string;
  estado: string;

  efectivoInicio: number;
  chequesInicio: number;

  movimientos: CajaCierreMovimientos;

  resumenMovimientos: CajaCierreMovimientoResumen[];

  resumenComprobantes: CajaCierreComprobantesResumen[];

  ventanilla: CajaCierreVentanilla;

  efectivoFinCalculado: number;
  chequesFinCalculado: number;
  totalFisicoFinal: number;

  movimientosPendientes: number;
}


export interface CajaCierreEfectivoDetalle {
  tipoDetalle: 'BILLETE' | 'VARIOS' | 'MONEDAS';

  denominacion: number;
  cantidad: number;
  valorTotal: number;
}


export interface CajaCierreConfirmarRequest {
  idProvision: number;

  observacion: string | null;

  efectivoDetalle: CajaCierreEfectivoDetalle[];
}
