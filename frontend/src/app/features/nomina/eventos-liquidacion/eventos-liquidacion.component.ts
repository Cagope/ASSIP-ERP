import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';

import {
  EventosLiquidacionApi,
  EventoLiquidacionListDTO
} from './eventos-liquidacion.api';


@Component({
  standalone: true,
  selector: 'app-eventos-liquidacion',
  imports: [CommonModule, FormsModule, RouterModule, HeaderActionsComponent],
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

  eliminar(row: EventoLiquidacionListDTO): void {
    const ok = confirm(`¿Desea eliminar el evento ${row.idEventoLiquidacion}?`);
    if (!ok) return;

    this.api.eliminar(row.idEventoLiquidacion).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error(err);
        alert(err?.error?.message || 'No fue posible eliminar el evento.');
      }
    });
  }

  getBadgeClass(estado: string | null | undefined): string {
    switch ((estado || '').toUpperCase()) {
      case 'ACTIVO':
        return 'badge bg-success';
      default:
        return 'badge bg-light text-dark';
    }
  }
}
