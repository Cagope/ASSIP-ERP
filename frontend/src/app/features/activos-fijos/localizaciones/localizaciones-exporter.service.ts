import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { LocalizacionListDTO } from './localizaciones.api';
import { GeneralApi } from '../../../shared/general/general.api';

@Injectable({ providedIn: 'root' })
export class LocalizacionesExporterService {

  private agenciasMap = new Map<number, string>();

  constructor(private generalApi: GeneralApi) {
    // cargar agencias una vez
    this.generalApi.listarAgencias().subscribe(data => {
      data.forEach((a: any) => {
        this.agenciasMap.set(a.idAgencia, a.nombreAgencia);
      });
    });
  }

  exportar(items: LocalizacionListDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(l => ({

      'Localización': l.nombre,
      'Teléfono': l.telefono ?? '',

      // 👉 ambos campos
      'ID Agencia': l.idAgencia,
      'Nombre Agencia': this.agenciasMap.get(l.idAgencia) ?? ''

    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Localizaciones');
    XLSX.writeFile(wb, 'localizaciones.xlsx');
  }
}
