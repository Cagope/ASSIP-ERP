import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { CajaCompensacionListDTO } from './caja-compensacion.api';

@Injectable({ providedIn: 'root' })
export class CajaCompensacionExporterService {

  exportar(items: CajaCompensacionListDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(x => ({
      'ID': x.idCaja,
      'Nombre Caja': x.nombreCaja,
      'Documento': x.documento ?? '',
      'Activo': x.activo ? 'SI' : 'NO',
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'CAJA_COMPENSACION');
    XLSX.writeFile(wb, 'caja_compensacion.xlsx');
  }
}
