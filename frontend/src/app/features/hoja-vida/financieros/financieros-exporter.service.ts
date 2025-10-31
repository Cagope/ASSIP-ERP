import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { Financiero } from '../../../shared/models/financiero.model';

/**
 * 💰 Servicio de exportación a Excel — Información Financiera
 * ------------------------------------------------------------
 * Genera un archivo Excel con los datos financieros de los afiliados.
 * Mantiene el formato uniforme del ERP ASSIP.
 */
@Injectable({ providedIn: 'root' })
export class FinancierosExporterService {
  /**
   * 📤 Exporta la lista de registros financieros al formato Excel.
   * Cada fila representa los valores de ingresos, egresos y patrimonio
   * de una persona (relación 1:1 con Datos Personales).
   */
  exportarExcel(financieros: (Financiero & {
    documento?: string;
    nombrePersona?: string;
  })[]): void {
    if (!financieros || financieros.length === 0) {
      alert('⚠️ No hay registros financieros para exportar.');
      return;
    }

    // ==========================================================
    // 🧮 Preparar los datos para exportación
    // ==========================================================
    const financierosDecod = financieros.map(f => ({
      Documento: f.documento ?? '',
      'Nombre Persona': f.nombrePersona ?? '',

      // --- Ingresos ---
      'Salario': f.valorSalario ?? 0,
      'Pensión': f.valorPension ?? 0,
      'Ingresos Arriendo': f.ingresosArriendo ?? 0,
      'Comisiones': f.ingresosComisiones ?? 0,
      'Otros Ingresos': f.otrosIngresos ?? 0,
      'Comentario Otros Ingresos': f.comentarioOtrosIngresos ?? '',
      'Origen de Fondos': f.origenFondos ?? '',
      'Total Ingresos':
        (f.valorSalario ?? 0) +
        (f.valorPension ?? 0) +
        (f.ingresosArriendo ?? 0) +
        (f.ingresosComisiones ?? 0) +
        (f.otrosIngresos ?? 0),

      // --- Egresos ---
      'Egresos Familiares': f.egresosFamiliares ?? 0,
      'Egresos Arriendo': f.egresosArriendo ?? 0,
      'Egresos Crédito': f.egresosCredito ?? 0,
      'Otros Egresos': f.otrosEgresos ?? 0,
      'Comentario Otros Egresos': f.comentarioOtrosEgresos ?? '',
      'Total Egresos':
        (f.egresosFamiliares ?? 0) +
        (f.egresosArriendo ?? 0) +
        (f.egresosCredito ?? 0) +
        (f.otrosEgresos ?? 0),

      // --- Patrimonio ---
      'Total Activos': f.totalActivos ?? 0,
      'Total Pasivos': f.totalPasivos ?? 0,
      'Patrimonio Neto': (f.totalActivos ?? 0) - (f.totalPasivos ?? 0),
      'Deuda Relación Financiera': f.deudaRelacionFinanciera ?? 0,
      'Relación Financiera': f.relacionFinanciera ?? '',

      // --- Auditoría ---
      'Fecha Creación': f.fechaCreacion
        ? new Date(f.fechaCreacion).toLocaleString()
        : '',
      'Fecha Actualización': f.fechaActualizacion
        ? new Date(f.fechaActualizacion).toLocaleString()
        : ''
    }));

    // ==========================================================
    // 📗 Generar archivo Excel
    // ==========================================================
    const ws = XLSX.utils.json_to_sheet(financierosDecod);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Financieros');

    // Ajustar el ancho de las columnas
    (ws as any)['!cols'] = [
      { wch: 14 }, { wch: 26 }, // Documento, Nombre Persona
      { wch: 12 }, { wch: 12 }, { wch: 14 }, { wch: 12 }, { wch: 14 }, // Ingresos
      { wch: 24 }, { wch: 20 }, { wch: 16 },
      { wch: 14 }, { wch: 14 }, { wch: 14 }, { wch: 14 }, { wch: 24 }, { wch: 16 }, // Egresos
      { wch: 14 }, { wch: 14 }, { wch: 14 }, { wch: 20 }, { wch: 20 }, // Patrimonio
      { wch: 20 }, { wch: 20 } // Fechas
    ];

    const fecha = new Date();
    const sufijo = `${fecha.getFullYear()}${String(
      fecha.getMonth() + 1
    ).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;

    XLSX.writeFile(wb, `financieros_${sufijo}.xlsx`);
  }
}
