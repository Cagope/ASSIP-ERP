import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AfiliacionPrintService } from '../afiliacion-print.service';
import { ReportingService } from '../../../../shared/reporting/reporting.service';
import { ReportQueryRequest, ReportResult } from '../../../../shared/reporting/reporting.api';

/**
 * 🧾 Componente — Formulario de Afiliación (Impresión)
 * --------------------------------------------------------------------
 * Recupera un registro desde la vista reporting.vw_hoja_vida_general_total_reciente
 * usando el motor de Reporting. El resultado se muestra directamente
 * en la plantilla HTML, sin remapeos ni transformaciones.
 */
@Component({
  selector: 'app-afiliacion-formulario',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './afiliacion-formulario.component.html',
  styleUrls: ['./afiliacion-formulario.component.scss']
})
export class AfiliacionFormularioComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly printService = inject(AfiliacionPrintService);
  private readonly reporting = inject(ReportingService);

  persona = signal<any | null>(null);
  today = new Date();

  async ngOnInit(): Promise<void> {
    console.log('🚀 AfiliacionFormularioComponent iniciado');

    // 1️⃣ Si viene con datos desde la lista, los usa directamente
    const personaData = history.state?.persona;
    if (personaData && personaData.idDatosPersonal) {
      this.persona.set(personaData);
      console.log('📄 Persona inicial (desde state):', personaData);
    }

    // 2️⃣ Recupera el ID de la ruta
    const id = this.route.snapshot.paramMap.get('id');
    console.log('🆔 ID en URL:', id);
    if (!id) return;

    try {
      // 3️⃣ Solicitud al motor de reporting (vista estable y actualizada)
      const req: ReportQueryRequest = {
        schema: 'reporting',
        view: 'vw_hoja_vida_general_total_reciente',
        filters: { id_datos_personal: +id }
      };

      const res: ReportResult = await this.reporting.ejecutarReporte(req);
      console.log('📦 Resultado recibido desde reporting:', res);

      if (Array.isArray(res.data) && res.data.length > 0) {
        const raw = res.data[0];
        this.persona.set(raw);
        console.log('✅ Registro cargado correctamente:', raw);
      } else {
        console.warn('⚠️ No se encontró el registro en reporting.');
      }
    } catch (error) {
      console.error('❌ Error al obtener datos desde Reporting:', error);
    }
  }

  onBack(): void {
    this.router.navigate(['/hoja-vida/impresiones/afiliacion-list']);
  }

  onPrint(): void {
    this.printService.imprimirDesdeVista();
  }

  orDash(v: any): string {
    return v === null || v === undefined || v === '' ? '—' : v;
  }

  yn(v: any): string {
    return v === true || v === '1'
      ? 'Sí'
      : v === false || v === '0'
      ? 'No'
      : '—';
  }
}
