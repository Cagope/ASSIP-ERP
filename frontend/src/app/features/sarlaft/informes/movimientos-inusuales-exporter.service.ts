import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class MovimientosInusualesExporterService {

  exportarListado(fecha: string, datos: any[]) {

    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(datos);
    const wb: XLSX.WorkBook = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Movimientos Inusuales');

    XLSX.writeFile(wb, `movimientos_inusuales_${fecha}.xlsx`);
  }
}
