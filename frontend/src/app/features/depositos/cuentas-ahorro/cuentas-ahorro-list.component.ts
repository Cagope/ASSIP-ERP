import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ReportingService } from '../../../shared/reporting/reporting.service';
import { ReportQueryRequest, ReportResult } from '../../../shared/reporting/reporting.api';
import { CuentasAhorroDetalleComponent } from './cuentas-ahorro-detalle.component';
import { ExtractoModalComponent } from './extracto-modal.component';


// ⭐ Importar SARLAFT
import {
  SarlaftApi,
  EvaluacionSarlaftRequest,
  EvaluacionSarlaftResponse
} from '../../../shared/sarlaft/sarlaft.api';

@Component({
  selector: 'app-cuentas-ahorro-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CuentasAhorroDetalleComponent,
    ExtractoModalComponent
  ],
  templateUrl: './cuentas-ahorro-list.component.html',
  styleUrls: ['./cuentas-ahorro-list.component.scss']
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
  verDetalle(cuenta: any): void {
    console.log("==== CUENTA DESDE BACKEND ====");
    console.log(JSON.stringify(cuenta, null, 2));

    this.seleccionada = cuenta;

    // ⭐ Construir request SARLAFT
    // ⭐ Construir request SARLAFT (versión completa)
    const req: EvaluacionSarlaftRequest = {
      idDatosPersonal: cuenta.id_datos_personal,
      idAgencia: cuenta.codigo_agencia,
      codigoModulo: "02",
      accion: "CONSULTA",
      monto: 0,

      fechaUltimaActualizacion: cuenta.fecha_actualizacion,
      fechaNacimiento: cuenta.fecha_nacimiento,
      tipoDocumento: cuenta.tipo_documento,
      codigoFormaAhorro: cuenta.codigo_forma,

      // 🔹 INGRESOS MENSUALES (suma real)
      ingresosMensuales:
        (cuenta.valor_salario ?? 0) +
        (cuenta.valor_pension ?? 0) +
        (cuenta.ingresos_arriendo ?? 0) +
        (cuenta.ingresos_comisiones ?? 0) +
        (cuenta.otros_ingresos ?? 0),

      // 🔹 EGRESOS MENSUALES (suma real)
      egresosMensuales:
        (cuenta.egresos_familiares ?? 0) +
        (cuenta.egresos_arriendo ?? 0) +
        (cuenta.egresos_credito ?? 0) +
        (cuenta.otros_egresos ?? 0),

      // 🔹 Total activos / pasivos (NO calcular)
      totalActivos: cuenta.total_activos ?? 0,
      totalPasivos: cuenta.total_pasivos ?? 0
    };


    console.log("==== SARLAFT REQUEST ====");
    console.log(req);

    // ⭐ Evaluar SARLAFT
    this.sarlaft.evaluar(req).subscribe({
      next: (res: EvaluacionSarlaftResponse) => {
        console.log("==== SARLAFT RESPONSE ====");
        console.log(res);

        // ⭐ Reasignación obligatoria para que el DETALLE reciba el cambio
        this.seleccionada = {
          ...this.seleccionada,
          sarlaft: res
        };
      },

      error: (err) => {
        console.error("❌ Error SARLAFT:", err);
      }
    });
  }


  /** 🧾 Abre el modal de extracto */
  abrirExtracto(cuenta: any): void {
    this.seleccionada = cuenta;
    this.modalVisible = true;
  }
}
