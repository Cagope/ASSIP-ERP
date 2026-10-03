import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ConsultaCdatsService } from './consulta-cdats.service';
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

  private readonly consultaCdatsService = inject(ConsultaCdatsService);

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

      this.cdats = await this.consultaCdatsService.consultar({
        documento: this.filtros.documento,
        nombres: this.filtros.nombres,
        primerApellido: this.filtros.primer_apellido,
        segundoApellido: this.filtros.segundo_apellido,
        codigoCdat: this.filtros.codigo_cdat
      });

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
