import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { SeccionesNominaApi, SeccionNominaDTO } from './secciones.api';
import { SeccionesNominaPrintService } from './secciones-print.service';
import { SeccionesNominaExporterService } from './secciones-exporter.service';

@Component({
  standalone: true,
  selector: 'app-secciones-list',
  templateUrl: './secciones-list.component.html',
  styleUrls: ['./secciones-list.component.scss'],
  imports: [CommonModule, RouterModule, FormsModule, HeaderActionsComponent],
})
export class SeccionesListComponent implements OnInit {

  private readonly api = inject(SeccionesNominaApi);
  private readonly router = inject(Router);

  private readonly printer = inject(SeccionesNominaPrintService);
  private readonly exporter = inject(SeccionesNominaExporterService);

  secciones: SeccionNominaDTO[] = [];
  loading = false;

  // ✅ filtro simple
  q = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading = true;

    this.api.listar().subscribe({
      next: (data) => {
        this.secciones = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error listando secciones nómina', err);
        this.secciones = [];
        this.loading = false;
      }
    });
  }

  get seccionesFiltradas(): SeccionNominaDTO[] {
    const q = (this.q ?? '').trim().toLowerCase();
    if (!q) return this.secciones;

    return this.secciones.filter(x =>
      (x.codigo ?? '').toLowerCase().includes(q) ||
      (x.nombreSeccion ?? '').toLowerCase().includes(q)
    );
  }

  nuevo(): void {
    this.router.navigate(['/nomina/secciones/nuevo']);
  }

  editar(id: number): void {
    this.router.navigate([`/nomina/secciones/${id}/editar`]);
  }

  eliminar(id: number): void {
    const ok = confirm('¿Desea eliminar esta sección?');
    if (!ok) return;

    this.api.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error eliminando sección', err);
        alert('No se pudo eliminar la sección.');
      }
    });
  }

  imprimir(): void {
    this.printer.imprimir(this.seccionesFiltradas);
  }

  exportar(): void {
    this.exporter.exportar(this.seccionesFiltradas);
  }
}
