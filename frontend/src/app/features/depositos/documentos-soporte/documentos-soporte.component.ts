import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent }
  from '../../../shared/header-actions/header-actions.component';

import {
  DocumentosSoporteApi,
  DocumentosSoporteCuenta,
  DocumentosSoporteActivo,
  DocumentosSoporteHistorico
} from './documentos-soporte.api';

import { SessionService }
  from '../../../core/auth/session.service';

import { DocumentosSoporteExporterService }
  from './documentos-soporte-exporter.service';

@Component({
  selector: 'app-documentos-soporte',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './documentos-soporte.component.html',
  styleUrls: ['./documentos-soporte.component.scss']
})
export class DocumentosSoporteComponent implements OnInit {

  private readonly api = inject(DocumentosSoporteApi);
  private readonly session = inject(SessionService);
  private readonly exporter = inject(DocumentosSoporteExporterService);

  agenciaActiva: any = null;

  filtros = {
    documento: '',
    nombres: '',
    primerApellido: '',
    segundoApellido: ''
  };

  cuentas: DocumentosSoporteCuenta[] = [];

  cuentaSeleccionada: DocumentosSoporteCuenta | null = null;
  documentoActivo: DocumentosSoporteActivo | null = null;
  historico: DocumentosSoporteHistorico[] = [];

  accion = '';

  numeroInicial = '';

  fechaEntrega = this.fechaHoy();

  cargando = false;
  guardando = false;

  error = '';
  mensaje = '';

  async ngOnInit(): Promise<void> {
    this.agenciaActiva =
      this.session.getAgenciaActiva();
  }

  async buscar(): Promise<void> {

    this.error = '';
    this.mensaje = '';
    this.cuentaSeleccionada = null;
    this.documentoActivo = null;
    this.historico = [];
    this.accion = '';
    this.numeroInicial = '';

    const tieneFiltro =
      !!this.filtros.documento?.trim()
      || !!this.filtros.nombres?.trim()
      || !!this.filtros.primerApellido?.trim()
      || !!this.filtros.segundoApellido?.trim();

    if (!tieneFiltro) {
      this.error =
        'Debe indicar al menos un filtro de búsqueda.';
      return;
    }

    const idAgencia =
      this.agenciaActiva?.id_agencia ??
      this.agenciaActiva?.idAgencia ??
      0;

    if (!idAgencia) {
      this.error =
        'No se pudo identificar la agencia activa.';
      return;
    }

    this.cargando = true;

    try {

      this.cuentas =
        await this.api.buscarCuentas({

          idAgencia,

          documento:
            this.filtros.documento,

          nombres:
            this.filtros.nombres,

          primerApellido:
            this.filtros.primerApellido,

          segundoApellido:
            this.filtros.segundoApellido

        });

      if (this.cuentas.length === 0) {

        this.mensaje =
          'No se encontraron cuentas.';

      }

    } catch (e: any) {

      console.error(e);

      this.error =
        e?.error?.message ||
        'No fue posible consultar.';

    } finally {

      this.cargando = false;

    }

  }

  async prepararAccion(
    cuenta: DocumentosSoporteCuenta,
    accion: string
  ): Promise<void> {

    this.error = '';
    this.mensaje = '';

    this.accion = accion;
    this.numeroInicial = '';
    this.fechaEntrega = this.fechaHoy();

    await this.seleccionarCuenta(cuenta);

  }

  async seleccionarCuenta(
    cuenta: DocumentosSoporteCuenta
  ): Promise<void> {

    this.cuentaSeleccionada = cuenta;

    try {
      this.documentoActivo =
        await this.api.obtenerActivo(
          cuenta.idCuentaAhorro
        );

      this.historico =
        await this.api.obtenerHistorico(
          cuenta.idCuentaAhorro
        );

    } catch (e) {
      console.error(e);
      this.documentoActivo = null;
      this.historico = [];
    }

  }

  async guardar(): Promise<void> {

    this.error = '';
    this.mensaje = '';

    if (!this.cuentaSeleccionada) {
      this.error = 'Debe seleccionar una cuenta.';
      return;
    }

    if (!this.accion) {
      this.error = 'Debe seleccionar una acción.';
      return;
    }

    if (
      (this.accion === 'INCLUIR' || this.accion === 'CAMBIAR') &&
      !this.numeroInicial
    ) {
      this.error = 'Debe indicar el número inicial.';
      return;
    }

    if (
      (this.accion === 'INCLUIR' || this.accion === 'CAMBIAR') &&
      !/^\d{1,10}$/.test(this.numeroInicial)
    ) {
      this.error = 'El número inicial debe ser numérico y máximo de 10 dígitos.';
      return;
    }

    this.guardando = true;

    try {

      const cuentaActual = this.cuentaSeleccionada;

      const response =
        await this.api.guardar({
          accion: this.accion,
          idCuentaAhorro: cuentaActual.idCuentaAhorro,
          numeroInicial: this.numeroInicial,
          fechaEntrega: this.fechaEntrega
        });

      if (!response.success) {
        this.error =
          response.message ||
          'No fue posible guardar el documento soporte.';
        return;
      }

      this.mensaje =
        response.message ||
        'Documento registrado correctamente.';

      this.accion = '';
      this.numeroInicial = '';

      await this.buscar();

    } catch (e: any) {

      console.error(e);

      this.error =
        e?.error?.message ||
        'No fue posible guardar.';

    } finally {

      this.guardando = false;

    }

  }

  limpiar(): void {

    this.filtros = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };

    this.cuentas = [];
    this.cuentaSeleccionada = null;
    this.documentoActivo = null;
    this.historico = [];
    this.accion = '';
    this.numeroInicial = '';
    this.fechaEntrega = this.fechaHoy();
    this.error = '';
    this.mensaje = '';

  }

  get mostrarFormularioNumero(): boolean {
    return this.accion === 'INCLUIR'
      || this.accion === 'CAMBIAR';
  }

  get tituloAccion(): string {

    switch (this.accion) {
      case 'INCLUIR':
        return 'Crear documento soporte';
      case 'CAMBIAR':
        return 'Cambiar documento soporte';
      case 'INACTIVAR':
        return 'Inhabilitar documento soporte';
      case 'PERDIDO':
        return 'Marcar documento como perdido';
      case 'ROBADO':
        return 'Marcar documento como robado';
      default:
        return 'Gestión documento soporte';
    }

  }

  private fechaHoy(): string {
    return new Date().toISOString().substring(0, 10);
  }

  soloNumerosDocumento(): void {

    this.numeroInicial =
      (this.numeroInicial || '')
        .replace(/[^0-9]/g, '');

  }

  exportarExcel(): void {

    this.exporter.exportarExcel(
      this.cuentaSeleccionada,
      this.historico
    );

  }

}
