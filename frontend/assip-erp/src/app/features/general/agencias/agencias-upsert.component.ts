import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AgenciasApi, AgenciaCreateUpdate } from './agencias.api';
import { map } from 'rxjs/operators';
import { DeptoCiudadComponent } from '../../../shared/catalogos/depto-ciudad/depto-ciudad.component';

@Component({
  standalone: true,
  selector: 'app-agencias-upsert',
  imports: [CommonModule, ReactiveFormsModule, DeptoCiudadComponent],
  templateUrl: './agencias-upsert.component.html',
  styleUrls: ['./agencias-upsert.component.scss']
})
export class AgenciasUpsertComponent implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(AgenciasApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  loading = signal(false);
  guardando = signal(false);
  error = signal<string | null>(null);
  id = signal<number | null>(null);

  titulo = computed(() => this.id() ? 'Editar agencia' : 'Nueva agencia');

  // ⚠️ Validaciones:
  // - codigo: exactamente 2 dígitos: "01", "02", ...
  // - sigla: requerida
  // - correo: si se digita, Validators.email
  // - celular/telefono: si se digitan, deben cumplir patrón
  form = this.fb.group({
    codigoAgencia: ['', [Validators.required, Validators.pattern(/^[0-9]{2}$/)]],
    nombreAgencia: ['', [Validators.required, Validators.maxLength(100)]],
    siglaAgencia: ['', [Validators.required, Validators.maxLength(100)]],
    direccionAgencia: ['', [Validators.required, Validators.maxLength(100)]],
    idDepartamento: new FormControl<number | null>(null, { nonNullable: false }),
    idCiudad: new FormControl<number | null>(null, { nonNullable: false }),
    correoAgencia: [null as string | null, [Validators.maxLength(100), Validators.email]],
    celularAgencia: [null as string | null, [Validators.pattern(/^[0-9]{10}$/)]],
    telefonoAgencia: [null as string | null, [Validators.pattern(/^[0-9]{7}$/)]],
  });

  get f() { return this.form.controls; }
  get deptoCtrl() { return this.form.get('idDepartamento') as FormControl<number | null>; }
  get ciudadCtrl() { return this.form.get('idCiudad') as FormControl<number | null>; }

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam && idParam !== 'new') {
      const id = Number(idParam);
      if (!Number.isNaN(id)) {
        this.id.set(id);
        this.load(id);
      }
    }
  }

  load(id: number) {
    this.loading.set(true);
    this.api.get(id).subscribe({
      next: dto => { this.form.patchValue(dto); this.loading.set(false); },
      error: (err: any) => { this.error.set(err?.error?.message ?? 'No se pudo cargar la agencia'); this.loading.set(false); }
    });
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const payload = this.form.getRawValue() as AgenciaCreateUpdate;

    this.guardando.set(true);
    this.error.set(null);

    const obs = this.id()
      ? this.api.update(this.id()!, payload).pipe(map(() => void 0))
      : this.api.create(payload).pipe(map(() => void 0));

    obs.subscribe({
      next: () => {
        this.guardando.set(false);
        this.router.navigate(['/general/agencias']);
      },
      error: (err: any) => {
        this.guardando.set(false);
        this.error.set(err?.error?.message ?? 'No se pudo guardar');
      }
    });
  }

  cancelar() { this.router.navigate(['/general/agencias']); }
}
