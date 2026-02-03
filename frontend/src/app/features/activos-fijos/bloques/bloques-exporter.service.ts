import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { BloqueListDTO } from './bloques.api';

@Injectable({ providedIn: 'root' })
export class BloquesExporterService {

  exportar(items: BloqueListDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(b => ({

      'Código': b.codigoBloque,
      'Nombre Bloque': b.nombreBloque,
      'Meses Depreciación Defecto': b.mesesDepreciacionDefecto

    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Bloques');
    XLSX.writeFile(wb, 'bloques.xlsx');
  }
}
