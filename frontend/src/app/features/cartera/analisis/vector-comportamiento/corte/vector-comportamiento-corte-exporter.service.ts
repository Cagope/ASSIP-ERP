import {
  Injectable
} from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions,
  ExcelFila
} from '../../../../../shared/services/excel-export.service';

import {
  VectorComportamientoCorteResumen
} from './vector-comportamiento-corte.models';


// =========================================================
// VECTOR DE COMPORTAMIENTO POR CORTE
// EXPORTADOR EXCEL
//
// Genera:
//
// 1. TODOS
// 2. SEVERIDAD 0-10
// 3. SEVERIDAD 10-25
// 4. SEVERIDAD 25-50
// 5. SEVERIDAD 50-75
// 6. SEVERIDAD 75-100
//
// Todas las hojas contienen la misma estructura.
// =========================================================

@Injectable({
  providedIn: 'root'
})
export class VectorComportamientoCorteExporterService {

  // =========================================================
  // COLUMNAS
  // =========================================================

  private readonly columnas: string[] = [

    'Documento',
    'Nombre completo',

    'Teléfono',
    'Celular',
    'Correo',

    'Pagaré',
    'Código línea',
    'Línea de crédito',

    'Fecha de corte',
    'Primer corte histórico',
    'Último corte histórico',
    'Cortes observados',

    'Mora al corte',
    'Mora máxima histórica',
    'Cortes al día',
    'Cortes con mora',
    'PBB mora %',

    'Valor inicial',
    'Valor desembolsado',
    'Saldo primer corte',
    'Saldo al corte',
    'Variación saldo',

    'Severidad %',
    'Rango severidad'
  ];


  // =========================================================
  // ANCHOS
  // =========================================================

  private readonly anchos: number[] = [

    16, // Documento
    38, // Nombre completo

    16, // Teléfono
    16, // Celular
    34, // Correo

    14, // Pagaré
    13, // Código línea
    35, // Línea de crédito

    15, // Fecha corte
    19, // Primer corte
    19, // Último corte
    17, // Cortes observados

    14, // Mora al corte
    20, // Mora máxima
    14, // Cortes al día
    16, // Cortes con mora
    13, // PBB mora

    18, // Valor inicial
    18, // Valor desembolsado
    18, // Saldo primer corte
    18, // Saldo corte
    18, // Variación saldo

    13, // Severidad
    22  // Rango severidad
  ];


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private excelExport: ExcelExportService
  ) {
  }


  // =========================================================
  // EXPORTAR
  // =========================================================

  exportar(
    items: VectorComportamientoCorteResumen[],
    fechaCorte: string
  ): void {

    // =======================================================
    // VALIDACIONES
    // =======================================================

    if (
      !items ||
      items.length === 0
    ) {

      alert(
        'No hay información del Vector de Comportamiento para exportar.'
      );

      return;
    }


    if (!fechaCorte) {

      alert(
        'Debe seleccionar una fecha de corte.'
      );

      return;
    }


    // =======================================================
    // LIBRO
    // =======================================================

    const hojas: ExcelSheetOptions[] = [

      // =====================================================
      // TODOS
      // =====================================================

      this.crearHoja(
        'TODOS',
        'Todos los créditos',
        items,
        fechaCorte
      ),


      // =====================================================
      // SEVERIDAD 0-10
      // =====================================================

      this.crearHoja(
        'SEVERIDAD 0-10',
        'Severidad menor o igual al 10%',
        this.filtrarRango(
          items,
          'SEVERIDAD 0-10'
        ),
        fechaCorte
      ),


      // =====================================================
      // SEVERIDAD 10-25
      // =====================================================

      this.crearHoja(
        'SEVERIDAD 10-25',
        'Severidad mayor al 10% y hasta el 25%',
        this.filtrarRango(
          items,
          'SEVERIDAD 10-25'
        ),
        fechaCorte
      ),


      // =====================================================
      // SEVERIDAD 25-50
      // =====================================================

      this.crearHoja(
        'SEVERIDAD 25-50',
        'Severidad mayor al 25% y hasta el 50%',
        this.filtrarRango(
          items,
          'SEVERIDAD 25-50'
        ),
        fechaCorte
      ),


      // =====================================================
      // SEVERIDAD 50-75
      // =====================================================

      this.crearHoja(
        'SEVERIDAD 50-75',
        'Severidad mayor al 50% y hasta el 75%',
        this.filtrarRango(
          items,
          'SEVERIDAD 50-75'
        ),
        fechaCorte
      ),


      // =====================================================
      // SEVERIDAD 75-100
      // =====================================================

      this.crearHoja(
        'SEVERIDAD 75-100',
        'Severidad mayor al 75%',
        this.filtrarRango(
          items,
          'SEVERIDAD 75-100'
        ),
        fechaCorte
      )
    ];


    // =======================================================
    // EXPORTAR USANDO SERVICIO COMPARTIDO
    // =======================================================

    this.excelExport.exportar({

      nombreArchivo:
        `Vector_Comportamiento_Corte_${fechaCorte}.xlsx`,

      hojas
    });
  }


  // =========================================================
  // CREAR HOJA
  // =========================================================

  private crearHoja(
    nombreHoja: string,
    descripcion: string,
    items: VectorComportamientoCorteResumen[],
    fechaCorte: string
  ): ExcelSheetOptions {

    const filas =
      this.crearFilas(
        items
      );


    // =======================================================
    // IMPORTANTE
    //
    // ExcelExportService no crea hojas que tengan
    // filas.length === 0.
    //
    // Como este informe debe generar siempre una hoja por
    // cada rango de severidad, agregamos una fila informativa
    // cuando el rango no tiene créditos.
    // =======================================================

    const filasExportar: ExcelFila[] =
      filas.length > 0
        ? filas
        : [
            [
              'SIN REGISTROS'
            ]
          ];


    return {

      nombreHoja,

      titulo:
        'VECTOR DE COMPORTAMIENTO POR CORTE',

      subtitulo:
        descripcion,

      filtros: [
        [
          'Fecha de corte',
          fechaCorte
        ]
      ],

      resumen: [
        [
          'Cantidad de créditos',
          items.length
        ],
        [
          'Saldo total al corte',
          this.sumarSaldo(
            items
          )
        ]
      ],

      columnas:
        this.columnas,

      filas:
        filasExportar,

      anchos:
        this.anchos
    };
  }


  // =========================================================
  // CREAR FILAS
  // =========================================================

  private crearFilas(
    items: VectorComportamientoCorteResumen[]
  ): ExcelFila[] {

    return items.map(
      item => [

        // ===================================================
        // IDENTIFICACIÓN / CONTACTO
        // ===================================================

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


        // ===================================================
        // CRÉDITO
        // ===================================================

        this.texto(
          item.pagareCartera
        ),

        this.texto(
          item.codigoLineaCredito
        ),

        this.texto(
          item.nombreLineaCredito
        ),


        // ===================================================
        // PERÍODO / HISTORIA
        // ===================================================

        this.texto(
          item.fechaCorte
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


        // ===================================================
        // COMPORTAMIENTO
        // ===================================================

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


        // ===================================================
        // VALORES
        // ===================================================

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


        // ===================================================
        // SEVERIDAD
        // ===================================================

        this.numeroNullable(
          item.severidad
        ),

        this.texto(
          item.rangoSeveridad
        )
      ]
    );
  }


  // =========================================================
  // FILTRAR RANGO
  // =========================================================

  private filtrarRango(
    items: VectorComportamientoCorteResumen[],
    rango: string
  ): VectorComportamientoCorteResumen[] {

    const rangoEsperado =
      this.normalizarTexto(
        rango
      );


    return items.filter(
      item =>
        this.normalizarTexto(
          item.rangoSeveridad
        ) ===
        rangoEsperado
    );
  }


  // =========================================================
  // SALDO TOTAL
  // =========================================================

  private sumarSaldo(
    items: VectorComportamientoCorteResumen[]
  ): number {

    return items.reduce(
      (
        total,
        item
      ) =>
        total +
        this.numero(
          item.saldoUltimoCorte
        ),
      0
    );
  }


  // =========================================================
  // TEXTO
  // =========================================================

  private texto(
    valor:
      | string
      | number
      | null
      | undefined
  ): string {

    if (
      valor === null ||
      valor === undefined
    ) {
      return '';
    }

    return String(
      valor
    ).trim();
  }


  // =========================================================
  // NORMALIZAR TEXTO
  // =========================================================

  private normalizarTexto(
    valor:
      | string
      | null
      | undefined
  ): string {

    return this.texto(
      valor
    )
      .toUpperCase()
      .replace(/\s+/g, ' ')
      .trim();
  }


  // =========================================================
  // NÚMERO
  // =========================================================

  private numero(
    valor:
      | number
      | string
      | null
      | undefined
  ): number {

    if (
      valor === null ||
      valor === undefined ||
      valor === ''
    ) {
      return 0;
    }

    const resultado =
      Number(
        valor
      );

    return Number.isFinite(
      resultado
    )
      ? resultado
      : 0;
  }


  // =========================================================
  // NÚMERO NULLABLE
  // =========================================================

  private numeroNullable(
    valor:
      | number
      | string
      | null
      | undefined
  ): number | null {

    if (
      valor === null ||
      valor === undefined ||
      valor === ''
    ) {
      return null;
    }

    const resultado =
      Number(
        valor
      );

    return Number.isFinite(
      resultado
    )
      ? resultado
      : null;
  }

}
