import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import {
  CancelacionPrepagoDetalle,
  CancelacionPrepagoLinea,
  CancelacionPrepagoResumen
} from './cancelacion-prepago.models';

@Injectable({ providedIn: 'root' })
export class CancelacionPrepagoExporterService {
  exportar(params: {
    fechaCorte: string;
    resumen: CancelacionPrepagoResumen;
    lineas: CancelacionPrepagoLinea[];
    detalle: CancelacionPrepagoDetalle[];
  }): void {
    const wb = XLSX.utils.book_new();

    const resumen = [{
      'Fecha corte': params.resumen.fechaCorte,
      'Corte siguiente': params.resumen.fechaCorteSiguiente,
      'Créditos expuestos': params.resumen.creditosExpuestos,
      'Personas expuestas': params.resumen.personasExpuestas,
      'Permanencias': params.resumen.permanencias,
      'Salidas definitivas': params.resumen.salidasDefinitivas,
      'Prepagos': params.resumen.prepagos,
      'Cancelaciones normales': params.resumen.cancelacionesNormales,
      'Cancelaciones posteriores': params.resumen.cancelacionesPosteriores,
      'Ausencias temporales': params.resumen.ausenciasTemporales,
      'Tasa permanencia %': params.resumen.tasaPermanencia,
      'Tasa salida definitiva %': params.resumen.tasaSalidaDefinitiva,
      'Tasa prepago población %': params.resumen.tasaPrepagoPoblacion,
      'Prepago / cancelaciones %': params.resumen.participacionPrepagoCancelaciones,
      'Saldo expuesto': params.resumen.saldoExpuesto,
      'Saldo previo prepagos': params.resumen.saldoPrevioPrepagos,
      'Saldo previo salidas': params.resumen.saldoPrevioSalidasDefinitivas,
      'Velocidad amortización promedio %': params.resumen.velocidadAmortizacionPromedio,
      'Amortizaciones aceleradas': params.resumen.amortizacionesAceleradas,
      'Vida contractual promedio días': params.resumen.vidaContractualPromedioDias,
      'Vida efectiva promedio días': params.resumen.vidaEfectivaPromedioDias,
      'Vida consumida promedio %': params.resumen.porcentajeVidaConsumidaPromedio,
      'Anticipación promedio prepago días': params.resumen.anticipacionPromedioPrepagoDias
    }];

    const lineas = params.lineas.map(x => ({
      'Código': x.codigoLineaCredito,
      'Línea': x.nombreLineaCredito,
      'Expuestos': x.creditosExpuestos,
      'Permanencias': x.permanencias,
      'Prepagos': x.prepagos,
      'Cancelación normal': x.cancelacionesNormales,
      'Cancelación posterior': x.cancelacionesPosteriores,
      'Salidas definitivas': x.salidasDefinitivas,
      'Tasa permanencia %': x.tasaPermanencia,
      'Tasa salida %': x.tasaSalidaDefinitiva,
      'Tasa prepago población %': x.tasaPrepagoPoblacion,
      'Prepago / cancelaciones %': x.participacionPrepagoCancelaciones,
      'Saldo expuesto': x.saldoExpuesto,
      'Saldo previo prepagos': x.saldoPrevioPrepagos,
      'Velocidad amortización %': x.velocidadAmortizacionPromedio,
      'Amortización acelerada %': x.porcentajeAmortizacionAcelerada,
      'Vida consumida %': x.porcentajeVidaConsumidaPromedio,
      'Anticipación prepago días': x.anticipacionPromedioPrepagoDias
    }));

    const detalle = params.detalle.map(x => ({
      'Documento': x.documento,
      'Nombre': x.nombreCompleto,
      'Pagaré': x.pagareCartera,
      'Código línea': x.codigoLineaCredito,
      'Línea': x.nombreLineaCredito,
      'Clasificación salida': x.clasificacionSalida,
      'Rango anticipación': x.rangoAnticipacion,
      'Fecha desembolso': x.fechaDesembolso,
      'Fecha final': x.fechaFinal,
      'Saldo corte': x.saldoCorteAnterior,
      'Saldo siguiente': x.saldoCorteSiguiente,
      'Reducción saldo': x.reduccionSaldo,
      'Reducción %': x.porcentajeReduccionSaldo,
      'Comportamiento amortización': x.comportamientoAmortizacion,
      'Días anticipación': x.diasAnticipacion,
      'Vida contractual días': x.diasVidaContractual,
      'Vida efectiva días': x.diasVidaEfectiva,
      'Vida consumida %': x.porcentajeVidaConsumida,
      'Días mora': x.diasMora,
      'Edad contable': x.edadContableResultado,
      'Deterioro total': x.deterioroTotal
    }));

    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(resumen), 'Resumen');
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(lineas), 'Por linea');
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(detalle), 'Detalle creditos');
    XLSX.writeFile(wb, `cancelacion-prepago-${params.fechaCorte}.xlsx`);
  }
}
