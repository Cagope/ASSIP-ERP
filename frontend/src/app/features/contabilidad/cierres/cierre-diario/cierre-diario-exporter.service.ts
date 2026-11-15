import { Injectable, inject } from '@angular/core';
import {
  ExcelExportService,
  ExcelFila,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';
import { CierreDiarioInforme } from './cierre-diario.models';

/** Exporta el informe de cierre diario usando el servicio Excel común del ERP. */
@Injectable({ providedIn: 'root' })
export class CierreDiarioExporterService {
  private readonly excel = inject(ExcelExportService);

  /** Genera tres hojas a partir de la misma respuesta utilizada para imprimir. */
  exportar(informe: CierreDiarioInforme): void {
    if (!informe || !informe.idAgencia || !informe.fecha) {
      throw new Error('No existe un informe diario válido para exportar.');
    }

    const movimientos = [...(informe.movimientos ?? [])].sort((a, b) =>
      a.codigoCuenta.localeCompare(b.codigoCuenta, 'es', { numeric: true }) ||
      a.tipoComprobante.localeCompare(b.tipoComprobante, 'es') ||
      a.numeroComprobante.localeCompare(b.numeroComprobante, 'es', { numeric: true }) ||
      a.idAuxiliarContable - b.idAuxiliarContable
    );
    const cuentas = [...(informe.resumenCuentas ?? [])].sort((a, b) =>
      a.codigoCuenta.localeCompare(b.codigoCuenta, 'es', { numeric: true })
    );

    const totalDetalleDebitos = movimientos.reduce((total, m) => total + m.valorDebito, 0);
    const totalDetalleCreditos = movimientos.reduce((total, m) => total + m.valorCredito, 0);
    const totalCuentasDebitos = cuentas.reduce((total, c) => total + c.totalDebitos, 0);
    const totalCuentasCreditos = cuentas.reduce((total, c) => total + c.totalCreditos, 0);
    const tolerancia = 0.005;
    if (
      movimientos.length !== informe.cantidadMovimientos ||
      Math.abs(totalDetalleDebitos - informe.totalDebitos) > tolerancia ||
      Math.abs(totalDetalleCreditos - informe.totalCreditos) > tolerancia ||
      Math.abs(totalCuentasDebitos - informe.totalDebitos) > tolerancia ||
      Math.abs(totalCuentasCreditos - informe.totalCreditos) > tolerancia
    ) {
      throw new Error('El detalle del informe no coincide con los totales del cierre diario.');
    }

    const agencia = `${informe.codigoAgencia} - ${informe.nombreAgencia}`;
    const filtros: ExcelFila[] = [
      ['Agencia', agencia],
      ['Fecha contable', informe.fecha],
      ['Estado del día', informe.estadoDia === 'C' ? 'CERRADO' : 'ABIERTO'],
      ['Estado del mes', informe.estadoPeriodoMensual]
    ];

    const control: ExcelSheetOptions = {
      nombreHoja: 'Control del informe',
      titulo: 'CONTROL DEL INFORME DE CIERRE DIARIO',
      columnas: ['Concepto', 'Valor'],
      filas: [
        ...filtros,
        ['Fecha de generación', informe.fechaGeneracion],
        ['Cantidad de comprobantes', informe.cantidadComprobantes],
        ['Cantidad de movimientos', informe.cantidadMovimientos],
        ['Total débitos', informe.totalDebitos],
        ['Total créditos', informe.totalCreditos],
        ['Diferencia débitos - créditos', informe.diferencia],
        ['Estados de auxiliares incluidos', 'A y C'],
        ['Verificación del detalle', 'COINCIDE']
      ],
      anchos: [40, 65]
    };

    const resumen: ExcelSheetOptions = {
      nombreHoja: 'Resumen por cuenta',
      titulo: 'CIERRE DIARIO - RESUMEN POR CUENTA',
      filtros,
      columnas: ['Código cuenta', 'Nombre cuenta', 'Movimientos', 'Débitos', 'Créditos'],
      filas: [
        ...cuentas.map((c): ExcelFila => [
          c.codigoCuenta, c.nombreCuenta, c.cantidadMovimientos,
          c.totalDebitos, c.totalCreditos
        ]),
        ['TOTALES GENERALES', '', informe.cantidadMovimientos,
          informe.totalDebitos, informe.totalCreditos]
      ],
      anchos: [22, 55, 18, 24, 24]
    };

    const detalle: ExcelSheetOptions = {
      nombreHoja: 'Detalle de movimientos',
      titulo: 'CIERRE DIARIO - DETALLE DE MOVIMIENTOS',
      filtros,
      columnas: [
        'Tipo comprobante', 'Número comprobante', 'Código cuenta',
        'Nombre cuenta', 'Documento tercero', 'Nombre tercero',
        'Detalle del movimiento', 'Débitos', 'Créditos',
        'Módulo origen', 'Documento origen', 'Documento soporte'
      ],
      filas: [
        ...movimientos.map((m): ExcelFila => [
          m.tipoComprobante, m.numeroComprobante, m.codigoCuenta,
          m.nombreCuenta, m.documentoTercero ?? '', m.nombreTercero ?? '',
          m.detalleMovimiento ?? '', m.valorDebito, m.valorCredito,
          m.origenModulo ?? '', m.documentoOrigen ?? '', m.documentoSoporte ?? ''
        ]),
        ['TOTALES GENERALES', '', '', '', '', '', '',
          informe.totalDebitos, informe.totalCreditos, '', '', '']
      ],
      anchos: [20, 24, 20, 50, 23, 45, 65, 23, 23, 24, 25, 25]
    };

    this.excel.exportar({
      nombreArchivo: `Cierre_Diario_${this.segmento(informe.codigoAgencia)}_${informe.fecha}.xlsx`,
      hojas: [control, resumen, detalle]
    });
  }

  /** Normaliza el código de agencia para formar un nombre de archivo seguro. */
  private segmento(valor: string): string {
    return (valor ?? '').trim().replace(/[^a-zA-Z0-9_-]/g, '_') || 'Agencia';
  }
}
