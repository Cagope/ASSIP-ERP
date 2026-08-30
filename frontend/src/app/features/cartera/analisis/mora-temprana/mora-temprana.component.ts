import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { MoraTempranaApi } from './mora-temprana.api';
import { MoraTempranaCosechasChartComponent } from './mora-temprana-cosechas-chart.component';
import { MoraTempranaExporterService } from './mora-temprana-exporter.service';
import { MoraTempranaPrimeraMoraChartComponent } from './mora-temprana-primera-mora-chart.component';
import { MoraTempranaSegmentosChartComponent } from './mora-temprana-segmentos-chart.component';
import {
  IndicadorMoraTemprana,
  MoraTempranaCosecha,
  MoraTempranaDetalle,
  MoraTempranaPrimeraMora,
  MoraTempranaResumen,
  MoraTempranaSegmento
} from './mora-temprana.models';

@Component({
  selector: 'app-mora-temprana',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    MoraTempranaCosechasChartComponent,
    MoraTempranaSegmentosChartComponent,
    MoraTempranaPrimeraMoraChartComponent
  ],
  templateUrl: './mora-temprana.component.html',
  styleUrls: ['./mora-temprana.component.scss']
})
export class MoraTempranaComponent implements OnInit {
  private readonly api = inject(MoraTempranaApi);
  private readonly exporter = inject(MoraTempranaExporterService);

  cosechaDesde = '';
  cosechaHasta = '';
  ultimaCosechaMadura = '';

  idAgencia: number | null = null;
  idLineaCredito: number | null = null;

  resumen: MoraTempranaResumen | null = null;
  cosechas: MoraTempranaCosecha[] = [];
  agencias: MoraTempranaSegmento[] = [];
  lineas: MoraTempranaSegmento[] = [];
  primeraMora: MoraTempranaPrimeraMora[] = [];
  detalle: MoraTempranaDetalle[] = [];

  catalogoAgencias: MoraTempranaSegmento[] = [];
  catalogoLineas: MoraTempranaSegmento[] = [];

  indicadorDetalle: IndicadorMoraTemprana = 'TODOS';
  detalleVisible = false;
  cargando = false;
  cargandoDetalle = false;
  error = '';
  textoDetalle = '';
  paginaDetalle = 1;
  filasPorPagina = 25;

  readonly indicadoresDetalle: { value: IndicadorMoraTemprana; label: string }[] = [
    { value: 'TODOS', label: 'Todos los créditos originados' },
    { value: 'MORA30_MOB3', label: '30+ hasta MOB3' },
    { value: 'MORA30_MOB6', label: '30+ hasta MOB6' },
    { value: 'MORA60_MOB6', label: '60+ hasta MOB6' }
  ];

  async ngOnInit(): Promise<void> {
    await this.inicializar();
  }

  async inicializar(): Promise<void> {
    this.error = '';
    try {
      const control = await firstValueFrom(this.api.control());
      this.ultimaCosechaMadura = this.mes(control.ultimaCosechaMadura);
      this.cosechaHasta = this.ultimaCosechaMadura;
      this.cosechaDesde = this.restarMeses(this.cosechaHasta, 11);
      await this.analizar();
    } catch (e) {
      console.error(e);
      this.error = 'No fue posible inicializar el análisis de mora temprana.';
    }
  }

  async analizar(): Promise<void> {
    if (!this.cosechaDesde || !this.cosechaHasta) return;
    if (this.cosechaDesde > this.cosechaHasta) {
      this.error = 'La cosecha desde no puede ser posterior a la cosecha hasta.';
      return;
    }

    this.cargando = true;
    this.error = '';
    this.detalle = [];
    this.detalleVisible = false;

    const desde = this.fechaApi(this.cosechaDesde);
    const hasta = this.fechaApi(this.cosechaHasta);

    try {
      const [resumen, cosechas, agencias, lineas, primeraMora] = await Promise.all([
        firstValueFrom(this.api.resumen(desde, hasta, this.idAgencia, this.idLineaCredito)),
        firstValueFrom(this.api.cosechas(desde, hasta, this.idAgencia, this.idLineaCredito)),
        firstValueFrom(this.api.agencias(desde, hasta, this.idAgencia, this.idLineaCredito)),
        firstValueFrom(this.api.lineas(desde, hasta, this.idAgencia, this.idLineaCredito)),
        firstValueFrom(this.api.primeraMora(desde, hasta, this.idAgencia, this.idLineaCredito))
      ]);

      this.resumen = resumen;
      this.cosechas = cosechas;
      this.agencias = agencias;
      this.lineas = lineas;
      this.primeraMora = primeraMora;

      if (this.idAgencia == null) this.catalogoAgencias = agencias;
      if (this.idLineaCredito == null) this.catalogoLineas = lineas;
    } catch (e) {
      console.error(e);
      this.error = 'No fue posible ejecutar el análisis de mora temprana.';
    } finally {
      this.cargando = false;
    }
  }

  async cargarDetalle(indicador?: IndicadorMoraTemprana): Promise<void> {
    if (indicador) this.indicadorDetalle = indicador;
    if (!this.cosechaDesde || !this.cosechaHasta) return;

    this.cargandoDetalle = true;
    this.error = '';
    try {
      this.detalle = await firstValueFrom(this.api.detalle(
        this.fechaApi(this.cosechaDesde),
        this.fechaApi(this.cosechaHasta),
        this.indicadorDetalle,
        this.idAgencia,
        this.idLineaCredito
      ));
      this.detalleVisible = true;
      this.paginaDetalle = 1;
    } catch (e) {
      console.error(e);
      this.error = 'No fue posible cargar el detalle de mora temprana.';
    } finally {
      this.cargandoDetalle = false;
    }
  }

  async exportarExcel(): Promise<void> {
    if (!this.resumen) return;
    if (!this.detalle.length || this.indicadorDetalle !== 'TODOS') {
      this.indicadorDetalle = 'TODOS';
      await this.cargarDetalle();
    }
    if (!this.detalle.length) return;

    this.exporter.exportar({
      resumen: this.resumen,
      cosechas: this.cosechas,
      agencias: this.agencias,
      lineas: this.lineas,
      primeraMora: this.primeraMora,
      detalle: this.detalle
    });
  }

  abrirDetalle(indicador: IndicadorMoraTemprana): void {
    void this.cargarDetalle(indicador);
  }

  get detalleFiltrado(): MoraTempranaDetalle[] {
    const q = this.textoDetalle.trim().toLowerCase();
    if (!q) return this.detalle;
    return this.detalle.filter(d =>
      [
        d.documento, d.nombreCompleto, d.pagareCartera,
        d.codigoAgencia, d.nombreAgencia,
        d.codigoLineaCredito, d.nombreLineaCredito
      ].some(v => String(v ?? '').toLowerCase().includes(q))
    );
  }

  get detallePaginado(): MoraTempranaDetalle[] {
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
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      maximumFractionDigits: 0
    }).format(Number(v ?? 0));
  }

  pct(v: number | null | undefined, dec = 2): string {
    return `${Number(v ?? 0).toLocaleString('es-CO', {
      minimumFractionDigits: dec,
      maximumFractionDigits: dec
    })}%`;
  }

  private fechaApi(mes: string): string {
    return `${mes.substring(0, 7)}-01`;
  }

  private mes(fecha: string): string {
    return fecha ? fecha.substring(0, 7) : '';
  }

  private restarMeses(mes: string, cantidad: number): string {
    const [anio, m] = mes.split('-').map(Number);
    const fecha = new Date(anio, m - 1 - cantidad, 1);
    return `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, '0')}`;
  }
}
