export interface BienInmueble {

  /*
   * Relación entre el bien y el asociado.
   */
  idBienPersona?: number | null;
  idDatosPersonal: number;
  porcentajePropiedad: number;

  /*
   * Información general del bien.
   */
  idBien?: number | null;

  idTipoBien: number;
  codigoTipoBien?: string | null;
  nombreTipoBien?: string | null;

  descripcionGeneral: string;

  fechaAdquisicion: string;
  estadoBien: string;
  nombreEstadoBien?: string | null;
  fechaEstado: string;

  valorComercial: number;
  valorGravamen: number;

  observacionesBien?: string | null;

  /*
   * Información específica del inmueble.
   */
  idBienInmueble?: number | null;

  idTipoInmueble: number;
  codigoTipoInmueble?: string | null;
  nombreTipoInmueble?: string | null;

  idTipoZonaInmueble: number;
  codigoTipoZonaInmueble?: string | null;
  nombreTipoZonaInmueble?: string | null;

  numeroMatriculaInmobiliaria?: string | null;
  cedulaCatastral?: string | null;

  /*
   * Ubicación del inmueble.
   */
  idPais: number;
  nombrePais?: string | null;

  idDepartamento: number;
  nombreDepartamento?: string | null;

  idCiudad: number;
  nombreCiudad?: string | null;

  direccion: string;
  barrioVereda?: string | null;

  /*
   * Áreas del inmueble.
   */
  areaTerreno?: number | null;
  areaConstruida?: number | null;

  /*
   * Información de la escritura.
   */
  numeroEscritura?: string | null;
  fechaEscritura?: string | null;
  fechaRegistroEscritura?: string | null;

  notaria?: string | null;
  oficinaRegistro?: string | null;

  idPaisNotaria?: number | null;
  nombrePaisNotaria?: string | null;

  idDepartamentoNotaria?: number | null;
  nombreDepartamentoNotaria?: string | null;

  idCiudadNotaria?: number | null;
  nombreCiudadNotaria?: string | null;

  /*
   * Gravamen del inmueble.
   */
  idTipoGravamen: number;
  nombreGravamen?: string | null;

  /*
   * Observaciones propias del inmueble.
   */
  observaciones?: string | null;

  /*
   * Información del asociado.
   */
  tipoDocumento?: string | null;
  nombreTipoDocumento?: string | null;
  documento?: string | null;
  nombreCompleto?: string | null;

  /*
   * Último avalúo del inmueble.
   */
  idBienInmuebleAvaluo?: number | null;

  fechaAvaluo?: string | null;
  vigenciaAnios?: number | null;
  fechaVencimientoAvaluo?: string | null;
  nombreEstadoAvaluo?: string | null;

  valorTerreno?: number | null;
  valorConstruccion?: number | null;
  valorCultivos?: number | null;
  valorOtros?: number | null;

  valorAvaluoComercial?: number | null;
  valorAvaluoCatastral?: number | null;
  valorComponentesAvaluo?: number | null;
  valorAvaluoPropiedadAsociado?: number | null;

  entidadAvaluadora?: string | null;
  numeroInforme?: string | null;
  observacionesAvaluo?: string | null;

  /*
   * Último seguro del inmueble.
   */
  idBienInmuebleSeguro?: number | null;

  aseguradora?: string | null;
  numeroPoliza?: string | null;
  valorAsegurado?: number | null;

  fechaInicioSeguro?: string | null;
  fechaVencimientoSeguro?: string | null;

  estadoSeguro?: string | null;
  nombreEstadoSeguro?: string | null;

  observacionesSeguro?: string | null;

  /*
   * Valores calculados según el porcentaje
   * de propiedad del asociado.
   */
  valorNetoBien?: number | null;
  valorPropiedadAsociado?: number | null;
  valorGravamenAsociado?: number | null;
  valorNetoAsociado?: number | null;

  /*
   * Auditoría.
   */
  fechaCreacion?: string | null;
  fechaEdicion?: string | null;
}

export function nuevoBienInmueble(
  idDatosPersonal: number
): BienInmueble {

  const fechaActual = obtenerFechaActual();

  return {
    idBienPersona: null,
    idDatosPersonal,
    porcentajePropiedad: 100,

    idBien: null,

    /*
     * Tipo de bien:
     * 1 = INMUEBLE
     */
    idTipoBien: 1,

    descripcionGeneral: '',
    fechaAdquisicion: '',
    estadoBien: 'A',
    fechaEstado: fechaActual,

    valorComercial: 0,
    valorGravamen: 0,

    observacionesBien: null,

    idBienInmueble: null,

    idTipoInmueble: 0,
    idTipoZonaInmueble: 0,

    numeroMatriculaInmobiliaria: null,
    cedulaCatastral: null,

    /*
     * Colombia.
     */
    idPais: 40,
    idDepartamento: 0,
    idCiudad: 0,

    direccion: '',
    barrioVereda: null,

    areaTerreno: 0,
    areaConstruida: 0,

    numeroEscritura: null,
    fechaEscritura: null,
    fechaRegistroEscritura: null,

    notaria: null,
    oficinaRegistro: null,

    idPaisNotaria: 40,
    idDepartamentoNotaria: null,
    idCiudadNotaria: null,

    /*
     * Tipo de gravamen:
     * 1 = Ninguno
     */
    idTipoGravamen: 1,

    observaciones: null
  };
}

function obtenerFechaActual(): string {
  const fecha = new Date();

  const anio = fecha.getFullYear();
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');

  return `${anio}-${mes}-${dia}`;
}
