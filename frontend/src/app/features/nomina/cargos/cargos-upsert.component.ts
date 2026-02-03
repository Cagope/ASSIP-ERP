import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { CargoApi, CargoFormDTO } from './cargos.api';

@Component({
  standalone: true,
  selector: 'app-cargos-upsert',
  templateUrl: './cargos-upsert.component.html',
  styleUrls: ['./cargos-upsert.component.scss'],
  imports: [CommonModule, FormsModule, RouterModule, HeaderActionsComponent],
})
export class CargosUpsertComponent implements OnInit {

  private readonly api = inject(CargoApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  id: number | null = null;
  loading = false;
  guardando = false;

  form: CargoFormDTO = {
    nombreCargo: '',
    activo: true
  };

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.id = idParam ? +idParam : null;

    if (this.id) {
      this.cargar(this.id);
    }
  }

  cargar(id: number): void {
    this.loading = true;

    this.api.obtener(id).subscribe({
      next: (data) => {
        this.form = {
          idCargo: data.idCargo,
          nombreCargo: data.nombreCargo ?? '',
          activo: data.activo ?? true
        };
        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando cargo', err);
        this.loading = false;
        alert('No se pudo cargar el cargo.');
        this.volver();
      }
    });
  }

  guardar(): void {
    const nombre = (this.form.nombreCargo ?? '').trim();
    if (!nombre) {
      alert('El nombre del cargo es obligatorio.');
      return;
    }

    this.guardando = true;

    if (this.id) {
      this.api.actualizar(this.id, this.form).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: (err) => {
          console.error('Error actualizando cargo', err);
          this.guardando = false;
          alert('No se pudo actualizar el cargo.');
        }
      });
    } else {
      this.api.crear(this.form).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: (err) => {
          console.error('Error creando cargo', err);
          this.guardando = false;
          alert('No se pudo crear el cargo.');
        }
      });
    }
  }

  volver(): void {
    this.router.navigate(['/nomina/cargos']);
  }
}
