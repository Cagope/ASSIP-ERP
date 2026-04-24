import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class NovedadesConceptoInformeExporterService {

  exportar(items: any[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(x => ({

      'Año': x.anio,
      'Mes': x.mes,
      'Período': x.numeroPeriodo,

      'Documento': x.documento,
      'Empleado': x.nombreEmpleado,

      'Agencia': `${x.codigoAgencia} - ${x.nombreAgencia}`,

      'Concepto': x.codigoConcepto,
      'Nombre Concepto': x.nombreConcepto,
      'Tipo': x.tipoConcepto,

      'Fecha Inicial': x.fechaInicial,
      'Fecha Final': x.fechaFinal,

      'Cantidad': x.cantidad,
      'Valor': x.valor,

      'Origen': x.origen,
      'Estado': x.estado,
      'Observación': x.observacion

    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Informe');
    XLSX.writeFile(wb, 'informe_novedades_concepto.xlsx');
  }
}
