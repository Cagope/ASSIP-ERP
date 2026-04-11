import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class NovedadesNominaExporterService {

  exportar(items: any[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(x => ({

      // =========================
      // NOVEDAD
      // =========================
      'ID Novedad': x.idNovedad ?? '',

      // =========================
      // EMPLEADO
      // =========================
      'ID Empleado': x.idEmpleado ?? '',
      'Documento': x.documentoEmpleado ?? '',
      'Nombre Empleado': x.nombreEmpleado ?? '',

      // =========================
      // CONTRATO / CONCEPTO
      // =========================
      'ID Contrato': x.idContrato ?? '',
      'Concepto': x.codigoConcepto ?? '',

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
      // ESTADO / OBSERVACIÓN
      // =========================
      'Estado': x.estado ?? '',
      'Observación': x.observacion ?? '',

      // =========================
      // PERÍODO
      // =========================
      'Año': x.anio ?? '',
      'Mes': x.mes ?? '',
      'Número Período': x.numeroPeriodo ?? '',
      'Tipo Período': x.tipoPeriodo ?? '',

      // =========================
      // AGENCIA
      // =========================
      'Agencia': x.fkAgencia ?? ''

    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Novedades');
    XLSX.writeFile(wb, 'novedades_nomina.xlsx');
  }
}
