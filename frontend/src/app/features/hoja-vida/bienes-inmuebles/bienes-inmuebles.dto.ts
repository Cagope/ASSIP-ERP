export interface BienInmueble {
  idBienPersona?: number | null;
  idDatosPersonal: number;
  porcentajePropiedad: number;

  idBien?: number | null;
  idTipoBien: number;
  codigoTipoBien?: string | null;
  nombreTipoBien?: string | null;
  descripcionGeneral: string;
  valorComercial: number;
  valorGravamen: number;

  idBienInmueble?: number | null;
  idTipoInmueble: number;
  codigoTipoInmueble?: string | null;
  nombreTipoInmueble?: string | null;

  numeroMatriculaInmobiliaria?: string | null;
  cedulaCatastral?: string | null;

  idPais: number;
  nombrePais?: string | null;

  idDepartamento: number;
  nombreDepartamento?: string | null;

  idCiudad: number;
  nombreCiudad?: string | null;

  direccion: string;
  barrioVereda?: string | null;

  areaTerreno?: number | null;
  areaConstruida?: number | null;

  numeroEscritura?: string | null;
  fechaEscritura?: string | null;
  notaria?: string | null;

  idPaisNotaria?: number | null;
  nombrePaisNotaria?: string | null;

  idDepartamentoNotaria?: number | null;
  nombreDepartamentoNotaria?: string | null;

  idCiudadNotaria?: number | null;
  nombreCiudadNotaria?: string | null;

  idTipoGravamen: number;
  nombreGravamen?: string | null;

  observaciones?: string | null;

  tipoDocumento?: string | null;
  nombreTipoDocumento?: string | null;
  documento?: string | null;
  nombreCompleto?: string | null;

  idBienInmuebleAvaluo?: number | null;
  fechaAvaluo?: string | null;
  valorAvaluoComercial?: number | null;
  valorAvaluoCatastral?: number | null;
  entidadAvaluadora?: string | null;
  numeroInforme?: string | null;
  observacionesAvaluo?: string | null;

  idBienInmuebleSeguro?: number | null;
  aseguradora?: string | null;
  numeroPoliza?: string | null;
  valorAsegurado?: number | null;
  fechaInicioSeguro?: string | null;
  fechaVencimientoSeguro?: string | null;
  estadoSeguro?: string | null;
  nombreEstadoSeguro?: string | null;
  observacionesSeguro?: string | null;

  valorNetoBien?: number | null;
  valorPropiedadAsociado?: number | null;
  valorGravamenAsociado?: number | null;
  valorNetoAsociado?: number | null;

  fechaCreacion?: string | null;
  fechaEdicion?: string | null;
}

export function nuevoBienInmueble(idDatosPersonal: number): BienInmueble {
  return {
    idDatosPersonal,
    porcentajePropiedad: 100,

    idTipoBien: 1,
    descripcionGeneral: '',
    valorComercial: 0,
    valorGravamen: 0,

    idTipoInmueble: 1,

    idPais: 40,
    idDepartamento: 0,
    idCiudad: 0,

    direccion: '',

    areaTerreno: 0,
    areaConstruida: 0,

    idPaisNotaria: 40,
    idDepartamentoNotaria: null,
    idCiudadNotaria: null,

    idTipoGravamen: 1,
    observaciones: null
  };
}
