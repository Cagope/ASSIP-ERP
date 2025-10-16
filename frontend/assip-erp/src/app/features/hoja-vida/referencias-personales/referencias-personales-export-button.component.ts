import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReferenciasPersonalesExporter } from '../../../exporters/referencias-personales.exporter';

@Component({
  selector: 'app-referencias-personales-export-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button class="btn" [disabled]="loading" (click)="onExport()">
      {{ loading ? 'Exportando...' : 'Exportar XLSX' }}
    </button>
  `,
})
export class ReferenciasPersonalesExportButtonComponent {
  @Input() q: string | undefined;
  loading = false;
  private exporter = inject(ReferenciasPersonalesExporter);

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
