import {
  Injectable
} from '@angular/core';

import {
  ExcelExportService,
  ExcelFila,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

import {
  CosechaDetalle,
  CosechaIndicador,
  CosechaResumen
} from './cosechas.models';


@Injectable({
  providedIn: 'root'
})
export class CosechasExporterService {

  constructor(
    private readonly excelExport:
      ExcelExportService
  ) {}


  // =========================================================
  // EXPORTAR ANÁLISIS
  // =========================================================

  exportarAnalisis(
    resumen: CosechaResumen,
    indicador: CosechaIndicador
  ): void {

    const hoja =
      this.crearHojaAnalisis(
        resumen,
        indicador
      );

    this.excelExport.exportar({

      nombreArchivo:
        `Analisis_Cosechas_${resumen.cosechaDesde}_${resumen.cosechaHasta}.xlsx`,

      hojas: [
        hoja
      ]

    });
  }


  // =========================================================
  // EXPORTAR DETALLE
  // =========================================================

  exportarDetalle(
    detalle: CosechaDetalle[],
    cosecha: string,
    fechaCorte: string,
    indicador: string
  ): void {

    const hoja:
      ExcelSheetOptions = {

        nombreHoja:
          'DETALLE',

        titulo:
          'ANÁLISIS DE COSECHAS - DETALLE',

        subtitulo:
          `${cosecha} | ${fechaCorte} | ${indicador}`,

        filtros: [
          [
            'Cosecha',
            cosecha
          ],
          [
            'Fecha corte',
            fechaCorte
          ],
          [
            'Indicador',
            indicador
          ]
        ],

        resumen: [
          [
            'Cantidad créditos',
            detalle.length
          ],
          [
            'Saldo total',
            this.total(
              detalle,
              item =>
                item.saldoCapital
            )
          ]
        ],

        columnas: [
          'Agencia',
          'Código línea',
          'Línea de crédito',
          'Pagaré',
          'Tipo documento',
          'Documento',
          'Nombre asociado',
          'Teléfono',
          'Celular',
          'Correo',
          'Fecha desembolso',
          'Cosecha',
          'Valor inicial',
          'Valor desembolsado',
          'Fecha corte',
          'MOB',
          'Presente corte',
          'Saldo capital',
          'Días mora',
          'Categoría mora',
          'Estado cartera',
          'Edad riesgo inicial',
          'Edad mora',
          'Edad riesgo',
          'Edad PE',
          'Edad homologación',
          'Edad contable',
          'VEA',
          'PI',
          'PDI',
          'Pérdida esperada',
          'Deterioro capital',
          'Deterioro intereses',
          'Deterioro otros',
          'Deterioro total',
          'Mora 30+',
          'Mora 60+',
          'Mora 90+',
          'Mora 180+'
        ],

        filas:
          this.construirDetalle(
            detalle
          ),

        anchos: [
          12,
          14,
          28,
          16,
          14,
          18,
          36,
          18,
          18,
          32,
          16,
          14,
          18,
          20,
          16,
          10,
          14,
          20,
          12,
          16,
          24,
          18,
          14,
          14,
          14,
          20,
          18,
          20,
          14,
          14,
          20,
          20,
          20,
          20,
          20,
          12,
          12,
          12,
          12
        ]

      };


    this.excelExport.exportar({

      nombreArchivo:
        `Cosecha_Detalle_${cosecha}_${fechaCorte}_${indicador}.xlsx`,

      hojas: [
        hoja
      ]

    });
  }


  // =========================================================
  // HOJA MATRIZ
  // =========================================================

  private crearHojaAnalisis(
    resumen: CosechaResumen,
    indicador: CosechaIndicador
  ): ExcelSheetOptions {

    const mobs =
      Array.from(
        {
          length:
            this.numero(
              resumen.maxMob
            ) + 1
        },
        (_, index) =>
          index
      );


    const filas:
      ExcelFila[] =
        resumen.filas.map(
          fila => [

            fila.cosecha,

            fila.cantidadOriginada,

            fila.valorDesembolsadoOriginal,

            ...mobs.map(
              mob => {

                const celda =
                  fila.celdas.find(
                    item =>
                      item.mob === mob
                  );

                return celda
                  ? this.valorIndicador(
                      celda,
                      indicador
                    )
                  : '';
              }
            )

          ]
        );


    return {

      nombreHoja:
        'COSECHAS',

      titulo:
        'ANÁLISIS DE COSECHAS',

      subtitulo:
        `${resumen.cosechaDesde} a ${resumen.cosechaHasta}`,

      filtros: [

        [
          'Cosecha desde',
          resumen.cosechaDesde
        ],

        [
          'Cosecha hasta',
          resumen.cosechaHasta
        ],

        [
          'Corte de análisis',
          resumen.hastaCorte
        ],

        [
          'Agencia',
          resumen.idAgencia ?? 'Todas'
        ],

        [
          'Línea de crédito',
          resumen.idLineaCredito ?? 'Todas'
        ],

        [
          'Indicador',
          this.nombreIndicador(
            indicador
          )
        ]

      ],

      resumen: [

        [
          'Cantidad cosechas',
          resumen.cantidadCosechas
        ],

        [
          'Créditos originados',
          resumen.cantidadCreditosOriginados
        ],

        [
          'Valor inicial total',
          resumen.valorInicialTotal
        ],

        [
          'Valor total desembolsado',
          resumen.valorTotalDesembolsado
        ],

        [
          'MOB máximo',
          resumen.maxMob
        ]

      ],

      columnas: [
        'Cosecha',
        'Créditos originados',
        'Valor desembolsado',
        ...mobs.map(
          mob =>
            `MOB ${mob}`
        )
      ],

      filas,

      anchos: [
        16,
        20,
        22,
        ...mobs.map(
          () =>
            14
        )
      ]

    };
  }


  // =========================================================
  // DETALLE
  // =========================================================

  private construirDetalle(
    detalle: CosechaDetalle[]
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

        item.idAgencia,

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
          item.tipoDocumento
        ),

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
          item.fechaDesembolso
        ),

        this.texto(
          item.cosecha
        ),

        this.numero(
          item.valorInicialCredito
        ),

        this.numero(
          item.valorDesembolsado
        ),

        this.texto(
          item.fechaCorte
        ),

        this.numero(
          item.mob
        ),

        this.siNo(
          item.presenteCorte
        ),

        this.numero(
          item.saldoCapital
        ),

        this.numero(
          item.diasMora
        ),

        this.texto(
          item.categoriaMora
        ),

        this.texto(
          item.descripcionEstadoCartera
          ??
          item.codigoEstadoCartera
        ),

        this.texto(
          item.edadRiesgoInicial
        ),

        this.texto(
          item.edadMora
        ),

        this.texto(
          item.edadRiesgo
        ),

        this.texto(
          item.edadPe
        ),

        this.texto(
          item.edadHomologacion
        ),

        this.texto(
          item.edadContable
        ),

        this.numero(
          item.vea
        ),

        this.numero(
          item.pi
        ),

        this.numero(
          item.pdi
        ),

        this.numero(
          item.perdidaEsperada
        ),

        this.numero(
          item.deterioroCapital
        ),

        this.numero(
          item.deterioroIntereses
        ),

        this.numero(
          item.deterioroOtros
        ),

        this.numero(
          item.deterioroTotal
        ),

        this.siNo(
          item.mora30
        ),

        this.siNo(
          item.mora60
        ),

        this.siNo(
          item.mora90
        ),

        this.siNo(
          item.mora180
        )

      ]
    );
  }


  // =========================================================
  // VALOR INDICADOR
  // =========================================================

  private valorIndicador(
    celda: any,
    indicador: CosechaIndicador
  ): number {

    switch (
      indicador
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
  // AUXILIARES
  // =========================================================

  private nombreIndicador(
    indicador: CosechaIndicador
  ): string {

    switch (
      indicador
    ) {

      case 'MORA_30':
        return 'Mora 30+';

      case 'MORA_60':
        return 'Mora 60+';

      case 'MORA_90':
        return 'Mora 90+';

      case 'MORA_180':
        return 'Mora 180+';

      case 'SALDO_REMANENTE':
      default:
        return 'Saldo remanente';
    }
  }


  private total(
    detalle: CosechaDetalle[],
    obtener:
      (
        item: CosechaDetalle
      ) => number
  ): number {

    return detalle.reduce(
      (
        acumulado,
        item
      ) =>
        acumulado
        +
        this.numero(
          obtener(
            item
          )
        ),
      0
    );
  }


  private siNo(
    valor:
      boolean
      | null
      | undefined
  ): string {

    return valor
      ? 'Sí'
      : 'No';
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
