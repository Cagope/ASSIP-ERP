import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class RevalorizacionExporterService {

  exportarExcel(items: any[], filtros: any): void {
    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    // ============================================================
    // 🧾 Construcción del JSON para Excel (incluye la CÉDULA)
    // ============================================================
    const data = items.map((x: any) => ({
      'Tipo Documento': x.tipoDocumento,
      'Documento': x.documento,          // ✔ CÉDULA
      'Nombre Completo': x.nombreCompleto,
      'Saldo Actual': x.saldoActual,
      'Valor Promedio': x.valorPromedio,
      'Revalorización': x.valorRevalorizacion,
      'Estado Cuenta': x.estadoCuenta
    }));

    // ============================================================
    // 📘 Crear libro y hoja
    // ============================================================
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(data);

    XLSX.utils.book_append_sheet(wb, ws, 'Revalorizacion');

    // ============================================================
    // 📄 Nombre del archivo
    // ============================================================
    const fecha = filtros.fechaInicio + '_al_' + filtros.fechaFin;

    XLSX.writeFile(wb, `revalorizacion_${fecha}.xlsx`);
  }
}
