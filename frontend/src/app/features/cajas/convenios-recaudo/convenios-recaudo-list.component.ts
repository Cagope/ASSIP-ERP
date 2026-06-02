import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import {
  ConvenioRecaudo,
  ConveniosRecaudoApi
} from './convenios-recaudo.api';

import {
  HeaderActionsComponent
} from '../../../shared/header-actions/header-actions.component';

import {
  ConveniosRecaudoExporterService
} from './convenios-recaudo-exporter.service';

import {
  ConveniosRecaudoPrintService
} from './convenios-recaudo-print.service';

@Component({
  selector: 'app-convenios-recaudo-list',
  standalone: true,
  imports: [
    CommonModule,
    HeaderActionsComponent
  ],
  templateUrl: './convenios-recaudo-list.component.html',
  styleUrls: ['./convenios-recaudo-list.component.scss']
})
export class ConveniosRecaudoListComponent implements OnInit {

  private readonly api = inject(ConveniosRecaudoApi);
  private readonly router = inject(Router);

  private readonly exporter =
    inject(ConveniosRecaudoExporterService);

  private readonly printer =
    inject(ConveniosRecaudoPrintService);

  lista: ConvenioRecaudo[] = [];

  cargando = false;
  error = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';

    this.api.listar().subscribe({
      next: data => {
        this.lista = data || [];
      },
      error: err => {
        console.error(err);

        this.error =
          err?.error?.message ||
          'Error al cargar convenios de recaudo.';
      },
      complete: () => {
        this.cargando = false;
      }
    });
  }

  nuevo(): void {
    this.router.navigate([
      '/cajas/convenios-recaudo/nuevo'
    ]);
  }

  editar(item: ConvenioRecaudo): void {
    if (!item.idConvenio) {
      return;
    }

    this.router.navigate([
      '/cajas/convenios-recaudo',
      item.idConvenio,
      'editar'
    ]);
  }

  eliminar(item: ConvenioRecaudo): void {
    if (!item.idConvenio) {
      return;
    }

    if (!confirm('¿Eliminar este convenio de recaudo?')) {
      return;
    }

    this.api.eliminar(item.idConvenio).subscribe({
      next: () => {
        this.cargar();
      },
      error: err => {
        console.error(err);

        alert(
          err?.error?.message ||
          'No fue posible eliminar el convenio.'
        );
      }
    });
  }

  estadoTexto(estado?: string): string {
    return estado === 'A'
      ? 'Activo'
      : 'Inactivo';
  }

  exportarExcel(): void {
    this.exporter.exportarConvenios(
      this.lista
    );
  }

  imprimirListado(): void {
    this.printer.imprimir(
      this.lista
    );
  }

}
