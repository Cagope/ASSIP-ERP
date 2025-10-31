import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { FinancierosApi } from './financieros.api';
import { Financiero } from '../../../shared/models/financiero.model'; // ✅ modelo correcto

// ✅ Directiva global de formato numérico
import { NumericFormatDirective } from '@shared/utils/numeric-format.directive';

/**
 * 💰 Componente Upsert (Crear / Editar) — Financieros
 * ------------------------------------------------------------
 * Gestiona la información financiera asociada a una persona.
 * Se organiza por secciones:
 *   - Ingresos
 *   - Egresos
 *   - Patrimonio
 */
@Component({
  selector: 'app-financieros-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NumericFormatDirective],
  templateUrl: './financieros-upsert.component.html',
  styleUrls: ['./financieros-upsert.component.scss']
})
export class FinancierosUpsertComponent implements OnInit {
  // ================================================================
  // 🧩 Inyección de dependencias
  // ================================================================
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(FinancierosApi);

  // ================================================================
  // 📋 Propiedades
  // ================================================================
  form!: FormGroup;
  editando = false;

  // ================================================================
  // 🚀 Inicialización
  // ================================================================
  ngOnInit(): void {
    this.crearFormulario();

    const id = this.route.snapshot.paramMap.get('id');
    const idDatosPersonalParam = this.route.snapshot.queryParamMap.get('idDatosPersonal');

    if (id) {
      // 🟡 Editar registro existente
      this.editando = true;
      this.api.obtener(+id).subscribe({
        next: (data) => this.form.patchValue(data),
        error: (err) => console.error('❌ Error al cargar datos financieros:', err)
      });
    } else if (idDatosPersonalParam) {
      // 🟢 Creación asociada a una persona
      const idDatosPersonal = Number(idDatosPersonalParam);
      this.form.get('idDatosPersonal')?.setValue(idDatosPersonal);
      // 🔹 No se deshabilita para que el formulario sea válido
    }
  }

  // ================================================================
  // 🧱 Configuración del formulario reactivo
  // ================================================================
  private crearFormulario(): void {
    this.form = this.fb.group({
      idFinanciero: [null],
      idDatosPersonal: [null, Validators.required], // ✅ singular correcto

      // === 💵 Grupo de ingresos ===
      valorSalario: [0, [Validators.min(0)]],
      valorPension: [0, [Validators.min(0)]],
      ingresosArriendo: [0, [Validators.min(0)]],
      ingresosComisiones: [0, [Validators.min(0)]],
      otrosIngresos: [0, [Validators.min(0)]],
      comentarioOtrosIngresos: ['', Validators.maxLength(100)],
      origenFondos: ['', Validators.maxLength(100)],

      // === 💸 Grupo de egresos ===
      egresosFamiliares: [0, [Validators.min(0)]],
      egresosArriendo: [0, [Validators.min(0)]],
      egresosCredito: [0, [Validators.min(0)]],
      otrosEgresos: [0, [Validators.min(0)]],
      comentarioOtrosEgresos: ['', Validators.maxLength(100)],

      // === 💰 Grupo de patrimonio ===
      totalActivos: [0, [Validators.min(0)]],
      totalPasivos: [0, [Validators.min(0)]],
      deudaRelacionFinanciera: [0, [Validators.min(0)]],
      relacionFinanciera: ['', Validators.maxLength(100)],

      // === Auditoría ===
      fkSeguridadCreacion: [1],
      fkSeguridadActualizacion: [1], // ✅ singular coherente con backend
    });
  }

  // ================================================================
  // 💾 Guardar o actualizar registro
  // ================================================================
  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      alert('⚠️ Complete los campos obligatorios o revise los valores.');
      return;
    }

    const raw = { ...this.form.getRawValue() } as Financiero;
    const id = this.form.get('idFinanciero')?.value;

    // Recuperar idDatosPersonal si vino por query param
    if (!raw.idDatosPersonal) {
      const idDP = this.route.snapshot.queryParamMap.get('idDatosPersonal');
      if (idDP) raw.idDatosPersonal = +idDP;
    }

    const accion = this.editando && id
      ? this.api.actualizar(id, raw)
      : this.api.crear(raw);

    accion.subscribe({
      next: () => {
        alert('✅ Información financiera guardada correctamente.');
        this.router.navigate(['/hoja-vida/financieros']);
      },
      error: (err) => console.error('❌ Error al guardar información financiera:', err)
    });
  }

  // ================================================================
  // 🔙 Volver al listado
  // ================================================================
  volver(): void {
    this.router.navigate(['/hoja-vida/financieros']);
  }
}
