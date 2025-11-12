import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AfiliacionPrintService } from '../afiliacion-print.service';
import { ReportingService } from '../../../../shared/reporting/reporting.service';
import { ReportQueryRequest, ReportResult } from '../../../../shared/reporting/reporting.api';

/**
 * 📋 Componente — Actualización de Datos Persona Naturales (Impresión)
 * --------------------------------------------------------------------
 * Recupera información desde la vista `reporting.vw_hoja_vida_general_total`
 * usando el motor de Reporting. Se usa para mostrar la actualización anual
 * de datos de los afiliados, con formato para impresión en hoja tamaño carta.
 */
@Component({
  selector: 'app-actualizacion-datos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './actualizacion-datos.component.html',
  styleUrls: ['./actualizacion-datos.component.scss']
})
export class ActualizacionDatosComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly printService = inject(AfiliacionPrintService);
  private readonly reporting = inject(ReportingService);

  /** Señal reactiva con la información completa del afiliado */
  persona = signal<any | null>(null);
  today = new Date();

  /** 🏢 Datos fijos de la empresa (cambiar si aplica) */
  empresa = signal({
    logoUrl: 'assets/logo-web.png',
    razonSocial: 'COOPVALLE',
    documento: '901234567',
    digitoVerificacion: '1',
    nombreComercial: 'COOPVALLE'
  });

  /** 🧩 Señales adicionales requeridas por la plantilla HTML */
  ubicacion = signal<any | null>(null);
  laboral = signal<any | null>(null);
  refFinanciera = signal<any | null>(null);
  sarlaft = signal<any | null>(null);

  async ngOnInit(): Promise<void> {
    console.log('🚀 ActualizacionDatosComponent iniciado');

    // Si viene con datos desde la lista, los usa directamente
    const personaData = history.state?.persona;
    if (personaData) {
      this.persona.set(personaData);
      console.log('📄 Persona recibida vía state:', personaData);
    }

    // Recuperar ID desde la URL
    const id = this.route.snapshot.paramMap.get('id');
    console.log('🆔 ID en URL:', id);
    if (!id) return;

    try {
      const req: ReportQueryRequest = {
        schema: 'reporting',
        view: 'vw_hoja_vida_general_total',
        filters: { id_datos_personal: +id }
      };

      const res: ReportResult = await this.reporting.ejecutarReporte(req);
      console.log('📦 Resultado desde reporting:', res);

      if (Array.isArray(res.data) && res.data.length > 0) {
        const raw = res.data[0];
        this.persona.set(raw);
        console.log('✅ Registro cargado:', raw);
      } else {
        console.warn('⚠️ No se encontró registro con el ID indicado.');
      }
    } catch (error) {
      console.error('❌ Error al obtener datos desde Reporting:', error);
    }
  }

  /** 🔙 Volver al listado anterior */
  onBack(): void {
    this.router.navigate(['/hoja-vida/impresiones/actualizacion-list']);
  }

  /** 🖨️ Ejecuta la impresión */
  onPrint(): void {
    this.printService.imprimirDesdeVista();
  }

  /** 🔹 Formatea valores vacíos */
  orDash(v: any): string {
      return v === null || v === undefined || v === '' ? '—' : v;
  }

  /** 🔹 Traduce booleanos a “Sí / No / —” */
  yn(v: any): string {
    return v === true || v === '1'
      ? 'Sí'
      : v === false || v === '0'
      ? 'No'
      : '—';
  }

  /** 🖼️ Resuelve URL del logo (local o remota) */
  toLogoUrl(url: string): string {
    return url?.startsWith('http') ? url : url || 'assets/logo-web.png';
  }
}
