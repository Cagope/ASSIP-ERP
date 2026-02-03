import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

export interface ResumenMovimientosExportMeta {
  agencia?: string;
  movimiento?: string;
}

@Injectable({ providedIn: 'root' })
export class ResumenMovimientosExporterService {

  exportar(rows: any[], meta?: ResumenMovimientosExportMeta): void {

    if (!rows || rows.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const fecha = new Date();

    // ============================================================
    // ✅ 1) Construir filas decodificadas
    // ============================================================
    const data = rows.map(r => ({
      'Periodo': this.val(r?.periodo_mes),
      'Id Agencia': this.num(r?.id_agencia),
      'Agencia': this.val(r?.nombre_agencia),

      'Código Movimiento': this.val(r?.codigo_movimiento),
      'Movimiento': this.val(r?.nombre_movimiento),

      'Cantidad Movimientos': this.num(r?.cantidad_movimientos),
      'Cantidad Activos': this.num(r?.cantidad_activos),

      'Total Débito': this.money(r?.total_debito),
      'Total Crédito': this.money(r?.total_credito),
      'Neto': this.money(r?.neto),
    }));

    // ============================================================
    // ✅ 2) Sheet y workbook
    // ============================================================
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(data, { skipHeader: false });

    // ✅ Ajuste de anchos (legible)
    ws['!cols'] = [
      { wch: 12 }, // Periodo
      { wch: 10 }, // Id Agencia
      { wch: 35 }, // Agencia
      { wch: 16 }, // Código Movimiento
      { wch: 25 }, // Movimiento
      { wch: 18 }, // Cant Mov
      { wch: 15 }, // Cant Activos
      { wch: 14 }, // Débito
      { wch: 14 }, // Crédito
      { wch: 14 }, // Neto
    ];

    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Resumen Movimientos');

    // ============================================================
    // ✅ 3) Nombre del archivo + descarga
    // ============================================================
    const nombreArchivo = this.buildFileName(meta?.agencia, meta?.movimiento, fecha);

    XLSX.writeFile(wb, nombreArchivo);
  }

  // ============================================================
  // ✅ Helpers
  // ============================================================
  private val(v: any): string {
    return String(v ?? '').trim();
  }

  private num(v: any): number {
    const n = Number(v ?? 0);
    return isNaN(n) ? 0 : n;
  }

  private money(v: any): number {
    const n = Number(v ?? 0);
    return isNaN(n) ? 0 : Math.round(n * 100) / 100;
  }

  private buildFileName(agencia?: string, movimiento?: string, fecha?: Date): string {

    const ag = (agencia || 'TODAS')
      .toUpperCase()
      .replaceAll(' ', '_')
      .replaceAll('/', '-');

    const mov = (movimiento || 'TODOS')
      .toUpperCase()
      .replaceAll(' ', '_')
      .replaceAll('/', '-')
      .replaceAll('—', '-');

    const hoy = fecha || new Date();
    const yyyy = hoy.getFullYear();
    const mm = String(hoy.getMonth() + 1).padStart(2, '0');
    const dd = String(hoy.getDate()).padStart(2, '0');

    return `resumen_movimientos_${ag}_${mov}_${yyyy}${mm}${dd}.xlsx`;
  }
}
