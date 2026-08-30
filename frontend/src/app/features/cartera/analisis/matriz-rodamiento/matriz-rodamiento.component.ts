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
  MatrizRodamientoApi
} from './matriz-rodamiento.api';

import {
  MatrizRodamientoExporterService
} from './matriz-rodamiento-exporter.service';

import {
  MATRIZ_RODAMIENTO_CATEGORIAS,
  MatrizRodamiento,
  MatrizRodamientoCategoria,
  MatrizRodamientoCelda,
  MatrizRodamientoCorte,
  MatrizRodamientoDetalle,
  MatrizRodamientoTipoPartida
} from './matriz-rodamiento.models';


@Component({
  selector:
    'app-matriz-rodamiento',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],

  templateUrl:
    './matriz-rodamiento.component.html',

  styleUrls: [
    './matriz-rodamiento.component.scss'
  ]
})
export class MatrizRodamientoComponent
implements OnInit {

  readonly categorias =
    MATRIZ_RODAMIENTO_CATEGORIAS;


  // =========================================================
  // PARÁMETROS
  // =========================================================

  tipoPartida:
    MatrizRodamientoTipoPartida =
      'ACTUAL';

  fechaPartida = '';

  fechaComparacion = '';


  // =========================================================
  // DATOS
  // =========================================================

  cortes:
    MatrizRodamientoCorte[] = [];

  matriz:
    MatrizRodamiento | null = null;

  detalle:
    MatrizRodamientoDetalle[] = [];


  // =========================================================
  // CELDA SELECCIONADA
  // =========================================================

  categoriaAnteriorSeleccionada:
    MatrizRodamientoCategoria | null =
      null;

  categoriaPartidaSeleccionada:
    MatrizRodamientoCategoria | null =
      null;


  // =========================================================
  // ESTADO
  // =========================================================

  cargandoCortes = false;

  cargandoMatriz = false;

  cargandoDetalle = false;

  error:
    string | null = null;

  errorDetalle:
    string | null = null;


  // =========================================================
  // FILTRO DETALLE
  // =========================================================

  filtroDetalle = '';

  paginaDetalle = 1;

  tamanoPaginaDetalle = 20;


  constructor(
    private readonly api:
      MatrizRodamientoApi,

    private readonly exporter:
      MatrizRodamientoExporterService
  ) {}


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    this.cargarCortes();

  }


  // =========================================================
  // CORTES
  // =========================================================

  cargarCortes(): void {

    this.cargandoCortes = true;

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

          this.cargandoCortes = false;

        },

        error: error => {

          console.error(
            'Error consultando cortes para Matriz de Rodamiento:',
            error
          );

          this.cortes = [];

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar los cortes disponibles.'
            );

          this.cargandoCortes = false;

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


    if (
      !this.fechaComparacion
    ) {

      this.fechaComparacion =
        fechas[0]
        ?? '';

    }


    if (
      !this.fechaPartida
    ) {

      this.fechaPartida =
        fechas[0]
        ?? '';

    }

  }


  // =========================================================
  // CAMBIO TIPO PARTIDA
  // =========================================================

  cambiarTipoPartida(): void {

    this.matriz = null;

    this.cerrarDetalle();

    this.error = null;

  }


  // =========================================================
  // CALCULAR
  // =========================================================

  calcular(): void {

    if (
      this.cargandoMatriz
    ) {
      return;
    }


    if (
      !this.fechaComparacion
    ) {

      this.error =
        'Seleccione el período de comparación.';

      return;
    }


    if (
      this.tipoPartida === 'CORTE'
      &&
      !this.fechaPartida
    ) {

      this.error =
        'Seleccione el corte de los datos de partida.';

      return;
    }


    if (
      this.tipoPartida === 'CORTE'
      &&
      this.fechaPartida
      <=
      this.fechaComparacion
    ) {

      this.error =
        'El corte de los datos de partida debe ser posterior al período de comparación.';

      return;
    }


    this.cargandoMatriz = true;

    this.error = null;

    this.matriz = null;

    this.cerrarDetalle();


    this.api
      .calcular({

        tipoPartida:
          this.tipoPartida,

        fechaPartida:
          this.tipoPartida === 'CORTE'
            ? this.fechaPartida
            : null,

        fechaComparacion:
          this.fechaComparacion

      })
      .subscribe({

        next: respuesta => {

          this.matriz =
            respuesta;

          this.cargandoMatriz =
            false;

        },

        error: error => {

          console.error(
            'Error calculando Matriz de Rodamiento:',
            error
          );

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible calcular la Matriz de Rodamiento.'
            );

          this.cargandoMatriz =
            false;

        }

      });

  }

  seleccionarCeldaPorCategorias(
    categoriaAnterior: MatrizRodamientoCategoria,
    categoriaPartida: MatrizRodamientoCategoria
  ): void {

    const celda =
      this.obtenerCelda(
        categoriaAnterior,
        categoriaPartida
      );

    if (!celda) {
      return;
    }

    this.seleccionarCelda(celda);
  }

  // =========================================================
  // CELDA
  // =========================================================

  seleccionarCelda(
    celda:
      MatrizRodamientoCelda
  ): void {

    if (
      !this.matriz
      ||
      this.cargandoDetalle
    ) {
      return;
    }


    this.categoriaAnteriorSeleccionada =
      celda.categoriaAnterior;

    this.categoriaPartidaSeleccionada =
      celda.categoriaPartida;

    this.detalle = [];

    this.filtroDetalle = '';

    this.paginaDetalle = 1;

    this.errorDetalle = null;


    if (
      this.numero(
        celda.cantidad
      ) === 0
    ) {
      return;
    }


    this.cargandoDetalle = true;


    this.api
      .listarDetalle({

        tipoPartida:
          this.matriz.tipoPartida,

        fechaPartida:
          this.matriz.tipoPartida
          === 'CORTE'
            ? this.matriz.fechaPartida
            : null,

        fechaComparacion:
          this.matriz.fechaComparacion,

        categoriaAnterior:
          celda.categoriaAnterior,

        categoriaPartida:
          celda.categoriaPartida

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
            'Error consultando detalle de la Matriz de Rodamiento:',
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


  // =========================================================
  // CERRAR DETALLE
  // =========================================================

  cerrarDetalle(): void {

    if (
      this.cargandoDetalle
    ) {
      return;
    }

    this.detalle = [];

    this.categoriaAnteriorSeleccionada =
      null;

    this.categoriaPartidaSeleccionada =
      null;

    this.filtroDetalle = '';

    this.paginaDetalle = 1;

    this.errorDetalle = null;

  }


  // =========================================================
  // DETALLE FILTRADO
  // =========================================================

  get detalleFiltrado():
  MatrizRodamientoDetalle[] {

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
          item.telefono,
          item.celular,
          item.correo,
          item.codigoLineaCredito,
          item.nombreLineaCredito,
          item.pagareCartera

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


  // =========================================================
  // DETALLE PAGINADO
  // =========================================================

  get detallePaginado():
  MatrizRodamientoDetalle[] {

    const inicio =
      (
        this.paginaDetalle
        -
        1
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
  // OBTENER CELDA
  // =========================================================

  obtenerCelda(
    categoriaAnterior:
      MatrizRodamientoCategoria,
    categoriaPartida:
      MatrizRodamientoCategoria
  ): MatrizRodamientoCelda | null {

    const fila =
      this.matriz
        ?.filas
        ?.find(
          item =>
            item.categoriaAnterior
            === categoriaAnterior
        );

    return fila
      ?.celdas
      ?.find(
        item =>
          item.categoriaPartida
          === categoriaPartida
      )
      ?? null;

  }


  // =========================================================
  // EXPORTAR
  // =========================================================

  exportarExcel(): void {

    if (
      !this.matriz
    ) {

      alert(
        'Primero debe calcular la Matriz de Rodamiento.'
      );

      return;
    }

    this.exporter.exportarMatriz(
      this.matriz
    );

  }


  exportarDetalle(): void {

    if (
      !this.matriz
      ||
      !this.categoriaAnteriorSeleccionada
      ||
      !this.categoriaPartidaSeleccionada
      ||
      this.detalle.length === 0
    ) {

      alert(
        'No hay detalle para exportar.'
      );

      return;
    }


    this.exporter.exportarDetalle(
      this.matriz,
      this.categoriaAnteriorSeleccionada,
      this.categoriaPartidaSeleccionada,
      this.detalle
    );

  }


  // =========================================================
  // TOTALES POR CATEGORÍA DE DESTINO
  // =========================================================

  totalCantidadCategoria(
    categoria: MatrizRodamientoCategoria
  ): number {

    if (!this.matriz?.filas) {
      return 0;
    }

    return this.matriz.filas.reduce(
      (total, fila) => {

        const celda =
          fila.celdas?.find(
            item =>
              item.categoriaPartida === categoria
          );

        return total + this.numero(
          celda?.cantidad
        );

      },
      0
    );
  }


  totalValorCategoria(
    categoria: MatrizRodamientoCategoria
  ): number {

    if (!this.matriz?.filas) {
      return 0;
    }

    return this.matriz.filas.reduce(
      (total, fila) => {

        const celda =
          fila.celdas?.find(
            item =>
              item.categoriaPartida === categoria
          );

        return total + this.numero(
          celda?.valor
        );

      },
      0
    );
  }

  // =========================================================
  // PRESENTACIÓN
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
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }
    );

  }


  formatearPorcentaje(
    valor:
      number
      | null
      | undefined
  ): string {

    return `${this.numero(valor).toFixed(2)} %`;

  }


  // =========================================================
  // AUXILIARES
  // =========================================================

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
      .replace(
        /\s+/g,
        ' '
      )
      .toUpperCase();

  }


  private obtenerMensajeError(
    error: any,
    mensajeDefecto: string
  ): string {

    const mensaje =
      error?.error?.message
      ??
      error?.error?.mensaje
      ??
      error?.message;

    return String(
      mensaje
      ?? mensajeDefecto
    );

  }

  claseMovimiento(
    categoriaAnterior: MatrizRodamientoCategoria,
    categoriaPartida: MatrizRodamientoCategoria
  ): string {

    const orden: Record<MatrizRodamientoCategoria, number> = {
      A: 1,
      B: 2,
      C: 3,
      D: 4,
      E: 5
    };

    const anterior = orden[categoriaAnterior];
    const partida = orden[categoriaPartida];

    if (partida === anterior) {
      return 'movimiento-permanencia';
    }

    if (partida < anterior) {
      return 'movimiento-mejora';
    }

    return 'movimiento-deterioro';
  }

}
