import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { SessionService } from '../../../core/auth/session.service';

import { AfpApi, AfpFormDTO } from './afp.api';

import { PersonasSelectorComponent } from '../../../shared/personas/personas-selector.component';
import { PersonaBusquedaDTO } from '../../../shared/personas/personas-busqueda.dto';

@Component({
  standalone: true,
  selector: 'app-afp-upsert',
  templateUrl: './afp-upsert.component.html',
  styleUrls: ['./afp-upsert.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    HeaderActionsComponent,
    PersonasSelectorComponent
  ],
})
export class AfpUpsertComponent implements OnInit {

  private readonly api = inject(AfpApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly session = inject(SessionService);

  id: number | null = null;
  loading = false;
  guardando = false;

  form: AfpFormDTO = {
    nombreAfp: '',
    activo: true,
    idDatosPersonal: null
  };

  get puedeEditar(): boolean {
    return this.session.tienePermiso('NOMINA_PARAMETROS_EDIT');
  }

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
          idAfp: data.idAfp,
          nombreAfp: data.nombreAfp ?? '',
          activo: data.activo ?? true,
          idDatosPersonal: data.idDatosPersonal ?? null
        };

        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando AFP', err);
        this.loading = false;
        alert('No se pudo cargar la AFP.');
        this.volver();
      }
    });
  }

  // =========================================================
  // PERSONA
  // =========================================================

  onPersonaSeleccionada(p: PersonaBusquedaDTO): void {
    this.form.idDatosPersonal = p.idDatosPersonal;
  }

  limpiarPersona(): void {
    this.form.idDatosPersonal = null;
  }

  // =========================================================
  // GUARDAR
  // =========================================================

  guardar(): void {
    if (!this.puedeEditar) {
      return;
    }

    const nombre = (this.form.nombreAfp ?? '').trim();

    if (!nombre) {
      alert('El nombre de la AFP es obligatorio.');
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
          console.error('Error actualizando AFP', err);
          this.guardando = false;
          alert('No se pudo actualizar la AFP.');
        }
      });

      return;
    }

    this.api.crear(this.form).subscribe({
      next: () => {
        this.guardando = false;
        this.volver();
      },
      error: (err) => {
        console.error('Error creando AFP', err);
        this.guardando = false;
        alert('No se pudo crear la AFP.');
      }
    });
  }

  volver(): void {
    this.router.navigate(['/nomina/afp']);
  }
}
