import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { GeneralApi } from '../../../shared/general/general.api';

import {
  CdatLiquidacionDiariaApi,
  CdatLiquidacionDiariaEntradaDTO,
  CdatLiquidacionDiariaPreviewDTO
} from './cdat-liquidacion-diaria.api';

import {
  TiposComprobantesApi,
  TipoComprobante
} from '../../contabilidad/tipos-comprobantes/tipos-comprobantes.api';

import { CdatLiquidacionDiariaExporterService } from './cdat-liquidacion-diaria-exporter.service';

@Component({
  selector: 'app-cdat-liquidacion-diaria',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cdat-liquidacion-diaria.component.html',
  styleUrls: ['./cdat-liquidacion-diaria.component.scss']
})
export class CdatLiquidacionDiariaComponent {

  error = '';
  procesando = false;

  tiposComprobantes: TipoComprobante[] = [];
  preview: CdatLiquidacionDiariaPreviewDTO | null = null;
  agencias: any[] = [];

  model: CdatLiquidacionDiariaEntradaDTO = {
    idAgencia: 2,
    fechaLiquidacion: new Date().toISOString().substring(0, 10),
    fechaContable: new Date().toISOString().substring(0, 10),
    tipoComprobante: '',
    numeroComprobante: '',
    confirmado: false
  };

  private readonly generalApi = inject(GeneralApi);

  constructor(
    private api: CdatLiquidacionDiariaApi,
    private tiposComprobantesApi: TiposComprobantesApi,
    private exporter: CdatLiquidacionDiariaExporterService
  ) {
    this.cargarAgencias();
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
        error: () => this.error = 'No se pudieron cargar los tipos de comprobante.'
      });
  }

  onChangeAgencia(): void {
    this.preview = null;
    this.model.tipoComprobante = '';
    this.model.numeroComprobante = '';
    this.cargarTiposComprobantes();
  }

  async generarPreview(): Promise<void> {
    this.error = '';
    this.preview = null;

    const validacion = this.validar();
    if (validacion) {
      this.error = validacion;
      return;
    }

    this.procesando = true;

    try {
      this.preview = await this.api.preview(this.model);
    } catch (err: any) {
      this.error = err?.error?.message || 'No se pudo generar el preview.';
    } finally {
      this.procesando = false;
    }
  }

  async aplicar(): Promise<void> {
    this.error = '';

    const validacion = this.validar();
    if (validacion) {
      this.error = validacion;
      return;
    }

    if (!this.preview || !this.preview.cuadrado) {
      this.error = 'Debe generar un preview contable válido antes de aplicar.';
      return;
    }

    if (!confirm('¿Desea aplicar la liquidación diaria de CDAT?')) {
      return;
    }

    this.procesando = true;

    try {
      await this.api.aplicar(this.model);

      // Exporta automáticamente el listado aplicado
      this.exporter.exportar(this.preview);

      alert('Liquidación diaria CDAT aplicada correctamente.');

      this.limpiar();

    } catch (err: any) {
      this.error = err?.error?.message || 'No se pudo aplicar la liquidación diaria.';
    } finally {
      this.procesando = false;
    }
  }

  limpiar(): void {
    this.error = '';
    this.preview = null;

    this.model = {
      idAgencia: 2,
      fechaLiquidacion: new Date().toISOString().substring(0, 10),
      fechaContable: new Date().toISOString().substring(0, 10),
      tipoComprobante: '',
      numeroComprobante: '',
      confirmado: false
    };

    this.cargarTiposComprobantes();
  }

  private validar(): string | null {
    if (!this.model.idAgencia) return 'Debe seleccionar la agencia.';
    if (!this.model.fechaLiquidacion) return 'Debe ingresar la fecha de liquidación.';
    if (!this.model.fechaContable) return 'Debe ingresar la fecha contable.';
    if (!this.model.tipoComprobante) return 'Debe seleccionar el tipo de comprobante.';

    return null;
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

      console.error(
        '❌ Error obteniendo comprobante:',
        e
      );

      this.error =
        'No se pudo obtener el consecutivo del comprobante.';
    }
  }
}
