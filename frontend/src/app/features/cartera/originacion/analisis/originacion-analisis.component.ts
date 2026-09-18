import {
  CommonModule
} from '@angular/common';

import {
  Component,
  OnInit
} from '@angular/core';

import {
  Router
} from '@angular/router';

import {
  forkJoin
} from 'rxjs';

import {
  OriginacionSolicitudStateService
} from '../solicitud/originacion-solicitud-state.service';

import {
  OriginacionAnalisisApi
} from './originacion-analisis.api';

import {
  SolicitudAnalisisComponente,
  SolicitudAnalisisDeudor,
  SolicitudAnalisisPersistencia,
  SolicitudAnalisisResultado
} from './originacion-analisis.models';

import {
  SolicitudValidacionAprobacion
} from '../solicitud/originacion-solicitud.models';

@Component({
  selector: 'app-originacion-analisis',
  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl:
    './originacion-analisis.component.html',

  styleUrl:
    './originacion-analisis.component.scss'
})
export class OriginacionAnalisisComponent
  implements OnInit {


  // =========================================================
  // SOLICITUD
  // =========================================================

  idSolicitudCredito: number | null = null;


  // =========================================================
  // RESULTADO GENERAL
  // =========================================================

  resultado:
    SolicitudAnalisisResultado | null = null;


  // =========================================================
  // DEUDORES
  // =========================================================

  deudores:
    SolicitudAnalisisDeudor[] = [];

  deudorSeleccionado:
    SolicitudAnalisisDeudor | null = null;


  // =========================================================
  // COMPONENTES
  // =========================================================

  componentes:
    SolicitudAnalisisComponente[] = [];

  componentesSeleccionados:
    SolicitudAnalisisComponente[] = [];


  // =========================================================
  // PERSISTENCIA
  // =========================================================

  resultadoPersistencia:
    SolicitudAnalisisPersistencia | null = null;

  // =========================================================
  // APROBACIÓN
  // =========================================================

  conceptoAsesorAprobacion = '';

  validacionAprobacion:
    SolicitudValidacionAprobacion | null = null;

  validandoAprobacion = false;
  enviandoAprobacion = false;

  // =========================================================
  // ESTADOS
  // =========================================================

  cargando = false;
  guardando = false;

  error = '';
  mensaje = '';


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly api:
      OriginacionAnalisisApi,

    private readonly state:
      OriginacionSolicitudStateService,

    private readonly router:
      Router
  ) {}


  // =========================================================
  // INICIO
  // =========================================================

  ngOnInit(): void {

    this.idSolicitudCredito =
      this.state.getIdSolicitudCredito();

    if (!this.idSolicitudCredito) {

      this.error =
        'No se encontró una solicitud de crédito activa.';

      return;
    }

    this.cargarAnalisis();
  }


  // =========================================================
  // CARGAR ANÁLISIS
  // =========================================================

  cargarAnalisis(): void {

    if (!this.idSolicitudCredito) {
      return;
    }

    this.cargando = true;

    this.error = '';
    this.mensaje = '';

    forkJoin({
      resultado:
        this.api.obtenerResultado(
          this.idSolicitudCredito
        ),

      deudores:
        this.api.listarDeudores(
          this.idSolicitudCredito
        ),

      componentes:
        this.api.listarComponentes(
          this.idSolicitudCredito
        )
    }).subscribe({

      next: ({
        resultado,
        deudores,
        componentes
      }) => {

        this.resultado =
          resultado;

        this.deudores =
          deudores ?? [];

        this.componentes =
          componentes ?? [];

        this.sincronizarSeleccion();

        this.cargando = false;
      },

      error: error => {

        this.cargando = false;

        this.error =
          this.obtenerMensajeError(
            error,
            'No fue posible cargar el análisis de la solicitud.'
          );
      }
    });
  }


  // =========================================================
  // SINCRONIZAR SELECCIÓN
  // =========================================================

  private sincronizarSeleccion(): void {

    if (this.deudores.length === 0) {

      this.deudorSeleccionado = null;
      this.componentesSeleccionados = [];

      return;
    }

    /*
     * Si ya había un deudor seleccionado después de
     * recalcular, conservamos la selección.
     */
    if (this.deudorSeleccionado) {

      const encontrado =
        this.deudores.find(
          deudor =>
            deudor.idSolicitudDeudor ===
            this.deudorSeleccionado?.idSolicitudDeudor
        );

      if (encontrado) {

        this.seleccionarDeudor(
          encontrado
        );

        return;
      }
    }

    /*
     * Primera carga:
     * seleccionamos el primer deudor según el orden
     * recibido del backend.
     */
    this.seleccionarDeudor(
      this.deudores[0]
    );
  }


  // =========================================================
  // SELECCIONAR DEUDOR
  // =========================================================

  seleccionarDeudor(
    deudor: SolicitudAnalisisDeudor
  ): void {

    this.deudorSeleccionado =
      deudor;

    this.componentesSeleccionados =
      this.componentes
        .filter(
          componente =>
            componente.idSolicitudDeudor ===
            deudor.idSolicitudDeudor
        )
        .sort(
          (a, b) =>
            a.ordenComponente -
            b.ordenComponente
        );
  }


  // =========================================================
  // GUARDAR / RECALCULAR ANÁLISIS
  // =========================================================

  persistirAnalisis(): void {

    if (!this.idSolicitudCredito) {

      this.error =
        'No se encontró una solicitud de crédito activa.';

      return;
    }

    if (this.guardando) {
      return;
    }

    this.guardando = true;

    this.error = '';
    this.mensaje = '';
    this.resultadoPersistencia = null;

    this.api
      .persistir(
        this.idSolicitudCredito
      )
      .subscribe({

        next: resultado => {

          this.resultadoPersistencia =
            resultado;

          this.mensaje =
            this.construirMensajePersistencia(
              resultado
            );

          /*
           * Volvemos a consultar las vistas.
           *
           * Así la pantalla siempre representa el
           * cálculo actual de PostgreSQL después de
           * persistir el análisis.
           */
          this.recargarDespuesDePersistir();
        },

        error: error => {

          this.guardando = false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible guardar el análisis de la solicitud.'
            );
        }
      });
  }


  // =========================================================
  // RECARGAR DESPUÉS DE PERSISTIR
  // =========================================================

  private recargarDespuesDePersistir(): void {

    if (!this.idSolicitudCredito) {

      this.guardando = false;

      return;
    }

    forkJoin({
      resultado:
        this.api.obtenerResultado(
          this.idSolicitudCredito
        ),

      deudores:
        this.api.listarDeudores(
          this.idSolicitudCredito
        ),

      componentes:
        this.api.listarComponentes(
          this.idSolicitudCredito
        )
    }).subscribe({

      next: ({
        resultado,
        deudores,
        componentes
      }) => {

        this.resultado =
          resultado;

        this.deudores =
          deudores ?? [];

        this.componentes =
          componentes ?? [];

        this.sincronizarSeleccion();

        this.guardando = false;
      },

      error: error => {

        this.guardando = false;

        this.error =
          this.obtenerMensajeError(
            error,
            'El análisis fue guardado, pero no fue posible actualizar la información en pantalla.'
          );
      }
    });
  }


  // =========================================================
  // MENSAJE DE PERSISTENCIA
  // =========================================================

  private construirMensajePersistencia(
    resultado: SolicitudAnalisisPersistencia
  ): string {

    const estado =
      resultado.estadoAnalisis
        ? ` Estado: ${resultado.estadoAnalisis}.`
        : '';

    const recomendacion =
      resultado.recomendacion
        ? ` Recomendación: ${resultado.recomendacion}.`
        : '';

    return (
      'Análisis actualizado correctamente.' +
      estado +
      recomendacion
    );
  }


  // =========================================================
  // INDICADORES DE ESTADO
  // =========================================================

  get analisisCompleto(): boolean {

    return this.resultado
      ?.analisisSolicitudCompleto === true;
  }


  get tienePendientes(): boolean {

    return (
      (this.resultado
        ?.cantidadPersonasPendientes ?? 0) > 0
    );
  }


  get solicitudViable(): boolean {

    return this.resultado
      ?.cumpleOtorgamientoSolicitud === true;
  }

  // =========================================================
  // CONCEPTO DEL ASESOR
  // =========================================================

  actualizarConceptoAsesor(
    valor: string
  ): void {

    this.conceptoAsesorAprobacion =
      valor ?? '';

    this.validacionAprobacion = null;
  }


  get conceptoAsesorValido(): boolean {

    const concepto =
      this.conceptoAsesorAprobacion.trim();

    return (
      concepto.length > 0 &&
      concepto.length <= 1000
    );
  }


  // =========================================================
  // VALIDAR EXPEDIENTE PARA APROBACIÓN
  // =========================================================

  validarParaAprobacion(): void {

    if (!this.idSolicitudCredito) {

      this.error =
        'No se encontró una solicitud de crédito activa.';

      return;
    }

    if (this.validandoAprobacion) {
      return;
    }

    this.validandoAprobacion = true;

    this.error = '';
    this.mensaje = '';
    this.validacionAprobacion = null;

    this.api
      .validarParaAprobacion(
        this.idSolicitudCredito
      )
      .subscribe({

        next: validacion => {

          this.validacionAprobacion =
            validacion;

          this.validandoAprobacion = false;

          this.mensaje =
            validacion.mensaje;
        },

        error: error => {

          this.validandoAprobacion = false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible validar el expediente para aprobación.'
            );
        }
      });
  }


  // =========================================================
  // ENVIAR A APROBACIÓN
  // =========================================================

  enviarAprobacion(): void {

    if (!this.idSolicitudCredito) {

      this.error =
        'No se encontró una solicitud de crédito activa.';

      return;
    }

    if (!this.conceptoAsesorValido) {

      this.error =
        'El concepto del asesor es obligatorio y no puede superar los 1000 caracteres.';

      return;
    }

    if (
      !this.validacionAprobacion
        ?.puedeEnviarAprobacion
    ) {

      this.error =
        'Primero debe validar el expediente y confirmar que está completo.';

      return;
    }

    if (this.enviandoAprobacion) {
      return;
    }

    this.enviandoAprobacion = true;

    this.error = '';
    this.mensaje = '';

    this.api
      .enviarAprobacion(
        this.idSolicitudCredito,
        this.conceptoAsesorAprobacion
      )
      .subscribe({

        next: respuesta => {

          this.enviandoAprobacion = false;

          this.mensaje =
            `Solicitud ${respuesta.numeroSolicitud} enviada a ${respuesta.nombreEnteAprobacion ?? 'aprobación'}.`;

          this.router.navigate([
            '/cartera/originacion'
          ]);
        },

        error: error => {

          this.enviandoAprobacion = false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible enviar la solicitud a aprobación.'
            );
        }
      });
  }


  // =========================================================
  // CLASE DEL RESULTADO GENERAL
  // =========================================================

  claseResultadoSolicitud(): string {

    if (!this.resultado) {
      return 'estado--neutral';
    }

    if (
      !this.resultado
        .analisisSolicitudCompleto
    ) {
      return 'estado--pendiente';
    }

    if (
      this.resultado
        .cumpleOtorgamientoSolicitud === true
    ) {
      return 'estado--favorable';
    }

    if (
      this.resultado
        .cumpleOtorgamientoSolicitud === false
    ) {
      return 'estado--desfavorable';
    }

    return 'estado--neutral';
  }


  // =========================================================
  // CLASE DEL DEUDOR
  // =========================================================

  claseResultadoDeudor(
    deudor: SolicitudAnalisisDeudor
  ): string {

    if (!deudor.analisisCompleto) {
      return 'estado--pendiente';
    }

    if (
      deudor.cumpleOtorgamiento === true
    ) {
      return 'estado--favorable';
    }

    if (
      deudor.cumpleOtorgamiento === false
    ) {
      return 'estado--desfavorable';
    }

    return 'estado--neutral';
  }


  // =========================================================
  // CLASE DEL COMPONENTE
  // =========================================================

  claseComponente(
    componente: SolicitudAnalisisComponente
  ): string {

    if (
      componente.estadoComponente !==
      'EVALUADO'
    ) {
      return 'estado--pendiente';
    }

    if (
      componente.cumple === true
    ) {
      return 'estado--favorable';
    }

    if (
      componente.cumple === false
    ) {
      return 'estado--desfavorable';
    }

    return 'estado--neutral';
  }


  // =========================================================
  // TEXTO TIPO DEUDOR
  // =========================================================

  descripcionTipoDeudor(
    tipoDeudor: string | null | undefined
  ): string {

    const tipo =
      (tipoDeudor ?? '')
        .trim()
        .toUpperCase();

    switch (tipo) {

      case 'D':
      case 'DEUDOR':
      case 'PRINCIPAL':
        return 'Deudor principal';

      case 'C':
      case 'CODEUDOR':
        return 'Codeudor';

      default:
        return tipoDeudor || 'Deudor';
    }
  }


  // =========================================================
  // TEXTO BOOLEANO
  // =========================================================

  textoCumple(
    valor: boolean | null | undefined
  ): string {

    if (valor === true) {
      return 'Sí';
    }

    if (valor === false) {
      return 'No';
    }

    return 'Pendiente';
  }


  // =========================================================
  // TEXTO ESTADO COMPONENTE
  // =========================================================

  textoEstadoComponente(
    componente: SolicitudAnalisisComponente
  ): string {

    if (
      componente.estadoComponente ===
      'EVALUADO'
    ) {

      return componente.descripcionResultado
        || 'Evaluado';
    }

    return componente.estadoComponente
      || 'Pendiente';
  }


  // =========================================================
  // VOLVER
  // =========================================================

  volver(): void {

    this.router.navigate([
      '/cartera/originacion/central-riesgo'
    ]);
  }


  // =========================================================
  // ERRORES
  // =========================================================

  private obtenerMensajeError(
    error: any,
    mensajeDefecto: string
  ): string {

    if (
      typeof error?.error === 'string'
      &&
      error.error.trim()
    ) {
      return error.error;
    }

    if (
      typeof error?.error?.message === 'string'
      &&
      error.error.message.trim()
    ) {
      return error.error.message;
    }

    if (
      typeof error?.message === 'string'
      &&
      error.message.trim()
    ) {
      return error.message;
    }

    return mensajeDefecto;
  }
}
