import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { GeneralApi } from '../../../shared/general/general.api';

import { CierreMensualDepositosExporterService }
  from './cierre-mensual-depositos-exporter.service';

import {
  CierreMensualDepositosApi,
  CierreMensualDepositosDetalle,
  CierreMensualDepositosPreview,
  CierreMensualDepositosResumen,
  CierreMensualDepositosResumenForma
} from './cierre-mensual-depositos.api';

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

  private readonly generalApi = inject(GeneralApi);

  agencias: any[] = [];
  cierres: CierreMensualDepositosPreview[] = [];

  model = {
    idAgencia: null as number | null,
    fechaCierre: this.obtenerUltimoDiaMesActual()
  };

  cargando = false;
  procesando = false;
  aplicando = false;

  error = '';
  mensaje = '';
  modoConsulta = false;


  resumen: CierreMensualDepositosResumen = {
    totalCuentas: 0,
    saldoTotal: 0,
    totalDebitos: 0,
    totalCreditos: 0,
    totalFormas: 0,
    hombres: 0,
    mujeres: 0,
    juridicas: 0
  };

  resumenFormas: CierreMensualDepositosResumenForma[] = [];
  detalle: CierreMensualDepositosDetalle[] = [];

  constructor(
    private api: CierreMensualDepositosApi,
    private exporter: CierreMensualDepositosExporterService
  ) {
    this.cargarAgencias();
    this.cargarCierres();
  }

  consultar(): void {

    this.error = '';
    this.mensaje = '';
    this.modoConsulta = false;

    const validacion = this.validar();

    if (validacion) {
      this.error = validacion;
      return;
    }

    this.procesando = true;
    this.limpiarResultados();

    this.api.preview({
      idAgencia: this.model.idAgencia!,
      fechaCierre: this.model.fechaCierre
    }).subscribe({

      next: (response: CierreMensualDepositosPreview) => {

        this.resumen = response.resumen;
        this.resumenFormas = response.resumenFormas;
        this.detalle = response.detalle;

        this.procesando = false;
      },

      error: err => {

        console.error(err);

        this.error =
          err?.error?.message ||
          'No fue posible generar el preview del cierre mensual.';

        this.procesando = false;
      }
    });
  }

  aplicar(): void {

    this.error = '';
    this.mensaje = '';

    const validacion = this.validar();

    if (validacion) {
      this.error = validacion;
      return;
    }

    if (!this.detalle.length) {
      this.error = 'Debe generar el preview antes de aplicar el cierre.';
      return;
    }

    const ok = confirm(
      `¿Desea aplicar el cierre mensual de depósitos para la fecha ${this.model.fechaCierre}?`
    );

    if (!ok) {
      return;
    }

    this.aplicando = true;

    this.api.aplicar({
      idAgencia: this.model.idAgencia!,
      fechaCierre: this.model.fechaCierre
    }).subscribe({

      next: async response => {

        this.mensaje = response.mensaje;

        this.aplicando = false;

        this.limpiarResultados();

        this.model = {
          idAgencia: null,
          fechaCierre: this.obtenerUltimoDiaMesActual()
        };

        this.modoConsulta = false;

        await this.cargarCierres();
      },

      error: err => {

        console.error(err);

        this.error =
          err?.error?.message ||
          'No fue posible aplicar el cierre mensual.';

        this.aplicando = false;
      }
    });
  }

  async cargarCierres(): Promise<void> {

    this.cargando = true;

    try {

      this.cierres =
        await this.api.listar();

    } catch (err: any) {

      console.error(err);

      this.error =
        err?.error?.message ||
        'No se pudieron cargar los cierres mensuales.';

      this.cierres = [];

    } finally {

      this.cargando = false;

    }
  }

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
        await this.api.obtenerPorId(idCierreMensual);

      this.model = {
        idAgencia: cierre.idAgencia ?? null,
        fechaCierre: cierre.fechaCierre ?? this.obtenerUltimoDiaMesActual()
      };

      this.resumen = cierre.resumen;
      this.resumenFormas = cierre.resumenFormas;
      this.detalle = cierre.detalle;
      this.modoConsulta = true;
      this.mensaje = 'Visualizando un cierre mensual ya aplicado.';

    } catch (err: any) {

      console.error(err);

      this.error =
        err?.error?.message ||
        'No se pudo consultar el cierre mensual.';

    } finally {

      this.procesando = false;

    }
  }

  async eliminarCierre(
    cierre: CierreMensualDepositosPreview
  ): Promise<void> {

    this.error = '';
    this.mensaje = '';

    if (!cierre.idCierreMensual) {
      this.error = 'No se encontró el identificador del cierre.';
      return;
    }

    const ok = confirm(
      `¿Desea eliminar el cierre mensual de depósitos de la fecha ${cierre.fechaCierre}?`
    );

    if (!ok) {
      return;
    }

    this.procesando = true;

    try {

      await this.api.eliminar(cierre.idCierreMensual);

      this.mensaje = 'Cierre mensual eliminado correctamente.';

      this.limpiarResultados();
      this.modoConsulta = false;

      await this.cargarCierres();

    } catch (err: any) {

      console.error(err);

      this.error =
        err?.error?.message ||
        'No se pudo eliminar el cierre mensual.';

    } finally {

      this.procesando = false;

    }
  }

  limpiar(): void {

    this.error = '';
    this.mensaje = '';
    this.modoConsulta = false;

    this.model = {
      idAgencia: null,
      fechaCierre: this.obtenerUltimoDiaMesActual()
    };

    this.limpiarResultados();
  }

  async cargarAgencias(): Promise<void> {

    try {

      const ag =
        await this.generalApi
          .listarAgencias()
          .toPromise();

      this.agencias = ag ?? [];

    } catch (e) {

      console.error('Error cargando agencias:', e);

      this.agencias = [];
      this.error = 'No se pudieron cargar las agencias.';

    }
  }

  exportarExcel(): void {

    this.error = '';

    if (!this.detalle.length) {
      this.error = 'Debe generar el preview antes de exportar.';
      return;
    }

    this.exporter.exportar(
      {
        resumen: this.resumen,
        resumenFormas: this.resumenFormas,
        detalle: this.detalle
      },
      this.model.fechaCierre
    );
  }

  private validar(): string | null {

    if (!this.model.idAgencia) {
      return 'Debe seleccionar la agencia.';
    }

    if (!this.model.fechaCierre) {
      return 'Debe ingresar la fecha de cierre.';
    }

    const fecha = new Date(`${this.model.fechaCierre}T00:00:00`);

    const ultimoDia = new Date(
      fecha.getFullYear(),
      fecha.getMonth() + 1,
      0
    );

    if (fecha.getDate() !== ultimoDia.getDate()) {
      return 'La fecha de cierre debe ser el último día del mes.';
    }

    return null;
  }

  public limpiarResultados(): void {

    this.resumen = {
      totalCuentas: 0,
      saldoTotal: 0,
      totalDebitos: 0,
      totalCreditos: 0,
      totalFormas: 0,
      hombres: 0,
      mujeres: 0,
      juridicas: 0
    };

    this.resumenFormas = [];
    this.detalle = [];
  }

  private obtenerUltimoDiaMesActual(): string {

    const hoy = new Date();

    const ultimoDia = new Date(
      hoy.getFullYear(),
      hoy.getMonth() + 1,
      0
    );

    return ultimoDia.toISOString().substring(0, 10);
  }

}
