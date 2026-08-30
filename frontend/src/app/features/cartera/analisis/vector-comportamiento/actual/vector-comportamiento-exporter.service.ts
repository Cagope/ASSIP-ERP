import {
  Injectable
} from '@angular/core';

import {
  ExcelExportService,
  ExcelFila,
  ExcelSheetOptions
} from '../../../../../shared/services/excel-export.service';

import {
  VectorComportamientoResumen
} from './vector-comportamiento.models';


@Injectable({
  providedIn: 'root'
})
export class VectorComportamientoExporterService {

  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly excelExport:
      ExcelExportService
  ) {}


  // =========================================================
  // EXPORTAR
  // =========================================================

  exportar(
    items:
      VectorComportamientoResumen[]
  ): void {

    if (
      !items
      ||
      items.length === 0
    ) {

      alert(
        'No hay información del Vector de Comportamiento para exportar.'
      );

      return;
    }


    const hojas:
      ExcelSheetOptions[] = [

        this.crearHoja(
          'TODOS',
          items
        ),

        this.crearHoja(
          'SEVERIDAD 0-10',
          this.filtrarPorSeveridad(
            items,
            'SEVERIDAD 0-10'
          )
        ),

        this.crearHoja(
          'SEVERIDAD 10-25',
          this.filtrarPorSeveridad(
            items,
            'SEVERIDAD 10-25'
          )
        ),

        this.crearHoja(
          'SEVERIDAD 25-50',
          this.filtrarPorSeveridad(
            items,
            'SEVERIDAD 25-50'
          )
        ),

        this.crearHoja(
          'SEVERIDAD 50-75',
          this.filtrarPorSeveridad(
            items,
            'SEVERIDAD 50-75'
          )
        ),

        this.crearHoja(
          'SEVERIDAD 75-100',
          this.filtrarPorSeveridad(
            items,
            'SEVERIDAD 75-100'
          )
        )

      ];


    this.excelExport.exportar({

      nombreArchivo:
        'Vector_Comportamiento_Actual.xlsx',

      hojas

    });

  }


  // =========================================================
  // CREAR HOJA
  // =========================================================

  private crearHoja(
    nombreHoja: string,
    items:
      VectorComportamientoResumen[]
  ): ExcelSheetOptions {

    const filas =
      this.construirFilas(
        items
      );


    return {

      nombreHoja,

      titulo:
        'VECTOR DE COMPORTAMIENTO ACTUAL',

      subtitulo:
        nombreHoja === 'TODOS'
          ? 'Cartera activa con saldo actual mayor a cero'
          : `Distribución ${nombreHoja}`,

      filtros: [
        [
          'Población',
          'Créditos activos con saldo actual mayor a cero'
        ]
      ],

      resumen: [
        [
          'Cantidad de créditos',
          items.length
        ],
        [
          'Saldo actual',
          this.totalSaldoActual(
            items
          )
        ]
      ],

      columnas:
        this.columnas(),

      filas,

      anchos:
        this.anchos()

    };

  }


  // =========================================================
  // COLUMNAS
  // =========================================================

  private columnas():
  string[] {

    return [

      'Documento',

      'Nombre completo',

      'Teléfono',

      'Celular',

      'Correo',

      'Pagaré',

      'Código línea',

      'Línea de crédito',

      'Fecha desembolso',

      'Primer corte histórico',

      'Último corte histórico',

      'Cortes observados',

      'Mora último corte',

      'Mora máxima histórica',

      'Cortes al día',

      'Cortes con mora',

      'PBB mora %',

      'Valor inicial',

      'Valor desembolsado',

      'Saldo primer corte',

      'Saldo último corte',

      'Variación saldo período',

      'Saldo actual',

      'Severidad %',

      'Rango severidad'

    ];

  }


  // =========================================================
  // ANCHOS
  // =========================================================

  private anchos():
  number[] {

    return [

      18, // Documento

      38, // Nombre completo

      16, // Teléfono

      16, // Celular

      32, // Correo

      16, // Pagaré

      14, // Código línea

      30, // Línea

      16, // Fecha desembolso

      18, // Primer corte

      18, // Último corte

      16, // Cortes observados

      16, // Mora último corte

      18, // Mora máxima

      14, // Cortes al día

      16, // Cortes con mora

      14, // PBB

      18, // Valor inicial

      18, // Valor desembolsado

      18, // Saldo primer corte

      18, // Saldo último corte

      20, // Variación

      18, // Saldo actual

      14, // Severidad

      22  // Rango severidad

    ];

  }


  // =========================================================
  // CONSTRUIR FILAS
  // =========================================================

  private construirFilas(
    items:
      VectorComportamientoResumen[]
  ): ExcelFila[] {

    if (
      !items
      ||
      items.length === 0
    ) {

      /*
       * Se conserva una fila para que ExcelExportService
       * no elimine la hoja cuando un rango de severidad
       * no tenga créditos.
       */
      return [
        [
          'SIN REGISTROS'
        ]
      ];

    }


    return items.map(
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

        this.texto(
          item.pagareCartera
        ),

        this.texto(
          item.codigoLineaCredito
        ),

        this.texto(
          item.nombreLineaCredito
        ),

        this.texto(
          item.fechaDesembolso
        ),

        this.texto(
          item.primerCorte
        ),

        this.texto(
          item.ultimoCorte
        ),

        this.numero(
          item.cantidadCortesObservados
        ),

        this.numero(
          item.moraUltimoCorte
        ),

        this.numero(
          item.moraMaxima
        ),

        this.numero(
          item.cantidadCortesAlDia
        ),

        this.numero(
          item.cantidadCortesConMora
        ),

        this.numero(
          item.pbbMora
        ),

        this.numero(
          item.valorInicialCredito
        ),

        this.numero(
          item.valorDesembolsado
        ),

        this.numero(
          item.saldoPrimerCorte
        ),

        this.numero(
          item.saldoUltimoCorte
        ),

        this.numero(
          item.variacionSaldoPeriodo
        ),

        this.numero(
          item.saldoActualMaestro
        ),

        this.numero(
          item.severidad
        ),

        this.texto(
          item.rangoSeveridad
        )

      ]
    );

  }


  // =========================================================
  // FILTRAR POR SEVERIDAD
  // =========================================================

  private filtrarPorSeveridad(
    items:
      VectorComportamientoResumen[],
    rango: string
  ):
  VectorComportamientoResumen[] {

    const rangoNormalizado =
      this.normalizarTexto(
        rango
      );


    return items.filter(
      item =>
        this.normalizarTexto(
          item.rangoSeveridad
        )
        ===
        rangoNormalizado
    );

  }


  // =========================================================
  // TOTAL SALDO ACTUAL
  // =========================================================

  private totalSaldoActual(
    items:
      VectorComportamientoResumen[]
  ): number {

    return items.reduce(
      (
        total,
        item
      ) =>
        total
        +
        this.numero(
          item.saldoActualMaestro
        ),
      0
    );

  }


  // =========================================================
  // NORMALIZAR TEXTO
  // =========================================================

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


  // =========================================================
  // TEXTO PARA EXCEL
  // =========================================================

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


  // =========================================================
  // NÚMERO PARA EXCEL
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

}
