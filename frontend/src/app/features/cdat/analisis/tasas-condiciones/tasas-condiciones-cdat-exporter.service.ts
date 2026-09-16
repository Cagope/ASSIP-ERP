import {
  Injectable
} from '@angular/core';

import {
  ExcelExportService,
  ExcelFila,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

import {
  TasasCondicionesCdatCondicion,
  TasasCondicionesCdatDetalle,
  TasasCondicionesCdatRangoSaldo,
  TasasCondicionesCdatResumen
} from './tasas-condiciones-cdat.models';


@Injectable({
  providedIn: 'root'
})
export class TasasCondicionesCdatExporterService {

  constructor(
    private readonly excelExport:
      ExcelExportService
  ) {}


  // =========================================================
  // EXPORTAR
  // =========================================================

  exportar(
    resumen: TasasCondicionesCdatResumen,
    condiciones: TasasCondicionesCdatCondicion[],
    rangos: TasasCondicionesCdatRangoSaldo[],
    detalle: TasasCondicionesCdatDetalle[],
    fechaCorte: string,
    nombreCorte: string,
    nombreAgencia: string,
    plazoMeses: number,
    nombreAmortizacion: string,
    nombreRango: string
  ): void {

    this.excelExport.exportar({

      nombreArchivo:
        this.crearNombreArchivo(fechaCorte),

      hojas: [

        this.crearHojaResumen(
          resumen,
          fechaCorte,
          nombreCorte,
          nombreAgencia,
          plazoMeses,
          nombreAmortizacion,
          nombreRango
        ),

        this.crearHojaCondiciones(
          condiciones,
          fechaCorte,
          nombreCorte,
          nombreAgencia,
          plazoMeses,
          nombreAmortizacion,
          nombreRango
        ),

        this.crearHojaRangos(
          rangos,
          fechaCorte,
          nombreCorte,
          nombreAgencia,
          plazoMeses,
          nombreAmortizacion,
          nombreRango
        ),

        this.crearHojaDetalle(
          detalle,
          fechaCorte,
          nombreCorte,
          nombreAgencia,
          plazoMeses,
          nombreAmortizacion,
          nombreRango
        )

      ]

    });
  }


  // =========================================================
  // RESUMEN
  // =========================================================

  private crearHojaResumen(
    resumen: TasasCondicionesCdatResumen,
    fechaCorte: string,
    nombreCorte: string,
    nombreAgencia: string,
    plazoMeses: number,
    nombreAmortizacion: string,
    nombreRango: string
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'RESUMEN',

      titulo:
        'ANÁLISIS DE TASAS Y CONDICIONES DE CAPTACIÓN CDAT',

      filtros:
        this.crearFiltros(
          fechaCorte,
          nombreCorte,
          nombreAgencia,
          plazoMeses,
          nombreAmortizacion,
          nombreRango
        ),

      resumen: [

        [
          'Saldo captado',
          this.numero(resumen.saldoTotal)
        ],

        [
          'CDAT',
          this.numero(resumen.cantidadCdats)
        ],

        [
          'Depositantes',
          this.numero(resumen.cantidadDepositantes)
        ],

        [
          'Saldo promedio',
          this.numero(resumen.saldoPromedio)
        ],

        [
          'TNA ponderada',
          this.numero(resumen.tasaNominalPonderada)
        ],

        [
          'TEA ponderada',
          this.numero(resumen.tasaEfectivaPonderada)
        ],

        [
          'Plazo ponderado meses',
          this.numero(resumen.plazoPonderadoMeses)
        ]

      ],

      columnas: [
        'Indicador',
        'Valor'
      ],

      filas: [
        [
          'Población analizada',
          resumen.cantidadCdats
        ]
      ],

      anchos: [
        32,
        24
      ]

    };
  }


  // =========================================================
  // CONDICIONES
  // =========================================================

  private crearHojaCondiciones(
    datos: TasasCondicionesCdatCondicion[],
    fechaCorte: string,
    nombreCorte: string,
    nombreAgencia: string,
    plazoMeses: number,
    nombreAmortizacion: string,
    nombreRango: string
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'CONDICIONES',

      titulo:
        'CDAT - CONDICIONES DE CAPTACIÓN',

      filtros:
        this.crearFiltros(
          fechaCorte,
          nombreCorte,
          nombreAgencia,
          plazoMeses,
          nombreAmortizacion,
          nombreRango
        ),

      columnas: [
        'Plazo meses',
        'Código amortización',
        'Amortización',
        'CDAT',
        'Saldo',
        '% Participación',
        'TNA ponderada',
        'TEA ponderada'
      ],

      filas:
        datos.map(
          item => [
            item.plazoMeses,
            item.amortizacionDeposito,
            item.nombreAmortizacion,
            item.cantidadCdats,
            item.saldoTotal,
            item.participacionSaldo,
            item.tasaNominalPonderada,
            item.tasaEfectivaPonderada
          ]
        ),

      anchos: [
        14,
        20,
        22,
        12,
        22,
        18,
        18,
        18
      ]

    };
  }


  // =========================================================
  // RANGOS
  // =========================================================

  private crearHojaRangos(
    datos: TasasCondicionesCdatRangoSaldo[],
    fechaCorte: string,
    nombreCorte: string,
    nombreAgencia: string,
    plazoMeses: number,
    nombreAmortizacion: string,
    nombreRango: string
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'RANGOS SALDO',

      titulo:
        'CDAT - TASAS POR TAMAÑO DE CAPTACIÓN',

      filtros:
        this.crearFiltros(
          fechaCorte,
          nombreCorte,
          nombreAgencia,
          plazoMeses,
          nombreAmortizacion,
          nombreRango
        ),

      columnas: [
        'Rango de saldo',
        'CDAT',
        'Saldo',
        '% Participación',
        'TNA ponderada',
        'TEA ponderada',
        'TEA mínima',
        'TEA máxima'
      ],

      filas:
        datos.map(
          item => [
            item.rangoSaldo,
            item.cantidadCdats,
            item.saldoTotal,
            item.participacionSaldo,
            item.tasaNominalPonderada,
            item.tasaEfectivaPonderada,
            item.tasaEfectivaMinima,
            item.tasaEfectivaMaxima
          ]
        ),

      anchos: [
        30,
        12,
        22,
        18,
        18,
        18,
        16,
        16
      ]

    };
  }


  // =========================================================
  // DETALLE
  // =========================================================

  private crearHojaDetalle(
    datos: TasasCondicionesCdatDetalle[],
    fechaCorte: string,
    nombreCorte: string,
    nombreAgencia: string,
    plazoMeses: number,
    nombreAmortizacion: string,
    nombreRango: string
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'DETALLE',

      titulo:
        'CDAT - DETALLE AUDITABLE',

      filtros:
        this.crearFiltros(
          fechaCorte,
          nombreCorte,
          nombreAgencia,
          plazoMeses,
          nombreAmortizacion,
          nombreRango
        ),

      columnas: [
        'Código CDAT',
        'Tipo documento',
        'Documento',
        'Depositante',
        'Código agencia',
        'Agencia',
        'Fecha apertura',
        'Fecha vencimiento',
        'Valor apertura',
        'Saldo al corte',
        'Plazo meses',
        'Plazo días',
        'Código amortización',
        'Amortización',
        'TNA',
        'TEA'
      ],

      filas:
        datos.map(
          item => [
            item.codigoCdat,
            item.tipoDocumento,
            item.documento,
            item.nombreCompleto,
            item.codigoAgencia,
            item.nombreAgencia,
            item.fechaAperturaCdat,
            item.fechaVencimientoCdat,
            item.valorAperturaCdat,
            item.saldoActualCdat,
            item.plazoMeses,
            item.plazoDias,
            item.amortizacionDeposito,
            item.nombreAmortizacion,
            item.tasaNominalAnual,
            item.tasaEfectivaAnual
          ]
        ),

      anchos: [
        15,
        16,
        18,
        40,
        15,
        32,
        16,
        18,
        20,
        20,
        14,
        14,
        20,
        20,
        14,
        14
      ]

    };
  }


  // =========================================================
  // FILTROS
  // =========================================================

  private crearFiltros(
    fechaCorte: string,
    nombreCorte: string,
    nombreAgencia: string,
    plazoMeses: number,
    nombreAmortizacion: string,
    nombreRango: string
  ): ExcelFila[] {

    return [

      [
        'Corte',
        nombreCorte || fechaCorte
      ],

      [
        'Fecha de corte',
        fechaCorte
      ],

      [
        'Agencia',
        nombreAgencia
      ],

      [
        'Plazo',
        plazoMeses > 0
          ? `${plazoMeses} meses`
          : 'Todos'
      ],

      [
        'Amortización',
        nombreAmortizacion
      ],

      [
        'Rango de saldo',
        nombreRango
      ]

    ];
  }


  // =========================================================
  // APOYO
  // =========================================================

  private crearNombreArchivo(
    fechaCorte: string
  ): string {

    if (!fechaCorte) {
      return 'Analisis_Tasas_Condiciones_CDAT.xlsx';
    }

    return `Analisis_Tasas_Condiciones_CDAT_${fechaCorte}.xlsx`;
  }


  private numero(
    valor: number | null | undefined
  ): number {

    return Number(
      valor ?? 0
    );
  }

}
