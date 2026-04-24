import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  ConsolidadoConceptosInformeApi,
  ConsolidadoConceptosInformeDTO
} from './consolidado-conceptos-informe.api';

import {
  ConceptosNominaApi,
  ConceptoNominaListDTO
} from '../../conceptos-nomina/conceptos-nomina.api';

import { ConsolidadoConceptosInformeExporterService } from './consolidado-conceptos-informe-exporter.service';

@Component({
  standalone: true,
  selector: 'app-consolidado-conceptos-informe',
  imports: [CommonModule, FormsModule],
  templateUrl: './consolidado-conceptos-informe.component.html',
  styleUrls: ['./consolidado-conceptos-informe.component.scss']
})
export class ConsolidadoConceptosInformeComponent implements OnInit {

  private api = inject(ConsolidadoConceptosInformeApi);
  private conceptosApi = inject(ConceptosNominaApi);
  private exporter = inject(ConsolidadoConceptosInformeExporterService);

  conceptos: ConceptoNominaListDTO[] = [];
  codigoConcepto: string | null = null;

  tipoConcepto: string | null = null;

  fechaInicial = '';
  fechaFinal = '';

  rows: ConsolidadoConceptosInformeDTO[] = [];

  cargando = false;
  error = '';

  ngOnInit(): void {
    this.cargarConceptos();
  }

  cargarConceptos(): void {
    this.conceptosApi.listar().subscribe({
      next: data => this.conceptos = data ?? [],
      error: () => this.conceptos = []
    });
  }

  consultar(): void {
    this.error = '';
    this.cargando = true;

    this.api.consultar({
      codigoConcepto: this.codigoConcepto,
      tipoConcepto: this.tipoConcepto,
      fechaInicial: this.fechaInicial || null,
      fechaFinal: this.fechaFinal || null
    }).subscribe({
      next: data => {
        this.rows = data ?? [];
        this.cargando = false;
      },
      error: () => {
        this.error = 'Error consultando informe';
        this.cargando = false;
      }
    });
  }

  limpiar(): void {
    this.codigoConcepto = null;
    this.tipoConcepto = null;
    this.fechaInicial = '';
    this.fechaFinal = '';
    this.rows = [];
    this.error = '';
  }

  exportarExcel(): void {
    this.exporter.exportar(this.rows);
  }

  get totalGeneral(): number {
    return this.rows.reduce((acc, r) => acc + (Number(r.totalValor) || 0), 0);
  }
}
