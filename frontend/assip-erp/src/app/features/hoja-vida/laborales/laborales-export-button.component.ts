import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LaboralesExporter } from '../../../exporters/laborales.exporter';

@Component({
  selector: 'app-laborales-export-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button class="btn" [disabled]="loading" (click)="onExport()">
      {{ loading ? 'Exportando...' : 'Exportar XLSX' }}
    </button>
  `
})
export class LaboralesExportButtonComponent {
  /** Filtro opcional (igual que el listado) */
  @Input() q: string | undefined;

  loading = false;
  private exporter = inject(LaboralesExporter);

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
