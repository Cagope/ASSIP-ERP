import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

import {
  EstadisticosAsociadosDetalle,
  EstadisticosAsociadosItem,
  EstadisticosAsociadosResumen
} from './estadisticos-asociados.api';

@Injectable({
  providedIn: 'root'
})
export class EstadisticosAsociadosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    resumen: EstadisticosAsociadosResumen | null,
    items: EstadisticosAsociadosItem[],
    detalle: EstadisticosAsociadosDetalle[],
    fechaCorte: string
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const hojas: ExcelSheetOptions[] = [
      this.crearHojaResumen(
        resumen,
        items,
        fechaCorte
      )
    ];

    const grupos =
      Array.from(
        new Set(
          items.map(i => i.grupo)
        )
      );

    for (const grupo of grupos) {

      const detalleGrupo =
        (detalle || []).filter(
          d => d.grupo === grupo
        );

      if (detalleGrupo.length > 0) {
        hojas.push(
          this.crearHojaGrupo(
            grupo,
            detalleGrupo
          )
        );
      }
    }

    this.excelExport.exportar({
      nombreArchivo:
        `estadisticos-asociados-${fechaCorte}.xlsx`,
      hojas
    });
  }

  private crearHojaResumen(
    resumen: EstadisticosAsociadosResumen | null,
    items: EstadisticosAsociadosItem[],
    fechaCorte: string
  ): ExcelSheetOptions {

    return {
      nombreHoja: 'Resumen',
      titulo: 'Estadísticos de asociados',
      filtros: [
        ['Fecha corte', fechaCorte]
      ],
      resumen: [
        [
          'Total asociados',
          Number(resumen?.totalAsociados || 0)
        ],
        [
          'Total aportes',
          Number(resumen?.totalAportes || 0)
        ]
      ],
      columnas: [
        'Grupo',
        'Categoría',
        'Cantidad',
        'Saldo total',
        'Participación %'
      ],
      filas: items.map(item => [
        item.grupo || '',
        item.categoria || '',
        Number(item.cantidad || 0),
        Number(item.saldoTotal || 0),
        Number(item.porcentaje || 0)
      ]),
      anchos: [
        28,
        36,
        14,
        18,
        18
      ]
    };
  }

  private crearHojaGrupo(
    grupo: string,
    detalle: EstadisticosAsociadosDetalle[]
  ): ExcelSheetOptions {

    return {
      nombreHoja: grupo || 'Grupo',
      titulo: grupo || 'Grupo',
      columnas: [
        'Categoría',
        'Documento',
        'Nombre completo',
        'Ciudad',
        'Celular',
        'Correo',
        'Saldo aportes',
        'Género',
        'Estado civil',
        'Cabeza familia',
        'Escolaridad',
        'Tipo vivienda',
        'Ocupación',
        'Sector económico'
      ],
      filas: detalle.map(item => [
        item.categoria || '',
        item.documento || '',
        item.nombreCompleto || '',
        item.ciudad || '',
        item.celular || '',
        item.correo || '',
        Number(item.saldoAportes || 0),
        item.genero || '',
        item.estadoCivil || '',
        item.cabezaFamilia || '',
        item.escolaridad || '',
        item.tipoVivienda || '',
        item.ocupacion || '',
        item.sectorEconomico || ''
      ]),
      anchos: [
        28,
        18,
        42,
        24,
        18,
        34,
        18,
        18,
        22,
        18,
        24,
        24,
        32,
        32
      ]
    };
  }
}
