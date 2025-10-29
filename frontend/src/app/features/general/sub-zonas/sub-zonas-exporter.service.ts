import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { SubZona } from './sub-zonas.api';

@Injectable({ providedIn: 'root' })
export class SubZonasExporterService {
  exportarSubZonas(items: SubZona[]): void {
    const data = items.map(i => ({
      Código: i.codigoSubZona,
      Nombre: i.nombreSubZona,
      Zona: i.zona?.nombreZona ?? '',
      Comentario: i.comentarioSubZona ?? ''
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'SubZonas');
    XLSX.writeFile(wb, 'subzonas.xlsx');
  }
}
