import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { FormasAhorroApi, FormaAhorro } from './formas-ahorro.api';
import { FormaAhorroExporterService } from './formas-ahorro-exporter.service';
import { FormaAhorroPrintService } from './formas-ahorro-print.service';

@Component({
  selector: 'app-formas-ahorro-list',
  standalone: true,
  imports: [
    CommonModule,
    HeaderActionsComponent
  ],
  templateUrl: './formas-ahorro-list.component.html',
  styleUrls: ['./formas-ahorro-list.component.scss']
})
export class FormasAhorroListComponent implements OnInit {

  private readonly api = inject(FormasAhorroApi);
  private readonly exporter = inject(FormaAhorroExporterService);
  private readonly printer = inject(FormaAhorroPrintService);
  private readonly router = inject(Router);

  formas: FormaAhorro[] = [];
  cargando = false;
  error = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;

    this.api.listar().subscribe({
      next: (data) => this.formas = data,
      error: () => this.error = 'Error cargando formas de ahorro.',
      complete: () => this.cargando = false
    });
  }

  nueva(): void {
    this.router.navigate(['/depositos/formas-ahorro/nuevo']);
  }

  editar(id: number): void {
    this.router.navigate(['/depositos/formas-ahorro', id, 'editar']);
  }

  eliminar(f: FormaAhorro): void {
    if (!confirm('¿Eliminar esta forma de ahorro?')) return;
    if (!f.id) return;

    this.api.eliminar(f.id).subscribe(() => this.cargar());
  }

  exportar(): void {
    this.exporter.exportar(this.formas);
  }

  imprimir(): void {
    this.printer.imprimir(this.formas);
  }
}
