export interface RollForwardCorte {
  fechaCorteAnterior: string;
  fechaCorte: string;
}

export interface RollForwardResumen {
  fechaCorteAnterior: string;
  fechaCorte: string;

  creditosIniciales: number;
  creditosFinales: number;
  creditosNuevos: number;
  creditosReingresados: number;
  creditosReduccion: number;
  creditosSinVariacion: number;
  creditosAumento: number;
  creditosPrepago: number;
  creditosCancelacionNormal: number;
  creditosCancelacionPostVencimiento: number;
  creditosAusenciaTemporal: number;
  creditosOtrasSalidas: number;

  saldoInicial: number;
  nuevos: number;
  reingresos: number;
  aumentosSaldo: number;
  reduccionesSaldo: number;
  prepagos: number;
  cancelacionesNormales: number;
  cancelacionesPostVencimiento: number;
  ausenciasTemporales: number;
  otrasSalidas: number;
  saldoFinal: number;

  totalEntradas: number;
  totalSalidasReducciones: number;
  variacionNeta: number;
  variacionPorcentaje: number;

  tasaNuevosSobreSaldoInicial: number;
  tasaReduccionSobreSaldoInicial: number;
  tasaPrepagoSobreSaldoInicial: number;
  tasaSalidasDefinitivasSobreSaldoInicial: number;

  saldoFinalCalculado: number;
  diferenciaControl: number;
  creditosFinalesCalculados: number;
  diferenciaControlCreditos: number;
}

export interface RollForwardLinea {
  fechaCorteAnterior: string;
  fechaCorte: string;

  idLineaCredito: number;
  codigoLineaCredito: string;
  nombreLineaCredito: string;

  creditosIniciales: number;
  creditosFinales: number;
  creditosNuevos: number;
  creditosReingresados: number;
  creditosReduccion: number;
  creditosSinVariacion: number;
  creditosAumento: number;
  creditosPrepago: number;
  creditosCancelacionNormal: number;
  creditosCancelacionPostVencimiento: number;
  creditosAusenciaTemporal: number;
  creditosOtrasSalidas: number;

  saldoInicial: number;
  nuevos: number;
  reingresos: number;
  aumentosSaldo: number;
  reduccionesSaldo: number;
  prepagos: number;
  cancelacionesNormales: number;
  cancelacionesPostVencimiento: number;
  ausenciasTemporales: number;
  otrasSalidas: number;
  saldoFinal: number;

  variacionNeta: number;
  variacionPorcentaje: number;

  saldoFinalCalculado: number;
  diferenciaControl: number;
  creditosFinalesCalculados: number;
  diferenciaControlCreditos: number;
}

export interface RollForwardDetalle {
  fechaCorteAnterior: string;
  fechaCorte: string;

  idCierreCarteraAnterior: number | null;
  idCierreCarteraActual: number | null;
  idCierreCarteraCreditoAnterior: number | null;
  idCierreCarteraCreditoActual: number | null;

  idCarteraCredito: number;
  idAgencia: number | null;

  idLineaCredito: number | null;
  codigoLineaCredito: string | null;
  nombreLineaCredito: string | null;

  pagareCartera: string | null;

  idDatosPersonal: number | null;
  tipoDocumento: string | null;
  documento: string | null;
  nombreCompleto: string | null;

  codigoClasificacionCredito: string | null;
  descripcionClasificacionCredito: string | null;
  codigoDestinoEconomico: string | null;
  descripcionDestinoEconomico: string | null;

  valorInicialCredito: number | null;
  valorDesembolsado: number | null;
  fechaDesembolso: string | null;
  fechaFinal: string | null;

  plazo: number | null;
  codigoFormaPago: string | null;
  codigoTipoCuota: string | null;
  amortizacionCapital: number | null;
  valorCuota: number | null;

  primeraAparicion: string | null;
  ultimaAparicion: string | null;

  existeCorteAnterior: boolean | null;
  existeCorteActual: boolean | null;

  saldoAnterior: number;
  saldoActual: number;
  variacionSaldo: number;

  tipoMovimiento: string;
  diasAnticipacion: number | null;
  rangoSalida: string | null;

  diasMoraAnterior: number | null;
  diasMoraActual: number | null;
  edadContableAnterior: string | null;
  edadContableActual: string | null;

  deterioroCapitalAnterior: number | null;
  deterioroCapitalActual: number | null;
  deterioroInteresesAnterior: number | null;
  deterioroInteresesActual: number | null;
  deterioroOtrosAnterior: number | null;
  deterioroOtrosActual: number | null;
  deterioroTotalAnterior: number | null;
  deterioroTotalActual: number | null;

  valorNuevos: number;
  valorReingresos: number;
  valorAumentosSaldo: number;
  valorReduccionesSaldo: number;
  valorPrepagos: number;
  valorCancelacionesNormales: number;
  valorCancelacionesPostVencimiento: number;
  valorAusenciasTemporales: number;
  valorOtrasSalidas: number;
  valorMovimientoNeto: number;
}

export type RollForwardVista = 'RESUMEN' | 'LINEAS' | 'HISTORICO' | 'DETALLE';
