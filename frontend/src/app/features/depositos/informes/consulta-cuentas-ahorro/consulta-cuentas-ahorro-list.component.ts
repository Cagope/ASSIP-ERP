import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ReportingService } from '../../../../shared/reporting/reporting.service';
import { ReportQueryRequest, ReportResult } from '../../../../shared/reporting/reporting.api';
import { CuentasAhorroDetalleComponent } from './consulta-cuentas-ahorro-detalle.component';
import { ExtractoModalComponent } from './consulta-extracto-modal.component';
import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';


// ⭐ Importar SARLAFT
import {
  SarlaftApi,
  EvaluacionSarlaftRequest,
  EvaluacionSarlaftResponse
} from '../../../../shared/sarlaft/sarlaft.api';

@Component({
  selector: 'app-cuentas-ahorro-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    CuentasAhorroDetalleComponent,
    ExtractoModalComponent
  ],
  templateUrl: './consulta-cuentas-ahorro-list.component.html',
  styleUrls: ['./consulta-cuentas-ahorro-list.component.scss']
})
export class CuentasAhorroListComponent {

  private readonly reporting = inject(ReportingService);
  private readonly sarlaft = inject(SarlaftApi);   // ⭐ NUEVO

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
  modalVisible = false;
  seleccionada: any = null;

  // ⭐ Resultado SARLAFT para mostrar en el detalle
  alertaSarlaft: any = null;

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
    this.seleccionada = null;

    try {
      const req: ReportQueryRequest = {
        schema: 'depositos',
        view: 'vw_depositos_cuentas_ahorro_total',
        scope: 'GLOBAL',
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

  /** 🕵️ Ver detalle de una cuenta (con evaluación SARLAFT) */
  async verDetalle(cuenta: any): Promise<void> {

    this.seleccionada = null;
    this.error = '';

    let cuentaCompleta = { ...cuenta };

    try {

      const reqHv: ReportQueryRequest = {
        schema: 'reporting',
        view: 'vw_hoja_vida_general_total_reciente',
        scope: 'GLOBAL',
        filters: {
          id_datos_personal: cuenta.id_datos_personal
        }
      };

      const resHv = await this.reporting.ejecutarReporte(reqHv);
      const hv = resHv.data?.[0];

      if (hv) {
        cuentaCompleta = {
          ...cuentaCompleta,
          ...hv
        };
      }

    } catch (error) {
      console.error('Error cargando hoja de vida completa:', error);
    }

    this.seleccionada = cuentaCompleta;

    const req: EvaluacionSarlaftRequest = {
      idDatosPersonal: cuentaCompleta.id_datos_personal,
      idAgencia: cuentaCompleta.id_agencia,
      codigoModulo: '02',
      accion: 'CONSULTA',
      monto: 0,

      fechaUltimaActualizacion: cuentaCompleta.fecha_actualizacion,
      fechaNacimiento: cuentaCompleta.fecha_nacimiento,
      tipoDocumento: cuentaCompleta.tipo_documento,
      codigoFormaAhorro: cuentaCompleta.id_forma_ahorro,

      ingresosMensuales:
        (cuentaCompleta.valor_salario ?? 0) +
        (cuentaCompleta.valor_pension ?? 0) +
        (cuentaCompleta.ingresos_arriendo ?? 0) +
        (cuentaCompleta.ingresos_comisiones ?? 0) +
        (cuentaCompleta.otros_ingresos ?? 0),

      egresosMensuales:
        (cuentaCompleta.egresos_familiares ?? 0) +
        (cuentaCompleta.egresos_arriendo ?? 0) +
        (cuentaCompleta.egresos_credito ?? 0) +
        (cuentaCompleta.otros_egresos ?? 0),

      totalActivos: cuentaCompleta.total_activos ?? 0,
      totalPasivos: cuentaCompleta.total_pasivos ?? 0
    };

    this.sarlaft.evaluar(req).subscribe({
      next: (res: EvaluacionSarlaftResponse) => {
        this.seleccionada = {
          ...cuentaCompleta,
          sarlaft: res
        };
      },

      error: err => {
        console.error('❌ Error SARLAFT:', err);
      }
    });
  }

  /** 🧾 Abre el modal de extracto */
  abrirExtracto(cuenta: any): void {
    this.seleccionada = cuenta;
    this.modalVisible = true;
  }
}
