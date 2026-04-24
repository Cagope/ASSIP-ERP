import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { EmpleadoAutocompleteComponent } from '../../busqueda/empleado-autocomplete.component';

import {
  NovedadesConceptoInformeApi,
  NovedadesConceptoInformeDTO
} from './novedades-concepto-informe.api';

import {
  ConceptosNominaApi,
  ConceptoNominaListDTO
} from '../../conceptos-nomina/conceptos-nomina.api';

import { NovedadesConceptoInformeExporterService } from './novedades-concepto-informe-exporter.service';

@Component({
  standalone: true,
  selector: 'app-novedades-concepto-informe',
  imports: [CommonModule, FormsModule, EmpleadoAutocompleteComponent],
  templateUrl: './novedades-concepto-informe.component.html',
  styleUrls: ['./novedades-concepto-informe.component.scss']
})
export class NovedadesConceptoInformeComponent implements OnInit {

  private api = inject(NovedadesConceptoInformeApi);
  private conceptosApi = inject(ConceptosNominaApi);
  private exporter = inject(NovedadesConceptoInformeExporterService);

  codigoConcepto: string | null = null;
  conceptos: ConceptoNominaListDTO[] = [];

  idEmpleado: number | null = null;

  fechaInicial = '';
  fechaFinal = '';

  rows: NovedadesConceptoInformeDTO[] = [];

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
      idEmpleado: this.idEmpleado,
      fechaInicial: this.fechaInicial || null,
      fechaFinal: this.fechaFinal || null
    }).subscribe({
      next: data => {
        this.rows = data ?? [];
        this.cargando = false;
      },
      error: err => {
        this.error = 'Error consultando informe';
        this.cargando = false;
      }
    });
  }

  limpiar(): void {
    this.codigoConcepto = null;
    this.idEmpleado = null;
    this.fechaInicial = '';
    this.fechaFinal = '';
    this.rows = [];
    this.error = '';
  }

  exportarExcel(): void {
    this.exporter.exportar(this.rows);
  }

  get totalValor(): number {
    return this.rows.reduce((acc, r) => acc + (Number(r.valor) || 0), 0);
  }
}
