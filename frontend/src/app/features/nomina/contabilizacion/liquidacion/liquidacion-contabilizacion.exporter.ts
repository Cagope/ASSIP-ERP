import * as XLSX from 'xlsx';
import { LiquidacionMovimientoContableDTO } from './liquidacion-contabilizacion.api';

export class LiquidacionContabilizacionExporter {

  // =========================================================
  // EXPORTAR COMPROBANTE CONTABLE (REPORTE)
  // =========================================================
  static exportarComprobante(
    movimientos: LiquidacionMovimientoContableDTO[],
    params: {
      periodo: number;
      fechaContabilizacion: string | null;
      tipoComprobante: string;
      numeroComprobante: string;
    }
  ): void {

    if (!movimientos || movimientos.length === 0) return;

    const header = [
      ['COMPROBANTE CONTABLE NÓMINA'],
      [`Período nómina: ${params.periodo}`],
      [`Fecha contabilización: ${params.fechaContabilizacion ?? ''}`],
      [`Documento: ${params.tipoComprobante} ${params.numeroComprobante}`],
      []
    ];

    const rows = movimientos.map((m, i) => ({

      '#': i + 1,

      'Agencia': m.idAgencia,

      'Cuenta': m.codigoCuenta,

      'Nombre cuenta': m.nombreCuenta,

      'Tercero': m.nombreTercero ?? '',

      'Empleado referencia': m.nombreEmpleadoReferencia ?? '',

      'Débito': Number(m.debito ?? 0),

      'Crédito': Number(m.credito ?? 0),

      // 🔵 base real del movimiento
      'Base movimiento': Number(m.valorBase ?? 0),

      // 🔵 documento del tercero
      'Documento tercero': m.documentoTercero ?? ''

    }));

    const ws = XLSX.utils.aoa_to_sheet(header);

    XLSX.utils.sheet_add_json(ws, rows, {
      origin: 'A6'
    });

    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(
      wb,
      ws,
      'Comprobante'
    );

    XLSX.writeFile(
      wb,
      `comprobante_nomina_periodo_${params.periodo}.xlsx`
    );
  }

}
