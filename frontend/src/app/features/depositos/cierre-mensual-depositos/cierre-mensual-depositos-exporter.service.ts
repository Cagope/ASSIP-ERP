import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../shared/services/excel-export.service';

import {
  CierreMensualDepositosDetalle,
  CierreMensualDepositosPreview,
  CierreMensualDepositosResumenAgencia,
  CierreMensualDepositosResumenForma
} from './cierre-mensual-depositos.api';


@Injectable({
  providedIn: 'root'
})
export class CierreMensualDepositosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }


  // =========================================================
  // EXPORTAR
  // =========================================================

  exportar(
    preview: CierreMensualDepositosPreview,
    fechaCierre: string
  ): void {

    if (!preview?.detalle?.length) {

      alert(
        'No hay información para exportar.'
      );

      return;
    }


    const hojas: ExcelSheetOptions[] = [];


    // =====================================================
    // 1. RESUMEN GENERAL
    // =====================================================

    hojas.push(
      this.crearResumenGeneral(
        preview,
        fechaCierre
      )
    );


    // =====================================================
    // 2. RESUMEN POR AGENCIA
    // =====================================================

    hojas.push(
      this.crearResumenAgencias(
        preview.resumenAgencias || []
      )
    );


    // =====================================================
    // 3. RESUMEN POR AGENCIA + FORMA
    // =====================================================

    hojas.push(
      this.crearResumenFormas(
        preview.resumenFormas || []
      )
    );


    // =====================================================
    // 4. DETALLE GENERAL
    // =====================================================

    hojas.push(
      this.crearDetalleGeneral(
        preview
      )
    );


    // =====================================================
    // 5. DETALLE POR AGENCIA
    // =====================================================

    for (
      const agencia
      of preview.resumenAgencias || []
    ) {

      hojas.push(
        this.crearDetalleAgencia(
          preview,
          agencia
        )
      );
    }


    // =====================================================
    // 6. DETALLE POR AGENCIA + FORMA
    // =====================================================

    for (
      const forma
      of preview.resumenFormas || []
    ) {

      hojas.push(
        this.crearDetalleForma(
          preview,
          forma
        )
      );
    }


    // =====================================================
    // 7. GENERAR ARCHIVO
    // =====================================================

    this.excelExport.exportar({

      nombreArchivo:
        `cierre-mensual-depositos-${fechaCierre || 'proceso'}.xlsx`,

      hojas

    });
  }


  // =========================================================
  // RESUMEN GENERAL
  // =========================================================

  private crearResumenGeneral(
    preview: CierreMensualDepositosPreview,
    fechaCierre: string
  ): ExcelSheetOptions {

    const r =
      preview.resumen;


    return {

      nombreHoja:
        'Resumen',

      titulo:
        'CIERRE MENSUAL DEPÓSITOS',

      filtros: [
        [
          'Fecha cierre',
          fechaCierre || ''
        ]
      ],

      columnas: [
        'Indicador',
        'Valor'
      ],

      filas: [

        [
          'Total cuentas',
          Number(
            r?.totalCuentas || 0
          )
        ],

        [
          'Saldo total',
          Number(
            r?.saldoTotal || 0
          )
        ],

        [
          'Total débitos',
          Number(
            r?.totalDebitos || 0
          )
        ],

        [
          'Total créditos',
          Number(
            r?.totalCreditos || 0
          )
        ],

        [
          'Hombres',
          Number(
            r?.hombres || 0
          )
        ],

        [
          'Mujeres',
          Number(
            r?.mujeres || 0
          )
        ],

        [
          'Jurídicas',
          Number(
            r?.juridicas || 0
          )
        ]

      ],

      anchos: [
        28,
        22
      ]
    };
  }


  // =========================================================
  // RESUMEN POR AGENCIA
  // =========================================================

  private crearResumenAgencias(
    resumenAgencias: CierreMensualDepositosResumenAgencia[]
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'Resumen agencias',

      titulo:
        'RESUMEN POR AGENCIA',

      columnas: [
        'Agencia',
        'Cuentas',
        'Saldo',
        'Débitos',
        'Créditos',
        'Hombres',
        'Mujeres',
        'Jurídicas'
      ],

      filas:
        (resumenAgencias || [])
          .map(
            r => [

              Number(
                r.idAgencia || 0
              ),

              Number(
                r.totalCuentas || 0
              ),

              Number(
                r.saldoTotal || 0
              ),

              Number(
                r.totalDebitos || 0
              ),

              Number(
                r.totalCreditos || 0
              ),

              Number(
                r.hombres || 0
              ),

              Number(
                r.mujeres || 0
              ),

              Number(
                r.juridicas || 0
              )
            ]
          ),

      anchos: [
        12,
        12,
        18,
        18,
        18,
        12,
        12,
        12
      ]
    };
  }


  // =========================================================
  // RESUMEN POR AGENCIA + FORMA
  // =========================================================

  private crearResumenFormas(
    resumenFormas: CierreMensualDepositosResumenForma[]
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'Resumen formas',

      titulo:
        'RESUMEN POR AGENCIA Y FORMA',

      columnas: [
        'Agencia',
        'Código',
        'Forma',
        'Cuentas',
        'Saldo',
        'Débitos',
        'Créditos',
        'Hombres',
        'Mujeres',
        'Jurídicas'
      ],

      filas:
        (resumenFormas || [])
          .map(
            r => [

              Number(
                r.idAgencia || 0
              ),

              r.codigoForma || '',

              r.nombreForma || '',

              Number(
                r.cantidadCuentas || 0
              ),

              Number(
                r.saldoTotal || 0
              ),

              Number(
                r.totalDebitos || 0
              ),

              Number(
                r.totalCreditos || 0
              ),

              Number(
                r.hombres || 0
              ),

              Number(
                r.mujeres || 0
              ),

              Number(
                r.juridicas || 0
              )
            ]
          ),

      anchos: [
        12,
        10,
        28,
        12,
        18,
        18,
        18,
        12,
        12,
        12
      ]
    };
  }


  // =========================================================
  // DETALLE GENERAL
  // =========================================================

  private crearDetalleGeneral(
    preview: CierreMensualDepositosPreview
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'Detalle general',

      titulo:
        'DETALLE GENERAL',

      columnas:
        this.columnasDetalle(),

      filas:
        this.filasDetalle(
          preview.detalle || []
        ),

      anchos:
        this.anchosDetalle()
    };
  }


  // =========================================================
  // DETALLE POR AGENCIA
  // =========================================================

  private crearDetalleAgencia(
    preview: CierreMensualDepositosPreview,
    agencia: CierreMensualDepositosResumenAgencia
  ): ExcelSheetOptions {

    const detalle =
      (preview.detalle || [])
        .filter(
          d =>
            d.idAgencia
            === agencia.idAgencia
        );


    return {

      nombreHoja:
        this.nombreHojaSegura(
          `Agencia-${agencia.idAgencia}`
        ),

      titulo:
        `AGENCIA ${agencia.idAgencia}`,

      columnas:
        this.columnasDetalle(),

      filas:
        this.filasDetalle(
          detalle
        ),

      anchos:
        this.anchosDetalle()
    };
  }


  // =========================================================
  // DETALLE POR AGENCIA + FORMA
  // =========================================================

  private crearDetalleForma(
    preview: CierreMensualDepositosPreview,
    forma: CierreMensualDepositosResumenForma
  ): ExcelSheetOptions {

    const detalle =
      (preview.detalle || [])
        .filter(
          d =>
            d.idAgencia === forma.idAgencia
            &&
            d.codigoForma === forma.codigoForma
        );


    return {

      nombreHoja:
        this.nombreHojaSegura(
          `A${forma.idAgencia}-${forma.codigoForma}`
        ),

      titulo:
        `AGENCIA ${forma.idAgencia} - `
        + `${forma.codigoForma} - ${forma.nombreForma}`,

      columnas:
        this.columnasDetalle(),

      filas:
        this.filasDetalle(
          detalle
        ),

      anchos:
        this.anchosDetalle()
    };
  }


  // =========================================================
  // COLUMNAS DETALLE
  // =========================================================

  private columnasDetalle(): string[] {

    return [
      'Agencia',
      'Forma',
      'Nombre forma',
      'Cuenta',
      'Documento',
      'Nombre',
      'Tipo persona',
      'Género',
      'Estado',
      'Fecha apertura',
      'Saldo cierre',
      'Débitos',
      'Créditos',
      'GMF',
      'Fecha GMF',
      'Plazo',
      'Cuota mensual',
      'Fecha final',
      'Tasa'
    ];
  }


  // =========================================================
  // FILAS DETALLE
  // =========================================================

  private filasDetalle(
    detalle: CierreMensualDepositosDetalle[]
  ): any[][] {

    return (detalle || [])
      .map(
        item => [

          Number(
            item.idAgencia || 0
          ),

          item.codigoForma || '',

          item.nombreForma || '',

          item.codigoCuenta || '',

          item.documento || '',

          item.nombreCompleto || '',

          item.tipoPersona || '',

          item.nombreGenero || '',

          item.estadoCuenta || '',

          item.fechaAperturaCuenta || '',

          Number(
            item.saldoCierre || 0
          ),

          Number(
            item.totalDebitos || 0
          ),

          Number(
            item.totalCreditos || 0
          ),

          item.gmfCuenta || '',

          item.fechaGmf || '',

          Number(
            item.plazo || 0
          ),

          Number(
            item.cuotaMensual || 0
          ),

          item.fechaFinal || '',

          Number(
            item.tasa || 0
          )
        ]
      );
  }


  // =========================================================
  // ANCHOS DETALLE
  // =========================================================

  private anchosDetalle(): number[] {

    return [
      10,
      10,
      28,
      16,
      16,
      42,
      12,
      18,
      10,
      16,
      18,
      18,
      18,
      8,
      16,
      10,
      16,
      16,
      10
    ];
  }


  // =========================================================
  // NOMBRE DE HOJA SEGURO
  // =========================================================

  private nombreHojaSegura(
    nombre: string
  ): string {

    return String(
      nombre || 'Detalle'
    )
      .replace(
        /[\\/?*[\]:]/g,
        ''
      )
      .substring(
        0,
        31
      );
  }

}
