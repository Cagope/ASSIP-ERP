
/** Configuración financiera de seguro recibida y enviada al backend. */
export interface AdministracionSegurosConfiguracion {
  porcentajeBaseSeguro: number;
  tipoCobroSeguro: string;
  fechaInicialCobro: string | null;
  porcentajeAuxilioEntidad: number;
  porcentajeExtraprima: number;
  porcentajeAplicar: number;
  sobreSaldoActual: boolean;
  incluyeInteresesCausados: boolean;
  entidadAsumeSeguroBase: boolean;
}

/** Registro de seguro devuelto por la consulta de administración. */
export interface AdministracionSegurosSeguro extends AdministracionSegurosConfiguracion {
  idCreditoSeguro: number;
  idCarteraCredito: number;
  activo: boolean;
}

/** Solicitud de edición del seguro existente, sin cambiar su identificador. */
export interface AdministracionSegurosEdicionRequest {
  configuracion: AdministracionSegurosConfiguracion;
  activo: boolean;
}
