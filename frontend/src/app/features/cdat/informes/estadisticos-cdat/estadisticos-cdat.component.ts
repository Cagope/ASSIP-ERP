import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { forkJoin, Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

import { EstadisticosCdatApi } from './estadisticos-cdat.api';

import {
  EstadisticosCdatBloqueCompleto,
  EstadisticosCdatBloqueDetalleItem,
  EstadisticosCdatDetalle,
  EstadisticosCdatGrupo,
  EstadisticosCdatResumen,
  EstadisticosCdatTasa
} from './estadisticos-cdat.models';

import { EstadisticosCdatPrintService } from './estadisticos-cdat-print.service';
import { EstadisticosCdatExporterService } from './estadisticos-cdat-exporter.service';

@Component({
  selector: 'app-estadisticos-cdat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './estadisticos-cdat.component.html',
  styleUrls: ['./estadisticos-cdat.component.scss']
})
export class EstadisticosCdatComponent {

  fechaCorteActual =
    new Date().toISOString().substring(0, 10);

  fechaCorteAnterior = '';

  cargando = false;
  error = '';

  cargandoDetalle = false;
  errorDetalle = '';

  detalleTitulo = '';
  detalleTipoBloque = '';
  detalle: EstadisticosCdatDetalle[] = [];

  resumen: EstadisticosCdatResumen | null = null;

  rangos: EstadisticosCdatGrupo[] = [];
  amortizacion: EstadisticosCdatGrupo[] = [];
  plazos: EstadisticosCdatGrupo[] = [];
  tasas: EstadisticosCdatTasa[] = [];
  plazosDetalle: EstadisticosCdatGrupo[] = [];
  tasasDetalle: EstadisticosCdatTasa[] = [];

  constructor(
    private api: EstadisticosCdatApi,
    private printService: EstadisticosCdatPrintService,
    private exporterService: EstadisticosCdatExporterService
  ) {
  }

  consultar(): void {

    this.error = '';
    this.errorDetalle = '';
    this.cargando = true;

    this.resumen = null;

    this.rangos = [];
    this.amortizacion = [];
    this.plazos = [];
    this.tasas = [];
    this.plazosDetalle = [];
    this.tasasDetalle = [];

    this.limpiarDetalle();

    this.api.consultar({
      fechaCorteActual: this.fechaCorteActual,
      fechaCorteAnterior: this.fechaCorteAnterior || null
    }).subscribe({
      next: resp => {

        this.resumen = resp.resumen;

        this.rangos = resp.rangos || [];
        this.amortizacion = resp.amortizacion || [];
        this.plazos = resp.plazos || [];
        this.tasas = resp.tasas || [];
        this.plazosDetalle = resp.plazosDetalle || [];
        this.tasasDetalle = resp.tasasDetalle || [];
      },
      error: err => {

        this.error =
          err.error?.message
          || 'No fue posible consultar los estadísticos CDAT.';
      },
      complete: () => {
        this.cargando = false;
      }
    });
  }

  verDetalle(
    tipoBloque: string,
    concepto: string
  ): void {

    this.errorDetalle = '';
    this.detalle = [];
    this.cargandoDetalle = true;

    this.detalleTipoBloque = tipoBloque;
    this.detalleTitulo = concepto;

    this.api.detalle({
      fechaCorte: this.fechaCorteActual,
      tipoBloque,
      concepto
    }).subscribe({
      next: resp => {
        this.detalle = resp || [];
      },
      error: err => {
        this.errorDetalle =
          err.error?.message
          || 'No fue posible consultar el detalle del bloque.';
      },
      complete: () => {
        this.cargandoDetalle = false;
      }
    });
  }

  private cargarBloqueCompleto<TResumen extends { concepto?: string; tasa?: string }>(
    titulo: string,
    tipoBloque: string,
    registros: TResumen[],
    obtenerConcepto: (registro: TResumen) => string
  ): Observable<EstadisticosCdatBloqueCompleto<TResumen>> {

    if (!registros || registros.length === 0) {
      return of({
        titulo,
        tipoBloque,
        fechaCorte: this.fechaCorteActual,
        grupos: []
      });
    }

    const consultas = registros.map(registro => {

      const concepto = obtenerConcepto(registro);

      return this.api.detalle({
        fechaCorte: this.fechaCorteActual,
        tipoBloque,
        concepto
      }).pipe(
        map(detalle => ({
          concepto,
          resumen: registro,
          detalle: detalle || []
        } as EstadisticosCdatBloqueDetalleItem<TResumen>))
      );
    });

    return forkJoin(consultas).pipe(
      map(grupos => ({
        titulo,
        tipoBloque,
        fechaCorte: this.fechaCorteActual,
        grupos
      }))
    );
  }

  imprimirBloque(
    titulo: string,
    tipoBloque: string,
    registros: any[],
    obtenerConcepto: (registro: any) => string
  ): void {

    this.cargandoDetalle = true;
    this.errorDetalle = '';

    this.cargarBloqueCompleto(
      titulo,
      tipoBloque,
      registros,
      obtenerConcepto
    ).subscribe({
      next: bloque => {

        this.printService.imprimirBloque(bloque);

        // En el siguiente paso conectamos el print service de bloque.
      },
      error: err => {

        this.errorDetalle =
          err.error?.message
          || 'No fue posible preparar la impresión del bloque.';
      },
      complete: () => {
        this.cargandoDetalle = false;
      }
    });
  }

  exportarBloque(
    titulo: string,
    tipoBloque: string,
    registros: any[],
    obtenerConcepto: (registro: any) => string
  ): void {

    this.cargandoDetalle = true;
    this.errorDetalle = '';

    this.cargarBloqueCompleto(
      titulo,
      tipoBloque,
      registros,
      obtenerConcepto
    ).subscribe({
      next: bloque => {

        this.exporterService.exportarBloque(bloque);

        // En el siguiente paso conectamos el exporter service de bloque.
      },
      error: err => {

        this.errorDetalle =
          err.error?.message
          || 'No fue posible preparar la exportación del bloque.';
      },
      complete: () => {
        this.cargandoDetalle = false;
      }
    });
  }

  mostrarDetalle(
    tipoBloque: string
  ): boolean {

    return this.detalleTipoBloque === tipoBloque
      && this.detalle.length > 0;
  }

  mostrarCargandoDetalle(
    tipoBloque: string
  ): boolean {

    return this.detalleTipoBloque === tipoBloque
      && this.cargandoDetalle;
  }

  mostrarErrorDetalle(
    tipoBloque: string
  ): boolean {

    return this.detalleTipoBloque === tipoBloque
      && !!this.errorDetalle;
  }

  limpiarDetalle(): void {
    this.detalle = [];
    this.detalleTitulo = '';
    this.detalleTipoBloque = '';
    this.errorDetalle = '';
    this.cargandoDetalle = false;
  }

  imprimir(): void {

    this.printService.imprimir(
      this.fechaCorteActual,
      this.fechaCorteAnterior,
      this.resumen,
      this.rangos,
      this.amortizacion,
      this.plazos,
      this.plazosDetalle,
      this.tasas,
      this.tasasDetalle
    );
  }

  exportarExcel(): void {

    this.exporterService.exportar(
      this.fechaCorteActual,
      this.fechaCorteAnterior,
      this.resumen,
      this.rangos,
      this.amortizacion,
      this.plazos,
      this.plazosDetalle,
      this.tasas,
      this.tasasDetalle
    );
  }

  volver(): void {
    history.back();
  }

  obtenerConceptoGrupo(
    registro: EstadisticosCdatGrupo
  ): string {

    return registro.concepto;
  }

  obtenerConceptoTasa(
    registro: EstadisticosCdatTasa
  ): string {

    return registro.tasa;
  }

}
