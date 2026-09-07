import {
  Injectable
} from '@angular/core';

import {
  ExcelExportService,
  ExcelFila,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

import {
  ConcentracionDepositosAgencia,
  ConcentracionDepositosAsociado,
  ConcentracionDepositosForma,
  ConcentracionDepositosResponse,
  ConcentracionDepositosTop
} from './concentracion-depositos.models';


@Injectable({
  providedIn: 'root'
})
export class ConcentracionDepositosExporterService {

  constructor(
    private readonly excelExport:
      ExcelExportService
  ) {}


  // =========================================================
  // EXPORTAR
  // =========================================================

  exportar(
    resultado: ConcentracionDepositosResponse,
    nombreAgencia: string,
    nombreFormaAhorro: string
  ): void {

    const fechaCorte =
      resultado.resumen.fechaCorte;

    this.excelExport.exportar({

      nombreArchivo:
        `Concentracion_Captaciones_${fechaCorte}.xlsx`,

      hojas: [

        this.crearHojaResumen(
          resultado,
          nombreAgencia,
          nombreFormaAhorro
        ),

        this.crearHojaAgencias(
          resultado.agencias ?? [],
          fechaCorte,
          nombreAgencia,
          nombreFormaAhorro
        ),

        this.crearHojaFormas(
          resultado.formas,
          fechaCorte,
          nombreAgencia,
          nombreFormaAhorro
        ),

        this.crearHojaAsociados(
          resultado.asociados,
          fechaCorte,
          nombreAgencia,
          nombreFormaAhorro
        )

      ]

    });
  }


  // =========================================================
  // RESUMEN
  // =========================================================

  private crearHojaResumen(
    resultado: ConcentracionDepositosResponse,
    nombreAgencia: string,
    nombreFormaAhorro: string
  ): ExcelSheetOptions {

    const resumen =
      resultado.resumen;

    const concentracion =
      resultado.concentracion ?? [];

    return {

      nombreHoja:
        'RESUMEN',

      titulo:
        'CONCENTRACIÓN DE CAPTACIONES',

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
        ],

        [
          'Forma de ahorro',
          nombreFormaAhorro
        ]

      ],

      resumen: [

        [
          'Captaciones',
          this.numero(
            resumen.saldoCaptaciones
          )
        ],

        [
          'Aportes sociales',
          this.numero(
            resumen.saldoAportes
          )
        ],

        [
          'Total recursos',
          this.numero(
            resumen.saldoTotal
          )
        ],

        [
          'Cantidad cuentas',
          this.numero(
            resumen.cantidadCuentas
          )
        ],

        [
          'Cantidad asociados',
          this.numero(
            resumen.cantidadAsociados
          )
        ],

        [
          'Promedio por cuenta',
          this.numero(
            resumen.saldoPromedioPorCuenta
          )
        ],

        [
          'Promedio por asociado',
          this.numero(
            resumen.saldoPromedioPorAsociado
          )
        ],

        [
          'Mayor saldo asociado',
          this.numero(
            resumen.mayorSaldoAsociado
          )
        ]

      ],

      columnas: [
        'Grupo',
        'Cantidad asociados',
        'Saldo',
        '% Participación'
      ],

      filas:
        this.construirConcentracion(
          concentracion
        ),

      anchos: [
        20,
        22,
        22,
        20
      ]

    };
  }


  // =========================================================
  // AGENCIAS
  // =========================================================

  private crearHojaAgencias(
    agencias: ConcentracionDepositosAgencia[],
    fechaCorte: string,
    nombreAgencia: string,
    nombreFormaAhorro: string
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'POR AGENCIA',

      titulo:
        'CONCENTRACIÓN DE CAPTACIONES - POR AGENCIA',

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
        ],

        [
          'Forma de ahorro',
          nombreFormaAhorro
        ]

      ],

      resumen: [

        [
          'Cantidad agencias',
          agencias.length
        ],

        [
          'Saldo total',
          this.totalAgencias(
            agencias
          )
        ]

      ],

      columnas: [
        'Código',
        'Agencia',
        'Cuentas',
        'Asociados',
        'Saldo',
        '% Participación'
      ],

      filas:
        this.construirAgencias(
          agencias
        ),

      anchos: [
        14,
        36,
        16,
        16,
        22,
        20
      ]

    };
  }


  // =========================================================
  // FORMAS DE AHORRO
  // =========================================================

  private crearHojaFormas(
    formas: ConcentracionDepositosForma[],
    fechaCorte: string,
    nombreAgencia: string,
    nombreFormaAhorro: string
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'FORMAS AHORRO',

      titulo:
        'CONCENTRACIÓN DE CAPTACIONES - FORMAS DE AHORRO',

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
        ],

        [
          'Forma de ahorro',
          nombreFormaAhorro
        ]

      ],

      resumen: [

        [
          'Cantidad formas',
          formas.length
        ],

        [
          'Saldo total',
          this.totalFormas(
            formas
          )
        ]

      ],

      columnas: [
        'Código',
        'Forma de ahorro',
        'Tipo captación',
        'Cuentas',
        'Asociados',
        'Saldo',
        '% Participación'
      ],

      filas:
        this.construirFormas(
          formas
        ),

      anchos: [
        14,
        36,
        18,
        16,
        16,
        22,
        20
      ]

    };
  }


  // =========================================================
  // RANKING ASOCIADOS
  // =========================================================

  private crearHojaAsociados(
    asociados: ConcentracionDepositosAsociado[],
    fechaCorte: string,
    nombreAgencia: string,
    nombreFormaAhorro: string
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'ASOCIADOS',

      titulo:
        'CONCENTRACIÓN DE CAPTACIONES - RANKING DE ASOCIADOS',

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
        ],

        [
          'Forma de ahorro',
          nombreFormaAhorro
        ]

      ],

      resumen: [

        [
          'Cantidad asociados',
          asociados.length
        ],

        [
          'Saldo total',
          this.totalAsociados(
            asociados
          )
        ]

      ],

      columnas: [
        'Posición',
        'Tipo documento',
        'Documento',
        'Asociado',
        'Cuentas',
        'Saldo',
        '% Participación',
        '% Acumulado'
      ],

      filas:
        this.construirAsociados(
          asociados
        ),

      anchos: [
        12,
        18,
        20,
        40,
        14,
        22,
        20,
        18
      ]

    };
  }


  // =========================================================
  // FILAS CONCENTRACIÓN
  // =========================================================

  private construirConcentracion(
    concentracion: ConcentracionDepositosTop[]
  ): ExcelFila[] {

    if (
      !concentracion
      ||
      concentracion.length === 0
    ) {

      return [
        [
          'SIN REGISTROS'
        ]
      ];
    }

    return concentracion.map(
      item => [

        this.nombreGrupo(
          item.grupo
        ),

        this.numero(
          item.cantidadAsociados
        ),

        this.numero(
          item.saldo
        ),

        this.numero(
          item.porcentajeParticipacion
        )

      ]
    );
  }


  // =========================================================
  // FILAS AGENCIAS
  // =========================================================

  private construirAgencias(
    agencias: ConcentracionDepositosAgencia[]
  ): ExcelFila[] {

    if (
      !agencias
      ||
      agencias.length === 0
    ) {

      return [
        [
          'SIN REGISTROS'
        ]
      ];
    }

    return agencias.map(
      agencia => [

        this.texto(
          agencia.codigoAgencia
        ),

        this.texto(
          agencia.nombreAgencia
        ),

        this.numero(
          agencia.cantidadCuentas
        ),

        this.numero(
          agencia.cantidadAsociados
        ),

        this.numero(
          agencia.saldo
        ),

        this.numero(
          agencia.porcentajeParticipacion
        )

      ]
    );
  }


  // =========================================================
  // FILAS FORMAS
  // =========================================================

  private construirFormas(
    formas: ConcentracionDepositosForma[]
  ): ExcelFila[] {

    if (
      !formas
      ||
      formas.length === 0
    ) {

      return [
        [
          'SIN REGISTROS'
        ]
      ];
    }

    return formas.map(
      forma => [

        this.texto(
          forma.codigoForma
        ),

        this.texto(
          forma.nombreForma
        ),

        this.texto(
          forma.tipoCaptacionForma
        ),

        this.numero(
          forma.cantidadCuentas
        ),

        this.numero(
          forma.cantidadAsociados
        ),

        this.numero(
          forma.saldo
        ),

        this.numero(
          forma.porcentajeParticipacion
        )

      ]
    );
  }


  // =========================================================
  // FILAS ASOCIADOS
  // =========================================================

  private construirAsociados(
    asociados: ConcentracionDepositosAsociado[]
  ): ExcelFila[] {

    if (
      !asociados
      ||
      asociados.length === 0
    ) {

      return [
        [
          'SIN REGISTROS'
        ]
      ];
    }

    return asociados.map(
      asociado => [

        this.numero(
          asociado.posicion
        ),

        this.texto(
          asociado.tipoDocumento
        ),

        this.texto(
          asociado.documento
        ),

        this.texto(
          asociado.nombreCompleto
        ),

        this.numero(
          asociado.cantidadCuentas
        ),

        this.numero(
          asociado.saldo
        ),

        this.numero(
          asociado.porcentajeParticipacion
        ),

        this.numero(
          asociado.porcentajeAcumulado
        )

      ]
    );
  }


  // =========================================================
  // TOTALES
  // =========================================================

  private totalAgencias(
    agencias: ConcentracionDepositosAgencia[]
  ): number {

    return agencias.reduce(
      (
        total,
        agencia
      ) =>
        total
        +
        this.numero(
          agencia.saldo
        ),
      0
    );
  }


  private totalFormas(
    formas: ConcentracionDepositosForma[]
  ): number {

    return formas.reduce(
      (
        total,
        forma
      ) =>
        total
        +
        this.numero(
          forma.saldo
        ),
      0
    );
  }


  private totalAsociados(
    asociados: ConcentracionDepositosAsociado[]
  ): number {

    return asociados.reduce(
      (
        total,
        asociado
      ) =>
        total
        +
        this.numero(
          asociado.saldo
        ),
      0
    );
  }


  // =========================================================
  // AUXILIARES
  // =========================================================

  private nombreGrupo(
    grupo: string
  ): string {

    switch (
      grupo
    ) {

      case 'TOP_10':
        return 'Top 10';

      case 'TOP_20':
        return 'Top 20';

      case 'TOP_50':
        return 'Top 50';

      case 'RESTO':
        return 'Resto';

      default:
        return this.texto(
          grupo
        );
    }
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
