import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { Parametro } from './parametros.api';

@Injectable({ providedIn: 'root' })
export class ParametrosExporterService {
  exportarParametros(items: Parametro[]): void {
    const data = items.map(i => ({
      Agencia: i.idAgencia,
      Código: i.codigoParametro,
      Nombre: i.nombreParametro,
      Valor: i.valorParametro,
      'Tipo de Valor': i.tipoValor ? 'Porcentaje' : 'Valor'
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Parámetros');
    XLSX.writeFile(wb, 'parametros.xlsx');
  }
}
