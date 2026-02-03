import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class MaestroActivosExporterService {

  exportar(rows: any[], nombreAgencia?: string): void {

    if (!rows || rows.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = rows.map(r => {

      // ------------------------------
      // Responsable / Proveedor (Nombre + Documento)
      // ------------------------------
      const responsableNombre = String(r?.nombre_responsable ?? '').trim();
      const responsableDoc = String(r?.documento_responsable ?? '').trim();

      const proveedorNombre = String(r?.nombre_proveedor ?? '').trim();
      const proveedorDoc = String(r?.documento_proveedor ?? '').trim();

      const responsableFull = this.personaConDocumento(responsableNombre, responsableDoc);
      const proveedorFull = this.personaConDocumento(proveedorNombre, proveedorDoc);

      return {

        // ============================================================
        // ✅ IDENTIFICACIÓN DEL ACTIVO
        // ============================================================
        'ID Activo': r?.id_activo_fijo ?? '',
        'Placa': r?.placa_activo ?? '',
        'Nombre Activo': r?.nombre_activo ?? '',

        // ============================================================
        // ✅ FECHAS
        // ============================================================
        'Fecha Ingreso': r?.fecha_ingreso ?? '',
        'Fecha Garantía': r?.fecha_garantia ?? '',
        'Fecha Baja': r?.fecha_baja ?? '',
        'Fecha Última Depreciación': r?.fecha_ultima_depreciacion ?? '',

        // ============================================================
        // ✅ AGENCIA (ID + CÓDIGO + NOMBRE)
        // ============================================================
        'ID Agencia': r?.id_agencia ?? '',
        'Código Agencia': r?.codigo_agencia ?? '',
        'Agencia': r?.nombre_agencia ?? '',

        // ============================================================
        // ✅ ESTADO (ID + CÓDIGO + NOMBRE)
        // ============================================================
        'ID Estado': r?.id_estado_activo ?? '',
        'Código Estado': r?.codigo_estado ?? '',
        'Estado': r?.nombre_estado ?? '',

        // ============================================================
        // ✅ BLOQUE (ID + CÓDIGO + NOMBRE)
        // ============================================================
        'ID Bloque': r?.id_bloque ?? '',
        'Código Bloque': r?.codigo_bloque ?? '',
        'Bloque': r?.nombre_bloque ?? '',

        // ============================================================
        // ✅ LOCALIZACIÓN
        // ============================================================
        'ID Localización': r?.id_localizacion ?? '',
        'Localización': r?.nombre_localizacion ?? '',

        // ============================================================
        // ✅ CLASIFICACIÓN / ADQUISICIÓN
        // ============================================================
        'ID Tipo Adquisición': r?.id_tipo_adquisicion ?? '',
        'Código Tipo Adquisición': r?.codigo_adquisicion ?? '',
        'Tipo Adquisición': r?.nombre_tipo_adquisicion ?? '',

        'ID Forma Depreciación': r?.id_forma_depreciacion ?? '',
        'Código Forma Depreciación': r?.codigo_forma ?? '',
        'Forma Depreciación': r?.nombre_forma ?? '',

        'Meses Depreciación': Number(r?.meses_depreciacion ?? 0),

        // ============================================================
        // ✅ VALORES
        // ============================================================
        'Valor Adquisición': Number(r?.valor_adquisicion ?? 0),
        'Valor Mensual Depreciación': Number(r?.valor_mensual ?? 0),
        'Depreciación Acumulada': Number(r?.valor_depreciacion_acumulada ?? 0),
        'Valor Neto': Number(r?.valor_neto ?? 0),

        // ============================================================
        // ✅ RESPONSABLE / PROVEEDOR
        // ============================================================
        'ID Responsable': r?.id_datos_personal_responsable ?? '',
        'Responsable (Documento)': responsableFull,

        'ID Proveedor': r?.id_datos_personal_proveedor ?? '',
        'Proveedor (Documento)': proveedorFull,

        // ============================================================
        // ✅ CUENTAS CONTABLES (SEPARADAS)
        // ============================================================

        // CTA ACTIVO
        'ID Cta Activo': r?.id_catalogo_cuenta_activo ?? '',
        'Cta Activo Código': r?.codigo_cuenta_activo ?? '',
        'Cta Activo Nombre': r?.nombre_cuenta_activo ?? '',

        // CTA DEPRECIACIÓN
        'ID Cta Depreciación': r?.id_catalogo_cuenta_depreciacion ?? '',
        'Cta Depreciación Código': r?.codigo_cuenta_depreciacion ?? '',
        'Cta Depreciación Nombre': r?.nombre_cuenta_depreciacion ?? '',

        // CTA GASTO
        'ID Cta Gasto': r?.id_catalogo_cuenta_gasto ?? '',
        'Cta Gasto Código': r?.codigo_cuenta_gasto ?? '',
        'Cta Gasto Nombre': r?.nombre_cuenta_gasto ?? '',

        // CTA CONTROL
        'ID Cta Control': r?.id_catalogo_cuenta_control ?? '',
        'Cta Control Código': r?.codigo_cuenta_control ?? '',
        'Cta Control Nombre': r?.nombre_cuenta_control ?? '',
      };
    });

    // ============================================================
    // ✅ Construcción Excel
    // ============================================================
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(data);
    ws['!cols'] = this.autoWidth(data);

    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Maestro Activos');

    // ============================================================
    // ✅ Nombre archivo
    // ============================================================
    const hoy = new Date();
    const yyyy = hoy.getFullYear();
    const mm = String(hoy.getMonth() + 1).padStart(2, '0');
    const dd = String(hoy.getDate()).padStart(2, '0');

    const ag = (nombreAgencia || 'TODAS')
      .replace(/\s+/g, '_')
      .replace(/[^\w\-]/g, '');

    const filename = `maestro_activos_full_${ag}_${yyyy}-${mm}-${dd}.xlsx`;

    XLSX.writeFile(wb, filename);
  }

  // ============================================================
  // ✅ Utils
  // ============================================================
  private personaConDocumento(nombre: string, documento: string): string {
    const n = (nombre || '').trim();
    const d = (documento || '').trim();

    if (!n && !d) return '';
    if (n && d) return `${n} (${d})`;
    return n || d;
  }

  private autoWidth(rows: any[]): { wch: number }[] {
    if (!rows || rows.length === 0) return [];

    const headers = Object.keys(rows[0] || {});
    const widths = headers.map(h => Math.max(h.length, 14));

    for (const row of rows) {
      headers.forEach((h, i) => {
        const val = row?.[h];
        const len = String(val ?? '').length;
        widths[i] = Math.max(widths[i], Math.min(len, 55));
      });
    }

    return widths.map(w => ({ wch: w + 2 }));
  }
}
