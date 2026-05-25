import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { GeneralApi } from '../../../shared/general/general.api';

import {
  CierreMensualCdatApi,
  CdatCierreMensualEntradaDTO,
  CdatCierreMensualPreviewDTO
} from './cierre-mensual-cdat.api';

import { CierreMensualCdatExporterService } from './cierre-mensual-cdat-exporter.service';

@Component({
  selector: 'app-cierre-mensual-cdat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cierre-mensual-cdat.component.html',
  styleUrls: ['./cierre-mensual-cdat.component.scss']
})
export class CierreMensualCdatComponent {

  private readonly generalApi = inject(GeneralApi);

  agencias: any[] = [];
  cierres: CdatCierreMensualPreviewDTO[] = [];

  preview: CdatCierreMensualPreviewDTO | null = null;

  error = '';
  mensaje = '';
  procesando = false;
  cargando = false;

  model: CdatCierreMensualEntradaDTO = {
    idAgencia: 2,
    fechaCorte: this.obtenerUltimoDiaMesActual(),
    confirmado: false
  };

  constructor(
    private api: CierreMensualCdatApi,
    private exporter: CierreMensualCdatExporterService
  ) {
    this.cargarAgencias();
    this.cargarCierres();
  }

  async generarPreview(): Promise<void> {
    this.error = '';
    this.mensaje = '';
    this.preview = null;

    const validacion = this.validar();
    if (validacion) {
      this.error = validacion;
      return;
    }

    this.procesando = true;

    try {
      this.preview = await this.api.preview(this.model);

      if (this.preview.existeCierre) {
        this.error = 'Ya existe un cierre mensual CDAT para esta agencia y fecha de corte.';
      }

    } catch (err: any) {
      this.error = err?.error?.message || 'No se pudo generar el preview del cierre mensual.';
    } finally {
      this.procesando = false;
    }
  }

  async aplicar(): Promise<void> {
    this.error = '';
    this.mensaje = '';

    const validacion = this.validar();
    if (validacion) {
      this.error = validacion;
      return;
    }

    if (!this.preview) {
      this.error = 'Debe generar el preview antes de aplicar el cierre.';
      return;
    }

    if (this.preview.existeCierre) {
      this.error = 'El cierre mensual ya existe. No se puede aplicar nuevamente.';
      return;
    }

    if (!this.preview.items?.length) {
      this.error = 'No hay CDAT activos para cerrar.';
      return;
    }

    const ok = confirm(
      `¿Desea aplicar el cierre mensual CDAT para la fecha ${this.model.fechaCorte}?`
    );

    if (!ok) {
      return;
    }

    this.procesando = true;

    try {

      const resp = await this.api.aplicar(this.model);

      // Genera soporte Excel automáticamente
      this.exporter.exportar(resp);

      this.mensaje = 'Cierre mensual CDAT aplicado correctamente.';

      // Limpia preview
      this.preview = null;

      // Reinicia formulario
      this.model = {
        idAgencia: 2,
        fechaCorte: this.obtenerUltimoDiaMesActual(),
        confirmado: false
      };

      // Recarga listado
      await this.cargarCierres();

    } catch (err: any) {

      this.error =
        err?.error?.message ||
        'No se pudo aplicar el cierre mensual CDAT.';

    } finally {

      this.procesando = false;

    }
  }

  limpiar(): void {
    this.error = '';
    this.mensaje = '';
    this.preview = null;

    this.model = {
      idAgencia: 2,
      fechaCorte: this.obtenerUltimoDiaMesActual(),
      confirmado: false
    };
  }

  async cargarAgencias(): Promise<void> {
    try {
      const ag = await this.generalApi
        .listarAgencias()
        .toPromise();

      this.agencias = ag ?? [];

    } catch (e) {
      console.error('Error cargando agencias:', e);
      this.agencias = [];
      this.error = 'No se pudieron cargar las agencias.';
    }
  }

  async cargarCierres(): Promise<void> {
    this.cargando = true;

    try {
      this.cierres = await this.api.listar();
    } catch (err: any) {
      this.error = err?.error?.message || 'No se pudieron cargar los cierres mensuales.';
      this.cierres = [];
    } finally {
      this.cargando = false;
    }
  }

  async verCierre(idCierre: number | null): Promise<void> {
    if (!idCierre) {
      return;
    }

    this.error = '';
    this.mensaje = '';
    this.procesando = true;

    try {
      this.preview = await this.api.obtenerPorId(idCierre);

      this.model = {
        idAgencia: this.preview.idAgencia,
        fechaCorte: this.preview.fechaCorte,
        confirmado: true
      };

    } catch (err: any) {
      this.error = err?.error?.message || 'No se pudo consultar el cierre mensual.';
    } finally {
      this.procesando = false;
    }
  }

  private validar(): string | null {
    if (!this.model.idAgencia) {
      return 'Debe seleccionar la agencia.';
    }

    if (!this.model.fechaCorte) {
      return 'Debe ingresar la fecha de corte.';
    }

    const fecha = new Date(`${this.model.fechaCorte}T00:00:00`);
    const ultimoDia = new Date(fecha.getFullYear(), fecha.getMonth() + 1, 0);

    if (fecha.getDate() !== ultimoDia.getDate()) {
      return 'La fecha de corte debe ser el último día del mes.';
    }

    return null;
  }

  private obtenerUltimoDiaMesActual(): string {
    const hoy = new Date();
    const ultimoDia = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);

    return ultimoDia.toISOString().substring(0, 10);
  }

  async eliminarCierre(cierre: CdatCierreMensualPreviewDTO): Promise<void> {
    this.error = '';
    this.mensaje = '';

    if (!cierre.idCierreMensualCdat) {
      this.error = 'No se encontró el identificador del cierre.';
      return;
    }

    const ok = confirm(
      `¿Desea eliminar el cierre mensual CDAT de la fecha ${cierre.fechaCorte}?`
    );

    if (!ok) {
      return;
    }

    this.procesando = true;

    try {
      await this.api.eliminar(cierre.idCierreMensualCdat);

      this.mensaje = 'Cierre mensual CDAT eliminado correctamente.';

      if (this.preview?.idCierreMensualCdat === cierre.idCierreMensualCdat) {
        this.preview = null;
      }

      await this.cargarCierres();

    } catch (err: any) {
      this.error = err?.error?.message || 'No se pudo eliminar el cierre mensual CDAT.';
    } finally {
      this.procesando = false;
    }
  }

}
