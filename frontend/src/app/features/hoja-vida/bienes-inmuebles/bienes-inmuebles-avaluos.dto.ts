export interface BienInmuebleAvaluo {
  idBienInmuebleAvaluo?: number | null;
  idBien: number;

  fechaAvaluo: string;
  vigenciaAnios: number;
  fechaVencimientoAvaluo?: string | null;

  valorAvaluoComercial: number;
  valorAvaluoCatastral: number;

  entidadAvaluadora?: string | null;
  numeroInforme?: string | null;
  observaciones?: string | null;

  fkSeguridadCreacion?: number | null;
  fechaCreacion?: string | null;
  fkSeguridadEdicion?: number | null;
  fechaEdicion?: string | null;
}

export function nuevoAvaluo(idBien: number): BienInmuebleAvaluo {
  return {
    idBien,
    fechaAvaluo: new Date().toISOString().substring(0, 10),
    vigenciaAnios: 3,
    fechaVencimientoAvaluo: null,
    valorAvaluoComercial: 0,
    valorAvaluoCatastral: 0,
    entidadAvaluadora: null,
    numeroInforme: null,
    observaciones: null
  };
}
