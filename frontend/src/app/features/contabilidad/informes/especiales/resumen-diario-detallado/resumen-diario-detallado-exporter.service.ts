
import { Injectable, inject } from '@angular/core';
import {
  ExcelExportService,
  ExcelFila,
  ExcelSheetOptions
} from '../../../../../shared/services/excel-export.service';
import {
  ResumenDiarioDetalladoResponse
} from './resumen-diario-detallado.models';

/** Identificación legible de la agencia consultada. */
export interface ResumenDiarioDetalladoAgenciaExportacion {
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Genera el Excel del Resumen diario detallado.
 * Conserva los movimientos individuales y los totales entregados por el backend.
 */
@Injectable({ providedIn: 'root' })
export class ResumenDiarioDetalladoExporterService {
  private readonly excel = inject(ExcelExportService);

  /**
   * Exporta el resultado consultado sin ejecutar una nueva consulta.
   * Incluye el detalle diario y una hoja de control del período.
   */
  exportar(
    informe: ResumenDiarioDetalladoResponse,
    agencia: ResumenDiarioDetalladoAgenciaExportacion
  ): void {
    if (!informe.dias.length || informe.cantidadMovimientos === 0) {
      return;
    }

    const nombreAgencia =
      `${agencia.codigoAgencia} - ${agencia.nombreAgencia}`;

    const filtros: ExcelFila[] = [
      ['Agencia', nombreAgencia],
      ['Fecha inicial', informe.fechaInicial],
      ['Fecha final', informe.fechaFinal]
    ];

    const hojaDetalle: ExcelSheetOptions = {
      nombreHoja: 'Resumen diario detallado',
      titulo: 'RESUMEN DIARIO DETALLADO',
      subtitulo: 'Movimientos contables agrupados por fecha',
      filtros,
      columnas: [
        'Fecha',
        'Comprobante',
        'Código cuenta',
        'Nombre cuenta',
        'Documento tercero',
        'Nombre tercero',
        'Detalle del movimiento',
        'Débitos',
        'Créditos'
      ],
      filas: this.construirDetalle(informe),
      anchos: [18, 22, 20, 48, 22, 42, 65, 22, 22]
    };

    const hojaControl: ExcelSheetOptions = {
      nombreHoja: 'Control del informe',
      titulo: 'CONTROL DEL RESUMEN DIARIO DETALLADO',
      columnas: ['Concepto', 'Valor'],
      filas: [
        ['Agencia', nombreAgencia],
        ['Fecha inicial', informe.fechaInicial],
        ['Fecha final', informe.fechaFinal],
        ['Cantidad de días con movimientos', informe.dias.length],
        ['Cantidad de movimientos', informe.cantidadMovimientos],
        ['Total débitos', informe.totalDebitos],
        ['Total créditos', informe.totalCreditos],
        ['Diferencia débitos - créditos', informe.diferencia],
        ['Estados incluidos', 'Movimientos activos']
      ],
      anchos: [42, 60]
    };

    this.excel.exportar({
      nombreArchivo:
        `Resumen_Diario_Detallado_` +
        `${this.segmentoArchivo(agencia.codigoAgencia)}_` +
        `${informe.fechaInicial}_${informe.fechaFinal}.xlsx`,
      hojas: [hojaDetalle, hojaControl]
    });
  }

  /**
   * Construye una fila por auxiliar y agrega los subtotales de cada día.
   * Utiliza los importes calculados por el backend sin recalcularlos.
   */
  private construirDetalle(
    informe: ResumenDiarioDetalladoResponse
  ): ExcelFila[] {
    const filas: ExcelFila[] = [];

    for (const dia of informe.dias) {
      filas.push([]);
      filas.push([`FECHA: ${dia.fecha}`]);

      for (const movimiento of dia.movimientos) {
        filas.push([
          movimiento.fecha,
          [
            movimiento.tipoComprobante,
            movimiento.numeroComprobante
          ].filter(Boolean).join(' '),
          movimiento.codigoCuenta,
          movimiento.nombreCuenta,
          movimiento.documentoTercero ?? '',
          movimiento.nombreTercero ?? '',
          movimiento.detalleMovimiento ?? '',
          movimiento.valorDebito,
          movimiento.valorCredito
        ]);
      }

      filas.push([
        `TOTAL DEL DÍA ${dia.fecha}`,
        null,
        null,
        null,
        null,
        null,
        null,
        dia.totalDebitos,
        dia.totalCreditos
      ]);
    }

    filas.push([]);
    filas.push([
      'TOTALES GENERALES',
      null,
      null,
      null,
      null,
      null,
      null,
      informe.totalDebitos,
      informe.totalCreditos
    ]);

    return filas;
  }

  /** Normaliza el código de agencia para el nombre del archivo. */
  private segmentoArchivo(valor: string): string {
    return valor.trim().replace(/[^a-zA-Z0-9_-]/g, '_') || 'Agencia';
  }
}
