import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import {
  LocalizacionesApi,
  LocalizacionListDTO
} from './localizaciones.api';

import { GeneralApi } from '../../../shared/general/general.api';
import { LocalizacionesPrintService } from './localizaciones-print.service';
import { LocalizacionesExporterService } from './localizaciones-exporter.service';
import { SessionService } from '../../../core/auth/session.service';

@Component({
  selector: 'app-localizaciones-list',
  standalone: true,
  imports: [
    CommonModule,
    HeaderActionsComponent
  ],
  templateUrl: './localizaciones-list.component.html',
  styleUrls: ['./localizaciones-list.component.scss']
})
export class LocalizacionesListComponent implements OnInit {

  private readonly api = inject(LocalizacionesApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly printer = inject(LocalizacionesPrintService);
  private readonly exporter = inject(LocalizacionesExporterService);
  private readonly router = inject(Router);
  private readonly session = inject(SessionService);

  localizaciones: LocalizacionListDTO[] = [];
  agenciasMap = new Map<number, string>();

  cargando = false;
  error = '';
  // ===============================
  // PERMISOS
  // ===============================
  readonly puedeEditar =
    this.session.tienePermiso('ACTIVOS_FIJOS_EDIT');

  ngOnInit(): void {
    this.cargarAgencias();
    this.cargar();
  }

  cargarAgencias(): void {
    this.generalApi.listarAgencias().subscribe(data => {
      data.forEach((a: any) => {
        this.agenciasMap.set(a.idAgencia, a.nombreAgencia);
      });
    });
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';

    this.api.listar().subscribe({
      next: (data) => this.localizaciones = data,
      error: () => this.error = 'Error cargando localizaciones.',
      complete: () => this.cargando = false
    });
  }

  nombreAgencia(idAgencia: number): string {
    return this.agenciasMap.get(idAgencia) ?? '';
  }

  // ===============================
  // ACCIONES HEADER
  // ===============================
  imprimir(): void {
    this.printer.imprimir(this.localizaciones);
  }

  exportar(): void {
    this.exporter.exportar(this.localizaciones);
  }

  // ===============================
  // ACCIONES CRUD
  // ===============================
  nuevo(): void {
    if (!this.puedeEditar) return;

    this.router.navigate(['/activos-fijos/localizaciones/nuevo']);
  }

  editar(id: number): void {
    if (!this.puedeEditar) return;

    this.router.navigate(['/activos-fijos/localizaciones', id, 'editar']);
  }

  eliminar(item: LocalizacionListDTO): void {
    if (!this.puedeEditar) return;

    if (!confirm('¿Eliminar esta localización?')) return;

    this.api.eliminar(item.idLocalizacion).subscribe(() => {
      this.cargar();
    });
  }
}
