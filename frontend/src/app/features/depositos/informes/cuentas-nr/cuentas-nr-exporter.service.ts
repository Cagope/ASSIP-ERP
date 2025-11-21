import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class CuentasNRExporterService {

  exportarExcel(items: any[], tipoInforme: 'NUEVAS' | 'RETIRADAS'): void {
    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    // Agrupar por agencia → forma
    const agencias = this.agrupar(items, tipoInforme);

    // Construcción AOA (Array Of Arrays)
    const rows: (string | number | null)[][] = [];

    for (const ag of agencias) {

      // ---------------------------------
      // 🟦 Encabezado de AGENCIA
      // ---------------------------------
      rows.push([`Agencia`, ag.codigoAgencia, ag.nombreAgencia]);
      rows.push([]);

      for (const forma of ag.formas) {

        // ---------------------------------
        // 🟩 Encabezado de FORMA
        // ---------------------------------
        rows.push([`Código Forma`, forma.codigoForma, forma.nombreForma]);
        rows.push([]);

        // ---------------------------------
        // 🟧 Encabezado de columnas
        // ---------------------------------
        const colsNuevas = [
          'Cuenta',
          'Documento',
          'Nombre',
          'Agencia',
          'Forma',
          'Fecha apertura',
          'Saldo inicial'
        ];

        const colsRetiradas = [
          'Cuenta',
          'Documento',
          'Nombre',
          'Agencia',
          'Forma',
          'Fecha retiro',
          'Último saldo'
        ];

        rows.push(tipoInforme === 'NUEVAS' ? colsNuevas : colsRetiradas);

        // ---------------------------------
        // 📄 Detalle de la forma
        // ---------------------------------
        forma.detalle.forEach((d: any) => {
          if (tipoInforme === 'NUEVAS') {
            rows.push([
              d.codigo_cuenta,
              d.documento,
              d.nombre_completo,
              d.agencia,
              d.forma,
              d.fecha_apertura,
              d.saldo_inicial ?? 0
            ]);
          } else {
            rows.push([
              d.codigo_cuenta,
              d.documento,
              d.nombre_completo,
              d.agencia,
              d.forma,
              d.fecha_retiro,
              d.ultimo_saldo ?? 0
            ]);
          }
        });

        // ---------------------------------
        // 🧮 TOTAL POR FORMA
        // ---------------------------------
        const totalForma = forma.detalle.reduce((acc: number, x: any) => {
          const valor = tipoInforme === 'NUEVAS'
            ? (x.saldo_inicial ?? 0)
            : (x.ultimo_saldo ?? 0);

          return acc + valor;
        }, 0);

        rows.push([
          '', '', '', '', '',
          `TOTAL ${forma.nombreForma}`,
          totalForma
        ]);

        rows.push([]);
      }

      // ---------------------------------
      // 🧮 TOTAL POR AGENCIA
      // ---------------------------------
      const totalAgencia = ag.formas
        .flatMap((f: any) => f.detalle)
        .reduce((acc: number, x: any) => {
          const valor = tipoInforme === 'NUEVAS'
            ? (x.saldo_inicial ?? 0)
            : (x.ultimo_saldo ?? 0);

          return acc + valor;
        }, 0);

      rows.push(['', '', '', '', '', 'TOTAL AGENCIA', totalAgencia]);
      rows.push([]);
      rows.push([]);
    }

    // Crear hoja
    const ws: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(rows);

    // Ajuste básico de columnas
    (ws as any)['!cols'] = [
      { wch: 12 }, // Cuenta
      { wch: 14 }, // Documento
      { wch: 32 }, // Nombre
      { wch: 24 }, // Agencia
      { wch: 20 }, // Forma
      { wch: 14 }, // Fecha
      { wch: 16 }  // Saldo
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Cuentas NR');

    XLSX.writeFile(wb, 'cuentas_nuevas_retiradas.xlsx');
  }

  // ---------------------------------------------
  // 🔵 Agrupar por agencia y forma
  // ---------------------------------------------
  private agrupar(items: any[], tipoInforme: string) {
    const map: any = {};

    for (const it of items) {
      const keyAg = it.codigo_agencia || it.codigoAgencia;
      const keyFo = it.codigo_forma || it.codigoForma;

      if (!map[keyAg]) {
        map[keyAg] = {
          codigoAgencia: keyAg,
          nombreAgencia: it.agencia,
          formas: {}
        };
      }

      if (!map[keyAg].formas[keyFo]) {
        map[keyAg].formas[keyFo] = {
          codigoForma: keyFo,
          nombreForma: it.forma,
          detalle: []
        };
      }

      map[keyAg].formas[keyFo].detalle.push(it);
    }

    return Object.values(map)
      .map((ag: any) => ({
        ...ag,
        formas: Object.values(ag.formas)
          .sort((a: any, b: any) =>
            String(a.codigoForma).localeCompare(String(b.codigoForma))
          )
      }))
      .sort((a: any, b: any) =>
        String(a.codigoAgencia).localeCompare(String(b.codigoAgencia))
      );
  }
}
