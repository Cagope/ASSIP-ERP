import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { CajaCompensacionApi, CajaCompensacionFormDTO } from './caja-compensacion.api';

import { PersonasSelectorComponent } from '../../../shared/personas/personas-selector.component';
import { PersonaBusquedaDTO } from '../../../shared/personas/personas-busqueda.dto';

@Component({
  standalone: true,
  selector: 'app-caja-compensacion-upsert',
  templateUrl: './caja-compensacion-upsert.component.html',
  styleUrls: ['./caja-compensacion-upsert.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    HeaderActionsComponent,
    PersonasSelectorComponent
  ],
})
export class CajaCompensacionUpsertComponent implements OnInit {

  private readonly api = inject(CajaCompensacionApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  id: number | null = null;
  loading = false;
  guardando = false;

  form: CajaCompensacionFormDTO = {
    nombreCaja: '',
    activo: true,
    idDatosPersonal: null
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
          idCaja: data.idCaja,
          nombreCaja: data.nombreCaja ?? '',
          activo: data.activo ?? true,
          idDatosPersonal: data.idDatosPersonal ?? null
        };
        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando Caja Compensación', err);
        this.loading = false;
        alert('No se pudo cargar.');
        this.volver();
      }
    });
  }

  onPersonaSeleccionada(p: PersonaBusquedaDTO): void {
    this.form.idDatosPersonal = p.idDatosPersonal;
  }

  limpiarPersona(): void {
    this.form.idDatosPersonal = null;
  }

  guardar(): void {
    const nombre = (this.form.nombreCaja ?? '').trim();
    if (!nombre) {
      alert('El nombre es obligatorio.');
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
          console.error('Error actualizando', err);
          this.guardando = false;
          alert('No se pudo actualizar.');
        }
      });
    } else {
      this.api.crear(this.form).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: (err) => {
          console.error('Error creando', err);
          this.guardando = false;
          alert('No se pudo crear.');
        }
      });
    }
  }

  volver(): void {
    this.router.navigate(['/nomina/caja-compensacion']);
  }
}
