import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { PlanCuentasApi, PlanCuenta } from './plan-cuentas.api';
import { AgenciasApi, Agencia } from '../../general/agencias/agencia.api';

@Component({
  selector: 'app-plan-cuentas-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './plan-cuentas-upsert.component.html',
  styleUrls: ['./plan-cuentas-upsert.component.scss']
})
export class PlanCuentasUpsertComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly api = inject(PlanCuentasApi);
  private readonly agenciasApi = inject(AgenciasApi);
  readonly route = inject(ActivatedRoute);
  readonly router = inject(Router);

  agencias: Agencia[] = [];
  tiposEspeciales: { id: number; nombre: string }[] = []; // ✔ agregado

  form = this.fb.group({
    idAgencia: [null as number | null, Validators.required],

    codigoCuenta: [
      '',
      [
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(11),
        Validators.pattern(/^[0-9]+$/)
      ]
    ],

    nombre: ['', [Validators.required, Validators.maxLength(100)]],

    naturaleza: ['D', [Validators.required]],

    tipoEspecial: [null as number | null], // ✔ correcto

    operable: [false],

    controlEntradaSalida: [false]
  });

  ngOnInit(): void {
    this.cargarAgencias();
    this.cargarTiposEspeciales(); // ✔ agregado

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.api.obtener(+id).subscribe({
        next: (data: PlanCuenta) => {
          this.form.patchValue({
            idAgencia: data.idAgencia,
            codigoCuenta: data.codigoCuenta,
            nombre: data.nombre,
            naturaleza: data.naturaleza,
            tipoEspecial: data.tipoEspecial,
            operable: data.operable,
            controlEntradaSalida: data.controlEntradaSalida
          });
        }
      });
    }
  }

  cargarAgencias(): void {
    this.agenciasApi.listar().subscribe({
      next: (data) => (this.agencias = data),
      error: () => alert('Error al cargar agencias.')
    });
  }

  // ===================================================================
  // ⭐ Carga del catálogo de tipos especiales (NUEVO)
  // ===================================================================
  cargarTiposEspeciales(): void {
    // 🚨 placeholder temporal
    // Si tienes API real, dime y lo conecto.
    this.tiposEspeciales = [
      { id: 1, nombre: 'Cuenta Puente' },
      { id: 2, nombre: 'Operación Especial' },
      { id: 3, nombre: 'Control Interno' }
    ];
  }

  guardar(): void {
    if (this.form.invalid) {
      console.warn('❌ Formulario inválido:', this.form.value);
      return;
    }

    const value = this.form.value;

    const payload: PlanCuenta = {
      idAgencia: Number(value.idAgencia),
      codigoCuenta: (value.codigoCuenta ?? '').trim(),
      nombre: value.nombre!,
      naturaleza: value.naturaleza!,
      tipoEspecial: value.tipoEspecial ?? null,
      operable: value.operable ?? false,
      controlEntradaSalida: value.controlEntradaSalida ?? false
    };

    const id = this.route.snapshot.paramMap.get('id');

    const request = id
      ? this.api.actualizar(+id, payload)
      : this.api.crear(payload);

    request.subscribe({
      next: () => this.router.navigate(['/contabilidad/plan-cuentas']),
      error: (err) => {
        console.error('❌ Error al guardar cuenta:', err);
        alert('Error al guardar la cuenta contable.');
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/contabilidad/plan-cuentas']);
  }
}
