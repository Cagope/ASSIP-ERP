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
  VectorComportamientoCorteApi
} from './vector-comportamiento-corte.api';

import {
  VectorComportamientoCorteDetalle,
  VectorComportamientoCorteResumen
} from './vector-comportamiento-corte.models';

import {
  VectorComportamientoCorteExporterService
} from './vector-comportamiento-corte-exporter.service';

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
    'app-vector-comportamiento-corte',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],

  templateUrl:
    './vector-comportamiento-corte.component.html',

  styleUrls: [
    './vector-comportamiento-corte.component.scss'
  ]
})
export class VectorComportamientoCorteComponent
implements OnInit {

  // =========================================================
  // FECHAS DISPONIBLES
  // =========================================================

  fechasCorte:
    string[] = [];

  fechaCorteSeleccionada = '';


  // =========================================================
  // DATOS
  // =========================================================

  resumen:
    VectorComportamientoCorteResumen[] = [];

  detalle:
    VectorComportamientoCorteDetalle[] = [];


  // =========================================================
  // CRÉDITO SELECCIONADO
  // =========================================================

  creditoSeleccionado:
    VectorComportamientoCorteResumen | null = null;


  // =========================================================
  // ESTADO
  // =========================================================

  cargandoFechas = false;

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
      VectorComportamientoCorteApi,

    private readonly exporter:
      VectorComportamientoCorteExporterService
  ) {}


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    this.cargarFechasCorte();

  }


  // =========================================================
  // CARGAR FECHAS
  // =========================================================

  cargarFechasCorte(): void {

    this.cargandoFechas = true;

    this.error = null;

    this.api
      .listarFechasCorteDisponibles()
      .subscribe({

        next: (
          fechas:
            string[]
        ) => {

          this.fechasCorte =
            Array.isArray(
              fechas
            )
              ? fechas
              : [];

          this.fechaCorteSeleccionada =
            '';

          this.resumen = [];

          this.creditoSeleccionado =
            null;

          this.detalle = [];

          this.errorDetalle =
            null;

          this.pagina = 1;

          this.cargandoFechas =
            false;

        },

        error: (
          error: unknown
        ) => {

          console.error(
            'Error consultando fechas disponibles del Vector de Comportamiento por Corte:',
            error
          );

          this.fechasCorte = [];

          this.fechaCorteSeleccionada =
            '';

          this.resumen = [];

          this.creditoSeleccionado =
            null;

          this.detalle = [];

          this.errorDetalle =
            null;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar las fechas de corte disponibles.'
            );

          this.cargandoFechas =
            false;

        }

      });

  }


  // =========================================================
  // CAMBIAR FECHA DE CORTE
  // =========================================================

  cambiarFechaCorte(): void {

    if (
      !this.fechaCorteSeleccionada
      ||
      this.cargando
      ||
      this.cargandoFechas
    ) {
      return;
    }

    this.pagina = 1;

    this.creditoSeleccionado =
      null;

    this.detalle = [];

    this.errorDetalle =
      null;

    this.cargarResumen();

  }


  // =========================================================
  // CARGAR RESUMEN
  // =========================================================

  cargarResumen(): void {

    const fechaCorte =
      this.fechaCorteSeleccionada;

    if (
      !fechaCorte
      ||
      this.cargando
    ) {
      return;
    }

    this.cargando = true;

    this.error = null;

    this.creditoSeleccionado =
      null;

    this.detalle = [];

    this.errorDetalle =
      null;

    this.api
      .listarResumenPorCorte(
        fechaCorte
      )
      .subscribe({

        next: (
          respuesta:
            VectorComportamientoCorteResumen[]
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
            'Error consultando Vector de Comportamiento por Corte:',
            error
          );

          this.resumen = [];

          this.creditoSeleccionado =
            null;

          this.detalle = [];

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar el Vector de Comportamiento para el corte seleccionado.'
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
      VectorComportamientoCorteResumen
  ): void {

    const idCarteraCredito =
      Number(
        credito?.idCarteraCredito
      );

    const fechaCorte =
      this.fechaCorteSeleccionada;

    if (
      !Number.isInteger(
        idCarteraCredito
      )
      ||
      idCarteraCredito <= 0
      ||
      !fechaCorte
      ||
      this.cargandoDetalle
    ) {
      return;
    }

    this.creditoSeleccionado =
      credito;

    this.detalle = [];

    this.errorDetalle =
      null;

    this.cargandoDetalle =
      true;

    /*
     * IMPORTANTE:
     *
     * En Vector por Corte siempre consultamos el detalle.
     *
     * La posición 1 corresponde al corte seleccionado
     * tratado conceptualmente como ACTUAL.
     *
     * Por eso incluso un crédito sin historia anterior
     * debe devolver su posición 1.
     */
    this.api
      .listarDetallePorCredito(
        idCarteraCredito,
        fechaCorte
      )
      .subscribe({

        next: (
          respuesta:
            VectorComportamientoCorteDetalle[]
        ) => {

          this.detalle =
            Array.isArray(
              respuesta
            )
              ? respuesta
              : [];

          this.cargandoDetalle =
            false;

        },

        error: (
          error: unknown
        ) => {

          console.error(
            'Error consultando detalle del Vector de Comportamiento por Corte:',
            error
          );

          this.detalle = [];

          this.errorDetalle =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar el Vector de Comportamiento del crédito.'
            );

          this.cargandoDetalle =
            false;

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

    this.errorDetalle =
      null;

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
      ||
      this.cargandoFechas
    ) {
      return;
    }

    if (
      !this.fechaCorteSeleccionada
    ) {

      alert(
        'Debe seleccionar una fecha de corte.'
      );

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
      this.resumen,
      this.fechaCorteSeleccionada
    );

  }

  // =========================================================
  // RESUMEN FILTRADO
  // =========================================================

  get resumenFiltrado():
  VectorComportamientoCorteResumen[] {

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

        // =====================================================
        // DOCUMENTO
        // =====================================================

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


        // =====================================================
        // NOMBRE
        // =====================================================

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


        // =====================================================
        // PAGARÉ
        // =====================================================

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


        // =====================================================
        // LÍNEA
        // =====================================================

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


        // =====================================================
        // COMPORTAMIENTO
        // =====================================================

        switch (
          this.filtroComportamiento
        ) {

          case 'AL_DIA':

            return (
              Number(
                credito.moraUltimoCorte
                ?? 0
              ) === 0
            );


          case 'EN_MORA':

            return (
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
              credito.tuvoMoraMayor90
              === true
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
  VectorComportamientoCorteResumen[] {

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
  //
  // MISMA FILOSOFÍA DEL VECTOR ACTUAL.
  //
  // Diferencia:
  //
  // Actual:
  //   población = maestro con saldo actual > 0.
  //
  // Corte:
  //   población = créditos con saldo > 0
  //   en la fecha seleccionada.
  //
  // Los indicadores históricos se calculan exclusivamente
  // sobre los cierres anteriores observados.
  // =========================================================

  get cantidadCreditos(): number {

    return this.resumen.length;

  }


  get cantidadCreditosFiltrados(): number {

    return this.resumenFiltrado.length;

  }


  // =========================================================
  // CRÉDITOS CON / SIN HISTORIA ANTERIOR
  // =========================================================

  get cantidadConHistoria(): number {

    return this.resumen.filter(
      credito =>
        Number(
          credito.cantidadCortesObservados
          ?? 0
        ) > 0
    ).length;

  }


  get cantidadSinHistoria(): number {

    return (
      this.cantidadCreditos
      -
      this.cantidadConHistoria
    );

  }


  // =========================================================
  // TOTAL CORTES HISTÓRICOS OBSERVADOS
  //
  // NO incluye la posición 1 del corte seleccionado.
  // =========================================================

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


  // =========================================================
  // TOTAL CORTES AL DÍA
  //
  // Solo cierres históricos anteriores.
  // =========================================================

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


  // =========================================================
  // TOTAL CORTES CON MORA
  //
  // Solo cierres históricos anteriores.
  // =========================================================

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


  // =========================================================
  // PORCENTAJE DE CORTES AL DÍA
  // =========================================================

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


  // =========================================================
  // PBB MORA DE LA CARTERA
  //
  // MISMA REGLA DEL VECTOR ACTUAL:
  //
  // total cortes con mora
  // ---------------------- x 100
  // total cortes observados
  //
  // NO es promedio simple de PBB por crédito.
  // =========================================================

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


  // =========================================================
  // ESTADO EN EL CORTE SELECCIONADO
  //
  // Son indicadores auxiliares.
  // No sustituyen el análisis histórico del Vector.
  // =========================================================

  get cantidadAlDiaCorte(): number {

    return this.resumen.filter(
      credito =>
        Number(
          credito.moraUltimoCorte
          ?? 0
        ) === 0
    ).length;

  }


  get cantidadEnMoraCorte(): number {

    return this.resumen.filter(
      credito =>
        Number(
          credito.moraUltimoCorte
          ?? 0
        ) > 0
    ).length;

  }


  // =========================================================
  // CRÉDITOS CON MORA HISTÓRICA
  // =========================================================

  get cantidadConMoraHistorica(): number {

    return this.resumen.filter(
      credito =>
        credito.tuvoMoraPeriodo
        === true
    ).length;

  }


  // =========================================================
  // CRÉDITOS SIN MORA HISTÓRICA
  //
  // Solo créditos que sí tienen historia anterior.
  // =========================================================

  get cantidadSinMoraHistorica(): number {

    return this.resumen.filter(
      credito =>
        Number(
          credito.cantidadCortesObservados
          ?? 0
        ) > 0
        &&
        credito.tuvoMoraPeriodo
        !== true
    ).length;

  }


  // =========================================================
  // MORA HISTÓRICA MAYOR A 90 DÍAS
  // =========================================================

  get cantidadConMoraMayor90(): number {

    return this.resumen.filter(
      credito =>
        credito.tuvoMoraMayor90
        === true
    ).length;

  }


  // =========================================================
  // SALDO TOTAL DEL CORTE
  //
  // Equivalente funcional de saldoTotalActual
  // en Vector Actual.
  // =========================================================

  get saldoTotalCorte(): number {

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


  // =========================================================
  // PBB PROMEDIO INDIVIDUAL
  //
  // Se conserva como indicador auxiliar.
  //
  // El encabezado principal debe usar pbbMoraCartera.
  // =========================================================

  get pbbMoraPromedio(): number {

    const creditosConHistoria =
      this.resumen.filter(
        credito =>
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


  // =========================================================
  // MORA MÁXIMA HISTÓRICA DE LA CARTERA
  //
  // Solo se consideran créditos con cierres anteriores.
  // =========================================================

  get moraMaximaCartera(): number {

    const creditosConHistoria =
      this.resumen.filter(
        credito =>
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
  //
  // MISMA FILOSOFÍA DEL VECTOR ACTUAL.
  //
  // La diferencia de fuente es:
  //
  // Actual:
  //   saldoActualMaestro
  //
  // Corte:
  //   saldoUltimoCorte
  //
  // El rango de severidad ya viene calculado por backend
  // con el saldo del corte frente al valor desembolsado.
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
  // DETALLE - POSICIONES DEL VECTOR
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


  // =========================================================
  // POSICIÓN 1 - CORTE SELECCIONADO
  // =========================================================

  get detalleReferencia():
  VectorComportamientoCorteDetalle | null {

    return (
      this.detalle.find(
        item =>
          this.esPosicionActual(
            item
          )
      )
      ?? null
    );

  }


  // =========================================================
  // HISTORIA ANTERIOR
  //
  // Excluye la posición 1 ACTUAL.
  // =========================================================

  get detalleHistorico():
  VectorComportamientoCorteDetalle[] {

    return this.detalle.filter(
      item =>
        !this.esPosicionActual(
          item
        )
    );

  }


  // =========================================================
  // CANTIDAD DE CORTES HISTÓRICOS
  // =========================================================

  get cantidadCortesDetalle(): number {

    return this.detalleHistorico.length;

  }


  // =========================================================
  // CORTES HISTÓRICOS AL DÍA
  // =========================================================

  get cantidadCortesDetalleAlDia(): number {

    return this.detalleHistorico.filter(
      corte =>
        Number(
          corte.diasMora
          ?? 0
        ) === 0
    ).length;

  }


  // =========================================================
  // CORTES HISTÓRICOS CON MORA
  // =========================================================

  get cantidadCortesDetalleConMora(): number {

    return this.detalleHistorico.filter(
      corte =>
        Number(
          corte.diasMora
          ?? 0
        ) > 0
    ).length;

  }


  // =========================================================
  // MORA MÁXIMA HISTÓRICA DEL DETALLE
  // =========================================================

  get moraMaximaDetalle(): number {

    if (
      this.detalleHistorico.length === 0
    ) {
      return 0;
    }

    return Math.max(
      ...this.detalleHistorico.map(
        corte =>
          Number(
            corte.diasMora
            ?? 0
          )
      )
    );

  }


  // =========================================================
  // ÚLTIMA POSICIÓN DEL VECTOR
  //
  // Es la posición histórica más antigua disponible.
  // =========================================================

  get ultimoDetalle():
  VectorComportamientoCorteDetalle | null {

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
      VectorComportamientoCorteResumen
  ): boolean {

    return (
      this.creditoSeleccionado
        ?.idCarteraCredito
      ===
      credito.idCarteraCredito
    );

  }


  // =========================================================
  // IDENTIFICAR POSICIÓN ACTUAL DEL VECTOR
  //
  // En Vector por Corte:
  //
  // ACTUAL = corte seleccionado.
  // =========================================================

  esPosicionActual(
    detalle:
      VectorComportamientoCorteDetalle
  ): boolean {

    return (
      Number(
        detalle.posicionVector
        ?? 0
      ) === 1
      ||
      this.normalizarTexto(
        detalle.tipoPosicion
      ) === 'ACTUAL'
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

    if (dias <= 120) {
      return 'mora mora--91-120';
    }

    if (dias <= 150) {
      return 'mora mora--121-150';
    }

    if (dias <= 180) {
      return 'mora mora--151-180';
    }

    if (dias <= 360) {
      return 'mora mora--181-360';
    }

    return 'mora mora--mayor-360';

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
      VectorComportamientoCorteResumen
  ): number {

    return (
      credito.idCarteraCredito
      ?? indice
    );

  }


  trackByDetalle(
    indice: number,
    detalle:
      VectorComportamientoCorteDetalle
  ): number {

    const posicion =
      Number(
        detalle.posicionVector
        ?? 0
      );

    if (posicion > 0) {
      return posicion;
    }

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
        this.normalizarTexto(
          credito.rangoSeveridad
        )
        ===
        rango
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
          this.normalizarTexto(
            credito.rangoSeveridad
          )
          ===
          rango
      )
      .reduce(
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
