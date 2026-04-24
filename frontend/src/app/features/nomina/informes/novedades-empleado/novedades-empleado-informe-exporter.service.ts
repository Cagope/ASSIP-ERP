import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class NovedadesEmpleadoInformeExporterService {

  exportar(items: any[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(x => ({

      // =========================
      // PERÍODO
      // =========================
      'Año': x.anio ?? '',
      'Mes': x.mes ?? '',
      'Período': x.numeroPeriodo ?? '',
      'Tipo Período': x.tipoPeriodo ?? '',

      // =========================
      // EMPLEADO
      // =========================
      'Documento': x.documento ?? '',
      'Empleado': x.nombreEmpleado ?? '',

      // =========================
      // AGENCIA
      // =========================
      'Agencia': `${x.codigoAgencia ?? ''} - ${x.nombreAgencia ?? ''}`,

      // =========================
      // CONCEPTO
      // =========================
      'Código Concepto': x.codigoConcepto ?? '',
      'Nombre Concepto': x.nombreConcepto ?? '',
      'Tipo': x.tipoConcepto ?? '',

      // =========================
      // FECHAS
      // =========================
      'Fecha Inicial': x.fechaInicial ?? '',
      'Fecha Final': x.fechaFinal ?? '',

      // =========================
      // VALORES
      // =========================
      'Cantidad': x.cantidad ?? 0,
      'Valor': x.valor ?? 0,

      // =========================
      // CONTROL
      // =========================
      'Origen': x.origen ?? '',
      'Estado': x.estado ?? '',
      'Observación': x.observacion ?? ''

    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Informe');
    XLSX.writeFile(wb, 'informe_novedades_empleado.xlsx');
  }
}
