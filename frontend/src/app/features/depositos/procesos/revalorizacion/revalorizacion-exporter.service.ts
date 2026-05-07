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
    // 🧾 Construcción del JSON para Excel
    // ============================================================
    const data = items.map((x: any) => ({

      'Documento': x.documento,
      'Nombre Completo': x.nombreCompleto,

      'Saldo Actual': x.saldoActual,
      'Valor Promedio': x.valorPromedio,
      'Valor Revalorización': x.valorRevalorizacion,
      'Nuevo Saldo': x.nuevoSaldo,

      'Tasa (%)': x.tasaRevalorizacion,
      'Tiempo Liquidación': x.tiempoLiquidacion,
      'Mínimo Forma': x.minimoForma,

      'Estado Cuenta': x.estadoCuenta

    }));

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(data);

    XLSX.utils.book_append_sheet(
      wb,
      ws,
      'Revalorizacion Aportes'
    );

    // ============================================================
    // 📄 Nombre del archivo
    // ============================================================
    const fecha =
      filtros.fechaInicio +
      '_al_' +
      filtros.fechaFin;

    const codigoAgencia =
      filtros.codigoAgencia ?? '00';

    const codigoForma =
      filtros.codigoForma ?? 'XX';

    const nombreArchivo =
      `revalorizacion_aportes_${fecha}_${codigoAgencia}_${codigoForma}.xlsx`;

    XLSX.writeFile(wb, nombreArchivo);
  }

}
