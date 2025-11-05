import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReportingService } from '../../../../shared/reporting/reporting.service';
import { ReportQueryRequest, ReportResult } from '../../../../shared/reporting/reporting.api';
import { ParametrosApi } from '../../../general/parametros/parametros.api';
import { calcularEdad } from '../../../../shared/utils/edad-utils'; // ✅ utilidad compartida

@Component({
  selector: 'app-origen-fondos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './origen-fondos.component.html',
  styleUrls: ['./origen-fondos.component.scss']
})
export class OrigenFondosComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly reporting = inject(ReportingService);
  private readonly parametrosApi = inject(ParametrosApi);

  persona = signal<any | null>(null);
  valorIngreso = signal<number>(0);
  today = new Date();
  edad = signal<number>(0);

  empresa = signal({
    logoUrl: 'assets/logo-web.png',
    razonSocial: 'COOPVALLE',
    documento: '901234567',
    digitoVerificacion: '1',
    nombreComercial: 'COOPVALLE'
  });

  async ngOnInit(): Promise<void> {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;

    try {
      // 🔸 1. Obtener persona desde Reporting
      const req: ReportQueryRequest = {
        schema: 'reporting',
        view: 'vw_hoja_vida_general_total',
        filters: { id_datos_personal: +id }
      };
      const res: ReportResult = await this.reporting.ejecutarReporte(req);

      if (Array.isArray(res.data) && res.data.length > 0) {
        const p = res.data[0];
        this.persona.set(p);

        // 🔸 2. Calcular edad
        if (p['fecha_nacimiento']) {
          const edadCalculada = calcularEdad(p['fecha_nacimiento']);
          this.edad.set(edadCalculada);
          await this.definirValorPorEdad(edadCalculada);
        }
      }
    } catch (err) {
      console.error('❌ Error cargando datos desde reporting:', err);
    }
  }

  /** 🧮 Determina el valor de ingreso según la edad */
  private async definirValorPorEdad(edad: number): Promise<void> {
    try {
      // Parametro 103 → edad límite
      const pLimite = await this.parametrosApi.obtenerPorAgenciaYCodigo(1, 103).toPromise();
      const edadLimite = pLimite?.valorParametro ?? 0;

      let codigoParametro = 101; // por defecto menor de edad
      if (edad > edadLimite) codigoParametro = 102;

      // Traer valor correspondiente
      const pValor = await this.parametrosApi.obtenerPorAgenciaYCodigo(1, codigoParametro).toPromise();
      const valor = pValor?.valorParametro ?? 0;

      this.valorIngreso.set(Number(valor) || 0); // ✅ este es el valor que va al HTML
      console.log(`🧩 Edad ${edad} — límite ${edadLimite} → parámetro ${codigoParametro} = ${valor}`);
    } catch (err) {
      console.warn('⚠️ No se pudo determinar el valor por edad:', err);
      this.valorIngreso.set(0);
    }
  }

  nombreCompleto(): string {
    const p = this.persona();
    if (!p) return '';
    return [p.nombres, p.primer_apellido, p.segundo_apellido].filter(Boolean).join(' ');
  }

  docConDv(): string {
    const p = this.persona();
    if (!p) return '';
    return p.digito_verificacion ? `${p.documento}-${p.digito_verificacion}` : p.documento;
  }

  orDash(v: any): string {
    return v ? v : '—';
  }

  onBack(): void {
    this.router.navigate(['/hoja-vida/impresiones/afiliacion-list']);
  }

onPrint(): void {
  const sheet = document.querySelector('.sheet');
  if (!sheet) return;

  const printWindow = window.open('', '_blank', 'width=900,height=650');
  if (!printWindow) return;

  // Copia estilos principales
  const styles = Array.from(document.styleSheets)
    .map((s: any) => {
      try {
        return Array.from(s.cssRules)
          .map((r: any) => r.cssText)
          .join('');
      } catch {
        return '';
      }
    })
    .join('');

  printWindow.document.write(`
    <html>
      <head>
        <title>Impresión</title>
        <style>${styles}</style>
      </head>
      <body>
        ${sheet.outerHTML}
      </body>
    </html>
  `);

  printWindow.document.close();
  printWindow.focus();

  setTimeout(() => {
    printWindow.print();
    printWindow.close();
  }, 400);
}

  toLogoUrl(url: string): string {
    return url?.startsWith('http') ? url : url || 'assets/logo-web.png';
  }
}
