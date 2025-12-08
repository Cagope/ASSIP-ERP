import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class InteresDiarioSmExporterService {

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

      'Saldo Mínimo Día': x.saldoMinimoDia,
      'Interés Bruto': x.interesBruto,
      'Retención': x.retencion,
      'Interés Neto': x.interesNeto,

      'Tasa (%)': x.tasaInteres,
      'Tiempo Liquidación': x.tiempoLiquidacion,
      'Mínimo Forma': x.minimoForma,

      'Retención Aplicada': x.aplicaRetencion ? 'Sí' : 'No'
    }));

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(data);
    XLSX.utils.book_append_sheet(wb, ws, 'Interes Diario SM');

    // ============================================================
    // 📄 Nombre del archivo
    // ============================================================
    const fecha = filtros.fechaProceso + '_liq_' + filtros.fechaLiquidacion;
    const codigoAgencia = filtros.codigoAgencia ?? '00';

    const nombreArchivo = `interes_diario_sm_${fecha}_${codigoAgencia}.xlsx`;

    XLSX.writeFile(wb, nombreArchivo);
  }
}
