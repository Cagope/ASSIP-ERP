import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ZonasApi, Zona } from './zonas.api';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-zona-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './zona-upsert.component.html',
  styleUrls: ['./zona-upsert.component.scss']
})
export class ZonaUpsertComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ZonasApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  form!: FormGroup;
  id?: number;
  cargando = false;
  error?: string;

  ngOnInit(): void {
    this.form = this.fb.group({
      codigoZona: ['', Validators.required],
      nombreZona: ['', Validators.required],
      comentarioZona: ['']
    });

    this.id = Number(this.route.snapshot.paramMap.get('id'));
    if (this.id) this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.api.obtener(this.id!).subscribe({
      next: (data) => {
        this.form.patchValue(data);
        this.cargando = false;
      },
      error: (err: HttpErrorResponse) => {
        this.error = err.message;
        this.cargando = false;
      }
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const data: Zona = this.form.value;
    this.cargando = true;

    const peticion = this.id
      ? this.api.actualizar(this.id, data)
      : this.api.crear(data);

    peticion.subscribe({
      next: () => {
        alert('✅ Zona guardada correctamente');
        this.router.navigateByUrl('/general/zonas');
      },
      error: (err: HttpErrorResponse) => {
        alert('❌ Error guardando la zona: ' + (err.error?.message || err.message));
        this.cargando = false;
      }
    });
  }

  cancelar(): void {
    this.router.navigateByUrl('/general/zonas');
  }

  editarZona(idZona: number): void {
    this.router.navigate([`/general/zonas/${idZona}/editar`]);
  }
}
