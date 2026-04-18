import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import {
  EventosLiquidacionApi,
  EventoLiquidacionListDTO
} from './eventos-liquidacion.api';

@Component({
  standalone: true,
  selector: 'app-eventos-liquidacion',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './eventos-liquidacion.component.html',
  styleUrls: ['./eventos-liquidacion.component.scss']
})
export class EventosLiquidacionComponent implements OnInit {

  private readonly api = inject(EventosLiquidacionApi);
  private readonly router = inject(Router);

  rows: EventoLiquidacionListDTO[] = [];
  rowsFiltradas: EventoLiquidacionListDTO[] = [];

  q = '';
  cargando = false;
  error = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';

    this.api.listar().subscribe({
      next: (data) => {
        this.rows = data ?? [];
        this.filtrar();
        this.cargando = false;
      },
      error: (err) => {
        console.error(err);
        this.error = err?.error?.message || 'Error cargando eventos de liquidación.';
        this.rows = [];
        this.rowsFiltradas = [];
        this.cargando = false;
      }
    });
  }

  filtrar(): void {
    const term = (this.q || '').trim().toLowerCase();

    if (!term) {
      this.rowsFiltradas = [...this.rows];
      return;
    }

    this.rowsFiltradas = this.rows.filter(r =>
      `
        ${r.idEventoLiquidacion ?? ''}
        ${r.documentoEmpleado ?? ''}
        ${r.nombreEmpleado ?? ''}
        ${r.idContrato ?? ''}
        ${r.tipoEvento ?? ''}
        ${r.numeroSoporte ?? ''}
        ${r.responsablePago ?? ''}
        ${r.estado ?? ''}
        ${r.observacion ?? ''}
      `
        .toLowerCase()
        .includes(term)
    );
  }

  nuevo(): void {
    this.router.navigate(['/nomina/eventos-liquidacion/nuevo']);
  }

  editar(row: EventoLiquidacionListDTO): void {
    this.router.navigate(['/nomina/eventos-liquidacion', row.idEventoLiquidacion, 'editar']);
  }

  cambiarEstado(row: EventoLiquidacionListDTO, estado: string): void {
    const ok = confirm(`¿Desea cambiar el estado del evento ${row.idEventoLiquidacion} a ${estado}?`);
    if (!ok) return;

    this.api.cambiarEstado(row.idEventoLiquidacion, estado).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error(err);
        alert(err?.error?.message || 'No fue posible actualizar el estado.');
      }
    });
  }

  getBadgeClass(estado: string | null | undefined): string {
    switch ((estado || '').toUpperCase()) {
      case 'ACTIVO':
        return 'badge bg-success';
      case 'INACTIVO':
        return 'badge bg-secondary';
      case 'ANULADO':
        return 'badge bg-danger';
      default:
        return 'badge bg-light text-dark';
    }
  }
}
