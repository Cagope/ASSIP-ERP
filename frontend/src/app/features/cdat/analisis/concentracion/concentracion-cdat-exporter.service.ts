import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelFila,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

import {
  ConcentracionCdatDepositante,
  ConcentracionCdatDetalle,
  ConcentracionCdatResumen
} from './concentracion-cdat.models';


@Injectable({
  providedIn: 'root'
})
export class ConcentracionCdatExporterService {

  constructor(
    private readonly excelExport: ExcelExportService
  ) {
  }


  // =========================================================
  // INFORME COMPLETO
  // =========================================================

  exportarInforme(
    resumen: ConcentracionCdatResumen,
    ranking: ConcentracionCdatDepositante[],
    nombreAgencia: string
  ): void {

    this.excelExport.exportar({

      nombreArchivo:
        `concentracion-cdat-${resumen.fechaCorte}.xlsx`,

      hojas: [

        this.crearHojaResumen(
          resumen,
          nombreAgencia
        ),

        this.crearHojaRanking(
          resumen.fechaCorte,
          nombreAgencia,
          ranking
        )

      ]

    });
  }


  // =========================================================
  // DETALLE DE UN DEPOSITANTE
  // =========================================================

  exportarDetalle(
    fechaCorte: string,
    nombreAgencia: string,
    depositante: ConcentracionCdatDepositante,
    detalle: ConcentracionCdatDetalle[]
  ): void {

    this.excelExport.exportar({

      nombreArchivo:
        `concentracion-cdat-${depositante.documento}-${fechaCorte}.xlsx`,

      hojas: [

        this.crearHojaDetalle(
          fechaCorte,
          nombreAgencia,
          depositante,
          detalle
        )

      ]

    });
  }


  // =========================================================
  // HOJA RESUMEN
  // =========================================================

  private crearHojaResumen(
    resumen: ConcentracionCdatResumen,
    nombreAgencia: string
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'RESUMEN',

      titulo:
        'CONCENTRACIÓN DE CDAT',

      subtitulo:
        `Fecha de corte: ${resumen.fechaCorte}`,

      filtros: [

        [
          'Fecha de corte',
          resumen.fechaCorte
        ],

        [
          'Agencia',
          nombreAgencia
        ]

      ],

      resumen: [

        [
          'CDAT activos',
          this.numero(
            resumen.totalCdats
          )
        ],

        [
          'Depositantes',
          this.numero(
            resumen.totalDepositantes
          )
        ],

        [
          'Saldo total',
          this.numero(
            resumen.saldoTotal
          )
        ],

        [
          'Saldo promedio',
          this.numero(
            resumen.saldoPromedio
          )
        ],

        [
          'Tasa ponderada',
          this.numero(
            resumen.tasaPonderada
          )
        ],

        [
          'Plazo ponderado meses',
          this.numero(
            resumen.plazoPonderadoMeses
          )
        ],

        [
          'Participación Top 10',
          this.numero(
            resumen.participacionTop10
          )
        ],

        [
          'Participación Top 20',
          this.numero(
            resumen.participacionTop20
          )
        ],

        [
          'Participación Top 50',
          this.numero(
            resumen.participacionTop50
          )
        ],

        [
          'HHI',
          this.numero(
            resumen.hhi
          )
        ],

        [
          'Depositantes concentran 50%',
          this.numero(
            resumen.depositantesConcentran50
          )
        ],

        [
          'Depositantes concentran 80%',
          this.numero(
            resumen.depositantesConcentran80
          )
        ]

      ],

      columnas: [
        'Indicador',
        'Valor'
      ],

      filas: [
        [
          'Concentración Top 10',
          this.numero(
            resumen.participacionTop10
          )
        ],
        [
          'Concentración Top 20',
          this.numero(
            resumen.participacionTop20
          )
        ],
        [
          'Concentración Top 50',
          this.numero(
            resumen.participacionTop50
          )
        ],
        [
          'HHI',
          this.numero(
            resumen.hhi
          )
        ]
      ],

      anchos: [
        34,
        22
      ]

    };
  }


  // =========================================================
  // HOJA RANKING
  // =========================================================

  private crearHojaRanking(
    fechaCorte: string,
    nombreAgencia: string,
    ranking: ConcentracionCdatDepositante[]
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'RANKING DEPOSITANTES',

      titulo:
        'CONCENTRACIÓN DE CDAT - RANKING DE DEPOSITANTES',

      subtitulo:
        `Fecha de corte: ${fechaCorte}`,

      filtros: [

        [
          'Fecha de corte',
          fechaCorte
        ],

        [
          'Agencia',
          nombreAgencia
        ]

      ],

      resumen: [

        [
          'Cantidad depositantes',
          ranking.length
        ],

        [
          'Saldo total',
          this.totalRanking(
            ranking
          )
        ]

      ],

      columnas: [
        'Posición',
        'Documento',
        'Depositante',
        'Cantidad CDAT',
        'Saldo total',
        '% Participación',
        '% Acumulado',
        'Tasa ponderada',
        'Plazo ponderado meses'
      ],

      filas:
        this.construirRanking(
          ranking
        ),

      anchos: [
        12,
        18,
        42,
        16,
        22,
        18,
        18,
        18,
        22
      ]

    };
  }


  // =========================================================
  // HOJA DETALLE
  // =========================================================

  private crearHojaDetalle(
    fechaCorte: string,
    nombreAgencia: string,
    depositante: ConcentracionCdatDepositante,
    detalle: ConcentracionCdatDetalle[]
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'DETALLE CDAT',

      titulo:
        'CONCENTRACIÓN DE CDAT - DETALLE DEL DEPOSITANTE',

      subtitulo:
        `${depositante.documento} - ${depositante.nombreCompleto}`,

      filtros: [

        [
          'Fecha de corte',
          fechaCorte
        ],

        [
          'Agencia',
          nombreAgencia
        ],

        [
          'Documento',
          depositante.documento
        ],

        [
          'Depositante',
          depositante.nombreCompleto
        ]

      ],

      resumen: [

        [
          'Cantidad CDAT',
          this.numero(
            depositante.cantidadCdats
          )
        ],

        [
          'Saldo consolidado',
          this.numero(
            depositante.saldoTotal
          )
        ],

        [
          '% Participación',
          this.numero(
            depositante.participacion
          )
        ],

        [
          'Tasa ponderada',
          this.numero(
            depositante.tasaPonderada
          )
        ],

        [
          'Plazo ponderado meses',
          this.numero(
            depositante.plazoPonderadoMeses
          )
        ]

      ],

      columnas: [
        'CDAT',
        'Código agencia',
        'Agencia',
        'Fecha apertura',
        'Fecha vencimiento',
        'Plazo meses',
        'Plazo días',
        'Tasa nominal anual',
        'Valor apertura',
        'Saldo actual',
        '% Participación depositante'
      ],

      filas:
        this.construirDetalle(
          detalle
        ),

      anchos: [
        14,
        14,
        34,
        16,
        16,
        14,
        14,
        18,
        22,
        22,
        24
      ]

    };
  }


  // =========================================================
  // FILAS RANKING
  // =========================================================

  private construirRanking(
    ranking: ConcentracionCdatDepositante[]
  ): ExcelFila[] {

    if (
      !ranking
      ||
      ranking.length === 0
    ) {

      return [
        [
          'SIN REGISTROS'
        ]
      ];
    }

    return ranking.map(
      item => [

        this.numero(
          item.posicion
        ),

        this.texto(
          item.documento
        ),

        this.texto(
          item.nombreCompleto
        ),

        this.numero(
          item.cantidadCdats
        ),

        this.numero(
          item.saldoTotal
        ),

        this.numero(
          item.participacion
        ),

        this.numero(
          item.participacionAcumulada
        ),

        this.numero(
          item.tasaPonderada
        ),

        this.numero(
          item.plazoPonderadoMeses
        )

      ]
    );
  }


  // =========================================================
  // FILAS DETALLE
  // =========================================================

  private construirDetalle(
    detalle: ConcentracionCdatDetalle[]
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
          item.codigoCdat
        ),

        this.texto(
          item.codigoAgencia
        ),

        this.texto(
          item.nombreAgencia
        ),

        this.texto(
          item.fechaApertura
        ),

        this.texto(
          item.fechaVencimiento
        ),

        this.numero(
          item.plazoMeses
        ),

        this.numero(
          item.plazoDias
        ),

        this.numero(
          item.tasaNominalAnual
        ),

        this.numero(
          item.valorApertura
        ),

        this.numero(
          item.saldoActual
        ),

        this.numero(
          item.participacionDepositante
        )

      ]
    );
  }


  // =========================================================
  // TOTALES
  // =========================================================

  private totalRanking(
    ranking: ConcentracionCdatDepositante[]
  ): number {

    return (ranking || [])
      .reduce(
        (total, item) =>
          total
          + Number(
            item.saldoTotal || 0
          ),
        0
      );
  }


  // =========================================================
  // APOYO
  // =========================================================

  private numero(
    valor: number | null | undefined
  ): number {

    return Number(
      valor ?? 0
    );
  }


  private texto(
    valor: string | null | undefined
  ): string {

    return String(
      valor ?? ''
    );
  }

}
