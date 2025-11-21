import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class InconsistenciasExporterService {

  exportar(items: any[]) {

    const hoja = XLSX.utils.json_to_sheet(items);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, hoja, 'Inconsistencias');

    XLSX.writeFile(wb, `inconsistencias_saldos.xlsx`);
  }
}
