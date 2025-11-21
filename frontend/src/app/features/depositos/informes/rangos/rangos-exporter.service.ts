import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({
  providedIn: 'root'
})
export class RangosExporterService {

  exportar(resumen: any[], detalle: any[], request: any): void {

    if (!detalle || detalle.length === 0) {
      console.warn('No hay datos para exportar');
      return;
    }

    // ============================================================
    // HOJA 1 — INFORME COMPLETO CON CORTES
    // ============================================================
    const hoja1 = this.generarHojaCortes(resumen, detalle, request);

    // ============================================================
    // HOJA 2 — RESUMEN GENERAL (el de pantalla)
    // ============================================================
    const hoja2 = this.generarHojaResumen(resumen, request);

    // ============================================================
    // HOJA 3 — RESUMEN POR FORMA
    // ============================================================
    const hoja3 = this.generarHojaPorForma(detalle, resumen, request);

    // ============================================================
    // CREAR LIBRO FINAL
    // ============================================================
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, hoja1, 'Informe');
    XLSX.utils.book_append_sheet(wb, hoja2, 'Resumen General');
    XLSX.utils.book_append_sheet(wb, hoja3, 'Resumen por Forma');

    XLSX.writeFile(wb, 'informe_rangos.xlsx');
  }

  // ============================================================
  // HOJA 1 — CORTES COMPLETOS
  // ============================================================
  private generarHojaCortes(resumen: any[], detalle: any[], request: any): XLSX.WorkSheet {

    const wsData: any[][] = [];
    let fila = 0;

    wsData[fila++] = ['INFORME POR RANGOS – DEPÓSITOS'];
    wsData[fila++] = [`Fecha de corte: ${request.fechaCorte}`];
    wsData[fila++] = [`Agencia: ${request.agencia}`];
    wsData[fila++] = [];
    wsData[fila++] = [];

    const agencias = this.agrupar(detalle, 'codigoAgencia');

    for (const ag of agencias) {

      wsData[fila++] = [`AGENCIA ${ag.clave}`];
      wsData[fila++] = [];

      const datosAgencia = detalle.filter(x => x.codigoAgencia === ag.clave);

      const formas = this.agrupar(datosAgencia, 'codigoForma');

      for (const f of formas) {

        wsData[fila++] = [`FORMA ${f.clave}`];
        wsData[fila++] = [];

        const datosForma = datosAgencia.filter(x => x.codigoForma === f.clave);

        for (let i = 0; i < resumen.length; i++) {

          const rangoFiltro = request.rangos[i];
          const desc = this.descripcionRango(request.tipo, rangoFiltro.desde, rangoFiltro.hasta);

          wsData[fila++] = [`RANGO ${i+1}: ${desc}`];
          wsData[fila++] = ['Documento', 'Nombre', 'Cuenta', 'Saldo', 'Edad', 'Antigüedad'];

          const datosRango = datosForma.filter(x => this.cumpleRango(request.tipo, x, rangoFiltro));

          for (const d of datosRango) {
            wsData[fila++] = [
              d.documento,
              d.nombreCompleto,
              d.codigoCuenta,
              d.saldo,
              d.edadAnios,
              d.antiguedadAnios
            ];
          }

          const totalSaldo = datosRango.reduce((a, b) => a + b.saldo, 0);

          wsData[fila++] = [];
          wsData[fila++] = [`TOTAL RANGO ${i+1}`, datosRango.length, totalSaldo];
          wsData[fila++] = [];
        }

        const totalForma = datosForma.reduce((a,b)=>a+b.saldo,0);

        wsData[fila++] = [];
        wsData[fila++] = [`TOTAL FORMA ${f.clave}`, datosForma.length, totalForma];
        wsData[fila++] = [];
      }

      const totalAg = datosAgencia.reduce((a,b)=>a+b.saldo,0);

      wsData[fila++] = [];
      wsData[fila++] = [`TOTAL AGENCIA ${ag.clave}`, datosAgencia.length, totalAg];
      wsData[fila++] = [];
    }

    const hoja = XLSX.utils.aoa_to_sheet(wsData);
    this.autoAjustarColumnas(hoja);
    return hoja;
  }

  // ============================================================
  // HOJA 2 — RESUMEN GENERAL (pantalla)
  // ============================================================
  private generarHojaResumen(resumen: any[], request: any): XLSX.WorkSheet {

    const datos = resumen.map((r, i) => ({
      Rango: `Rango ${i+1}`,
      Descripcion: this.descripcionRango(request.tipo, request.rangos[i].desde, request.rangos[i].hasta),
      Cuentas: r.cuentas,
      TotalSaldo: r.saldo
    }));

    const hoja = XLSX.utils.json_to_sheet(datos);
    this.autoAjustarColumnas(hoja);

    return hoja;
  }

  // ============================================================
  // HOJA 3 — RESUMEN POR FORMA
  // ============================================================
  private generarHojaPorForma(detalle: any[], resumen: any[], request: any): XLSX.WorkSheet {

    const formas = this.agrupar(detalle, 'codigoForma');

    const salida: any[] = [];

    for (const f of formas) {

      salida.push({ Forma: f.clave, Descripcion: '', Cuentas: '', TotalSaldo: '' });

      const datosForma = detalle.filter(x => x.codigoForma === f.clave);

      for (let i = 0; i < resumen.length; i++) {

        const rf = request.rangos[i];
        const desc = this.descripcionRango(request.tipo, rf.desde, rf.hasta);

        const datosRango = datosForma.filter(x => this.cumpleRango(request.tipo, x, rf));

        const total = datosRango.reduce((a,b)=>a+b.saldo,0);

        salida.push({
          Forma: f.clave,
          Descripcion: `Rango ${i+1}: ${desc}`,
          Cuentas: datosRango.length,
          TotalSaldo: total
        });
      }

      salida.push({});
    }

    const hoja = XLSX.utils.json_to_sheet(salida);
    this.autoAjustarColumnas(hoja);

    return hoja;
  }

  // ============================================================
  // Validaciones y utilidades
  // ============================================================
  private cumpleRango(tipo: string, item: any, rango: any): boolean {
    if (tipo === 'EDAD') return item.edadAnios >= rango.desde && item.edadAnios <= rango.hasta;
    if (tipo === 'SALDO') return item.saldo >= rango.desde && item.saldo <= rango.hasta;
    if (tipo === 'ANTIGUEDAD') return item.antiguedadAnios >= rango.desde && item.antiguedadAnios <= rango.hasta;
    return false;
  }

  private descripcionRango(tipo: string, desde: number, hasta: number): string {
    if (tipo === 'EDAD') return `Edad entre ${desde} y ${hasta} años`;
    if (tipo === 'SALDO') return `Saldo entre ${desde.toLocaleString()} y ${hasta.toLocaleString()}`;
    if (tipo === 'ANTIGUEDAD') return `Antigüedad entre ${desde} y ${hasta} años`;
    return `${desde} - ${hasta}`;
  }

  private agrupar(datos: any[], campo: string) {
    const mapa = new Map<string, { cuentas: number, saldo: number }>();
    for (const x of datos) {
      const c = x[campo];
      if (!mapa.has(c)) mapa.set(c, { cuentas: 0, saldo: 0 });
      mapa.get(c)!.cuentas++;
      mapa.get(c)!.saldo += x.saldo;
    }
    return Array.from(mapa.entries()).map(([clave, v]) => ({ clave, ...v }));
  }

  private autoAjustarColumnas(hoja: XLSX.WorkSheet) {
    const rango = XLSX.utils.decode_range(hoja['!ref'] || '');
    const anchuras: any[] = [];
    for (let C = rango.s.c; C <= rango.e.c; C++) {
      let maxWidth = 12;
      for (let R = rango.s.r; R <= rango.e.r; R++) {
        const celda = hoja[XLSX.utils.encode_cell({ r: R, c: C })];
        if (celda?.v) maxWidth = Math.max(maxWidth, celda.v.toString().length + 2);
      }
      anchuras.push({ wch: maxWidth });
    }
    hoja['!cols'] = anchuras;
  }
}
