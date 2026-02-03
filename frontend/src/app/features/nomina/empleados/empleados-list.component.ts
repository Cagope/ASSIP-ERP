import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { EmpleadosApi, EmpleadoListDTO } from './empleados.api';
import { EmpleadosPrintService } from './empleados-print.service';
import { EmpleadosExporterService } from './empleados-exporter.service';

import { PersonasApi, PersonaBusquedaDTO } from '../../../shared/personas/personas.api';

@Component({
  standalone: true,
  selector: 'app-empleados-list',
  templateUrl: './empleados-list.component.html',
  styleUrls: ['./empleados-list.component.scss'],
  imports: [CommonModule, RouterModule, FormsModule, HeaderActionsComponent],
})
export class EmpleadosListComponent implements OnInit {

  private readonly api = inject(EmpleadosApi);
  private readonly router = inject(Router);

  private readonly printService = inject(EmpleadosPrintService);
  private readonly exporterService = inject(EmpleadosExporterService);

  private readonly personasApi = inject(PersonasApi);

  empleados: EmpleadoListDTO[] = [];
  loading = false;

  q = '';

  // ✅ cache idDatosPersonal -> persona
  personasMap = new Map<number, PersonaBusquedaDTO>();

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading = true;

    this.api.listar().subscribe({
      next: (data) => {
        this.empleados = data ?? [];
        this.cargarPersonasDeEmpleados(this.empleados);
      },
      error: (err) => {
        console.error('Error listando empleados nómina', err);
        this.empleados = [];
        this.loading = false;
      }
    });
  }

  // =========================================================
  // ✅ Cargar documento + nombre usando obtenerPorId()
  // =========================================================
  private cargarPersonasDeEmpleados(items: EmpleadoListDTO[]): void {

    const ids = Array.from(new Set(
      (items ?? [])
        .map(x => x.idDatosPersonal)
        .filter(x => !!x)
    ));

    if (ids.length === 0) {
      this.personasMap.clear();
      this.loading = false;
      return;
    }

    const requests = ids.map(id =>
      this.personasApi.obtenerPorId(id).pipe(
        map((p) => ({ id, persona: p })),
        catchError(() => of({ id, persona: null }))
      )
    );

    forkJoin(requests).subscribe({
      next: (rows) => {
        this.personasMap.clear();
        for (const r of rows) {
          if (r.persona) this.personasMap.set(r.id, r.persona);
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  personaDocumento(idDatosPersonal: number): string {
    return this.personasMap.get(idDatosPersonal)?.documento ?? '';
  }

  personaNombre(idDatosPersonal: number): string {
    return this.personasMap.get(idDatosPersonal)?.nombreCompleto ?? '';
  }

  // =========================================================
  // ✅ FILTRO (por id/agencia/persona/doc/nombre)
  // =========================================================
  get empleadosFiltrados(): EmpleadoListDTO[] {
    const q = (this.q ?? '').trim().toLowerCase();
    if (!q) return this.empleados;

    return this.empleados.filter(x => {
      const idEmpleado = String(x.idEmpleado ?? '');
      const idAgencia = String(x.idAgencia ?? '');
      const idPersona = String(x.idDatosPersonal ?? '');

      const doc = (this.personaDocumento(x.idDatosPersonal) ?? '').toLowerCase();
      const nom = (this.personaNombre(x.idDatosPersonal) ?? '').toLowerCase();

      return (
        idEmpleado.includes(q) ||
        idAgencia.includes(q) ||
        idPersona.includes(q) ||
        doc.includes(q) ||
        nom.includes(q)
      );
    });
  }

  // =========================================================
  // ✅ ACCIONES HEADER
  // =========================================================

  nuevo(): void {
    this.router.navigate(['/nomina/empleados/nuevo']);
  }

  refrescar(): void {
    this.cargar();
  }

  imprimir(): void {
    this.printService.imprimir(this.empleadosFiltrados);
  }

  exportar(): void {
    this.exporterService.exportar(this.empleadosFiltrados);
  }

  // =========================================================
  // ✅ ACCIONES FILA
  // =========================================================

  editar(id: number): void {
    this.router.navigate([`/nomina/empleados/${id}/editar`]);
  }

  eliminar(id: number): void {
    const ok = confirm('¿Desea eliminar este empleado?');
    if (!ok) return;

    this.api.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error eliminando empleado', err);
        alert('No se pudo eliminar el empleado.');
      }
    });
  }
}
