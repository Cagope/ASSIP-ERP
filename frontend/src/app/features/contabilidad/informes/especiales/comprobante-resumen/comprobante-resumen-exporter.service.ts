import { Injectable, inject } from '@angular/core';
import { ExcelExportService, ExcelFila, ExcelSheetOptions } from '../../../../../shared/services/excel-export.service';
import { ComprobanteResumenResponse } from './comprobante-resumen.models';

@Injectable({ providedIn: 'root' })
export class ComprobanteResumenExporterService {
  private readonly excel = inject(ExcelExportService);

  /** Exporta el mismo consolidado consultado, con totales y período. */
  exportar(informe: ComprobanteResumenResponse, agencia: string): void {
    if (!informe.filas.length) { return; }
    const filas: ExcelFila[] = informe.filas.map(fila => [
      fila.codigoCuenta, fila.nombreCuenta, Number(fila.totalDebitos), Number(fila.totalCreditos)
    ]);
    filas.push(['TOTAL DEL INFORME', '', Number(informe.totalDebitos), Number(informe.totalCreditos)]);
    const hoja: ExcelSheetOptions = {
      nombreHoja: 'Comprobante resumen',
      titulo: 'COMPROBANTE RESUMEN',
      subtitulo: agencia,
      filtros: [
        ['Fecha inicial', informe.fechaInicial],
        ['Fecha final', informe.fechaFinal]
      ],
      resumen: [['Cuentas', informe.cantidadCuentas]],
      columnas: ['Código cuenta', 'Nombre cuenta', 'Débitos', 'Créditos'],
      filas,
      anchos: [20, 56, 24, 24]
    };
    this.excel.exportar({
      nombreArchivo: `Comprobante_Resumen_${informe.fechaInicial}_${informe.fechaFinal}.xlsx`,
      hojas: [hoja]
    });
  }
}
