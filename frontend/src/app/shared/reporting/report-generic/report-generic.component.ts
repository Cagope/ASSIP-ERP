import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportingService } from '../reporting.service';
import { ReportMetadata, ReportQueryRequest, ReportResult } from '../reporting.api';

/**
 * 🧾 Componente genérico para probar / explorar vistas del módulo Reporting.
 */
@Component({
  selector: 'app-report-generic',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './report-generic.component.html',
  styleUrls: ['./report-generic.component.scss']
})
export class ReportGenericComponent implements OnInit {
  private readonly service = inject(ReportingService);

  vistas = signal<ReportMetadata[]>([]);
  resultados = signal<Record<string, any>[]>([]);
  vistaSeleccionada = signal<string>('');
  cargando = signal(false);

  async ngOnInit(): Promise<void> {
    try {
      const vistas = await this.service.listarVistas();
      this.vistas.set(vistas);
      console.log('📋 Vistas cargadas:', vistas);
    } catch (error) {
      console.error('❌ Error cargando vistas:', error);
    }
  }

  /** 🔹 Ejecuta el reporte seleccionado */
  async ejecutar(): Promise<void> {
    const vista = this.vistaSeleccionada();
    if (!vista) return;

    this.cargando.set(true);
    try {
      const req: ReportQueryRequest = {
        schema: 'reporting',
        view: vista
      };

      const res: ReportResult = await this.service.ejecutarReporte(req);
      this.resultados.set(res.data ?? []);
      console.log('✅ Resultado recibido:', res);

    } catch (error) {
      console.error('❌ Error ejecutando vista:', error);
    } finally {
      this.cargando.set(false);
    }
  }
}
