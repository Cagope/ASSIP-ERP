import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { Zona } from './zonas.api';

@Injectable({ providedIn: 'root' })
export class ZonasExporterService {
  exportar(data: Zona[]) {
    const hoja = XLSX.utils.json_to_sheet(
      data.map(z => ({
        Código: z.codigoZona,
        Nombre: z.nombreZona,
        Comentario: z.comentarioZona
      }))
    );
    const libro = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(libro, hoja, 'Zonas');
    XLSX.writeFile(libro, 'zonas.xlsx');
  }
}
