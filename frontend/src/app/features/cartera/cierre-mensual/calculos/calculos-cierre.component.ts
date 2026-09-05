import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  CierreMensualCarteraApi
} from '../cierre-mensual-cartera.api';

import {
  CalculosCierreApi,
  ResumenAportesGarantias,
  ResumenControlesCalculos,
  ResumenEdadMora,
  DetalleCalculosCierre
} from './calculos-cierre.api';

import {
  CalculosCierreExporterService
} from './calculos-cierre-exporter.service';

@Component({
  selector: 'app-calculos-cierre',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './calculos-cierre.component.html',
  styleUrls: ['./calculos-cierre.component.scss']
})
export class CalculosCierreComponent implements OnInit {

  private readonly cierreApi =
    inject(CierreMensualCarteraApi);

  private readonly calculosApi =
    inject(CalculosCierreApi);

  private readonly exporter =
    inject(CalculosCierreExporterService);

  cierres: any[] = [];

  idCierreSeleccionado: number | null = null;

  cierreSeleccionado: any | null = null;

  cargando = false;
  procesando = false;
  cerrandoCalculos = false;

  cargandoResumen = false;
  cargandoControles = false;
  cargandoAportesGarantias = false;

  exportandoExcel = false;

  mensaje = '';
  error = '';

  cantidadProcesada: number | null = null;

  resumenEdadMora: ResumenEdadMora[] = [];

  resumenControles:
    ResumenControlesCalculos | null = null;

  resumenAportesGarantias:
    ResumenAportesGarantias | null = null;

  detalleCalculos: DetalleCalculosCierre[] = [];

  ngOnInit(): void {
    this.cargarCierres();
  }

  // =========================================================
  // CARGAR CIERRES
  // =========================================================

  cargarCierres(
    conservarSeleccion = false
  ): void {

    this.cargando = true;
    this.error = '';

    const idActual =
      conservarSeleccion
        ? this.idCierreSeleccionado
        : null;

    this.cierreApi
      .listar()
      .subscribe({

        next: (data) => {

          this.cierres =
            data ?? [];

          this.cargando = false;

          if (
            conservarSeleccion
            && idActual
          ) {

            this.idCierreSeleccionado =
              idActual;

            this.actualizarCierreSeleccionado();

          }

        },

        error: (err) => {

          this.cargando = false;

          this.error =
            err?.error?.message
            ?? err?.error
            ?? 'No fue posible cargar los cierres mensuales.';

        }

      });

  }

  // =========================================================
  // ACTUALIZAR CIERRE SELECCIONADO
  // =========================================================

  private actualizarCierreSeleccionado(): void {

    if (!this.idCierreSeleccionado) {

      this.cierreSeleccionado = null;
      return;

    }

    this.cierreSeleccionado =
      this.cierres.find(
        c =>
          Number(c.idCierreCartera) ===
          Number(this.idCierreSeleccionado)
      ) ?? null;

  }

  // =========================================================
  // SELECCIONAR CIERRE
  // =========================================================

  seleccionarCierre(): void {

    this.mensaje = '';
    this.error = '';

    this.cantidadProcesada = null;

    this.resumenEdadMora = [];
    this.resumenControles = null;
    this.resumenAportesGarantias = null;

    this.detalleCalculos = [];

    if (!this.idCierreSeleccionado) {

      this.cierreSeleccionado = null;
      return;

    }

    this.actualizarCierreSeleccionado();

    if (!this.cierreSeleccionado) {
      return;
    }

    this.cargarInformacionCalculos();

  }

  // =========================================================
  // CARGAR INFORMACIÓN DE CÁLCULOS
  // =========================================================

  private cargarInformacionCalculos(): void {

    if (!this.idCierreSeleccionado) {
      return;
    }

    /*
     * Si todavía no hay base de resultados calculada,
     * estas consultas pueden responder error.
     *
     * Se ejecutan porque también permiten consultar
     * cierres históricos y cierres ya procesados.
     */

    this.cargarResumenEdadMora();
    this.cargarResumenControles();
    this.cargarResumenAportesGarantias();

  }

  // =========================================================
  // CARGAR RESUMEN POR EDAD DE MORA
  // =========================================================

  cargarResumenEdadMora(): void {

    if (!this.idCierreSeleccionado) {
      return;
    }

    this.cargandoResumen = true;
    this.resumenEdadMora = [];

    this.calculosApi
      .obtenerResumenEdadMora(
        this.idCierreSeleccionado
      )
      .subscribe({

        next: (data) => {

          this.cargandoResumen = false;

          this.resumenEdadMora =
            data ?? [];

        },

        error: () => {

          this.cargandoResumen = false;

          /*
           * No se muestra error general aquí.
           *
           * Un cierre pendiente puede no tener
           * todavía resultados calculados.
           */
          this.resumenEdadMora = [];

        }

      });

  }

  // =========================================================
  // CARGAR CONTROLES DE CÁLCULOS
  // =========================================================

  cargarResumenControles(): void {

    if (!this.idCierreSeleccionado) {
      return;
    }

    this.cargandoControles = true;
    this.resumenControles = null;

    this.calculosApi
      .obtenerResumenControles(
        this.idCierreSeleccionado
      )
      .subscribe({

        next: (data) => {

          this.cargandoControles = false;

          this.resumenControles =
            data ?? null;

        },

        error: () => {

          this.cargandoControles = false;

          this.resumenControles = null;

        }

      });

  }

  // =========================================================
  // CARGAR RESUMEN DE APORTES Y GARANTÍAS
  // =========================================================

  cargarResumenAportesGarantias(): void {

    if (!this.idCierreSeleccionado) {

      this.resumenAportesGarantias = null;
      return;

    }

    this.cargandoAportesGarantias = true;
    this.resumenAportesGarantias = null;

    this.calculosApi
      .obtenerResumenAportesGarantias(
        this.idCierreSeleccionado
      )
      .subscribe({

        next: (resumen) => {

          this.cargandoAportesGarantias = false;

          this.resumenAportesGarantias =
            resumen ?? null;

        },

        error: () => {

          this.cargandoAportesGarantias = false;

          this.resumenAportesGarantias = null;

        }

      });

  }

  // =========================================================
  // ESTADO DE FOTOGRAFÍA
  // =========================================================

  fotoEnFirme(): boolean {

    return (
      (this.cierreSeleccionado?.estadoFotografia ?? '')
        .trim()
        .toUpperCase() === 'C'
    );

  }

  // =========================================================
  // ESTADO DE CÁLCULOS
  // =========================================================

  estadoCalculos(): string {

    return (
      this.cierreSeleccionado?.estadoCalculos
      ?? 'P'
    )
      .trim()
      .toUpperCase();

  }

  calculosPendientes(): boolean {

    return this.estadoCalculos() === 'P';

  }

  calculosEnProceso(): boolean {

    return this.estadoCalculos() === 'E';

  }

  calculosEnFirme(): boolean {

    return this.estadoCalculos() === 'C';

  }

  // =========================================================
  // TEXTO ESTADO DE CÁLCULOS
  // =========================================================

  textoEstadoCalculos(): string {

    switch (this.estadoCalculos()) {

      case 'E':
        return 'En proceso';

      case 'C':
        return 'En firme';

      default:
        return 'Pendiente';

    }

  }

  // =========================================================
  // PUEDE PROCESAR
  // =========================================================

  puedeProcesar(): boolean {

    return !!this.idCierreSeleccionado
      && !!this.cierreSeleccionado
      && this.fotoEnFirme()
      && !this.calculosEnFirme()
      && !this.procesando
      && !this.cerrandoCalculos;

  }

  // =========================================================
  // PUEDE CERRAR CÁLCULOS
  // =========================================================

  puedeCerrarCalculos(): boolean {

    return !!this.idCierreSeleccionado
      && !!this.cierreSeleccionado
      && this.fotoEnFirme()
      && this.calculosEnProceso()
      && !!this.resumenControles
      && this.resumenControles.procesoConsistente === true
      && !this.procesando
      && !this.cerrandoCalculos;

  }

  // =========================================================
  // EJECUTAR CÁLCULOS
  // =========================================================

  ejecutar(): void {

    if (!this.idCierreSeleccionado) {
      return;
    }

    if (!this.fotoEnFirme()) {

      this.error =
        'La fotografía del cierre debe estar en firme antes de ejecutar los cálculos.';

      return;

    }

    if (this.calculosEnFirme()) {

      this.error =
        'Los cálculos del cierre ya se encuentran en firme.';

      return;

    }

    const confirmar =
      window.confirm(
        this.calculosEnProceso()
          ? 'Los cálculos ya fueron ejecutados. Se volverán a calcular los resultados del cierre. ¿Desea continuar?'
          : 'Se ejecutarán los cálculos del cierre seleccionado. ¿Desea continuar?'
      );

    if (!confirmar) {
      return;
    }

    this.procesando = true;

    this.mensaje = '';
    this.error = '';

    this.cantidadProcesada = null;

    this.calculosApi
      .ejecutar(
        this.idCierreSeleccionado
      )
      .subscribe({

        next: (cantidad) => {

          this.procesando = false;

          this.cantidadProcesada =
            cantidad ?? 0;

          this.mensaje =
            'Cálculos ejecutados correctamente. Revise los resultados antes de dejarlos en firme.';

          /*
           * Al ejecutar, backend cambia:
           *
           * estado_calculos:
           * P -> E
           *
           * Por eso debemos refrescar
           * la cabecera del cierre.
           */

          this.cargarCierres(true);

          this.cargarResumenEdadMora();
          this.cargarResumenControles();
          this.cargarResumenAportesGarantias();

        },

        error: (err) => {

          this.procesando = false;

          this.error =
            err?.error?.message
            ?? err?.error
            ?? 'No fue posible ejecutar los cálculos del cierre.';

        }

      });

  }

  // =========================================================
  // CERRAR CÁLCULOS EN FIRME
  // =========================================================

  cerrarCalculos(): void {

    if (!this.idCierreSeleccionado) {
      return;
    }

    if (!this.fotoEnFirme()) {

      this.error =
        'La fotografía debe estar cerrada en firme.';

      return;

    }

    if (this.calculosEnFirme()) {

      this.error =
        'Los cálculos ya se encuentran cerrados en firme.';

      return;

    }

    if (!this.calculosEnProceso()) {

      this.error =
        'Debe ejecutar los cálculos antes de cerrarlos en firme.';

      return;

    }

    if (
      !this.resumenControles
      || !this.resumenControles.procesoConsistente
    ) {

      this.error =
        'Los controles de los cálculos deben estar correctos antes de cerrar el proceso en firme.';

      return;

    }

    const confirmar =
      window.confirm(
        'Los cálculos quedarán cerrados en firme y ya no podrán ser recalculados. ¿Desea continuar?'
      );

    if (!confirmar) {
      return;
    }

    this.cerrandoCalculos = true;

    this.mensaje = '';
    this.error = '';

    this.calculosApi
      .cerrarCalculos(
        this.idCierreSeleccionado
      )
      .subscribe({

        next: (cierre) => {

          this.cerrandoCalculos = false;

          /*
           * Actualizamos inmediatamente
           * el objeto seleccionado.
           */

          if (cierre) {

            this.cierreSeleccionado =
              cierre;

          }

          this.mensaje =
            'Cálculos cerrados en firme correctamente. El cierre queda habilitado para Anexo 1.';

          /*
           * Refrescamos la lista para mantener
           * sincronizada toda la cabecera.
           */

          this.cargarCierres(true);

        },

        error: (err) => {

          this.cerrandoCalculos = false;

          this.error =
            err?.error?.message
            ?? err?.error
            ?? 'No fue posible cerrar los cálculos en firme.';

        }

      });

  }

  // =========================================================
  // EXPORTAR EXCEL
  // =========================================================

  exportarExcel(): void {

    if (!this.idCierreSeleccionado) {

      this.error =
        'Debe seleccionar un cierre.';

      return;

    }

    if (!this.cierreSeleccionado) {

      this.error =
        'No fue posible determinar el cierre seleccionado.';

      return;

    }

    this.error = '';

    this.exportandoExcel = true;

    this.calculosApi
      .obtenerDetalleCalculos(
        this.idCierreSeleccionado
      )
      .subscribe({

        next: (detalle) => {

          this.detalleCalculos =
            detalle ?? [];

          if (!this.detalleCalculos.length) {

            this.error =
              'El cierre no tiene resultados de cálculos para exportar.';

            this.exportandoExcel = false;

            return;

          }

          this.exporter.exportar(
            this.detalleCalculos,
            this.cierreSeleccionado!.fechaCorte
          );

          this.exportandoExcel = false;

        },

        error: (err) => {

          console.error(err);

          this.error =
            err?.error?.message
            ?? err?.error
            ?? 'No fue posible generar el archivo Excel.';

          this.exportandoExcel = false;

        }

      });

  }

}
