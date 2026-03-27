import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { ConceptoCuentaContableListDTO } from './concepto-cuentas-contables.api';

@Injectable({ providedIn: 'root' })
export class ConceptoCuentasContablesExporterService {

  exportar(items: ConceptoCuentaContableListDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    // ==========================================
    // DATA (alineado con el LIST + PRINT)
    // ==========================================
    const data = items.map(x => ({
      'Concepto': x.codigoConcepto ?? '',
      'Agencia': x.nombreAgencia ?? '',
      'Cuenta Débito': x.cuentaDebito ?? '',
      'Cuenta Crédito': x.cuentaCredito ?? '',
      'Activo': x.activo ? 'SI' : 'NO',
    }));

    // ==========================================
    // EXCEL
    // ==========================================
    const ws = XLSX.utils.json_to_sheet(data);

    // Auto ancho de columnas
    ws['!cols'] = [
      { wch: 18 }, // Concepto
      { wch: 30 }, // Agencia
      { wch: 35 }, // Cuenta Débito
      { wch: 35 }, // Cuenta Crédito
      { wch: 10 }, // Activo
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'ConceptoCuentasContables');

    XLSX.writeFile(wb, 'concepto_cuentas_contables.xlsx');
  }
}
