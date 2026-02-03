import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { EmpleadosApi, EmpleadoFormDTO } from './empleados.api';

import { PersonasSelectorComponent } from '../../../shared/personas/personas-selector.component';
import { GeneralApi } from '../../../shared/general/general.api';
import { PersonasApi } from '../../../shared/personas/personas.api';
import { PersonaBusquedaDTO } from '../../../shared/personas/personas-busqueda.dto';

@Component({
  standalone: true,
  selector: 'app-empleados-upsert',
  templateUrl: './empleados-upsert.component.html',
  styleUrls: ['./empleados-upsert.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    HeaderActionsComponent,
    PersonasSelectorComponent
  ],
})
export class EmpleadosUpsertComponent implements OnInit {

  private readonly api = inject(EmpleadosApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private readonly personasApi = inject(PersonasApi);
  private readonly generalApi = inject(GeneralApi);

  id: number | null = null;

  loading = false;
  guardando = false;

  // ✅ agencias catálogo
  agencias: any[] = [];

  // ✅ persona seleccionada (para mostrar tarjeta)
  personaSeleccionada: PersonaBusquedaDTO | null = null;

  form: EmpleadoFormDTO = {
    idAgencia: null,
    idDatosPersonal: null,
    activo: true
  };

  ngOnInit(): void {
    this.cargarAgencias();

    const idParam = this.route.snapshot.paramMap.get('id');
    this.id = idParam ? +idParam : null;

    if (this.id) {
      this.cargar(this.id);
    }
  }

  // =========================================================
  // ✅ Cargar agencias (catálogo)
  // =========================================================
  cargarAgencias(): void {

    // ✅ usa GeneralApi (no CatalogosApi)
    this.generalApi.listarAgencias().subscribe({
      next: (data: any[]) => this.agencias = data ?? [],
      error: (err: any) => {
        console.error('Error cargando agencias', err);
        this.agencias = [];
      }
    });
  }

  // =========================================================
  // ✅ Cargar empleado (edición)
  // =========================================================
  cargar(id: number): void {
    this.loading = true;

    this.api.obtener(id).subscribe({
      next: (data) => {
        this.form = {
          idEmpleado: data.idEmpleado,
          idAgencia: (data as any).idAgencia ?? null,
          idDatosPersonal: (data as any).idDatosPersonal ?? null,
          activo: (data as any).activo ?? true
        };

        // ✅ pintar persona en tarjeta (si existe)
        const idPersona = this.form.idDatosPersonal ?? null;
        if (idPersona) {
          this.personasApi.obtenerPorId(idPersona).subscribe({
            next: p => this.personaSeleccionada = p,
            error: () => this.personaSeleccionada = null
          });
        }

        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando empleado', err);
        this.loading = false;
        alert('No se pudo cargar el empleado.');
        this.volver();
      }
    });
  }

  // =========================================================
  // ✅ Selección desde app-personas-selector
  // =========================================================
  onPersonaSeleccionada(p: PersonaBusquedaDTO): void {
    this.personaSeleccionada = p;
    this.form.idDatosPersonal = p.idDatosPersonal;
  }

  limpiarPersona(): void {
    this.personaSeleccionada = null;
    this.form.idDatosPersonal = null;
  }

  // =========================================================
  // ✅ Guardar
  // =========================================================
  guardar(): void {
    if (!this.form.idAgencia) {
      alert('La agencia es obligatoria.');
      return;
    }

    if (!this.form.idDatosPersonal) {
      alert('Debe seleccionar un tercero (persona).');
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
          console.error('Error actualizando empleado', err);
          this.guardando = false;
          alert('No se pudo actualizar el empleado.');
        }
      });
    } else {
      this.api.crear(this.form).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: (err) => {
          console.error('Error creando empleado', err);
          this.guardando = false;
          alert('No se pudo crear el empleado.');
        }
      });
    }
  }

  volver(): void {
    this.router.navigate(['/nomina/empleados']);
  }
}
