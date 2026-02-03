import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import {
  TiposComprobantesApi,
  TipoComprobante
} from './tipos-comprobantes.api';

@Component({
  selector: 'app-tipos-comprobantes-list',
  standalone: true,
  imports: [CommonModule, HeaderActionsComponent],
  templateUrl: './tipos-comprobantes-list.component.html',
  styleUrls: ['./tipos-comprobantes-list.component.scss']
})
export class TiposComprobantesListComponent implements OnInit {

  private readonly api = inject(TiposComprobantesApi);
  private readonly router = inject(Router);

  tipos: TipoComprobante[] = [];
  cargando = false;
  error = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;

    // ✅ CRUD ADMIN → TODOS (todas las agencias, activos + inactivos)
    this.api.listarTodos().subscribe({
      next: (data: TipoComprobante[]) => {
        this.tipos = data;
      },
      error: () => {
        this.error = 'Error al cargar tipos de comprobantes';
      },
      complete: () => {
        this.cargando = false;
      }
    });
  }

  nuevo(): void {
    this.router.navigate(['/contabilidad/tipos-comprobantes/nuevo']);
  }

  editar(t: TipoComprobante): void {
    this.router.navigate([
      '/contabilidad/tipos-comprobantes',
      t.tipoComprobante,
      t.idAgencia,
      'editar'
    ]);
  }

  cambiarEstado(t: TipoComprobante): void {
    const nuevoEstado = !t.comprobanteActivo;

    this.api
      .cambiarEstado(
        t.tipoComprobante,
        t.idAgencia,
        nuevoEstado
      )
      .subscribe(() => {
        t.comprobanteActivo = nuevoEstado;
      });
  }
}
