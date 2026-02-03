import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { ArlListDTO } from './arl.api';

@Injectable({ providedIn: 'root' })
export class ArlExporterService {

  exportar(items: ArlListDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(x => ({
      'ID': x.idArl,
      'Nombre ARL': x.nombreArl,
      'Documento': x.documento ?? '',
      'Activo': x.activo ? 'SI' : 'NO',
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'ARL');
    XLSX.writeFile(wb, 'arl.xlsx');
  }
}
