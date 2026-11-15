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

/**
 * Agencia autorizada que identifica el informe exportado.
 */
export interface LibroDiarioResumidoAgenciaExportacion {
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Genera el Excel del Libro Diario Resumido.
 *
 * Respeta la modalidad consultada y los valores entregados
 * por el backend.
 *
 * Organiza los movimientos con cortes y subtotales por fecha,
 * siguiendo la presentación del informe en pantalla.
 */
@Injectable({
  providedIn: 'root'
})
export class LibroDiarioResumidoExporterService {

  private readonly excel = inject(ExcelExportService);

  /**
   * Exporta el informe vigente con su detalle y controles generales.
   *
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

    // =========================================================
    // HOJA PRINCIPAL - LIBRO DIARIO RESUMIDO
    // =========================================================

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

    // =========================================================
    // HOJA DE CONTROL
    // =========================================================

    const hojaControl: ExcelSheetOptions = {
      nombreHoja: 'Control del informe',
      titulo: 'CONTROL DEL LIBRO DIARIO RESUMIDO',
      columnas: [
        'Concepto',
        'Valor'
      ],
      filas: [
        ['Agencia', nombreAgencia],
        ['Fecha inicial', informe.fechaInicial],
        ['Fecha final', informe.fechaFinal],
        ['Modalidad', nombreModalidad],
        ['Cantidad de días', this.contarDias(informe)],
        ['Cantidad de registros', informe.filas.length],
        ['Total débitos', informe.totalDebitos],
        ['Total créditos', informe.totalCreditos],
        [
          'Diferencia débitos - créditos',
          informe.diferencia
        ],
        [
          'Estados incluidos',
          'A - Activo / C - Contabilizado'
        ]
      ],
      anchos: [40, 60]
    };

    // =========================================================
    // GENERACIÓN DEL ARCHIVO
    // =========================================================

    this.excel.exportar({
      nombreArchivo:
        `Libro_Diario_Resumido_${informe.modalidad}_` +
        `${this.segmentoArchivo(agencia.codigoAgencia)}_` +
        `${informe.fechaInicial}_${informe.fechaFinal}.xlsx`,
      hojas: [
        hojaDetalle,
        hojaControl
      ]
    });
  }

  /**
   * Construye el detalle del informe agrupado por fecha.
   *
   * Para cada día:
   * 1. Agrega el encabezado de fecha.
   * 2. Incorpora los movimientos del día.
   * 3. Calcula los subtotales diarios.
   *
   * Al finalizar agrega los totales generales del backend.
   */
  private construirDetalle(
    informe: LibroDiarioResumidoResponse
  ): ExcelFila[] {

    const filas: ExcelFila[] = [];

    const movimientosOrdenados = [...informe.filas].sort(
      (a, b) => {

        const comparacionFecha =
          a.fecha.localeCompare(b.fecha);

        if (comparacionFecha !== 0) {
          return comparacionFecha;
        }

        return a.codigoAgrupacion.localeCompare(
          b.codigoAgrupacion
        );
      }
    );

    let fechaActual: string | null = null;

    let totalDebitosDia = 0;
    let totalCreditosDia = 0;

    for (const movimiento of movimientosOrdenados) {

      // =======================================================
      // CAMBIO DE FECHA
      // =======================================================

      if (fechaActual !== movimiento.fecha) {

        // Cerrar el día anterior antes de iniciar uno nuevo.
        if (fechaActual !== null) {

          filas.push([
            `TOTAL DEL DÍA ${fechaActual}`,
            null,
            null,
            totalDebitosDia,
            totalCreditosDia,
            totalDebitosDia - totalCreditosDia
          ]);

          filas.push([]);
        }

        // Iniciar el nuevo día.
        fechaActual = movimiento.fecha;

        totalDebitosDia = 0;
        totalCreditosDia = 0;

        filas.push([
          `FECHA: ${fechaActual}`,
          null,
          null,
          null,
          null,
          null
        ]);
      }

      // =======================================================
      // MOVIMIENTO CONTABLE
      // =======================================================

      const debito = movimiento.totalDebitos ?? 0;
      const credito = movimiento.totalCreditos ?? 0;

      filas.push([
        movimiento.fecha,
        movimiento.codigoAgrupacion,
        movimiento.nombreAgrupacion,
        debito,
        credito,
        debito - credito
      ]);

      // =======================================================
      // ACUMULADOS DEL DÍA
      // =======================================================

      totalDebitosDia += debito;
      totalCreditosDia += credito;
    }

    // =========================================================
    // CIERRE DEL ÚLTIMO DÍA
    // =========================================================

    if (fechaActual !== null) {

      filas.push([
        `TOTAL DEL DÍA ${fechaActual}`,
        null,
        null,
        totalDebitosDia,
        totalCreditosDia,
        totalDebitosDia - totalCreditosDia
      ]);

      filas.push([]);
    }

    // =========================================================
    // TOTAL GENERAL
    // =========================================================

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

  /**
   * Cuenta las fechas distintas que tienen movimientos.
   *
   * No incluye días sin registros contables.
   */
  private contarDias(
    informe: LibroDiarioResumidoResponse
  ): number {

    const fechas = new Set(
      informe.filas.map(fila => fila.fecha)
    );

    return fechas.size;
  }

  /**
   * Evita caracteres no permitidos en el nombre del archivo.
   */
  private segmentoArchivo(
    valor: string
  ): string {

    return valor
      .trim()
      .replace(/[^a-zA-Z0-9_-]/g, '_') || 'Agencia';
  }
}
