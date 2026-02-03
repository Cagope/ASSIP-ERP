import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

export interface KardexActivoExportMeta {
  agencia?: string;
  activo?: string;
  fechaIni?: string | null;
  fechaFin?: string | null;
  idActivoFijo?: number | null;
}

@Injectable({ providedIn: 'root' })
export class KardexActivoExporterService {

  exportar(rows: any[], meta?: KardexActivoExportMeta): void {

    if (!rows || rows.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const fechaGen = new Date();
    const fechaTexto = fechaGen.toLocaleString('es-CO');

    // ============================================================
    // HOJA 1: MOVIMIENTOS (KARDEX)
    // ============================================================
    const data = rows.map(r => ({
      'Fecha': r?.fecha ?? '',
      'Hora': r?.hora ?? '',

      'Tipo Movimiento': r?.codigo_movimiento ?? '',
      'Movimiento': r?.nombre_movimiento ?? '',

      'Tipo Comprobante': r?.tipo_comprobante ?? '',
      'Número Comprobante': r?.numero_comprobante ?? '',

      'Débito': Number(r?.valor_debito ?? 0),
      'Crédito': Number(r?.valor_credito ?? 0),
    }));

    const ws = XLSX.utils.json_to_sheet(data);

    ws['!cols'] = [
      { wch: 12 }, // Fecha
      { wch: 10 }, // Hora
      { wch: 14 }, // Tipo mov
      { wch: 28 }, // Movimiento
      { wch: 18 }, // Tipo comp
      { wch: 18 }, // Numero comp
      { wch: 14 }, // Debito
      { wch: 14 }, // Credito
    ];

    // ============================================================
    // HOJA 2: FILTROS (METADATA)
    // ============================================================
    const metaRows = [
      { 'Campo': 'Agencia', 'Valor': meta?.agencia ?? 'TODAS' },
      { 'Campo': 'Activo', 'Valor': meta?.activo ?? '' },
      { 'Campo': 'Fecha Inicial', 'Valor': meta?.fechaIni ?? '' },
      { 'Campo': 'Fecha Final', 'Valor': meta?.fechaFin ?? '' },
      { 'Campo': 'Generado', 'Valor': fechaTexto },
      { 'Campo': 'Total filas', 'Valor': rows.length }
    ];

    const wsMeta = XLSX.utils.json_to_sheet(metaRows);
    wsMeta['!cols'] = [{ wch: 18 }, { wch: 60 }];

    // ============================================================
    // LIBRO
    // ============================================================
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, wsMeta, 'Filtros');
    XLSX.utils.book_append_sheet(wb, ws, 'Kardex');

    const fileName = this.buildFileName(meta?.agencia, meta?.activo);
    XLSX.writeFile(wb, fileName);
  }

  private buildFileName(agencia?: string, activo?: string): string {

    const ag = (agencia || 'TODAS')
      .toUpperCase()
      .replaceAll(' ', '_')
      .replaceAll('/', '-')
      .replaceAll('.', '');

    const act = (activo || 'ACTIVO')
      .toUpperCase()
      .replaceAll(' ', '_')
      .replaceAll('/', '-')
      .replaceAll('.', '')
      .slice(0, 40);

    const hoy = new Date();
    const yyyy = hoy.getFullYear();
    const mm = String(hoy.getMonth() + 1).padStart(2, '0');
    const dd = String(hoy.getDate()).padStart(2, '0');

    return `KARDEX_ACTIVO_${ag}_${act}_${yyyy}${mm}${dd}.xlsx`;
  }
}
