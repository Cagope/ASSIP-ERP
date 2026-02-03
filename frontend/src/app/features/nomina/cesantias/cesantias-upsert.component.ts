import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { CesantiasApi, CesantiasDTO } from './cesantias.api';

import { PersonasSelectorComponent } from '../../../shared/personas/personas-selector.component';
import { PersonaBusquedaDTO } from '../../../shared/personas/personas-busqueda.dto';

@Component({
  standalone: true,
  selector: 'app-cesantias-upsert',
  templateUrl: './cesantias-upsert.component.html',
  styleUrls: ['./cesantias-upsert.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    HeaderActionsComponent,
    PersonasSelectorComponent
  ],
})
export class CesantiasUpsertComponent implements OnInit {

  private readonly api = inject(CesantiasApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  id: number | null = null;
  loading = false;
  guardando = false;

  form: CesantiasDTO = {
    nombreCesantias: '',
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

  // =========================================================
  // 📥 Cargar para edición
  // =========================================================
  cargar(id: number): void {
    this.loading = true;

    this.api.obtener(id).subscribe({
      next: (data) => {
        this.form = {
          idCesantias: data.idCesantias,
          nombreCesantias: data.nombreCesantias ?? '',
          activo: data.activo ?? true,
          idDatosPersonal: data.idDatosPersonal ?? null
        };
        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando Cesantías', err);
        this.loading = false;
        alert('No se pudo cargar la Cesantía.');
        this.volver();
      }
    });
  }

  // =========================================================
  // 👤 Selector de persona
  // =========================================================
  onPersonaSeleccionada(p: PersonaBusquedaDTO): void {
    this.form.idDatosPersonal = p.idDatosPersonal;
  }

  limpiarPersona(): void {
    this.form.idDatosPersonal = null;
  }

  // =========================================================
  // 💾 Guardar
  // =========================================================
  guardar(): void {
    const nombre = (this.form.nombreCesantias ?? '').trim();
    if (!nombre) {
      alert('El nombre de la Cesantía es obligatorio.');
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
          console.error('Error actualizando Cesantías', err);
          this.guardando = false;
          alert('No se pudo actualizar la Cesantía.');
        }
      });
    } else {
      this.api.crear(this.form).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: (err) => {
          console.error('Error creando Cesantías', err);
          this.guardando = false;
          alert('No se pudo crear la Cesantía.');
        }
      });
    }
  }

  // =========================================================
  // 🔙 Volver al listado
  // =========================================================
  volver(): void {
    this.router.navigate(['/nomina/cesantias']);
  }
}
