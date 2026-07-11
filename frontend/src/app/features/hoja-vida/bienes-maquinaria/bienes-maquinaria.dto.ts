export interface BienMaquinaria {

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

  idBienMaquinaria?: number | null;

  idTipoMaquinaria: number;
  nombreTipoMaquinaria?: string | null;

  marca?: string | null;
  modelo?: string | null;
  serial?: string | null;
  referencia?: string | null;
  descripcionTecnica?: string | null;
  ubicacion?: string | null;
  estadoOperativo?: string | null;
  fechaAdquisicion?: string | null;

  idTipoGravamen: number;
  nombreGravamen?: string | null;

  observaciones?: string | null;

  tipoDocumento?: string | null;
  nombreTipoDocumento?: string | null;
  documento?: string | null;
  nombreCompleto?: string | null;

  valorNetoBien?: number | null;
  valorPropiedadAsociado?: number | null;
  valorGravamenAsociado?: number | null;
  valorNetoAsociado?: number | null;

  fechaCreacion?: string | null;
  fechaEdicion?: string | null;
}

export function nuevoBienMaquinaria(
  idDatosPersonal: number
): BienMaquinaria {

  return {

    idDatosPersonal,
    porcentajePropiedad: 100,

    // Tipo de bien: Maquinaria
    idTipoBien: 3,

    descripcionGeneral: '',

    valorComercial: 0,
    valorGravamen: 0,

    idTipoMaquinaria: 1,

    marca: '',
    modelo: '',
    serial: '',
    referencia: '',
    descripcionTecnica: '',
    ubicacion: '',
    estadoOperativo: '',
    fechaAdquisicion: null,

    idTipoGravamen: 1,

    observaciones: null
  };
}
