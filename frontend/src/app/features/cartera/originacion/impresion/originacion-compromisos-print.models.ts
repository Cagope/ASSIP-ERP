import type {
  SolicitudAprobacionFotos
} from '../aprobacion/originacion-aprobacion.models';

import type {
  OriginacionCartera,
  OriginacionVectorResumen
} from '../contexto/originacion-contexto.models';


// =========================================================
// DOCUMENTO DE COMPROMISOS Y AUTORIZACIONES
// =========================================================

/**
 * Documento de compromisos:
 *
 * - Datos económicos exclusivamente del deudor principal.
 * - Cartera vigente del deudor principal.
 * - La línea 010 se consolidará únicamente en la impresión.
 * - No modifica el Expediente Integral.
 */
export interface OriginacionCompromisosPrintData {

  idSolicitudCredito: number;

  numeroSolicitud: string | null;

  fechaImpresion: string;

  fotos: SolicitudAprobacionFotos;

  // =======================================================
  // CARTERA ACTUAL DEL DEUDOR PRINCIPAL
  // =======================================================

  carteraActual: OriginacionCartera[];

  // =======================================================
  // VECTOR RESUMEN - VALORES INICIALES Y DESEMBOLSADOS
  // =======================================================

  vectorResumen: OriginacionVectorResumen[];

}
