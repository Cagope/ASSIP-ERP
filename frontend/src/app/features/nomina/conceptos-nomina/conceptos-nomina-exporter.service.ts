import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { ConceptoNominaListDTO } from './conceptos-nomina.api';

@Injectable({ providedIn: 'root' })
export class ConceptosNominaExporterService {

  exportar(items: ConceptoNominaListDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(x => ({
      'Código': x.codigo,
      'Nombre': x.nombre,
      'Tipo': x.tipo,
      'Es fijo': x.esFijo ? 'SI' : 'NO',
      'Activo': x.activo ? 'SI' : 'NO',
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'ConceptosNomina');
    XLSX.writeFile(wb, 'conceptos_nomina.xlsx');
  }
}
