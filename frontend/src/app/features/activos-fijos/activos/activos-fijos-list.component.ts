import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { SessionService } from '../../../core/auth/session.service';

import {
  ActivosFijosApi,
  ActivoFijoListDTO
} from './activos-fijos.api';

@Component({
  selector: 'app-activos-fijos-list',
  standalone: true,
  imports: [
    CommonModule,
    HeaderActionsComponent
  ],
  templateUrl: './activos-fijos-list.component.html',
  styleUrls: ['./activos-fijos-list.component.scss']
})
export class ActivosFijosListComponent implements OnInit {

  private readonly api = inject(ActivosFijosApi);
  private readonly router = inject(Router);
  private readonly session = inject(SessionService);

  activos: ActivoFijoListDTO[] = [];
  cargando = false;
  error = '';

  // =========================================================
  // PERMISOS
  // =========================================================
  readonly puedeEditar =
    this.session.tienePermiso('ACTIVOS_FIJOS_EDIT');

  // =========================================================
  // INIT
  // =========================================================
  ngOnInit(): void {
    this.cargar();
  }

  // =========================================================
  // LISTAR
  // =========================================================
  cargar(): void {
    this.cargando = true;
    this.error = '';

    this.api.listar().subscribe({
      next: (data) => this.activos = data,
      error: () => this.error = 'Error cargando activos fijos.',
      complete: () => this.cargando = false
    });
  }

  // =========================================================
  // ACCIONES
  // =========================================================
  nuevo(): void {
    if (!this.puedeEditar) return;

    this.router.navigate(['/activos-fijos/activos/nuevo']);
  }

  editar(id: number): void {
    if (!this.puedeEditar) return;

    this.router.navigate(['/activos-fijos/activos', id, 'editar']);
  }

  eliminar(item: ActivoFijoListDTO): void {
    if (!this.puedeEditar) return;

    if (!confirm('¿Eliminar este activo fijo?')) return;

    this.api.eliminar(item.idActivoFijo).subscribe(() => {
      this.cargar();
    });
  }
}
