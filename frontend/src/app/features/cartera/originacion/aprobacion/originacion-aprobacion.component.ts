import {
  CommonModule
} from '@angular/common';

import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  forkJoin
} from 'rxjs';

import {
  OriginacionAprobacionApi
} from './originacion-aprobacion.api';

import {
  SessionService
} from '../../../../core/auth/session.service';

import {
  SolicitudAprobacionBandeja,
  SolicitudAprobacionDecision,
  SolicitudAprobacionActuacion,
  SolicitudAprobacionFotos,
  SolicitudAprobacionDecisionRequest,
  ENTES_APROBACION,
  DECISIONES_APROBACION
} from './originacion-aprobacion.models';


import { AprobacionSolicitudComponent } from './expediente/solicitud/aprobacion-solicitud.component';
import { AprobacionDeudoresComponent } from './expediente/deudores/aprobacion-deudores.component';
import { AprobacionFinancieroComponent } from './expediente/financiero/aprobacion-financiero.component';
import { AprobacionBienesComponent } from './expediente/bienes/aprobacion-bienes.component';
import { AprobacionCentralRiesgoComponent } from './expediente/central-riesgo/aprobacion-central-riesgo.component';
import { AprobacionAnalisisComponent } from './expediente/analisis/aprobacion-analisis.component';

@Component({
  selector: 'app-originacion-aprobacion',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    AprobacionSolicitudComponent,
    AprobacionDeudoresComponent,
    AprobacionFinancieroComponent,
    AprobacionBienesComponent,
    AprobacionCentralRiesgoComponent,
    AprobacionAnalisisComponent
  ],

  templateUrl:
    './originacion-aprobacion.component.html',

  styleUrl:
    './originacion-aprobacion.component.scss'
})
export class OriginacionAprobacionComponent
  implements OnInit {


  // =========================================================
  // SERVICIO
  // =========================================================

  private readonly api =
    inject(OriginacionAprobacionApi);

  private readonly session =
      inject(SessionService);

  // La autorización depende del ente actual, no del permiso general.
  get puedeAprobar(): boolean {
    const idEnte = this.solicitudSeleccionada?.idEnteActual;

    const permisoPorEnte: Record<number, string> = {
      [ENTES_APROBACION.GERENCIA]: 'CARTERA_ORIGINACION_APROBACION_GERENCIA',
      [ENTES_APROBACION.COMITE]: 'CARTERA_ORIGINACION_APROBACION_COMITE',
      [ENTES_APROBACION.CONSEJO]: 'CARTERA_ORIGINACION_APROBACION_CONSEJO'
    };

    if (idEnte == null) {
      return false;
    }

    const permiso = permisoPorEnte[idEnte];
    return !!permiso && this.session.tienePermiso(permiso);
  }

  // =========================================================
  // CONSTANTES
  // =========================================================

  readonly ENTES_APROBACION =
    ENTES_APROBACION;

  readonly DECISIONES_APROBACION =
    DECISIONES_APROBACION;


  // =========================================================
  // BANDEJA
  // =========================================================

  solicitudes:
    SolicitudAprobacionBandeja[] = [];

  solicitudSeleccionada:
    SolicitudAprobacionBandeja | null = null;

  filtro = '';

  cargandoBandeja = false;


  // =========================================================
  // CATÁLOGO DE DECISIONES
  // =========================================================

  decisiones:
    SolicitudAprobacionDecision[] = [];


  // =========================================================
  // HISTORIAL
  // =========================================================

  historial:
    SolicitudAprobacionActuacion[] = [];

  actuacionSeleccionada:
    SolicitudAprobacionActuacion | null = null;


  // =========================================================
  // FOTOGRAFÍAS
  // =========================================================

  fotosActuales:
    SolicitudAprobacionFotos | null = null;

  fotosHistoricas:
    SolicitudAprobacionFotos | null = null;

  cargandoDetalle = false;

  cargandoFotosHistoricas = false;


  // =========================================================
  // SECCIÓN DE CONSULTA
  // =========================================================

  seccionActiva = 'solicitud';


  // =========================================================
  // FORMULARIO DE DECISIÓN
  // =========================================================

  idAprobacionDecision:
    number | null = null;

  concepto = '';

  numeroActa = '';

  fechaActa = '';

  registrandoDecision = false;


  // =========================================================
  // MENSAJES
  // =========================================================

  error = '';

  mensaje = '';


  // =========================================================
  // INICIO
  // =========================================================

  ngOnInit(): void {

    this.cargarInicial();
  }


  // =========================================================
  // CARGA INICIAL
  // =========================================================

  cargarInicial(): void {

    if (this.cargandoBandeja) {
      return;
    }

    this.cargandoBandeja = true;

    this.error = '';
    this.mensaje = '';

    forkJoin({

      bandeja:
        this.api.listarBandeja(),

      decisiones:
        this.api.listarDecisiones()

    }).subscribe({

      next: ({
        bandeja,
        decisiones
      }) => {

        this.solicitudes =
          bandeja ?? [];

        this.decisiones =
          decisiones ?? [];

        this.cargandoBandeja = false;
      },

      error: error => {

        this.cargandoBandeja = false;

        this.error =
          this.obtenerMensajeError(
            error,
            'No fue posible cargar la bandeja de aprobación.'
          );
      }

    });
  }


  // =========================================================
  // RECARGAR BANDEJA
  // =========================================================

  recargarBandeja(): void {

    if (this.cargandoBandeja) {
      return;
    }

    this.cargandoBandeja = true;

    this.error = '';

    this.api.listarBandeja()
      .subscribe({

        next: solicitudes => {

          this.solicitudes =
            solicitudes ?? [];

          this.cargandoBandeja = false;
        },

        error: error => {

          this.cargandoBandeja = false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible actualizar la bandeja.'
            );
        }

      });
  }


  // =========================================================
  // FILTRAR BANDEJA
  // =========================================================

  get solicitudesFiltradas():
    SolicitudAprobacionBandeja[] {

    const texto =
      this.filtro
        .trim()
        .toLocaleLowerCase();

    if (!texto) {
      return this.solicitudes;
    }

    return this.solicitudes.filter(
      solicitud => {

        const contenido = [

          solicitud.numeroSolicitud,

          solicitud.documento,

          solicitud.nombreCompleto,

          solicitud.nombreAgencia,

          solicitud.nombreLineaCredito,

          solicitud.nombreEnteFinal

        ]
          .filter(
            valor => valor != null
          )
          .join(' ')
          .toLocaleLowerCase();

        return contenido.includes(texto);
      }
    );
  }


  // =========================================================
  // SELECCIONAR SOLICITUD
  // =========================================================

  seleccionarSolicitud(
    solicitud: SolicitudAprobacionBandeja
  ): void {

    if (
      this.cargandoDetalle ||
      this.registrandoDecision
    ) {
      return;
    }

    this.solicitudSeleccionada =
      solicitud;

    this.historial = [];

    this.fotosActuales = null;

    this.fotosHistoricas = null;

    this.actuacionSeleccionada = null;

    this.seccionActiva =
      'solicitud';

    this.limpiarFormularioDecision();

    this.cargarDetalleSolicitud(
      solicitud.idSolicitudCredito
    );
  }


  // =========================================================
  // CARGAR DETALLE DE LA SOLICITUD
  // =========================================================

  private cargarDetalleSolicitud(
    idSolicitudCredito: number
  ): void {

    this.cargandoDetalle = true;

    this.error = '';
    this.mensaje = '';

    forkJoin({

      fotos:
        this.api.obtenerFotos(
          idSolicitudCredito
        ),

      historial:
        this.api.listarHistorial(
          idSolicitudCredito
        )

    }).subscribe({

      next: ({
        fotos,
        historial
      }) => {

        if (
          this.solicitudSeleccionada
            ?.idSolicitudCredito !==
          idSolicitudCredito
        ) {
          return;
        }

        this.fotosActuales =
          fotos;

        this.historial =
          historial ?? [];

        this.cargandoDetalle = false;
      },

      error: error => {

        this.cargandoDetalle = false;

        this.error =
          this.obtenerMensajeError(
            error,
            'No fue posible consultar la información de aprobación.'
          );
      }

    });
  }


  // =========================================================
  // CERRAR DETALLE
  // =========================================================

  cerrarDetalle(): void {

    if (
      this.registrandoDecision ||
      this.cargandoDetalle
    ) {
      return;
    }

    this.solicitudSeleccionada = null;

    this.fotosActuales = null;

    this.fotosHistoricas = null;

    this.historial = [];

    this.actuacionSeleccionada = null;

    this.limpiarFormularioDecision();

    this.error = '';
    this.mensaje = '';
  }


  // =========================================================
  // FOTOGRAFÍAS VISIBLES
  // =========================================================

  get fotosVisibles():
    SolicitudAprobacionFotos | null {

    if (this.actuacionSeleccionada) {

      return this.fotosHistoricas;
    }

    return this.fotosActuales;
  }


  // =========================================================
  // CONSULTAR FOTOGRAFÍAS HISTÓRICAS
  // =========================================================

  seleccionarActuacion(
    actuacion: SolicitudAprobacionActuacion
  ): void {

    const solicitud =
      this.solicitudSeleccionada;

    if (
      !solicitud ||
      this.cargandoFotosHistoricas
    ) {
      return;
    }

    this.actuacionSeleccionada =
      actuacion;

    this.fotosHistoricas = null;

    this.cargandoFotosHistoricas = true;

    this.error = '';

    this.api.obtenerFotosActuacion(
      solicitud.idSolicitudCredito,
      actuacion.idSolicitudAprobacion
    ).subscribe({

      next: fotos => {

        if (
          this.solicitudSeleccionada
            ?.idSolicitudCredito !==
            solicitud.idSolicitudCredito ||

          this.actuacionSeleccionada
            ?.idSolicitudAprobacion !==
            actuacion.idSolicitudAprobacion
        ) {
          return;
        }

        this.fotosHistoricas =
          fotos;

        this.cargandoFotosHistoricas = false;
      },

      error: error => {

        this.cargandoFotosHistoricas = false;

        this.error =
          this.obtenerMensajeError(
            error,
            'No fue posible consultar las fotografías históricas.'
          );
      }

    });
  }


  // =========================================================
  // VOLVER A FOTOGRAFÍAS ACTUALES
  // =========================================================

  mostrarFotosActuales(): void {

    this.actuacionSeleccionada = null;

    this.fotosHistoricas = null;

    this.error = '';
  }


  // =========================================================
  // CAMBIAR SECCIÓN
  // =========================================================

  seleccionarSeccion(
    seccion: string
  ): void {

    this.seccionActiva =
      seccion;
  }


  // =========================================================
  // DECISIÓN SELECCIONADA
  // =========================================================

  get decisionSeleccionada():
    SolicitudAprobacionDecision | null {

    if (
      this.idAprobacionDecision == null
    ) {
      return null;
    }

    return this.decisiones.find(
      decision =>
        decision.idAprobacionDecision ===
        this.idAprobacionDecision
    ) ?? null;
  }


  // =========================================================
  // CONCEPTOS SUGERIDOS POR ENTE Y DECISIÓN
  // =========================================================

  private ultimoConceptoSugerido = '';

  private normalizarDecision(valor: string): string {
    return (valor ?? '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toUpperCase()
      .replace(/[^A-Z0-9]+/g, '_')
      .replace(/^_+|_+$/g, '');
  }

  private obtenerTipoDecision(): 'APROBADA' | 'NO_VIABLE' | 'SOLICITA_AJUSTES' | null {
    const seleccionada = this.decisionSeleccionada;
    if (!seleccionada) {
      return null;
    }

    const codigo = this.normalizarDecision(seleccionada.codigoDecision);
    const nombre = this.normalizarDecision(seleccionada.nombreDecision);

    if (codigo === 'APROBADA' || codigo === 'APROBADO' || nombre === 'APROBADA' || nombre === 'APROBADO') {
      return 'APROBADA';
    }

    if (codigo === 'NO_VIABLE' || codigo === 'NO_APROBADA' || nombre === 'NO_VIABLE' || nombre === 'NO_APROBADA') {
      return 'NO_VIABLE';
    }

    if (codigo === 'SOLICITA_AJUSTES' || codigo === 'SOLICITAR_AJUSTES' || nombre === 'SOLICITA_AJUSTES' || nombre === 'SOLICITAR_AJUSTES') {
      return 'SOLICITA_AJUSTES';
    }

    return null;
  }

  // Al cambiar la decisión, retirar solo el texto generado automáticamente.
  // Una observación editada por el funcionario permanece intacta.
  alCambiarDecision(): void {
    if (this.ultimoConceptoSugerido && this.concepto === this.ultimoConceptoSugerido) {
      this.concepto = '';
    }
    this.ultimoConceptoSugerido = '';
  }

  get puedeSugerirConcepto(): boolean {
    return !this.registrandoDecision
      && this.obtenerTipoDecision() !== null
      && (!this.concepto.trim() || this.concepto === this.ultimoConceptoSugerido);
  }

  sugerirConcepto(): void {
    if (!this.puedeSugerirConcepto) {
      return;
    }

    const decision = this.obtenerTipoDecision();
    const ente = this.solicitudSeleccionada?.idEnteActual;
    if (!decision || ente == null) {
      return;
    }

    const nombreEnte = this.nombreEnteActual;
    let sugerencia = '';

    switch (decision) {
      case 'APROBADA':
        sugerencia = ente === ENTES_APROBACION.CONSEJO
          ? 'El Consejo de Administración emite concepto favorable sobre la solicitud de crédito presentada, de conformidad con la documentación y las condiciones sometidas a su consideración. Se aprueba la solicitud para continuar con las etapas posteriores previstas en el procedimiento institucional.'
          : `Se registra concepto favorable de ${nombreEnte} sobre la solicitud de crédito, con base en la información presentada para esta actuación. Se autoriza continuar el trámite ante la siguiente instancia que corresponda, conforme al procedimiento institucional.`;
        break;

      case 'NO_VIABLE':
        sugerencia = `${nombreEnte} determina que la solicitud de crédito no es viable. Motivos específicos que sustentan la decisión: [describir los motivos evaluados]. Esta decisión da por terminado el trámite de la solicitud.`;
        break;

      case 'SOLICITA_AJUSTES':
        sugerencia = `${nombreEnte} solicita ajustes a la solicitud de crédito antes de emitir una decisión definitiva. Ajustes, aclaraciones o documentos requeridos: [detallar los requerimientos]. Una vez atendidos, la solicitud podrá presentarse nuevamente conforme al procedimiento institucional.`;
        break;
    }

    this.concepto = sugerencia;
    this.ultimoConceptoSugerido = sugerencia;
  }


  // =========================================================
  // ENTE ACTUAL
  // =========================================================

  get nombreEnteActual(): string {

    const idEnte =
      this.solicitudSeleccionada
        ?.idEnteActual;

    switch (idEnte) {

      case ENTES_APROBACION.GERENCIA:
        return 'Gerencia';

      case ENTES_APROBACION.COMITE:
        return 'Comité';

      case ENTES_APROBACION.CONSEJO:
        return 'Consejo de Administración';

      default:
        return 'Sin ente asignado';
    }
  }


  // =========================================================
  // REQUIERE ACTA
  // =========================================================

  get requiereActa(): boolean {

    const idEnte =
      this.solicitudSeleccionada
        ?.idEnteActual;

    return (
      idEnte ===
        ENTES_APROBACION.COMITE ||

      idEnte ===
        ENTES_APROBACION.CONSEJO
    );
  }


  // =========================================================
  // VALIDACIÓN DEL FORMULARIO
  // =========================================================

  get formularioDecisionValido():
    boolean {

    if (
      !this.solicitudSeleccionada ||
      this.idAprobacionDecision == null ||
      !this.decisionSeleccionada ||
      !this.concepto.trim()
    ) {
      return false;
    }

    if (
      this.requiereActa &&
      (
        !this.numeroActa.trim() ||
        !this.fechaActa
      )
    ) {
      return false;
    }

    return true;
  }


  // =========================================================
  // REGISTRAR DECISIÓN
  // =========================================================

  registrarDecision(): void {

    if (!this.puedeAprobar) {

      this.error =
        'El usuario no tiene permiso para registrar decisiones de aprobación.';

      return;
    }

    const solicitud =
      this.solicitudSeleccionada;

    if (
      !solicitud ||
      this.registrandoDecision ||
      this.cargandoDetalle
    ) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    if (!this.formularioDecisionValido) {

      this.error =
        'Complete la decisión, el concepto y los datos del acta cuando sean obligatorios.';

      return;
    }

    const decision =
      this.decisionSeleccionada;

    if (!decision) {
      return;
    }

    const confirmado =
      window.confirm(
        '¿Confirma registrar la decisión "' +
        decision.nombreDecision +
        '" para la solicitud ' +
        solicitud.numeroSolicitud +
        '? Esta actuación quedará registrada en el historial.'
      );

    if (!confirmado) {
      return;
    }

    const request:
      SolicitudAprobacionDecisionRequest = {

      idAprobacionDecision:
        decision.idAprobacionDecision,

      concepto:
        this.concepto.trim(),

      numeroActa:
        this.numeroActa.trim() || null,

      fechaActa:
        this.fechaActa || null

    };

    this.registrandoDecision = true;

    this.api.registrarDecision(
      solicitud.idSolicitudCredito,
      request
    ).subscribe({

      next: idActuacion => {

        this.registrandoDecision = false;

        this.mensaje =
          'Decisión registrada correctamente. ' +
          'Actuación N.º ' +
          idActuacion +
          '.';

        this.limpiarFormularioDecision();

        /*
         * La solicitud puede cambiar de ente,
         * regresar a documentación o finalizar.
         *
         * Por eso cerramos el detalle y
         * actualizamos la bandeja.
         */

        this.solicitudSeleccionada = null;

        this.fotosActuales = null;

        this.fotosHistoricas = null;

        this.actuacionSeleccionada = null;

        this.historial = [];

        this.recargarBandeja();
      },

      error: error => {

        this.registrandoDecision = false;

        this.error =
          this.obtenerMensajeError(
            error,
            'No fue posible registrar la decisión de aprobación.'
          );
      }

    });
  }


  // =========================================================
  // LIMPIAR FORMULARIO
  // =========================================================

  limpiarFormularioDecision(): void {

    this.idAprobacionDecision = null;

    this.concepto = '';

    this.numeroActa = '';

    this.fechaActa = '';
  }


  // =========================================================
  // IDENTIFICACIÓN DE FILAS
  // =========================================================

  trackBySolicitud(
    _index: number,
    solicitud: SolicitudAprobacionBandeja
  ): number {

    return solicitud.idSolicitudCredito;
  }


  trackByActuacion(
    _index: number,
    actuacion: SolicitudAprobacionActuacion
  ): number {

    return actuacion.idSolicitudAprobacion;
  }


  // =========================================================
  // MANEJO DE ERRORES
  // =========================================================

  private obtenerMensajeError(
    error: any,
    mensajePredeterminado: string
  ): string {

    const mensajeBackend =
      error?.error?.message
      ?? error?.error?.mensaje
      ?? error?.error?.error;

    if (
      typeof mensajeBackend === 'string' &&
      mensajeBackend.trim()
    ) {

      return mensajeBackend.trim();
    }

    return mensajePredeterminado;
  }

}
