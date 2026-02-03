import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { ActivoFijoListDTO } from './activos-fijos.api';

@Injectable({ providedIn: 'root' })
export class ActivosFijosExporterService {

  exportar(items: ActivoFijoListDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(a => ({

      'Placa': a.placaActivo,
      'Nombre Activo': a.nombreActivo,
      'Agencia': a.nombreAgencia,
      'Fecha Ingreso': a.fechaIngreso,
      'Meses Depreciación': a.mesesDepreciacion,
      'Valor Adquisición': a.valorAdquisicion,
      'Valor Mensual Depreciación': a.valorMensual,
      'Estado': a.nombreEstadoActivo

    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Activos Fijos');
    XLSX.writeFile(wb, 'activos_fijos.xlsx');
  }
}
