import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';

import {
  forkJoin
} from 'rxjs';

import {
  ConcentracionDepositosApi
} from './concentracion-depositos.api';

import {
  ConcentracionDepositosExporterService
} from './concentracion-depositos-exporter.service';

import {
  AgenciaDTO,
  AgenciaFormaApi,
  FormaAhorroDTO
} from '../../../../shared/agencia-forma/agencia-forma.api';

import {
  ConcentracionDepositosAgencia,
  ConcentracionDepositosAsociado,
  ConcentracionDepositosForma,
  ConcentracionDepositosRequest,
  ConcentracionDepositosResponse,
  ConcentracionDepositosResumen,
  ConcentracionDepositosTop
} from './concentracion-depositos.models';


@Component({
  selector: 'app-concentracion-depositos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './concentracion-depositos.component.html',
  styleUrl: './concentracion-depositos.component.scss'
})
export class ConcentracionDepositosComponent implements OnInit {

  private readonly api =
    inject(ConcentracionDepositosApi);

  private readonly agenciaFormaApi =
    inject(AgenciaFormaApi);

  private readonly exporter =
    inject(ConcentracionDepositosExporterService);


  // =========================================================
  // CATÁLOGOS
  // =========================================================

  agencias: AgenciaDTO[] = [];

  formasAhorro: FormaAhorroDTO[] = [];


  // =========================================================
  // FILTROS
  // =========================================================

  fechaCorte = '';

  idAgencia = 0;

  idFormaAhorro = 0;


  // =========================================================
  // ESTADO
  // =========================================================

  cargando = false;

  cargandoCatalogos = false;

  error = '';

  consultado = false;


  // =========================================================
  // RESULTADO
  // =========================================================

  resultado: ConcentracionDepositosResponse | null = null;

  resumen: ConcentracionDepositosResumen | null = null;

  agenciasResultado: ConcentracionDepositosAgencia[] = [];

  formas: ConcentracionDepositosForma[] = [];

  concentracion: ConcentracionDepositosTop[] = [];

  asociados: ConcentracionDepositosAsociado[] = [];


  // =========================================================
  // INICIO
  // =========================================================

  ngOnInit(): void {

    this.fechaCorte =
      this.obtenerFechaActual();

    this.cargarAgencias();

  }


  // =========================================================
  // CATÁLOGOS
  // =========================================================

  private cargarAgencias(): void {

    this.cargandoCatalogos = true;

    this.agenciaFormaApi
      .listarAgencias()
      .subscribe({

        next: (agencias) => {

          this.agencias =
            agencias ?? [];

          this.cargarTodasLasFormas();

        },

        error: () => {

          this.agencias = [];
          this.formasAhorro = [];
          this.cargandoCatalogos = false;

        }

      });

  }


  private cargarTodasLasFormas(): void {

    if (this.agencias.length === 0) {

      this.formasAhorro = [];
      this.cargandoCatalogos = false;

      return;
    }

    const consultas =
      this.agencias.map(
        agencia =>
          this.agenciaFormaApi.listarFormasPorAgencia(
            agencia.idAgencia
          )
      );

    forkJoin(consultas)
      .subscribe({

        next: (resultados) => {

          const mapa =
            new Map<number, FormaAhorroDTO>();

          resultados
            .flat()
            .forEach(forma => {

              if (!mapa.has(forma.idFormaAhorro)) {

                mapa.set(
                  forma.idFormaAhorro,
                  forma
                );

              }

            });

          this.formasAhorro =
            Array.from(
              mapa.values()
            ).sort(
              (a, b) =>
                (a.codigoForma ?? '')
                  .localeCompare(
                    b.codigoForma ?? ''
                  )
            );

          this.cargandoCatalogos = false;

        },

        error: () => {

          this.formasAhorro = [];
          this.cargandoCatalogos = false;

        }

      });

  }


  cambiarAgencia(): void {

    this.idFormaAhorro = 0;

    if (!this.idAgencia) {

      this.cargandoCatalogos = true;
      this.cargarTodasLasFormas();

      return;
    }

    this.cargandoCatalogos = true;

    this.agenciaFormaApi
      .listarFormasPorAgencia(
        this.idAgencia
      )
      .subscribe({

        next: (formas) => {

          this.formasAhorro =
            formas ?? [];

          this.cargandoCatalogos = false;

        },

        error: () => {

          this.formasAhorro = [];
          this.cargandoCatalogos = false;

        }

      });

  }


  // =========================================================
  // CONSULTAR
  // =========================================================

  consultar(): void {

    this.error = '';

    if (!this.fechaCorte) {

      this.error =
        'Debe seleccionar la fecha de corte.';

      return;
    }

    const request: ConcentracionDepositosRequest = {
      fechaCorte: this.fechaCorte,
      idAgencia: this.idAgencia,
      idFormaAhorro: this.idFormaAhorro
    };

    this.cargando = true;
    this.consultado = false;

    this.api
      .analizar(request)
      .subscribe({

        next: (response) => {

          this.resultado =
            response;

          this.resumen =
            response.resumen;

          this.agenciasResultado =
            response.agencias ?? [];

          this.formas =
            response.formas ?? [];

          this.concentracion =
            response.concentracion ?? [];

          this.asociados =
            response.asociados ?? [];

          this.consultado = true;
          this.cargando = false;

        },

        error: (err) => {

          this.limpiarResultado();

          this.error =
            this.obtenerMensajeError(err);

          this.cargando = false;

        }

      });

  }


  // =========================================================
  // EXPORTAR
  // =========================================================

  exportar(): void {

    if (!this.resultado) {
      return;
    }

    const nombreAgencia =
      this.obtenerNombreAgencia();

    const nombreFormaAhorro =
      this.obtenerNombreFormaAhorro();

    this.exporter.exportar(
      this.resultado,
      nombreAgencia,
      nombreFormaAhorro
    );

  }


  private obtenerNombreAgencia(): string {

    if (!this.idAgencia) {
      return 'Todas';
    }

    const agencia =
      this.agencias.find(
        item =>
          item.idAgencia === this.idAgencia
      );

    return agencia?.nombreAgencia
      ?? `Agencia ${this.idAgencia}`;

  }


  private obtenerNombreFormaAhorro(): string {

    if (!this.idFormaAhorro) {
      return 'Todas';
    }

    const forma =
      this.formasAhorro.find(
        item =>
          item.idFormaAhorro === this.idFormaAhorro
      );

    if (!forma) {
      return `Forma ${this.idFormaAhorro}`;
    }

    return `${forma.codigoForma} - ${forma.nombreForma}`;

  }


  // =========================================================
  // LIMPIAR
  // =========================================================

  limpiar(): void {

    this.fechaCorte =
      this.obtenerFechaActual();

    this.idAgencia = 0;
    this.idFormaAhorro = 0;

    this.error = '';
    this.consultado = false;

    this.limpiarResultado();

    this.cargandoCatalogos = true;
    this.cargarTodasLasFormas();

  }


  private limpiarResultado(): void {

    this.resultado = null;
    this.resumen = null;
    this.agenciasResultado = [];
    this.formas = [];
    this.concentracion = [];
    this.asociados = [];

  }


  // =========================================================
  // TOP
  // =========================================================

  obtenerTop(
    grupo: string
  ): ConcentracionDepositosTop | undefined {

    return this.concentracion.find(
      item => item.grupo === grupo
    );

  }


  // =========================================================
  // APOYO
  // =========================================================

  porcentaje(
    valor: number | null | undefined
  ): number {

    return valor ?? 0;

  }


  saldo(
    valor: number | null | undefined
  ): number {

    return valor ?? 0;

  }


  trackByAgenciaResultado(
    index: number,
    item: ConcentracionDepositosAgencia
  ): number {

    return item.idAgencia;

  }


  trackByFormaResultado(
    index: number,
    item: ConcentracionDepositosForma
  ): number {

    return item.idFormaAhorro;

  }


  trackByAsociado(
    index: number,
    item: ConcentracionDepositosAsociado
  ): number {

    return item.idDatosPersonal;

  }


  trackByAgencia(
    index: number,
    item: AgenciaDTO
  ): number {

    return item.idAgencia;

  }


  trackByFormaCatalogo(
    index: number,
    item: FormaAhorroDTO
  ): number {

    return item.idFormaAhorro;

  }


  // =========================================================
  // FECHA
  // =========================================================

  private obtenerFechaActual(): string {

    const fecha =
      new Date();

    const anio =
      fecha.getFullYear();

    const mes =
      String(
        fecha.getMonth() + 1
      ).padStart(
        2,
        '0'
      );

    const dia =
      String(
        fecha.getDate()
      ).padStart(
        2,
        '0'
      );

    return `${anio}-${mes}-${dia}`;

  }


  // =========================================================
  // ERRORES
  // =========================================================

  private obtenerMensajeError(
    error: any
  ): string {

    if (
      typeof error?.error === 'string'
      && error.error.trim()
    ) {

      return error.error;

    }

    if (
      typeof error?.error?.message === 'string'
      && error.error.message.trim()
    ) {

      return error.error.message;

    }

    return 'No fue posible consultar la concentración de depósitos.';

  }

}
