import { Injectable, inject } from '@angular/core';
import { ExcelExportService, ExcelFila, ExcelSheetOptions } from '../../../../../shared/services/excel-export.service';
import { VerificarPartidaDobleResponse } from './verificar-partida-doble.models';

@Injectable({ providedIn: 'root' })
export class VerificarPartidaDobleExporterService {
  private readonly excel = inject(ExcelExportService);

  /** Exporta todos los comprobantes consultados y marca los errores en la columna Estado. */
  exportar(informe: VerificarPartidaDobleResponse, agencia: string): void {
    if (!informe.filas.length) { return; }
    const filas: ExcelFila[] = informe.filas.map(fila => [
      fila.fecha, fila.tipoComprobante, fila.nombreTipoComprobante,
      fila.numeroComprobante, fila.totalDebitos, fila.totalCreditos,
      fila.diferencia, fila.descuadrado ? 'DESCUADRADO' : 'CUADRADO'
    ]);
    const hoja: ExcelSheetOptions = {
      nombreHoja: 'Verificar partida doble',
      titulo: 'VERIFICAR LA PARTIDA DOBLE',
      filtros: [
        ['Agencia', agencia],
        ['Fecha inicial', informe.fechaInicial],
        ['Fecha final', informe.fechaFinal]
      ],
      resumen: [
        ['Comprobantes verificados', informe.cantidadComprobantes],
        ['Cuadrados', informe.cantidadCuadrados],
        ['Descuadrados', informe.cantidadDescuadrados],
        ['Total débitos', informe.totalDebitos],
        ['Total créditos', informe.totalCreditos],
        ['Diferencia general', informe.diferencia]
      ],
      columnas: ['Fecha', 'Tipo', 'Nombre tipo', 'Número comprobante',
        'Débitos', 'Créditos', 'Diferencia', 'Estado'],
      filas,
      anchos: [18, 14, 36, 24, 22, 22, 22, 18]
    };
    this.excel.exportar({
      nombreArchivo: `Verificar_Partida_Doble_${informe.fechaInicial}_${informe.fechaFinal}.xlsx`,
      hojas: [hoja]
    });
  }
}
