import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { CentralRiesgoResultadoApi } from './central-riesgo-resultado.api';
import {
  CentralRiesgoResultadoDato,
  CentralRiesgoResultadoImportacion
} from './central-riesgo-resultado.models';

import { HeaderActionsComponent } from '../../../../../shared/header-actions/header-actions.component';

@Component({
  selector: 'app-central-riesgo-resultado-import',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './central-riesgo-resultado-import.component.html',
  styleUrl: './central-riesgo-resultado-import.component.scss'
})

export class CentralRiesgoResultadoImportComponent {

  private readonly api = inject(CentralRiesgoResultadoApi);

  // =========================================================
  // DATOS DE LA IMPORTACIÓN
  // =========================================================

  idCentralRiesgo: number | null = null;

  fechaCorte = '';

  archivoSeleccionado: File | null = null;

  nombreArchivoSeleccionado = '';

  // =========================================================
  // RESULTADOS
  // =========================================================

  resultadoImportacion: CentralRiesgoResultadoImportacion | null = null;

  datosImportados: CentralRiesgoResultadoDato[] = [];

  // =========================================================
  // ESTADOS
  // =========================================================

  importando = false;

  cargandoDatos = false;

  mensajeExito = '';

  mensajeError = '';

  // =========================================================
  // SELECCIONAR ARCHIVO
  // =========================================================

  seleccionarArchivo(
    event: Event
  ): void {

    this.limpiarMensajes();

    const input =
      event.target as HTMLInputElement;

    const archivo =
      input.files?.item(0) ?? null;

    if (!archivo) {
      this.archivoSeleccionado = null;
      this.nombreArchivoSeleccionado = '';
      return;
    }

    if (!archivo.name.toLowerCase().endsWith('.csv')) {
      this.archivoSeleccionado = null;
      this.nombreArchivoSeleccionado = '';

      input.value = '';

      this.mensajeError =
        'Debe seleccionar un archivo con extensión CSV.';

      return;
    }

    this.archivoSeleccionado = archivo;
    this.nombreArchivoSeleccionado = archivo.name;
  }

  // =========================================================
  // IMPORTAR
  // =========================================================

  importar(): void {

    this.limpiarMensajes();

    if (!this.validarAntesDeImportar()) {
      return;
    }

    this.ejecutarImportacion(false);
  }

  private ejecutarImportacion(
    reemplazar: boolean
  ): void {

    this.importando = true;
    this.resultadoImportacion = null;
    this.datosImportados = [];

    this.api.importar(
      this.idCentralRiesgo as number,
      this.fechaCorte,
      this.archivoSeleccionado as File,
      reemplazar
    )
      .pipe(
        finalize(
          () => {
            this.importando = false;
          }
        )
      )
      .subscribe({
        next: resultado => {

          if (resultado.requiereConfirmacion) {

            const confirmar =
              window.confirm(
                resultado.mensajeConfirmacion
                ?? 'Ya existe una importación para la fecha y central seleccionadas. ¿Desea reemplazarla?'
              );

            if (confirmar) {
              this.ejecutarImportacion(true);
            }

            return;
          }

          this.resultadoImportacion = resultado;

          this.mensajeExito =
            `El archivo fue importado y se guardaron `
            + `${resultado.cantidadRegistrosImportados} registros correctamente.`;

          this.cargarDatosImportados(
            resultado.idCentralArchivo
          );
        },
        error: error => {

          this.mensajeError =
            this.obtenerMensajeError(error);
        }
      });
  }

  // =========================================================
  // CARGAR DATOS IMPORTADOS
  // =========================================================

  private cargarDatosImportados(
    idCentralArchivo: number
  ): void {

    this.cargandoDatos = true;

    this.api.listarDatos(
      idCentralArchivo
    )
      .pipe(
        finalize(
          () => {
            this.cargandoDatos = false;
          }
        )
      )
      .subscribe({
        next: datos => {
          this.datosImportados = datos;
        },
        error: error => {

          this.datosImportados = [];

          this.mensajeError =
            this.obtenerMensajeError(error);
        }
      });
  }

  // =========================================================
  // LIMPIAR
  // =========================================================

  limpiar(
    inputArchivo?: HTMLInputElement
  ): void {

    this.idCentralRiesgo = null;
    this.fechaCorte = '';

    this.archivoSeleccionado = null;
    this.nombreArchivoSeleccionado = '';

    this.resultadoImportacion = null;
    this.datosImportados = [];

    this.importando = false;
    this.cargandoDatos = false;

    this.limpiarMensajes();

    if (inputArchivo) {
      inputArchivo.value = '';
    }
  }

  // =========================================================
  // VALIDACIONES
  // =========================================================

  private validarAntesDeImportar(): boolean {

    if (
      this.idCentralRiesgo === null
      || this.idCentralRiesgo <= 0
    ) {
      this.mensajeError =
        'Debe seleccionar una Central de Riesgos.';

      return false;
    }

    if (!this.fechaCorte) {
      this.mensajeError =
        'Debe seleccionar la fecha de corte.';

      return false;
    }

    if (!this.archivoSeleccionado) {
      this.mensajeError =
        'Debe seleccionar el archivo CSV.';

      return false;
    }

    return true;
  }

  // =========================================================
  // MENSAJES
  // =========================================================

  private limpiarMensajes(): void {
    this.mensajeExito = '';
    this.mensajeError = '';
  }

  private obtenerMensajeError(
    error: any
  ): string {

    const mensajeBackend =
      error?.error?.message
      ?? error?.error?.mensaje
      ?? error?.error?.error;

    if (
      typeof mensajeBackend === 'string'
      && mensajeBackend.trim()
    ) {
      return mensajeBackend;
    }

    if (
      typeof error?.error === 'string'
      && error.error.trim()
    ) {
      return error.error;
    }

    return 'No fue posible importar el archivo de Central de Riesgos.';
  }

  // =========================================================
  // UTILIDADES DE VISTA
  // =========================================================

  get puedeImportar(): boolean {

    return (
      !this.importando
      && this.idCentralRiesgo !== null
      && this.idCentralRiesgo > 0
      && !!this.fechaCorte
      && this.archivoSeleccionado !== null
    );
  }

  get tieneResultado(): boolean {
    return this.resultadoImportacion !== null;
  }

  get tieneDatos(): boolean {
    return this.datosImportados.length > 0;
  }
}
