export interface BienInmuebleSeguro {

  idBienInmuebleSeguro?: number | null;

  idBien: number;

  /*
   * Información de la póliza.
   */
  aseguradora?: string | null;

  numeroPoliza?: string | null;

  valorAsegurado: number;

  /*
   * Vigencia.
   */
  fechaInicioSeguro?: string | null;

  fechaVencimientoSeguro?: string | null;

  estadoSeguro: string;
  nombreEstadoSeguro?: string | null;

  /*
   * Observaciones.
   */
  observaciones?: string | null;

  /*
   * Auditoría.
   */
  fkSeguridadCreacion?: number | null;
  fechaCreacion?: string | null;

  fkSeguridadEdicion?: number | null;
  fechaEdicion?: string | null;
}

export function nuevoSeguro(
  idBien: number
): BienInmuebleSeguro {

  return {

    idBien,

    aseguradora: null,

    numeroPoliza: null,

    valorAsegurado: 0,

    fechaInicioSeguro: obtenerFechaActual(),

    fechaVencimientoSeguro: null,

    estadoSeguro: 'A',

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
