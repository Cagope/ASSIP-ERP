import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SubZonasApi } from './sub-zonas.api';
import { ZonasApi, Zona } from '../zonas/zonas.api';

@Component({
  selector: 'app-sub-zona-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './sub-zona-upsert.component.html',
  styleUrls: ['./sub-zona-upsert.component.scss']
})
export class SubZonaUpsertComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(SubZonasApi);
  private readonly zonasApi = inject(ZonasApi);
  readonly route = inject(ActivatedRoute); // ✅ corregido
  readonly router = inject(Router);

  zonas: Zona[] = [];
  form = this.fb.group({
    codigoSubZona: ['', [Validators.required, Validators.maxLength(5)]],
    nombreSubZona: ['', [Validators.required, Validators.maxLength(100)]],
    idZona: [null as number | null, Validators.required],
    comentarioSubZona: ['', [Validators.maxLength(100)]]
  });

  ngOnInit(): void {
    this.cargarZonas();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.api.obtener(+id).subscribe((data) => {
        if (data) {
          this.form.patchValue({
            codigoSubZona: data.codigoSubZona,
            nombreSubZona: data.nombreSubZona,
            idZona: data.zona?.idZona ?? null,
            comentarioSubZona: data.comentarioSubZona
          });
        }
      });
    }
  }

  cargarZonas(): void {
    this.zonasApi.listar().subscribe({
      next: (data) => (this.zonas = data),
      error: () => alert('Error al cargar zonas.')
    });
  }

  guardar(): void {
    if (this.form.invalid) return;

    const value = this.form.value;
    const payload = {
      codigoSubZona: value.codigoSubZona!,
      nombreSubZona: value.nombreSubZona!,
      comentarioSubZona: value.comentarioSubZona ?? '',
      zona: value.idZona ? { idZona: Number(value.idZona) } : undefined
    };

    const id = this.route.snapshot.paramMap.get('id');
    const request = id
      ? this.api.actualizar(+id, payload)
      : this.api.crear(payload);

    request.subscribe(() => this.router.navigate(['/general/sub-zonas']));
  }
}
