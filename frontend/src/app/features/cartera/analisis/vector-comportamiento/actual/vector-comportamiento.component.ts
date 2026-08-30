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
} from '../../../../../shared/header-actions/header-actions.component';

import {
  VectorComportamientoApi
} from './vector-comportamiento.api';

import {
  VectorComportamientoDetalle,
  VectorComportamientoResumen
} from './vector-comportamiento.models';

import {
  VectorComportamientoExporterService
} from './vector-comportamiento-exporter.service';

// =========================================================
// FILTRO DE COMPORTAMIENTO
// =========================================================

type FiltroComportamiento =
  | 'TODOS'
  | 'AL_DIA'
  | 'EN_MORA'
  | 'MORA_HISTORICA'
  | 'MORA_MAYOR_90';


@Component({
  selector:
    'app-vector-comportamiento',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],

  templateUrl:
    './vector-comportamiento.component.html',

  styleUrls: [
    './vector-comportamiento.component.scss'
  ]
})
export class VectorComportamientoComponent
implements OnInit {

  // =========================================================
  // DATOS
  // =========================================================

  resumen:
    VectorComportamientoResumen[] = [];

  detalle:
    VectorComportamientoDetalle[] = [];


  // =========================================================
  // CRÉDITO SELECCIONADO
  // =========================================================

  creditoSeleccionado:
    VectorComportamientoResumen | null = null;


  // =========================================================
  // ESTADO
  // =========================================================

  cargando = false;

  cargandoDetalle = false;

  error:
    string | null = null;

  errorDetalle:
    string | null = null;


  // =========================================================
  // FILTROS
  // =========================================================

  filtroDocumento = '';

  filtroNombre = '';

  filtroPagare = '';

  filtroLinea = '';

  filtroComportamiento:
    FiltroComportamiento = 'TODOS';


  // =========================================================
  // PAGINACIÓN
  // =========================================================

  pagina = 1;

  tamanoPagina = 20;


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly api:
      VectorComportamientoApi,

    private readonly exporter:
      VectorComportamientoExporterService
  ) {}


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    this.cargarResumen();

  }


  // =========================================================
  // CARGAR RESUMEN
  // =========================================================

  cargarResumen(): void {

    if (this.cargando) {
      return;
    }

    this.cargando = true;

    this.error = null;

    this.creditoSeleccionado =
      null;

    this.detalle = [];

    this.errorDetalle = null;

    this.api
      .listarResumenCarteraActiva()
      .subscribe({

        next: (
          respuesta:
            VectorComportamientoResumen[]
        ) => {

          this.resumen =
            Array.isArray(
              respuesta
            )
              ? respuesta
              : [];

          this.pagina = 1;

          this.cargando = false;

        },

        error: (
          error: unknown
        ) => {

          console.error(
            'Error consultando Vector de Comportamiento:',
            error
          );

          this.resumen = [];

          this.creditoSeleccionado =
            null;

          this.detalle = [];

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar el Vector de Comportamiento.'
            );

          this.cargando = false;

        }

      });

  }


  // =========================================================
  // SELECCIONAR CRÉDITO
  // =========================================================

  seleccionarCredito(
    credito:
      VectorComportamientoResumen
  ): void {

    const idCarteraCredito =
      Number(
        credito?.idCarteraCredito
      );

    if (
      !Number.isInteger(
        idCarteraCredito
      )
      ||
      idCarteraCredito <= 0
      ||
      this.cargandoDetalle
    ) {
      return;
    }

    this.creditoSeleccionado =
      credito;

    this.detalle = [];

    this.errorDetalle = null;

    /*
     * Un crédito actual puede formar parte del Vector
     * aunque todavía no tenga cortes históricos.
     *
     * En ese caso no hacemos una consulta innecesaria
     * de detalle.
     */
    if (
      credito.tieneHistoria
      !== true
    ) {

      this.cargandoDetalle = false;

      return;
    }

    this.cargandoDetalle = true;

    this.api
      .listarDetallePorCredito(
        idCarteraCredito
      )
      .subscribe({

        next: (
          respuesta:
            VectorComportamientoDetalle[]
        ) => {

          this.detalle =
            Array.isArray(
              respuesta
            )
              ? respuesta
              : [];

          this.cargandoDetalle = false;

        },

        error: (
          error: unknown
        ) => {

          console.error(
            'Error consultando detalle del Vector de Comportamiento:',
            error
          );

          this.detalle = [];

          this.errorDetalle =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar el histórico del crédito.'
            );

          this.cargandoDetalle = false;

        }

      });

  }


  // =========================================================
  // CERRAR DETALLE
  // =========================================================

  cerrarDetalle(): void {

    if (this.cargandoDetalle) {
      return;
    }

    this.creditoSeleccionado =
      null;

    this.detalle = [];

    this.errorDetalle = null;

  }


  // =========================================================
  // LIMPIAR FILTROS
  // =========================================================

  limpiarFiltros(): void {

    this.filtroDocumento = '';

    this.filtroNombre = '';

    this.filtroPagare = '';

    this.filtroLinea = '';

    this.filtroComportamiento =
      'TODOS';

    this.pagina = 1;

  }


  // =========================================================
  // APLICAR FILTROS
  // =========================================================

  aplicarFiltros(): void {

    this.pagina = 1;

  }

  // =========================================================
  // EXPORTAR EXCEL
  // =========================================================

  exportarExcel(): void {

    if (
      this.cargando
    ) {
      return;
    }

    if (
      !this.resumen
      ||
      this.resumen.length === 0
    ) {

      alert(
        'No hay información del Vector de Comportamiento para exportar.'
      );

      return;
    }

    this.exporter.exportar(
      this.resumen
    );

  }


  // =========================================================
  // RESUMEN FILTRADO
  // =========================================================

  get resumenFiltrado():
  VectorComportamientoResumen[] {

    const documento =
      this.normalizarTexto(
        this.filtroDocumento
      );

    const nombre =
      this.normalizarTexto(
        this.filtroNombre
      );

    const pagare =
      this.normalizarTexto(
        this.filtroPagare
      );

    const linea =
      this.normalizarTexto(
        this.filtroLinea
      );

    return this.resumen.filter(
      credito => {

        if (
          documento
          &&
          !this.normalizarTexto(
            credito.documento
          ).includes(
            documento
          )
        ) {
          return false;
        }

        if (
          nombre
          &&
          !this.normalizarTexto(
            credito.nombreCompleto
          ).includes(
            nombre
          )
        ) {
          return false;
        }

        if (
          pagare
          &&
          !this.normalizarTexto(
            credito.pagareCartera
          ).includes(
            pagare
          )
        ) {
          return false;
        }

        if (
          linea
          &&
          !(
            this.normalizarTexto(
              credito.codigoLineaCredito
            ).includes(
              linea
            )
            ||
            this.normalizarTexto(
              credito.nombreLineaCredito
            ).includes(
              linea
            )
          )
        ) {
          return false;
        }

        switch (
          this.filtroComportamiento
        ) {

          case 'AL_DIA':

            return (
              credito.tieneHistoria
              === true
              &&
              Number(
                credito.moraUltimoCorte
                ?? 0
              ) === 0
            );


          case 'EN_MORA':

            return (
              credito.tieneHistoria
              === true
              &&
              Number(
                credito.moraUltimoCorte
                ?? 0
              ) > 0
            );


          case 'MORA_HISTORICA':

            return (
              credito.tuvoMoraPeriodo
              === true
            );


          case 'MORA_MAYOR_90':

             return (
               credito.tieneHistoria
               === true
               &&
               Number(
                 credito.moraMaxima
                 ?? 0
               ) > 90
             );


          default:

            return true;

        }

      }
    );

  }


  // =========================================================
  // PAGINACIÓN
  // =========================================================

  get totalPaginas(): number {

    const total =
      this.resumenFiltrado.length;

    if (total <= 0) {
      return 1;
    }

    return Math.ceil(
      total
      /
      this.tamanoPagina
    );

  }


  get creditosPagina():
  VectorComportamientoResumen[] {

    const inicio =
      (
        this.pagina - 1
      )
      *
      this.tamanoPagina;

    const fin =
      inicio
      +
      this.tamanoPagina;

    return this.resumenFiltrado.slice(
      inicio,
      fin
    );

  }


  get registroInicialPagina(): number {

    if (
      this.resumenFiltrado.length === 0
    ) {
      return 0;
    }

    return (
      (
        this.pagina - 1
      )
      *
      this.tamanoPagina
    )
      + 1;

  }


  get registroFinalPagina(): number {

    return Math.min(
      this.pagina
      *
      this.tamanoPagina,
      this.resumenFiltrado.length
    );

  }


  paginaAnterior(): void {

    if (
      this.pagina <= 1
    ) {
      return;
    }

    this.pagina--;

  }


  paginaSiguiente(): void {

    if (
      this.pagina
      >=
      this.totalPaginas
    ) {
      return;
    }

    this.pagina++;

  }


  irPagina(
    pagina: number
  ): void {

    if (
      !Number.isInteger(
        pagina
      )
      ||
      pagina < 1
      ||
      pagina > this.totalPaginas
    ) {
      return;
    }

    this.pagina =
      pagina;

  }


  // =========================================================
  // INDICADORES GENERALES - COMPORTAMIENTO
  // =========================================================

  get cantidadCreditos(): number {

    return this.resumen.length;

  }


  get cantidadCreditosFiltrados(): number {

    return this.resumenFiltrado.length;

  }


  get cantidadConHistoria(): number {

    return this.resumen.filter(
      credito =>
        credito.tieneHistoria
        === true
    ).length;

  }


  get cantidadSinHistoria(): number {

    return (
      this.cantidadCreditos
      -
      this.cantidadConHistoria
    );

  }


  get totalCortesObservados(): number {

    return this.resumen.reduce(
      (
        total,
        credito
      ) =>
        total
        +
        Number(
          credito.cantidadCortesObservados
          ?? 0
        ),
      0
    );

  }


  get totalCortesAlDia(): number {

    return this.resumen.reduce(
      (
        total,
        credito
      ) =>
        total
        +
        Number(
          credito.cantidadCortesAlDia
          ?? 0
        ),
      0
    );

  }


  get totalCortesConMora(): number {

    return this.resumen.reduce(
      (
        total,
        credito
      ) =>
        total
        +
        Number(
          credito.cantidadCortesConMora
          ?? 0
        ),
      0
    );

  }


  get porcentajeCortesAlDia(): number {

    if (
      this.totalCortesObservados <= 0
    ) {
      return 0;
    }

    return (
      this.totalCortesAlDia
      /
      this.totalCortesObservados
      *
      100
    );

  }


  get pbbMoraCartera(): number {

    if (
      this.totalCortesObservados <= 0
    ) {
      return 0;
    }

    return (
      this.totalCortesConMora
      /
      this.totalCortesObservados
      *
      100
    );

  }


  get cantidadAlDiaActual(): number {

    return this.resumen.filter(
      credito =>
        credito.tieneHistoria
        === true
        &&
        Number(
          credito.moraUltimoCorte
          ?? 0
        ) === 0
    ).length;

  }


  get cantidadEnMoraActual(): number {

    return this.resumen.filter(
      credito =>
        credito.tieneHistoria
        === true
        &&
        Number(
          credito.moraUltimoCorte
          ?? 0
        ) > 0
    ).length;

  }


  get cantidadConMoraHistorica(): number {

    return this.resumen.filter(
      credito =>
        credito.tuvoMoraPeriodo
        === true
    ).length;

  }


  get cantidadSinMoraHistorica(): number {

    return this.resumen.filter(
      credito =>
        credito.tieneHistoria
        === true
        &&
        credito.tuvoMoraPeriodo
        !== true
    ).length;

  }


  get cantidadConMoraMayor90(): number {

    return this.resumen.filter(
      credito =>
        credito.tieneHistoria
        === true
        &&
        Number(
          credito.moraMaxima
          ?? 0
        ) > 90
    ).length;

  }


  get saldoTotalActual(): number {

    return this.resumen.reduce(
      (
        total,
        credito
      ) =>
        total
        +
        Number(
          credito.saldoActualMaestro
          ?? 0
        ),
      0
    );

  }


  get saldoTotalUltimoCorte(): number {

    return this.resumen.reduce(
      (
        total,
        credito
      ) =>
        total
        +
        Number(
          credito.saldoUltimoCorte
          ?? 0
        ),
      0
    );

  }


  /*
   * Promedio individual de PBB.
   *
   * Se conserva porque puede seguir siendo útil
   * en el detalle o en otras presentaciones.
   *
   * Para el encabezado general utilizaremos
   * pbbMoraCartera, calculado sobre todos los cortes.
   */
  get pbbMoraPromedio(): number {

    const creditosConHistoria =
      this.resumen.filter(
        credito =>
          credito.tieneHistoria
          === true
          &&
          Number(
            credito.cantidadCortesObservados
            ?? 0
          ) > 0
      );

    if (
      creditosConHistoria.length === 0
    ) {
      return 0;
    }

    const total =
      creditosConHistoria.reduce(
        (
          acumulado,
          credito
        ) =>
          acumulado
          +
          Number(
            credito.pbbMora
            ?? 0
          ),
        0
      );

    return (
      total
      /
      creditosConHistoria.length
    );

  }


  get moraMaximaCartera(): number {

    const creditosConHistoria =
      this.resumen.filter(
        credito =>
          credito.tieneHistoria
          === true
      );

    if (
      creditosConHistoria.length === 0
    ) {
      return 0;
    }

    return Math.max(
      ...creditosConHistoria.map(
        credito =>
          Number(
            credito.moraMaxima
            ?? 0
          )
      )
    );

  }


  // =========================================================
  // INDICADORES GENERALES - SEVERIDAD
  // =========================================================

  get cantidadSeveridad0_10(): number {

    return this.cantidadPorRangoSeveridad(
      'SEVERIDAD 0-10'
    );

  }


  get cantidadSeveridad10_25(): number {

    return this.cantidadPorRangoSeveridad(
      'SEVERIDAD 10-25'
    );

  }


  get cantidadSeveridad25_50(): number {

    return this.cantidadPorRangoSeveridad(
      'SEVERIDAD 25-50'
    );

  }


  get cantidadSeveridad50_75(): number {

    return this.cantidadPorRangoSeveridad(
      'SEVERIDAD 50-75'
    );

  }


  get cantidadSeveridad75_100(): number {

    return this.cantidadPorRangoSeveridad(
      'SEVERIDAD 75-100'
    );

  }


  get porcentajeSeveridad0_10(): number {

    return this.porcentajePorRangoSeveridad(
      'SEVERIDAD 0-10'
    );

  }


  get porcentajeSeveridad10_25(): number {

    return this.porcentajePorRangoSeveridad(
      'SEVERIDAD 10-25'
    );

  }


  get porcentajeSeveridad25_50(): number {

    return this.porcentajePorRangoSeveridad(
      'SEVERIDAD 25-50'
    );

  }


  get porcentajeSeveridad50_75(): number {

    return this.porcentajePorRangoSeveridad(
      'SEVERIDAD 50-75'
    );

  }


  get porcentajeSeveridad75_100(): number {

    return this.porcentajePorRangoSeveridad(
      'SEVERIDAD 75-100'
    );

  }


  get saldoSeveridad0_10(): number {

    return this.saldoPorRangoSeveridad(
      'SEVERIDAD 0-10'
    );

  }


  get saldoSeveridad10_25(): number {

    return this.saldoPorRangoSeveridad(
      'SEVERIDAD 10-25'
    );

  }


  get saldoSeveridad25_50(): number {

    return this.saldoPorRangoSeveridad(
      'SEVERIDAD 25-50'
    );

  }


  get saldoSeveridad50_75(): number {

    return this.saldoPorRangoSeveridad(
      'SEVERIDAD 50-75'
    );

  }


  get saldoSeveridad75_100(): number {

    return this.saldoPorRangoSeveridad(
      'SEVERIDAD 75-100'
    );

  }


  // =========================================================
  // INDICADORES DEL CRÉDITO SELECCIONADO
  // =========================================================

  get tieneCreditoSeleccionado(): boolean {

    return (
      this.creditoSeleccionado
      !== null
    );

  }


  get tieneDetalle(): boolean {

    return (
      this.detalle.length > 0
    );

  }


  get cantidadCortesDetalle(): number {

    return this.detalle.length;

  }


  get cantidadCortesDetalleAlDia(): number {

    return this.detalle.filter(
      corte =>
        Number(
          corte.diasMora
          ?? 0
        ) === 0
    ).length;

  }


  get cantidadCortesDetalleConMora(): number {

    return this.detalle.filter(
      corte =>
        Number(
          corte.diasMora
          ?? 0
        ) > 0
    ).length;

  }


  get moraMaximaDetalle(): number {

    if (
      this.detalle.length === 0
    ) {
      return 0;
    }

    return Math.max(
      ...this.detalle.map(
        corte =>
          Number(
            corte.diasMora
            ?? 0
          )
      )
    );

  }


  // =========================================================
  // ÚLTIMO CORTE DEL DETALLE
  // =========================================================

  get ultimoDetalle():
  VectorComportamientoDetalle | null {

    if (
      this.detalle.length === 0
    ) {
      return null;
    }

    return this.detalle[
      this.detalle.length - 1
    ];

  }


  // =========================================================
  // SELECCIÓN VISUAL
  // =========================================================

  esCreditoSeleccionado(
    credito:
      VectorComportamientoResumen
  ): boolean {

    return (
      this.creditoSeleccionado
        ?.idCarteraCredito
      ===
      credito.idCarteraCredito
    );

  }


  // =========================================================
  // CLASIFICACIÓN VISUAL DE MORA
  // =========================================================

  claseMora(
    diasMora:
      number
      | null
      | undefined
  ): string {

    const dias =
      Number(
        diasMora
        ?? 0
      );

    if (dias <= 0) {
      return 'mora mora--al-dia';
    }

    if (dias <= 30) {
      return 'mora mora--1-30';
    }

    if (dias <= 60) {
      return 'mora mora--31-60';
    }

    if (dias <= 90) {
      return 'mora mora--61-90';
    }

    return 'mora mora--mayor-90';

  }


  // =========================================================
  // CLASIFICACIÓN VISUAL DE EDAD
  // =========================================================

  claseEdad(
    edad:
      string
      | null
      | undefined
  ): string {

    switch (
      this.normalizarTexto(
        edad
      )
    ) {

      case 'A':
        return 'edad edad--a';

      case 'B':
        return 'edad edad--b';

      case 'C':
        return 'edad edad--c';

      case 'D':
        return 'edad edad--d';

      case 'E':
        return 'edad edad--e';

      default:
        return 'edad';

    }

  }


  // =========================================================
  // TRACK BY
  // =========================================================

  trackByCredito(
    indice: number,
    credito:
      VectorComportamientoResumen
  ): number {

    return (
      credito.idCarteraCredito
      ?? indice
    );

  }


  trackByDetalle(
    indice: number,
    detalle:
      VectorComportamientoDetalle
  ): number {

    return (
      detalle.idCierreCarteraCredito
      ?? indice
    );

  }


  // =========================================================
  // UTILIDADES DE SEVERIDAD
  // =========================================================

  private cantidadPorRangoSeveridad(
    rango: string
  ): number {

    return this.resumen.filter(
      credito =>
        credito.rangoSeveridad
        === rango
    ).length;

  }


  private porcentajePorRangoSeveridad(
    rango: string
  ): number {

    if (
      this.cantidadCreditos <= 0
    ) {
      return 0;
    }

    return (
      this.cantidadPorRangoSeveridad(
        rango
      )
      /
      this.cantidadCreditos
      *
      100
    );

  }


  private saldoPorRangoSeveridad(
    rango: string
  ): number {

    return this.resumen
      .filter(
        credito =>
          credito.rangoSeveridad
          === rango
      )
      .reduce(
        (
          total,
          credito
        ) =>
          total
          +
          Number(
            credito.saldoActualMaestro
            ?? 0
          ),
        0
      );

  }


  // =========================================================
  // UTILIDADES
  // =========================================================

  private normalizarTexto(
    valor:
      | string
      | number
      | null
      | undefined
  ): string {

    return String(
      valor
      ?? ''
    )
      .trim()
      .replace(
        /\s+/g,
        ' '
      )
      .toUpperCase();

  }


  private obtenerMensajeError(
    error: unknown,
    mensajeDefault: string
  ): string {

    if (
      error
      &&
      typeof error === 'object'
    ) {

      const posibleError =
        error as {
          error?: {
            message?: string;
            mensaje?: string;
            error?: string;
          } | string;
          message?: string;
        };

      if (
        typeof posibleError.error
        === 'string'
        &&
        posibleError.error.trim()
      ) {

        return posibleError.error.trim();

      }

      if (
        posibleError.error
        &&
        typeof posibleError.error
        === 'object'
      ) {

        const mensajeBackend =
          posibleError.error.message
          ??
          posibleError.error.mensaje
          ??
          posibleError.error.error;

        if (
          mensajeBackend
          &&
          String(
            mensajeBackend
          ).trim()
        ) {

          return String(
            mensajeBackend
          ).trim();

        }

      }

      if (
        posibleError.message
        &&
        posibleError.message.trim()
      ) {

        return posibleError.message.trim();

      }

    }

    return mensajeDefault;

  }
}
