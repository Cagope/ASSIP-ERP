// =========================================================
// DATOS PARA IMPRESIÓN DEL PAGARÉ
//
// Documentos generados desde Angular:
//
// - Pagaré diligenciado.
// - Pagaré en blanco.
// - Carta de instrucciones.
//
// La información procede del crédito constituido.
// =========================================================

export interface PagareDiligenciadoDatos {

  tipoPagare: 'DILIGENCIADO' | 'EN_BLANCO';

  pagare: PagareDatos;

  firmantes: PagareFirmante[];

  valorFormalizadoEnLetras: string;

  condicionesPago: string;
}

// =========================================================
// PAGARÉ Y CONDICIONES FINANCIERAS
// =========================================================

export interface PagareDatos {

  // -------------------------------------------------------
  // IDENTIFICACIÓN DE LA SOLICITUD
  // -------------------------------------------------------

  idSolicitudCredito: number;
  numeroSolicitud: string;

  idAgencia: number;
  idDatosPersonal: number;

  idCuentaAportes: number | null;
  idLineaCredito: number;

  idSolicitudProceso: number;
  idSolicitudResultado: number;


  // -------------------------------------------------------
  // CRÉDITO CONSTITUIDO
  // -------------------------------------------------------

  idCarteraCredito: number;
  pagareCartera: string;
  codigoEstadoCartera: string;


  // -------------------------------------------------------
  // FECHAS DEFINITIVAS DEL CRÉDITO
  //
  // Fuente: cartera.carteras_creditos
  //
  // No se calculan nuevamente en Angular.
  // -------------------------------------------------------

  fechaInclusionSistema: string | null;

  fechaPrimeraCuota: string | null;

  fechaPrimeraCuotaCapital: string | null;

  fechaPrimeraCuotaInteres: string | null;

  fechaFinal: string | null;


  // -------------------------------------------------------
  // CONDICIONES COMPLEMENTARIAS
  // -------------------------------------------------------

  mesesGraciaCapital: number | null;

  mesesGraciaInteres: number | null;

  codigoGarantiaCredito: string | null;

  descripcionGarantiaCredito: string | null;

  codigoDestinoEconomico: string | null;

  descripcionDestinoEconomico: string | null;

  numeroCuentaAportes: string | null;


  // -------------------------------------------------------
  // LÍNEA DE CRÉDITO
  // -------------------------------------------------------

  codigoLineaCredito: string;
  nombreLineaCredito: string;


  // -------------------------------------------------------
  // CONDICIONES FINANCIERAS DEFINITIVAS
  // -------------------------------------------------------

  valorFormalizado: number;

  plazoFormalizado: number;

  amortizacionCapitalFormalizada: number;

  valorCuotaFormalizada: number;

  tasaNominalFormalizada: number;

  tasaEfectivaAnualFormalizada: number;

  codigoFormaPagoFormalizada: string;

  periodoCodigoInteresFormalizado: string;

  tipoModalidadInteresFormalizado: string;

  codigoTipoCuotaFormalizada: string;

}


// =========================================================
// DEUDOR PRINCIPAL Y CODEUDORES
// =========================================================

export interface PagareFirmante {

  idSolicitudDeudor: number;

  idSolicitudCredito: number;

  idDatosPersonal: number;

  tipoDeudor: string;

  ordenDeudor: number | null;

  tipoDocumento: string;

  documento: string;

  nombreCompleto: string;

  direccion: string | null;

  telefono: string | null;

  celularUno: string | null;

  celularDos: string | null;

  correo: string | null;

}
