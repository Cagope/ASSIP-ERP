import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

import { ReportingService } from '../../../../shared/reporting/reporting.service';
import { ReportQueryRequest } from '../../../../shared/reporting/reporting.api';

@Component({
  selector: 'app-exoneracion-gmf',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './exoneracion-gmf.component.html',
  styleUrls: ['./exoneracion-gmf.component.scss']
})
export class ExoneracionGmfComponent implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly reporting = inject(ReportingService);

  persona = signal<any | null>(null);
  cuenta  = signal<any | null>(null);
  today = new Date();

  async ngOnInit(): Promise<void> {
    const idPersona = Number(this.route.snapshot.paramMap.get('idPersona'));
    if (idPersona) {
      await this.cargarPersona(idPersona);
      await this.cargarCuentaAhorros(idPersona);
    }
  }

  // =======================================================
  // 1️⃣ CARGAR PERSONA (vista con dirección + ciudad)
  // =======================================================
  private async cargarPersona(idPersona: number) {
    const req: ReportQueryRequest = {
      schema: 'reporting',
      view: 'vw_hoja_vida_general_total_reciente',
      filters: {
        id_datos_personal: idPersona,
        _disableAgencyFilter: true       // 👈 evita error id_agencia
      }
    };

    const res = await this.reporting.ejecutarReporte(req);

    if (res.data?.length > 0) {
      this.persona.set(res.data[0]);
    }
  }

  // =======================================================
  // 2️⃣ CARGAR SOLO CUENTAS DE AHORROS
  // =======================================================
  private async cargarCuentaAhorros(idPersona: number) {
    const req: ReportQueryRequest = {
      schema: 'depositos',
      view: 'vw_depositos_cuentas_ahorro_total',
      filters: {
        id_datos_personal: idPersona,
        _disableAgencyFilter: true
      }
    };

    const res = await this.reporting.ejecutarReporte(req);

    if (!res.data?.length) return;

    // 🟩 1️⃣ Filtrar SOLO cuentas de ahorro (excluir aportes)
    const soloAhorros = res.data.filter((c: any) =>
      c.codigo_forma !== '01' && c.codigo_forma !== 1
    );

    if (soloAhorros.length === 0) return;

    // 🟦 2️⃣ Seleccionar LA PRIMERA CUENTA (la más antigua)
    const primeraCuenta = soloAhorros.sort(
      (a: any, b: any) =>
        new Date(a.fecha_apertura_cuenta).getTime() -
        new Date(b.fecha_apertura_cuenta).getTime()
    )[0];

    // 🟧 3️⃣ Asignar cuenta al componente
    this.cuenta.set(primeraCuenta);
  }

  nombreCompleto(): string {
    const p = this.persona();
    return p ? `${p.nombres} ${p.primer_apellido} ${p.segundo_apellido}`.trim() : '';
  }

  onBack(): void {
    this.router.navigate(['/hoja-vida/impresiones/afiliacion-list']);
  }

  onPrint(): void {
    window.print();
  }
}
