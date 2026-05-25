import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';

import { InconsistenciasApi, InconsistenciasRequest } from './inconsistencias.api';
import { InconsistenciasExporterService } from './inconsistencias-exporter.service';

@Component({
  selector: 'app-inconsistencias-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './inconsistencias-list.component.html',
  styleUrls: ['./inconsistencias-list.component.scss']
})
export class InconsistenciasListComponent implements OnInit {

  private readonly api = inject(InconsistenciasApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly exporter = inject(InconsistenciasExporterService);

  cargando = false;
  error = '';
  items: any[] = [];

  agencias: any[] = [];

  filtros: InconsistenciasRequest = {
    agencia: '0',
    fechaCorte: ''
  };

  async ngOnInit(): Promise<void> {
    await this.cargarAgencias();
  }

  async cargarAgencias(): Promise<void> {
    try {
      const agencias =
        await this.generalApi.listarAgencias().toPromise();

      this.agencias =
        agencias || [];

    } catch (e) {
      console.error('Error cargando agencias:', e);
      this.agencias = [];
    }
  }

  buscar(): void {

    if (!this.filtros.fechaCorte) {
      this.error = 'Debe seleccionar una fecha de corte';
      return;
    }

    this.cargando = true;
    this.error = '';
    this.items = [];

    this.api.consultar(this.filtros).subscribe({
      next: (res: any[]) => {
        this.items = res || [];
        this.cargando = false;
      },
      error: () => {
        this.error = 'Error consultando inconsistencias';
        this.cargando = false;
      }
    });
  }

  limpiar(): void {
    this.filtros = {
      agencia: '0',
      fechaCorte: ''
    };

    this.items = [];
    this.error = '';
  }

  exportar(): void {
    if (!this.items || this.items.length === 0) {
      return;
    }

    this.exporter.exportar(this.items);
  }
}
