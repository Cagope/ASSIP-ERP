// src/app/features/ses/aportes/aportes-list.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { Aportes, AportesApi } from './aportes.api';
import { AportesExporterService } from './aportes-exporter.service';

// Ajusta la ruta según dónde tengas este componente compartido
import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';


@Component({
  selector: 'app-aportes-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './aportes-list.component.html',
  styleUrls: ['./aportes-list.component.scss']
})
export class AportesListComponent implements OnInit {

  private readonly api = inject(AportesApi);
  private readonly exporter = inject(AportesExporterService);

  fechaCorte = '';
  cargando = false;
  error: string | null = null;

  aportes: Aportes[] = [];
  totalRegistros = 0;

  totalSaldo = 0;
  totalRevalorizacion = 0;
  totalOrdinarios = 0;
  totalExtraordinarios = 0;
  totalPromedio = 0;

  ngOnInit(): void {
    // opcional: fechaCorte por defecto = hoy
    const hoy = new Date();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const dia = String(hoy.getDate()).padStart(2, '0');
    this.fechaCorte = `${hoy.getFullYear()}-${mes}-${dia}`;
  }

  buscar(): void {
    this.error = null;

    if (!this.fechaCorte) {
      this.error = 'Debe seleccionar una fecha de corte.';
      return;
    }

    this.cargando = true;
    this.aportes = [];

    this.api.consultar(this.fechaCorte).subscribe({
      next: (data) => {
        this.aportes = data || [];
        this.totalRegistros = this.aportes.length;
        this.recalcularTotales();
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error consultando aportes SES', err);
        this.error = 'Ocurrió un error consultando la información.';
        this.cargando = false;
      }
    });
  }

  limpiar(): void {
    this.error = null;
    this.aportes = [];
    this.totalRegistros = 0;

    this.totalSaldo = 0;
    this.totalRevalorizacion = 0;
    this.totalOrdinarios = 0;
    this.totalExtraordinarios = 0;
    this.totalPromedio = 0;
  }

  exportar(): void {
    if (!this.aportes || this.aportes.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }
    this.exporter.exportar(this.aportes, this.fechaCorte);
  }

  private recalcularTotales(): void {
    this.totalSaldo = 0;
    this.totalRevalorizacion = 0;
    this.totalOrdinarios = 0;
    this.totalExtraordinarios = 0;
    this.totalPromedio = 0;

    for (const a of this.aportes) {
      this.totalSaldo += a.saldoAportes || 0;
      this.totalRevalorizacion += a.valorRevalorizacion || 0;
      this.totalOrdinarios += a.aportesOrdinarios || 0;
      this.totalExtraordinarios += a.aportesExtraordinarios || 0;
      this.totalPromedio += a.promedioDiaAnual || 0;
    }
  }
}
