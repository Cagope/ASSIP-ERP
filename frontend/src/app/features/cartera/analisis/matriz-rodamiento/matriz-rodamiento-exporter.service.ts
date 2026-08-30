import {
  Injectable
} from '@angular/core';

import {
  ExcelExportService,
  ExcelFila,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

import {
  MATRIZ_RODAMIENTO_CATEGORIAS,
  MatrizRodamiento,
  MatrizRodamientoCategoria,
  MatrizRodamientoDetalle
} from './matriz-rodamiento.models';


@Injectable({
  providedIn: 'root'
})
export class MatrizRodamientoExporterService {

  constructor(
    private readonly excelExport:
      ExcelExportService
  ) {}


  // =========================================================
  // EXPORTAR MATRIZ
  // =========================================================

  exportarMatriz(
    matriz: MatrizRodamiento
  ): void {

    const hojas:
      ExcelSheetOptions[] = [

        this.crearHojaCantidades(
          matriz
        ),

        this.crearHojaValores(
          matriz
        ),

        this.crearHojaProbabilidadesCantidad(
          matriz
        ),

        this.crearHojaProbabilidadesValor(
          matriz
        )

      ];

    this.excelExport.exportar({

      nombreArchivo:
        `Matriz_Rodamiento_${matriz.fechaComparacion}_${matriz.fechaPartida}.xlsx`,

      hojas

    });

  }


  // =========================================================
  // EXPORTAR DETALLE DE CELDA
  // =========================================================

  exportarDetalle(
    matriz: MatrizRodamiento,
    categoriaAnterior:
      MatrizRodamientoCategoria,
    categoriaPartida:
      MatrizRodamientoCategoria,
    detalle:
      MatrizRodamientoDetalle[]
  ): void {

    const hoja:
      ExcelSheetOptions = {

      nombreHoja:
        `${categoriaAnterior}-${categoriaPartida}`,

      titulo:
        'MATRIZ DE RODAMIENTO - DETALLE',

      subtitulo:
        `Transición ${categoriaAnterior} → ${categoriaPartida}`,

      filtros: [

        [
          'Datos de partida',
          matriz.tipoPartida === 'ACTUAL'
            ? 'Cartera actual'
            : matriz.fechaPartida
        ],

        [
          'Período de comparación',
          matriz.fechaComparacion
        ],

        [
          'Categoría anterior',
          categoriaAnterior
        ],

        [
          'Categoría partida',
          categoriaPartida
        ]

      ],

      resumen: [

        [
          'Cantidad de créditos',
          detalle.length
        ],

        [
          'Valor datos de partida',
          this.totalSaldoPartida(
            detalle
          )
        ]

      ],

      columnas: [
        'Documento',
        'Nombre completo',
        'Teléfono',
        'Celular',
        'Correo',
        'Agencia',
        'Código línea',
        'Línea de crédito',
        'Pagaré',
        'Fecha desembolso',
        'Fecha comparación',
        'Saldo anterior',
        'Días mora anterior',
        'Categoría anterior',
        'Fecha partida',
        'Saldo partida',
        'Días mora partida',
        'Categoría partida'
      ],

      filas:
        this.construirDetalle(
          detalle
        ),

      anchos: [
        18,
        38,
        16,
        16,
        32,
        12,
        14,
        30,
        16,
        18,
        18,
        18,
        16,
        16,
        18,
        18,
        16,
        16
      ]

    };


    this.excelExport.exportar({

      nombreArchivo:
        `Matriz_Rodamiento_Detalle_${categoriaAnterior}_${categoriaPartida}.xlsx`,

      hojas: [
        hoja
      ]

    });

  }


  // =========================================================
  // HOJA CANTIDADES
  // =========================================================

  private crearHojaCantidades(
    matriz: MatrizRodamiento
  ): ExcelSheetOptions {

    const filas:
      ExcelFila[] =
        matriz.filas.map(
          fila => [

            fila.categoriaAnterior,

            ...MATRIZ_RODAMIENTO_CATEGORIAS.map(
              categoria =>
                this.obtenerCantidad(
                  fila.celdas,
                  categoria
                )
            ),

            fila.totalCantidad,

            fila.probabilidadMejoraCantidad,

            fila.probabilidadPermanenciaCantidad,

            fila.probabilidadDeterioroCantidad

          ]
        );


    filas.push([

      'TOTAL',

      ...MATRIZ_RODAMIENTO_CATEGORIAS.map(
        categoria =>
          this.numero(
            matriz.totalesCantidadPorCategoria?.[
              categoria
            ]
          )
      ),

      matriz.totalCantidad,

      '',
      '',
      ''

    ]);


    return {

      nombreHoja:
        'CANTIDADES',

      titulo:
        'MATRIZ DE RODAMIENTO - CANTIDADES',

      subtitulo:
        this.subtitulo(
          matriz
        ),

      filtros:
        this.filtros(
          matriz
        ),

      resumen: [
        [
          'Población datos de partida',
          matriz.cantidadPoblacionPartida
        ],
        [
          'Créditos incluidos en matriz',
          matriz.cantidadCreditosMatriz
        ]
      ],

      columnas: [
        'Categoría anterior',
        'A',
        'B',
        'C',
        'D',
        'E',
        'Total',
        'Mejora %',
        'Permanencia %',
        'Deterioro %'
      ],

      filas,

      anchos: [
        20,
        14,
        14,
        14,
        14,
        14,
        16,
        16,
        18,
        18
      ]

    };

  }


  // =========================================================
  // HOJA VALORES
  // =========================================================

  private crearHojaValores(
    matriz: MatrizRodamiento
  ): ExcelSheetOptions {

    const filas:
      ExcelFila[] =
        matriz.filas.map(
          fila => [

            fila.categoriaAnterior,

            ...MATRIZ_RODAMIENTO_CATEGORIAS.map(
              categoria =>
                this.obtenerValor(
                  fila.celdas,
                  categoria
                )
            ),

            fila.totalValor,

            fila.probabilidadMejoraValor,

            fila.probabilidadPermanenciaValor,

            fila.probabilidadDeterioroValor

          ]
        );


    filas.push([

      'TOTAL',

      ...MATRIZ_RODAMIENTO_CATEGORIAS.map(
        categoria =>
          this.numero(
            matriz.totalesValorPorCategoria?.[
              categoria
            ]
          )
      ),

      matriz.totalValor,

      '',
      '',
      ''

    ]);


    return {

      nombreHoja:
        'VALORES',

      titulo:
        'MATRIZ DE RODAMIENTO - VALORES',

      subtitulo:
        this.subtitulo(
          matriz
        ),

      filtros:
        this.filtros(
          matriz
        ),

      resumen: [
        [
          'Valor total matriz',
          matriz.totalValor
        ]
      ],

      columnas: [
        'Categoría anterior',
        'A',
        'B',
        'C',
        'D',
        'E',
        'Total',
        'Mejora %',
        'Permanencia %',
        'Deterioro %'
      ],

      filas,

      anchos: [
        20,
        20,
        20,
        20,
        20,
        20,
        22,
        16,
        18,
        18
      ]

    };

  }


  // =========================================================
  // PROBABILIDADES CANTIDAD
  // =========================================================

  private crearHojaProbabilidadesCantidad(
    matriz: MatrizRodamiento
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'PROB CANTIDAD',

      titulo:
        'MATRIZ DE RODAMIENTO - PROBABILIDADES POR CANTIDAD',

      subtitulo:
        this.subtitulo(
          matriz
        ),

      filtros:
        this.filtros(
          matriz
        ),

      resumen: [],

      columnas: [
        'Categoría',
        'Mejora %',
        'Permanencia %',
        'Deterioro %'
      ],

      filas:
        matriz.filas.map(
          fila => [
            fila.categoriaAnterior,
            fila.probabilidadMejoraCantidad,
            fila.probabilidadPermanenciaCantidad,
            fila.probabilidadDeterioroCantidad
          ]
        ),

      anchos: [
        18,
        18,
        18,
        18
      ]

    };

  }


  // =========================================================
  // PROBABILIDADES VALOR
  // =========================================================

  private crearHojaProbabilidadesValor(
    matriz: MatrizRodamiento
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'PROB VALOR',

      titulo:
        'MATRIZ DE RODAMIENTO - PROBABILIDADES POR VALOR',

      subtitulo:
        this.subtitulo(
          matriz
        ),

      filtros:
        this.filtros(
          matriz
        ),

      resumen: [],

      columnas: [
        'Categoría',
        'Mejora %',
        'Permanencia %',
        'Deterioro %'
      ],

      filas:
        matriz.filas.map(
          fila => [
            fila.categoriaAnterior,
            fila.probabilidadMejoraValor,
            fila.probabilidadPermanenciaValor,
            fila.probabilidadDeterioroValor
          ]
        ),

      anchos: [
        18,
        18,
        18,
        18
      ]

    };

  }


  // =========================================================
  // DETALLE
  // =========================================================

  private construirDetalle(
    detalle:
      MatrizRodamientoDetalle[]
  ): ExcelFila[] {

    if (
      !detalle
      ||
      detalle.length === 0
    ) {

      return [
        [
          'SIN REGISTROS'
        ]
      ];

    }


    return detalle.map(
      item => [

        this.texto(
          item.documento
        ),

        this.texto(
          item.nombreCompleto
        ),

        this.texto(
          item.telefono
        ),

        this.texto(
          item.celular
        ),

        this.texto(
          item.correo
        ),

        this.numero(
          item.idAgencia
        ),

        this.texto(
          item.codigoLineaCredito
        ),

        this.texto(
          item.nombreLineaCredito
        ),

        this.texto(
          item.pagareCartera
        ),

        this.texto(
          item.fechaDesembolso
        ),

        this.texto(
          item.fechaComparacion
        ),

        this.numero(
          item.saldoAnterior
        ),

        this.numero(
          item.diasMoraAnterior
        ),

        item.categoriaAnterior,

        this.texto(
          item.fechaPartida
        ),

        this.numero(
          item.saldoPartida
        ),

        this.numero(
          item.diasMoraPartida
        ),

        item.categoriaPartida

      ]
    );

  }


  // =========================================================
  // AUXILIARES
  // =========================================================

  private obtenerCantidad(
    celdas: MatrizRodamiento['filas'][number]['celdas'],
    categoria:
      MatrizRodamientoCategoria
  ): number {

    return this.numero(
      celdas.find(
        celda =>
          celda.categoriaPartida
          === categoria
      )?.cantidad
    );

  }


  private obtenerValor(
    celdas: MatrizRodamiento['filas'][number]['celdas'],
    categoria:
      MatrizRodamientoCategoria
  ): number {

    return this.numero(
      celdas.find(
        celda =>
          celda.categoriaPartida
          === categoria
      )?.valor
    );

  }


  private filtros(
    matriz: MatrizRodamiento
  ): ExcelFila[] {

    return [

      [
        'Datos de partida',
        matriz.tipoPartida === 'ACTUAL'
          ? `Cartera actual - ${matriz.fechaPartida}`
          : matriz.fechaPartida
      ],

      [
        'Período de comparación',
        matriz.fechaComparacion
      ]

    ];

  }


  private subtitulo(
    matriz: MatrizRodamiento
  ): string {

    const partida =
      matriz.tipoPartida === 'ACTUAL'
        ? `Actual ${matriz.fechaPartida}`
        : matriz.fechaPartida;

    return `${matriz.fechaComparacion} → ${partida}`;

  }


  private totalSaldoPartida(
    detalle:
      MatrizRodamientoDetalle[]
  ): number {

    return detalle.reduce(
      (
        total,
        item
      ) =>
        total
        +
        this.numero(
          item.saldoPartida
        ),
      0
    );

  }


  private texto(
    valor:
      string
      | number
      | null
      | undefined
  ): string {

    return String(
      valor
      ?? ''
    ).trim();

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

}
