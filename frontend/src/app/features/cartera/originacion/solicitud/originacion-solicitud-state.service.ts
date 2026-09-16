import {
  Injectable
} from '@angular/core';

import {
  OriginacionAsociado,
  SolicitudCreditoDetalle,
  SolicitudCreditoGuardarResponse,
  SolicitudCreditoResumen
} from './originacion-solicitud.models';


export interface OriginacionFiltrosAsociadoEstado {

  documento: string;

  nombres: string;

  primerApellido: string;

  segundoApellido: string;
}


export interface OriginacionFormularioCreditoEstado {

  idLineaCredito: number | null;

  codigoClasificacionCredito: string;

  codigoDestinoEconomico: string;

  codigoGarantiaCredito: string;

  codigoSubgarantia: string;

  idFondoGarantia: number | null;

  codigoFormaPago: string;

  amortizacionCapital: number | null;

  codigoTipoCuota: string;

  plazoSolicitado: number | null;

  mesesGraciaCapital: number;

  mesesGraciaInteres: number;

  valorSolicitado: number | null;

  idEmpresaLibranza: number | null;

  observacionAsesor: string;
}


export interface OriginacionSolicitudEstadoNavegacion {

  idAgencia: number;

  filtrosAsociado:
    OriginacionFiltrosAsociadoEstado;

  busquedaRealizada: boolean;

  asociados:
    OriginacionAsociado[];

  asociadoSeleccionado:
    OriginacionAsociado | null;

  solicitudExistente:
    SolicitudCreditoResumen | null;

  solicitud:
    SolicitudCreditoDetalle | null;

  formulario:
    OriginacionFormularioCreditoEstado;

  modalidadSeleccionada: string;

  resultadoGuardado:
    SolicitudCreditoGuardarResponse | null;

  scrollY: number;
}


@Injectable({
  providedIn: 'root'
})
export class OriginacionSolicitudStateService {

  private estado:
    OriginacionSolicitudEstadoNavegacion | null =
      null;


  guardar(
    estado:
      OriginacionSolicitudEstadoNavegacion
  ): void {

    this.estado = {
      ...estado,

      filtrosAsociado: {
        ...estado.filtrosAsociado
      },

      asociados:
        [...estado.asociados],

      formulario: {
        ...estado.formulario
      }
    };
  }


  consumir():
    OriginacionSolicitudEstadoNavegacion | null {

    const estado =
      this.estado;

    this.estado =
      null;

    if (!estado) {
      return null;
    }

    return {
      ...estado,

      filtrosAsociado: {
        ...estado.filtrosAsociado
      },

      asociados:
        [...estado.asociados],

      formulario: {
        ...estado.formulario
      }
    };
  }

  getIdSolicitudCredito():
    number | null {

    const idSolicitudCredito =
      this.estado?.solicitud?.idSolicitudCredito
      ?? this.estado?.solicitudExistente?.idSolicitudCredito
      ?? this.estado?.resultadoGuardado?.idSolicitudCredito
      ?? null;

    if (
      idSolicitudCredito == null
      || !Number.isInteger(
        Number(idSolicitudCredito)
      )
      || Number(idSolicitudCredito) <= 0
    ) {
      return null;
    }

    return Number(
      idSolicitudCredito
    );
  }


  limpiar(): void {

    this.estado =
      null;
  }
}
