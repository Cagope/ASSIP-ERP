import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { FormaAhorro } from './formas-ahorro.api';

@Injectable({ providedIn: 'root' })
export class FormaAhorroExporterService {

  exportar(items: FormaAhorro[]): void {

    const data = items.map(f => ({

      Código: f.codigoForma,
      Nombre: f.nombreForma,
      Consecutivo: f.consecutivoForma,
      'Tipo Captación': f.tipoCaptacion,
      'Tiempo Liquidación': f.tiempoLiquidacion,
      'Cuenta Corto Plazo': f.cuentaFormaCorto,
      'Cuenta Largo Plazo': f.cuentaFormaLargo,
      'Cuenta Gasto': f.cuentaGasto,
      'Cuenta CxP Forma': f.cuentaCxpForma,
      'Cuenta GMF': f.cuentaGmfForma,
      'Tipo Interés': f.tipoInteresForma,
      'Autorizado': f.autorizadoForma ? 'SI' : 'NO',
      'Documento Forma': f.documentoForma,
      'Período Gracia': f.periodoGracia,
      'Valor Mínimo': f.valorMinimo,
      'Tasa Interés (%)': f.tasaInteresForma
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Formas de Ahorro');
    XLSX.writeFile(wb, 'formas_ahorro.xlsx');
  }
}
