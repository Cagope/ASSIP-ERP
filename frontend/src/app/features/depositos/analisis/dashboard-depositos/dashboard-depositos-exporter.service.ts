import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

import {
  DashboardDepositosForma,
  DashboardDepositosGrupo,
  DashboardDepositosResumen,
  DashboardDepositosTendencia
} from './dashboard-depositos.api';

@Injectable({
  providedIn: 'root'
})
export class DashboardDepositosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    fechaCorte: string,
    fechaCorteAnterior: string,
    resumen: DashboardDepositosResumen | null,
    formas: DashboardDepositosForma[],
    agencias: DashboardDepositosGrupo[],
    tendencia: DashboardDepositosTendencia[]
  ): void {

    const hojas: ExcelSheetOptions[] = [
      this.crearHojaResumen(fechaCorte, fechaCorteAnterior, resumen, formas),
      this.crearHojaAgencias(agencias),
      this.crearHojaTendencia(tendencia)
    ];

    this.excelExport.exportar({
      nombreArchivo: `dashboard-depositos-${fechaCorte || 'dashboard'}.xlsx`,
      hojas
    });
  }

  private crearHojaResumen(
    fechaCorte: string,
    fechaCorteAnterior: string,
    resumen: DashboardDepositosResumen | null,
    formas: DashboardDepositosForma[]
  ): ExcelSheetOptions {

    return {
      nombreHoja: 'Resumen',
      titulo: 'DASHBOARD DEPÓSITOS',
      filtros: [
        ['Fecha corte', fechaCorte || ''],
        ['Corte anterior', fechaCorteAnterior || '']
      ],
      resumen: [
        ['Total depósitos', Number(resumen?.saldoTotal || 0)],
        ['Total cuentas', Number(resumen?.totalCuentas || 0)],
        ['Aportes sociales', Number(resumen?.saldoAportes || 0)],
        ['Hombres', Number(resumen?.hombres || 0)],
        ['Mujeres', Number(resumen?.mujeres || 0)],
        ['Jurídicas', Number(resumen?.juridicas || 0)]
      ],
      columnas: [
        'Forma',
        'Cuentas corte anterior',
        'Saldo corte anterior',
        'Cuentas actuales',
        'Saldo actual',
        'Ingresos',
        'Egresos',
        'Variación saldo',
        '% crecimiento'
      ],
      filas: (formas || []).map(f => [
        f.nombreForma || '',
        Number(f.cuentasAnteriores || 0),
        Number(f.saldoAnterior || 0),
        Number(f.cuentasActuales || 0),
        Number(f.saldoActual || 0),
        Number(f.ingresosPeriodo || 0),
        Number(f.egresosPeriodo || 0),
        Number(f.variacionSaldo || 0),
        Number(f.porcentajeCrecimiento || 0)
      ]),
      anchos: [28, 18, 22, 18, 22, 18, 18, 22, 16]
    };
  }

  private crearHojaAgencias(
    agencias: DashboardDepositosGrupo[]
  ): ExcelSheetOptions {

    return {
      nombreHoja: 'Agencias',
      titulo: 'PARTICIPACIÓN POR AGENCIAS',
      columnas: [
        '#',
        'Agencia',
        'Ctas aportes',
        'Valor aportes',
        '% aportes',
        'Ctas ahorros',
        'Valor ahorros',
        '% ahorros'
      ],
      filas: (agencias || []).map(a => [
        Number(a.ranking || 0),
        a.concepto || '',
        Number(a.cuentasAportes || 0),
        Number(a.valorAportes || 0),
        Number(a.participacionAportes || 0),
        Number(a.cuentasAhorros || 0),
        Number(a.valorAhorros || 0),
        Number(a.participacionAhorros || 0)
      ]),
      anchos: [8, 28, 18, 22, 14, 18, 22, 14]
    };
  }

  private crearHojaTendencia(
    tendencia: DashboardDepositosTendencia[]
  ): ExcelSheetOptions {

    return {
      nombreHoja: 'Tendencia 12 meses',
      titulo: 'TENDENCIA 12 MESES',
      columnas: [
        'Periodo',
        'Cuentas aportes',
        'Saldo aportes',
        'Cuentas depósitos',
        'Saldo depósitos'
      ],
      filas: (tendencia || []).map(t => [
        t.periodo || '',
        Number(t.totalCuentasAportes || 0),
        Number(t.saldoAportes || 0),
        Number(t.totalCuentasDepositos || 0),
        Number(t.saldoDepositos || 0)
      ]),
      anchos: [14, 18, 22, 20, 24]
    };
  }
}
