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
      anio: number;
      mes: number;
      numeroPeriodo: number;
      codigoAgencia: string | number;
      fechaContabilizacion: string | null;
      tipoComprobante: string;
      numeroComprobante: string;
    }
  ): void {

    if (!movimientos || movimientos.length === 0) return;

    const mes2 = String(params.mes).padStart(2, '0');
    const agencia2 = String(params.codigoAgencia).padStart(2, '0');
    const periodoTexto = `${params.anio}-${mes2}-${params.numeroPeriodo}`;

    const header = [
      ['COMPROBANTE CONTABLE NÓMINA'],
      [`Período nómina: ${periodoTexto}`],
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

      'Base movimiento': Number(m.valorBase ?? 0),

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

    const fileName =
      `comprobante_nomina_periodo_${params.anio}_${mes2}_${params.numeroPeriodo}_${agencia2}.xlsx`;

    XLSX.writeFile(wb, fileName);
  }

}
