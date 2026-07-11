export interface BienInversion {

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

  idBienInversion?: number | null;

  idTipoInversion: number;
  nombreTipoInversion?: string | null;

  entidad?: string | null;
  numeroTitulo?: string | null;

  fechaInversion?: string | null;
  fechaVencimiento?: string | null;

  valorNominal: number;
  valorActual: number;
  tasaRendimiento?: number | null;

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

export function nuevoBienInversion(
  idDatosPersonal: number
): BienInversion {

  return {

    idDatosPersonal,
    porcentajePropiedad: 100,

    idTipoBien: 4,

    descripcionGeneral: '',

    valorComercial: 0,
    valorGravamen: 0,

    idTipoInversion: 1,

    entidad: '',
    numeroTitulo: '',

    fechaInversion: null,
    fechaVencimiento: null,

    valorNominal: 0,
    valorActual: 0,
    tasaRendimiento: 0,

    idTipoGravamen: 1,

    observaciones: null
  };
}
