import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { RollForwardApi } from './roll-forward.api';
import { RollForwardExporterService } from './roll-forward-exporter.service';
import {
  RollForwardCorte,
  RollForwardDetalle,
  RollForwardLinea,
  RollForwardResumen,
  RollForwardVista
} from './roll-forward.models';

@Component({
  selector: 'app-roll-forward',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './roll-forward.component.html',
  styleUrls: ['./roll-forward.component.scss']
})
export class RollForwardComponent implements OnInit {
  private readonly api = inject(RollForwardApi);
  private readonly exporter = inject(RollForwardExporterService);

  cortes: RollForwardCorte[] = [];
  fechaCorte = '';

  resumen: RollForwardResumen | null = null;
  lineas: RollForwardLinea[] = [];
  historico: RollForwardResumen[] = [];
  tiposMovimiento: string[] = [];

  detalle: RollForwardDetalle[] = [];
  tipoMovimiento = '';
  idLineaCredito: number | null = null;
  textoDetalle = '';

  vista: RollForwardVista = 'RESUMEN';

  cargando = false;
  cargandoDetalle = false;
  exportando = false;
  error = '';

  paginaDetalle = 1;
  filasPorPagina = 25;

  async ngOnInit(): Promise<void> {
    await this.inicializar();
  }

  async inicializar(): Promise<void> {
    this.cargando = true;
    this.error = '';

    try {
      const [cortes, historico] = await Promise.all([
        firstValueFrom(this.api.cortes()),
        firstValueFrom(this.api.historico())
      ]);

      this.cortes = cortes ?? [];
      this.historico = historico ?? [];
      this.fechaCorte = this.cortes[0]?.fechaCorte ?? '';

      if (this.fechaCorte) {
        await this.cargarPeriodo();
      }
    } catch (e) {
      this.manejarError(e, 'No fue posible inicializar el análisis de Roll Forward.');
    } finally {
      this.cargando = false;
    }
  }

  async cambiarCorte(): Promise<void> {
    if (!this.fechaCorte) return;

    this.tipoMovimiento = '';
    this.idLineaCredito = null;
    this.textoDetalle = '';
    this.detalle = [];
    this.paginaDetalle = 1;

    await this.cargarPeriodo();
  }

  async cargarPeriodo(): Promise<void> {
    if (!this.fechaCorte) return;

    this.cargando = true;
    this.error = '';

    try {
      const [resumen, lineas, tipos] = await Promise.all([
        firstValueFrom(this.api.resumen(this.fechaCorte)),
        firstValueFrom(this.api.lineas(this.fechaCorte)),
        firstValueFrom(this.api.tiposMovimiento(this.fechaCorte))
      ]);

      this.resumen = resumen;
      this.lineas = lineas ?? [];
      this.tiposMovimiento = tipos ?? [];
    } catch (e) {
      this.manejarError(e, 'No fue posible cargar el período seleccionado.');
    } finally {
      this.cargando = false;
    }
  }

  async cambiarVista(vista: RollForwardVista): Promise<void> {
    this.vista = vista;

    if (vista === 'DETALLE' && this.detalle.length === 0) {
      await this.cargarDetalle();
    }
  }

  async cargarDetalle(): Promise<void> {
    if (!this.fechaCorte) return;

    this.cargandoDetalle = true;
    this.error = '';

    try {
      this.detalle = await firstValueFrom(
        this.api.detalle(
          this.fechaCorte,
          this.tipoMovimiento || null,
          this.idLineaCredito
        )
      );
      this.paginaDetalle = 1;
    } catch (e) {
      this.manejarError(e, 'No fue posible cargar el detalle de Roll Forward.');
    } finally {
      this.cargandoDetalle = false;
    }
  }

  async verMovimiento(tipo: string): Promise<void> {
    this.tipoMovimiento = tipo;
    this.idLineaCredito = null;
    this.textoDetalle = '';
    this.vista = 'DETALLE';
    await this.cargarDetalle();
  }

  async verLinea(linea: RollForwardLinea): Promise<void> {
    this.tipoMovimiento = '';
    this.idLineaCredito = linea.idLineaCredito;
    this.textoDetalle = '';
    this.vista = 'DETALLE';
    await this.cargarDetalle();
  }

  async limpiarFiltrosDetalle(): Promise<void> {
    this.tipoMovimiento = '';
    this.idLineaCredito = null;
    this.textoDetalle = '';
    await this.cargarDetalle();
  }

  async exportarExcel(): Promise<void> {
    if (!this.resumen || !this.fechaCorte || this.exportando) return;

    this.exportando = true;
    this.error = '';

    try {
      const detalleCompleto = await firstValueFrom(
        this.api.detalle(this.fechaCorte)
      );

      this.exporter.exportar({
        resumen: this.resumen,
        lineas: this.lineas,
        historico: this.historico,
        detalle: detalleCompleto ?? []
      });
    } catch (e) {
      this.manejarError(e, 'No fue posible generar el informe Excel de Roll Forward.');
    } finally {
      this.exportando = false;
    }
  }

  get detalleFiltrado(): RollForwardDetalle[] {
    const q = this.textoDetalle.trim().toLowerCase();
    if (!q) return this.detalle;

    return this.detalle.filter(d =>
      [
        d.tipoMovimiento,
        d.documento,
        d.nombreCompleto,
        d.pagareCartera,
        d.codigoLineaCredito,
        d.nombreLineaCredito,
        d.codigoClasificacionCredito,
        d.descripcionClasificacionCredito,
        d.codigoDestinoEconomico,
        d.descripcionDestinoEconomico,
        d.rangoSalida
      ].some(v => String(v ?? '').toLowerCase().includes(q))
    );
  }

  get detallePaginado(): RollForwardDetalle[] {
    const desde = (this.paginaDetalle - 1) * this.filasPorPagina;
    return this.detalleFiltrado.slice(desde, desde + this.filasPorPagina);
  }

  get totalPaginasDetalle(): number {
    return Math.max(
      1,
      Math.ceil(this.detalleFiltrado.length / this.filasPorPagina)
    );
  }

  paginaAnterior(): void {
    if (this.paginaDetalle > 1) {
      this.paginaDetalle--;
    }
  }

  paginaSiguiente(): void {
    if (this.paginaDetalle < this.totalPaginasDetalle) {
      this.paginaDetalle++;
    }
  }

  nombreMovimiento(tipo: string | null | undefined): string {
    switch (tipo) {
      case 'NUEVO': return 'Nuevo';
      case 'REINGRESO': return 'Reingreso';
      case 'PERMANECE_DISMINUYE': return 'Permanece - Disminuye';
      case 'PERMANECE_IGUAL': return 'Permanece - Igual';
      case 'PERMANECE_AUMENTA': return 'Permanece - Aumenta';
      case 'AUSENCIA_TEMPORAL': return 'Ausencia temporal';
      case 'SALIDA_PREPAGO': return 'Prepago';
      case 'SALIDA_NORMAL': return 'Cancelación normal';
      case 'SALIDA_POST_VENCIMIENTO': return 'Cancelación post vencimiento';
      case 'SALIDA_OTRA': return 'Otra salida';
      default: return tipo || '-';
    }
  }

  claseMovimiento(tipo: string | null | undefined): string {
    switch (tipo) {
      case 'NUEVO':
      case 'REINGRESO':
        return 'estado estado--entrada';
      case 'PERMANECE_DISMINUYE':
        return 'estado estado--reduccion';
      case 'PERMANECE_IGUAL':
        return 'estado estado--igual';
      case 'PERMANECE_AUMENTA':
        return 'estado estado--aumento';
      case 'AUSENCIA_TEMPORAL':
        return 'estado estado--temporal';
      case 'SALIDA_PREPAGO':
      case 'SALIDA_NORMAL':
      case 'SALIDA_POST_VENCIMIENTO':
      case 'SALIDA_OTRA':
        return 'estado estado--salida';
      default:
        return 'estado';
    }
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

  trackLinea(_: number, item: RollForwardLinea): number {
    return item.idLineaCredito;
  }

  trackDetalle(index: number, item: RollForwardDetalle): string {
    return `${item.idCarteraCredito}-${item.tipoMovimiento}-${index}`;
  }

  private manejarError(e: any, mensaje: string): void {
    console.error(e);
    this.error = e?.error?.message || e?.error || mensaje;
  }
}
