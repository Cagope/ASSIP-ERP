import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';

import {
  BloquesApi,
  BloqueListDTO
} from './bloques.api';

import { BloquesPrintService } from './bloques-print.service';
import { BloquesExporterService } from './bloques-exporter.service';

import { SessionService } from '../../../core/auth/session.service';

@Component({
  selector: 'app-bloques-list',
  standalone: true,
  imports: [
    CommonModule,
    HeaderActionsComponent
  ],
  templateUrl: './bloques-list.component.html',
  styleUrls: ['./bloques-list.component.scss']
})
export class BloquesListComponent implements OnInit {

  // =========================================================
  // INYECCIONES
  // =========================================================
  private readonly api = inject(BloquesApi);
  private readonly printer = inject(BloquesPrintService);
  private readonly exporter = inject(BloquesExporterService);
  private readonly router = inject(Router);
  private readonly session = inject(SessionService);

  // =========================================================
  // PERMISOS
  // =========================================================
  readonly puedeEditar =
    this.session.tienePermiso('ACTIVOS_FIJOS_EDIT');

  // =========================================================
  // DATOS
  // =========================================================
  bloques: BloqueListDTO[] = [];

  cargando = false;
  error = '';

  // =========================================================
  // INIT
  // =========================================================
  ngOnInit(): void {
    this.cargar();
  }

  // =========================================================
  // CARGA
  // =========================================================
  cargar(): void {
    this.cargando = true;
    this.error = '';

    this.api.listar().subscribe({
      next: (data) => this.bloques = data,
      error: () => this.error = 'Error cargando bloques.',
      complete: () => this.cargando = false
    });
  }

  // =========================================================
  // HEADER ACTIONS
  // =========================================================
  imprimir(): void {
    this.printer.imprimir(this.bloques);
  }

  exportar(): void {
    this.exporter.exportar(this.bloques);
  }

  // =========================================================
  // CRUD
  // =========================================================
  nuevo(): void {

    if (!this.puedeEditar) return;

    this.router.navigate(['/activos-fijos/bloques/nuevo']);
  }

  editar(id: number): void {

    if (!this.puedeEditar) return;

    this.router.navigate(['/activos-fijos/bloques', id, 'editar']);
  }

  eliminar(item: BloqueListDTO): void {

    if (!this.puedeEditar) return;

    if (!confirm('¿Eliminar este bloque?')) return;

    this.api.eliminar(item.idBloque).subscribe(() => {
      this.cargar();
    });
  }
}
