
/** Parámetros de consulta del Estado de Excedentes o Pérdidas individual. */
export interface PyGIndividualFiltro {
  idAgencia: number;
  ano: number;
  mes: number;
  incluirDetalle: boolean;
}

/** Tipos de renglón definidos por el servicio de PyG individual. */
export type PyGIndividualTipoFila =
  | 'GRUPO'
  | 'CUENTA'
  | 'TOTAL'
  | 'RESULTADO';

/** Cuenta contable, agrupación o resultado calculado del período. */
export interface PyGIndividualFila {
  codigo: string;
  nombre: string;
  tipo: PyGIndividualTipoFila;
  nivel: number;
  valor: number;
}

/** Resultado del Estado de Excedentes o Pérdidas individual. */
export interface PyGIndividualRespuesta extends PyGIndividualFiltro {
  filas: PyGIndividualFila[];
}

/** Opciones de exportación del informe PDF. */
export interface PyGIndividualOpcionesPdf {
  incluirFirmas: boolean;
}
