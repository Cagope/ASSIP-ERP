import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { ArlApi, ArlFormDTO } from './arl.api';

import { PersonasSelectorComponent } from '../../../shared/personas/personas-selector.component';
import { PersonaBusquedaDTO } from '../../../shared/personas/personas-busqueda.dto';

@Component({
  standalone: true,
  selector: 'app-arl-upsert',
  templateUrl: './arl-upsert.component.html',
  styleUrls: ['./arl-upsert.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    HeaderActionsComponent,
    PersonasSelectorComponent
  ],
})
export class ArlUpsertComponent implements OnInit {

  private readonly api = inject(ArlApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  id: number | null = null;
  loading = false;
  guardando = false;

  form: ArlFormDTO = {
    nombreArl: '',
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
          idArl: data.idArl,
          nombreArl: data.nombreArl ?? '',
          activo: data.activo ?? true,
          idDatosPersonal: data.idDatosPersonal ?? null
        };
        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando ARL', err);
        this.loading = false;
        alert('No se pudo cargar la ARL.');
        this.volver();
      }
    });
  }

  // =========================================================
  // ✅ Capturar id_datos_personal desde el selector
  // =========================================================
  onPersonaSeleccionada(p: PersonaBusquedaDTO): void {
    this.form.idDatosPersonal = p.idDatosPersonal;
  }

  limpiarPersona(): void {
    this.form.idDatosPersonal = null;
  }

  guardar(): void {
    const nombre = (this.form.nombreArl ?? '').trim();
    if (!nombre) {
      alert('El nombre de la ARL es obligatorio.');
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
          console.error('Error actualizando ARL', err);
          this.guardando = false;
          alert('No se pudo actualizar la ARL.');
        }
      });
    } else {
      this.api.crear(this.form).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: (err) => {
          console.error('Error creando ARL', err);
          this.guardando = false;
          alert('No se pudo crear la ARL.');
        }
      });
    }
  }

  volver(): void {
    this.router.navigate(['/nomina/arl']);
  }
}
