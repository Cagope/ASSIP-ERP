import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class RevalorizacionExporterService {

  exportarExcel(items: any[], filtros: any): void {
    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const data = items.map((x: any) => ({
      'Tipo Documento': x.tipoDocumento,
      'Documento': x.documento,
      'Nombre Completo': x.nombreCompleto,
      'Saldo Actual': x.saldoActual,
      'Valor Promedio': x.valorPromedio,
      'Revalorización': x.valorRevalorizacion,
      'Estado Cuenta': x.estadoCuenta
    }));

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(data);
    XLSX.utils.book_append_sheet(wb, ws, 'Revalorizacion');

    // -----------------------------------------
    //  📄 Nombre archivo: revalorizacion_xxx_01
    // -----------------------------------------
    const fecha = `${filtros.fechaInicio}_al_${filtros.fechaFin}`;

    // soporte para: filtros.codigoAgencia o filtros.agenciaId
    const codAgencia =
      filtros.codigoAgencia
        ? filtros.codigoAgencia.padStart(2, '0')
        : (filtros.agenciaId === '0'
            ? '00'
            : String(filtros.agenciaId).padStart(2, '0')
          );

    XLSX.writeFile(wb, `revalorizacion_${fecha}_${codAgencia}.xlsx`);
  }
}
