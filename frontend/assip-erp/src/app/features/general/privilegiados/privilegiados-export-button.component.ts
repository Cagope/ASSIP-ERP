import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrivilegiadosExporter } from '../../../exporters/privilegiados.exporter';

@Component({
  selector: 'app-privilegiados-export-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button class="btn" [disabled]="loading" (click)="onExport()">
      {{ loading ? 'Exportando...' : 'Exportar XLSX' }}
    </button>
  `
})
export class PrivilegiadosExportButtonComponent {
  @Input() q: string | undefined;

  loading = false;
  private exporter = inject(PrivilegiadosExporter);

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
