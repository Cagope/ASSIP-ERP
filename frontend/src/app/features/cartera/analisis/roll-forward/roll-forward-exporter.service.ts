import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import {
  RollForwardDetalle,
  RollForwardLinea,
  RollForwardResumen
} from './roll-forward.models';

@Injectable({ providedIn: 'root' })
export class RollForwardExporterService {

  exportar(params: {
    resumen: RollForwardResumen;
    lineas: RollForwardLinea[];
    historico: RollForwardResumen[];
    detalle: RollForwardDetalle[];
  }): void {
    const wb = XLSX.utils.book_new();

    const resumen = [
      { Indicador: 'Corte anterior', Valor: params.resumen.fechaCorteAnterior },
      { Indicador: 'Corte actual', Valor: params.resumen.fechaCorte },
      { Indicador: 'Créditos iniciales', Valor: params.resumen.creditosIniciales },
      { Indicador: 'Créditos finales', Valor: params.resumen.creditosFinales },
      { Indicador: 'Créditos nuevos', Valor: params.resumen.creditosNuevos },
      { Indicador: 'Créditos reingresados', Valor: params.resumen.creditosReingresados },
      { Indicador: 'Créditos con reducción', Valor: params.resumen.creditosReduccion },
      { Indicador: 'Créditos sin variación', Valor: params.resumen.creditosSinVariacion },
      { Indicador: 'Créditos con aumento', Valor: params.resumen.creditosAumento },
      { Indicador: 'Prepagos', Valor: params.resumen.creditosPrepago },
      { Indicador: 'Cancelaciones normales', Valor: params.resumen.creditosCancelacionNormal },
      { Indicador: 'Cancelaciones post vencimiento', Valor: params.resumen.creditosCancelacionPostVencimiento },
      { Indicador: 'Ausencias temporales', Valor: params.resumen.creditosAusenciaTemporal },
      { Indicador: 'Otras salidas', Valor: params.resumen.creditosOtrasSalidas },
      { Indicador: 'Saldo inicial', Valor: params.resumen.saldoInicial },
      { Indicador: 'Nuevos', Valor: params.resumen.nuevos },
      { Indicador: 'Reingresos', Valor: params.resumen.reingresos },
      { Indicador: 'Aumentos de saldo', Valor: params.resumen.aumentosSaldo },
      { Indicador: 'Reducciones de saldo', Valor: params.resumen.reduccionesSaldo },
      { Indicador: 'Prepagos - valor', Valor: params.resumen.prepagos },
      { Indicador: 'Cancelaciones normales - valor', Valor: params.resumen.cancelacionesNormales },
      { Indicador: 'Cancelaciones post vencimiento - valor', Valor: params.resumen.cancelacionesPostVencimiento },
      { Indicador: 'Ausencias temporales - valor', Valor: params.resumen.ausenciasTemporales },
      { Indicador: 'Otras salidas - valor', Valor: params.resumen.otrasSalidas },
      { Indicador: 'Saldo final', Valor: params.resumen.saldoFinal },
      { Indicador: 'Total entradas', Valor: params.resumen.totalEntradas },
      { Indicador: 'Total salidas y reducciones', Valor: params.resumen.totalSalidasReducciones },
      { Indicador: 'Variación neta', Valor: params.resumen.variacionNeta },
      { Indicador: 'Variación %', Valor: params.resumen.variacionPorcentaje },
      { Indicador: 'Tasa nuevos / saldo inicial %', Valor: params.resumen.tasaNuevosSobreSaldoInicial },
      { Indicador: 'Tasa reducción / saldo inicial %', Valor: params.resumen.tasaReduccionSobreSaldoInicial },
      { Indicador: 'Tasa prepago / saldo inicial %', Valor: params.resumen.tasaPrepagoSobreSaldoInicial },
      { Indicador: 'Tasa salidas definitivas / saldo inicial %', Valor: params.resumen.tasaSalidasDefinitivasSobreSaldoInicial },
      { Indicador: 'Saldo final calculado', Valor: params.resumen.saldoFinalCalculado },
      { Indicador: 'Diferencia control', Valor: params.resumen.diferenciaControl },
      { Indicador: 'Créditos finales calculados', Valor: params.resumen.creditosFinalesCalculados },
      { Indicador: 'Diferencia control créditos', Valor: params.resumen.diferenciaControlCreditos }
    ];

    XLSX.utils.book_append_sheet(
      wb,
      XLSX.utils.json_to_sheet(resumen),
      'Resumen'
    );

    XLSX.utils.book_append_sheet(
      wb,
      XLSX.utils.json_to_sheet(params.lineas),
      'Lineas'
    );

    XLSX.utils.book_append_sheet(
      wb,
      XLSX.utils.json_to_sheet(params.historico),
      'Historico'
    );

    XLSX.utils.book_append_sheet(
      wb,
      XLSX.utils.json_to_sheet(params.detalle),
      'Detalle'
    );

    XLSX.writeFile(
      wb,
      `roll-forward-${params.resumen.fechaCorteAnterior}-${params.resumen.fechaCorte}.xlsx`
    );
  }
}
