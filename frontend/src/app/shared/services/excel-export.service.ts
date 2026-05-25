import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

export type ExcelValor =
  string
  | number
  | boolean
  | Date
  | null
  | undefined;

export type ExcelFila = ExcelValor[];

export interface ExcelSheetOptions {
  nombreHoja: string;
  titulo?: string;
  subtitulo?: string;
  filtros?: ExcelFila[];
  resumen?: ExcelFila[];
  columnas: string[];
  filas: ExcelFila[];
  anchos?: number[];
}

export interface ExcelExportOptions {
  nombreArchivo: string;
  hojas: ExcelSheetOptions[];
}

@Injectable({
  providedIn: 'root'
})
export class ExcelExportService {

  exportar(
    options: ExcelExportOptions
  ): void {

    if (!options.hojas || options.hojas.length === 0) {
      alert('No hay hojas para exportar.');
      return;
    }

    const workbook =
      XLSX.utils.book_new();

    for (const hoja of options.hojas) {

      if (!hoja.filas || hoja.filas.length === 0) {
        continue;
      }

      const datos =
        this.construirDatosHoja(hoja);

      const worksheet =
        XLSX.utils.aoa_to_sheet(datos);

      worksheet['!cols'] =
        hoja.anchos
          ? hoja.anchos.map(wch => ({ wch }))
          : this.calcularAnchos(datos);

      XLSX.utils.book_append_sheet(
        workbook,
        worksheet,
        this.nombreHoja(hoja.nombreHoja)
      );
    }

    if (workbook.SheetNames.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    XLSX.writeFile(
      workbook,
      this.nombreArchivo(options.nombreArchivo)
    );
  }

  private construirDatosHoja(
    hoja: ExcelSheetOptions
  ): ExcelFila[] {

    const datos: ExcelFila[] = [];

    if (hoja.titulo) {
      datos.push([hoja.titulo]);
    }

    if (hoja.subtitulo) {
      datos.push([hoja.subtitulo]);
    }

    if (hoja.titulo || hoja.subtitulo) {
      datos.push([]);
    }

    if (hoja.filtros && hoja.filtros.length > 0) {
      datos.push(['Filtros']);
      datos.push(...hoja.filtros);
      datos.push([]);
    }

    if (hoja.resumen && hoja.resumen.length > 0) {
      datos.push(['Resumen']);
      datos.push(...hoja.resumen);
      datos.push([]);
    }

    datos.push(hoja.columnas);
    datos.push(...hoja.filas);

    return datos;
  }

  private calcularAnchos(
    datos: ExcelFila[]
  ): XLSX.ColInfo[] {

    const totalColumnas =
      Math.max(
        ...datos.map(fila => fila.length),
        1
      );

    return Array.from({
      length: totalColumnas
    }).map((_, index) => {

      const max =
        Math.max(
          ...datos.map(fila =>
            String(fila[index] ?? '').length
          ),
          10
        );

      return {
        wch: Math.min(max + 2, 50)
      };
    });
  }

  private nombreArchivo(
    value: string
  ): string {

    const limpio =
      (value || 'reporte')
        .trim()
        .replace(/[\\/:*?"<>|]/g, '-');

    return limpio.toLowerCase().endsWith('.xlsx')
      ? limpio
      : `${limpio}.xlsx`;
  }

  private nombreHoja(
    value: string
  ): string {

    const limpio =
      (value || 'Reporte')
        .trim()
        .replace(/[\\/?*[\]:]/g, '')
        .substring(0, 31);

    return limpio || 'Reporte';
  }
}
