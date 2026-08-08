import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

import {
  ExpedienteCredito
} from '../expediente-asociado.dto';

@Component({
  selector: 'app-expediente-creditos',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-creditos.component.html',
  styleUrls: ['./expediente-creditos.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteCreditosComponent {

  private creditosEntrada: ExpedienteCredito[] = [];

  // =========================================================
  // Entrada
  // =========================================================

  @Input()
  set creditos(
    valor: ExpedienteCredito[] | null | undefined
  ) {
    this.creditosEntrada =
      Array.isArray(valor)
        ? valor
        : [];
  }

  get creditos(): ExpedienteCredito[] {
    return this.creditosEntrada;
  }

  // =========================================================
  // Créditos visibles
  // =========================================================

  /**
   * El expediente muestra únicamente obligaciones
   * que actualmente tienen saldo pendiente.
   */
  get lista(): ExpedienteCredito[] {

    return this.creditosEntrada.filter(
      credito =>
        this.valorNumerico(
          credito.saldoActual
        ) > 0
    );

  }

  get tieneCreditos(): boolean {
    return this.lista.length > 0;
  }

  get cantidadCreditos(): number {
    return this.lista.length;
  }

  // =========================================================
  // Seguimiento
  // =========================================================

  get cantidadVigentes(): number {

    return this.lista.filter(
      credito =>
        credito.vigente === true
    ).length;

  }

  get cantidadEnMora(): number {

    return this.lista.filter(
      credito =>
        credito.enMora === true
    ).length;

  }

  get cantidadJuridicos(): number {

    return this.lista.filter(
      credito =>
        credito.juridico === true
    ).length;

  }

  get cantidadConRevision(): number {

    return this.lista.filter(
      credito =>
        credito.requiereRevision === true
    ).length;

  }

  // =========================================================
  // Resumen
  // =========================================================

  get saldoActualTotal(): number {

    return this.lista.reduce(
      (
        total,
        credito
      ) =>
        total +
        this.valorNumerico(
          credito.saldoActual
        ),
      0
    );

  }

  get moraMaxima(): number {

    if (!this.lista.length) {
      return 0;
    }

    return Math.max(
      ...this.lista.map(
        credito =>
          Math.max(
            0,
            this.valorNumerico(
              credito.diasMora
            )
          )
      )
    );

  }

  // =========================================================
  // Estado visual
  // =========================================================

  claseEstado(
    credito: ExpedienteCredito
  ): string {

    if (credito.castigado === true) {
      return 'estado--castigado';
    }

    if (credito.juridico === true) {
      return 'estado--juridico';
    }

    if (credito.enMora === true) {
      return 'estado--mora';
    }

    if (credito.cancelado === true) {
      return 'estado--cancelado';
    }

    if (credito.vigente === true) {
      return 'estado--activo';
    }

    return 'estado--neutral';

  }

  claseRiesgo(
    credito: ExpedienteCredito
  ): string {

    const riesgo =
      (credito.edadRiesgo ?? '')
        .trim()
        .toUpperCase();

    switch (riesgo) {

      case 'A':
        return 'riesgo--bajo';

      case 'B':
      case 'C':
        return 'riesgo--medio';

      case 'D':
      case 'E':
        return 'riesgo--alto';

      default:
        return 'riesgo--neutral';

    }

  }

  /**
   * Solo se consideran situaciones objetivas.
   *
   * No se utiliza requiereRevision porque actualmente
   * la vista lo entrega en true para créditos normales.
   */
  requiereAtencion(
    credito: ExpedienteCredito
  ): boolean {

    return (
      credito.enMora === true ||
      credito.juridico === true ||
      credito.castigado === true ||
      this.valorNumerico(
        credito.diasMora
      ) > 0
    );

  }

  // =========================================================
  // Textos
  // =========================================================

  textoEstado(
    credito: ExpedienteCredito
  ): string {

    if (
      credito.nombreEstadoCartera
        ?.trim()
    ) {
      return credito.nombreEstadoCartera
        .trim();
    }

    if (
      credito.codigoEstadoCartera
        ?.trim()
    ) {
      return credito.codigoEstadoCartera
        .trim();
    }

    if (credito.castigado === true) {
      return 'Castigado';
    }

    if (credito.juridico === true) {
      return 'En jurídico';
    }

    if (credito.enMora === true) {
      return 'En mora';
    }

    if (credito.cancelado === true) {
      return 'Cancelado';
    }

    if (credito.vigente === true) {
      return 'Activo';
    }

    return 'Sin estado';

  }

  textoLinea(
    credito: ExpedienteCredito
  ): string {

    return (
      credito.nombreLineaCredito
        ?.trim()
      ||
      credito.codigoLineaCredito
        ?.trim()
      ||
      'Sin línea'
    );

  }

  textoNumeroCredito(
    credito: ExpedienteCredito
  ): string {

    return (
      credito.numeroCredito
        ?.trim()
      ||
      'Sin número'
    );

  }

  textoEdadRiesgo(
    credito: ExpedienteCredito
  ): string {

    return (
      credito.edadRiesgo
        ?.trim()
      ||
      'Sin clasificar'
    );

  }

  textoGarantia(
    credito: ExpedienteCredito
  ): string {

    return (
      credito.nombreGarantia
        ?.trim()
      ||
      credito.codigoGarantia
        ?.trim()
      ||
      'No registrada'
    );

  }

  textoEstadoJuridico(
    credito: ExpedienteCredito
  ): string {

    if (credito.juridico === true) {

      return (
        credito.nombreEstadoJuridico
          ?.trim()
        ||
        credito.codigoEstadoJuridico
          ?.trim()
        ||
        'Cobro jurídico'
      );

    }

    return (
      credito.nombreEstadoJuridico
        ?.trim()
      ||
      credito.codigoEstadoJuridico
        ?.trim()
      ||
      'Sin proceso jurídico'
    );

  }

  // =========================================================
  // TrackBy
  // =========================================================

  trackByCredito(
    indice: number,
    credito: ExpedienteCredito
  ): number {

    return credito.idCredito
      ?? indice;

  }

  // =========================================================
  // Utilidades
  // =========================================================

  private valorNumerico(
    valor: number | null | undefined
  ): number {

    const numero =
      Number(valor ?? 0);

    return Number.isFinite(numero)
      ? numero
      : 0;

  }

}
