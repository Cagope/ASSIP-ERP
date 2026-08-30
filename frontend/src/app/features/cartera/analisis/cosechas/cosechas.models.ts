// =========================================================
// ANÁLISIS DE COSECHAS
// =========================================================


// =========================================================
// INDICADORES DISPONIBLES
// =========================================================

export type CosechaIndicador =
  | 'SALDO_REMANENTE'
  | 'MORA_30'
  | 'MORA_60'
  | 'MORA_90'
  | 'MORA_180';


// =========================================================
// CATÁLOGO
// =========================================================

export interface CosechaCatalogo {

  id: number;

  codigo: string;

  nombre: string;
}


// =========================================================
// CORTE DISPONIBLE
// =========================================================

export interface CosechaCorte {

  fechaCorte: string;

  cantidadCreditos: number;
}


// =========================================================
// CELDA
// =========================================================

export interface CosechaCelda {

  // -------------------------------------------------------
  // Identificación
  // -------------------------------------------------------

  cosecha: string;

  fechaCorte: string;

  mob: number;


  // -------------------------------------------------------
  // Originación
  // -------------------------------------------------------

  cantidadOriginada: number;

  valorInicialOriginal: number;

  valorDesembolsadoOriginal: number;


  // -------------------------------------------------------
  // Presencia
  // -------------------------------------------------------

  cantidadPresentesCorte: number;

  cantidadSinPresenciaCorte: number;

  porcentajePresentesCorte: number;

  porcentajeSinPresenciaCorte: number;


  // -------------------------------------------------------
  // Saldo
  // -------------------------------------------------------

  cantidadConSaldo: number;

  saldoCapital: number;

  porcentajeSaldoRemanente: number;


  // -------------------------------------------------------
  // Mora 30+
  // -------------------------------------------------------

  cantidadMora30: number;

  saldoMora30: number;

  porcentajeCantidadMora30: number;

  porcentajeSaldoMora30SobreSaldo: number;

  porcentajeSaldoMora30SobreOriginacion: number;


  // -------------------------------------------------------
  // Mora 60+
  // -------------------------------------------------------

  cantidadMora60: number;

  saldoMora60: number;

  porcentajeCantidadMora60: number;

  porcentajeSaldoMora60SobreSaldo: number;

  porcentajeSaldoMora60SobreOriginacion: number;


  // -------------------------------------------------------
  // Mora 90+
  // -------------------------------------------------------

  cantidadMora90: number;

  saldoMora90: number;

  porcentajeCantidadMora90: number;

  porcentajeSaldoMora90SobreSaldo: number;

  porcentajeSaldoMora90SobreOriginacion: number;


  // -------------------------------------------------------
  // Mora 180+
  // -------------------------------------------------------

  cantidadMora180: number;

  saldoMora180: number;

  porcentajeCantidadMora180: number;

  porcentajeSaldoMora180SobreSaldo: number;

  porcentajeSaldoMora180SobreOriginacion: number;
}


// =========================================================
// FILA
// =========================================================

export interface CosechaFila {

  cosecha: string;

  cantidadOriginada: number;

  valorInicialOriginal: number;

  valorDesembolsadoOriginal: number;

  maxMob: number;

  celdas: CosechaCelda[];
}


// =========================================================
// RESUMEN GENERAL
// =========================================================

export interface CosechaResumen {

  cosechaDesde: string;

  cosechaHasta: string;

  hastaCorte: string;

  idAgencia: number | null;

  idLineaCredito: number | null;

  cantidadCosechas: number;

  cantidadCreditosOriginados: number;

  valorInicialTotal: number;

  valorTotalDesembolsado: number;

  maxMob: number;

  filas: CosechaFila[];
}


// =========================================================
// DETALLE
// =========================================================

export interface CosechaDetalle {

  // -------------------------------------------------------
  // Crédito
  // -------------------------------------------------------

  idCarteraCredito: number;

  idAgencia: number;

  idLineaCredito: number;

  codigoLineaCredito: string | null;

  nombreLineaCredito: string | null;

  pagareCartera: string;


  // -------------------------------------------------------
  // Asociado
  // -------------------------------------------------------

  idDatosPersonal: number;

  tipoDocumento: string | null;

  documento: string;

  nombreCompleto: string;


  // -------------------------------------------------------
  // Contacto
  // -------------------------------------------------------

  telefono: string | null;

  celular: string | null;

  correo: string | null;


  // -------------------------------------------------------
  // Originación
  // -------------------------------------------------------

  fechaDesembolso: string | null;

  cosecha: string;

  valorInicialCredito: number;

  valorDesembolsado: number;


  // -------------------------------------------------------
  // Observación
  // -------------------------------------------------------

  fechaCorte: string;

  mob: number;

  presenteCorte: boolean;

  saldoCapital: number;

  diasMora: number;

  categoriaMora: string | null;

  codigoEstadoCartera: string | null;

  descripcionEstadoCartera: string | null;


  // -------------------------------------------------------
  // Edades
  // -------------------------------------------------------

  edadRiesgoInicial: string | null;

  edadMora: string | null;

  edadRiesgo: string | null;

  edadPe: string | null;

  edadHomologacion: string | null;

  edadContable: string | null;


  // -------------------------------------------------------
  // PE / deterioro
  // -------------------------------------------------------

  vea: number;

  pi: number;

  pdi: number;

  perdidaEsperada: number;

  deterioroCapital: number;

  deterioroIntereses: number;

  deterioroOtros: number;

  deterioroTotal: number;


  // -------------------------------------------------------
  // Indicadores
  // -------------------------------------------------------

  conSaldo: boolean;

  mora30: boolean;

  mora60: boolean;

  mora90: boolean;

  mora180: boolean;
}


// =========================================================
// CONSULTA
// =========================================================

export interface CosechaConsulta {

  cosechaDesde: string;

  cosechaHasta: string;

  hastaCorte?: string | null;

  idAgencia?: number | null;

  idLineaCredito?: number | null;
}


// =========================================================
// CONSULTA DETALLE
// =========================================================

export interface CosechaDetalleConsulta {

  cosecha: string;

  fechaCorte: string;

  indicador:
    | 'ORIGINADOS'
    | 'PRESENTES'
    | 'SIN_PRESENCIA'
    | 'CON_SALDO'
    | 'SALDO'
    | 'SALDO_REMANENTE'
    | 'MORA_30'
    | 'MORA_60'
    | 'MORA_90'
    | 'MORA_180';

  idAgencia?: number | null;

  idLineaCredito?: number | null;
}


// =========================================================
// OPCIONES DE INDICADOR
// =========================================================

export interface CosechaIndicadorOpcion {

  codigo: CosechaIndicador;

  nombre: string;

  descripcion: string;
}


export const COSECHA_INDICADORES:
  CosechaIndicadorOpcion[] = [

    {
      codigo: 'SALDO_REMANENTE',
      nombre: 'Saldo remanente',
      descripcion:
        'Saldo de capital frente al valor originalmente desembolsado.'
    },

    {
      codigo: 'MORA_30',
      nombre: 'Mora 30+',
      descripcion:
        'Saldo con 30 o más días de mora.'
    },

    {
      codigo: 'MORA_60',
      nombre: 'Mora 60+',
      descripcion:
        'Saldo con 60 o más días de mora.'
    },

    {
      codigo: 'MORA_90',
      nombre: 'Mora 90+',
      descripcion:
        'Saldo con 90 o más días de mora.'
    },

    {
      codigo: 'MORA_180',
      nombre: 'Mora 180+',
      descripcion:
        'Saldo con 180 o más días de mora.'
    }

  ];
