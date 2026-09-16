import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import {
  GeneralApi,
  AgenciaDTO
} from '../../../../shared/general/general.api';

import { ConcentracionCdatApi } from './concentracion-cdat.api';

import {
  ConcentracionCdatExporterService
} from './concentracion-cdat-exporter.service';

import {
  ConcentracionCdatDepositante,
  ConcentracionCdatDetalle,
  ConcentracionCdatFiltros,
  ConcentracionCdatResumen
} from './concentracion-cdat.models';


@Component({
  selector: 'app-concentracion-cdat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './concentracion-cdat.component.html',
  styleUrl: './concentracion-cdat.component.scss'
})
export class ConcentracionCdatComponent implements OnInit {

  // =========================================================
  // ESTADO
  // =========================================================

  cargando = false;
  cargandoDetalle = false;

  error = '';
  errorDetalle = '';

  cortes: string[] = [];
  agencias: AgenciaDTO[] = [];

  resumen:
    ConcentracionCdatResumen | null =
      null;

  ranking:
    ConcentracionCdatDepositante[] =
      [];

  detalle:
    ConcentracionCdatDetalle[] =
      [];

  depositanteSeleccionado:
    ConcentracionCdatDepositante | null =
      null;


  // =========================================================
  // FILTROS
  // =========================================================

  filtros: ConcentracionCdatFiltros = {
    fechaCorte: '',
    idAgencia: null
  };


  // =========================================================
  // PAGINACIÓN
  // =========================================================

  pagina = 1;
  porPagina = 25;


  constructor(
    private readonly api:
      ConcentracionCdatApi,

    private readonly generalApi:
      GeneralApi,

    private readonly exporter:
      ConcentracionCdatExporterService
  ) {
  }


  // =========================================================
  // INICIO
  // =========================================================

  ngOnInit(): void {

    this.cargando = true;
    this.error = '';

    forkJoin({

      cortes:
        this.api.cortes(),

      agencias:
        this.generalApi.listarAgencias()

    }).subscribe({

      next: response => {

        this.cortes =
          response.cortes ?? [];

        this.agencias =
          response.agencias ?? [];

        if (this.cortes.length === 0) {

          this.cargando = false;

          this.error =
            'No existen cierres mensuales de CDAT para consultar.';

          return;
        }

        this.filtros.fechaCorte =
          this.cortes[0];

        this.consultar();
      },

      error: error => {

        this.cargando = false;

        this.error =
          this.obtenerError(
            error,
            'No fue posible cargar los datos iniciales.'
          );
      }

    });
  }


  // =========================================================
  // CONSULTAR
  // =========================================================

  consultar(): void {

    if (!this.filtros.fechaCorte) {

      this.error =
        'Debe seleccionar una fecha de corte.';

      return;
    }

    this.cargando = true;
    this.error = '';

    this.limpiarDetalle();

    forkJoin({

      resumen:
        this.api.resumen(
          this.filtros
        ),

      ranking:
        this.api.ranking(
          this.filtros
        )

    }).subscribe({

      next: response => {

        this.resumen =
          response.resumen;

        this.ranking =
          response.ranking ?? [];

        this.pagina = 1;

        this.cargando = false;
      },

      error: error => {

        this.resumen = null;
        this.ranking = [];

        this.cargando = false;

        this.error =
          this.obtenerError(
            error,
            'No fue posible consultar la concentración de CDAT.'
          );
      }

    });
  }


  // =========================================================
  // CAMBIO DE CORTE
  // =========================================================

  cambiarCorte(): void {

    this.filtros.idAgencia =
      null;

    this.consultar();
  }


  // =========================================================
  // LIMPIAR FILTROS
  // =========================================================

  limpiar(): void {

    this.filtros = {
      fechaCorte:
        this.cortes.length > 0
          ? this.cortes[0]
          : '',
      idAgencia: null
    };

    this.consultar();
  }


  // =========================================================
  // DETALLE DEPOSITANTE
  // =========================================================

  verDetalle(
    depositante:
      ConcentracionCdatDepositante
  ): void {

    this.depositanteSeleccionado =
      depositante;

    this.detalle = [];
    this.errorDetalle = '';
    this.cargandoDetalle = true;

    this.api
      .detalle(
        this.filtros,
        depositante.idDatosPersonal
      )
      .subscribe({

        next: response => {

          this.detalle =
            response ?? [];

          this.cargandoDetalle =
            false;
        },

        error: error => {

          this.detalle = [];

          this.cargandoDetalle =
            false;

          this.errorDetalle =
            this.obtenerError(
              error,
              'No fue posible consultar el detalle del depositante.'
            );
        }

      });
  }


  limpiarDetalle(): void {

    this.depositanteSeleccionado =
      null;

    this.detalle = [];

    this.errorDetalle = '';

    this.cargandoDetalle = false;
  }


  // =========================================================
  // EXPORTACIÓN EXCEL
  // =========================================================

  exportarInforme(): void {

    if (
      !this.resumen
      ||
      this.ranking.length === 0
    ) {
      return;
    }

    this.exporter.exportarInforme(
      this.resumen,
      this.ranking,
      this.obtenerNombreAgencia()
    );
  }


  exportarDetalle(): void {

    if (
      !this.depositanteSeleccionado
      ||
      this.detalle.length === 0
    ) {
      return;
    }

    this.exporter.exportarDetalle(
      this.filtros.fechaCorte,
      this.obtenerNombreAgencia(),
      this.depositanteSeleccionado,
      this.detalle
    );
  }


  // =========================================================
  // PAGINACIÓN
  // =========================================================

  get rankingPagina():
    ConcentracionCdatDepositante[] {

    const inicio =
      (this.pagina - 1)
      * this.porPagina;

    return this.ranking.slice(
      inicio,
      inicio + this.porPagina
    );
  }


  get totalPaginas(): number {

    return Math.max(
      1,
      Math.ceil(
        this.ranking.length
        / this.porPagina
      )
    );
  }


  anteriorPagina(): void {

    if (this.pagina > 1) {
      this.pagina--;
    }
  }


  siguientePagina(): void {

    if (
      this.pagina < this.totalPaginas
    ) {
      this.pagina++;
    }
  }


  // =========================================================
  // AGENCIA
  // =========================================================

  obtenerNombreAgencia(): string {

    if (
      this.filtros.idAgencia === null
    ) {
      return 'Todas';
    }

    const agencia =
      this.agencias.find(
        item =>
          Number(item.idAgencia)
          === Number(
            this.filtros.idAgencia
          )
      );

    return agencia?.nombreAgencia
      ?? `Agencia ${this.filtros.idAgencia}`;
  }


  // =========================================================
  // FORMATOS
  // =========================================================

  moneda(
    valor:
      number
      | null
      | undefined
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        style: 'currency',
        currency: 'COP',
        maximumFractionDigits: 0
      }
    ).format(
      valor ?? 0
    );
  }


  porcentaje(
    valor:
      number
      | null
      | undefined
  ): string {

    return `${Number(
      valor ?? 0
    ).toLocaleString(
      'es-CO',
      {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }
    )} %`;
  }


  decimal(
    valor:
      number
      | null
      | undefined
  ): string {

    return Number(
      valor ?? 0
    ).toLocaleString(
      'es-CO',
      {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }
    );
  }


  // =========================================================
  // TRACK BY
  // =========================================================

  trackByDepositante(
    index: number,
    item: ConcentracionCdatDepositante
  ): number {

    return item.idDatosPersonal;
  }


  trackByDetalle(
    index: number,
    item: ConcentracionCdatDetalle
  ): number {

    return item.idCuentaCdat;
  }


  trackByCorte(
    index: number,
    item: string
  ): string {

    return item;
  }


  trackByAgencia(
    index: number,
    item: AgenciaDTO
  ): number {

    return item.idAgencia;
  }


  // =========================================================
  // ERROR
  // =========================================================

  private obtenerError(
    error: any,
    defecto: string
  ): string {

    if (
      typeof error?.error === 'string'
      &&
      error.error.trim()
    ) {
      return error.error;
    }

    if (
      typeof error?.error?.message === 'string'
      &&
      error.error.message.trim()
    ) {
      return error.error.message;
    }

    if (
      typeof error?.message === 'string'
      &&
      error.message.trim()
    ) {
      return error.message;
    }

    return defecto;
  }

}
