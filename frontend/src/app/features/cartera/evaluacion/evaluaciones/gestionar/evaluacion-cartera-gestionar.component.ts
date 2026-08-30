import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  HeaderActionsComponent
} from '../../../../../shared/header-actions/header-actions.component';

import {
  EvaluacionCarteraApi
} from '../evaluacion-cartera.api';

import {
  EvaluacionCartera,
  EstadoEvaluacionCartera,
  ESTADO_EVALUACION_DESCRIPCION
} from '../evaluacion-cartera.models';


@Component({
  selector: 'app-evaluacion-cartera-gestionar',
  standalone: true,
  imports: [
    CommonModule,
    HeaderActionsComponent
  ],
  templateUrl: './evaluacion-cartera-gestionar.component.html',
  styleUrl: './evaluacion-cartera-gestionar.component.scss'
})
export class EvaluacionCarteraGestionarComponent implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly api =
    inject(EvaluacionCarteraApi);

  // =========================================================
  // IDENTIFICACIÓN
  // =========================================================

  idEvaluacionCartera:
    number | null = null;

  evaluacion:
    EvaluacionCartera | null = null;

  // =========================================================
  // ESTADO
  // =========================================================

  cargando = false;

  ejecutando = false;

  marcandoDefinitiva = false;

  error = '';

  mensaje = '';

  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    const id =
      Number(
        this.route.snapshot.paramMap.get(
          'idEvaluacionCartera'
        )
      );

    if (
      !Number.isInteger(id)
      || id <= 0
    ) {

      this.error =
        'El identificador de la evaluación no es válido.';

      return;
    }

    this.idEvaluacionCartera = id;

    this.cargarEvaluacion();
  }

  // =========================================================
  // CARGAR EVALUACIÓN
  // =========================================================

  cargarEvaluacion(): void {

    if (
      this.idEvaluacionCartera === null
      || this.cargando
    ) {
      return;
    }

    this.cargando = true;

    this.error = '';

    this.mensaje = '';

    this.api
      .buscarPorId(
        this.idEvaluacionCartera
      )
      .subscribe({

        next: (
          evaluacion:
            EvaluacionCartera
        ) => {

          this.evaluacion =
            evaluacion;

          this.cargando = false;
        },

        error: (
          error:
            unknown
        ) => {

          console.error(
            'Error cargando la evaluación de cartera:',
            error
          );

          this.evaluacion =
            null;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible cargar la evaluación de cartera.'
            );

          this.cargando = false;
        }
      });
  }

  // =========================================================
  // EJECUTAR EVALUACIÓN
  // =========================================================

  ejecutarEvaluacion(): void {

    if (
      this.idEvaluacionCartera === null
      || !this.evaluacion
      || !this.estaEnProceso
      || this.ejecutando
      || this.marcandoDefinitiva
    ) {
      return;
    }

    const mensajeConfirmacion =
      this.tieneResultados
        ? (
          'La evaluación será ejecutada nuevamente. '
          + 'Los resultados actuales serán recalculados '
          + 'utilizando la información y las reglas vigentes. '
          + '¿Desea continuar?'
        )
        : (
          'Se ejecutará la evaluación de cartera '
          + 'para el corte seleccionado. '
          + '¿Desea continuar?'
        );

    const continuar =
      window.confirm(
        mensajeConfirmacion
      );

    if (
      !continuar
    ) {
      return;
    }

    this.ejecutando = true;

    this.error = '';

    this.mensaje = '';

    this.api
      .ejecutar(
        this.idEvaluacionCartera
      )
      .subscribe({

        next: () => {

          this.mensaje =
            this.tieneResultados
              ? 'La evaluación de cartera fue recalculada correctamente.'
              : 'La evaluación de cartera fue ejecutada correctamente.';

          this.ejecutando = false;

          this.recargarDespuesDeProceso();
        },

        error: (
          error:
            unknown
        ) => {

          console.error(
            'Error ejecutando la evaluación de cartera:',
            error
          );

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible ejecutar la evaluación de cartera.'
            );

          this.ejecutando = false;
        }
      });
  }

  // =========================================================
  // MARCAR DEFINITIVA
  // =========================================================

  marcarDefinitiva(): void {

    if (
      this.idEvaluacionCartera === null
      || !this.evaluacion
      || !this.estaEnProceso
      || !this.tieneResultados
      || this.marcandoDefinitiva
      || this.ejecutando
    ) {
      return;
    }

    const continuar =
      window.confirm(
        'La evaluación será marcada como definitiva. '
        + 'Después de esta operación no podrá volver a ejecutarse. '
        + '¿Desea continuar?'
      );

    if (
      !continuar
    ) {
      return;
    }

    this.marcandoDefinitiva = true;

    this.error = '';

    this.mensaje = '';

    this.api
      .marcarDefinitiva(
        this.idEvaluacionCartera
      )
      .subscribe({

        next: (
          evaluacion:
            EvaluacionCartera
        ) => {

          this.evaluacion =
            evaluacion;

          this.mensaje =
            'La evaluación de cartera fue marcada como definitiva.';

          this.marcandoDefinitiva =
            false;
        },

        error: (
          error:
            unknown
        ) => {

          console.error(
            'Error marcando la evaluación como definitiva:',
            error
          );

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible marcar la evaluación como definitiva.'
            );

          this.marcandoDefinitiva =
            false;
        }
      });
  }

  // =========================================================
  // VER RESULTADOS
  // =========================================================

  verResultados(): void {

    if (
      this.idEvaluacionCartera === null
      || !this.evaluacion
      || !this.tieneResultados
    ) {
      return;
    }

    this.router.navigate([
      '/cartera/evaluacion/evaluaciones',
      this.idEvaluacionCartera,
      'resultados'
    ]);
  }

  // =========================================================
  // VOLVER
  // =========================================================

  volver(): void {

    this.router.navigate([
      '/cartera/evaluacion/evaluaciones'
    ]);
  }

  // =========================================================
  // DESCRIPCIÓN DEL ESTADO
  // =========================================================

  descripcionEstado(
    estado:
      EstadoEvaluacionCartera
  ): string {

    return (
      ESTADO_EVALUACION_DESCRIPCION[
        estado
      ]
      ?? estado
    );
  }

  // =========================================================
  // INDICADORES
  // =========================================================

  get estaEnProceso(): boolean {

    return (
      this.evaluacion?.estado
      === 'P'
    );
  }

  get esDefinitiva(): boolean {

    return (
      this.evaluacion?.estado
      === 'D'
    );
  }

  // =========================================================
  // RESULTADOS
  // =========================================================

  get tieneResultados(): boolean {

    return (
      (
        this.evaluacion
          ?.cantidadCreditos
        ?? 0
      ) > 0
    );
  }

  // =========================================================
  // PENDIENTE DE EJECUTAR
  // =========================================================

  get pendienteEjecucion(): boolean {

    return (
      this.estaEnProceso
      && !this.tieneResultados
    );
  }

  // =========================================================
  // EJECUTADA EN REVISIÓN
  // =========================================================

  get ejecutadaEnRevision(): boolean {

    return (
      this.estaEnProceso
      && this.tieneResultados
    );
  }

  // =========================================================
  // PUEDE EJECUTAR
  // =========================================================

  get puedeEjecutar(): boolean {

    return (
      this.estaEnProceso
      && !this.procesando
    );
  }

  // =========================================================
  // PUEDE VER RESULTADOS
  // =========================================================

  get puedeVerResultados(): boolean {

    return (
      this.tieneResultados
      && !this.procesando
    );
  }

  // =========================================================
  // PUEDE MARCAR DEFINITIVA
  // =========================================================

  get puedeMarcarDefinitiva(): boolean {

    return (
      this.estaEnProceso
      && this.tieneResultados
      && !this.procesando
    );
  }

  // =========================================================
  // TEXTO BOTÓN EJECUCIÓN
  // =========================================================

  get textoBotonEjecutar(): string {

    if (
      this.ejecutando
    ) {

      return this.tieneResultados
        ? 'RECALCULANDO...'
        : 'EJECUTANDO...';
    }

    return this.tieneResultados
      ? 'VOLVER A EJECUTAR'
      : 'EJECUTAR EVALUACIÓN';
  }

  // =========================================================
  // PROCESANDO
  // =========================================================

  get procesando(): boolean {

    return (
      this.cargando
      || this.ejecutando
      || this.marcandoDefinitiva
    );
  }

  // =========================================================
  // RECARGAR DESPUÉS DE EJECUTAR
  // =========================================================

  private recargarDespuesDeProceso(): void {

    if (
      this.idEvaluacionCartera === null
    ) {
      return;
    }

    this.api
      .buscarPorId(
        this.idEvaluacionCartera
      )
      .subscribe({

        next: (
          evaluacion:
            EvaluacionCartera
        ) => {

          this.evaluacion =
            evaluacion;
        },

        error: (
          error:
            unknown
        ) => {

          console.error(
            'Error recargando la evaluación:',
            error
          );

          this.error =
            this.obtenerMensajeError(
              error,
              'La evaluación fue ejecutada, pero no fue posible actualizar el resumen.'
            );
        }
      });
  }

  // =========================================================
  // MENSAJE DE ERROR
  // =========================================================

  private obtenerMensajeError(
    error:
      unknown,
    mensajePredeterminado:
      string
  ): string {

    if (
      !error
      || typeof error !== 'object'
    ) {

      return mensajePredeterminado;
    }

    const respuesta =
      error as {
        message?: string;
        error?: {
          mensaje?: string;
          message?: string;
        };
      };

    return (
      respuesta.error?.mensaje
      ?? respuesta.error?.message
      ?? respuesta.message
      ?? mensajePredeterminado
    );
  }
}
