// =========================================================
// MATRIZ DE RODAMIENTO
//
// Replica la lógica del proceso histórico:
//
// - filas    = categoría del período anterior;
// - columnas = categoría de los datos de partida;
// - población = créditos existentes en los datos de partida
//               con saldo > 0;
// - únicamente entran a la matriz los créditos que también
//   existen en el período de comparación.
//
// La matriz se presenta paralelamente:
//
// 1. Por cantidades.
// 2. Por valores.
//
// Cada celda A-E puede consultar su detalle individual.
// =========================================================


// =========================================================
// TIPO DE DATOS DE PARTIDA
// =========================================================

export type MatrizRodamientoTipoPartida =
  | 'ACTUAL'
  | 'CORTE';


// =========================================================
// CATEGORÍAS
// =========================================================

export type MatrizRodamientoCategoria =
  | 'A'
  | 'B'
  | 'C'
  | 'D'
  | 'E';


// =========================================================
// CORTE DISPONIBLE
// =========================================================

export interface MatrizRodamientoCorte {

  fechaCorte: string;

  cantidadCreditos: number;
}


// =========================================================
// CELDA DE MATRIZ
//
// 1 celda = transición:
//
// categoría anterior → categoría partida
//
// Ejemplo:
//
// B → D
// =========================================================

export interface MatrizRodamientoCelda {

  categoriaAnterior: MatrizRodamientoCategoria;

  categoriaPartida: MatrizRodamientoCategoria;

  cantidad: number;

  porcentajeCantidad: number;

  valor: number;

  porcentajeValor: number;
}


// =========================================================
// FILA DE MATRIZ
//
// 1 fila = categoría del período anterior.
//
// Contiene las cinco posibles categorías de destino:
// A, B, C, D y E.
// =========================================================

export interface MatrizRodamientoFila {

  categoriaAnterior: MatrizRodamientoCategoria;

  celdas: MatrizRodamientoCelda[];

  totalCantidad: number;

  totalValor: number;


  // =======================================================
  // PROBABILIDADES POR CANTIDAD
  // =======================================================

  probabilidadMejoraCantidad: number;

  probabilidadPermanenciaCantidad: number;

  probabilidadDeterioroCantidad: number;


  // =======================================================
  // PROBABILIDADES POR VALOR
  // =======================================================

  probabilidadMejoraValor: number;

  probabilidadPermanenciaValor: number;

  probabilidadDeterioroValor: number;
}


// =========================================================
// RESULTADO GENERAL DE LA MATRIZ
// =========================================================

export interface MatrizRodamiento {

  // =======================================================
  // PERÍODOS
  // =======================================================

  tipoPartida: MatrizRodamientoTipoPartida;

  fechaPartida: string;

  fechaComparacion: string;


  // =======================================================
  // POBLACIÓN
  // =======================================================

  cantidadPoblacionPartida: number;

  cantidadCreditosMatriz: number;


  // =======================================================
  // MATRIZ
  // =======================================================

  filas: MatrizRodamientoFila[];


  // =======================================================
  // TOTALES POR CATEGORÍA DE DESTINO
  // =======================================================

  totalesCantidadPorCategoria: Record<
    MatrizRodamientoCategoria,
    number
  >;

  totalesValorPorCategoria: Record<
    MatrizRodamientoCategoria,
    number
  >;


  // =======================================================
  // TOTALES GENERALES
  // =======================================================

  totalCantidad: number;

  totalValor: number;
}


// =========================================================
// DETALLE INDIVIDUAL DE UNA CELDA
//
// Corresponde exactamente a los créditos que componen
// una transición determinada de la matriz.
//
// Ejemplo:
//
// categoríaAnterior = B
// categoríaPartida  = D
// =========================================================

export interface MatrizRodamientoDetalle {

  // =======================================================
  // IDENTIFICACIÓN DEL CRÉDITO
  // =======================================================

  idCarteraCredito: number;

  idAgencia: number;

  idLineaCredito: number;

  codigoLineaCredito: string | null;

  nombreLineaCredito: string | null;

  pagareCartera: string;

  idDatosPersonal: number;


  // =======================================================
  // IDENTIFICACIÓN DEL ASOCIADO
  // =======================================================

  tipoDocumento: string | null;

  documento: string;

  nombreCompleto: string;


  // =======================================================
  // CONTACTO
  // =======================================================

  telefono: string | null;

  celular: string | null;

  correo: string | null;


  // =======================================================
  // ORIGINACIÓN
  // =======================================================

  fechaDesembolso: string | null;


  // =======================================================
  // PERÍODO ANTERIOR / COMPARACIÓN
  // =======================================================

  fechaComparacion: string;

  diasMoraAnterior: number;

  categoriaAnterior: MatrizRodamientoCategoria;

  saldoAnterior: number;


  // =======================================================
  // DATOS DE PARTIDA
  // =======================================================

  tipoPartida: MatrizRodamientoTipoPartida;

  fechaPartida: string;

  diasMoraPartida: number;

  categoriaPartida: MatrizRodamientoCategoria;

  saldoPartida: number;
}


// =========================================================
// PARÁMETROS PARA CALCULAR MATRIZ
// =========================================================

export interface MatrizRodamientoConsulta {

  tipoPartida: MatrizRodamientoTipoPartida;

  fechaComparacion: string;

  fechaPartida?: string | null;
}


// =========================================================
// PARÁMETROS PARA CONSULTAR DETALLE DE CELDA
// =========================================================

export interface MatrizRodamientoDetalleConsulta {

  tipoPartida: MatrizRodamientoTipoPartida;

  fechaComparacion: string;

  fechaPartida?: string | null;

  categoriaAnterior: MatrizRodamientoCategoria;

  categoriaPartida: MatrizRodamientoCategoria;
}


// =========================================================
// CATEGORÍAS FIJAS DE LA MATRIZ
// =========================================================

export const MATRIZ_RODAMIENTO_CATEGORIAS:
  MatrizRodamientoCategoria[] = [
    'A',
    'B',
    'C',
    'D',
    'E'
  ];
