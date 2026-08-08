export interface ExpedienteAsociado {

  encontrado: boolean;
  codigoResultado: string;
  mensaje: string;

  idDatosPersonal: number | null;
  tipoDocumento: string | null;
  nombreTipoDocumento: string | null;
  documento: string | null;
  nombreCompleto: string | null;

  resumenGeneral: ExpedienteResumenGeneral | null;
  afiliacion: ExpedienteAfiliacion | null;
  contacto: ExpedienteContacto | null;
  informacionFinanciera: ExpedienteInformacionFinanciera | null;
  sarlaft: ExpedienteSarlaft | null;

  participacionInstitucional: ExpedienteParticipacionInstitucional[];

  cuentasAhorro: ExpedienteCuentaAhorro[];
  cdats: ExpedienteCdat[];
  creditos: ExpedienteCredito[];

  bienesInmuebles: ExpedienteBienInmueble[];
  bienesVehiculos: ExpedienteBienVehiculo[];
  bienesMaquinaria: ExpedienteBienMaquinaria[];
  bienesInversiones: ExpedienteBienInversion[];

  garantias: ExpedienteGarantia[];
  alertas: ExpedienteAlerta[];

  indicadores: ExpedienteIndicadores | null;

  cantidadParticipacionesInstitucionales: number;
  cantidadCuentasAhorro: number;
  cantidadCdats: number;
  cantidadCreditos: number;
  cantidadBienes: number;
  cantidadGarantias: number;

  cantidadAlertas: number;
  cantidadAlertasCriticas: number;
  cantidadAlertasAdvertencia: number;
  cantidadAlertasInformativas: number;

  estadoCarga: ExpedienteEstadoCarga | null;

  fechaHoraConsulta: string | null;
  idUsuarioConsulta: number | null;
  idAgenciaConsulta: number | null;
}

export interface ExpedienteResumenGeneral {
  idDatosPersonal: number;

  tipoPersona: string | null;
  tipoDocumento: string | null;
  nombreTipoDocumento: string | null;
  documento: string | null;

  nombres: string | null;
  primerApellido: string | null;
  segundoApellido: string | null;
  nombreCompleto: string | null;

  codigoEstadoAsociado: string | null;
  nombreEstadoAsociado: string | null;

  codigoTipoAsociado: string | null;
  nombreTipoAsociado: string | null;

  codigoCategoria: string | null;
  nombreCategoria: string | null;

  activo: boolean;
  fechaAfiliacion: string | null;

  antiguedadDias: number | null;
  antiguedadMeses: number | null;
  antiguedadAnios: number | null;

  // =========================================================
  // Información personal
  // =========================================================

  fechaNacimiento: string | null;
  edad: number | null;

  genero: string | null;
  estadoCivil: string | null;
  nombreEscolaridad: string | null;
  ocupacion: string | null;

  cabezaFamilia: string | null;
  numeroHijos: number | null;
  estratoSocial: number | null;
  nombreTipoVivienda: string | null;

  // =========================================================
  // Lugar de nacimiento
  // =========================================================

  paisNacimiento: string | null;
  departamentoNacimiento: string | null;
  ciudadNacimiento: string | null;

  // =========================================================
  // Perfil económico
  // =========================================================

  nombreSectorEconomico: string | null;
  nombreActividadSes: string | null;
  nombreActividadDian: string | null;

  // =========================================================
  // Contacto y ubicación
  // =========================================================

  direccion: string | null;
  barrio: string | null;
  ciudad: string | null;
  departamento: string | null;
  pais: string | null;

  telefono: string | null;
  celular: string | null;
  correoElectronico: string | null;

  // =========================================================
  // Información laboral
  // =========================================================

  empresa: string | null;

  // =========================================================
  // Información financiera
  // =========================================================

  ingresosMensuales: number;
  egresosMensuales: number;

  activos: number;
  pasivos: number;
  patrimonio: number;

  // =========================================================
  // Productos
  // =========================================================

  numeroCuentasAhorro: number;
  numeroCdats: number;
  numeroCreditos: number;
  numeroBienes: number;

  saldoAportes: number;
  saldoAhorros: number;
  saldoCdats: number;

  saldoCapitalCartera: number;
  saldoInteresesCartera: number;

  valorBienes: number;
  valorGarantias: number;
  patrimonioEstimado: number;

  // =========================================================
  // SARLAFT y señales especiales
  // =========================================================

  pep: boolean | null;
  familiarPep: boolean | null;
  monedaExtranjera: boolean | null;
  cuentaExterior: boolean | null;

  alertasCriticas: number;
  alertasAdvertencia: number;
  alertasInformativas: number;

  // =========================================================
  // Actualización de información
  // =========================================================

  fechaActualizacionHojaVida: string | null;
  diasSinActualizar: number | null;

  // =========================================================
  // Agencia
  // =========================================================

  idAgencia: number | null;
  codigoAgencia: string | null;
  nombreAgencia: string | null;
}

export interface ExpedienteAfiliacion {

  // =========================================================
  // Identificación del asociado
  // =========================================================
  idDatosPersonal: number;

  tipoDocumento: string | null;
  nombreTipoDocumento: string | null;
  documento: string | null;

  nombres: string | null;
  primerApellido: string | null;
  segundoApellido: string | null;
  nombreCompleto: string | null;

  // =========================================================
  // Identificación de la afiliación
  // =========================================================
  idAfiliacion: number | null;

  codigoAsociado: string | null;
  numeroAfiliacion: string | null;

  fechaAfiliacion: string | null;
  fechaIngreso: string | null;
  fechaAntiguedad: string | null;

  // =========================================================
  // Estado actual de la afiliación
  // =========================================================
  codigoEstadoAsociado: string | null;
  nombreEstadoAsociado: string | null;

  codigoEstadoAfiliacion: string | null;
  nombreEstadoAfiliacion: string | null;

  afiliacionActiva: boolean;

  fechaEstado: string | null;
  motivoEstado: string | null;
  observacionEstado: string | null;

  // =========================================================
  // Tipo y clasificación del asociado
  // =========================================================
  idTipoAsociado: number | null;

  codigoTipoAsociado: string | null;
  nombreTipoAsociado: string | null;

  idCategoriaAsociado: number | null;

  codigoCategoriaAsociado: string | null;
  nombreCategoriaAsociado: string | null;

  idClaseAsociado: number | null;

  codigoClaseAsociado: string | null;
  nombreClaseAsociado: string | null;

  // =========================================================
  // Modalidad o vínculo institucional
  // =========================================================
  codigoVinculacion: string | null;
  nombreVinculacion: string | null;

  codigoOrigenVinculacion: string | null;
  nombreOrigenVinculacion: string | null;

  asociadoFundador: boolean;
  asociadoHabil: boolean;
  delegado: boolean;

  // =========================================================
  // Agencia de afiliación
  // =========================================================
  idAgenciaAfiliacion: number | null;

  codigoAgenciaAfiliacion: string | null;
  nombreAgenciaAfiliacion: string | null;

  // =========================================================
  // Agencia actual
  // =========================================================
  idAgenciaActual: number | null;

  codigoAgenciaActual: string | null;
  nombreAgenciaActual: string | null;

  // =========================================================
  // Zona o ubicación administrativa
  // =========================================================
  idZona: number | null;

  codigoZona: string | null;
  nombreZona: string | null;

  idSubZona: number | null;

  codigoSubZona: string | null;
  nombreSubZona: string | null;

  // =========================================================
  // Aportes sociales
  // =========================================================
  idCuentaAportes: number | null;

  codigoCuentaAportes: string | null;
  codigoFormaAportes: string | null;
  nombreFormaAportes: string | null;

  fechaAperturaAportes: string | null;

  cuotaAportes: number;
  saldoAportes: number;
  saldoDisponibleAportes: number;

  codigoEstadoCuentaAportes: string | null;
  nombreEstadoCuentaAportes: string | null;

  cuentaAportesActiva: boolean;

  // =========================================================
  // Reciprocidad y obligaciones sociales
  // =========================================================
  porcentajeReciprocidad: number;
  valorReciprocidad: number;

  aporteMinimo: number;
  aporteOrdinario: number;
  aporteExtraordinario: number;

  cumpleAporteMinimo: boolean;
  cumpleReciprocidad: boolean;

  // =========================================================
  // Fechas y antigüedad
  // =========================================================
  antiguedadDias: number;
  antiguedadMeses: number;
  antiguedadAnios: number;

  fechaUltimoAporte: string | null;
  fechaUltimoMovimientoAportes: string | null;

  // =========================================================
  // Retiro o desvinculación
  // =========================================================
  retirado: boolean;

  fechaSolicitudRetiro: string | null;
  fechaRetiro: string | null;
  fechaLiquidacionRetiro: string | null;

  codigoMotivoRetiro: string | null;
  nombreMotivoRetiro: string | null;

  observacionRetiro: string | null;

  // =========================================================
  // Reingreso
  // =========================================================
  reingreso: boolean;

  numeroReingresos: number;
  fechaUltimoReingreso: string | null;

  // =========================================================
  // Derechos políticos y participación
  // =========================================================
  puedeElegir: boolean;
  puedeSerElegido: boolean;
  puedeParticiparAsamblea: boolean;

  restriccionDerechos: string | null;
  fechaInicioRestriccion: string | null;
  fechaFinRestriccion: string | null;

  // =========================================================
  // Información laboral relacionada con la afiliación
  // =========================================================
  idEmpresaVinculada: number | null;

  documentoEmpresa: string | null;
  nombreEmpresa: string | null;
  cargoEmpresa: string | null;

  fechaIngresoEmpresa: string | null;
  fechaRetiroEmpresa: string | null;

  libranzaActiva: boolean;

  // =========================================================
  // Convenio o grupo empresarial
  // =========================================================
  idConvenio: number | null;

  codigoConvenio: string | null;
  nombreConvenio: string | null;

  idGrupoAsociado: number | null;

  codigoGrupoAsociado: string | null;
  nombreGrupoAsociado: string | null;

  // =========================================================
  // Actualización de información
  // =========================================================
  fechaUltimaActualizacion: string | null;

  diasSinActualizar: number;
  informacionActualizada: boolean;

  // =========================================================
  // Documentación de afiliación
  // =========================================================
  formularioAfiliacionCompleto: boolean;
  documentosIdentificacionCompletos: boolean;
  autorizacionTratamientoDatos: boolean;
  declaracionOrigenFondos: boolean;
  consultaCentralesAutorizada: boolean;

  documentacionCompleta: boolean;

  // =========================================================
  // Alertas propias del bloque
  // =========================================================
  presentaNovedades: boolean;
  cantidadNovedades: number;

  nivelNovedad: string | null;
  resumenNovedades: string | null;

  // =========================================================
  // Observaciones
  // =========================================================
  observaciones: string | null;

  // =========================================================
  // Auditoría
  // =========================================================
  fkSeguridadCreacion: number | null;
  fechaCreacion: string | null;

  fkSeguridadEdicion: number | null;
  fechaEdicion: string | null;
}

// =========================================================
// CONTACTO
// =========================================================

export interface ExpedienteContacto {

  // =======================================================
  // Identificación de la persona
  // =======================================================

  idDatosPersonal: number | null;

  tipoDocumento: string | null;
  nombreTipoDocumento: string | null;
  documento: string | null;

  nombres: string | null;
  primerApellido: string | null;
  segundoApellido: string | null;
  nombreCompleto: string | null;

  // =======================================================
  // Ubicación principal
  // =======================================================

  idUbicacion: number | null;

  direccionPrincipal: string | null;
  complementoDireccion: string | null;
  barrioVereda: string | null;

  idPais: number | null;
  codigoPais: string | null;
  nombrePais: string | null;

  idDepartamento: number | null;
  codigoDepartamento: string | null;
  nombreDepartamento: string | null;

  idCiudad: number | null;
  codigoCiudad: string | null;
  nombreCiudad: string | null;

  codigoPostal: string | null;

  direccionPrincipalActiva: boolean | null;

  // =======================================================
  // Teléfonos
  // =======================================================

  telefonoResidencia: string | null;
  telefonoTrabajo: string | null;
  telefonoAlterno: string | null;

  celularPrincipal: string | null;
  celularAlterno: string | null;

  // =======================================================
  // Correos y medios digitales
  // =======================================================

  correoPrincipal: string | null;
  correoAlterno: string | null;

  sitioWeb: string | null;

  usuarioWhatsapp: string | null;
  usuarioTelegram: string | null;

  medioContactoPreferido: string | null;

  // =======================================================
  // Autorizaciones de contacto
  // =======================================================

  autorizaCorreoElectronico: boolean | null;
  autorizaMensajesTexto: boolean | null;
  autorizaWhatsapp: boolean | null;
  autorizaLlamadasTelefonicas: boolean | null;

  // =======================================================
  // Información laboral de contacto
  // =======================================================

  empresa: string | null;
  cargo: string | null;

  direccionEmpresa: string | null;
  telefonoEmpresa: string | null;
  extensionEmpresa: string | null;

  // =======================================================
  // Residencia y correspondencia
  // =======================================================

  resideExterior: boolean | null;

  direccionCorrespondenciaDiferente: boolean | null;
  direccionCorrespondencia: string | null;

  ciudadCorrespondencia: string | null;
  departamentoCorrespondencia: string | null;
  paisCorrespondencia: string | null;

  // =======================================================
  // Indicadores de disponibilidad
  // =======================================================

  tieneDireccion: boolean | null;
  tieneTelefono: boolean | null;
  tieneCelular: boolean | null;
  tieneCorreo: boolean | null;

  contactoCompleto: boolean | null;
  porcentajeCompletitud: number | null;

  // =======================================================
  // Validación y actualización
  // =======================================================

  direccionActualizada: boolean | null;

  telefonoValido: boolean | null;
  celularValido: boolean | null;
  correoValido: boolean | null;

  fechaUltimaActualizacion: string | null;
  diasSinActualizar: number | null;

  // =======================================================
  // Novedades
  // =======================================================

  presentaNovedades: boolean | null;
  cantidadNovedades: number | null;

  nivelNovedad: string | null;
  resumenNovedades: string | null;

  observaciones: string | null;

  // =======================================================
  // Auditoría
  // =======================================================

  fkSeguridadCreacion: number | null;
  fechaCreacion: string | null;

  fkSeguridadEdicion: number | null;
  fechaEdicion: string | null;
}

// =========================================================
// INFORMACIÓN FINANCIERA
// =========================================================

export interface ExpedienteInformacionFinanciera {

  // =======================================================
  // Identificación
  // =======================================================

  idDatosPersonal: number | null;

  tipoDocumento: string | null;
  nombreTipoDocumento: string | null;
  documento: string | null;

  nombres: string | null;
  primerApellido: string | null;
  segundoApellido: string | null;
  nombreCompleto: string | null;

  // =======================================================
  // Registro financiero
  // =======================================================

  idDatoFinanciero: number | null;

  // =======================================================
  // Ingresos
  // =======================================================

  valorSalario: number | null;
  valorPension: number | null;

  ingresosArriendo: number | null;
  ingresosComisiones: number | null;
  otrosIngresos: number | null;

  conceptoOtrosIngresos: string | null;

  ingresosLaborales: number | null;
  ingresosNoLaborales: number | null;

  ingresosMensuales: number | null;
  totalIngresos: number | null;

  // =======================================================
  // Egresos
  // =======================================================

  egresosFamiliares: number | null;
  egresosArriendo: number | null;
  egresosCredito: number | null;
  otrosEgresos: number | null;

  conceptoOtrosEgresos: string | null;

  egresosMensuales: number | null;
  totalEgresos: number | null;

  // =======================================================
  // Patrimonio declarado
  // =======================================================

  totalActivos: number | null;
  totalPasivos: number | null;

  activos: number | null;
  pasivos: number | null;

  patrimonio: number | null;
  patrimonioNeto: number | null;

  // =======================================================
  // Flujo y capacidad financiera
  // =======================================================

  disponibleMensual: number | null;
  capacidadPago: number | null;

  porcentajeEndeudamiento: number | null;
  porcentajeCompromisoIngresos: number | null;

  // =======================================================
  // Productos del asociado
  // =======================================================

  numeroCuentasAhorro: number | null;
  numeroCdats: number | null;
  numeroCreditos: number | null;
  numeroBienes: number | null;

  saldoAportes: number | null;
  saldoAhorros: number | null;
  saldoCdats: number | null;

  saldoCapitalCartera: number | null;
  saldoInteresesCartera: number | null;

  valorBienes: number | null;
  valorGarantias: number | null;

  patrimonioEstimado: number | null;

  // =======================================================
  // Actividad económica
  // =======================================================

  idActividadEconomica: number | null;
  codigoActividadEconomica: string | null;
  nombreActividadEconomica: string | null;

  idSectorEconomico: number | null;
  codigoSectorEconomico: string | null;
  nombreSectorEconomico: string | null;

  ocupacion: string | null;
  profesion: string | null;

  actividadPrincipal: string | null;
  actividadSecundaria: string | null;

  // =======================================================
  // Origen y destino de fondos
  // =======================================================

  origenFondos: string | null;
  detalleOrigenFondos: string | null;

  destinoFondos: string | null;
  detalleDestinoFondos: string | null;

  declaracionOrigenFondos: boolean | null;
  origenFondosCompleto: boolean | null;

  // =======================================================
  // Moneda extranjera
  // =======================================================

  realizaOperacionesMonedaExtranjera: boolean | null;

  tipoOperacionMonedaExtranjera: string | null;
  monedaPrincipal: string | null;

  paisOperacion: string | null;
  ciudadOperacion: string | null;
  entidadOperacion: string | null;
  productoOperacion: string | null;

  valorPromedioOperacion: number | null;
  valorMaximoOperacion: number | null;

  observacionesMonedaExtranjera: string | null;

  // =======================================================
  // Cuentas en el exterior
  // =======================================================

  poseeCuentasExterior: boolean | null;

  paisCuentaExterior: string | null;
  ciudadCuentaExterior: string | null;

  entidadCuentaExterior: string | null;
  numeroCuentaExterior: string | null;
  monedaCuentaExterior: string | null;

  // =======================================================
  // Indicadores y validaciones
  // =======================================================

  informacionCompleta: boolean | null;
  informacionFinancieraCompleta: boolean | null;

  ingresosRegistrados: boolean | null;
  egresosRegistrados: boolean | null;
  patrimonioRegistrado: boolean | null;

  presentaPatrimonioNegativo: boolean | null;
  presentaDeficitMensual: boolean | null;
  requiereActualizacion: boolean | null;
  requiereRevision: boolean | null;

  // =======================================================
  // Actualización
  // =======================================================

  fechaActualizacion: string | null;
  fechaUltimaActualizacion: string | null;

  diasSinActualizar: number | null;
  informacionActualizada: boolean | null;

  // =======================================================
  // Novedades
  // =======================================================

  presentaNovedades: boolean | null;
  cantidadNovedades: number | null;

  nivelNovedad: string | null;
  resumenNovedades: string | null;

  observaciones: string | null;

  // =======================================================
  // Auditoría
  // =======================================================

  fkSeguridadCreacion: number | null;
  fechaCreacion: string | null;

  fkSeguridadEdicion: number | null;
  fechaEdicion: string | null;
}

// =========================================================
// SARLAFT
// =========================================================

// =========================================================
// SARLAFT
// =========================================================

export interface ExpedienteSarlaft {

  // =======================================================
  // Identificación
  // =======================================================

  idDatosPersonal: number | null;

  tipoDocumento: string | null;
  nombreTipoDocumento: string | null;
  documento: string | null;

  tipoPersona: string | null;

  nombres: string | null;
  primerApellido: string | null;
  segundoApellido: string | null;
  nombreCompleto: string | null;

  fechaNacimiento: string | null;

  // =======================================================
  // Estado de actualización
  // =======================================================

  fechaActualizacion: string | null;

  fechaCreacionDatos: string | null;
  fechaEdicionDatos: string | null;

  // =======================================================
  // Información económica
  // =======================================================

  codigoOcupacion: string | null;
  nombreOcupacion: string | null;

  codigoSectorEconomico: string | null;
  nombreSectorEconomico: string | null;

  codigoActividadSes: string | null;
  nombreActividadSes: string | null;

  codigoActividadDian: string | null;
  nombreActividadDian: string | null;

  origenFondos: string | null;

  // =======================================================
  // Persona Expuesta Políticamente
  // =======================================================

  asociadoPeps: boolean | null;

  tipoPeps: string | null;
  nombreTipoPeps: string | null;

  observacionesPeps: string | null;

  fechaInicialPeps: string | null;
  fechaFinalPeps: string | null;

  // =======================================================
  // Familiar PEP
  // =======================================================

  familiaPeps: boolean | null;

  tipoFamiliaPeps: string | null;

  cedulaFamiliaPeps: string | null;

  codigoParentesco: string | null;
  nombreParentesco: string | null;

  nombreFamiliaPeps: string | null;

  // =======================================================
  // Operaciones internacionales
  // =======================================================

  monedaExtranjera: boolean | null;

  observacionMonedaExtranjera: string | null;

  // =======================================================
  // Cuenta en el exterior
  // =======================================================

  cuentaExtranjero: boolean | null;

  tipoMonedaExtranjera: string | null;

  numeroCuentaExtranjero: string | null;

  nombreBancoExtranjero: string | null;

  ciudadCuentaExtranjero: string | null;
  paisCuentaExtranjero: string | null;

  // =======================================================
  // Residencia fiscal (FATCA / CRS)
  // =======================================================

  idResidenciaFiscal: number | null;

  tieneInformacionResidenciaFiscal: boolean | null;

  ciudadanoEstadosUnidos: boolean | null;

  residenteFiscalEstadosUnidos: boolean | null;

  residenteFiscalExterior: boolean | null;

  paisResidenciaFiscal: string | null;

  numeroIdentificacionFiscal: string | null;

  tipoIdentificacionFiscal: string | null;

  ciudadResidenciaFiscal: string | null;

  direccionResidenciaFiscal: string | null;

  observacionesResidenciaFiscal: string | null;

  fechaCreacionResidenciaFiscal: string | null;
  fechaEdicionResidenciaFiscal: string | null;

  // =======================================================
  // Condiciones especiales de protección
  // =======================================================

  idCondicionProteccion: number | null;

  tieneInformacionCondicionesProteccion: boolean | null;

  administraRecursosPublicos: boolean | null;

  grupoProteccionEspecialConstitucional: boolean | null;

  personaMayor60Anos: boolean | null;

  discapacidadFisica: boolean | null;

  victimaConflictoArmado: boolean | null;

  pobrezaExtrema: boolean | null;

  poblacionIndigena: boolean | null;

  poblacionAfrodescendiente: boolean | null;

  poblacionLgbtiqMas: boolean | null;

  perteneceGrupoProteccionConstitucional: boolean | null;

  observacionesCondicionesProteccion: string | null;

  fechaCreacionCondicionesProteccion: string | null;
  fechaEdicionCondicionesProteccion: string | null;
}

// =========================================================
// PARTICIPACIÓN INSTITUCIONAL
// =========================================================

export type TipoParticipacionInstitucional =
  | 'DIRECTIVO'
  | 'COMITE'
  | 'PRIVILEGIADO'
  | 'PERSONA_RELACIONADA';

export interface ExpedienteParticipacionInstitucional {

  // =======================================================
  // Clasificación general
  // =======================================================

  tipoParticipacion:
    TipoParticipacionInstitucional
    | string
    | null;

  esPrivilegiado: boolean | null;

  // =======================================================
  // Directivos
  // =======================================================

  idDirectivo: number | null;

  codigoTipoDirectivo: string | null;
  nombreTipoDirectivo: string | null;

  calidadDirectivo: string | null;
  nombreCalidadDirectivo: string | null;

  estadoDirectivo: string | null;
  nombreEstadoDirectivo: string | null;

  actaAsamblea: string | null;
  fechaAsamblea: string | null;

  resolucionSes: string | null;
  fechaResolucion: string | null;

  fechaRetiro: string | null;

  // =======================================================
  // Comités
  // =======================================================

  idComite: number | null;
  nombreComite: string | null;

  idComiteDetalle: number | null;

  codigoCargoComite: string | null;
  nombreCargoComite: string | null;

  numeroActa: string | null;
  fechaNombramiento: string | null;

  // =======================================================
  // Privilegiados y personas relacionadas
  // =======================================================

  idPrivilegiado: number | null;
  idPersonaRelacionada: number | null;

  documentoRelacionado: string | null;
  nombreRelacionado: string | null;

  codigoParentesco: string | null;
  nombreParentesco: string | null;

  // =======================================================
  // Presentación
  // =======================================================

  orden: number;
}

// =========================================================
// CUENTAS DE AHORRO
// =========================================================

export interface ExpedienteCuentaAhorro {

  // =======================================================
  // Identificación
  // =======================================================

  idCuentaAhorro: number | null;
  idDatosPersonal: number | null;

  documento: string | null;
  nombreCompleto: string | null;

  // =======================================================
  // Forma de ahorro
  // =======================================================

  idFormaAhorro: number | null;
  codigoFormaAhorro: string | null;
  nombreFormaAhorro: string | null;

  codigoTipoCaptacion: string | null;
  nombreTipoCaptacion: string | null;

  // =======================================================
  // Cuenta
  // =======================================================

  numeroCuenta: string | null;
  numeroLibreta: string | null;

  codigoEstadoCuenta: string | null;
  nombreEstadoCuenta: string | null;

  // =======================================================
  // Estado
  // =======================================================

  activa: boolean | null;
  bloqueada: boolean | null;
  embargada: boolean | null;
  cancelada: boolean | null;

  // =======================================================
  // Fechas y movimiento
  // =======================================================

  fechaApertura: string | null;
  fechaUltimoMovimiento: string | null;

  diasSinMovimiento: number | null;

  // =======================================================
  // Saldos
  // =======================================================

  saldoDisponible: number | null;
  saldoCanje: number | null;
  saldoTotal: number | null;
  saldoPromedio: number | null;

  // =======================================================
  // Acumulados históricos
  // =======================================================

  totalConsignaciones: number | null;
  totalRetiros: number | null;
  totalIntereses: number | null;

  // =======================================================
  // Intereses e impuestos
  // =======================================================

  generaIntereses: boolean | null;
  exentaGMF: boolean | null;

  // =======================================================
  // Titularidad y relaciones
  // =======================================================

  cuentaConjunta: boolean | null;
  tieneBeneficiarios: boolean | null;
  tieneApoderados: boolean | null;

  // =======================================================
  // Cheques en canje
  // =======================================================

  cantidadChequesCanje: number | null;
  valorChequesCanje: number | null;

  // =======================================================
  // Entradas del mes
  // =======================================================

  cantidadEntradasMes: number | null;
  valorEntradasMes: number | null;

  // =======================================================
  // Salidas del mes
  // =======================================================

  cantidadSalidasMes: number | null;
  valorSalidasMes: number | null;

  // =======================================================
  // Movimiento consolidado del período
  // =======================================================

  cantidadMovimientosMes: number | null;
  cantidadMovimientosAno: number | null;

  valorMovimientosMes: number | null;
  valorMovimientosAno: number | null;

  // =======================================================
  // Señales de seguimiento
  // =======================================================

  saldoNegativo: boolean | null;
  movimientosInusuales: boolean | null;
  cuentaInactiva: boolean | null;
  requiereRevision: boolean | null;

  // =======================================================
  // Alertas
  // =======================================================

  cantidadAlertas: number | null;
  alertasCriticas: number | null;
  alertasAdvertencia: number | null;
  alertasInformativas: number | null;

  nivelAlerta: string | null;
  observaciones: string | null;
}


// =========================================================
// CDAT
// =========================================================

export interface ExpedienteCdat {

  // =======================================================
  // Identificación
  // =======================================================

  idCdat: number | null;
  idDatosPersonal: number | null;

  documento: string | null;
  nombreCompleto: string | null;

  // =======================================================
  // Certificado
  // =======================================================

  numeroCdat: string | null;

  codigoEstado: string | null;
  nombreEstado: string | null;

  // =======================================================
  // Estado operativo
  // =======================================================

  activo: boolean | null;
  cancelado: boolean | null;
  vencido: boolean | null;
  proximoVencer: boolean | null;

  // =======================================================
  // Fechas y plazo
  // =======================================================

  fechaConstitucion: string | null;
  fechaVencimiento: string | null;
  fechaCancelacion: string | null;

  fechaUltimoMovimiento: string | null;
  fechaUltimoPagoIntereses: string | null;

  plazoDias: number | null;
  diasTranscurridos: number | null;
  diasParaVencimiento: number | null;

  // =======================================================
  // Capital
  // =======================================================

  capitalInicial: number | null;

  capitalHistoricoInvertido: number | null;
  capitalVigente: number | null;

  saldoCapital: number | null;
  saldoTotal: number | null;

  // =======================================================
  // Rendimiento histórico
  // =======================================================

  interesesLiquidados: number | null;
  retencionFuente: number | null;
  rendimientoHistorico: number | null;

  cantidadPagosIntereses: number | null;
  cantidadMovimientos: number | null;

  // =======================================================
  // Tasas
  // =======================================================

  tasaEA: number | null;
  tasaNominal: number | null;

  // =======================================================
  // Titularidad
  // =======================================================

  conjunto: boolean | null;
  numeroTitulares: number | null;

  // =======================================================
  // Alertas
  // =======================================================

  cantidadAlertas: number | null;

  alertasCriticas: number | null;
  alertasAdvertencia: number | null;
  alertasInformativas: number | null;

  nivelAlerta: string | null;

  // =======================================================
  // Observaciones
  // =======================================================

  observaciones: string | null;
}

export interface ExpedienteCredito {

  // =========================================================
  // Identificación del asociado
  // =========================================================

  idDatosPersonal: number | null;

  tipoDocumento: string | null;
  nombreTipoDocumento: string | null;
  documento: string | null;

  nombres: string | null;
  primerApellido: string | null;
  segundoApellido: string | null;
  nombreCompleto: string | null;

  // =========================================================
  // Identificación del crédito
  // =========================================================

  idCredito: number | null;

  numeroCredito: string | null;
  numeroSolicitud: string | null;

  referenciaCredito: string | null;
  descripcionCredito: string | null;

  // =========================================================
  // Fábrica u origen
  // =========================================================

  idFabricaCredito: number | null;

  codigoFabricaCredito: string | null;
  nombreFabricaCredito: string | null;

  origenCredito: string | null;

  // =========================================================
  // Línea y producto
  // =========================================================

  idLineaCredito: number | null;

  codigoLineaCredito: string | null;
  nombreLineaCredito: string | null;

  idTipoProducto: number | null;
  codigoTipoProducto: string | null;
  nombreTipoProducto: string | null;

  idClasificacionCredito: number | null;
  codigoClasificacionCredito: string | null;
  nombreClasificacionCredito: string | null;

  // =========================================================
  // Estado del crédito
  // =========================================================

  idEstadoCartera: number | null;

  codigoEstadoCartera: string | null;
  nombreEstadoCartera: string | null;

  vigente: boolean;
  cancelado: boolean;
  castigado: boolean;
  enMora: boolean;

  reestructurado: boolean;
  novado: boolean;
  alivio: boolean;
  refinanciado: boolean;

  // =========================================================
  // Fechas principales
  // =========================================================

  fechaSolicitud: string | null;
  fechaAprobacion: string | null;
  fechaDesembolso: string | null;

  fechaPrimerVencimiento: string | null;
  fechaVencimiento: string | null;
  fechaProximoPago: string | null;

  fechaUltimoPago: string | null;
  fechaCancelacion: string | null;

  diasDesdeDesembolso: number;
  diasParaProximoPago: number;

  // =========================================================
  // Condiciones iniciales
  // =========================================================

  valorSolicitado: number;
  valorAprobado: number;
  valorDesembolsado: number;

  plazoInicial: number;
  plazo: number;

  numeroCuotasInicial: number;
  numeroCuotasPagadas: number;
  numeroCuotasPendientes: number;
  numeroCuotasVencidas: number;

  // =========================================================
  // Tipo de cuota y forma de pago
  // =========================================================

  idTipoCuota: number | null;
  codigoTipoCuota: string | null;
  nombreTipoCuota: string | null;

  idFormaPago: number | null;
  codigoFormaPago: string | null;
  nombreFormaPago: string | null;

  periodicidadPago: string | null;

  valorCuotaInicial: number;
  valorCuota: number;

  // =========================================================
  // Modalidad y tasas
  // =========================================================

  idModalidadInteres: number | null;

  codigoModalidadInteres: string | null;
  nombreModalidadInteres: string | null;

  tasaNominal: number;
  tasaEfectivaAnual: number;
  tasaMora: number;

  tasaRedescuento: number;
  margenRedescuento: number;

  // =========================================================
  // Saldos
  // =========================================================

  saldoActual: number;

  saldoInteresCorriente: number;
  saldoInteresMora: number;

  saldoSeguro: number;
  saldoOtrosConceptos: number;

  saldoTotal: number;

  capitalVencido: number;
  interesesVencidos: number;
  valorVencidoTotal: number;

  // =========================================================
  // Pagos
  // =========================================================

  totalPagadoCapital: number;
  totalPagadoIntereses: number;
  totalPagadoMora: number;
  totalPagadoSeguros: number;
  totalPagadoOtros: number;

  totalPagado: number;
  ultimoValorPagado: number;

  // =========================================================
  // Mora
  // =========================================================

  diasMora: number;
  edadMora: number;

  rangoMora: string | null;

  moraLeve: boolean;
  moraModerada: boolean;
  moraGrave: boolean;

  // =========================================================
  // Riesgo
  // =========================================================

  edadRiesgoInicial: string | null;
  edadRiesgo: string | null;

  edadRiesgoEvaluada: string | null;
  edadRiesgoFinal: string | null;

  codigoCalificacion: string | null;
  nombreCalificacion: string | null;

  resultadoEvaluacion: string | null;
  accionEvaluacion: string | null;

  fechaUltimaEvaluacion: string | null;
  fechaProximaEvaluacion: string | null;

  requiereEvaluacion: boolean;
  evaluacionVencida: boolean;

  // =========================================================
  // Garantía principal
  // =========================================================

  idTipoGarantiaCredito: number | null;

  codigoGarantia: string | null;
  nombreGarantia: string | null;

  garantiaIdonea: boolean;
  tieneGarantiaReal: boolean;

  cantidadGarantias: number;

  valorGarantias: number;
  valorGarantiasAdmisible: number;

  porcentajeCoberturaGarantias: number;

  excesoGarantia: number;
  faltanteGarantia: number;

  garantiaSuficiente: boolean;
  garantiaInsuficiente: boolean;

  // =========================================================
  // Estado jurídico
  // =========================================================

  idEstadoJuridico: number | null;

  codigoEstadoJuridico: string | null;
  nombreEstadoJuridico: string | null;

  enCobranza: boolean;
  prejuridico: boolean;
  juridico: boolean;
  insolvente: boolean;

  fechaInicioCobranza: string | null;
  fechaInicioJuridico: string | null;

  abogadoResponsable: string | null;
  numeroProcesoJuridico: string | null;

  // =========================================================
  // Seguimiento
  // =========================================================

  requiereRevision: boolean;
  requiereGestionCobranza: boolean;
  requiereActualizacionGarantias: boolean;

  cantidadAlertas: number;

  alertasCriticas: number;
  alertasAdvertencia: number;
  alertasInformativas: number;

  nivelAlerta: string | null;
  resumenAlertas: string | null;
  motivoRevision: string | null;

  // =========================================================
  // Agencia y responsable
  // =========================================================

  idAgencia: number | null;

  codigoAgencia: string | null;
  nombreAgencia: string | null;

  idAsesor: number | null;
  nombreAsesor: string | null;

  // =========================================================
  // Observaciones
  // =========================================================

  observaciones: string | null;

}

export interface ExpedienteBienInmueble {
  idBienPersona: number;
  idDatosPersonal: number;

  porcentajePropiedad: number;
  valorParticipacion: number;

  titularPrincipal: boolean;
  propiedadCompartida: boolean;
  cantidadPropietarios: number;

  documento: string | null;
  nombreCompleto: string | null;

  idBien: number;
  descripcionGeneral: string | null;

  valorComercial: number;
  valorGravamen: number;
  valorNeto: number;

  idBienInmueble: number;
  codigoTipoInmueble: string | null;
  nombreTipoInmueble: string | null;

  numeroMatriculaInmobiliaria: string | null;
  cedulaCatastral: string | null;

  direccion: string | null;
  barrioVereda: string | null;
  ubicacionCompleta: string | null;

  nombreCiudad: string | null;
  nombreDepartamento: string | null;
  nombrePais: string | null;

  nombreTipoGravamen: string | null;
  tieneGravamen: boolean;

  fechaAvaluo: string | null;
  valorAvaluoComercial: number;
  valorAvaluoCatastral: number;

  tieneAvaluo: boolean;
  avaluoVigente: boolean;
  avaluoProximoVencer: boolean;
  avaluoVencido: boolean;

  aseguradora: string | null;
  numeroPoliza: string | null;
  valorAsegurado: number;

  fechaInicioSeguro: string | null;
  fechaVencimientoSeguro: string | null;

  tieneSeguro: boolean;
  seguroVigente: boolean;
  seguroProximoVencer: boolean;
  seguroVencido: boolean;

  vinculadoComoGarantia: boolean;
  cantidadCreditosGarantizados: number;
  saldoCreditosGarantizados: number;

  informacionCompleta: boolean;
  porcentajeCompletitud: number;

  activo: boolean;
  requiereActualizacion: boolean;
  requiereRevision: boolean;

  cantidadAlertas: number;
  alertasCriticas: number;
  alertasAdvertencia: number;
  alertasInformativas: number;
  nivelAlerta: string | null;

  observaciones: string | null;

  [campo: string]: unknown;
}

export interface ExpedienteBienVehiculo {
  idBien: number;
  idDatosPersonal: number;
  descripcionGeneral: string | null;
  valorComercial: number;
  valorGravamen: number;
  valorNeto: number;
  placa: string | null;
  marca: string | null;
  linea: string | null;
  modelo: number | null;
  activo: boolean;
  requiereRevision: boolean;

  [campo: string]: unknown;
}

export interface ExpedienteBienMaquinaria {
  idBien: number;
  idDatosPersonal: number;
  descripcionGeneral: string | null;
  valorComercial: number;
  valorGravamen: number;
  valorNeto: number;
  marca: string | null;
  modelo: string | null;
  serie: string | null;
  activo: boolean;
  requiereRevision: boolean;

  [campo: string]: unknown;
}

export interface ExpedienteBienInversion {
  idBien: number;
  idDatosPersonal: number;
  descripcionGeneral: string | null;
  valorComercial: number;
  valorGravamen: number;
  valorNeto: number;
  entidad: string | null;
  numeroTitulo: string | null;
  fechaVencimiento: string | null;
  activo: boolean;
  requiereRevision: boolean;

  [campo: string]: unknown;
}

export interface ExpedienteGarantia {
  idGarantia: number;
  idCredito: number | null;
  idBien: number | null;

  tipoGarantia: string | null;
  descripcionGarantia: string | null;

  valorGarantia: number;
  saldoRespaldado: number;
  porcentajeCobertura: number;

  activa: boolean;
  suficiente: boolean;
  requiereRevision: boolean;

  [campo: string]: unknown;
}

export interface ExpedienteAlerta {
  idAlerta: number | null;
  codigoAlerta: string;
  titulo: string;
  descripcion: string | null;

  idDatosPersonal: number;
  documento: string | null;
  nombreCompleto: string | null;

  modulo: string | null;
  submodulo: string | null;

  idCredito: number | null;
  idCuentaAhorro: number | null;
  idCdat: number | null;
  idBien: number | null;
  idGarantia: number | null;

  nivel: string;
  prioridad: string;
  tipoAlerta: string;
  categoria: string;

  codigoEstado: string;
  nombreEstado: string;

  activa: boolean;
  abierta: boolean;
  enGestion: boolean;
  atendida: boolean;
  descartada: boolean;
  vencida: boolean;

  fechaGeneracion: string | null;
  fechaPrimeraDeteccion: string | null;
  fechaUltimaDeteccion: string | null;
  fechaVencimiento: string | null;

  idUsuarioResponsable: number | null;
  nombreUsuarioResponsable: string | null;

  idAgencia: number | null;
  codigoAgencia: string | null;
  nombreAgencia: string | null;

  requiereGestion: boolean;
  requiereSeguimiento: boolean;
  requiereEscalamiento: boolean;
  bloqueante: boolean;

  ordenVisual: number;
  icono: string | null;
  color: string | null;

  rutaFrontend: string | null;
  parametroRuta: string | null;
  etiquetaAccion: string | null;

  permiteGestionar: boolean;
  permiteDescartar: boolean;
  permiteCerrar: boolean;

  [campo: string]: unknown;
}

// =========================================================
// INDICADORES DEL EXPEDIENTE
// =========================================================

export interface ExpedienteIndicadores {

  // =======================================================
  // Identificación
  // =======================================================

  idDatosPersonal: number | null;

  documento: string | null;
  nombreCompleto: string | null;

  // =======================================================
  // Cantidades por producto
  // =======================================================

  cantidadCuentasAhorro: number | null;
  cantidadCdats: number | null;
  cantidadCreditos: number | null;
  cantidadBienes: number | null;
  cantidadGarantias: number | null;

  // =======================================================
  // Ahorros, inversiones y patrimonio
  // =======================================================

  totalAportes: number | null;
  totalAhorros: number | null;
  totalCdats: number | null;
  totalBienes: number | null;

  totalPatrimonio: number | null;
  totalObligaciones: number | null;
  patrimonioNeto: number | null;

  // =======================================================
  // Cartera
  // =======================================================

  saldoCapital: number | null;
  saldoTotalCredito: number | null;

  creditosEnMora: number | null;
  diasMayorMora: number | null;

  mayorEdadRiesgo: string | null;
  provisionTotal: number | null;

  // =======================================================
  // Garantías
  // =======================================================

  valorGarantias: number | null;
  coberturaGarantias: number | null;

  garantiasSuficientes: boolean | null;

  // =======================================================
  // Riesgo general
  // =======================================================

  nivelRiesgo: string | null;
  colorRiesgo: string | null;

  // =======================================================
  // Alertas
  // =======================================================

  cantidadAlertasCriticas: number | null;
  cantidadAlertasAdvertencia: number | null;
  cantidadAlertasInformativas: number | null;

  // =======================================================
  // Completitud de información
  // =======================================================

  sarlaftVigente: boolean | null;
  contactoCompleto: boolean | null;
  informacionFinancieraCompleta: boolean | null;
  documentacionCompleta: boolean | null;

  // =======================================================
  // Indicadores porcentuales
  // =======================================================

  porcentajeCoberturaPatrimonial: number | null;
  porcentajeReciprocidad: number | null;
  porcentajeEndeudamiento: number | null;
  porcentajeCompromisoIngresos: number | null;

  // =======================================================
  // Resultado ejecutivo
  // =======================================================

  estadoGeneral: string | null;
  resumenEjecutivo: string | null;

  fechaActualizacion: string | null;
}

export interface ExpedienteEstadoCarga {

  cargaCompleta: boolean;
  presentaErrores: boolean;
  presentaAdvertencias: boolean;

  resumenGeneralCargado: boolean;
  afiliacionCargada: boolean;
  contactoCargado: boolean;
  informacionFinancieraCargada: boolean;
  sarlaftCargado: boolean;
  participacionInstitucionalCargada: boolean;

  cuentasAhorroCargadas: boolean;
  cdatsCargados: boolean;
  creditosCargados: boolean;

  bienesInmueblesCargados: boolean;
  bienesVehiculosCargados: boolean;
  bienesMaquinariaCargados: boolean;
  bienesInversionesCargados: boolean;

  garantiasCargadas: boolean;
  indicadoresCalculados: boolean;
  alertasCalculadas: boolean;

  cantidadBloquesCargados: number;
  cantidadBloquesConError: number;

  advertencias: string[];
  errores: string[];
}
