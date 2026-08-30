import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  CierreMensualDepositosApi,
  CierreMensualDepositosDetalle,
  CierreMensualDepositosPreview,
  CierreMensualDepositosResumen,
  CierreMensualDepositosResumenAgencia,
  CierreMensualDepositosResumenForma
} from './cierre-mensual-depositos.api';

import {
  CierreMensualDepositosExporterService
} from './cierre-mensual-depositos-exporter.service';


@Component({
  selector: 'app-cierre-mensual-depositos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './cierre-mensual-depositos.component.html',
  styleUrls: ['./cierre-mensual-depositos.component.scss']
})
export class CierreMensualDepositosComponent {

  // =========================================================
  // DATOS GENERALES
  // =========================================================

  cierres: CierreMensualDepositosPreview[] = [];

  model = {
    fechaCierre: this.obtenerUltimoDiaMesActual()
  };


  // =========================================================
  // ESTADOS DE PANTALLA
  // =========================================================

  cargando = false;
  procesando = false;
  generando = false;
  regenerando = false;
  cerrando = false;

  error = '';
  mensaje = '';

  modoConsulta = false;

  idCierreSeleccionado: number | null = null;
  estadoCierreSeleccionado: string | null = null;


  // =========================================================
  // RESUMEN GENERAL
  // =========================================================

  resumen: CierreMensualDepositosResumen = {
    totalCuentas: 0,
    saldoTotal: 0,
    totalDebitos: 0,
    totalCreditos: 0,
    hombres: 0,
    mujeres: 0,
    juridicas: 0
  };


  // =========================================================
  // RESUMEN POR AGENCIA
  // =========================================================

  resumenAgencias: CierreMensualDepositosResumenAgencia[] = [];


  // =========================================================
  // RESUMEN POR AGENCIA + FORMA
  // =========================================================

  resumenFormas: CierreMensualDepositosResumenForma[] = [];


  // =========================================================
  // DETALLE
  // =========================================================

  detalle: CierreMensualDepositosDetalle[] = [];


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private api: CierreMensualDepositosApi,
    private exporter: CierreMensualDepositosExporterService
  ) {

    this.cargarCierres();
  }


  // =========================================================
  // PREVIEW
  //
  // No persiste información.
  // Calcula todas las agencias.
  // =========================================================

  consultar(): void {

    this.error = '';
    this.mensaje = '';

    this.modoConsulta = false;
    this.idCierreSeleccionado = null;
    this.estadoCierreSeleccionado = null;

    const validacion = this.validar();

    if (validacion) {
      this.error = validacion;
      return;
    }

    this.procesando = true;

    this.limpiarResultados();

    this.api.preview({
      fechaCierre: this.model.fechaCierre
    }).subscribe({

      next: (response: CierreMensualDepositosPreview) => {

        this.cargarRespuesta(
          response
        );

        this.mensaje =
          'Preview generado correctamente. '
          + 'Revise la información antes de generar la fotografía.';

        this.procesando = false;
      },

      error: err => {

        console.error(err);

        this.error =
          this.obtenerMensajeError(
            err,
            'No fue posible generar el preview del cierre mensual.'
          );

        this.procesando = false;
      }
    });
  }


  // =========================================================
  // GENERAR FOTOGRAFÍA
  //
  // Estado resultante:
  //
  // P = En proceso
  // =========================================================

  generar(): void {

    this.error = '';
    this.mensaje = '';

    const validacion = this.validar();

    if (validacion) {
      this.error = validacion;
      return;
    }

    if (!this.detalle.length) {

      this.error =
        'Debe generar y revisar el preview antes de generar la fotografía.';

      return;
    }

    const ok = confirm(
      '¿Desea generar la fotografía mensual de depósitos '
      + `para la fecha ${this.model.fechaCierre}?`
    );

    if (!ok) {
      return;
    }

    this.generando = true;

    this.api.generar({
      fechaCierre: this.model.fechaCierre
    }).subscribe({

      next: async response => {

        this.mensaje = response.mensaje;

        this.generando = false;

        await this.cargarCierres();

        await this.verCierre(
          response.idCierreMensual
        );
      },

      error: err => {

        console.error(err);

        this.error =
          this.obtenerMensajeError(
            err,
            'No fue posible generar la fotografía mensual.'
          );

        this.generando = false;
      }
    });
  }


  // =========================================================
  // REGENERAR FOTOGRAFÍA
  //
  // Solo estado P.
  // Conserva el mismo id.
  // =========================================================

  regenerar(): void {

    this.error = '';
    this.mensaje = '';

    if (!this.idCierreSeleccionado) {

      this.error =
        'Debe seleccionar un cierre mensual.';

      return;
    }

    if (!this.esCierreEnProceso()) {

      this.error =
        'Solo se puede regenerar una fotografía en estado En proceso.';

      return;
    }

    const ok = confirm(
      '¿Desea regenerar la fotografía mensual de depósitos? '
      + 'La información fotografiada actualmente será reemplazada.'
    );

    if (!ok) {
      return;
    }

    this.regenerando = true;

    this.api.regenerar(
      this.idCierreSeleccionado
    ).subscribe({

      next: async response => {

        this.mensaje = response.mensaje;

        this.regenerando = false;

        await this.cargarCierres();

        await this.verCierre(
          response.idCierreMensual
        );
      },

      error: err => {

        console.error(err);

        this.error =
          this.obtenerMensajeError(
            err,
            'No fue posible regenerar la fotografía mensual.'
          );

        this.regenerando = false;
      }
    });
  }


  // =========================================================
  // CERRAR FOTOGRAFÍA EN FIRME
  //
  // P -> C
  // =========================================================

  cerrar(): void {

    this.error = '';
    this.mensaje = '';

    if (!this.idCierreSeleccionado) {

      this.error =
        'Debe seleccionar un cierre mensual.';

      return;
    }

    if (!this.esCierreEnProceso()) {

      this.error =
        'Solo se puede cerrar una fotografía en estado En proceso.';

      return;
    }

    const ok = confirm(
      '¿Desea cerrar en firme la fotografía mensual de depósitos? '
      + 'Después de cerrar no podrá regenerarse ni eliminarse.'
    );

    if (!ok) {
      return;
    }

    this.cerrando = true;

    this.api.cerrar(
      this.idCierreSeleccionado
    ).subscribe({

      next: async response => {

        this.mensaje = response.mensaje;

        this.cerrando = false;

        await this.cargarCierres();

        await this.verCierre(
          response.idCierreMensual
        );
      },

      error: err => {

        console.error(err);

        this.error =
          this.obtenerMensajeError(
            err,
            'No fue posible cerrar en firme la fotografía mensual.'
          );

        this.cerrando = false;
      }
    });
  }


  // =========================================================
  // CARGAR CIERRES
  // =========================================================

  async cargarCierres(): Promise<void> {

    this.cargando = true;

    try {

      this.cierres =
        await this.api.listar();

    } catch (err: any) {

      console.error(err);

      this.error =
        this.obtenerMensajeError(
          err,
          'No se pudieron cargar los cierres mensuales.'
        );

      this.cierres = [];

    } finally {

      this.cargando = false;

    }
  }


  // =========================================================
  // VER CIERRE
  // =========================================================

  async verCierre(
    idCierreMensual: number | undefined
  ): Promise<void> {

    if (!idCierreMensual) {
      return;
    }

    this.error = '';
    this.mensaje = '';
    this.procesando = true;

    try {

      const cierre =
        await this.api.obtenerPorId(
          idCierreMensual
        );

      this.model = {
        fechaCierre:
          cierre.fechaCierre
          ?? this.obtenerUltimoDiaMesActual()
      };

      this.idCierreSeleccionado =
        cierre.idCierreMensual
        ?? idCierreMensual;

      this.estadoCierreSeleccionado =
        cierre.estado
        ?? null;

      this.cargarRespuesta(
        cierre
      );

      this.modoConsulta = true;

      if (this.esCierreCerrado()) {

        this.mensaje =
          'Visualizando una fotografía mensual cerrada en firme.';

      } else {

        this.mensaje =
          'Visualizando una fotografía mensual en proceso.';
      }

    } catch (err: any) {

      console.error(err);

      this.error =
        this.obtenerMensajeError(
          err,
          'No se pudo consultar el cierre mensual.'
        );

    } finally {

      this.procesando = false;
    }
  }


  // =========================================================
  // ELIMINAR PRECierre
  //
  // Solo estado P.
  // =========================================================

  async eliminarCierre(
    cierre: CierreMensualDepositosPreview
  ): Promise<void> {

    this.error = '';
    this.mensaje = '';

    if (!cierre.idCierreMensual) {

      this.error =
        'No se encontró el identificador del cierre.';

      return;
    }

    if (
      cierre.estado
      && cierre.estado.toUpperCase() !== 'P'
    ) {

      this.error =
        'Solo se pueden eliminar cierres en estado En proceso.';

      return;
    }

    const ok = confirm(
      '¿Desea eliminar la fotografía mensual de depósitos '
      + `de la fecha ${cierre.fechaCierre}?`
    );

    if (!ok) {
      return;
    }

    this.procesando = true;

    try {

      await this.api.eliminar(
        cierre.idCierreMensual
      );

      this.mensaje =
        'Fotografía mensual eliminada correctamente.';

      if (
        this.idCierreSeleccionado
        === cierre.idCierreMensual
      ) {

        this.limpiar();
      }

      await this.cargarCierres();

    } catch (err: any) {

      console.error(err);

      this.error =
        this.obtenerMensajeError(
          err,
          'No se pudo eliminar la fotografía mensual.'
        );

    } finally {

      this.procesando = false;
    }
  }


  // =========================================================
  // NUEVO / LIMPIAR
  // =========================================================

  limpiar(): void {

    this.error = '';
    this.mensaje = '';

    this.modoConsulta = false;

    this.idCierreSeleccionado = null;
    this.estadoCierreSeleccionado = null;

    this.model = {
      fechaCierre:
        this.obtenerUltimoDiaMesActual()
    };

    this.limpiarResultados();
  }


  // =========================================================
  // EXPORTAR EXCEL
  // =========================================================

  exportarExcel(): void {

    this.error = '';

    if (!this.detalle.length) {

      this.error =
        'Debe generar o consultar una fotografía antes de exportar.';

      return;
    }

    this.exporter.exportar(
      {
        resumen: this.resumen,
        resumenAgencias: this.resumenAgencias,
        resumenFormas: this.resumenFormas,
        detalle: this.detalle
      },
      this.model.fechaCierre
    );
  }


  // =========================================================
  // ESTADO
  // =========================================================

  esCierreEnProceso(): boolean {

    return (
      this.estadoCierreSeleccionado
        ?.toUpperCase()
      === 'P'
    );
  }


  esCierreCerrado(): boolean {

    return (
      this.estadoCierreSeleccionado
        ?.toUpperCase()
      === 'C'
    );
  }


  textoEstado(
    estado: string | undefined | null
  ): string {

    switch (
      estado
        ?.toUpperCase()
    ) {

      case 'P':
        return 'En proceso';

      case 'C':
        return 'Cerrado';

      default:
        return '';
    }
  }


  // =========================================================
  // CARGAR RESPUESTA
  // =========================================================

  private cargarRespuesta(
    response: CierreMensualDepositosPreview
  ): void {

    this.resumen =
      response.resumen
      ?? this.crearResumenVacio();

    this.resumenAgencias =
      response.resumenAgencias
      ?? [];

    this.resumenFormas =
      response.resumenFormas
      ?? [];

    this.detalle =
      response.detalle
      ?? [];
  }


  // =========================================================
  // VALIDAR
  // =========================================================

  private validar(): string | null {

    if (!this.model.fechaCierre) {

      return 'Debe ingresar la fecha de cierre.';
    }

    const fecha =
      new Date(
        `${this.model.fechaCierre}T00:00:00`
      );

    if (
      Number.isNaN(
        fecha.getTime()
      )
    ) {

      return 'La fecha de cierre no es válida.';
    }

    const ultimoDia =
      new Date(
        fecha.getFullYear(),
        fecha.getMonth() + 1,
        0
      );

    if (
      fecha.getDate()
      !== ultimoDia.getDate()
    ) {

      return 'La fecha de cierre debe ser el último día del mes.';
    }

    return null;
  }


  // =========================================================
  // LIMPIAR RESULTADOS
  // =========================================================

  public limpiarResultados(): void {

    this.resumen =
      this.crearResumenVacio();

    this.resumenAgencias = [];
    this.resumenFormas = [];
    this.detalle = [];
  }


  // =========================================================
  // RESUMEN VACÍO
  // =========================================================

  private crearResumenVacio():
    CierreMensualDepositosResumen {

    return {
      totalCuentas: 0,
      saldoTotal: 0,
      totalDebitos: 0,
      totalCreditos: 0,
      hombres: 0,
      mujeres: 0,
      juridicas: 0
    };
  }


  // =========================================================
  // ÚLTIMO DÍA DEL MES ACTUAL
  // =========================================================

  private obtenerUltimoDiaMesActual(): string {

    const hoy =
      new Date();

    const ultimoDia =
      new Date(
        hoy.getFullYear(),
        hoy.getMonth() + 1,
        0
      );

    const year =
      ultimoDia.getFullYear();

    const month =
      String(
        ultimoDia.getMonth() + 1
      ).padStart(
        2,
        '0'
      );

    const day =
      String(
        ultimoDia.getDate()
      ).padStart(
        2,
        '0'
      );

    return `${year}-${month}-${day}`;
  }


  // =========================================================
  // MENSAJE DE ERROR
  // =========================================================

  private obtenerMensajeError(
    err: any,
    mensajeDefault: string
  ): string {

    return (
      err?.error?.message
      || err?.message
      || mensajeDefault
    );
  }

}
