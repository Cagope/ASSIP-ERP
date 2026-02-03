import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { EpsListDTO } from './eps.api';

@Injectable({ providedIn: 'root' })
export class EpsExporterService {

  exportar(items: EpsListDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(x => ({
      'ID': x.idEps,
      'Nombre EPS': x.nombreEps,
      'Documento': x.documento ?? '',
      'Activo': x.activo ? 'SI' : 'NO',
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'EPS');
    XLSX.writeFile(wb, 'eps.xlsx');
  }
}
