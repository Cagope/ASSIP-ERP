import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';

import {
  ExtractoCuentaApi,
  ExtractoCuentaBusqueda,
  ExtractoCuentaMovimiento,
  ExtractoCuentaResumen
} from './extracto-cuenta.api';

import {
  ExtractoCuentaExporterService
} from './extracto-cuenta-exporter.service';

import {
  ExtractoCuentaPrintService
} from './extracto-cuenta-print.service';


@Component({
  selector: 'app-extracto-cuenta',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './extracto-cuenta.component.html',
  styleUrls: ['./extracto-cuenta.component.scss']
})
export class ExtractoCuentaComponent {

  private readonly api =
    inject(ExtractoCuentaApi);

  private readonly exporter =
    inject(ExtractoCuentaExporterService);

  private readonly printService =
    inject(ExtractoCuentaPrintService);

  filtrosBusqueda = {
    documento: '',
    nombres: '',
    primerApellido: '',
    segundoApellido: '',
    codigoCuenta: ''
  };
  cargandoBusqueda = false;

  cuentas: ExtractoCuentaBusqueda[] = [];
  cuentaSeleccionada: ExtractoCuentaBusqueda | null = null;

  filtros = {
    fechaInicial: this.primerDiaMes(),
    fechaFinal: this.fechaHoy()
  };

  cargando = false;
  error = '';

  resumen: ExtractoCuentaResumen | null = null;
  movimientos: ExtractoCuentaMovimiento[] = [];

  async buscarCuentas(): Promise<void> {

    this.error = '';
    this.cuentas = [];
    this.cuentaSeleccionada = null;
    this.resumen = null;
    this.movimientos = [];

    const tieneFiltros =
      this.filtrosBusqueda.documento ||
      this.filtrosBusqueda.nombres ||
      this.filtrosBusqueda.primerApellido ||
      this.filtrosBusqueda.segundoApellido ||
      this.filtrosBusqueda.codigoCuenta;

    if (!tieneFiltros) {
      this.error = 'Debe ingresar al menos un filtro de búsqueda.';
      return;
    }

    this.cargandoBusqueda = true;

    try {

      this.cuentas =
        await this.api.buscarCuentas(this.filtrosBusqueda);

      if (this.cuentas.length === 0) {
        this.error = 'No se encontraron cuentas.';
      }

    } catch (e: any) {

      console.error('Error buscando cuentas:', e);

      this.error =
        e?.error?.message ||
        'No fue posible buscar las cuentas.';

    } finally {

      this.cargandoBusqueda = false;

    }

  }

  seleccionarCuenta(
    cuenta: ExtractoCuentaBusqueda
  ): void {

    this.cuentaSeleccionada = cuenta;
    this.resumen = null;
    this.movimientos = [];
    this.error = '';

  }

  async consultar(): Promise<void> {

    this.error = '';

    if (!this.cuentaSeleccionada) {
      this.error = 'Debe seleccionar una cuenta.';
      return;
    }

    if (!this.filtros.fechaInicial) {
      this.error = 'Debe seleccionar la fecha inicial.';
      return;
    }

    if (!this.filtros.fechaFinal) {
      this.error = 'Debe seleccionar la fecha final.';
      return;
    }

    if (this.filtros.fechaInicial > this.filtros.fechaFinal) {
      this.error = 'La fecha inicial no puede ser mayor que la fecha final.';
      return;
    }

    this.cargando = true;

    try {

      const response =
        await this.api.consultar({
          idCuentaAhorro: this.cuentaSeleccionada.idCuentaAhorro,
          fechaInicial: this.filtros.fechaInicial,
          fechaFinal: this.filtros.fechaFinal
        });

      this.resumen =
        response.resumen;

      this.movimientos =
        response.movimientos || [];

    } catch (e: any) {

      console.error('Error consultando extracto:', e);

      this.error =
        e?.error?.message ||
        'No fue posible consultar el extracto.';

    } finally {

      this.cargando = false;

    }

  }

  limpiar(): void {

    this.filtrosBusqueda = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: '',
      codigoCuenta: ''
    };

    this.cuentas = [];
    this.cuentaSeleccionada = null;

    this.filtros = {
      fechaInicial: this.primerDiaMes(),
      fechaFinal: this.fechaHoy()
    };

    this.resumen = null;
    this.movimientos = [];
    this.error = '';

  }

  exportar(): void {

    this.exporter.exportarExcel(
      this.resumen,
      this.movimientos,
      this.filtros.fechaInicial,
      this.filtros.fechaFinal
    );

  }

  imprimir(): void {

    if (!this.resumen || this.movimientos.length === 0) {
      return;
    }

    this.printService.imprimir(
      this.resumen,
      this.movimientos,
      this.filtros.fechaInicial,
      this.filtros.fechaFinal
    );

  }

  async generarPdf(): Promise<void> {

    if (!this.cuentaSeleccionada || !this.resumen) {
      this.error = 'Debe consultar primero el extracto.';
      return;
    }

    const pdf =
      await this.api.generarPdf({
        idCuentaAhorro: this.cuentaSeleccionada.idCuentaAhorro,
        fechaInicial: this.filtros.fechaInicial,
        fechaFinal: this.filtros.fechaFinal
      });

    const url =
      window.URL.createObjectURL(pdf);

    const a =
      document.createElement('a');

    a.href = url;
    a.download =
      `extracto-cuenta-${this.resumen.codigoCuenta}.pdf`;

    a.click();

    window.URL.revokeObjectURL(url);
  }

  private fechaHoy(): string {

    return new Date()
      .toISOString()
      .substring(0, 10);

  }

  private primerDiaMes(): string {

    const hoy =
      new Date();

    const primerDia =
      new Date(
        hoy.getFullYear(),
        hoy.getMonth(),
        1
      );

    return primerDia
      .toISOString()
      .substring(0, 10);

  }

}
