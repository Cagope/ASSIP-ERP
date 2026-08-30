import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import {
  CuracionReincidenciaControl,
  CuracionReincidenciaDetalle,
  CuracionReincidenciaDistribucionCura,
  CuracionReincidenciaEdadEntrada,
  CuracionReincidenciaPeriodo,
  CuracionReincidenciaPrimeraReincidencia,
  CuracionReincidenciaResumen,
  CuracionReincidenciaSegmento,
  IndicadorDetalle
} from './curacion-reincidencia.models';
import { CuracionReincidenciaService } from './curacion-reincidencia.service';

@Component({
  selector: 'app-curacion-reincidencia',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './curacion-reincidencia.component.html',
  styleUrl: './curacion-reincidencia.component.scss'
})
export class CuracionReincidenciaComponent implements OnInit {

  control: CuracionReincidenciaControl | null = null;
  resumen: CuracionReincidenciaResumen | null = null;

  periodos: CuracionReincidenciaPeriodo[] = [];
  agencias: CuracionReincidenciaSegmento[] = [];
  lineas: CuracionReincidenciaSegmento[] = [];
  edadesEntrada: CuracionReincidenciaEdadEntrada[] = [];
  distribucionCura: CuracionReincidenciaDistribucionCura[] = [];
  primeraReincidencia: CuracionReincidenciaPrimeraReincidencia[] = [];
  detalle: CuracionReincidenciaDetalle[] = [];

  periodoDesde = '';
  periodoHasta = '';
  idAgencia: number | null = null;
  idLineaCredito: number | null = null;
  edadEntrada = '';

  indicadorDetalle: IndicadorDetalle = 'TODOS';
  textoDetalle = '';

  cargando = false;
  cargandoDetalle = false;
  error = '';

  private catalogoAgencias: CuracionReincidenciaSegmento[] = [];
  private catalogoLineas: CuracionReincidenciaSegmento[] = [];

  constructor(
    private readonly service: CuracionReincidenciaService
  ) {}

  ngOnInit(): void {
    this.cargarControl();
  }

  cargarControl(): void {
    this.cargando = true;
    this.error = '';

    this.service.control().subscribe({
      next: control => {
        this.control = control;
        this.periodoDesde = control.periodoDesdeSugerido;
        this.periodoHasta = control.periodoHastaSugerido;
        this.cargarAnalisis();
      },
      error: err => {
        this.cargando = false;
        this.error = this.mensajeError(err);
      }
    });
  }

  cargarAnalisis(): void {
    if (!this.periodoDesde || !this.periodoHasta) {
      this.error = 'Seleccione el período desde y hasta.';
      return;
    }

    this.cargando = true;
    this.error = '';

    const desde = this.periodoDesde;
    const hasta = this.periodoHasta;
    const agencia = this.idAgencia;
    const linea = this.idLineaCredito;
    const edad = this.edadEntrada || null;

    forkJoin({
      resumen: this.service.resumen(desde, hasta, agencia, linea, edad),
      periodos: this.service.periodos(desde, hasta, agencia, linea, edad),
      agencias: this.service.agencias(desde, hasta, agencia, linea, edad),
      lineas: this.service.lineas(desde, hasta, agencia, linea, edad),
      edades: this.service.edadesEntrada(desde, hasta, agencia, linea, edad),
      distribucion: this.service.distribucionCura(desde, hasta, agencia, linea, edad),
      reincidencia: this.service.primeraReincidencia(desde, hasta, agencia, linea, edad)
    }).subscribe({
      next: r => {
        this.resumen = r.resumen;
        this.periodos = r.periodos.value ?? [];
        this.agencias = r.agencias.value ?? [];
        this.lineas = r.lineas.value ?? [];
        this.edadesEntrada = r.edades.value ?? [];
        this.distribucionCura = r.distribucion.value ?? [];
        this.primeraReincidencia = r.reincidencia.value ?? [];

        if (!this.idAgencia) {
          this.catalogoAgencias = [...this.agencias];
        }
        if (!this.idLineaCredito) {
          this.catalogoLineas = [...this.lineas];
        }

        this.cargando = false;
        this.cargarDetalle();
      },
      error: err => {
        this.cargando = false;
        this.error = this.mensajeError(err);
      }
    });
  }

  cargarDetalle(): void {
    if (!this.periodoDesde || !this.periodoHasta) {
      return;
    }

    this.cargandoDetalle = true;

    this.service.detalle(
      this.periodoDesde,
      this.periodoHasta,
      this.indicadorDetalle,
      this.idAgencia,
      this.idLineaCredito,
      this.edadEntrada || null
    ).subscribe({
      next: r => {
        this.detalle = r.value ?? [];
        this.cargandoDetalle = false;
      },
      error: err => {
        this.detalle = [];
        this.cargandoDetalle = false;
        this.error = this.mensajeError(err);
      }
    });
  }

  limpiarFiltros(): void {
    if (!this.control) {
      return;
    }

    this.periodoDesde = this.control.periodoDesdeSugerido;
    this.periodoHasta = this.control.periodoHastaSugerido;
    this.idAgencia = null;
    this.idLineaCredito = null;
    this.edadEntrada = '';
    this.indicadorDetalle = 'TODOS';
    this.textoDetalle = '';
    this.cargarAnalisis();
  }

  detalleFiltrado(): CuracionReincidenciaDetalle[] {
    const t = this.textoDetalle.trim().toLowerCase();

    if (!t) {
      return this.detalle;
    }

    return this.detalle.filter(d =>
      String(d.documento ?? '').toLowerCase().includes(t) ||
      String(d.nombreCompleto ?? '').toLowerCase().includes(t) ||
      String(d.pagareCartera ?? '').toLowerCase().includes(t) ||
      String(d.nombreAgencia ?? '').toLowerCase().includes(t) ||
      String(d.nombreLineaCredito ?? '').toLowerCase().includes(t)
    );
  }

  porcentajeBarra(valor: number | null | undefined): number {
    if (valor == null || !Number.isFinite(valor)) {
      return 0;
    }
    return Math.max(0, Math.min(100, valor));
  }

  maxDistribucion(): number {
    return Math.max(1, ...this.distribucionCura.map(x => x.episodios));
  }

  maxPrimeraReincidencia(): number {
    return Math.max(1, ...this.primeraReincidencia.map(x => x.reincidentes));
  }

  anchoDistribucion(valor: number): number {
    return (valor / this.maxDistribucion()) * 100;
  }

  anchoPrimeraReincidencia(valor: number): number {
    return (valor / this.maxPrimeraReincidencia()) * 100;
  }

  trackByPeriodo(_: number, item: CuracionReincidenciaPeriodo): string {
    return item.periodoInicio;
  }

  trackBySegmento(_: number, item: CuracionReincidenciaSegmento): number {
    return item.id;
  }

  trackByEdad(_: number, item: CuracionReincidenciaEdadEntrada): string {
    return item.edadEntrada;
  }

  trackByDetalle(_: number, item: CuracionReincidenciaDetalle): string {
    return `${item.idCarteraCredito}-${item.numeroEpisodio}`;
  }

  private mensajeError(err: any): string {
    return err?.error?.message
      ?? err?.error?.error
      ?? err?.message
      ?? 'No fue posible cargar el análisis de Curación y Reincidencia.';
  }

  get opcionesAgencia(): CuracionReincidenciaSegmento[] {
    return this.catalogoAgencias.length ? this.catalogoAgencias : this.agencias;
  }

  get opcionesLinea(): CuracionReincidenciaSegmento[] {
    return this.catalogoLineas.length ? this.catalogoLineas : this.lineas;
  }
}
