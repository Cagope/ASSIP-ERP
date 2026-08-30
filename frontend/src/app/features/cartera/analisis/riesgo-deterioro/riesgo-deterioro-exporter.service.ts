import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import {
  RiesgoDeterioroConcentracion,
  RiesgoDeterioroDetalle,
  RiesgoDeterioroEdad,
  RiesgoDeterioroEvolucion,
  RiesgoDeterioroResumen,
  RiesgoDeterioroSegmento
} from './riesgo-deterioro.models';

@Injectable({ providedIn: 'root' })
export class RiesgoDeterioroExporterService {
  exportar(params: {
    fechaCorte: string;
    resumen: RiesgoDeterioroResumen | null;
    edades: RiesgoDeterioroEdad[];
    segmentacion: RiesgoDeterioroSegmento[];
    evolucion: RiesgoDeterioroEvolucion[];
    concentracion: RiesgoDeterioroConcentracion[];
    detalle: RiesgoDeterioroDetalle[];
  }): void {
    if (!params.resumen) return;

    const wb = XLSX.utils.book_new();

    const resumen = [
      { Indicador: 'Fecha de corte', Valor: params.fechaCorte },
      { Indicador: 'Cantidad de créditos', Valor: params.resumen.cantidadCreditos },
      { Indicador: 'Saldo cartera', Valor: params.resumen.saldoCartera },
      { Indicador: 'VEA', Valor: params.resumen.vea },
      { Indicador: 'Exposición total', Valor: params.resumen.exposicionTotal },
      { Indicador: 'Pérdida esperada', Valor: params.resumen.perdidaEsperada },
      { Indicador: 'Deterioro capital', Valor: params.resumen.deterioroCapital },
      { Indicador: 'Deterioro intereses', Valor: params.resumen.deterioroIntereses },
      { Indicador: 'Deterioro otros', Valor: params.resumen.deterioroOtros },
      { Indicador: 'Deterioro total', Valor: params.resumen.deterioroTotal },
      { Indicador: '% deterioro / saldo', Valor: params.resumen.porcentajeDeterioroSobreSaldo },
      { Indicador: 'Saldo mora 30+', Valor: params.resumen.saldoMora30 },
      { Indicador: '% mora 30+', Valor: params.resumen.porcentajeSaldoMora30 },
      { Indicador: 'Saldo mora 60+', Valor: params.resumen.saldoMora60 },
      { Indicador: '% mora 60+', Valor: params.resumen.porcentajeSaldoMora60 },
      { Indicador: 'Saldo mora 90+', Valor: params.resumen.saldoMora90 },
      { Indicador: '% mora 90+', Valor: params.resumen.porcentajeSaldoMora90 },
      { Indicador: 'Saldo mora 180+', Valor: params.resumen.saldoMora180 },
      { Indicador: '% mora 180+', Valor: params.resumen.porcentajeSaldoMora180 }
    ];

    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(resumen), 'Resumen');
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(params.edades), 'Edades');
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(params.segmentacion), 'Segmentacion');
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(params.evolucion), 'Evolucion');
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(params.concentracion), 'Concentracion');
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(params.detalle), 'Detalle');

    XLSX.writeFile(wb, `riesgo-deterioro-${params.fechaCorte}.xlsx`);
  }
}
