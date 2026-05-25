import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';

import {
  ExtractoAsociadoApi,
  ExtractoAsociadoBusqueda,
  ExtractoAsociadoCuenta,
  ExtractoAsociadoMovimiento,
  ExtractoAsociadoResumen
} from './extracto-asociado.api';

@Component({
  selector: 'app-extracto-asociado',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './extracto-asociado.component.html',
  styleUrls: ['./extracto-asociado.component.scss']
})
export class ExtractoAsociadoComponent {

  private readonly api =
    inject(ExtractoAsociadoApi);

  formas: any[] = [];

  filtrosBusqueda = {
    documento: '',
    nombres: '',
    primerApellido: '',
    segundoApellido: '',
    codigoCuenta: ''
  };

  filtros = {
    fechaInicial: this.primerDiaMes(),
    fechaFinal: this.fechaHoy(),
    codigoForma: '0'
  };

  cargandoBusqueda = false;
  cargando = false;
  error = '';

  asociados: ExtractoAsociadoBusqueda[] = [];
  asociadoSeleccionado: ExtractoAsociadoBusqueda | null = null;

  resumen: ExtractoAsociadoResumen | null = null;
  cuentas: ExtractoAsociadoCuenta[] = [];
  movimientos: ExtractoAsociadoMovimiento[] = [];

  constructor() {
    this.cargarFormas();
  }

  async cargarFormas(): Promise<void> {

    try {

      this.formas =
        await this.api.listarFormasAhorro();

    } catch (e) {

      console.error('Error cargando formas:', e);
      this.formas = [];

    }
  }

  async buscarAsociados(): Promise<void> {

    this.error = '';
    this.asociados = [];
    this.asociadoSeleccionado = null;
    this.resumen = null;
    this.cuentas = [];
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

      this.asociados =
        await this.api.buscarAsociados(this.filtrosBusqueda);

      if (this.asociados.length === 0) {
        this.error = 'No se encontraron asociados.';
      }

    } catch (e: any) {

      console.error('Error buscando asociados:', e);

      this.error =
        e?.error?.message ||
        'No fue posible buscar los asociados.';

    } finally {

      this.cargandoBusqueda = false;

    }
  }

  seleccionarAsociado(
    asociado: ExtractoAsociadoBusqueda
  ): void {

    this.asociadoSeleccionado = asociado;
    this.resumen = null;
    this.cuentas = [];
    this.movimientos = [];
    this.error = '';

  }

  async consultar(): Promise<void> {

    this.error = '';

    if (!this.asociadoSeleccionado) {
      this.error = 'Debe seleccionar un asociado.';
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
          idDatosPersonal: this.asociadoSeleccionado.idDatosPersonal,
          fechaInicial: this.filtros.fechaInicial,
          fechaFinal: this.filtros.fechaFinal,
          codigoForma: this.filtros.codigoForma || '0'
        });

      this.resumen =
        response.resumen;

      this.cuentas =
        response.cuentas || [];

      this.movimientos =
        response.movimientos || [];

    } catch (e: any) {

      console.error('Error consultando extracto asociado:', e);

      this.error =
        e?.error?.message ||
        'No fue posible consultar el extracto del asociado.';

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

    this.filtros = {
      fechaInicial: this.primerDiaMes(),
      fechaFinal: this.fechaHoy(),
      codigoForma: '0'
    };

    this.asociados = [];
    this.asociadoSeleccionado = null;
    this.resumen = null;
    this.cuentas = [];
    this.movimientos = [];
    this.error = '';

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
