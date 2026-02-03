import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { CargoListDTO } from './cargos.api';

@Injectable({ providedIn: 'root' })
export class CargosExporterService {

  exportar(items: CargoListDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(c => ({

      'ID': c.idCargo,
      'Nombre Cargo': c.nombreCargo,
      'Activo': c.activo ? 'SI' : 'NO',

    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Cargos');
    XLSX.writeFile(wb, 'cargos.xlsx');
  }
}
