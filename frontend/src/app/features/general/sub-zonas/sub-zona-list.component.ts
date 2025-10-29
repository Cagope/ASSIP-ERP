import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { SubZonasApi, SubZona } from './sub-zonas.api';
import { SubZonasExporterService } from './sub-zonas-exporter.service';
import { SubZonasPrintService } from './sub-zonas-print.service';

@Component({
  selector: 'app-sub-zona-list',
  standalone: true,
  imports: [CommonModule, HeaderActionsComponent],
  templateUrl: './sub-zona-list.component.html',
  styleUrls: ['./sub-zona-list.component.scss']
})
export class SubZonaListComponent implements OnInit {
  private readonly api = inject(SubZonasApi);
  private readonly exporter = inject(SubZonasExporterService);
  private readonly printer = inject(SubZonasPrintService);
  private readonly router = inject(Router);

  subzonas: SubZona[] = [];
  cargando = false;
  error = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.api.listar().subscribe({
      next: (data) => (this.subzonas = data),
      error: () => (this.error = 'Error al cargar subzonas'),
      complete: () => (this.cargando = false)
    });
  }

  nuevaSubZona(): void {
    this.router.navigate(['/general/sub-zonas/nuevo']);
  }

  editarSubZona(id: number): void {
    this.router.navigate(['/general/sub-zonas', id, 'editar']);
  }

  eliminarSubZona(id: number): void {
    if (!confirm('¿Eliminar esta subzona?')) return;
    this.api.eliminar(id).subscribe(() => this.cargar());
  }

  exportarExcel(): void {
    this.exporter.exportarSubZonas(this.subzonas);
  }

  imprimirListado(): void {
    this.printer.imprimir(this.subzonas);
  }
}
