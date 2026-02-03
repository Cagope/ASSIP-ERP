import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { SeccionesNominaApi, SeccionNominaDTO } from './secciones.api';

@Component({
  standalone: true,
  selector: 'app-secciones-upsert',
  templateUrl: './secciones-upsert.component.html',
  styleUrls: ['./secciones-upsert.component.scss'],
  imports: [CommonModule, RouterModule, ReactiveFormsModule, HeaderActionsComponent],
})
export class SeccionesUpsertComponent implements OnInit {

  private readonly api = inject(SeccionesNominaApi);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  id: number | null = null;
  loading = false;

  form = this.fb.group({
    codigo: ['', [Validators.required, Validators.maxLength(10)]],
    nombreSeccion: ['', [Validators.required, Validators.maxLength(80)]],
    activo: [true, [Validators.required]],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.id = +id;
      this.cargar(this.id);
    }
  }

  cargar(id: number): void {
    this.loading = true;

    this.api.obtener(id).subscribe({
      next: (data) => {
        this.form.patchValue({
          codigo: data.codigo ?? '',
          nombreSeccion: data.nombreSeccion ?? '',
          activo: data.activo ?? true,
        });
        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando sección', err);
        this.loading = false;
        alert('No se pudo cargar la sección.');
      }
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    const payload: SeccionNominaDTO = {
      codigo: (raw.codigo ?? '').trim(),
      nombreSeccion: (raw.nombreSeccion ?? '').trim(),
      activo: raw.activo ?? true,
    };


    this.loading = true;

    // ✅ EDITAR
    if (this.id) {
      this.api.actualizar(this.id, payload).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/nomina/secciones']);
        },
        error: (err) => {
          console.error('Error actualizando sección', err);
          this.loading = false;
          alert('No se pudo actualizar.');
        }
      });
      return;
    }

    // ✅ CREAR
    this.api.crear(payload).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/nomina/secciones']);
      },
      error: (err) => {
        console.error('Error creando sección', err);
        this.loading = false;
        alert('No se pudo crear.');
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/nomina/secciones']);
  }
}
