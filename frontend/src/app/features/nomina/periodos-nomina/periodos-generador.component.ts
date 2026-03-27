import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';

import { GeneralApi } from '../../../shared/general/general.api';
import { PeriodosGeneradorApi } from './periodos-generador.api';

@Component({
  standalone: true,
  selector: 'app-periodos-generador',
  templateUrl: './periodos-generador.component.html',
  styleUrls: ['./periodos-generador.component.scss'],
  imports: [CommonModule, FormsModule, RouterModule],
})
export class PeriodosGeneradorComponent implements OnInit {

  private readonly api = inject(PeriodosGeneradorApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly router = inject(Router);

  loading = false;

  agencias: any[] = [];

  // ✅ captura completa
  form = {
    idAgencia: null as number | null,
    anio: new Date().getFullYear(),
    tipoPeriodo: 'QUINCENAL' // default recomendado
  };

  ngOnInit(): void {
    this.cargarAgencias();
  }

  // =========================
  // Cargar agencias
  // =========================
  cargarAgencias(): void {

    this.generalApi.listarAgencias().subscribe({
      next: (data: any[]) => this.agencias = data ?? [],
      error: (err: any) => {
        console.error('Error cargando agencias', err);
        this.agencias = [];
      }
    });
  }

  // =========================
  // Generar períodos
  // =========================
  generar(): void {

    if (!this.form.idAgencia) {
      alert('La agencia es obligatoria.');
      return;
    }

    if (!this.form.anio) {
      alert('El año es obligatorio.');
      return;
    }

    if (!this.form.tipoPeriodo) {
      alert('Seleccione el tipo de período.');
      return;
    }

    this.loading = true;

    this.api.generar(this.form).subscribe({
      next: () => {
        this.loading = false;
        alert('Períodos generados correctamente.');
        this.volver();
      },
      error: (err) => {
        console.error('Error generando períodos', err);
        this.loading = false;
        alert('No se pudieron generar los períodos.');
      }
    });
  }

  // =========================
  // Volver
  // =========================
  volver(): void {
    this.router.navigate(['/nomina/periodos']);
  }
}
