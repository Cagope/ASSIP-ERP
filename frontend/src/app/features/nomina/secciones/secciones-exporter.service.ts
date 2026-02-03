import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { SeccionNominaDTO } from './secciones.api';

@Injectable({ providedIn: 'root' })
export class SeccionesNominaExporterService {

  exportar(items: SeccionNominaDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(x => ({
      'Código': x.codigo,
      'Nombre Sección': x.nombreSeccion,
      'Activo': x.activo ? 'SI' : 'NO'
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Secciones');
    XLSX.writeFile(wb, 'secciones_nomina.xlsx');
  }
}
