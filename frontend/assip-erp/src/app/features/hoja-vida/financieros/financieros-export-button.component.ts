import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FinancierosExporter } from '../../../exporters/financieros.exporter'; // ✅ ruta corregida

@Component({
  standalone: true,
  selector: 'app-financieros-export-button',
  imports: [CommonModule],
  template: `
    <button type="button" class="btn btn--secondary" (click)="onExport()" [disabled]="busy">
      {{ busy ? 'Exportando…' : 'Exportar XLSX' }}
    </button>
  `,
})
export class FinancierosExportButtonComponent {
  @Input() q?: string;                 // modo selector
  @Input() idDatosPersonal?: number;   // modo detalle
  @Input() afiliadoNombre?: string;    // opcional detalle

  private exporter = inject(FinancierosExporter);
  busy = false;

  async onExport() {
    this.busy = true;
    try {
      if (this.idDatosPersonal && this.idDatosPersonal > 0) {
        await this.exporter.exportXlsx({
          idDatosPersonal: this.idDatosPersonal,
          afiliadoNombre: this.afiliadoNombre,
        });
      } else {
        await this.exporter.exportXlsx({ q: this.q });
      }
    } finally {
      this.busy = false;
    }
  }
}
