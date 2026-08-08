export interface CentralRiesgoResultadoImportacion {

  idCentralArchivo: number;

  idCentralRiesgo: number;
  codigoCentral: string;
  nombreCentral: string;

  fechaCorte: string;

  nombreArchivo: string;
  tamanoArchivo: number;
  extensionArchivo: string;
  codificacionArchivo: string;
  separadorArchivo: string;

  fechaImportacion: string;

  cantidadRegistrosLeidos: number;
  cantidadRegistrosImportados: number;
  cantidadRegistrosRechazados: number;

  observaciones: string | null;

  requiereConfirmacion: boolean | null;

  mensajeConfirmacion: string | null;

}

export interface CentralRiesgoResultadoDato {

  idCentralDato: number;

  idCentralArchivo: number;

  numeroFila: number;

  documento: string;

  clasificacionCartera: string;

  calificacionGuiaFinal: string | null;

  alertasTotales: number | null;

}

export interface CentralRiesgoResultadoDato {

  idCentralDato: number;

  idCentralArchivo: number;

  numeroFila: number;

  documento: string;

  clasificacionCartera: string;

  calificacionGuiaFinal: string | null;

  alertasTotales: number | null;

}
