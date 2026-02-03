import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

export interface MovimientosActivosExportMeta {
  agencia?: string;
  activo?: string;
  movimiento?: string;
  fechaIni?: string | null;
  fechaFin?: string | null;

  // ✅ opcional (si quieres llevarlo como dato de control)
  idActivoFijo?: number | null;
}

@Injectable({ providedIn: 'root' })
export class MovimientosActivosExporterService {

  // ============================================================
  // ✅ EXPORTAR A EXCEL (XLSX)
  // ============================================================
  exportar(rows: any[], meta?: MovimientosActivosExportMeta): void {

    if (!rows || rows.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const fechaGen = new Date();
    const fechaTexto = fechaGen.toLocaleString('es-CO');

    // ============================================================
    // ✅ HOJA 1: MOVIMIENTOS
    // ============================================================
    const data = rows.map(r => ({
      'Fecha': r?.fecha ?? '',
      'Hora': r?.hora ?? '',

      'ID Activo': r?.id_activo_fijo ?? '',
      'Placa': r?.placa_activo ?? '',
      'Activo': r?.nombre_activo ?? '',

      'ID Agencia': r?.id_agencia ?? '',
      'Agencia': r?.nombre_agencia ?? '',

      'Tipo Movimiento': r?.codigo_movimiento ?? '',
      'Movimiento': r?.nombre_movimiento ?? '',

      'Tipo Comprobante': r?.tipo_comprobante ?? '',
      'Número Comprobante': r?.numero_comprobante ?? '',

      'Débito': Number(r?.valor_debito ?? 0),
      'Crédito': Number(r?.valor_credito ?? 0),
    }));

    const wsMov = XLSX.utils.json_to_sheet(data);

    // ✅ Anchos recomendados
    wsMov['!cols'] = [
      { wch: 12 }, // Fecha
      { wch: 10 }, // Hora
      { wch: 10 }, // ID Activo
      { wch: 12 }, // Placa
      { wch: 35 }, // Activo
      { wch: 10 }, // ID Agencia
      { wch: 30 }, // Agencia
      { wch: 14 }, // Tipo Mov
      { wch: 22 }, // Movimiento
      { wch: 16 }, // Tipo comp
      { wch: 18 }, // Número comp
      { wch: 14 }, // Débito
      { wch: 14 }, // Crédito
    ];

    // ============================================================
    // ✅ HOJA 2: METADATOS (FILTROS)
    // ============================================================
    const metaRows = [
      { 'Campo': 'Agencia', 'Valor': meta?.agencia ?? 'TODAS' },
      { 'Campo': 'Activo', 'Valor': meta?.activo ?? 'TODOS' },
      { 'Campo': 'Movimiento', 'Valor': meta?.movimiento ?? 'TODOS' },
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
    XLSX.utils.book_append_sheet(wb, wsMov, 'Movimientos');

    // ============================================================
    // ✅ NOMBRE ARCHIVO
    // ============================================================
    const fileName = this.buildFileName(meta?.agencia, meta?.activo);

    XLSX.writeFile(wb, fileName);
  }

  // ============================================================
  // ✅ Nombre del archivo
  // ============================================================
  private buildFileName(agencia?: string, activo?: string): string {

    const ag = (agencia || 'TODAS')
      .toUpperCase()
      .replaceAll(' ', '_')
      .replaceAll('/', '-')
      .replaceAll('.', '');

    const act = (activo || 'TODOS')
      .toUpperCase()
      .replaceAll(' ', '_')
      .replaceAll('/', '-')
      .replaceAll('.', '');

    const hoy = new Date();
    const yyyy = hoy.getFullYear();
    const mm = String(hoy.getMonth() + 1).padStart(2, '0');
    const dd = String(hoy.getDate()).padStart(2, '0');

    return `MOVIMIENTOS_ACTIVOS_${ag}_${act}_${yyyy}${mm}${dd}.xlsx`;
  }
}
