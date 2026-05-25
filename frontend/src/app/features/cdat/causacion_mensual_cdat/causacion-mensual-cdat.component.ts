import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { GeneralApi } from '../../../shared/general/general.api';

import {
  TiposComprobantesApi,
  TipoComprobante
} from '../../contabilidad/tipos-comprobantes/tipos-comprobantes.api';

import {
  CausacionMensualCdatApi,
  CdatCausacionMensualEntradaDTO,
  CdatCausacionMensualPreviewDTO
} from './causacion-mensual-cdat.api';

import {
  CausacionMensualCdatExporterService
} from './causacion-mensual-cdat-exporter.service';

@Component({
  selector: 'app-causacion-mensual-cdat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './causacion-mensual-cdat.component.html',
  styleUrl: './causacion-mensual-cdat.component.scss'
})
export class CausacionMensualCdatComponent {

  private readonly generalApi = inject(GeneralApi);

  agencias: any[] = [];
  tiposComprobantes: TipoComprobante[] = [];

  preview: CdatCausacionMensualPreviewDTO | null = null;

  procesando = false;
  mensaje = '';
  error = '';

  model: CdatCausacionMensualEntradaDTO = {
    idAgencia: 2,
    fechaCorte: '',
    fechaContable: new Date().toISOString().substring(0, 10),
    tipoComprobante: '',
    numeroComprobante: '',
    confirmado: false
  };

  constructor(
    private api: CausacionMensualCdatApi,
    private exporter: CausacionMensualCdatExporterService,
    private tiposComprobantesApi: TiposComprobantesApi
  ) {
    this.cargarAgencias();
    this.cargarTiposComprobantes();
  }

  async generarPreview(): Promise<void> {
    this.mensaje = '';
    this.error = '';
    this.preview = null;

    const validacion = this.validar(false);
    if (validacion) {
      this.error = validacion;
      return;
    }

    this.procesando = true;

    try {
      this.preview = await this.api.preview(this.model);

      if (this.preview.existeCausacion) {
        this.error = 'Ya existe causación mensual CDAT para esta agencia y fecha de corte.';
      }

    } catch (err: any) {
      this.error =
        err?.error?.message ||
        'No se pudo generar la causación mensual.';
    } finally {
      this.procesando = false;
    }
  }

  async aplicar(): Promise<void> {
    this.mensaje = '';
    this.error = '';

    const validacion = this.validar(true);
    if (validacion) {
      this.error = validacion;
      return;
    }

    if (!this.preview) {
      this.error = 'Debe generar el preview antes de aplicar la causación.';
      return;
    }

    if (this.preview.existeCausacion) {
      this.error = 'La causación mensual ya existe. No se puede aplicar nuevamente.';
      return;
    }

    const ok = confirm(
      `¿Desea aplicar la causación mensual CDAT para la fecha ${this.model.fechaCorte}?`
    );

    if (!ok) {
      return;
    }

    this.procesando = true;

    try {
      const resp = await this.api.aplicar(this.model);

      this.exporter.exportar(resp);

      this.mensaje = 'Causación mensual CDAT aplicada correctamente.';

      this.preview = null;

      this.model = {
        idAgencia: 2,
        fechaCorte: '',
        fechaContable: new Date().toISOString().substring(0, 10),
        tipoComprobante: '',
        numeroComprobante: '',
        confirmado: false
      };

      this.cargarTiposComprobantes();

    } catch (err: any) {
      this.error =
        err?.error?.message ||
        'No se pudo aplicar la causación mensual.';
    } finally {
      this.procesando = false;
    }
  }

  limpiar(): void {
    this.mensaje = '';
    this.error = '';
    this.preview = null;

    this.model = {
      idAgencia: 2,
      fechaCorte: '',
      fechaContable: new Date().toISOString().substring(0, 10),
      tipoComprobante: '',
      numeroComprobante: '',
      confirmado: false
    };

    this.cargarTiposComprobantes();
  }

  cargarTiposComprobantes(): void {
    if (!this.model.idAgencia) {
      this.tiposComprobantes = [];
      return;
    }

    this.tiposComprobantesApi
      .listarPorAgencia(this.model.idAgencia)
      .subscribe({
        next: data => this.tiposComprobantes = data ?? [],
        error: () =>
          this.error = 'No se pudieron cargar los tipos de comprobante.'
      });
  }

  onChangeAgencia(): void {
    this.preview = null;
    this.model.tipoComprobante = '';
    this.model.numeroComprobante = '';
    this.cargarTiposComprobantes();
  }

  async cargarAgencias(): Promise<void> {
    this.error = '';

    try {
      const ag = await this.generalApi
        .listarAgencias()
        .toPromise();

      this.agencias = ag ?? [];

    } catch (e) {
      console.error('❌ Error cargando agencias:', e);
      this.error = 'No se pudieron cargar las agencias.';
      this.agencias = [];
    }
  }

  async cargarProximoComprobante(): Promise<void> {
    this.error = '';

    if (!this.model.tipoComprobante) {
      this.model.numeroComprobante = '';
      return;
    }

    if (!this.model.idAgencia) {
      this.error = 'Debe seleccionar la agencia.';
      return;
    }

    try {
      const res = await this.api.obtenerProximoComprobante(
        this.model.idAgencia,
        this.model.tipoComprobante
      );

      this.model.numeroComprobante =
        res?.numeroComprobante ?? '';

    } catch (e: any) {
      console.error('❌ Error obteniendo comprobante:', e);
      this.error = 'No se pudo obtener el consecutivo del comprobante.';
    }
  }

  private validar(aplicar: boolean): string | null {
    if (!this.model.idAgencia) return 'Debe seleccionar la agencia.';
    if (!this.model.fechaCorte) return 'Debe ingresar la fecha de corte.';
    if (!this.model.fechaContable) return 'Debe ingresar la fecha contable.';

    if (aplicar && !this.model.tipoComprobante) {
      return 'Debe seleccionar el tipo de comprobante.';
    }

    if (aplicar && !this.model.numeroComprobante) {
      return 'Debe ingresar el número de comprobante.';
    }

    return null;
  }
}
