
/** Parámetros de consulta del Estado de Excedentes o Pérdidas sin formato. */
export interface PyGSinFormatoFiltro {
  idAgencia: number;
  ano: number;
  mes: number;
}

/** Cuenta contable incluida en el resultado mensual. */
export interface PyGSinFormatoCuenta {
  codigoCuenta: string;
  nombreCuenta: string;
  nivelCuenta: number;
  naturalezaCuenta: string;
  debitos: number;
  creditos: number;
  saldo: number;
}

/** Resultado mensual del PyG sin formato. */
export interface PyGSinFormatoRespuesta {
  idAgencia: number;
  ano: number;
  mes: number;
  cuentas: PyGSinFormatoCuenta[];
  totalIngresos: number;
  totalCostosGastos: number;
  excedentePerdida: number;
}
