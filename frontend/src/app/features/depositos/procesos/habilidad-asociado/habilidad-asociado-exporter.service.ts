import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class HabilidadAsociadoExporterService {

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

    const habiles =
      items.filter(
        x => (x.resultado || '').toUpperCase() === 'HÁBIL'
      );

    const inHabiles =
      items.filter(
        x => (x.resultado || '').toUpperCase() !== 'HÁBIL'
      );

    const fechaInicio =
      filtros.fechaInicio ?? 'sin_fecha_i';

    const fechaFin =
      filtros.fechaFin ?? 'sin_fecha_f';

    const codigoAgencia =
      filtros.agenciaId ?? '00';

    this.excelExport.exportar({

      nombreArchivo:
        `habilidad_asociado_${fechaInicio}_${fechaFin}_${codigoAgencia}.xlsx`,

      hojas: [
        this.crearHojaPorZonaYSubzona(
          'HÁBILES',
          habiles,
          filtros
        ),
        this.crearHojaPorZonaYSubzona(
          'INHÁBILES',
          inHabiles,
          filtros
        )
      ]

    });
  }

  private crearHojaPorZonaYSubzona(
    nombreHoja: string,
    lista: any[],
    filtros: any
  ): ExcelSheetOptions {

    if (!lista || lista.length === 0) {
      return {
        nombreHoja,
        titulo: nombreHoja,
        filtros: this.filtrosHoja(filtros),
        columnas: [
          'Mensaje'
        ],
        filas: [
          [
            `No hay registros para ${nombreHoja}`
          ]
        ],
        anchos: [
          40
        ]
      };
    }

    const filas: any[][] = [];

    const zonasUnicas =
      Array.from(
        new Set(
          lista.map(x => x.zona ?? 'SIN_ZONA')
        )
      );

    let totalGeneral = 0;

    zonasUnicas.forEach(zona => {

      const itemsZona =
        lista.filter(
          x => (x.zona ?? 'SIN_ZONA') === zona
        );

      if (itemsZona.length === 0) {
        return;
      }

      filas.push([
        'Zona',
        zona
      ]);

      filas.push([]);

      const subzonas =
        Array.from(
          new Set(
            itemsZona.map(x => x.subzona ?? 'SIN_SUBZONA')
          )
        );

      let totalZona = 0;

      subzonas.forEach(subzona => {

        const itemsSubzona =
          itemsZona.filter(
            x => (x.subzona ?? 'SIN_SUBZONA') === subzona
          );

        if (itemsSubzona.length === 0) {
          return;
        }

        filas.push([
          'Subzona',
          subzona
        ]);

        filas.push([]);

        for (const r of itemsSubzona) {
          filas.push([
            r.documento || '',
            r.nombre || '',
            Number(r.edad || 0),
            r.tipoPersona === '1'
              ? 'Natural'
              : 'Jurídica',
            Number(r.saldoHoy || 0),
            Number(r.totalAportes || 0),
            r.resultado || ''
          ]);
        }

        filas.push([]);

        filas.push([
          'TOTAL',
          subzona,
          '',
          '',
          '',
          '',
          itemsSubzona.length
        ]);

        filas.push([]);

        totalZona +=
          itemsSubzona.length;
      });

      filas.push([
        'total zona',
        zona,
        '',
        '',
        '',
        '',
        totalZona
      ]);

      filas.push([]);

      totalGeneral +=
        totalZona;
    });

    filas.push([
      'TOTAL GENERAL',
      '',
      '',
      '',
      '',
      '',
      totalGeneral
    ]);

    return {
      nombreHoja,
      titulo: nombreHoja,
      filtros: this.filtrosHoja(filtros),
      columnas: [
        'Documento',
        'Nombre',
        'Edad',
        'Tipo Persona',
        'Saldo Actual',
        'Aportes',
        'Resultado'
      ],
      filas,
      anchos: [
        18,
        42,
        10,
        16,
        18,
        18,
        16
      ]
    };
  }

  private filtrosHoja(
    filtros: any
  ): any[][] {

    return [
      ['Fecha inicio', filtros?.fechaInicio || ''],
      ['Fecha fin', filtros?.fechaFin || ''],
      ['Agencia', filtros?.agenciaId ?? '']
    ];
  }
}
