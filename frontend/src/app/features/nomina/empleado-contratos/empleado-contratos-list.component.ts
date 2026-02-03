import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { EmpleadoContratosApi, EmpleadoContratoListDTO } from './empleado-contratos.api';

import { EmpleadosApi, EmpleadoListDTO } from '../empleados/empleados.api';
import { PersonasApi, PersonaBusquedaDTO } from '../../../shared/personas/personas.api';

import { EmpleadoContratosPrintService } from './empleado-contratos-print.service';
import { EmpleadoContratosExporterService } from './empleado-contratos-exporter.service';

@Component({
  standalone: true,
  selector: 'app-empleado-contratos-list',
  templateUrl: './empleado-contratos-list.component.html',
  styleUrls: ['./empleado-contratos-list.component.scss'],
  imports: [CommonModule, RouterModule, FormsModule, HeaderActionsComponent],
})
export class EmpleadoContratosListComponent implements OnInit {

  private readonly api = inject(EmpleadoContratosApi);
  private readonly router = inject(Router);

  private readonly empleadosApi = inject(EmpleadosApi);
  private readonly personasApi = inject(PersonasApi);

  private readonly printService = inject(EmpleadoContratosPrintService);
  private readonly exporterService = inject(EmpleadoContratosExporterService);

  contratos: EmpleadoContratoListDTO[] = [];
  loading = false;

  q = '';

  // ✅ mapas para mostrar documento/nombre del empleado
  empleadosMap = new Map<number, EmpleadoListDTO>();
  personasMap = new Map<number, PersonaBusquedaDTO>();

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading = true;

    this.api.listar().subscribe({
      next: (data) => {
        this.contratos = data ?? [];
        this.cargarMapasEmpleado();
      },
      error: (err) => {
        console.error('Error listando contratos', err);
        this.contratos = [];
        this.loading = false;
      }
    });
  }

  private cargarMapasEmpleado(): void {
    const idsEmpleado = Array.from(new Set(this.contratos.map(x => x.idEmpleado).filter(Boolean)));

    if (idsEmpleado.length === 0) {
      this.loading = false;
      return;
    }

    const reqEmp = idsEmpleado.map(id =>
      this.empleadosApi.obtener(id).pipe(
        map(emp => ({ id, emp })),
        catchError(() => of({ id, emp: null as any }))
      )
    );

    forkJoin(reqEmp).subscribe({
      next: (rows) => {
        this.empleadosMap.clear();

        const idsPersona: number[] = [];
        for (const r of rows) {
          if (r.emp) {
            this.empleadosMap.set(r.id, r.emp);
            if (r.emp.idDatosPersonal) idsPersona.push(r.emp.idDatosPersonal);
          }
        }

        const uniquePersona = Array.from(new Set(idsPersona));
        if (uniquePersona.length === 0) {
          this.loading = false;
          return;
        }

        const reqPer = uniquePersona.map(idDatosPersonal =>
          this.personasApi.obtenerPorId(idDatosPersonal).pipe(
            map(p => ({ idDatosPersonal, p })),
            catchError(() => of({ idDatosPersonal, p: null as any }))
          )
        );

        forkJoin(reqPer).subscribe({
          next: (prs) => {
            this.personasMap.clear();
            for (const r of prs) {
              if (r.p) this.personasMap.set(r.idDatosPersonal, r.p);
            }
            this.loading = false;
          },
          error: () => this.loading = false
        });
      },
      error: () => this.loading = false
    });
  }

  nombreEmpleado(idEmpleado: number): string {
    const emp = this.empleadosMap.get(idEmpleado);
    if (!emp?.idDatosPersonal) return '';
    const p = this.personasMap.get(emp.idDatosPersonal);
    return p?.nombreCompleto ?? '';
  }

  documentoEmpleado(idEmpleado: number): string {
    const emp = this.empleadosMap.get(idEmpleado);
    if (!emp?.idDatosPersonal) return '';
    const p = this.personasMap.get(emp.idDatosPersonal);
    return p?.documento ?? '';
  }

  get filtrados(): EmpleadoContratoListDTO[] {
    const q = (this.q ?? '').trim().toLowerCase();
    if (!q) return this.contratos;

    return this.contratos.filter(x => {
      const doc = (this.documentoEmpleado(x.idEmpleado) ?? '').toLowerCase();
      const nom = (this.nombreEmpleado(x.idEmpleado) ?? '').toLowerCase();
      return doc.includes(q) || nom.includes(q) || (x.periodoPago ?? '').toLowerCase().includes(q);
    });
  }

  // header
  nuevo(): void {
    this.router.navigate(['/nomina/empleado-contratos/nuevo']);
  }

  refrescar(): void {
    this.cargar();
  }

  imprimir(): void {
    this.printService.imprimir(this.filtrados, this.empleadosMap, this.personasMap);
  }

  exportar(): void {
    this.exporterService.exportar(this.filtrados, this.empleadosMap, this.personasMap);
  }

  // fila
  editar(id: number): void {
    this.router.navigate([`/nomina/empleado-contratos/${id}/editar`]);
  }

  eliminar(id: number): void {
    const ok = confirm('¿Desea eliminar este contrato?');
    if (!ok) return;

    this.api.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error eliminando contrato', err);
        alert('No se pudo eliminar el contrato.');
      }
    });
  }
}
