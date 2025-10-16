import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SarlaftExporter } from '../../../exporters/sarlaft.exporter';

@Component({
  selector: 'app-sarlaft-export-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button class="btn" [disabled]="loading" (click)="onExport()">
      {{ loading ? 'Exportando...' : 'Exportar XLSX' }}
    </button>
  `
})
export class SarlaftExportButtonComponent {
  @Input() q: string | undefined;

  loading = false;
  private exporter = inject(SarlaftExporter);

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
