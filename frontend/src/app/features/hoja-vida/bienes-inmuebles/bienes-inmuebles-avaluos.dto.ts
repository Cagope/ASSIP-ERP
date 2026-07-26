export interface BienInmuebleAvaluo {

  idBienInmuebleAvaluo?: number | null;
  idBien: number;

  /*
   * Información del avalúo.
   */
  fechaAvaluo: string;

  vigenciaAnios: number;
  fechaVencimientoAvaluo?: string | null;

  /*
   * Valores por componente.
   */
  valorTerreno: number;
  valorConstruccion: number;
  valorCultivos: number;
  valorOtros: number;

  /*
   * Valores generales.
   */
  valorAvaluoComercial: number;
  valorAvaluoCatastral: number;

  /*
   * Valores calculados (Reporting).
   */
  valorComponentesAvaluo?: number | null;
  valorAvaluoPropiedadAsociado?: number | null;

  /*
   * Estado del avalúo (Reporting).
   */
  nombreEstadoAvaluo?: string | null;

  /*
   * Información del perito.
   */
  entidadAvaluadora?: string | null;
  numeroInforme?: string | null;

  observaciones?: string | null;

  /*
   * Auditoría.
   */
  fkSeguridadCreacion?: number | null;
  fechaCreacion?: string | null;

  fkSeguridadEdicion?: number | null;
  fechaEdicion?: string | null;
}

export function nuevoAvaluo(
  idBien: number
): BienInmuebleAvaluo {

  return {
    idBien,

    fechaAvaluo: obtenerFechaActual(),

    vigenciaAnios: 3,
    fechaVencimientoAvaluo: null,

    valorTerreno: 0,
    valorConstruccion: 0,
    valorCultivos: 0,
    valorOtros: 0,

    valorAvaluoComercial: 0,
    valorAvaluoCatastral: 0,

    entidadAvaluadora: null,
    numeroInforme: null,
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
