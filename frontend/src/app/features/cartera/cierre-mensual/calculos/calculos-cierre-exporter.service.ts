import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

import {
  DetalleCalculosCierre
} from './calculos-cierre.api';

@Injectable({
  providedIn: 'root'
})
export class CalculosCierreExporterService {

  // =========================================================
  // EXPORTAR
  // =========================================================

  exportar(
    detalle: DetalleCalculosCierre[],
    fechaCorte: string
  ): void {

    if (!detalle?.length) {
      return;
    }

    const wb = XLSX.utils.book_new();

    const grupos =
      this.agruparPorClasificacion(detalle);

    // =======================================================
    // RESUMEN GENERAL
    // =======================================================

    this.agregarResumen(
      wb,
      detalle,
      grupos,
      fechaCorte
    );

    // =======================================================
    // UNA HOJA POR CLASIFICACIÓN
    // =======================================================

    for (const [clasificacion, creditos] of grupos.entries()) {

      this.agregarDetalleClasificacion(
        wb,
        clasificacion,
        creditos
      );

    }

    // =======================================================
    // GENERAR ARCHIVO
    // =======================================================

    XLSX.writeFile(
      wb,
      `calculos-cierre-cartera-${fechaCorte || 'proceso'}.xlsx`
    );
  }


  // =========================================================
  // RESUMEN
  // =========================================================

  private agregarResumen(
    wb: XLSX.WorkBook,
    detalle: DetalleCalculosCierre[],
    grupos: Map<string, DetalleCalculosCierre[]>,
    fechaCorte: string
  ): void {

    const filas: any[][] = [];

    // =======================================================
    // ENCABEZADO
    // =======================================================

    filas.push(
      ['CÁLCULOS DEL CIERRE MENSUAL DE CARTERA'],
      [],
      ['Fecha de corte', fechaCorte || ''],
      ['Total créditos', detalle.length],
      ['Saldo total', this.sumar(detalle, 'saldoFotografia')],
      ['Aportes aplicados', this.sumar(detalle, 'valorAportesCredito')],
      ['Garantías asignadas', this.sumar(detalle, 'valorGarantiasCredito')],
      []
    );

    // =======================================================
    // RESUMEN POR CLASIFICACIÓN
    // =======================================================

    filas.push([
      'Clasificación',
      'Créditos',
      'Saldo',
      'Edad A',
      'Edad B',
      'Edad C',
      'Edad D',
      'Edad E',
      'Sin edad',
      'Aportes aplicados',
      'Garantías asignadas'
    ]);

    for (const [clasificacion, creditos] of grupos.entries()) {

      filas.push([
        clasificacion,

        creditos.length,

        this.sumar(
          creditos,
          'saldoFotografia'
        ),

        this.contarEdad(
          creditos,
          'A'
        ),

        this.contarEdad(
          creditos,
          'B'
        ),

        this.contarEdad(
          creditos,
          'C'
        ),

        this.contarEdad(
          creditos,
          'D'
        ),

        this.contarEdad(
          creditos,
          'E'
        ),

        this.contarSinEdad(
          creditos
        ),

        this.sumar(
          creditos,
          'valorAportesCredito'
        ),

        this.sumar(
          creditos,
          'valorGarantiasCredito'
        )
      ]);
    }

    // =======================================================
    // TOTAL
    // =======================================================

    filas.push([
      'TOTAL',

      detalle.length,

      this.sumar(
        detalle,
        'saldoFotografia'
      ),

      this.contarEdad(
        detalle,
        'A'
      ),

      this.contarEdad(
        detalle,
        'B'
      ),

      this.contarEdad(
        detalle,
        'C'
      ),

      this.contarEdad(
        detalle,
        'D'
      ),

      this.contarEdad(
        detalle,
        'E'
      ),

      this.contarSinEdad(
        detalle
      ),

      this.sumar(
        detalle,
        'valorAportesCredito'
      ),

      this.sumar(
        detalle,
        'valorGarantiasCredito'
      )
    ]);

    const ws =
      XLSX.utils.aoa_to_sheet(filas);

    ws['!cols'] = [
      { wch: 28 }, // Clasificación
      { wch: 14 }, // Créditos
      { wch: 20 }, // Saldo
      { wch: 12 }, // A
      { wch: 12 }, // B
      { wch: 12 }, // C
      { wch: 12 }, // D
      { wch: 12 }, // E
      { wch: 12 }, // Sin edad
      { wch: 22 }, // Aportes
      { wch: 22 }  // Garantías
    ];

    XLSX.utils.book_append_sheet(
      wb,
      ws,
      'RESUMEN'
    );
  }


  // =========================================================
  // DETALLE POR CLASIFICACIÓN
  // =========================================================

  private agregarDetalleClasificacion(
    wb: XLSX.WorkBook,
    clasificacion: string,
    creditos: DetalleCalculosCierre[]
  ): void {

    const filas: any[][] = [];

    // =======================================================
    // COLUMNAS
    // =======================================================

    filas.push([
      'Tipo documento',
      'Documento',
      'Asociado',
      'Pagaré',

      'Código línea',
      'Línea',
      'Clasificación',

      'Saldo al corte',

      'Días mora',
      'Edad mora',

      'Cantidad créditos asociado',
      'Saldo total créditos asociado',

      'Saldo aportes al corte',
      '% aporte crédito',
      'Valor aporte aplicado',

      'Cantidad bienes garantía',
      'Valor garantías total',
      '% garantías crédito',
      'Valor garantía asignada'
    ]);

    // =======================================================
    // DETALLE
    // =======================================================

    creditos.forEach(c => {

      filas.push([
        c.tipoDocumento || '',
        c.documento || '',
        c.nombreCompleto || '',
        c.pagareCartera || '',

        c.codigoLineaCredito || '',
        c.nombreLineaCredito || '',
        c.descripcionClasificacionCredito || '',

        this.numero(
          c.saldoFotografia
        ),

        this.numero(
          c.diasMora
        ),

        c.edadMoraCalculada || '',

        this.numero(
          c.cantidadCreditosAsociado
        ),

        this.numero(
          c.saldoTotalCreditosAsociado
        ),

        this.numero(
          c.saldoAportesFechaCorte
        ),

        this.numero(
          c.porcentajeAportesCredito
        ),

        this.numero(
          c.valorAportesCredito
        ),

        this.numero(
          c.cantidadBienesGarantia
        ),

        this.numero(
          c.valorGarantiasTotal
        ),

        this.numero(
          c.porcentajeGarantiasCredito
        ),

        this.numero(
          c.valorGarantiasCredito
        )
      ]);

    });

    const ws =
      XLSX.utils.aoa_to_sheet(filas);

    ws['!cols'] = [
      { wch: 16 },
      { wch: 18 },
      { wch: 38 },
      { wch: 16 },

      { wch: 14 },
      { wch: 32 },
      { wch: 24 },

      { wch: 20 },

      { wch: 12 },
      { wch: 12 },

      { wch: 18 },
      { wch: 24 },

      { wch: 22 },
      { wch: 18 },
      { wch: 22 },

      { wch: 20 },
      { wch: 22 },
      { wch: 20 },
      { wch: 24 }
    ];

    // =======================================================
    // AUTOFILTRO
    // =======================================================

    if (creditos.length > 0) {

      ws['!autofilter'] = {
        ref: `A1:S${creditos.length + 1}`
      };

    }

    XLSX.utils.book_append_sheet(
      wb,
      ws,
      this.nombreHoja(
        clasificacion,
        wb.SheetNames
      )
    );
  }


  // =========================================================
  // AGRUPAR POR CLASIFICACIÓN
  // =========================================================

  private agruparPorClasificacion(
    detalle: DetalleCalculosCierre[]
  ): Map<string, DetalleCalculosCierre[]> {

    const grupos =
      new Map<string, DetalleCalculosCierre[]>();

    detalle.forEach(c => {

      const clasificacion =
        (
          c.descripcionClasificacionCredito ||
          c.codigoClasificacionCredito ||
          'SIN CLASIFICAR'
        ).trim();

      if (!grupos.has(clasificacion)) {
        grupos.set(
          clasificacion,
          []
        );
      }

      grupos.get(clasificacion)!
        .push(c);

    });

    return new Map(
      [...grupos.entries()]
        .sort((a, b) =>
          a[0].localeCompare(
            b[0],
            'es'
          )
        )
    );
  }


  // =========================================================
  // CONTAR EDADES
  // =========================================================

  private contarEdad(
    detalle: DetalleCalculosCierre[],
    edad: string
  ): number {

    return detalle.filter(c =>
      (c.edadMoraCalculada || '')
        .trim()
        .toUpperCase() === edad
    ).length;
  }


  private contarSinEdad(
    detalle: DetalleCalculosCierre[]
  ): number {

    return detalle.filter(c =>
      !(c.edadMoraCalculada || '')
        .trim()
    ).length;
  }


  // =========================================================
  // SUMAR
  // =========================================================

  private sumar(
    detalle: DetalleCalculosCierre[],
    campo: keyof DetalleCalculosCierre
  ): number {

    return detalle.reduce(
      (total, item) =>
        total +
        this.numero(
          item[campo]
        ),
      0
    );
  }


  // =========================================================
  // CONVERTIR A NÚMERO
  // =========================================================

  private numero(
    valor: unknown
  ): number {

    if (
      valor === null ||
      valor === undefined ||
      valor === ''
    ) {
      return 0;
    }

    const numero =
      Number(valor);

    return Number.isFinite(numero)
      ? numero
      : 0;
  }


  // =========================================================
  // NOMBRE SEGURO PARA LA HOJA
  // =========================================================

  private nombreHoja(
    nombre: string,
    existentes: string[]
  ): string {

    let base =
      (nombre || 'SIN CLASIFICAR')
        .replace(
          /[\\/?*\[\]:]/g,
          ' '
        )
        .replace(
          /\s+/g,
          ' '
        )
        .trim()
        .substring(
          0,
          31
        );

    if (!base) {
      base = 'SIN CLASIFICAR';
    }

    let candidato =
      base;

    let consecutivo =
      2;

    while (
      existentes.includes(candidato)
    ) {

      const sufijo =
        ` ${consecutivo}`;

      candidato =
        base.substring(
          0,
          31 - sufijo.length
        ) + sufijo;

      consecutivo++;
    }

    return candidato;
  }
}
