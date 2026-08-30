export type IndicadorMoraTemprana = 'TODOS' | 'MORA30_MOB3' | 'MORA30_MOB6' | 'MORA60_MOB6';

export interface MoraTempranaControl {
  ultimoCorteDisponible: string;
  ultimaCosechaMadura: string;
  mobMinimoEvaluacion: number;
}

export interface MoraTempranaResumen {
  cosechaDesde: string;
  cosechaHasta: string;
  cantidadCosechas: number;
  creditosOriginados: number;
  valorDesembolsado: number;
  mora30HastaMob3: number;
  porcentajeMora30Mob3: number;
  mora30HastaMob6: number;
  porcentajeMora30Mob6: number;
  mora60HastaMob6: number;
  porcentajeMora60Mob6: number;
}

export interface MoraTempranaCosecha {
  cosecha: string;
  creditosOriginados: number;
  valorDesembolsado: number;
  mora30HastaMob3: number;
  porcentajeMora30Mob3: number;
  mora30HastaMob6: number;
  porcentajeMora30Mob6: number;
  mora60HastaMob6: number;
  porcentajeMora60Mob6: number;
}

export interface MoraTempranaSegmento {
  id: number;
  codigo: string;
  descripcion: string;
  creditosOriginados: number;
  valorDesembolsado: number;
  mora30HastaMob3: number;
  porcentajeMora30Mob3: number;
  mora30HastaMob6: number;
  porcentajeMora30Mob6: number;
  mora60HastaMob6: number;
  porcentajeMora60Mob6: number;
}

export interface MoraTempranaPrimeraMora {
  evento: string;
  mob: number;
  cantidadCreditos: number;
  porcentajeCreditos: number;
}

export interface MoraTempranaDetalle {
  idCarteraCredito: number;
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
  idLineaCredito: number;
  codigoLineaCredito: string;
  nombreLineaCredito: string;
  pagareCartera: string;
  idDatosPersonal: number;
  tipoDocumento: string;
  documento: string;
  nombreCompleto: string;
  cosecha: string;
  fechaDesembolso: string;
  valorInicialCredito: number;
  valorDesembolsado: number;
  primerMob30: number | null;
  primerMob60: number | null;
  maxDiasMoraHastaMob6: number;
  mora30HastaMob3: boolean;
  mora30HastaMob6: boolean;
  mora60HastaMob6: boolean;
  fechaUltimoCorteObservado: string | null;
  mobUltimoCorteObservado: number | null;
  saldoUltimoCorteObservado: number;
  diasMoraUltimoCorteObservado: number;
  telefono: string | null;
  celular: string | null;
  correo: string | null;
}
