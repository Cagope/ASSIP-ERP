import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class DemograficosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarListado(
    fecha: string,
    registros: any[],
    stats: any
  ): void {

    if (!registros || registros.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const hojas: ExcelSheetOptions[] = [
      this.crearHojaDetalle(registros),
      this.crearHojaEstadisticas(stats)
    ];

    this.excelExport.exportar({
      nombreArchivo:
        `informe_demografico_${fecha}.xlsx`,
      hojas
    });
  }

  private crearHojaDetalle(
    registros: any[]
  ): ExcelSheetOptions {

    const columnas =
      Object.keys(registros[0] || {});

    return {
      nombreHoja: 'Detalle',
      titulo: 'INFORME DEMOGRÁFICO - DETALLE',
      columnas,
      filas: registros.map(r =>
        columnas.map(c => r[c])
      ),
      anchos: columnas.map(() => 24)
    };
  }

  private crearHojaEstadisticas(
    stats: any
  ): ExcelSheetOptions {

    const filas: any[][] = [];

    filas.push(
      ['TOTAL ASOCIADOS', Number(stats?.total || 0), ''],
      ['TOTAL APORTES', '', Number(stats?.resumen?.totalAportes || 0)],
      ['PROMEDIO APORTES', '', Number(stats?.resumen?.promedioAportes || 0)],
      []
    );

    this.agregarGrupo(
      filas,
      'DISTRIBUCIÓN POR GÉNERO',
      stats?.genero,
      stats?.aportexGenero
    );

    this.agregarGrupo(
      filas,
      'RANGOS DE EDAD',
      stats?.rangosEdad
    );

    this.agregarGrupo(
      filas,
      'TIPO VIVIENDA',
      stats?.tipoVivienda
    );

    this.agregarGrupo(
      filas,
      'ZONAS',
      stats?.zonas
    );

    this.agregarGrupo(
      filas,
      'SUBZONAS',
      stats?.subzonas
    );

    this.agregarGrupo(
      filas,
      'ESCOLARIDAD',
      stats?.escolaridad
    );

    this.agregarGrupo(
      filas,
      'OCUPACIÓN',
      stats?.ocupacion
    );

    this.agregarGrupo(
      filas,
      'MUNICIPIO (Dirección)',
      stats?.municipio
    );

    return {
      nombreHoja: 'Estadisticas',
      titulo: 'INFORME DEMOGRÁFICO - ESTADÍSTICAS',
      columnas: [
        'Categoría',
        'Cantidad',
        'Valor'
      ],
      filas,
      anchos: [
        38,
        18,
        20
      ]
    };
  }

  private agregarGrupo(
    filas: any[][],
    titulo: string,
    cantidades: any,
    valores?: any
  ): void {

    filas.push([]);
    filas.push([
      `--- ${titulo} ---`,
      '',
      ''
    ]);

    Object.keys(cantidades || {}).forEach(key => {
      filas.push([
        key,
        Number(cantidades[key] || 0),
        valores
          ? Number(valores[key] || 0)
          : ''
      ]);
    });
  }
}
