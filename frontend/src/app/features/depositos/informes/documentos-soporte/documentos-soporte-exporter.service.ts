import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class DocumentosSoporteExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    items: any[],
    filtros: any
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const agencias =
      this.agrupar(items);

    const hojas: ExcelSheetOptions[] =
      agencias.map(ag =>
        this.crearHojaAgencia(
          ag,
          filtros
        )
      );

    this.excelExport.exportar({
      nombreArchivo:
        `documentos-soporte-${filtros.fechaDesde}-${filtros.fechaHasta}.xlsx`,
      hojas
    });
  }

  private crearHojaAgencia(
    ag: any,
    filtros: any
  ): ExcelSheetOptions {

    const filas: any[][] = [];

    let totalDocumentosAgencia = 0;
    let totalFisicosAgencia = 0;

    for (const forma of ag.formas) {

      filas.push([
        `${forma.codigoForma} - ${forma.nombreForma}`,
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        ''
      ]);

      for (const d of forma.detalle) {

        const cantidad =
          Number(d.cantidadDocumentos || 0);

        filas.push([
          d.codigoCuenta || '',
          d.documento || '',
          d.nombreCompleto || '',
          d.descripcionSoporte || '',
          d.numeroInicial || '',
          d.numeroFinal || '',
          cantidad,
          d.descripcionEstado || '',
          d.fechaEntrega || ''
        ]);

        totalDocumentosAgencia++;
        totalFisicosAgencia += cantidad;
      }

      const totalFisicosForma =
        forma.detalle.reduce(
          (acc: number, x: any) =>
            acc + Number(x.cantidadDocumentos || 0),
          0
        );

      filas.push([
        '',
        '',
        `TOTAL ${forma.nombreForma}`,
        '',
        '',
        '',
        totalFisicosForma,
        '',
        ''
      ]);

      filas.push([]);
    }

    filas.push([
      '',
      '',
      'TOTAL AGENCIA',
      '',
      '',
      '',
      totalFisicosAgencia,
      '',
      ''
    ]);

    filas.push([
      '',
      '',
      'Cantidad registros agencia',
      totalDocumentosAgencia,
      '',
      '',
      '',
      '',
      ''
    ]);

    return {
      nombreHoja:
        this.nombreHojaSegura(
          `${ag.codigoAgencia} ${ag.nombreAgencia}`
        ),

      titulo:
        'INFORME DOCUMENTOS SOPORTE',

      filtros: [
        ['Agencia', `${ag.codigoAgencia} - ${ag.nombreAgencia}`],
        ['Fecha inicial', filtros.fechaDesde || ''],
        ['Fecha final', filtros.fechaHasta || '']
      ],

      columnas: [
        'Cuenta',
        'Documento',
        'Nombre completo',
        'Tipo soporte',
        'Número inicial',
        'Número final',
        'Cantidad',
        'Estado',
        'Fecha entrega'
      ],

      filas,

      anchos: [
        14,
        18,
        38,
        20,
        18,
        18,
        14,
        16,
        16
      ]
    };
  }

  private agrupar(
    items: any[]
  ): any[] {

    const map: any = {};

    for (const it of items) {

      const keyAg =
        it.codigoAgencia || '00';

      const keyFo =
        it.codigoForma || '00';

      if (!map[keyAg]) {
        map[keyAg] = {
          codigoAgencia: it.codigoAgencia || '',
          nombreAgencia: it.nombreAgencia || '',
          formas: {}
        };
      }

      if (!map[keyAg].formas[keyFo]) {
        map[keyAg].formas[keyFo] = {
          codigoForma: it.codigoForma || '',
          nombreForma: it.nombreForma || '',
          detalle: []
        };
      }

      map[keyAg].formas[keyFo].detalle.push(it);
    }

    return Object.values(map)
      .map((ag: any) => ({
        ...ag,
        formas:
          Object.values(ag.formas)
            .sort((a: any, b: any) =>
              String(a.codigoForma)
                .localeCompare(String(b.codigoForma))
            )
      }))
      .sort((a: any, b: any) =>
        String(a.codigoAgencia)
          .localeCompare(String(b.codigoAgencia))
      );
  }

  private nombreHojaSegura(
    nombre: string
  ): string {

    return String(nombre || 'Agencia')
      .replace(/[\\/?*[\]:]/g, '')
      .substring(0, 31);
  }
}
