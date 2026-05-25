import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ReportingService } from '../../../../shared/reporting/reporting.service';
import {
  ReportQueryRequest,
  ReportResult
} from '../../../../shared/reporting/reporting.api';

import { ConsultaCdatsDetalleComponent } from './consulta-cdats-detalle.component';

@Component({
  selector: 'app-consulta-cdats-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ConsultaCdatsDetalleComponent
  ],
  templateUrl: './consulta-cdats-list.component.html',
  styleUrls: ['./consulta-cdats-list.component.scss']
})
export class ConsultaCdatsListComponent {

  private readonly reporting = inject(ReportingService);

  filtros = {
    documento: '',
    nombres: '',
    primer_apellido: '',
    segundo_apellido: '',
    codigo_cdat: ''
  };

  cdats: any[] = [];

  cargando = false;
  error = '';

  seleccionada: any = null;

  async buscar(): Promise<void> {

    const activos = Object.values(this.filtros)
      .some(v => String(v ?? '').trim() !== '');

    if (!activos) {
      this.error = 'Ingrese al menos un criterio de búsqueda.';
      this.cdats = [];
      this.seleccionada = null;
      return;
    }

    this.cargando = true;
    this.error = '';
    this.seleccionada = null;

    try {

      const req: ReportQueryRequest = {
        schema: 'cdat',
        view: 'vw_cdat_cuentas_total_extendida',
        filters: this.filtros
      };

      const res: ReportResult =
        await this.reporting.ejecutarReporte(req);

      this.cdats = res.data ?? [];

      if (this.cdats.length === 0) {
        this.error = 'No se encontraron CDATs con los criterios ingresados.';
      }

    } catch (e) {

      console.error('❌ Error al consultar CDATs:', e);
      this.error = 'Error al cargar la consulta de CDATs.';

    } finally {
      this.cargando = false;
    }
  }

  limpiar(): void {

    this.filtros = {
      documento: '',
      nombres: '',
      primer_apellido: '',
      segundo_apellido: '',
      codigo_cdat: ''
    };

    this.cdats = [];
    this.error = '';
    this.seleccionada = null;
  }

  verDetalle(cdat: any): void {
    this.seleccionada = cdat;
  }


  nombreCompleto(cdat: any): string {
    return cdat?.nombre_completo_apellidos
      || cdat?.nombre_completo_nombres
      || cdat?.nombres
      || '';
  }

  estadoTexto(cdat: any): string {
    return cdat?.descripcion_estado_cdat
      || cdat?.estado_cdat
      || '';
  }
}
