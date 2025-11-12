import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { ReportingService } from '../../../shared/reporting/reporting.service';
import { ReportQueryRequest, ReportResult } from '../../../shared/reporting/reporting.api';
import { CuentasAhorroDetalleComponent } from './cuentas-ahorro-detalle.component';

/**
 * 💰 Cuentas de Ahorro — Consulta dinámica avanzada
 * ------------------------------------------------------------
 * Permite buscar cuentas de ahorro filtrando por documento,
 * nombres, apellidos o código de cuenta. Consulta el backend
 * directamente, sin mantener datos en memoria.
 */
@Component({
  selector: 'app-cuentas-ahorro-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    CuentasAhorroDetalleComponent // ✅ incluye el componente hijo
  ],
  templateUrl: './cuentas-ahorro-list.component.html',
  styleUrls: ['./cuentas-ahorro-list.component.scss']
})
export class CuentasAhorroListComponent {
  private readonly reporting = inject(ReportingService);

  filtros = {
    documento: '',
    nombres: '',
    primer_apellido: '',
    segundo_apellido: '',
    codigo_cuenta: ''
  };

  cuentas: any[] = [];
  cargando = false;
  error = '';
  seleccionada: any = null; // ✅ nueva propiedad para manejar el detalle

  /** 🔍 Buscar usando filtros individuales */
  async buscar(): Promise<void> {
    const activos = Object.values(this.filtros).some(v => v.trim() !== '');
    if (!activos) {
      this.error = 'Ingrese al menos un criterio de búsqueda.';
      this.cuentas = [];
      return;
    }

    this.cargando = true;
    this.error = '';
    this.seleccionada = null; // ✅ al hacer nueva búsqueda, se oculta el detalle

    try {
      const req: ReportQueryRequest = {
        schema: 'depositos',
        view: 'vw_depositos_cuentas_ahorro_total',
        filters: this.filtros
      };

      const res: ReportResult = await this.reporting.ejecutarReporte(req);
      this.cuentas = res.data ?? [];

      if (this.cuentas.length === 0) {
        this.error = 'No se encontraron resultados.';
      }
    } catch (e) {
      console.error('❌ Error al buscar cuentas de ahorro:', e);
      this.error = 'Error al cargar las cuentas de ahorro.';
    } finally {
      this.cargando = false;
    }
  }

  /** 🧹 Limpia los filtros y resultados */
  limpiar(): void {
    this.filtros = {
      documento: '',
      nombres: '',
      primer_apellido: '',
      segundo_apellido: '',
      codigo_cuenta: ''
    };
    this.cuentas = [];
    this.error = '';
    this.seleccionada = null;
  }

  /** 🕵️ Ver detalle de una cuenta */
  verDetalle(cuenta: any): void {
    this.seleccionada = cuenta; // ✅ abre el detalle en la misma vista
  }
}
