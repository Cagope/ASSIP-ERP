import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { TipoComprobante } from './tipos-comprobantes.api';

@Injectable({ providedIn: 'root' })
export class TiposComprobantesExporterService {

  exportar(items: TipoComprobante[]): void {

    const data = items.map(t => ({
      Agencia: t.idAgencia,
      Tipo: t.tipoComprobante,
      Nombre: t.nombreTipoComprobante,
      Consecutivo: t.cscComprobante,
      Activo: t.comprobanteActivo ? 'SI' : 'NO'
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Tipos de Comprobantes');
    XLSX.writeFile(wb, 'tipos_comprobantes.xlsx');
  }
}
