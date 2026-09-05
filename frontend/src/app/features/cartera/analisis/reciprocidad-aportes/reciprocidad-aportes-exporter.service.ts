import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelFila,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

import {
  ReciprocidadAportesDetalle,
  ReciprocidadAportesPersona,
  ReciprocidadAportesResumen
} from './reciprocidad-aportes.models';

@Injectable({
  providedIn: 'root'
})
export class ReciprocidadAportesExporterService {

  constructor(
    private readonly excelExport: ExcelExportService
  ) {}

  exportar(
    resumen: ReciprocidadAportesResumen,
    personas: ReciprocidadAportesPersona[],
    detalle: ReciprocidadAportesDetalle[],
    fechaCorte: string
  ): void {

    if (!resumen || !personas?.length || !detalle?.length) {
      alert('No hay información de Reciprocidad de Aportes para exportar.');
      return;
    }

    const hojas: ExcelSheetOptions[] = [
      this.hojaResumen(resumen, fechaCorte),
      this.hojaPersonas(personas, fechaCorte),
      this.hojaDetalle(detalle, fechaCorte)
    ];

    this.excelExport.exportar({
      nombreArchivo: `reciprocidad_aportes_${fechaCorte}.xlsx`,
      hojas
    });
  }

  private hojaResumen(
    r: ReciprocidadAportesResumen,
    fechaCorte: string
  ): ExcelSheetOptions {

    const filas: ExcelFila[] = [
      ['Personas', r.cantidadPersonas],
      ['Créditos', r.cantidadCreditos],
      ['Saldo cartera', r.saldoCartera],
      ['Saldo aportes', r.saldoAportes],
      ['Reciprocidad global %', r.porcentajeReciprocidadGlobal],
      ['Apalancamiento global', r.apalancamientoGlobal],
      ['Exposición neta aportes', r.exposicionNetaAportes],
      ['Excedente aportes', r.excedenteAportes],
      ['Personas sin aportes', r.personasSinAportes],
      ['Personas con aportes menores a cartera', r.personasAportesMenoresCartera],
      ['Personas cuyos aportes cubren cartera', r.personasAportesCubrenCartera],
      ['Reciprocidad menor a 5%', r.personasReciprocidadMenor5],
      ['Reciprocidad 5% a menor de 10%', r.personasReciprocidad5_10],
      ['Reciprocidad 10% a menor de 20%', r.personasReciprocidad10_20],
      ['Reciprocidad 20% a menor de 50%', r.personasReciprocidad20_50],
      ['Reciprocidad 50% a menor de 100%', r.personasReciprocidad50_100],
      ['Reciprocidad 100% o más', r.personasReciprocidad100Mas]
    ];

    return {
      nombreHoja: 'Resumen',
      titulo: 'Reciprocidad de Aportes',
      subtitulo: `Corte: ${fechaCorte}`,
      columnas: ['Indicador', 'Valor'],
      filas,
      anchos: [43, 22]
    };
  }

  private hojaPersonas(
    personas: ReciprocidadAportesPersona[],
    fechaCorte: string
  ): ExcelSheetOptions {

    return {
      nombreHoja: 'Personas',
      titulo: 'Reciprocidad de Aportes por Persona',
      subtitulo: `Corte: ${fechaCorte}`,
      columnas: [
        'Tipo documento',
        'Documento',
        'Nombre completo',
        'Créditos',
        'Saldo cartera',
        'Saldo aportes',
        'Aportes distribuidos',
        'Diferencia distribución',
        'Reciprocidad %',
        'Apalancamiento',
        'Exposición neta',
        'Excedente aportes',
        'Aportes cubren cartera',
        'Rango reciprocidad'
      ],
      filas: personas.map(p => [
        p.tipoDocumento,
        p.documento,
        p.nombreCompleto,
        p.cantidadCreditos,
        p.saldoCartera,
        p.saldoAportes,
        p.aportesDistribuidos,
        p.diferenciaDistribucionAportes,
        p.porcentajeReciprocidad,
        p.apalancamiento,
        p.exposicionNetaAportes,
        p.excedenteAportes,
        p.aportesCubrenCartera ? 'Sí' : 'No',
        p.rangoReciprocidad
      ]),
      anchos: [
        15, 18, 38, 10, 18, 18, 20, 20,
        16, 16, 18, 18, 20, 28
      ]
    };
  }

  private hojaDetalle(
    detalle: ReciprocidadAportesDetalle[],
    fechaCorte: string
  ): ExcelSheetOptions {

    return {
      nombreHoja: 'Detalle créditos',
      titulo: 'Detalle Auditable de Reciprocidad de Aportes',
      subtitulo: `Corte: ${fechaCorte}`,
      columnas: [
        'Tipo documento',
        'Documento',
        'Nombre completo',
        'Créditos persona',
        'Cartera persona',
        'Aportes persona',
        'Reciprocidad persona %',
        'Apalancamiento persona',
        'Pagaré',
        'Agencia',
        'Código línea',
        'Línea',
        'Código clasificación',
        'Clasificación',
        'Código destino',
        'Destino económico',
        'Fecha desembolso',
        'Valor inicial',
        'Valor desembolsado',
        'Saldo crédito al corte',
        'Distribución aportes %',
        'Aportes asignados',
        'Exposición neta crédito',
        'Excedente aportes crédito',
        'Días mora',
        'Edad contable',
        'Deterioro capital',
        'Deterioro intereses',
        'Deterioro otros',
        'Deterioro total'
      ],
      filas: detalle.map(d => [
        d.tipoDocumento,
        d.documento,
        d.nombreCompleto,
        d.cantidadCreditosPersona,
        d.saldoCarteraPersona,
        d.saldoAportesPersona,
        d.porcentajeReciprocidadPersona,
        d.apalancamientoPersona,
        d.pagareCartera,
        d.idAgencia,
        d.codigoLineaCredito,
        d.nombreLineaCredito,
        d.codigoClasificacionCredito,
        d.descripcionClasificacionCredito,
        d.codigoDestinoEconomico,
        d.descripcionDestinoEconomico,
        d.fechaDesembolso,
        d.valorInicialCredito,
        d.valorDesembolsado,
        d.saldoCreditoFechaCorte,
        d.porcentajeAportesCredito,
        d.valorAportesCredito,
        d.exposicionNetaCredito,
        d.excedenteAportesCredito,
        d.diasMora,
        d.edadContableResultado,
        d.deterioroCapital,
        d.deterioroIntereses,
        d.deterioroOtros,
        d.deterioroTotal
      ]),
      anchos: [
        15, 18, 38, 15, 18, 18, 20, 20,
        14, 10, 13, 34, 18, 22, 14, 48,
        16, 18, 18, 20, 20, 18, 20, 20,
        12, 14, 18, 18, 18, 18
      ]
    };
  }
}
