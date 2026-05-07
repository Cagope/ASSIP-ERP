import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class InteresMensualTacExporterService {

  exportarExcel(items: any[], filtros: any): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    // ============================================================
    // 🧾 Construcción del JSON para Excel
    // ============================================================
    const data = items.map((x: any) => ({

      'Documento': x.documento,
      'Nombre Completo': x.nombreCompleto,

      'Promedio Mensual': x.promedioMensual,

      'Interés Bruto': x.interesBruto,
      'Retención': x.retencion,
      'Interés Neto': x.interesNeto,

      'Tasa (%)': x.tasa,
      'Saldo Actual': x.saldoActual,

      'Retención Aplicada':
        x.aplicaRetencion ? 'Sí' : 'No'

    }));

    const wb = XLSX.utils.book_new();

    const ws = XLSX.utils.json_to_sheet(data);

    XLSX.utils.book_append_sheet(
      wb,
      ws,
      'Interes Mensual TAC'
    );

    // ============================================================
    // 📄 Nombre del archivo (MEJORADO)
    // ============================================================

    const fecha =
      filtros.fechaProceso +
      '_liq_' +
      filtros.fechaLiquidacion;

    const codigoAgencia =
      filtros.codigoAgencia ?? '00';

    const codigoForma =
      filtros.codigoForma ?? 'XX';

    const nombreArchivo =
      `interes_mensual_tac_${fecha}_${codigoAgencia}_${codigoForma}.xlsx`;

    XLSX.writeFile(wb, nombreArchivo);
  }

}
