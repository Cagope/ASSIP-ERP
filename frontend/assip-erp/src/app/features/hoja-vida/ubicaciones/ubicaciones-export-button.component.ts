import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UbicacionesExporter } from '../../../exporters/ubicaciones.exporter';

@Component({
  standalone: true,
  selector: 'app-ubicaciones-export-button',
  imports: [CommonModule],
  template: `
    <button type="button" class="btn btn--secondary" (click)="onExport()" [disabled]="loading">
      {{ loading ? 'Exportando…' : 'Exportar' }}
    </button>
  `,
})
export class UbicacionesExportButtonComponent {
  @Input() q: string | undefined;
  loading = false;

  private exporter = inject(UbicacionesExporter);

  async onExport(): Promise<void> {
    this.loading = true;
    try {
      await this.exporter.exportXlsx({ q: this.q });
    } catch (e) {
      alert('No fue posible exportar.');
    } finally {
      this.loading = false;
    }
  }
}
