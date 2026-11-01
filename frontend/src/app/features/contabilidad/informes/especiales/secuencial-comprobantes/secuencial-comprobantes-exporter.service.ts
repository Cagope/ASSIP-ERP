
import { Injectable, inject } from '@angular/core';
import {
  ExcelExportService,
  ExcelFila,
  ExcelSheetOptions
} from '../../../../../shared/services/excel-export.service';
import {
  SecuencialComprobantesGrupo,
  SecuencialComprobantesResponse
} from './secuencial-comprobantes.models';

/** Información de la agencia utilizada en el encabezado del Excel. */
export interface SecuencialComprobantesAgenciaExportacion {
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Construye la exportación del Secuencial de comprobantes.
 * Mantiene los cortes por tipo, sus subtotales y los totales generales.
 */
@Injectable({ providedIn: 'root' })
export class SecuencialComprobantesExporterService {
  private readonly excel = inject(ExcelExportService);

  /**
   * Exporta el resultado consultado utilizando el servicio compartido.
   * No realiza nuevas consultas ni modifica los datos contables.
   */
  exportar(
    informe: SecuencialComprobantesResponse,
    grupos: SecuencialComprobantesGrupo[],
    agencia: SecuencialComprobantesAgenciaExportacion
  ): void {
    if (!informe.filas.length || !grupos.length) {
      return;
    }

    const etiquetaAgencia =
      `${agencia.codigoAgencia} - ${agencia.nombreAgencia}`;

    const filtros: ExcelFila[] = [
      ['Agencia', etiquetaAgencia],
      ['Fecha inicial', informe.fechaInicial],
      ['Fecha final', informe.fechaFinal]
    ];

    const hoja: ExcelSheetOptions = {
      nombreHoja: 'Secuencial comprobantes',
      titulo: 'SECUENCIAL DE COMPROBANTES',
      filtros,
      resumen: [
        ['Tipos de comprobante', grupos.length],
        ['Cantidad de comprobantes', informe.filas.length],
        ['Total débitos', informe.totalDebitos],
        ['Total créditos', informe.totalCreditos],
        ['Diferencia general', informe.diferencia]
      ],
      columnas: [
        'Tipo de registro',
        'Tipo comprobante',
        'Nombre tipo comprobante',
        'Número comprobante',
        'Fecha',
        'Cantidad',
        'Débitos',
        'Créditos',
        'Diferencia'
      ],
      filas: this.construirFilas(informe, grupos),
      anchos: [
        22, 20, 42, 24, 18, 16, 22, 22, 22
      ]
    };

    this.excel.exportar({
      nombreArchivo:
        `Secuencial_Comprobantes_` +
        `${this.segmentoArchivo(agencia.codigoAgencia)}_` +
        `${informe.fechaInicial}_${informe.fechaFinal}.xlsx`,
      hojas: [hoja]
    });
  }

  /**
   * Construye encabezados, comprobantes, subtotales y total general.
   * Conserva los consecutivos como texto para mantener los ceros iniciales.
   */
  private construirFilas(
    informe: SecuencialComprobantesResponse,
    grupos: SecuencialComprobantesGrupo[]
  ): ExcelFila[] {
    const filas: ExcelFila[] = [];

    for (const grupo of grupos) {
      filas.push([
        'ENCABEZADO TIPO',
        grupo.tipoComprobante,
        grupo.nombreTipoComprobante,
        null,
        null,
        null,
        null,
        null,
        null
      ]);

      for (const comprobante of grupo.comprobantes) {
        filas.push([
          'COMPROBANTE',
          grupo.tipoComprobante,
          grupo.nombreTipoComprobante,
          comprobante.numeroComprobante,
          comprobante.fecha,
          null,
          comprobante.totalDebitos,
          comprobante.totalCreditos,
          comprobante.totalDebitos - comprobante.totalCreditos
        ]);
      }

      filas.push([
        'SUBTOTAL TIPO',
        grupo.tipoComprobante,
        grupo.nombreTipoComprobante,
        null,
        null,
        grupo.cantidadComprobantes,
        grupo.totalDebitos,
        grupo.totalCreditos,
        grupo.diferencia
      ]);

      filas.push([]);
    }

    filas.push([
      'TOTAL GENERAL',
      null,
      null,
      null,
      null,
      informe.filas.length,
      informe.totalDebitos,
      informe.totalCreditos,
      informe.diferencia
    ]);

    return filas;
  }

  /** Limpia el código de agencia para utilizarlo en el nombre del archivo. */
  private segmentoArchivo(valor: string): string {
    return valor.trim().replace(/[^a-zA-Z0-9_-]/g, '_') || 'Agencia';
  }
}
