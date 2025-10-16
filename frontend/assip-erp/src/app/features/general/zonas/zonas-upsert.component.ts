// src/app/features/general/zonas/zonas-upsert.component.ts
import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ZonasApi, ZonaCreate } from './zonas.api';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-zonas-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './zonas-upsert.component.html',
  styleUrls: ['./zonas-upsert.component.scss']
})
export class ZonasUpsertComponent implements OnInit {
  private api = inject(ZonasApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  id = signal<number | null>(null);
  loading = signal<boolean>(false);
  guardando = signal<boolean>(false);
  errorMsg = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    codigoZona: ['', [Validators.required, Validators.pattern(/^\d{3}$/)]],
    nombreZona: ['', [Validators.required, Validators.maxLength(100)]],
    comentarioZona: ['', [Validators.maxLength(100)]],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id.set(Number(idParam));
      this.cargar();
    }
  }

  cargar() {
    if (!this.id()) return;
    this.loading.set(true);
    this.api.get(this.id()!).subscribe({
      next: (z) => {
        this.form.patchValue({
          codigoZona: z.codigoZona ?? '',
          nombreZona: z.nombreZona ?? '',
          comentarioZona: z.comentarioZona ?? ''
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMsg.set(err?.error?.message ?? 'Error cargando la zona');
        this.loading.set(false);
      }
    });
  }

  guardar() {
    this.errorMsg.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const payload: ZonaCreate = this.form.getRawValue();
    this.guardando.set(true);

    const goList = () => {
      this.guardando.set(false);
      this.router.navigate(['/general/zonas']);
    };

    if (this.id()) {
      this.api.update(this.id()!, payload).subscribe({
        next: goList,
        error: (err) => {
          this.errorMsg.set(err?.error?.message ?? 'Error actualizando');
          this.guardando.set(false);
        }
      });
    } else {
      this.api.create(payload).subscribe({
        next: goList,
        error: (err) => {
          this.errorMsg.set(err?.error?.message ?? 'Error creando');
          this.guardando.set(false);
        }
      });
    }
  }

  cancelar() {
    this.router.navigate(['/general/zonas']);
  }

  // <-- FALTABA ESTE MÉTODO
  onlyDigits(e: KeyboardEvent) {
    const key = e.key;
    if (!/^\d$/.test(key)) {
      e.preventDefault();
    }
  }
}
