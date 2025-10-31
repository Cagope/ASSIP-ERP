import { Component, OnInit, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { PermisosEspecialesApi, PermisoEspecial } from './permisos-especiales.api';

/**
 * 🧾 Componente Upsert (Crear / Editar) — Permisos Especiales
 * ------------------------------------------------------------
 * Reglas:
 *  - Cada persona puede tener un solo registro de permisos especiales.
 *  - Todos los campos son booleanos (Sí / No) y reflejan su autorización.
 *  - Se almacenan las fechas de aceptación o revocación.
 *  - El botón Guardar solo se habilita cuando el formulario está válido.
 */
@Component({
  selector: 'app-permisos-especiales-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './permisos-especiales-upsert.component.html',
  styleUrls: ['./permisos-especiales-upsert.component.scss']
})
export class PermisosEspecialesUpsertComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(PermisosEspecialesApi);

  form!: FormGroup;
  editando = false;

  // ✅ Habilita el botón guardar solo si el formulario es válido
  puedeGuardar = computed(() => this.form?.valid ?? false);

  ngOnInit(): void {
    this.crearFormulario();

    const id = this.route.snapshot.paramMap.get('id');
    const idDatosPersonalParam = this.route.snapshot.queryParamMap.get('idDatosPersonal');

    if (id) {
      this.editando = true;
      this.api.obtener(+id).subscribe({
        next: (data) => this.form.patchValue(data),
        error: (err) => console.error('❌ Error al cargar Permisos Especiales:', err)
      });
    } else if (idDatosPersonalParam) {
      this.form.get('idDatosPersonal')?.setValue(+idDatosPersonalParam);
      this.form.get('idDatosPersonal')?.disable();
    }

    this.setupListeners();
  }

  // ============================================================
  // 🧱 Crear formulario
  // ============================================================
  private crearFormulario(): void {
    const hoy = this.hoyISO();

    this.form = this.fb.group({
      idPermisoEspecial: [null],
      idDatosPersonal: [null, Validators.required],

      // 🟩 Permisos de comunicación
      recibeLlamadas: [false],
      fechaLlamadas: [hoy],

      recibeSms: [false],
      fechaSms: [hoy],

      recibeEmails: [false],
      fechaEmails: [hoy],

      recibeCartas: [false],
      fechaCartas: [hoy],

      recibeRedesSociales: [false],
      fechaRedesSociales: [hoy],

      // 🔒 Auditoría
      fkSeguridadCreacion: [1],
      fkSeguridadEdicion: [1],
    });
  }

  // ============================================================
  // 🔄 Reacciones de cambio
  // ============================================================
  private setupListeners(): void {
    const hoy = this.hoyISO();

    const controles = [
      { campo: 'recibeLlamadas', fecha: 'fechaLlamadas' },
      { campo: 'recibeSms', fecha: 'fechaSms' },
      { campo: 'recibeEmails', fecha: 'fechaEmails' },
      { campo: 'recibeCartas', fecha: 'fechaCartas' },
      { campo: 'recibeRedesSociales', fecha: 'fechaRedesSociales' }
    ];

    controles.forEach(({ campo, fecha }) => {
      this.form.get(campo)?.valueChanges.subscribe((val) => {
        if (val) {
          // Si el usuario acepta, registra o actualiza la fecha
          this.form.get(fecha)?.setValue(hoy);
        } else {
          // Si revoca, también actualiza la fecha de cambio
          this.form.get(fecha)?.setValue(hoy);
        }
      });
    });
  }

  private hoyISO(): string {
    return new Date().toISOString().split('T')[0];
  }

  // ============================================================
  // 💾 Guardar / Actualizar
  // ============================================================
  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      alert('⚠️ Complete los campos obligatorios o revise los formatos.');
      return;
    }

    const raw = { ...this.form.getRawValue() } as PermisoEspecial;
    const id = this.form.get('idPermisoEspecial')?.value;

    if (!raw.idDatosPersonal) {
      const idDP = this.route.snapshot.queryParamMap.get('idDatosPersonal');
      if (idDP) raw.idDatosPersonal = +idDP;
    }

    const accion = this.editando && id
      ? this.api.actualizar(id, raw)
      : this.api.crear(raw);

    accion.subscribe({
      next: () => {
        alert('✅ Permisos especiales guardados correctamente.');
        this.router.navigate(['/hoja-vida/permisos-especiales']);
      },
      error: (err) => console.error('❌ Error al guardar Permisos Especiales:', err)
    });
  }

  volver(): void {
    this.router.navigate(['/hoja-vida/permisos-especiales']);
  }
}
