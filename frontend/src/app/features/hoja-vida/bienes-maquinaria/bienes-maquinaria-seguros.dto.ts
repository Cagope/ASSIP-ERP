export interface BienMaquinariaSeguro {

  idBienMaquinariaSeguro?: number | null;

  idBien: number;

  aseguradora?: string | null;

  numeroPoliza?: string | null;

  valorAsegurado: number;

  fechaInicioSeguro?: string | null;

  fechaVencimientoSeguro?: string | null;

  estadoSeguro: string;

  observaciones?: string | null;

  fkSeguridadCreacion?: number | null;

  fechaCreacion?: string | null;

  fkSeguridadEdicion?: number | null;

  fechaEdicion?: string | null;

}

export function nuevoSeguro(
  idBien: number
): BienMaquinariaSeguro {

  return {

    idBien,

    aseguradora: null,

    numeroPoliza: null,

    valorAsegurado: 0,

    fechaInicioSeguro: null,

    fechaVencimientoSeguro: null,

    estadoSeguro: 'A',

    observaciones: null

  };

}
