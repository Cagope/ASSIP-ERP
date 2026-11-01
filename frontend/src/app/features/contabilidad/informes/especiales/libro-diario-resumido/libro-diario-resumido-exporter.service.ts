
import { Injectable, inject } from '@angular/core';
import {
  ExcelExportService,
  ExcelFila,
  ExcelSheetOptions
} from '../../../../../shared/services/excel-export.service';
import {
  LibroDiarioResumidoResponse,
  LIBRO_DIARIO_RESUMIDO_MODALIDADES
} from './libro-diario-resumido.models';

/** Agencia autorizada que identifica el informe exportado. */
export interface LibroDiarioResumidoAgenciaExportacion {
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Genera el Excel del Libro diario resumido.
 * Respeta la modalidad consultada y los valores entregados por el backend.
 */
@Injectable({ providedIn: 'root' })
export class LibroDiarioResumidoExporterService {
  private readonly excel = inject(ExcelExportService);

  /**
   * Exporta el informe vigente con su detalle y controles generales.
   * No consulta nuevamente ni modifica los movimientos contables.
   */
  exportar(
    informe: LibroDiarioResumidoResponse,
    agencia: LibroDiarioResumidoAgenciaExportacion
  ): void {
    if (!informe.filas.length) {
      return;
    }

    const nombreAgencia =
      `${agencia.codigoAgencia} - ${agencia.nombreAgencia}`;

    const nombreModalidad =
      LIBRO_DIARIO_RESUMIDO_MODALIDADES.find(
        modalidad => modalidad.value === informe.modalidad
      )?.label ?? informe.modalidad;

    const filtros: ExcelFila[] = [
      ['Agencia', nombreAgencia],
      ['Fecha inicial', informe.fechaInicial],
      ['Fecha final', informe.fechaFinal],
      ['Modalidad', nombreModalidad]
    ];

    const hojaDetalle: ExcelSheetOptions = {
      nombreHoja: 'Libro diario resumido',
      titulo: 'LIBRO DIARIO RESUMIDO',
      subtitulo: nombreModalidad,
      filtros,
      columnas: [
        'Fecha',
        informe.modalidad === 'CUENTA'
          ? 'Código cuenta'
          : 'Tipo comprobante',
        informe.modalidad === 'CUENTA'
          ? 'Nombre cuenta'
          : 'Nombre tipo comprobante',
        'Débitos',
        'Créditos',
        'Diferencia'
      ],
      filas: this.construirDetalle(informe),
      anchos: [18, 22, 48, 22, 22, 22]
    };

    const hojaControl: ExcelSheetOptions = {
      nombreHoja: 'Control del informe',
      titulo: 'CONTROL DEL LIBRO DIARIO RESUMIDO',
      columnas: ['Concepto', 'Valor'],
      filas: [
        ['Agencia', nombreAgencia],
        ['Fecha inicial', informe.fechaInicial],
        ['Fecha final', informe.fechaFinal],
        ['Modalidad', nombreModalidad],
        ['Cantidad de registros', informe.filas.length],
        ['Total débitos', informe.totalDebitos],
        ['Total créditos', informe.totalCreditos],
        ['Diferencia débitos - créditos', informe.diferencia],
        ['Estados incluidos', 'Movimientos activos']
      ],
      anchos: [40, 60]
    };

    this.excel.exportar({
      nombreArchivo:
        `Libro_Diario_Resumido_${informe.modalidad}_` +
        `${this.segmentoArchivo(agencia.codigoAgencia)}_` +
        `${informe.fechaInicial}_${informe.fechaFinal}.xlsx`,
      hojas: [hojaDetalle, hojaControl]
    });
  }

  /**
   * Construye las filas agrupadas por fecha y cuenta o comprobante.
   * Agrega los totales generales sin alterar los importes originales.
   */
  private construirDetalle(
    informe: LibroDiarioResumidoResponse
  ): ExcelFila[] {
    const filas: ExcelFila[] = informe.filas.map(fila => [
      fila.fecha,
      fila.codigoAgrupacion,
      fila.nombreAgrupacion,
      fila.totalDebitos,
      fila.totalCreditos,
      fila.totalDebitos - fila.totalCreditos
    ]);

    filas.push([]);
    filas.push([
      'TOTAL GENERAL',
      null,
      null,
      informe.totalDebitos,
      informe.totalCreditos,
      informe.diferencia
    ]);

    return filas;
  }

  /** Evita caracteres no permitidos en el nombre del archivo. */
  private segmentoArchivo(valor: string): string {
    return valor.trim().replace(/[^a-zA-Z0-9_-]/g, '_') || 'Agencia';
  }
}
