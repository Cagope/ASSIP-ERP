// =========================================================
// CAJA DISPONIBLE
// =========================================================

export interface CajaDisponible {
  idCaja: number;
  idAgencia: number;

  codigoCaja: string;
  descripcionCaja: string;

  permiteEfectivo: boolean;
  permiteCheques: boolean;
  permiteTarjetas: boolean;
  permiteTransferencias: boolean;
}


// =========================================================
// VINCULACIÓN USUARIO ↔ CAJA
// =========================================================

export interface CajaUsuario {
  idCajaUsuario: number;

  fechaJornada: string;
  estado: string;

  fechaVinculacion: string;
  fechaFinalizacion: string | null;
  motivoFinalizacion: string | null;

  idCaja: number;
  idAgencia: number;

  codigoCaja: string;
  descripcionCaja: string;

  idUsuario: number;
  username: string;
  nombreCompleto: string;
}


export interface CajaUsuarioVincularRequest {
  idCaja: number;
}


// =========================================================
// PREPARACIÓN DE APERTURA
// =========================================================

export interface CajaApertura {
  idCaja: number;
  idAgencia: number;

  codigoCaja: string;
  descripcionCaja: string;

  tipoCaja: string;

  fechaJornada: string;

  idProvision: number | null;
  estado: string | null;

  efectivoInicio: number;
  chequesInicio: number;

  fondoSugeridoVentanilla: number;
  limiteMaximoVentanilla: number;

  tieneProvision: boolean;
  tieneCierreAnterior: boolean;

  fechaUltimoCierre: string | null;
}


// =========================================================
// APERTURA DE CAJA
// =========================================================

export interface CajaAperturaRequest {
  idCaja: number;
  efectivoInicio: number;
  chequesInicio: number;

  valorInicialVentanilla: number;
}


// =========================================================
// PROVISIÓN ACTIVA
//
// Este modelo también es consumido por captura de depósitos,
// recaudos, convenios y movimientos interagencia.
// =========================================================

export interface CajaProvisionActiva {
  idCaja: number;

  codigoCaja: string;
  descripcionCaja: string;

  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;

  idProvision: number;
  fechaContable: string;
  estado: string;
}


// =========================================================
// ESTADO DE CAJA
// =========================================================

export interface CajaEstado {
  idCaja: number;

  codigoCaja: string;
  descripcionCaja: string;

  idProvision: number | null;
  estado: string | null;

  fkUsuarioApertura: number | null;
  fechaApertura: string | null;

  disponible: boolean;
}
