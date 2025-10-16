import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DirectivosExporter } from '../../../exporters/directivos.exporter';

@Component({
  selector: 'app-directivos-export-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button class="btn" [disabled]="loading" (click)="onExport()">
      {{ loading ? 'Exportando…' : 'Exportar XLSX' }}
    </button>
  `
})
export class DirectivosExportButtonComponent {
  /** filtro q (igual que en el listado principal) */
  @Input() q: string | undefined;

  loading = false;
  private exporter = inject(DirectivosExporter);

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
