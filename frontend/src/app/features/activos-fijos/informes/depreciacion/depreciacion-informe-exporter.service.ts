import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

export interface DepreciacionInformeExportMeta {
  agencia?: string;
  fechaIni?: string | null;
  fechaFin?: string | null;
}

@Injectable({ providedIn: 'root' })
export class DepreciacionInformeExporterService {

  // ============================================================
  // ✅ EXPORTAR A EXCEL (XLSX)
  // ============================================================
  exportar(rows: any[], meta?: DepreciacionInformeExportMeta): void {

    if (!rows || rows.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const fechaGen = new Date();
    const fechaTexto = fechaGen.toLocaleString('es-CO');

    // ============================================================
    // ✅ HOJA 1: INFORME DEPRECIACIÓN
    // ============================================================
    const data = rows.map(r => ({
      'Fecha': r?.fecha ?? '',
      'Placa': r?.placa_activo ?? '',
      'Activo': r?.nombre_activo ?? '',

      'Valor adquisición': Number(r?.valor_adquisicion ?? 0),
      'Dep. mensual': Number(r?.valor_depreciacion_mes ?? 0),
      'Dep. acumulada': Number(r?.valor_depreciacion_acumulada ?? 0),
      'Neto': Number(r?.valor_neto ?? 0),
    }));

    const wsDep = XLSX.utils.json_to_sheet(data);

    wsDep['!cols'] = [
      { wch: 12 }, // Fecha
      { wch: 14 }, // Placa
      { wch: 40 }, // Activo
      { wch: 18 }, // Valor adquisición
      { wch: 16 }, // Dep mensual
      { wch: 18 }, // Dep acumulada
      { wch: 14 }, // Neto
    ];

    // ============================================================
    // ✅ HOJA 2: FILTROS
    // ============================================================
    const metaRows = [
      { 'Campo': 'Agencia', 'Valor': meta?.agencia ?? 'TODAS' },
      { 'Campo': 'Fecha Inicial', 'Valor': meta?.fechaIni ?? '' },
      { 'Campo': 'Fecha Final', 'Valor': meta?.fechaFin ?? '' },
      { 'Campo': 'Generado', 'Valor': fechaTexto },
      { 'Campo': 'Total filas', 'Valor': rows.length }
    ];

    const wsMeta = XLSX.utils.json_to_sheet(metaRows);
    wsMeta['!cols'] = [{ wch: 18 }, { wch: 60 }];

    // ============================================================
    // ✅ LIBRO
    // ============================================================
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, wsMeta, 'Filtros');
    XLSX.utils.book_append_sheet(wb, wsDep, 'Depreciacion');

    // ============================================================
    // ✅ NOMBRE ARCHIVO
    // ============================================================
    const fileName = this.buildFileName(meta?.agencia);

    XLSX.writeFile(wb, fileName);
  }

  private buildFileName(agencia?: string): string {

    const ag = (agencia || 'TODAS')
      .toUpperCase()
      .replaceAll(' ', '_')
      .replaceAll('/', '-')
      .replaceAll('.', '');

    const hoy = new Date();
    const yyyy = hoy.getFullYear();
    const mm = String(hoy.getMonth() + 1).padStart(2, '0');
    const dd = String(hoy.getDate()).padStart(2, '0');

    return `DEPRECIACION_${ag}_${yyyy}${mm}${dd}.xlsx`;
  }
}
