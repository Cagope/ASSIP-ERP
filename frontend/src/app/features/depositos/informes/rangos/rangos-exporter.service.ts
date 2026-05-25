import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class RangosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    resumen: any[],
    detalle: any[],
    request: any
  ): void {

    if (!detalle || detalle.length === 0) {
      console.warn('No hay datos para exportar');
      return;
    }

    const hojas: ExcelSheetOptions[] = [

      this.crearHojaInforme(
        resumen,
        detalle,
        request
      ),

      this.crearHojaResumen(
        resumen,
        request
      ),

      this.crearHojaPorForma(
        detalle,
        resumen,
        request
      )

    ];

    this.excelExport.exportar({

      nombreArchivo:
        'informe_rangos.xlsx',

      hojas

    });

  }

  // ============================================================
  // HOJA 1 — INFORME COMPLETO
  // ============================================================
  private crearHojaInforme(
    resumen: any[],
    detalle: any[],
    request: any
  ): ExcelSheetOptions {

    const filas: any[][] = [];

    const agencias =
      this.agrupar(detalle, 'codigoAgencia');

    for (const ag of agencias) {

      filas.push([
        `AGENCIA ${ag.clave}`
      ]);

      filas.push([]);

      const datosAgencia =
        detalle.filter(
          (x: any) =>
            x.codigoAgencia === ag.clave
        );

      const formas =
        this.agrupar(
          datosAgencia,
          'codigoForma'
        );

      for (const f of formas) {

        filas.push([
          `FORMA ${f.clave}`
        ]);

        filas.push([]);

        const datosForma =
          datosAgencia.filter(
            (x: any) =>
              x.codigoForma === f.clave
          );

        for (let i = 0; i < resumen.length; i++) {

          const rangoFiltro =
            request.rangos[i];

          const desc =
            this.descripcionRango(
              request.tipo,
              rangoFiltro.desde,
              rangoFiltro.hasta
            );

          filas.push([
            `RANGO ${i + 1}: ${desc}`
          ]);

          filas.push([
            'Documento',
            'Nombre',
            'Cuenta',
            'Saldo',
            'Edad',
            'Antigüedad'
          ]);

          const datosRango =
            datosForma.filter(
              (x: any) =>
                this.cumpleRango(
                  request.tipo,
                  x,
                  rangoFiltro
                )
            );

          for (const d of datosRango) {

            filas.push([
              d.documento,
              d.nombreCompleto,
              d.codigoCuenta,
              Number(d.saldo || 0),
              Number(d.edadAnios || 0),
              Number(d.antiguedadAnios || 0)
            ]);

          }

          const totalSaldo =
            datosRango.reduce(
              (a: number, b: any) =>
                a + Number(b.saldo || 0),
              0
            );

          filas.push([]);

          filas.push([
            `TOTAL RANGO ${i + 1}`,
            datosRango.length,
            totalSaldo
          ]);

          filas.push([]);

        }

        const totalForma =
          datosForma.reduce(
            (a: number, b: any) =>
              a + Number(b.saldo || 0),
            0
          );

        filas.push([]);

        filas.push([
          `TOTAL FORMA ${f.clave}`,
          datosForma.length,
          totalForma
        ]);

        filas.push([]);

      }

      const totalAg =
        datosAgencia.reduce(
          (a: number, b: any) =>
            a + Number(b.saldo || 0),
          0
        );

      filas.push([]);

      filas.push([
        `TOTAL AGENCIA ${ag.clave}`,
        datosAgencia.length,
        totalAg
      ]);

      filas.push([]);

    }

    return {

      nombreHoja:
        'Informe',

      titulo:
        'INFORME POR RANGOS – DEPÓSITOS',

      filtros: [
        ['Fecha corte', request.fechaCorte || ''],
        ['Agencia', request.agencia || ''],
        ['Tipo', request.tipo || '']
      ],

      columnas: [],

      filas,

      anchos: [
        18,
        42,
        18,
        18,
        14,
        14
      ]

    };

  }

  // ============================================================
  // HOJA 2 — RESUMEN
  // ============================================================
  private crearHojaResumen(
    resumen: any[],
    request: any
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'Resumen General',

      titulo:
        'RESUMEN GENERAL',

      columnas: [
        'Rango',
        'Descripción',
        'Cuentas',
        'Total saldo'
      ],

      filas:
        resumen.map((r: any, i: number) => [

          `Rango ${i + 1}`,

          this.descripcionRango(
            request.tipo,
            request.rangos[i].desde,
            request.rangos[i].hasta
          ),

          Number(r.cuentas || 0),

          Number(r.saldo || 0)

        ]),

      anchos: [
        18,
        42,
        18,
        22
      ]

    };

  }

  // ============================================================
  // HOJA 3 — RESUMEN POR FORMA
  // ============================================================
  private crearHojaPorForma(
    detalle: any[],
    resumen: any[],
    request: any
  ): ExcelSheetOptions {

    const filas: any[][] = [];

    const formas =
      this.agrupar(
        detalle,
        'codigoForma'
      );

    for (const f of formas) {

      const datosForma =
        detalle.filter(
          (x: any) =>
            x.codigoForma === f.clave
        );

      for (let i = 0; i < resumen.length; i++) {

        const rf =
          request.rangos[i];

        const desc =
          this.descripcionRango(
            request.tipo,
            rf.desde,
            rf.hasta
          );

        const datosRango =
          datosForma.filter(
            (x: any) =>
              this.cumpleRango(
                request.tipo,
                x,
                rf
              )
          );

        const total =
          datosRango.reduce(
            (a: number, b: any) =>
              a + Number(b.saldo || 0),
            0
          );

        filas.push([
          f.clave,
          `Rango ${i + 1}: ${desc}`,
          datosRango.length,
          total
        ]);

      }

      filas.push([]);

    }

    return {

      nombreHoja:
        'Resumen por Forma',

      titulo:
        'RESUMEN POR FORMA',

      columnas: [
        'Forma',
        'Descripción',
        'Cuentas',
        'Total saldo'
      ],

      filas,

      anchos: [
        18,
        42,
        18,
        22
      ]

    };

  }

  // ============================================================
  // UTILIDADES
  // ============================================================
  private cumpleRango(
    tipo: string,
    item: any,
    rango: any
  ): boolean {

    if (tipo === 'EDAD') {
      return item.edadAnios >= rango.desde
        && item.edadAnios <= rango.hasta;
    }

    if (tipo === 'SALDO') {
      return item.saldo >= rango.desde
        && item.saldo <= rango.hasta;
    }

    if (tipo === 'ANTIGUEDAD') {
      return item.antiguedadAnios >= rango.desde
        && item.antiguedadAnios <= rango.hasta;
    }

    return false;
  }

  private descripcionRango(
    tipo: string,
    desde: number,
    hasta: number
  ): string {

    if (tipo === 'EDAD') {
      return `Edad entre ${desde} y ${hasta} años`;
    }

    if (tipo === 'SALDO') {
      return `Saldo entre ${desde.toLocaleString()} y ${hasta.toLocaleString()}`;
    }

    if (tipo === 'ANTIGUEDAD') {
      return `Antigüedad entre ${desde} y ${hasta} años`;
    }

    return `${desde} - ${hasta}`;
  }

  private agrupar(
    datos: any[],
    campo: string
  ): any[] {

    const mapa =
      new Map<
        string,
        {
          cuentas: number;
          saldo: number;
        }
      >();

    for (const x of datos || []) {

      const c =
        x[campo];

      if (!mapa.has(c)) {

        mapa.set(c, {
          cuentas: 0,
          saldo: 0
        });

      }

      mapa.get(c)!.cuentas++;

      mapa.get(c)!.saldo +=
        Number(x.saldo || 0);

    }

    return Array.from(
      mapa.entries()
    ).map(([clave, v]) => ({
      clave,
      ...v
    }));

  }

}
