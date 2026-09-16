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
  GeneralApi,
  AgenciaDTO
} from '../../../../shared/general/general.api';

import {
  TasasCondicionesCdatApi
} from './tasas-condiciones-cdat.api';

import {
  TasasCondicionesCdatExporterService
} from './tasas-condiciones-cdat-exporter.service';

import {
  TasasCondicionesCdatAmortizacion,
  TasasCondicionesCdatCondicion,
  TasasCondicionesCdatCorte,
  TasasCondicionesCdatDetalle,
  TasasCondicionesCdatFiltros,
  TasasCondicionesCdatRango,
  TasasCondicionesCdatRangoSaldo,
  TasasCondicionesCdatResumen
} from './tasas-condiciones-cdat.models';


@Component({
  selector: 'app-tasas-condiciones-cdat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl:
    './tasas-condiciones-cdat.component.html',
  styleUrl:
    './tasas-condiciones-cdat.component.scss'
})
export class TasasCondicionesCdatComponent implements OnInit {

  private readonly api =
    inject(TasasCondicionesCdatApi);

  private readonly generalApi =
    inject(GeneralApi);

  private readonly exporter =
    inject(TasasCondicionesCdatExporterService);


  // =========================================================
  // CATÁLOGOS
  // =========================================================

  agencias: AgenciaDTO[] = [];

  cortes:
    TasasCondicionesCdatCorte[] = [];

  readonly amortizaciones:
    TasasCondicionesCdatAmortizacion[] = [

      {
        codigo: 'M',
        nombre: 'Mensual'
      },

      {
        codigo: 'B',
        nombre: 'Bimestral'
      },

      {
        codigo: 'T',
        nombre: 'Trimestral'
      },

      {
        codigo: 'S',
        nombre: 'Semestral'
      },

      {
        codigo: 'A',
        nombre: 'Anual'
      }

    ];


  readonly rangosSaldo:
    TasasCondicionesCdatRango[] = [

      {
        codigo: '01. MENOR A 10 MILLONES',
        nombre: 'Menor a $10 millones'
      },

      {
        codigo: '02. 10 A 50 MILLONES',
        nombre: '$10 a $50 millones'
      },

      {
        codigo: '03. 50 A 100 MILLONES',
        nombre: '$50 a $100 millones'
      },

      {
        codigo: '04. 100 A 250 MILLONES',
        nombre: '$100 a $250 millones'
      },

      {
        codigo: '05. 250 A 500 MILLONES',
        nombre: '$250 a $500 millones'
      },

      {
        codigo: '06. 500 MILLONES O MAS',
        nombre: '$500 millones o más'
      }

    ];


  // =========================================================
  // FILTROS
  // =========================================================

  fechaCorte = '';

  idAgencia = 0;

  plazoMeses = 0;

  amortizacion = '';

  rangoSaldo = '';


  // =========================================================
  // ESTADO
  // =========================================================

  cargando = false;

  cargandoCatalogos = false;

  consultado = false;

  error = '';


  // =========================================================
  // RESULTADO
  // =========================================================

  resumen:
    TasasCondicionesCdatResumen | null = null;

  condiciones:
    TasasCondicionesCdatCondicion[] = [];

  rangos:
    TasasCondicionesCdatRangoSaldo[] = [];

  detalle:
    TasasCondicionesCdatDetalle[] = [];


  // =========================================================
  // INICIO
  // =========================================================

  ngOnInit(): void {

    this.cargarAgencias();

    this.cargarCortes();
  }


  // =========================================================
  // CATÁLOGOS
  // =========================================================

  private cargarAgencias(): void {

    this.cargandoCatalogos = true;

    this.generalApi
      .listarAgencias()
      .subscribe({

        next: (agencias) => {

          this.agencias =
            agencias ?? [];

          this.cargandoCatalogos = false;
        },

        error: () => {

          this.agencias = [];

          this.cargandoCatalogos = false;
        }

      });
  }


  private cargarCortes(): void {

    this.api
      .listarCortes()
      .subscribe({

        next: (cortes) => {

          this.cortes =
            cortes ?? [];

          if (this.cortes.length > 0) {

            this.fechaCorte =
              this.cortes[0].fechaCorte;

            this.consultar();

          } else {

            this.fechaCorte = '';

            this.error =
              'No existen cortes mensuales CDAT disponibles para analizar.';

            this.limpiarResultado();
          }
        },

        error: (err) => {

          this.cortes = [];

          this.fechaCorte = '';

          this.limpiarResultado();

          this.error =
            this.obtenerMensajeError(err);
        }

      });
  }


  // =========================================================
  // CONSULTAR
  // =========================================================

  consultar(): void {

    this.error = '';

    if (!this.fechaCorte) {

      this.limpiarResultado();

      this.error =
        'Seleccione un corte mensual.';

      return;
    }

    const filtros =
      this.obtenerFiltros();

    this.cargando = true;
    this.consultado = false;

    forkJoin({

      resumen:
        this.api.obtenerResumen(filtros),

      condiciones:
        this.api.obtenerCondiciones(filtros),

      rangos:
        this.api.obtenerRangosSaldo(filtros),

      detalle:
        this.api.obtenerDetalle(filtros)

    }).subscribe({

      next: (response) => {

        this.resumen =
          response.resumen;

        this.condiciones =
          response.condiciones ?? [];

        this.rangos =
          response.rangos ?? [];

        this.detalle =
          response.detalle ?? [];

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

    if (!this.resumen) {
      return;
    }

    this.exporter.exportar(
      this.resumen,
      this.condiciones,
      this.rangos,
      this.detalle,
      this.fechaCorte,
      this.obtenerNombreCorte(),
      this.obtenerNombreAgencia(),
      this.plazoMeses,
      this.obtenerNombreAmortizacion(),
      this.obtenerNombreRango()
    );
  }

  // =========================================================
  // LIMPIAR
  // =========================================================

  limpiar(): void {

    this.idAgencia = 0;

    this.plazoMeses = 0;

    this.amortizacion = '';

    this.rangoSaldo = '';

    this.error = '';

    if (this.fechaCorte) {
      this.consultar();
    } else {
      this.limpiarResultado();
    }
  }


  // =========================================================
  // FILTROS
  // =========================================================

  private obtenerFiltros():
    TasasCondicionesCdatFiltros {

    return {

      fechaCorte:
        this.fechaCorte ?? '',

      idAgencia:
        Number(this.idAgencia) || 0,

      plazoMeses:
        Number(this.plazoMeses) || 0,

      amortizacion:
        this.amortizacion ?? '',

      rangoSaldo:
        this.rangoSaldo ?? ''

    };
  }


  // =========================================================
  // NOMBRES FILTROS
  // =========================================================

  obtenerNombreCorte(): string {

    if (!this.fechaCorte) {
      return '';
    }

    const corte =
      this.cortes.find(
        item =>
          item.fechaCorte ===
          this.fechaCorte
      );

    if (!corte) {
      return this.fechaCorte;
    }

    return `${this.obtenerNombreMes(corte.mes)} ${corte.anio}`;
  }


  private obtenerNombreMes(
    mes: number
  ): string {

    const meses = [
      'Enero',
      'Febrero',
      'Marzo',
      'Abril',
      'Mayo',
      'Junio',
      'Julio',
      'Agosto',
      'Septiembre',
      'Octubre',
      'Noviembre',
      'Diciembre'
    ];

    return meses[mes - 1]
      ?? String(mes);
  }


  private obtenerNombreAgencia(): string {

    if (!this.idAgencia) {
      return 'Todas';
    }

    const agencia =
      this.agencias.find(
        item =>
          item.idAgencia ===
          Number(this.idAgencia)
      );

    return agencia?.nombreAgencia
      ?? `Agencia ${this.idAgencia}`;
  }


  private obtenerNombreAmortizacion(): string {

    if (!this.amortizacion) {
      return 'Todas';
    }

    return this.amortizaciones.find(
      item =>
        item.codigo === this.amortizacion
    )?.nombre ?? this.amortizacion;
  }


  private obtenerNombreRango(): string {

    if (!this.rangoSaldo) {
      return 'Todos';
    }

    return this.rangosSaldo.find(
      item =>
        item.codigo === this.rangoSaldo
    )?.nombre ?? this.rangoSaldo;
  }

  // =========================================================
  // GRÁFICOS
  // =========================================================

  obtenerAlturaCondicion(
    item: TasasCondicionesCdatCondicion
  ): number {

    if (!this.condiciones.length) {
      return 0;
    }

    const maximo =
      Math.max(
        ...this.condiciones.map(
          condicion =>
            Number(condicion.saldoTotal) || 0
        )
      );

    if (maximo <= 0) {
      return 0;
    }

    const valor =
      Number(item.saldoTotal) || 0;

    return Math.max(
      4,
      (valor / maximo) * 100
    );
  }


  obtenerAnchoRango(
    item: TasasCondicionesCdatRangoSaldo
  ): number {

    const participacion =
      Number(item.participacionSaldo) || 0;

    if (participacion <= 0) {
      return 0;
    }

    return Math.min(
      100,
      participacion
    );
  }


  obtenerEtiquetaCondicion(
    item: TasasCondicionesCdatCondicion
  ): string {

    const amortizacion =
      item.nombreAmortizacion
      || item.amortizacionDeposito
      || '';

    return `${item.plazoMeses}m - ${amortizacion}`;
  }


  obtenerEtiquetaRango(
    item: TasasCondicionesCdatRangoSaldo
  ): string {

    const codigo =
      item.rangoSaldo ?? '';

    return codigo.replace(
      /^\d+\.\s*/,
      ''
    );
  }

  // =========================================================
  // TRACK BY
  // =========================================================

  trackByCorte(
    index: number,
    item: TasasCondicionesCdatCorte
  ): string {

    return item.fechaCorte;
  }


  trackByCondicion(
    index: number,
    item: TasasCondicionesCdatCondicion
  ): string {

    return `${item.plazoMeses}-${item.amortizacionDeposito}`;
  }


  trackByRango(
    index: number,
    item: TasasCondicionesCdatRangoSaldo
  ): string {

    return item.rangoSaldo;
  }


  trackByDetalle(
    index: number,
    item: TasasCondicionesCdatDetalle
  ): number {

    return item.idCuentaCdat;
  }


  trackByAgencia(
    index: number,
    item: AgenciaDTO
  ): number {

    return item.idAgencia;
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

    return 'No fue posible consultar el análisis de tasas y condiciones CDAT.';
  }


  private limpiarResultado(): void {

    this.resumen = null;

    this.condiciones = [];

    this.rangos = [];

    this.detalle = [];

    this.consultado = false;
  }

}
