import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../shared/services/excel-export.service';

import {
  CierreMensualDepositosPreview,
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

  exportar(
    preview: CierreMensualDepositosPreview,
    fechaCierre: string
  ): void {

    if (!preview?.detalle?.length) {
      alert('No hay información para exportar.');
      return;
    }

    const hojas: ExcelSheetOptions[] = [];

    hojas.push(
      this.crearResumenGeneral(
        preview,
        fechaCierre
      )
    );

    hojas.push(
      this.crearResumenFormas(
        preview.resumenFormas || []
      )
    );

    hojas.push(
      this.crearDetalleGeneral(
        preview
      )
    );

    for (const forma of preview.resumenFormas || []) {

      hojas.push(
        this.crearDetalleForma(
          preview,
          forma
        )
      );

    }

    this.excelExport.exportar({

      nombreArchivo:
        `cierre-mensual-depositos-${fechaCierre || 'proceso'}.xlsx`,

      hojas

    });

  }

  private crearResumenGeneral(
    preview: CierreMensualDepositosPreview,
    fechaCierre: string
  ): ExcelSheetOptions {

    const r = preview.resumen;

    return {

      nombreHoja: 'Resumen',

      titulo: 'CIERRE MENSUAL DEPÓSITOS',

      filtros: [
        ['Fecha cierre', fechaCierre || '']
      ],

      columnas: [
        'Indicador',
        'Valor'
      ],

      filas: [

        [
          'Total cuentas',
          Number(r?.totalCuentas || 0)
        ],

        [
          'Saldo total',
          Number(r?.saldoTotal || 0)
        ],

        [
          'Total débitos',
          Number(r?.totalDebitos || 0)
        ],

        [
          'Total créditos',
          Number(r?.totalCreditos || 0)
        ],

        [
          'Total formas',
          Number(r?.totalFormas || 0)
        ],

        [
          'Hombres',
          Number(r?.hombres || 0)
        ],

        [
          'Mujeres',
          Number(r?.mujeres || 0)
        ],

        [
          'Jurídicas',
          Number(r?.juridicas || 0)
        ]

      ],

      anchos: [
        28,
        22
      ]

    };

  }

  private crearResumenFormas(
    resumenFormas: CierreMensualDepositosResumenForma[]
  ): ExcelSheetOptions {

    return {

      nombreHoja: 'Resumen formas',

      titulo: 'RESUMEN POR FORMAS',

      columnas: [
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

      filas: (resumenFormas || []).map(r => [

        r.codigoForma || '',

        r.nombreForma || '',

        Number(r.cantidadCuentas || 0),

        Number(r.saldoTotal || 0),

        Number(r.totalDebitos || 0),

        Number(r.totalCreditos || 0),

        Number(r.hombres || 0),

        Number(r.mujeres || 0),

        Number(r.juridicas || 0)

      ]),

      anchos: [
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

  private crearDetalleGeneral(
    preview: CierreMensualDepositosPreview
  ): ExcelSheetOptions {

    return {

      nombreHoja: 'Detalle general',

      titulo: 'DETALLE GENERAL',

      columnas: this.columnasDetalle(),

      filas: this.filasDetalle(
        preview.detalle || []
      ),

      anchos: this.anchosDetalle()

    };

  }

  private crearDetalleForma(
    preview: CierreMensualDepositosPreview,
    forma: CierreMensualDepositosResumenForma
  ): ExcelSheetOptions {

    const detalle = (preview.detalle || [])
      .filter(
        d => d.codigoForma === forma.codigoForma
      );

    return {

      nombreHoja:
        this.nombreHojaSegura(
          `${forma.codigoForma}-${forma.nombreForma}`
        ),

      titulo:
        `${forma.codigoForma} - ${forma.nombreForma}`,

      columnas: this.columnasDetalle(),

      filas: this.filasDetalle(
        detalle
      ),

      anchos: this.anchosDetalle()

    };

  }

  private columnasDetalle(): string[] {

    return [
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

  private filasDetalle(
    detalle: any[]
  ): any[][] {

    return (detalle || []).map(item => [

      item.codigoForma || '',

      item.nombreForma || '',

      item.codigoCuenta || '',

      item.documento || '',

      item.nombreCompleto || '',

      item.tipoPersona || '',

      item.nombreGenero || '',

      item.estadoCuenta || '',

      item.fechaAperturaCuenta || '',

      Number(item.saldoCierre || 0),

      Number(item.totalDebitos || 0),

      Number(item.totalCreditos || 0),

      item.gmfCuenta || '',

      item.fechaGmf || '',

      Number(item.plazo || 0),

      Number(item.cuotaMensual || 0),

      item.fechaFinal || '',

      Number(item.tasa || 0)

    ]);

  }

  private anchosDetalle(): number[] {

    return [
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

  private nombreHojaSegura(
    nombre: string
  ): string {

    return String(nombre || 'Detalle')
      .replace(/[\\/?*[\]:]/g, '')
      .substring(0, 31);

  }

}
