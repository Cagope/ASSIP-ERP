// =========================================================
// ESTADO ACTUAL CAJA / VENTANILLA
// =========================================================

export interface CajaVentanillaEstado {

  // Provisión
  idProvision: number;
  fechaContable: string;
  estadoProvision: string;

  // Caja
  idCaja: number;
  idAgencia: number;
  codigoCaja: string;
  descripcionCaja: string;
  tipoCaja: string;

  // Parámetros de ventanilla
  fondoSugeridoVentanilla: number;
  limiteMaximoVentanilla: number;

  // Efectivo de la provisión
  efectivoInicio: number;
  entradasOperacionesEfectivo: number;
  salidasOperacionesEfectivo: number;
  efectivoTotal: number;

  // Traslados internos
  cajaAVentanilla: number;
  ventanillaACaja: number;

  // Saldos físicos actuales
  saldoVentanilla: number;
  saldoCajaInterna: number;
}


// =========================================================
// TIPO DE TRASLADO
// =========================================================

export type CajaVentanillaTipoMovimiento =
  | 'CAJA_A_VENTANILLA'
  | 'VENTANILLA_A_CAJA';


// =========================================================
// SOLICITUD DE TRASLADO
// =========================================================

export interface CajaVentanillaMovimiento {
  tipoMovimiento: CajaVentanillaTipoMovimiento;
  valor: number;
  observacion: string | null;
}
