import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { EmpleadoAutocompleteComponent } from '../../busqueda/empleado-autocomplete.component';

import {
  NovedadesEmpleadoInformeApi,
  NovedadesEmpleadoInformeDTO
} from './novedades-empleado-informe.api';

import {
  ConceptosNominaApi,
  ConceptoNominaListDTO
} from '../../conceptos-nomina/conceptos-nomina.api';

import { NovedadesEmpleadoInformeExporterService } from './novedades-empleado-informe-exporter.service';

@Component({
  standalone: true,
  selector: 'app-novedades-empleado-informe',
  imports: [
    CommonModule,
    FormsModule,
    EmpleadoAutocompleteComponent
  ],
  templateUrl: './novedades-empleado-informe.component.html',
  styleUrls: ['./novedades-empleado-informe.component.scss']
})
export class NovedadesEmpleadoInformeComponent {

  private api = inject(NovedadesEmpleadoInformeApi);
  private conceptosApi = inject(ConceptosNominaApi);
  private exporter = inject(NovedadesEmpleadoInformeExporterService);

  conceptos: ConceptoNominaListDTO[] = [];
  codigoConcepto: string | null = null;

  ngOnInit(): void {
    this.cargarConceptos();
  }

  private cargarConceptos(): void {
    this.conceptosApi.listar().subscribe({
      next: data => this.conceptos = data ?? [],
      error: () => this.conceptos = []
    });
  }

  documento = '';

  fechaInicial = '';
  fechaFinal = '';

  idEmpleado: number | null = null;
  empleadoSeleccionado: any = null;

  rows: NovedadesEmpleadoInformeDTO[] = [];

  cargando = false;
  error = '';

  consultar(): void {
    this.error = '';

    if (this.fechaInicial && this.fechaFinal && this.fechaFinal < this.fechaInicial) {
      this.error = 'La fecha final no puede ser menor que la fecha inicial.';
      return;
    }

    this.cargando = true;

    this.api.consultar({
      documento: this.documento?.trim() || null,
      idEmpleado: this.idEmpleado,
      codigoConcepto: this.codigoConcepto?.trim() || null,
      fechaInicial: this.fechaInicial || null,
      fechaFinal: this.fechaFinal || null
    }).subscribe({
      next: data => {
        this.rows = data ?? [];
        this.cargando = false;
      },
      error: err => {
        this.error = err?.error?.message || 'No fue posible consultar el informe.';
        this.cargando = false;
      }
    });
  }

  limpiar(): void {
    this.documento = '';
    this.codigoConcepto = '';
    this.fechaInicial = '';
    this.fechaFinal = '';
    this.idEmpleado = null;
    this.empleadoSeleccionado = null;
    this.rows = [];
    this.error = '';
    this.codigoConcepto = null;
  }

  get totalValor(): number {
    return this.rows.reduce((acc, r) => acc + (Number(r.valor) || 0), 0);
  }

  exportarExcel(): void {
    this.exporter.exportar(this.rows);
  }

}
