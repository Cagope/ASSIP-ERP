import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

import { DesprendibleApi, DesprendibleEmpleadoListDTO } from './desprendible.api';
import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { PeriodoNominaListDTO } from '../periodos-nomina/periodos-nomina.api';

@Component({
  standalone: true,
  selector: 'app-desprendible',
  templateUrl: './desprendible.component.html',
  styleUrls: ['./desprendible.component.scss'],
  imports: [CommonModule, HeaderActionsComponent],
})
export class DesprendibleComponent implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(DesprendibleApi);

  idPeriodo!: number;

  // 👇 ESTE ES EL DATO CLAVE
  periodo?: PeriodoNominaListDTO;

  loading = false;
  empleados: DesprendibleEmpleadoListDTO[] = [];

  ngOnInit(): void {

    // =========================
    // ID PERÍODO (URL)
    // =========================
    const id = this.route.snapshot.paramMap.get('idPeriodo');
    if (!id) {
      alert('Período de nómina inválido.');
      this.router.navigate(['/nomina/periodos']);
      return;
    }

    this.idPeriodo = Number(id);

    // =========================
    // PERÍODO (DESDE LISTADO)
    // ✅ USAR history.state
    // =========================
    this.periodo = history.state?.periodo;

    // =========================
    // CARGAR EMPLEADOS
    // =========================
    this.cargar();
  }

  cargar(): void {
    this.loading = true;

    this.api.listarEmpleados(this.idPeriodo).subscribe({
      next: (data) => {
        this.empleados = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando empleados del período', err);
        alert('No se pudieron cargar los empleados del período.');
        this.empleados = [];
        this.loading = false;
      }
    });
  }

  imprimir(e: DesprendibleEmpleadoListDTO): void {
    this.api.imprimirPdf(this.idPeriodo, e.idContrato);
  }

  volver(): void {
    this.router.navigate(['/nomina/periodos']);
  }

  // =========================================================
  // GENERAR TODOS (ZIP)
  // =========================================================
  generarTodos(): void {

    if (!this.empleados || this.empleados.length === 0) {
      alert('No hay empleados para generar desprendibles.');
      return;
    }

    const ok = confirm(
      `¿Generar desprendibles para ${this.empleados.length} empleados?\n\n` +
      `Se descargará un archivo ZIP con los PDFs.`
    );

    if (!ok) return;

    // =========================
    // NOMBRE DEL ARCHIVO (PRO)
    // =========================
    let nombre = `desprendibles_periodo_${this.idPeriodo}.zip`;

    if (this.periodo) {
      const mes = String(this.periodo.mes).padStart(2, '0');
      const tipo = (this.periodo.tipoPeriodo ?? '').replace(/\s+/g, '_').toUpperCase();

      nombre = `desprendibles_nomina_${this.periodo.anio}-${mes}${tipo ? '_' + tipo : ''}.zip`;
    }

    this.api.generarTodosPdf(this.idPeriodo, nombre);
  }

}
