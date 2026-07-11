export interface BienVehiculo {

  idBienPersona?: number | null;
  idDatosPersonal: number;
  porcentajePropiedad: number;

  idBien?: number | null;
  idTipoBien: number;
  codigoTipoBien?: string | null;
  nombreTipoBien?: string |null;
  descripcionGeneral: string;
  valorComercial: number;
  valorGravamen: number;

  idBienVehiculo?: number | null;

  idTipoVehiculo: number;
  nombreTipoVehiculo?: string | null;

  placa: string;
  marca?: string | null;
  linea?: string | null;
  modelo?: number | null;
  color?: string | null;

  numeroMotor?: string | null;
  numeroChasis?: string | null;
  numeroSerie?: string | null;

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

export function nuevoBienVehiculo(
  idDatosPersonal: number
): BienVehiculo {

  return {

    idDatosPersonal,
    porcentajePropiedad: 100,

    idTipoBien: 2,

    descripcionGeneral: '',

    valorComercial: 0,
    valorGravamen: 0,

    idTipoVehiculo: 1,

    placa: '',
    marca: '',
    linea: '',
    modelo: new Date().getFullYear(),
    color: '',

    numeroMotor: '',
    numeroChasis: '',
    numeroSerie: '',

    idTipoGravamen: 1,

    observaciones: null
  };
}
