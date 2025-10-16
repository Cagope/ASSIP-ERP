import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { SubZonasApi, SubZonaCreate } from './sub-zonas.api';
import { ZonasApi, Zona } from '../zonas/zonas.api';

@Component({
  selector: 'app-sub-zonas-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './sub-zonas-upsert.component.html',
  styleUrls: ['./sub-zonas-upsert.component.scss']
})
export class SubZonasUpsertComponent implements OnInit {
  private api = inject(SubZonasApi);
  private zonasApi = inject(ZonasApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  id = signal<number | null>(null);
  loading = signal<boolean>(false);
  guardando = signal<boolean>(false);
  errorMsg = signal<string | null>(null);

  zonas = signal<Zona[]>([]);

  form = this.fb.nonNullable.group({
    idZona: [null as number | null, [Validators.required]],
    codigoSubZona: ['', [Validators.required, Validators.pattern(/^\d{3}$/)]],
    nombreSubZona: ['', [Validators.required, Validators.maxLength(100)]],
    comentarioSubZona: ['', [Validators.required, Validators.maxLength(100)]],
  });

  ngOnInit(): void {
    // cargar zonas para selector
    this.zonasApi.list({ page: 0, size: 500, sort: 'nombreZona,asc' })
      .subscribe(res => this.zonas.set(res.content));

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
      next: (s) => {
        this.form.patchValue({
          idZona: s.idZona ?? null,
          codigoSubZona: s.codigoSubZona ?? '',
          nombreSubZona: s.nombreSubZona ?? '',
          comentarioSubZona: s.comentarioSubZona ?? '',
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMsg.set(err?.error?.message ?? 'Error cargando la sub zona');
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
    const payload: SubZonaCreate = this.form.getRawValue();
    this.guardando.set(true);

    const goList = () => {
      this.guardando.set(false);
      this.router.navigate(['/general/sub-zonas']);
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
    this.router.navigate(['/general/sub-zonas']);
  }

  onlyDigits(e: KeyboardEvent) {
    const key = e.key;
    if (!/^\d$/.test(key)) {
      e.preventDefault();
    }
  }
}
