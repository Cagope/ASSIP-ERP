import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { CesantiasDTO } from './cesantias.api';

@Injectable({ providedIn: 'root' })
export class CesantiasExporterService {

  exportar(items: CesantiasDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(x => ({
      'ID': x.idCesantias ?? '',
      'Nombre Cesantías': x.nombreCesantias ?? '',
      'Documento': x.documento ?? '',
      'Activo': x.activo ? 'SI' : 'NO',
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Cesantías');
    XLSX.writeFile(wb, 'cesantias.xlsx');
  }
}
