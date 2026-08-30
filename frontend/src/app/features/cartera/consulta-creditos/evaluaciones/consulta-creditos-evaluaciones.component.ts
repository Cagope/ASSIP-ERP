import {
  CommonModule
} from '@angular/common';

import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  ConsultaCreditoDetalle,
  ConsultaCreditoEvaluacion
} from '../consulta-creditos.models';


@Component({
  selector: 'app-consulta-creditos-evaluaciones',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl:
    './consulta-creditos-evaluaciones.component.html',
  styleUrls: [
    './consulta-creditos-evaluaciones.component.scss'
  ]
})
export class ConsultaCreditosEvaluacionesComponent {

  // =========================================================
  // ENTRADAS
  // =========================================================

  @Input({
    required: true
  })
  credito!:
    ConsultaCreditoDetalle;

  @Input()
  evaluaciones:
    ConsultaCreditoEvaluacion[] = [];


  // =========================================================
  // SALIDAS
  // =========================================================

  @Output()
  cerrar =
    new EventEmitter<void>();


  // =========================================================
  // INDICADORES GENERALES
  // =========================================================

  get cantidadEvaluaciones(): number {

    return (
      this.evaluaciones?.length
      ?? 0
    );

  }

  get tieneEvaluaciones(): boolean {

    return this.cantidadEvaluaciones > 0;

  }


  // =========================================================
  // ÚLTIMA EVALUACIÓN
  // =========================================================

  get ultimaEvaluacion():
    ConsultaCreditoEvaluacion | null {

    if (
      !this.evaluaciones
      || this.evaluaciones.length === 0
    ) {
      return null;
    }

    const marcada =
      this.evaluaciones.find(
        evaluacion =>
          evaluacion.ultimaEvaluacionCredito
          === true
      );

    if (marcada) {
      return marcada;
    }

    return this.evaluaciones.reduce(
      (
        ultima,
        actual
      ) => {

        if (!ultima) {
          return actual;
        }

        const fechaUltima =
          this.fechaComparable(
            ultima.fechaEvaluacion
          );

        const fechaActual =
          this.fechaComparable(
            actual.fechaEvaluacion
          );

        if (
          fechaActual > fechaUltima
        ) {
          return actual;
        }

        if (
          fechaActual === fechaUltima
          &&
          Number(
            actual.idEvaluacionCartera
            ?? 0
          )
          >
          Number(
            ultima.idEvaluacionCartera
            ?? 0
          )
        ) {
          return actual;
        }

        return ultima;

      },
      null as ConsultaCreditoEvaluacion | null
    );

  }


  // =========================================================
  // RESUMEN DE ACCIONES
  // =========================================================

  get cantidadRecalificadas(): number {

    return this.evaluaciones.filter(
      evaluacion =>
        this.codigoAccion(
          evaluacion
        ) === 'R'
    ).length;

  }

  get cantidadHabilitadas(): number {

    return this.evaluaciones.filter(
      evaluacion =>
        this.codigoAccion(
          evaluacion
        ) === 'H'
    ).length;

  }

  get cantidadMantenidas(): number {

    return this.evaluaciones.filter(
      evaluacion =>
        this.codigoAccion(
          evaluacion
        ) === 'M'
    ).length;

  }


  // =========================================================
  // RESUMEN DE VARIACIÓN
  // =========================================================

  get cantidadMejoras(): number {

    return this.evaluaciones.filter(
      evaluacion =>
        evaluacion.riesgoMejora === true
        ||
        this.normalizarTexto(
          evaluacion.variacionRiesgo
        ) === 'MEJORA'
    ).length;

  }

  get cantidadDeterioros(): number {

    return this.evaluaciones.filter(
      evaluacion =>
        evaluacion.riesgoDeteriora === true
        ||
        this.normalizarTexto(
          evaluacion.variacionRiesgo
        ) === 'DETERIORO'
    ).length;

  }

  get cantidadIguales(): number {

    return this.evaluaciones.filter(
      evaluacion =>
        evaluacion.riesgoIgual === true
        ||
        this.normalizarTexto(
          evaluacion.variacionRiesgo
        ) === 'IGUAL'
    ).length;

  }


  // =========================================================
  // ACCIONES
  // =========================================================

  cerrarDetalle(): void {

    this.cerrar.emit();

  }


  // =========================================================
  // DESCRIPCIONES
  // =========================================================

  descripcionAccion(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): string {

    const codigo =
      this.codigoAccion(
        evaluacion
      );

    switch (codigo) {

      case 'R':
        return 'Recalificar';

      case 'H':
        return 'Habilitar';

      case 'M':
        return 'Mantener';

      default:
        return (
          evaluacion.resultadoEvaluacion
          ?? evaluacion.accionEvaluacion
          ?? ''
        );

    }

  }

  descripcionVariacion(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): string {

    if (
      evaluacion.riesgoMejora === true
    ) {
      return 'Mejora';
    }

    if (
      evaluacion.riesgoDeteriora === true
    ) {
      return 'Deterioro';
    }

    if (
      evaluacion.riesgoIgual === true
    ) {
      return 'Igual';
    }

    const variacion =
      this.normalizarTexto(
        evaluacion.variacionRiesgo
      );

    switch (variacion) {

      case 'MEJORA':
        return 'Mejora';

      case 'DETERIORO':
        return 'Deterioro';

      case 'IGUAL':
        return 'Igual';

      default:
        return (
          evaluacion.variacionRiesgo
          ?? ''
        );

    }

  }

  descripcionEstado(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): string {

    if (
      evaluacion.evaluacionAplicada === true
    ) {
      return 'Aplicada';
    }

    if (
      evaluacion.evaluacionAplicada === false
    ) {
      return 'No aplicada';
    }

    return '';

  }

  descripcionAlerta(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): string {

    return (
      evaluacion.motivoAlertaEvaluacion
      ?? evaluacion.nivelAlertaEvaluacion
      ?? ''
    );

  }


  // =========================================================
  // EDADES
  // =========================================================

  edadAnterior(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): string {

    return (
      evaluacion.edadRiesgoAnterior
      ?? ''
    );

  }

  edadInicial(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): string {

    return (
      evaluacion.edadRiesgoInicial
      ?? ''
    );

  }

  edadMora(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): string {

    return (
      evaluacion.edadMora
      ?? ''
    );

  }

  edadEvaluada(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): string {

    return (
      evaluacion.edadRiesgoEvaluada
      ?? ''
    );

  }

  edadFinal(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): string {

    return (
      evaluacion.edadRiesgoNueva
      ?? ''
    );

  }


  // =========================================================
  // VALIDACIONES VISUALES
  // =========================================================

  esMejora(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): boolean {

    return (
      evaluacion.riesgoMejora === true
      ||
      this.normalizarTexto(
        evaluacion.variacionRiesgo
      ) === 'MEJORA'
    );

  }

  esDeterioro(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): boolean {

    return (
      evaluacion.riesgoDeteriora === true
      ||
      this.normalizarTexto(
        evaluacion.variacionRiesgo
      ) === 'DETERIORO'
    );

  }

  esIgual(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): boolean {

    return (
      evaluacion.riesgoIgual === true
      ||
      this.normalizarTexto(
        evaluacion.variacionRiesgo
      ) === 'IGUAL'
    );

  }

  requiereRevision(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): boolean {

    return (
      evaluacion.requiereRevision
      === true
    );

  }

  esUltimaEvaluacion(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): boolean {

    if (
      evaluacion.ultimaEvaluacionCredito
      === true
    ) {
      return true;
    }

    return (
      this.ultimaEvaluacion
        ?.idEvaluacionCartera
      === evaluacion.idEvaluacionCartera
    );

  }


  // =========================================================
  // TRACK BY
  // =========================================================

  trackByEvaluacion(
    indice: number,
    evaluacion:
      ConsultaCreditoEvaluacion
  ): number {

    return (
      evaluacion.idEvaluacionCartera
      ?? indice
    );

  }


  // =========================================================
  // UTILIDADES
  // =========================================================

  private codigoAccion(
    evaluacion:
      ConsultaCreditoEvaluacion
  ): string {

    const codigo =
      this.normalizarTexto(
        evaluacion.accionEvaluacion
      );

    if (
      codigo === 'R'
      || codigo === 'H'
      || codigo === 'M'
    ) {
      return codigo;
    }

    const resultado =
      this.normalizarTexto(
        evaluacion.resultadoEvaluacion
      );

    if (
      resultado.includes(
        'RECALIFIC'
      )
    ) {
      return 'R';
    }

    if (
      resultado.includes(
        'HABILIT'
      )
    ) {
      return 'H';
    }

    if (
      resultado.includes(
        'MANTEN'
      )
    ) {
      return 'M';
    }

    return '';

  }

  private normalizarTexto(
    valor:
      string | null | undefined
  ): string {

    return String(
      valor
      ?? ''
    )
      .trim()
      .toUpperCase();

  }

  private fechaComparable(
    fecha:
      string | null | undefined
  ): number {

    if (!fecha) {
      return 0;
    }

    const valor =
      Date.parse(
        `${fecha}T00:00:00`
      );

    return Number.isNaN(
      valor
    )
      ? 0
      : valor;

  }

}
