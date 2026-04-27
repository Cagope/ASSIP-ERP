import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import {
  VariablesVigenciaApi,
  VariablesVigenciaFormDTO
} from './variables-vigencia.api';

@Component({
  standalone: true,
  selector: 'app-variables-vigencia-upsert',
  templateUrl: './variables-vigencia-upsert.component.html',
  styleUrls: ['./variables-vigencia-upsert.component.scss'],
  imports: [CommonModule, FormsModule, RouterModule, HeaderActionsComponent],
})
export class VariablesVigenciaUpsertComponent implements OnInit {

  private readonly api = inject(VariablesVigenciaApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  id: number | null = null;
  loading = false;
  guardando = false;

  form: VariablesVigenciaFormDTO = this.nuevo();

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.id = idParam ? +idParam : null;

    if (this.id) {
      this.cargar(this.id);
    }
  }

  // =========================================================
  // CARGAR
  // =========================================================

  cargar(id: number): void {
    this.loading = true;

    this.api.obtener(id).subscribe({
      next: (data) => {
        this.form = {
          ...this.nuevo(),
          ...data
        };
        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando Variables Vigencia', err);
        this.loading = false;
        alert('No se pudo cargar el registro.');
        this.volver();
      }
    });
  }

  // =========================================================
  // GUARDAR
  // =========================================================

  guardar(): void {

    // =========================
    // VALIDACIONES
    // =========================

    if (!this.form.fechaInicial || !this.form.fechaFinal) {
      alert('Debe indicar Fecha inicial y Fecha final.');
      return;
    }

    if ((this.form.smmlv ?? 0) <= 0) {
      alert('El SMMLV debe ser mayor a cero.');
      return;
    }

    if ((this.form.auxTransporte ?? 0) < 0) {
      alert('El auxilio de transporte no puede ser negativo.');
      return;
    }

    if ((this.form.horasMes ?? 0) <= 0) {
      alert('Las horas del mes deben ser mayores a cero.');
      return;
    }

    if ((this.form.diasMes ?? 0) <= 0) {
      alert('Los días del mes deben ser mayores a cero.');
      return;
    }

    if ((this.form.topeIbcMaxSmmlv ?? 0) < (this.form.topeIbcMinSmmlv ?? 0)) {
      alert('El tope máximo IBC no puede ser menor que el tope mínimo.');
      return;
    }

    this.guardando = true;

    if (this.id) {

      this.api.actualizar(this.id, this.form).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: (err) => {
          console.error('Error actualizando Variables Vigencia', err);
          this.guardando = false;
          alert('No se pudo actualizar.');
        }
      });

    } else {

      this.api.crear(this.form).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: (err) => {
          console.error('Error creando Variables Vigencia', err);
          this.guardando = false;
          alert('No se pudo crear.');
        }
      });

    }
  }

  // =========================================================
  // VOLVER
  // =========================================================

  volver(): void {
    this.router.navigate(['/nomina/variables-vigencia']);
  }

  // =========================================================
  // NUEVO
  // =========================================================

  private nuevo(): VariablesVigenciaFormDTO {
    return {

      fechaInicial: '',
      fechaFinal: '',

      smmlv: 0,
      auxTransporte: 0,

      porcSaludEmpleado: 0,
      porcSaludEmpleador: 0,

      porcPensionEmpleado: 0,
      porcPensionEmpleador: 0,

      porcCajaCompensacion: 0,
      porcSena: 0,
      porcIcbf: 0,

      porProvisionPrima: 0,
      porProvisionPrimaSemestral: 0,
      porProvisionVacaciones: 0,
      porProvisionCesantias: 0,
      porProvisionInteresCesantias: 0,

      topeIbcMinSmmlv: 1,
      topeIbcMaxSmmlv: 25,

      horasMes: 240,
      diasMes: 30,

      exoneradoSalud: false,
      exoneradoParafiscales: false,

      aplicaCajaCompensacion: true,
      aplicaSena: true,
      aplicaIcbf: true,

      activo: true
    };
  }
}
