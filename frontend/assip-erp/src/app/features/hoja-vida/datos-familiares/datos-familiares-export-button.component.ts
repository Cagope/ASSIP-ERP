import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DatosFamiliaresExporter } from '../../../exporters/datos-familiares.exporter';

@Component({
  selector: 'app-datos-familiares-export-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button class="btn" [disabled]="loading" (click)="onExport()">
      {{ loading ? 'Exportando...' : 'Exportar XLSX' }}
    </button>
  `
})
export class DatosFamiliaresExportButtonComponent {
  /** Filtro opcional (mismo criterio que en el listado) */
  @Input() q: string | undefined;

  loading = false;
  private exporter = inject(DatosFamiliaresExporter);

  async onExport() {
    if (this.loading) return;
    this.loading = true;
    try {
      await this.exporter.exportXlsx({ q: (this.q ?? '').trim() || undefined });
    } catch (e) {
      console.error(e);
      alert('Error exportando XLSX');
    } finally {
      this.loading = false;
    }
  }
}
