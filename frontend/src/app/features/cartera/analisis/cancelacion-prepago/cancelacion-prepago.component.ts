import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import * as XLSX from 'xlsx';
import {
  CancelacionPrepagoCredito,
  CancelacionPrepagoDetalle,
  CancelacionPrepagoLinea,
  CancelacionPrepagoResumen
} from './cancelacion-prepago.models';
import { CancelacionPrepagoService } from './cancelacion-prepago.service';

@Component({
  selector: 'app-cancelacion-prepago',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cancelacion-prepago.component.html',
  styleUrls: ['./cancelacion-prepago.component.scss']
})
export class CancelacionPrepagoComponent implements OnInit {
  cortes: string[] = [];
  fechaCorte = '';
  resumen: CancelacionPrepagoResumen | null = null;
  lineas: CancelacionPrepagoLinea[] = [];
  detalle: CancelacionPrepagoDetalle[] = [];

  idLineaCredito: number | null = null;
  clasificacion = '';
  textoBusqueda = '';

  pagina = 1;
  tamanoPagina = 50;

  creditoSeleccionado: CancelacionPrepagoCredito | null = null;
  historiaCredito: CancelacionPrepagoDetalle[] = [];
  mostrarHistoria = false;

  cargando = false;
  cargandoDetalle = false;
  cargandoHistoria = false;
  exportando = false;
  error = '';

  readonly clasificaciones = [
    { codigo: '', descripcion: 'Todas' },
    { codigo: 'PERMANENCIA', descripcion: 'Permanencia' },
    { codigo: 'AUSENCIA TEMPORAL', descripcion: 'Ausencia temporal' },
    { codigo: 'PREPAGO', descripcion: 'Prepago' },
    { codigo: 'CANCELACION NORMAL', descripcion: 'Cancelación normal' },
    { codigo: 'CANCELACION POSTERIOR AL VENCIMIENTO', descripcion: 'Cancelación posterior al vencimiento' }
  ];

  constructor(private readonly service: CancelacionPrepagoService) {}

  ngOnInit(): void {
    this.cargarCortes();
  }

  cargarCortes(): void {
    this.cargando = true;
    this.error = '';
    this.service.listarCortes().subscribe({
      next: cortes => {
        this.cortes = cortes ?? [];
        this.fechaCorte = this.cortes[0] ?? '';
        if (this.fechaCorte) this.cargarAnalisis();
        else this.cargando = false;
      },
      error: err => this.manejarError(err)
    });
  }

  cambiarCorte(): void {
    this.idLineaCredito = null;
    this.clasificacion = '';
    this.textoBusqueda = '';
    this.pagina = 1;
    this.cerrarHistoria();
    this.cargarAnalisis();
  }

  cargarAnalisis(): void {
    if (!this.fechaCorte) return;
    this.cargando = true;
    this.error = '';
    forkJoin({
      resumen: this.service.obtenerResumen(this.fechaCorte),
      lineas: this.service.listarLineas(this.fechaCorte),
      detalle: this.service.listarDetalle(this.fechaCorte)
    }).subscribe({
      next: r => {
        this.resumen = r.resumen;
        this.lineas = r.lineas ?? [];
        this.detalle = r.detalle ?? [];
        this.pagina = 1;
        this.cargando = false;
      },
      error: err => this.manejarError(err)
    });
  }

  aplicarFiltros(): void {
    if (!this.fechaCorte) return;
    this.cargandoDetalle = true;
    this.error = '';
    this.pagina = 1;
    this.cerrarHistoria();

    const solicitud = this.idLineaCredito != null
      ? this.service.listarDetalle(this.fechaCorte, this.idLineaCredito)
      : this.service.listarCreditos(this.fechaCorte, this.clasificacion || undefined);

    solicitud.subscribe({
      next: filas => {
        this.detalle = filas ?? [];
        this.cargandoDetalle = false;
      },
      error: err => {
        this.cargandoDetalle = false;
        this.manejarError(err);
      }
    });
  }

  limpiarFiltros(): void {
    this.idLineaCredito = null;
    this.clasificacion = '';
    this.textoBusqueda = '';
    this.pagina = 1;
    this.cerrarHistoria();
    this.aplicarFiltros();
  }

  seleccionarLinea(linea: CancelacionPrepagoLinea): void {
    this.idLineaCredito = linea.idLineaCredito;
    this.clasificacion = '';
    this.textoBusqueda = '';
    this.aplicarFiltros();
  }

  get detalleFiltrado(): CancelacionPrepagoDetalle[] {
    const clasificacion = this.clasificacion.trim();
    const q = this.textoBusqueda.trim().toLowerCase();

    return this.detalle.filter(x => {
      if (clasificacion && x.clasificacionSalida !== clasificacion) return false;
      if (!q) return true;
      return [
        x.documento,
        x.nombreCompleto,
        x.pagareCartera,
        x.codigoLineaCredito,
        x.nombreLineaCredito,
        x.descripcionClasificacionCredito,
        x.descripcionDestinoEconomico,
        x.clasificacionSalida,
        x.rangoAnticipacion ?? ''
      ].some(v => String(v ?? '').toLowerCase().includes(q));
    });
  }

  get detallePagina(): CancelacionPrepagoDetalle[] {
    const inicio = (this.pagina - 1) * this.tamanoPagina;
    return this.detalleFiltrado.slice(inicio, inicio + this.tamanoPagina);
  }

  get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.detalleFiltrado.length / this.tamanoPagina));
  }

  cambiarTamanoPagina(): void {
    this.pagina = 1;
  }

  verHistoria(fila: CancelacionPrepagoDetalle): void {
    this.cargandoHistoria = true;
    this.error = '';
    forkJoin({
      credito: this.service.obtenerCredito(fila.idCarteraCredito),
      historia: this.service.listarHistoriaCredito(fila.idCarteraCredito)
    }).subscribe({
      next: r => {
        this.creditoSeleccionado = r.credito;
        this.historiaCredito = r.historia ?? [];
        this.mostrarHistoria = true;
        this.cargandoHistoria = false;
        setTimeout(() => document.getElementById('historia-credito')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
      },
      error: err => {
        this.cargandoHistoria = false;
        this.manejarError(err);
      }
    });
  }

  cerrarHistoria(): void {
    this.mostrarHistoria = false;
    this.creditoSeleccionado = null;
    this.historiaCredito = [];
  }

  claseSalida(valor: string | null | undefined): string {
    switch (valor) {
      case 'PREPAGO': return 'estado estado--prepago';
      case 'CANCELACION NORMAL': return 'estado estado--normal';
      case 'CANCELACION POSTERIOR AL VENCIMIENTO': return 'estado estado--posterior';
      case 'AUSENCIA TEMPORAL': return 'estado estado--temporal';
      default: return 'estado estado--permanencia';
    }
  }

  claseAmortizacion(valor: string | null | undefined): string {
    switch (valor) {
      case 'AMORTIZACION ACELERADA': return 'amort amort--acelerada';
      case 'AUMENTO DE SALDO': return 'amort amort--aumento';
      case 'AMORTIZACION NORMAL': return 'amort amort--normal';
      default: return 'amort';
    }
  }

  moneda(valor: number | null | undefined): string {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor ?? 0);
  }

  pct(valor: number | null | undefined): string {
    return `${new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(valor ?? 0)}%`;
  }

  numero(valor: number | null | undefined, decimales = 2): string {
    return new Intl.NumberFormat('es-CO', { minimumFractionDigits: decimales, maximumFractionDigits: decimales }).format(valor ?? 0);
  }

  diasComoMeses(valor: number | null | undefined): string {
    if (valor == null) return '—';
    if (Math.abs(valor) < 31) return `${valor} días`;
    return `${this.numero(valor / 30.4375, 1)} meses`;
  }

  exportarExcel(): void {
    if (!this.resumen) return;
    this.exportando = true;
    try {
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet([this.filaResumenExcel()]), 'Resumen');
      XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(this.lineas.map(x => this.filaLineaExcel(x))), 'Por linea');
      XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(this.detalleFiltrado.map(x => this.filaDetalleExcel(x))), 'Detalle creditos');

      if (this.creditoSeleccionado && this.historiaCredito.length) {
        XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(this.historiaCredito.map(x => this.filaHistoriaExcel(x))), 'Historia credito');
      }

      XLSX.writeFile(wb, `Cancelacion_Prepago_${this.fechaCorte}.xlsx`);
    } finally {
      this.exportando = false;
    }
  }

  exportarHistoriaExcel(): void {
    if (!this.creditoSeleccionado || !this.historiaCredito.length) return;
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet([this.filaCreditoExcel(this.creditoSeleccionado)]), 'Credito');
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(this.historiaCredito.map(x => this.filaHistoriaExcel(x))), 'Historia');
    XLSX.writeFile(wb, `Historia_Cancelacion_Prepago_${this.creditoSeleccionado.pagareCartera}.xlsx`);
  }

  private filaResumenExcel(): Record<string, unknown> {
    const r = this.resumen!;
    return {
      'Fecha corte': r.fechaCorte,
      'Fecha corte siguiente': r.fechaCorteSiguiente,
      'Creditos expuestos': r.creditosExpuestos,
      'Personas expuestas': r.personasExpuestas,
      'Permanencias': r.permanencias,
      'Ausencias temporales': r.ausenciasTemporales,
      'Prepagos': r.prepagos,
      'Cancelaciones normales': r.cancelacionesNormales,
      'Cancelaciones posteriores': r.cancelacionesPosteriores,
      'Salidas definitivas': r.salidasDefinitivas,
      'Tasa permanencia %': r.tasaPermanencia,
      'Tasa salida definitiva %': r.tasaSalidaDefinitiva,
      'Tasa prepago poblacion %': r.tasaPrepagoPoblacion,
      'Participacion prepago en cancelaciones %': r.participacionPrepagoCancelaciones,
      'Saldo expuesto': r.saldoExpuesto,
      'Saldo previo prepagos': r.saldoPrevioPrepagos,
      'Saldo previo salidas definitivas': r.saldoPrevioSalidasDefinitivas,
      'Velocidad amortizacion promedio %': r.velocidadAmortizacionPromedio,
      'Mediana velocidad amortizacion %': r.medianaVelocidadAmortizacion,
      'Amortizaciones aceleradas': r.amortizacionesAceleradas,
      'Aumentos saldo': r.aumentosSaldo,
      'Amortizacion acelerada %': r.porcentajeAmortizacionAcelerada,
      'Vida contractual promedio dias': r.vidaContractualPromedioDias,
      'Vida efectiva promedio dias': r.vidaEfectivaPromedioDias,
      'Vida consumida promedio %': r.porcentajeVidaConsumidaPromedio,
      'Anticipacion promedio prepago dias': r.anticipacionPromedioPrepagoDias,
      'Prepagos 31-90 dias': r.prepagos3190Dias,
      'Prepagos 3-6 meses': r.prepagos36Meses,
      'Prepagos 6-12 meses': r.prepagos612Meses,
      'Prepagos 1-2 anios': r.prepagos12Anios,
      'Prepagos 2-5 anios': r.prepagos25Anios,
      'Prepagos mas de 5 anios': r.prepagosMas5Anios
    };
  }

  private filaLineaExcel(x: CancelacionPrepagoLinea): Record<string, unknown> {
    return {
      'Codigo linea': x.codigoLineaCredito,
      'Linea': x.nombreLineaCredito,
      'Creditos expuestos': x.creditosExpuestos,
      'Permanencias': x.permanencias,
      'Ausencias temporales': x.ausenciasTemporales,
      'Prepagos': x.prepagos,
      'Cancelaciones normales': x.cancelacionesNormales,
      'Cancelaciones posteriores': x.cancelacionesPosteriores,
      'Salidas definitivas': x.salidasDefinitivas,
      'Tasa permanencia %': x.tasaPermanencia,
      'Tasa salida definitiva %': x.tasaSalidaDefinitiva,
      'Tasa prepago poblacion %': x.tasaPrepagoPoblacion,
      'Participacion prepago cancelaciones %': x.participacionPrepagoCancelaciones,
      'Saldo expuesto': x.saldoExpuesto,
      'Saldo previo prepagos': x.saldoPrevioPrepagos,
      'Saldo previo salidas': x.saldoPrevioSalidasDefinitivas,
      'Velocidad amortizacion promedio %': x.velocidadAmortizacionPromedio,
      'Mediana velocidad amortizacion %': x.medianaVelocidadAmortizacion,
      'Amortizaciones aceleradas': x.amortizacionesAceleradas,
      'Aumentos saldo': x.aumentosSaldo,
      'Amortizacion acelerada %': x.porcentajeAmortizacionAcelerada,
      'Vida contractual promedio dias': x.vidaContractualPromedioDias,
      'Vida efectiva promedio dias': x.vidaEfectivaPromedioDias,
      'Vida consumida promedio %': x.porcentajeVidaConsumidaPromedio,
      'Anticipacion promedio prepago dias': x.anticipacionPromedioPrepagoDias
    };
  }

  private filaDetalleExcel(x: CancelacionPrepagoDetalle): Record<string, unknown> {
    return {
      'Fecha corte': x.fechaCorte,
      'Fecha corte siguiente': x.fechaCorteSiguiente,
      'Id credito': x.idCarteraCredito,
      'Pagare': x.pagareCartera,
      'Documento': x.documento,
      'Nombre': x.nombreCompleto,
      'Codigo linea': x.codigoLineaCredito,
      'Linea': x.nombreLineaCredito,
      'Clasificacion credito': x.descripcionClasificacionCredito,
      'Destino economico': x.descripcionDestinoEconomico,
      'Fecha desembolso': x.fechaDesembolso,
      'Fecha final contractual': x.fechaFinal,
      'Plazo': x.plazo,
      'Valor inicial': x.valorInicialCredito,
      'Valor desembolsado': x.valorDesembolsado,
      'Valor cuota': x.valorCuota,
      'Saldo corte': x.saldoCorteAnterior,
      'Saldo corte siguiente': x.saldoCorteSiguiente,
      'Reduccion saldo': x.reduccionSaldo,
      'Reduccion saldo %': x.porcentajeReduccionSaldo,
      'Mediana linea %': x.medianaReduccionLinea,
      'Percentil 75 linea %': x.percentil75ReduccionLinea,
      'Comportamiento amortizacion': x.comportamientoAmortizacion,
      'Clasificacion salida': x.clasificacionSalida,
      'Rango anticipacion': x.rangoAnticipacion,
      'Dias anticipacion': x.diasAnticipacion,
      'Dias vida contractual': x.diasVidaContractual,
      'Dias vida efectiva': x.diasVidaEfectiva,
      'Vida consumida %': x.porcentajeVidaConsumida,
      'Dias mora': x.diasMora,
      'Edad contable': x.edadContableResultado,
      'Deterioro capital': x.deterioroCapital,
      'Deterioro intereses': x.deterioroIntereses,
      'Deterioro otros': x.deterioroOtros,
      'Deterioro total': x.deterioroTotal
    };
  }

  private filaHistoriaExcel(x: CancelacionPrepagoDetalle): Record<string, unknown> {
    return this.filaDetalleExcel(x);
  }

  private filaCreditoExcel(x: CancelacionPrepagoCredito): Record<string, unknown> {
    return {
      'Id credito': x.idCarteraCredito,
      'Pagare': x.pagareCartera,
      'Documento': x.documento,
      'Nombre': x.nombreCompleto,
      'Linea': `${x.codigoLineaCredito} - ${x.nombreLineaCredito}`,
      'Fecha desembolso': x.fechaDesembolso,
      'Fecha final': x.fechaFinal,
      'Valor inicial': x.valorInicialCredito,
      'Saldo ultimo corte': x.saldoCorteAnterior,
      'Primer corte observado': x.primerCorteObservado,
      'Ultimo corte evaluable': x.ultimoCorteEvaluable,
      'Transiciones': x.cantidadTransiciones,
      'Permanencias': x.cantidadPermanencias,
      'Ausencias temporales': x.cantidadAusenciasTemporales,
      'Periodos amortizacion acelerada': x.periodosAmortizacionAcelerada,
      'Periodos aumento saldo': x.periodosAumentoSaldo,
      'Velocidad amortizacion promedio %': x.velocidadAmortizacionPromedio,
      'Maxima reduccion %': x.maximaReduccionPorcentual,
      'Clasificacion final': x.clasificacionSalida,
      'Rango anticipacion': x.rangoAnticipacion,
      'Dias anticipacion': x.diasAnticipacion,
      'Vida consumida %': x.porcentajeVidaConsumida,
      'Deterioro total': x.deterioroTotal
    };
  }

  private manejarError(err: any): void {
    console.error(err);
    this.error = err?.error?.message || err?.error || 'No fue posible cargar el análisis de cancelación y prepago.';
    this.cargando = false;
    this.cargandoDetalle = false;
    this.cargandoHistoria = false;
  }
}
