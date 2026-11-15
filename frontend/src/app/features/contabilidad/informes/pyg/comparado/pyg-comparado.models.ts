
/** Parámetros de consulta del Estado de Excedentes o Pérdidas comparado. */
export interface PyGComparadoFiltro {
  idAgencia: number;
  anoPeriodo1: number;
  mesPeriodo1: number;
  anoPeriodo2: number;
  mesPeriodo2: number;
}

/** Tipos de renglón definidos por el servicio de PyG comparado. */
export type PyGComparadoTipoFila = 'GRUPO' | 'TOTAL' | 'RESULTADO';

/** Renglón contable o resultado calculado del PyG comparado. */
export interface PyGComparadoFila {
  codigo: string;
  nombre: string;
  tipo: PyGComparadoTipoFila;
  periodo1: number;
  periodo2: number;
  variacion: number;
  porcentaje: number | null;
}

/** Resultado de la comparación entre dos períodos contables. */
export interface PyGComparadoRespuesta extends PyGComparadoFiltro {
  filas: PyGComparadoFila[];
}
