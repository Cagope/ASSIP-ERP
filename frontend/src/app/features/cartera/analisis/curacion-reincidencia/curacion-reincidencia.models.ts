export interface CuracionReincidenciaControl {
  primerCorteDisponible: string;
  ultimoCorteDisponible: string;
  periodoDesdeSugerido: string;
  periodoHastaSugerido: string;
  mesesSeguimientoReincidencia: number;
}

export interface CuracionReincidenciaResumen {
  periodoDesde: string;
  periodoHasta: string;
  episodios: number;
  episodiosCurados: number;
  episodiosAbiertos: number;
  tasaCuraObservada: number;
  mesesPromedioCura: number | null;
  curasSeguimiento6m: number;
  reincidentes6m: number;
  tasaReincidencia6m: number | null;
  mesesPromedioReincidencia: number | null;
}

export interface CuracionReincidenciaPeriodo {
  periodoInicio: string;
  episodios: number;
  episodiosCurados: number;
  episodiosAbiertos: number;
  tasaCuraObservada: number;
  mesesPromedioCura: number | null;
  curasSeguimiento6m: number;
  reincidentes6m: number;
  tasaReincidencia6m: number | null;
  mesesPromedioReincidencia: number | null;
}

export interface CuracionReincidenciaSegmento {
  id: number;
  codigo: string;
  descripcion: string;
  episodios: number;
  episodiosCurados: number;
  episodiosAbiertos: number;
  tasaCuraObservada: number;
  mesesPromedioCura: number | null;
  curasSeguimiento6m: number;
  reincidentes6m: number;
  tasaReincidencia6m: number | null;
  mesesPromedioReincidencia: number | null;
}

export interface CuracionReincidenciaEdadEntrada {
  edadEntrada: string;
  episodios: number;
  episodiosCurados: number;
  episodiosAbiertos: number;
  tasaCuraObservada: number;
  mesesPromedioCura: number | null;
  curasSeguimiento6m: number;
  reincidentes6m: number;
  tasaReincidencia6m: number | null;
  mesesPromedioReincidencia: number | null;
}

export interface CuracionReincidenciaDistribucionCura {
  orden: number;
  codigo: string;
  descripcion: string;
  episodios: number;
  porcentajeEpisodios: number;
}

export interface CuracionReincidenciaPrimeraReincidencia {
  mes: number;
  reincidentes: number;
  porcentajeSobreCurasMaduras: number;
}

export interface CuracionReincidenciaDetalle {
  idCarteraCredito: number;
  numeroEpisodio: number;
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
  periodoInicio: string;
  fechaInicio: string;
  edadEntrada: string;
  maximaEdadAlcanzada: string;
  saldoInicio: number;
  diasMoraInicio: number;
  curado: boolean;
  fechaCura: string | null;
  mesesHastaCura: number | null;
  saldoCura: number | null;
  seguimiento6mCompleto: boolean;
  reincidente6m: boolean;
  fechaReincidencia: string | null;
  mesesHastaReincidencia: number | null;
  edadReincidencia: string | null;
  saldoReincidencia: number | null;
  diasMoraReincidencia: number | null;
  fechaUltimoCorteObservado: string;
  edadActual: string;
  saldoActual: number;
  diasMoraActual: number;
  telefono: string | null;
  celular: string | null;
  correo: string | null;
}

export interface ApiListResponse<T> {
  value: T[];
  Count: number;
}

export type IndicadorDetalle =
  | 'TODOS'
  | 'CURADOS'
  | 'ABIERTOS'
  | 'CURAS_MADURAS_6M'
  | 'REINCIDENTES_6M'
  | 'NO_REINCIDENTES_6M';
