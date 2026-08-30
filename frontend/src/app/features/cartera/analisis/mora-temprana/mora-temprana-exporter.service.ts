import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import {
  MoraTempranaCosecha,
  MoraTempranaDetalle,
  MoraTempranaPrimeraMora,
  MoraTempranaResumen,
  MoraTempranaSegmento
} from './mora-temprana.models';

@Injectable({ providedIn: 'root' })
export class MoraTempranaExporterService {
  exportar(params: {
    resumen: MoraTempranaResumen;
    cosechas: MoraTempranaCosecha[];
    agencias: MoraTempranaSegmento[];
    lineas: MoraTempranaSegmento[];
    primeraMora: MoraTempranaPrimeraMora[];
    detalle: MoraTempranaDetalle[];
  }): void {
    const wb = XLSX.utils.book_new();

    const resumen = [
      { Indicador: 'Cosecha desde', Valor: params.resumen.cosechaDesde },
      { Indicador: 'Cosecha hasta', Valor: params.resumen.cosechaHasta },
      { Indicador: 'Cantidad de cosechas', Valor: params.resumen.cantidadCosechas },
      { Indicador: 'Créditos originados', Valor: params.resumen.creditosOriginados },
      { Indicador: 'Valor desembolsado', Valor: params.resumen.valorDesembolsado },
      { Indicador: '30+ hasta MOB3', Valor: params.resumen.mora30HastaMob3 },
      { Indicador: '% 30+ hasta MOB3', Valor: params.resumen.porcentajeMora30Mob3 },
      { Indicador: '30+ hasta MOB6', Valor: params.resumen.mora30HastaMob6 },
      { Indicador: '% 30+ hasta MOB6', Valor: params.resumen.porcentajeMora30Mob6 },
      { Indicador: '60+ hasta MOB6', Valor: params.resumen.mora60HastaMob6 },
      { Indicador: '% 60+ hasta MOB6', Valor: params.resumen.porcentajeMora60Mob6 }
    ];

    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(resumen), 'Resumen');
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(params.cosechas), 'Cosechas');
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(params.lineas), 'Lineas');
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(params.agencias), 'Agencias');
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(params.primeraMora), 'Primera mora');
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(params.detalle), 'Detalle');

    XLSX.writeFile(
      wb,
      `mora-temprana-${params.resumen.cosechaDesde.substring(0, 7)}-${params.resumen.cosechaHasta.substring(0, 7)}.xlsx`
    );
  }
}
