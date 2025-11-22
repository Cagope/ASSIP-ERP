import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class AportesExporterService {

  // 🔧 Formato NIT para jurídicas
  private formatearNIT(num: string, dv: string | null): string {
    if (!num) return '';
    const limpio = num.padStart(9, '0');
    return `${limpio.substring(0, 3)}-${limpio.substring(3, 6)}-${limpio.substring(6, 9)}-${dv ?? ''}`;
  }

  exportar(lista: any[], fechaCorte: string) {

    if (!lista || lista.length === 0) return;

    // ❌ EXCLUIR REGISTROS CON SALDO 0
    const filtrados = lista.filter(item =>
      item.saldoAportes !== 0
    );

    // ✔ ORDEN EXACTO DEL EXCEL
    const columnas = [
      'TipoIdentificacion',
      'NumeroIdentificacion',
      'SaldoAportes',
      'ValorAporteMensual',
      'AportesOrdinarios',
      'AportesExtraordinarios',
      'ValorRevalorizacion',
      'PromedioDiaAnual',
      'FechaUltimoPago',
      'NombreCompleto',
      'CodigoCuenta',
      'IdCuentaAhorro'
    ];

    const datos = filtrados.map(item => {

      const esJuridica = item.tipoIdentificacion === 'N';

      return {
        TipoIdentificacion: item.tipoIdentificacion ?? '',

        // 📌 Jurídicas se formatean
        NumeroIdentificacion: esJuridica
          ? this.formatearNIT(item.numeroIdentificacion, item.digitoVerificacion)
          : item.numeroIdentificacion,

        SaldoAportes: item.saldoAportes ?? 0,
        ValorAporteMensual: item.valorAporteMensual ?? 0,
        AportesOrdinarios: item.aportesOrdinarios ?? 0,
        AportesExtraordinarios: item.aportesExtraordinarios ?? 0,
        ValorRevalorizacion: item.valorRevalorizacion ?? 0,
        PromedioDiaAnual: item.promedioDiaAnual ?? 0,

        FechaUltimoPago: item.fechaUltimoPago ?? '',

        NombreCompleto: item.nombreCompleto ?? '',
        CodigoCuenta: item.codigoCuenta ?? '',

        // ✔ ID de la cuenta
        IdCuentaAhorro: item.idCuentaAhorro ?? ''
      };
    });

    const ws = XLSX.utils.json_to_sheet(datos, { header: columnas });

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Aportes SES');

    // ✔ Nombre final con fechaCorte
    const nombre = `aportes_ses_${fechaCorte}.xlsx`;

    XLSX.writeFile(wb, nombre);
  }
}
