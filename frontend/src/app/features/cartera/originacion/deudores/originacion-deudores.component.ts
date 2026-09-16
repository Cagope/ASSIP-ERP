import {
  Component,
  OnInit
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  HeaderActionsComponent
} from '../../../../shared/header-actions/header-actions.component';

import {
  SessionService
} from '../../../../core/auth/session.service';

import {
  OriginacionDeudoresApi
} from './originacion-deudores.api';

import {
  OriginacionContextoComponent
} from '../contexto/originacion-contexto.component';

import {
  SolicitudDeudor
} from './originacion-deudores.models';

import {
  OriginacionSolicitudApi
} from '../solicitud/originacion-solicitud.api';

import {
  OriginacionSolicitudStateService
} from '../solicitud/originacion-solicitud-state.service';

import {
  OriginacionAsociado,
  SolicitudCreditoDetalle
} from '../solicitud/originacion-solicitud.models';

import {
  OriginacionContextoApi
} from '../contexto/originacion-contexto.api';

import {
  OriginacionContexto
} from '../contexto/originacion-contexto.models';


@Component({
  selector: 'app-originacion-deudores',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    OriginacionContextoComponent
  ],

  templateUrl:
    './originacion-deudores.component.html',

  styleUrls: [
    './originacion-deudores.component.scss'
  ]
})
export class OriginacionDeudoresComponent
  implements OnInit {

  // =========================================================
  // AGENCIA
  // =========================================================

  agenciaActiva: any | null = null;


  // =========================================================
  // SOLICITUD
  // =========================================================

  solicitud:
    SolicitudCreditoDetalle | null = null;

  cargandoSolicitud = false;


  // =========================================================
  // DEUDORES VINCULADOS
  // =========================================================

  deudores:
    SolicitudDeudor[] = [];

  deudorPrincipal:
    SolicitudDeudor | null = null;

  codeudores:
    SolicitudDeudor[] = [];

  cargandoDeudores = false;

  codeudorSeleccionado:
    SolicitudDeudor | null = null;

  contextoCodeudorSeleccionado:
    OriginacionContexto | null = null;

  cargandoContextoCodeudor = false;

  // =========================================================
  // CONTEXTO DEUDOR PRINCIPAL
  // =========================================================

  contextoPrincipal:
    OriginacionContexto | null = null;

  cargandoContextoPrincipal = false;


  // =========================================================
  // BÚSQUEDA CODEUDOR
  // =========================================================

  mostrarBusquedaCodeudor = false;

  filtrosCodeudor = {
    documento: '',
    nombres: '',
    primerApellido: '',
    segundoApellido: ''
  };

  buscandoCodeudores = false;
  busquedaCodeudorRealizada = false;

  personasEncontradas:
    OriginacionAsociado[] = [];

  personaSeleccionada:
    OriginacionAsociado | null = null;


  // =========================================================
  // CONTEXTO CODEUDOR CANDIDATO
  // =========================================================

  contextoCandidato:
    OriginacionContexto | null = null;

  cargandoContextoCandidato = false;

  agregandoCodeudor = false;


  // =========================================================
  // MENSAJES
  // =========================================================

  error = '';
  mensaje = '';


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private deudoresApi:
      OriginacionDeudoresApi,

    private solicitudApi:
      OriginacionSolicitudApi,

    private contextoApi:
      OriginacionContextoApi,

    private solicitudState:
      OriginacionSolicitudStateService,

    private sessionService:
      SessionService,

    private router:
      Router
  ) {
  }


  // =========================================================
  // INICIO
  // =========================================================

  ngOnInit(): void {

    this.agenciaActiva =
      this.sessionService.getAgenciaActiva();

    if (!this.agenciaActiva) {

      this.error =
        'Debe seleccionar una agencia para continuar.';

      return;
    }

    this.cargarSolicitud();
  }


  // =========================================================
  // CARGAR SOLICITUD
  // =========================================================

  private cargarSolicitud(): void {

    const idSolicitudCredito =
      this.obtenerIdSolicitudCredito();

    if (!idSolicitudCredito) {

      this.error =
        'No hay una solicitud de crédito seleccionada.';

      return;
    }

    this.cargandoSolicitud = true;
    this.error = '';

    this.solicitudApi
      .buscarSolicitudPorId(
        idSolicitudCredito
      )
      .subscribe({

        next: solicitud => {

          this.solicitud =
            solicitud;

          this.cargandoSolicitud =
            false;

          this.cargarDeudores();
        },

        error: error => {

          this.solicitud =
            null;

          this.cargandoSolicitud =
            false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar la solicitud.'
            );
        }
      });
  }


  // =========================================================
  // CARGAR DEUDORES
  // =========================================================

  cargarDeudores(): void {

    const idSolicitudCredito =
      this.obtenerIdSolicitudCredito();

    if (!idSolicitudCredito) {
      return;
    }

    this.cargandoDeudores = true;
    this.error = '';

    this.deudoresApi
      .listarPorSolicitud(
        idSolicitudCredito
      )
      .subscribe({

        next: deudores => {

          this.deudores =
            deudores ?? [];

          this.deudorPrincipal =
            this.deudores.find(
              item =>
                this.normalizar(
                  item.tipoDeudor
                ) === 'PRINCIPAL'
            ) ?? null;

          this.codeudores =
            this.deudores
              .filter(
                item =>
                  this.normalizar(
                    item.tipoDeudor
                  ) === 'CODEUDOR'
              )
              .sort(
                (a, b) =>
                  Number(a.ordenDeudor)
                  - Number(b.ordenDeudor)
              );

          this.cargandoDeudores =
            false;

          this.cargarContextoPrincipal();
        },

        error: error => {

          this.deudores = [];
          this.deudorPrincipal = null;
          this.codeudores = [];

          this.cargandoDeudores =
            false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar los deudores de la solicitud.'
            );
        }
      });
  }


  // =========================================================
  // CONTEXTO DEUDOR PRINCIPAL
  // =========================================================

  private cargarContextoPrincipal(): void {

    if (
      !this.deudorPrincipal
      || !this.agenciaActiva
    ) {

      this.contextoPrincipal =
        null;

      return;
    }

    const idDatosPersonal =
      Number(
        this.deudorPrincipal
          .idDatosPersonal
      );

    const idAgencia =
      Number(
        this.agenciaActiva.idAgencia
      );

    if (
      !Number.isInteger(idDatosPersonal)
      || idDatosPersonal <= 0
      || !Number.isInteger(idAgencia)
      || idAgencia <= 0
    ) {

      this.contextoPrincipal =
        null;

      return;
    }

    this.cargandoContextoPrincipal =
      true;

    this.contextoApi
      .consultar(
        idDatosPersonal,
        idAgencia
      )
      .subscribe({

        next: contexto => {

          this.contextoPrincipal =
            contexto;

          this.cargandoContextoPrincipal =
            false;
        },

        error: () => {

          this.contextoPrincipal =
            null;

          this.cargandoContextoPrincipal =
            false;
        }
      });
  }


  // =========================================================
  // ABRIR BÚSQUEDA CODEUDOR
  // =========================================================

  abrirBusquedaCodeudor(): void {

    this.mostrarBusquedaCodeudor =
      true;

    this.limpiarCandidato();

    this.filtrosCodeudor = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };

    this.personasEncontradas = [];
    this.busquedaCodeudorRealizada = false;

    this.error = '';
    this.mensaje = '';
  }


  // =========================================================
  // CANCELAR BÚSQUEDA
  // =========================================================

  cancelarBusquedaCodeudor(): void {

    if (
      this.buscandoCodeudores
      || this.cargandoContextoCandidato
      || this.agregandoCodeudor
    ) {
      return;
    }

    this.mostrarBusquedaCodeudor =
      false;

    this.personasEncontradas = [];
    this.busquedaCodeudorRealizada = false;

    this.limpiarCandidato();

    this.error = '';
  }


  // =========================================================
  // BUSCAR PERSONA
  //
  // Reutilizamos exactamente la búsqueda de personas utilizada
  // por Solicitud. La persona solamente necesita existir en
  // hoja_vida.datos_personales.
  // =========================================================

  buscarCodeudor(): void {

    if (this.buscandoCodeudores) {
      return;
    }

    const documento =
      this.filtrosCodeudor.documento.trim();

    const nombres =
      this.filtrosCodeudor.nombres.trim();

    const primerApellido =
      this.filtrosCodeudor
        .primerApellido
        .trim();

    const segundoApellido =
      this.filtrosCodeudor
        .segundoApellido
        .trim();

    if (
      !documento
      && !nombres
      && !primerApellido
      && !segundoApellido
    ) {

      this.error =
        'Ingrese al menos un criterio de búsqueda.';

      return;
    }

    const idAgencia =
      Number(
        this.agenciaActiva?.idAgencia
      );

    if (
      !Number.isInteger(idAgencia)
      || idAgencia <= 0
    ) {

      this.error =
        'No fue posible identificar la agencia activa.';

      return;
    }

    this.buscandoCodeudores = true;
    this.busquedaCodeudorRealizada = false;

    this.personasEncontradas = [];

    this.limpiarCandidato();

    this.error = '';
    this.mensaje = '';

    this.solicitudApi
      .buscarAsociados(
        idAgencia,
        documento,
        nombres,
        primerApellido,
        segundoApellido
      )
      .subscribe({

        next: personas => {

          this.personasEncontradas =
            personas ?? [];

          this.busquedaCodeudorRealizada =
            true;

          this.buscandoCodeudores =
            false;
        },

        error: error => {

          this.personasEncontradas = [];

          this.busquedaCodeudorRealizada =
            true;

          this.buscandoCodeudores =
            false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible realizar la búsqueda.'
            );
        }
      });
  }


  // =========================================================
  // SELECCIONAR CANDIDATO
  // =========================================================

  seleccionarCandidato(
    persona: OriginacionAsociado
  ): void {

    if (
      !persona
      || this.cargandoContextoCandidato
      || this.agregandoCodeudor
    ) {
      return;
    }

    if (
      this.personaYaVinculada(
        persona.idDatosPersonal
      )
    ) {

      this.error =
        'La persona seleccionada ya se encuentra vinculada a la solicitud.';

      return;
    }

    this.personaSeleccionada =
      persona;

    this.contextoCandidato =
      null;

    this.error = '';
    this.mensaje = '';

    this.cargarContextoCandidato();
  }


  // =========================================================
  // CARGAR CONTEXTO CANDIDATO
  // =========================================================

  private cargarContextoCandidato(): void {

    if (
      !this.personaSeleccionada
      || !this.agenciaActiva
    ) {
      return;
    }

    const idDatosPersonal =
      Number(
        this.personaSeleccionada
          .idDatosPersonal
      );

    const idAgencia =
      Number(
        this.agenciaActiva.idAgencia
      );

    if (
      !Number.isInteger(idDatosPersonal)
      || idDatosPersonal <= 0
      || !Number.isInteger(idAgencia)
      || idAgencia <= 0
    ) {

      this.error =
        'No fue posible identificar correctamente la persona seleccionada.';

      return;
    }

    this.cargandoContextoCandidato =
      true;

    this.contextoApi
      .consultar(
        idDatosPersonal,
        idAgencia
      )
      .subscribe({

        next: contexto => {

          this.contextoCandidato =
            contexto;

          this.cargandoContextoCandidato =
            false;
        },

        error: error => {

          this.contextoCandidato =
            null;

          this.cargandoContextoCandidato =
            false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar el contexto del posible codeudor.'
            );
        }
      });
  }


  // =========================================================
  // DESCARTAR CANDIDATO
  // =========================================================

  descartarCandidato(): void {

    if (
      this.cargandoContextoCandidato
      || this.agregandoCodeudor
    ) {
      return;
    }

    this.limpiarCandidato();
    this.error = '';
  }


  // =========================================================
  // AGREGAR CODEUDOR
  //
  // La persona ya fue seleccionada y su contexto fue consultado.
  // Solamente aquí se vincula formalmente a la solicitud.
  // =========================================================

  agregarCodeudor(): void {

    const idSolicitudCredito =
      this.obtenerIdSolicitudCredito();

    if (
      !idSolicitudCredito
      || !this.personaSeleccionada
    ) {
      return;
    }

    if (!this.contextoCandidato) {

      this.error =
        'Debe consultar las condiciones de la persona antes de agregarla como codeudor.';

      return;
    }

    const idDatosPersonal =
      Number(
        this.personaSeleccionada
          .idDatosPersonal
      );

    if (
      !Number.isInteger(idDatosPersonal)
      || idDatosPersonal <= 0
    ) {

      this.error =
        'La persona seleccionada no es válida.';

      return;
    }

    if (
      this.personaYaVinculada(
        idDatosPersonal
      )
    ) {

      this.error =
        'La persona ya se encuentra vinculada a la solicitud.';

      return;
    }

    this.agregandoCodeudor = true;

    this.error = '';
    this.mensaje = '';

    this.deudoresApi
      .agregarCodeudor({
        idSolicitudCredito,
        idDatosPersonal
      })
      .subscribe({

        next: () => {

          this.agregandoCodeudor =
            false;

          this.mensaje =
            'Codeudor agregado correctamente.';

          this.mostrarBusquedaCodeudor =
            false;

          this.personasEncontradas = [];
          this.busquedaCodeudorRealizada = false;

          this.limpiarCandidato();

          this.cargarDeudores();
        },

        error: error => {

          this.agregandoCodeudor =
            false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible agregar el codeudor.'
            );
        }
      });
  }


  // =========================================================
  // RETIRAR CODEUDOR
  // =========================================================

  retirarCodeudor(
    codeudor: SolicitudDeudor
  ): void {

    if (
      !codeudor
      || this.agregandoCodeudor
    ) {
      return;
    }

    const idSolicitudDeudor =
      Number(
        codeudor.idSolicitudDeudor
      );

    if (
      !Number.isInteger(idSolicitudDeudor)
      || idSolicitudDeudor <= 0
    ) {
      return;
    }

    const confirmado =
      window.confirm(
        `¿Desea retirar a ${codeudor.nombreCompleto} como codeudor de la solicitud?`
      );

    if (!confirmado) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    this.deudoresApi
      .retirarCodeudor(
        idSolicitudDeudor
      )
      .subscribe({

        next: () => {

          this.mensaje =
            'Codeudor retirado correctamente.';

          this.cargarDeudores();
        },

        error: error => {

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible retirar el codeudor.'
            );
        }
      });
  }


  // =========================================================
  // VALIDAR PERSONA YA VINCULADA
  // =========================================================

  personaYaVinculada(
    idDatosPersonal: number
  ): boolean {

    const idPersona =
      Number(idDatosPersonal);

    return this.deudores.some(
      item =>
        item.activo
        && Number(
          item.idDatosPersonal
        ) === idPersona
    );
  }


  // =========================================================
  // LIMPIAR CANDIDATO
  // =========================================================

  private limpiarCandidato(): void {

    this.personaSeleccionada =
      null;

    this.contextoCandidato =
      null;

    this.cargandoContextoCandidato =
      false;
  }


  // =========================================================
  // ID SOLICITUD
  // =========================================================

  private obtenerIdSolicitudCredito():
    number | null {

    const idSolicitudCredito =
      Number(
        this.solicitudState
          .getIdSolicitudCredito()
      );

    if (
      !Number.isInteger(idSolicitudCredito)
      || idSolicitudCredito <= 0
    ) {
      return null;
    }

    return idSolicitudCredito;
  }


  // =========================================================
  // VOLVER
  // =========================================================

  volverSolicitud(): void {

    this.router.navigate([
      '/cartera/originacion'
    ]);
  }

  // =========================================================
  // UTILIDADES
  // =========================================================

  private normalizar(
    valor: string | null | undefined
  ): string {

    return (
      valor
        ?? ''
    )
      .trim()
      .toUpperCase();
  }


  private obtenerMensajeError(
    respuesta: any,
    predeterminado: string
  ): string {

    return (
      respuesta?.error?.mensaje
      ?? respuesta?.error?.message
      ?? respuesta?.error?.error
      ?? respuesta?.message
      ?? predeterminado
    );
  }

  verContextoCodeudor(
    codeudor: SolicitudDeudor
  ): void {

    if (
      !codeudor
      || !this.agenciaActiva
    ) {
      return;
    }

    const idDatosPersonal =
      Number(
        codeudor.idDatosPersonal
      );

    const idAgencia =
      Number(
        this.agenciaActiva.idAgencia
      );

    if (
      !Number.isInteger(idDatosPersonal)
      || idDatosPersonal <= 0
      || !Number.isInteger(idAgencia)
      || idAgencia <= 0
    ) {
      return;
    }

    this.codeudorSeleccionado =
      codeudor;

    this.contextoCodeudorSeleccionado =
      null;

    this.cargandoContextoCodeudor =
      true;

    this.error = '';

    this.contextoApi
      .consultar(
        idDatosPersonal,
        idAgencia
      )
      .subscribe({

        next: contexto => {

          this.contextoCodeudorSeleccionado =
            contexto;

          this.cargandoContextoCodeudor =
            false;
        },

        error: error => {

          this.contextoCodeudorSeleccionado =
            null;

          this.cargandoContextoCodeudor =
            false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar el contexto del codeudor.'
            );
        }
      });
  }

  ocultarContextoCodeudor(): void {

    this.codeudorSeleccionado = null;

    this.contextoCodeudorSeleccionado = null;

    this.cargandoContextoCodeudor = false;
  }

  irABienes(): void {
    this.router.navigate([
      '/cartera/originacion/bienes'
    ]);
  }

}
