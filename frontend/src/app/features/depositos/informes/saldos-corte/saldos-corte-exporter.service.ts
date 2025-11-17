import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class SaldosCorteExporterService {

  exportarExcel(items: any[]): void {
    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    // 1️⃣ Agrupamos por AGENCIA → FORMA usando los códigos
    const agencias = this.agrupar(items);

    // 2️⃣ Construimos todas las filas manualmente (AOA)
    const rows: (string | number | null)[][] = [];

    for (const ag of agencias) {
      // ---- Encabezado de AGENCIA ----
      rows.push([`Agencia`, ag.codigoAgencia, ag.nombreAgencia]);
      rows.push([]); // línea en blanco

      for (const forma of ag.formas) {

        // ---- Encabezado de FORMA ----
        rows.push([`Código Forma`, forma.codigoForma, forma.nombreForma]);
        rows.push([]); // blanco

        // ---- Encabezado de columnas ----
        rows.push([
          'Cuenta',
          'Documento',
          'Nombre',
          'Zona',
          'SubZona',
          'Estado',
          'Saldo a Corte'
        ]);

        // ---- Detalle de la forma ----
        forma.detalle.forEach((d: any) => {
          rows.push([
            d.codigoCuenta,
            d.documento,
            d.nombreCompleto,
            d.zona,
            d.subZona,
            d.estadoCuentaNombre,
            d.saldoCorte ?? 0
          ]);
        });

        // ---- Total por FORMA ----
        const totalForma = forma.detalle.reduce(
          (acc: number, x: any) => acc + (x.saldoCorte ?? 0),
          0
        );

        rows.push([
          '', '', '',
          '', '',
          `TOTAL ${forma.nombreForma}`,
          totalForma
        ]);

        rows.push([]); // separador entre formas
      }

      // ---- Total por AGENCIA (suma de todas las formas) ----
      const totalAgencia = ag.formas
        .flatMap((x: any) => x.detalle)
        .reduce((acc: number, x: any) => acc + (x.saldoCorte ?? 0), 0);

      rows.push([
        '', '', '',
        '', '',
        'TOTAL AGENCIA',
        totalAgencia
      ]);

      rows.push([]); // línea en blanco entre agencias
      rows.push([]);
    }

    // 3️⃣ Crear la hoja usando AOA (sin encabezado automático)
    const ws: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(rows);

    // (opcional) ajustar anchos básicos
    (ws as any)['!cols'] = [
      { wch: 12 }, // Cuenta
      { wch: 14 }, // Documento
      { wch: 32 }, // Nombre
      { wch: 14 }, // Zona
      { wch: 16 }, // SubZona
      { wch: 18 }, // Estado
      { wch: 18 }  // Saldo
    ];

    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Saldos Corte');

    XLSX.writeFile(wb, 'saldos_corte.xlsx');
  }

  // Agrupa items por agencia y forma usando los CÓDIGOS
  private agrupar(items: any[]) {
    const map: any = {};

    for (const it of items) {
      const keyAg = it.codigoAgencia;
      const keyFo = it.codigoForma;

      if (!map[keyAg]) {
        map[keyAg] = {
          codigoAgencia: it.codigoAgencia,
          nombreAgencia: it.agencia,
          formas: {}
        };
      }

      if (!map[keyAg].formas[keyFo]) {
        map[keyAg].formas[keyFo] = {
          codigoForma: it.codigoForma,
          nombreForma: it.forma,
          detalle: []
        };
      }

      map[keyAg].formas[keyFo].detalle.push(it);
    }

    // Convertir a array ordenado por código agencia / forma
    return Object.values(map)
      .map((ag: any) => ({
        ...ag,
        formas: Object.values(ag.formas)
          .sort((a: any, b: any) => String(a.codigoForma).localeCompare(String(b.codigoForma)))
      }))
      .sort((a: any, b: any) =>
        String(a.codigoAgencia).localeCompare(String(b.codigoAgencia))
      );
  }
}
