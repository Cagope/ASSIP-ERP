import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { CargoApi, CargoListDTO } from './cargos.api';
import { CargosPrintService } from './cargos-print.service';
import { CargosExporterService } from './cargos-exporter.service';

@Component({
  standalone: true,
  selector: 'app-cargos-list',
  templateUrl: './cargos-list.component.html',
  styleUrls: ['./cargos-list.component.scss'],
  imports: [CommonModule, RouterModule, FormsModule, HeaderActionsComponent],
})
export class CargosListComponent implements OnInit {

  private readonly api = inject(CargoApi);
  private readonly router = inject(Router);

  private readonly printService = inject(CargosPrintService);
  private readonly exporterService = inject(CargosExporterService);

  cargos: CargoListDTO[] = [];
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
        this.cargos = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error listando cargos nómina', err);
        this.cargos = [];
        this.loading = false;
      }
    });
  }

  get cargosFiltrados(): CargoListDTO[] {
    const q = (this.q ?? '').trim().toLowerCase();
    if (!q) return this.cargos;

    return this.cargos.filter(x =>
      (x.nombreCargo ?? '').toLowerCase().includes(q)
    );
  }

  // =========================================================
  // ✅ ACCIONES HEADER
  // =========================================================

  nuevo(): void {
    this.router.navigate(['/nomina/cargos/nuevo']);
  }

  refrescar(): void {
    this.cargar();
  }

  imprimir(): void {
    this.printService.imprimir(this.cargosFiltrados);
  }

  exportar(): void {
    this.exporterService.exportar(this.cargosFiltrados);
  }

  // =========================================================
  // ✅ ACCIONES FILA
  // =========================================================

  editar(id: number): void {
    this.router.navigate([`/nomina/cargos/${id}/editar`]);
  }

  eliminar(id: number): void {
    const ok = confirm('¿Desea eliminar este cargo?');
    if (!ok) return;

    this.api.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error eliminando cargo', err);
        alert('No se pudo eliminar el cargo.');
      }
    });
  }
}
