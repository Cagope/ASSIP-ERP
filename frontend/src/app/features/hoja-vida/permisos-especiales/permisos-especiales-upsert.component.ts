import { Component, OnInit, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { PermisosEspecialesApi, PermisoEspecial } from './permisos-especiales.api';
import { Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';

/**
 * 🧾 Componente Upsert (Crear / Editar) — Permisos Especiales
 * ------------------------------------------------------------
 * Reglas:
 *  - Cada persona puede tener un solo registro de permisos especiales.
 *  - Todos los campos son booleanos (Sí / No) y reflejan su autorización.
 *  - Se almacenan las fechas de aceptación o revocación.
 *  - El botón Guardar solo se habilita cuando el formulario está válido.
 *  - 💡 Nuevo registro: todos los permisos comienzan en TRUE.
 */
@Component({
  selector: 'app-permisos-especiales-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './permisos-especiales-upsert.component.html',
  styleUrls: ['./permisos-especiales-upsert.component.scss']
})
export class PermisosEspecialesUpsertComponent implements OnInit, OnChanges {
  @Output() formularioValido = new EventEmitter<boolean>();
  @Input() idDatosPersonal?: number;
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
      // 🟡 Editar registro existente
      this.editando = true;
      this.api.obtener(+id).subscribe({
        next: (data) => {
          this.form.patchValue(data);
          // ✅ Emitir validez inicial para activar el botón “Siguiente”
          queueMicrotask(() => {
            this.formularioValido.emit(this.form.valid);
          });
        },
        error: (err) => console.error('❌ Error al cargar Permisos Especiales:', err)
      });
    } else {
      // 🟢 Nuevo registro (Wizard o QueryParam)
      const idDatosPersonal = idDatosPersonalParam
        ? Number(idDatosPersonalParam)
        : this.idDatosPersonal;

      if (idDatosPersonal) {
        this.form.get('idDatosPersonal')?.setValue(idDatosPersonal);
        this.form.updateValueAndValidity({ emitEvent: true }); // 🔹 nueva línea
      }

      // ✅ Emitir estado inicial del formulario vacío
      queueMicrotask(() => {
        this.formularioValido.emit(this.form.valid);
      });
    }

    // 🔁 Reaccionar ante cambios de validez
    this.form.statusChanges.subscribe(() => {
      this.formularioValido.emit(this.form.valid);
    });

    // ⚙️ Escuchar campos booleanos o fechas (ya definidos en setupListeners)
    this.setupListeners();
  }

  // ============================================================
  // 🔗 Detectar cambios en el ID recibido desde el wizard
  // ============================================================
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['idDatosPersonal'] && this.idDatosPersonal) {
      console.log('🔗 Recibido idDatosPersonal desde wizard:', this.idDatosPersonal);
      this.form.get('idDatosPersonal')?.setValue(this.idDatosPersonal);
      this.form.updateValueAndValidity();
      this.formularioValido.emit(this.form.valid);
    }
  }

  // ============================================================
  // 🧱 Crear formulario
  // ============================================================
  private crearFormulario(): void {
    const hoy = this.hoyISO();

    this.form = this.fb.group({
      idPermisoEspecial: [null],
      idDatosPersonal: [null, Validators.required],

      // 🟩 Permisos de comunicación — INICIAN EN TRUE
      recibeLlamadas: [true],
      fechaLlamadas: [hoy],

      recibeMsm: [true],
      fechaSms: [hoy],

      recibeEmails: [true],
      fechaEmails: [hoy],

      recibeCartas: [true],
      fechaCartas: [hoy],

      recibeRedesSociales: [true],
      fechaRedes: [hoy],

      // 🔒 Auditoría
      fkSeguridadCreacion: [1],
      fkSeguridadEdicion: [1],
    });

    // ✅ Emitir estado inicial
    this.formularioValido.emit(this.form.valid);

    // ✅ Escuchar cambios y emitir estado actual
    this.form.statusChanges.subscribe(() => {
      this.formularioValido.emit(this.form.valid);
    });
  }

  // ============================================================
  // 🔄 Reacciones de cambio
  // ============================================================
  private setupListeners(): void {
    const hoy = this.hoyISO();

    const controles = [
      { campo: 'recibeLlamadas', fecha: 'fechaLlamadas' },
      { campo: 'recibeMsm', fecha: 'fechaSms' },
      { campo: 'recibeEmails', fecha: 'fechaEmails' },
      { campo: 'recibeCartas', fecha: 'fechaCartas' },
      { campo: 'recibeRedesSociales', fecha: 'fechaRedes' }
    ];

    controles.forEach(({ campo, fecha }) => {
      this.form.get(campo)?.valueChanges.subscribe(() => {
        this.form.get(fecha)?.setValue(hoy);
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

    // ✅ Si el formulario NO tiene idDatosPersonal y el wizard lo pasó, lo asignamos
    if (!this.form.get('idDatosPersonal')?.value && this.idDatosPersonal) {
      this.form.get('idDatosPersonal')?.setValue(this.idDatosPersonal);
    }

    const raw = { ...this.form.getRawValue() } as PermisoEspecial;
    const id = this.form.get('idPermisoEspecial')?.value;

    const accion = this.editando && id
      ? this.api.actualizar(id, raw)
      : this.api.crear(raw);

    accion.subscribe({
      next: () => {
        alert('✅ Permisos especiales guardados correctamente.');

        if (this.idDatosPersonal) {
          // 🟢 Modo Wizard: no redirige, solo confirma y continúa el flujo
          console.log('🟢 Permisos Especiales vinculados a ID:', this.idDatosPersonal);
        } else {
          // 🟡 Modo CRUD individual: volver al listado
          this.router.navigate(['/hoja-vida/permisos-especiales']);
        }
      },
      error: (err) => console.error('❌ Error al guardar Permisos Especiales:', err)
    });
  }


  volver(): void {
    this.router.navigate(['/hoja-vida/permisos-especiales']);
  }
}
