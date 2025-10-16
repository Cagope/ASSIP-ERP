import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DatosPersonalesExporter } from '../../../exporters/datos-personales.exporter';

@Component({
  selector: 'app-datos-personales-export-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button class="btn" [disabled]="loading" (click)="onExport()">
      {{ loading ? 'Exportando...' : 'Exportar XLSX' }}
    </button>
  `
})
export class DatosPersonalesExportButtonComponent {
  @Input() q: string | undefined;
  loading = false;
  private exporter = inject(DatosPersonalesExporter);

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
