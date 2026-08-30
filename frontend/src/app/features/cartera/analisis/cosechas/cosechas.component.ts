import {
  CommonModule
} from '@angular/common';

import {
  Component,
  OnInit
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  HeaderActionsComponent
} from '../../../../shared/header-actions/header-actions.component';

import {
  CosechasApi
} from './cosechas.api';

import {
  CosechasExporterService
} from './cosechas-exporter.service';

import {
  COSECHA_INDICADORES,
  CosechaCatalogo,
  CosechaCelda,
  CosechaCorte,
  CosechaDetalle,
  CosechaFila,
  CosechaIndicador,
  CosechaResumen
} from './cosechas.models';


@Component({
  selector:
    'app-cosechas',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],

  templateUrl:
    './cosechas.component.html',

  styleUrls: [
    './cosechas.component.scss'
  ]
})
export class CosechasComponent
implements OnInit {

  readonly indicadores =
    COSECHA_INDICADORES;


  // =========================================================
  // CATÁLOGOS
  // =========================================================

  cortes:
    CosechaCorte[] = [];

  agencias:
    CosechaCatalogo[] = [];

  lineas:
    CosechaCatalogo[] = [];


  // =========================================================
  // FILTROS
  // =========================================================

  cosechaDesdeMes = '';

  cosechaHastaMes = '';

  hastaCorte = '';

  idAgencia:
    number | null = null;

  idLineaCredito:
    number | null = null;

  indicador:
    CosechaIndicador =
      'SALDO_REMANENTE';


  // =========================================================
  // RESULTADO
  // =========================================================

  resumen:
    CosechaResumen | null = null;


  // =========================================================
  // DETALLE
  // =========================================================

  detalle:
    CosechaDetalle[] = [];

  celdaSeleccionada:
    CosechaCelda | null = null;

  filtroDetalle = '';

  paginaDetalle = 1;

  tamanoPaginaDetalle = 20;


  // =========================================================
  // ESTADO
  // =========================================================

  cargandoCatalogos = false;

  cargandoAnalisis = false;

  cargandoDetalle = false;

  error:
    string | null = null;

  errorDetalle:
    string | null = null;


  constructor(
    private readonly api:
      CosechasApi,

    private readonly exporter:
      CosechasExporterService
  ) {}


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    this.cargarCatalogos();
  }


  // =========================================================
  // CATÁLOGOS
  // =========================================================

  cargarCatalogos(): void {

    this.cargandoCatalogos = true;

    this.error = null;


    this.api
      .listarCortes()
      .subscribe({

        next: respuesta => {

          this.cortes =
            Array.isArray(
              respuesta
            )
              ? respuesta
              : [];

          this.inicializarFechas();

          this.cargarAgencias();

        },

        error: error => {

          console.error(
            'Error consultando cortes de cosechas:',
            error
          );

          this.cargandoCatalogos = false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar los cortes disponibles.'
            );
        }

      });
  }


  private cargarAgencias(): void {

    this.api
      .listarAgencias()
      .subscribe({

        next: respuesta => {

          this.agencias =
            Array.isArray(
              respuesta
            )
              ? respuesta
              : [];

          this.cargarLineas();

        },

        error: error => {

          console.error(
            'Error consultando agencias:',
            error
          );

          this.cargandoCatalogos = false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar las agencias.'
            );
        }

      });
  }


  private cargarLineas(): void {

    this.api
      .listarLineas()
      .subscribe({

        next: respuesta => {

          this.lineas =
            Array.isArray(
              respuesta
            )
              ? respuesta
              : [];

          this.cargandoCatalogos = false;

        },

        error: error => {

          console.error(
            'Error consultando líneas de crédito:',
            error
          );

          this.cargandoCatalogos = false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar las líneas de crédito.'
            );
        }

      });
  }


  private inicializarFechas(): void {

    if (
      this.cortes.length === 0
    ) {
      return;
    }

    const fechas =
      this.cortes
        .map(
          item =>
            item.fechaCorte
        )
        .filter(
          fecha =>
            !!fecha
        )
        .sort()
        .reverse();


    this.hastaCorte =
      fechas[0]
      ?? '';


    if (
      !this.cosechaHastaMes
      &&
      this.hastaCorte
    ) {

      this.cosechaHastaMes =
        this.hastaCorte.substring(
          0,
          7
        );
    }


    if (
      !this.cosechaDesdeMes
      &&
      this.cosechaHastaMes
    ) {

      this.cosechaDesdeMes =
        this.restarMeses(
          this.cosechaHastaMes,
          11
        );
    }
  }


  // =========================================================
  // ANALIZAR
  // =========================================================

  analizar(): void {

    if (
      this.cargandoAnalisis
    ) {
      return;
    }


    if (
      !this.cosechaDesdeMes
    ) {

      this.error =
        'Seleccione la cosecha inicial.';

      return;
    }


    if (
      !this.cosechaHastaMes
    ) {

      this.error =
        'Seleccione la cosecha final.';

      return;
    }


    if (
      this.cosechaDesdeMes
      >
      this.cosechaHastaMes
    ) {

      this.error =
        'La cosecha inicial no puede ser posterior a la cosecha final.';

      return;
    }


    if (
      !this.hastaCorte
    ) {

      this.error =
        'Seleccione el corte de análisis.';

      return;
    }


    const cosechaDesde =
      this.primerDiaMes(
        this.cosechaDesdeMes
      );

    const cosechaHasta =
      this.primerDiaMes(
        this.cosechaHastaMes
      );


    if (
      cosechaHasta
      >
      this.hastaCorte
    ) {

      this.error =
        'La cosecha final no puede ser posterior al corte de análisis.';

      return;
    }


    this.cargandoAnalisis = true;

    this.error = null;

    this.resumen = null;

    this.cerrarDetalle();


    this.api
      .analizar({

        cosechaDesde,

        cosechaHasta,

        hastaCorte:
          this.hastaCorte,

        idAgencia:
          this.idAgencia,

        idLineaCredito:
          this.idLineaCredito

      })
      .subscribe({

        next: respuesta => {

          this.resumen =
            respuesta;

          this.cargandoAnalisis =
            false;

        },

        error: error => {

          console.error(
            'Error ejecutando análisis de cosechas:',
            error
          );

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible ejecutar el análisis de cosechas.'
            );

          this.cargandoAnalisis =
            false;
        }

      });
  }


  // =========================================================
  // CAMBIO DE INDICADOR
  // =========================================================

  cambiarIndicador(): void {

    this.cerrarDetalle();
  }


  // =========================================================
  // MOBS
  // =========================================================

  get mobs():
  number[] {

    if (
      !this.resumen
    ) {
      return [];
    }

    return Array.from(
      {
        length:
          this.numero(
            this.resumen.maxMob
          ) + 1
      },
      (_, index) =>
        index
    );
  }


  // =========================================================
  // OBTENER CELDA
  // =========================================================

  obtenerCelda(
    fila: CosechaFila,
    mob: number
  ): CosechaCelda | null {

    return fila.celdas.find(
      celda =>
        celda.mob === mob
    )
    ?? null;
  }


  // =========================================================
  // VALOR PRINCIPAL
  // =========================================================

  valorPrincipal(
    celda: CosechaCelda
  ): number {

    switch (
      this.indicador
    ) {

      case 'MORA_30':
        return this.numero(
          celda.porcentajeSaldoMora30SobreOriginacion
        );

      case 'MORA_60':
        return this.numero(
          celda.porcentajeSaldoMora60SobreOriginacion
        );

      case 'MORA_90':
        return this.numero(
          celda.porcentajeSaldoMora90SobreOriginacion
        );

      case 'MORA_180':
        return this.numero(
          celda.porcentajeSaldoMora180SobreOriginacion
        );

      case 'SALDO_REMANENTE':
      default:
        return this.numero(
          celda.porcentajeSaldoRemanente
        );
    }
  }


  // =========================================================
  // VALOR MONETARIO
  // =========================================================

  valorMonetario(
    celda: CosechaCelda
  ): number {

    switch (
      this.indicador
    ) {

      case 'MORA_30':
        return this.numero(
          celda.saldoMora30
        );

      case 'MORA_60':
        return this.numero(
          celda.saldoMora60
        );

      case 'MORA_90':
        return this.numero(
          celda.saldoMora90
        );

      case 'MORA_180':
        return this.numero(
          celda.saldoMora180
        );

      case 'SALDO_REMANENTE':
      default:
        return this.numero(
          celda.saldoCapital
        );
    }
  }


  // =========================================================
  // CANTIDAD
  // =========================================================

  cantidadIndicador(
    celda: CosechaCelda
  ): number {

    switch (
      this.indicador
    ) {

      case 'MORA_30':
        return this.numero(
          celda.cantidadMora30
        );

      case 'MORA_60':
        return this.numero(
          celda.cantidadMora60
        );

      case 'MORA_90':
        return this.numero(
          celda.cantidadMora90
        );

      case 'MORA_180':
        return this.numero(
          celda.cantidadMora180
        );

      case 'SALDO_REMANENTE':
      default:
        return this.numero(
          celda.cantidadConSaldo
        );
    }
  }


  // =========================================================
  // DETALLE CELDA
  // =========================================================

  seleccionarCelda(
    celda: CosechaCelda
  ): void {

    if (
      this.cargandoDetalle
    ) {
      return;
    }


    this.celdaSeleccionada =
      celda;

    this.detalle = [];

    this.filtroDetalle = '';

    this.paginaDetalle = 1;

    this.errorDetalle = null;

    this.cargandoDetalle = true;


    this.api
      .listarDetalle({

        cosecha:
          celda.cosecha,

        fechaCorte:
          celda.fechaCorte,

        indicador:
          this.indicador,

        idAgencia:
          this.idAgencia,

        idLineaCredito:
          this.idLineaCredito

      })
      .subscribe({

        next: respuesta => {

          this.detalle =
            Array.isArray(
              respuesta
            )
              ? respuesta
              : [];

          this.cargandoDetalle =
            false;

        },

        error: error => {

          console.error(
            'Error consultando detalle de cosecha:',
            error
          );

          this.detalle = [];

          this.errorDetalle =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar el detalle de la celda.'
            );

          this.cargandoDetalle =
            false;
        }

      });
  }


  cerrarDetalle(): void {

    if (
      this.cargandoDetalle
    ) {
      return;
    }

    this.detalle = [];

    this.celdaSeleccionada =
      null;

    this.filtroDetalle = '';

    this.paginaDetalle = 1;

    this.errorDetalle = null;
  }


  // =========================================================
  // DETALLE FILTRADO
  // =========================================================

  get detalleFiltrado():
  CosechaDetalle[] {

    const filtro =
      this.normalizarTexto(
        this.filtroDetalle
      );

    if (
      !filtro
    ) {
      return this.detalle;
    }


    return this.detalle.filter(
      item => {

        const contenido = [

          item.documento,

          item.nombreCompleto,

          item.pagareCartera,

          item.codigoLineaCredito,

          item.nombreLineaCredito,

          item.telefono,

          item.celular,

          item.correo,

          item.categoriaMora,

          item.codigoEstadoCartera,

          item.descripcionEstadoCartera

        ]
          .map(
            valor =>
              this.normalizarTexto(
                valor
              )
          )
          .join(
            ' '
          );

        return contenido.includes(
          filtro
        );
      }
    );
  }


  get totalPaginasDetalle():
  number {

    return Math.max(
      1,
      Math.ceil(
        this.detalleFiltrado.length
        /
        this.tamanoPaginaDetalle
      )
    );
  }


  get detallePaginado():
  CosechaDetalle[] {

    const pagina =
      Math.min(
        this.paginaDetalle,
        this.totalPaginasDetalle
      );

    const inicio =
      (
        pagina - 1
      )
      *
      this.tamanoPaginaDetalle;

    return this.detalleFiltrado.slice(
      inicio,
      inicio
      +
      this.tamanoPaginaDetalle
    );
  }


  paginaAnteriorDetalle(): void {

    if (
      this.paginaDetalle > 1
    ) {

      this.paginaDetalle--;
    }
  }


  paginaSiguienteDetalle(): void {

    if (
      this.paginaDetalle
      <
      this.totalPaginasDetalle
    ) {

      this.paginaDetalle++;
    }
  }


  cambiarFiltroDetalle(): void {

    this.paginaDetalle = 1;
  }


  // =========================================================
  // TOTALES DETALLE
  // =========================================================

  get totalSaldoDetalle():
  number {

    return this.detalle.reduce(
      (
        total,
        item
      ) =>
        total
        +
        this.numero(
          item.saldoCapital
        ),
      0
    );
  }


  // =========================================================
  // EXPORTAR
  // =========================================================

  exportarExcel(): void {

    if (
      !this.resumen
    ) {

      this.error =
        'Primero ejecute el análisis de cosechas.';

      return;
    }


    this.exporter
      .exportarAnalisis(
        this.resumen,
        this.indicador
      );
  }


  exportarDetalle(): void {

    if (
      !this.celdaSeleccionada
      ||
      this.detalle.length === 0
    ) {
      return;
    }


    this.exporter
      .exportarDetalle(

        this.detalle,

        this.celdaSeleccionada.cosecha,

        this.celdaSeleccionada.fechaCorte,

        this.indicador

      );
  }


  // =========================================================
  // CLASE CELDA
  // =========================================================

  claseCelda(
    celda: CosechaCelda
  ): string {

    const valor =
      this.valorPrincipal(
        celda
      );


    if (
      this.indicador ===
      'SALDO_REMANENTE'
    ) {

      if (
        valor >= 80
      ) {
        return 'nivel-alto';
      }

      if (
        valor >= 50
      ) {
        return 'nivel-medio';
      }

      if (
        valor > 0
      ) {
        return 'nivel-bajo';
      }

      return 'nivel-cero';
    }


    if (
      valor >= 10
    ) {
      return 'riesgo-alto';
    }

    if (
      valor >= 5
    ) {
      return 'riesgo-medio';
    }

    if (
      valor > 0
    ) {
      return 'riesgo-bajo';
    }

    return 'nivel-cero';
  }


  // =========================================================
  // TEXTOS
  // =========================================================

  get nombreIndicador():
  string {

    return this.indicadores.find(
      item =>
        item.codigo ===
        this.indicador
    )?.nombre
    ?? this.indicador;
  }


  get descripcionIndicador():
  string {

    return this.indicadores.find(
      item =>
        item.codigo ===
        this.indicador
    )?.descripcion
    ?? '';
  }


  // =========================================================
  // FORMATO
  // =========================================================

  formatearNumero(
    valor:
      number
      | null
      | undefined
  ): string {

    return this.numero(
      valor
    ).toLocaleString(
      'es-CO',
      {
        maximumFractionDigits: 0
      }
    );
  }


  formatearMoneda(
    valor:
      number
      | null
      | undefined
  ): string {

    return this.numero(
      valor
    ).toLocaleString(
      'es-CO',
      {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }
    );
  }


  formatearPorcentaje(
    valor:
      number
      | null
      | undefined
  ): string {

    return this.numero(
      valor
    ).toLocaleString(
      'es-CO',
      {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }
    );
  }


  formatearCosecha(
    fecha:
      string
      | null
      | undefined
  ): string {

    if (
      !fecha
    ) {
      return '';
    }

    return fecha.substring(
      0,
      7
    );
  }


  // =========================================================
  // AUXILIARES
  // =========================================================

  private primerDiaMes(
    mes: string
  ): string {

    return `${mes}-01`;
  }


  private restarMeses(
    mes: string,
    cantidad: number
  ): string {

    const partes =
      mes.split(
        '-'
      );

    if (
      partes.length !== 2
    ) {
      return mes;
    }


    const anio =
      Number(
        partes[0]
      );

    const numeroMes =
      Number(
        partes[1]
      );


    const fecha =
      new Date(
        anio,
        numeroMes - 1 - cantidad,
        1
      );


    return [
      fecha.getFullYear(),
      String(
        fecha.getMonth() + 1
      ).padStart(
        2,
        '0'
      )
    ].join(
      '-'
    );
  }


  private normalizarTexto(
    valor:
      string
      | number
      | null
      | undefined
  ): string {

    return String(
      valor
      ?? ''
    )
      .trim()
      .toLocaleLowerCase(
        'es'
      )
      .normalize(
        'NFD'
      )
      .replace(
        /[\u0300-\u036f]/g,
        ''
      );
  }


  private numero(
    valor:
      number
      | string
      | null
      | undefined
  ): number {

    const numero =
      Number(
        valor
        ?? 0
      );

    return Number.isFinite(
      numero
    )
      ? numero
      : 0;
  }


  private obtenerMensajeError(
    error: any,
    mensajeDefecto: string
  ): string {

    return error?.error?.message
      ??
      error?.error?.mensaje
      ??
      error?.message
      ??
      mensajeDefecto;
  }

}
