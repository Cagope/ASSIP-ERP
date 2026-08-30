import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { RiesgoDeterioroApi } from './riesgo-deterioro.api';
import { RiesgoDeterioroExporterService } from './riesgo-deterioro-exporter.service';

import {
  RiesgoDeterioroEdadesChartComponent
} from './riesgo-deterioro-edades-chart.component';

import {
  RiesgoDeterioroSegmentacionChartComponent
} from './riesgo-deterioro-segmentacion-chart.component';

import {
  CriterioConcentracion,
  DimensionRiesgo,
  RiesgoDeterioroConcentracion,
  RiesgoDeterioroDetalle,
  RiesgoDeterioroEdad,
  RiesgoDeterioroEvolucion,
  RiesgoDeterioroResumen,
  RiesgoDeterioroSegmento,
  TipoEdadRiesgo
} from './riesgo-deterioro.models';

@Component({
  selector: 'app-riesgo-deterioro',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    RiesgoDeterioroEdadesChartComponent,
    RiesgoDeterioroSegmentacionChartComponent
  ],
  templateUrl: './riesgo-deterioro.component.html',
  styleUrls: ['./riesgo-deterioro.component.scss']
})

export class RiesgoDeterioroComponent implements OnInit {
  private readonly api = inject(RiesgoDeterioroApi);
  private readonly exporter = inject(RiesgoDeterioroExporterService);

  cortes: string[] = [];
  fechaCorte = '';
  fechaDesde = '';
  idAgencia: number | null = null;
  idLineaCredito: number | null = null;

  tipoEdad: TipoEdadRiesgo = 'CONTABLE';
  dimension: DimensionRiesgo = 'LINEA';
  criterio: CriterioConcentracion = 'DETERIORO';
  limite = 20;

  resumen: RiesgoDeterioroResumen | null = null;
  edades: RiesgoDeterioroEdad[] = [];
  segmentacion: RiesgoDeterioroSegmento[] = [];
  evolucion: RiesgoDeterioroEvolucion[] = [];
  concentracion: RiesgoDeterioroConcentracion[] = [];
  detalle: RiesgoDeterioroDetalle[] = [];

  cargando = false;
  cargandoDetalle = false;
  error = '';
  detalleVisible = false;
  textoDetalle = '';
  paginaDetalle = 1;
  filasPorPagina = 25;

  readonly tiposEdad: { value: TipoEdadRiesgo; label: string }[] = [
    { value: 'CONTABLE', label: 'Edad contable' },
    { value: 'MORA', label: 'Edad de mora' },
    { value: 'RIESGO', label: 'Edad de riesgo' },
    { value: 'PE', label: 'Edad PE' },
    { value: 'HOMOLOGACION', label: 'Edad homologación' }
  ];

  readonly dimensiones: { value: DimensionRiesgo; label: string }[] = [
    { value: 'AGENCIA', label: 'Agencia' },
    { value: 'LINEA', label: 'Línea de crédito' },
    { value: 'CLASIFICACION', label: 'Clasificación' },
    { value: 'GARANTIA', label: 'Garantía' },
    { value: 'DESTINO', label: 'Destino económico' },
    { value: 'ESTADO_JURIDICO', label: 'Estado jurídico' },
    { value: 'MODIFICACION', label: 'Modificación' },
    { value: 'METODO_CALCULO', label: 'Método de cálculo' }
  ];

  readonly criterios: { value: CriterioConcentracion; label: string }[] = [
    { value: 'DETERIORO', label: 'Deterioro' },
    { value: 'SALDO', label: 'Saldo' },
    { value: 'EXPOSICION', label: 'Exposición' },
    { value: 'PERDIDA_ESPERADA', label: 'Pérdida esperada' }
  ];

  async ngOnInit(): Promise<void> {
    await this.cargarCortes();
  }

  async cargarCortes(): Promise<void> {
    this.error = '';
    try {
      this.cortes = await firstValueFrom(this.api.listarCortes());
      if (this.cortes.length) {
        this.fechaCorte = this.cortes[0];
        this.fechaDesde = this.cortes[Math.min(11, this.cortes.length - 1)] ?? this.fechaCorte;
        await this.analizar();
      }
    } catch (e) {
      console.error(e);
      this.error = 'No fue posible cargar los cortes históricos.';
    }
  }

  async analizar(): Promise<void> {
    if (!this.fechaCorte) return;
    this.cargando = true;
    this.error = '';
    this.detalle = [];
    this.detalleVisible = false;

    try {
      const [resumen, edades, segmentacion, evolucion, concentracion] = await Promise.all([
        firstValueFrom(this.api.resumen(this.fechaCorte, this.idAgencia, this.idLineaCredito)),
        firstValueFrom(this.api.edades(this.fechaCorte, this.tipoEdad, this.idAgencia, this.idLineaCredito)),
        firstValueFrom(this.api.segmentacion(this.fechaCorte, this.dimension, this.idAgencia, this.idLineaCredito)),
        firstValueFrom(this.api.evolucion(this.fechaDesde || this.fechaCorte, this.fechaCorte, this.idAgencia, this.idLineaCredito)),
        firstValueFrom(this.api.concentracion(this.fechaCorte, this.criterio, this.limite, this.idAgencia, this.idLineaCredito))
      ]);

      this.resumen = resumen;
      this.edades = edades;
      this.segmentacion = segmentacion;
      this.evolucion = evolucion;
      this.concentracion = concentracion;
    } catch (e) {
      console.error(e);
      this.error = 'No fue posible ejecutar el análisis de riesgo y deterioro.';
    } finally {
      this.cargando = false;
    }
  }

  async recargarEdades(): Promise<void> {
    if (!this.fechaCorte) return;
    this.edades = await firstValueFrom(this.api.edades(this.fechaCorte, this.tipoEdad, this.idAgencia, this.idLineaCredito));
  }

  async recargarSegmentacion(): Promise<void> {
    if (!this.fechaCorte) return;
    this.segmentacion = await firstValueFrom(this.api.segmentacion(this.fechaCorte, this.dimension, this.idAgencia, this.idLineaCredito));
  }

  async recargarConcentracion(): Promise<void> {
    if (!this.fechaCorte) return;
    this.concentracion = await firstValueFrom(this.api.concentracion(this.fechaCorte, this.criterio, this.limite, this.idAgencia, this.idLineaCredito));
  }

  async cargarDetalle(): Promise<void> {
    if (!this.fechaCorte) return;
    this.cargandoDetalle = true;
    this.error = '';
    try {
      this.detalle = await firstValueFrom(this.api.detalle(this.fechaCorte, this.idAgencia, this.idLineaCredito));
      this.detalleVisible = true;
      this.paginaDetalle = 1;
    } catch (e) {
      console.error(e);
      this.error = 'No fue posible cargar el detalle del análisis.';
    } finally {
      this.cargandoDetalle = false;
    }
  }

  async exportarExcel(): Promise<void> {
    if (!this.resumen) return;
    if (!this.detalle.length) await this.cargarDetalle();
    if (!this.detalle.length) return;

    this.exporter.exportar({
      fechaCorte: this.fechaCorte,
      resumen: this.resumen,
      edades: this.edades,
      segmentacion: this.segmentacion,
      evolucion: this.evolucion,
      concentracion: this.concentracion,
      detalle: this.detalle
    });
  }

  get detalleFiltrado(): RiesgoDeterioroDetalle[] {
    const q = this.textoDetalle.trim().toLowerCase();
    if (!q) return this.detalle;
    return this.detalle.filter(d =>
      [d.documento, d.nombreCompleto, d.pagareCartera, d.nombreLineaCredito, d.edadContable]
        .some(v => String(v ?? '').toLowerCase().includes(q))
    );
  }

  get detallePaginado(): RiesgoDeterioroDetalle[] {
    const desde = (this.paginaDetalle - 1) * this.filasPorPagina;
    return this.detalleFiltrado.slice(desde, desde + this.filasPorPagina);
  }

  get totalPaginasDetalle(): number {
    return Math.max(1, Math.ceil(this.detalleFiltrado.length / this.filasPorPagina));
  }

  paginaAnterior(): void {
    if (this.paginaDetalle > 1) this.paginaDetalle--;
  }

  paginaSiguiente(): void {
    if (this.paginaDetalle < this.totalPaginasDetalle) this.paginaDetalle++;
  }

  money(v: number | null | undefined): string {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(Number(v ?? 0));
  }

  pct(v: number | null | undefined, dec = 2): string {
    return `${Number(v ?? 0).toLocaleString('es-CO', { minimumFractionDigits: dec, maximumFractionDigits: dec })}%`;
  }
}
