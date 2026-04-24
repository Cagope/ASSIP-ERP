import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class ConsolidadoConceptosInformeExporterService {

  exportar(items: any[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(x => ({
      'Código': x.codigoConcepto,
      'Nombre': x.nombreConcepto,
      'Tipo': x.tipoConcepto,
      'Registros': x.cantidadRegistros,
      'Cantidad Total': x.totalCantidad,
      'Valor Total': x.totalValor
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Consolidado');
    XLSX.writeFile(wb, 'consolidado_conceptos.xlsx');
  }
}
