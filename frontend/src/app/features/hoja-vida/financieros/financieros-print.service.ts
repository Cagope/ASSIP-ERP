import { Injectable } from '@angular/core';
import { Financiero } from '../../../shared/models/financiero.model';

/**
 * 🖨️ Servicio de impresión — Listado de Información Financiera
 * ------------------------------------------------------------
 * Genera un informe en formato Legal horizontal con los datos
 * financieros de los afiliados, incluyendo totales calculados.
 */
@Injectable({ providedIn: 'root' })
export class FinancierosPrintService {

  /**
   * 🖨️ Imprime la información financiera en formato tabular.
   */
  imprimir(financieros: (Financiero & {
    documento?: string;
    nombrePersona?: string;
  })[]): void {
    if (!financieros || financieros.length === 0) {
      alert('⚠️ No hay registros financieros para imprimir.');
      return;
    }

    // ==========================================================
    // 🧮 Filas de la tabla
    // ==========================================================
    const filas = financieros
      .map((f, i) => {
        const totalIngresos =
          (f.valorSalario ?? 0) +
          (f.valorPension ?? 0) +
          (f.ingresosArriendo ?? 0) +
          (f.ingresosComisiones ?? 0) +
          (f.otrosIngresos ?? 0);

        const totalEgresos =
          (f.egresosFamiliares ?? 0) +
          (f.egresosArriendo ?? 0) +
          (f.egresosCredito ?? 0) +
          (f.otrosEgresos ?? 0);

        const patrimonioNeto = (f.totalActivos ?? 0) - (f.totalPasivos ?? 0);

        return `
          <tr>
            <td style="text-align:center;">${i + 1}</td>
            <td>${f.documento ?? ''}</td>
            <td>${f.nombrePersona ?? ''}</td>
            <td style="text-align:right;">${this.format(f.valorSalario)}</td>
            <td style="text-align:right;">${this.format(f.valorPension)}</td>
            <td style="text-align:right;">${this.format(f.ingresosArriendo)}</td>
            <td style="text-align:right;">${this.format(f.ingresosComisiones)}</td>
            <td style="text-align:right;">${this.format(f.otrosIngresos)}</td>
            <td style="text-align:right;">${this.format(totalIngresos)}</td>
            <td style="text-align:right;">${this.format(totalEgresos)}</td>
            <td style="text-align:right;">${this.format(f.totalActivos)}</td>
            <td style="text-align:right;">${this.format(f.totalPasivos)}</td>
            <td style="text-align:right;">${this.format(patrimonioNeto)}</td>
            <td>${f.relacionFinanciera ?? ''}</td>
          </tr>`;
      })
      .join('');

    const total = financieros.length;

    // ==========================================================
    // 🧾 Plantilla HTML
    // ==========================================================
    const html = `
      <html>
        <head>
          <meta charset="utf-8">
          <title>Listado de Información Financiera</title>
          <style>
            @page { size: Legal landscape; margin: 18mm; }
            body { font-family: Arial, sans-serif; color: #222; margin: 0; font-size: 12px; }
            header { text-align: center; margin-bottom: 10px; }
            img.logo { height: 60px; display: block; margin: 0 auto 5px auto; }
            h1 { font-size: 18px; margin: 4px 0; }
            table { width: 100%; border-collapse: collapse; border-spacing: 0; }
            th, td { padding: 5px 6px; border-bottom: 0.5px solid #ccc; vertical-align: middle; }
            th { background-color: #f3f3f3; text-align: left; }
            tbody tr:nth-child(even) { background-color: #fafafa; }
            td:nth-child(4),
            td:nth-child(5),
            td:nth-child(6),
            td:nth-child(7),
            td:nth-child(8),
            td:nth-child(9),
            td:nth-child(10),
            td:nth-child(11),
            td:nth-child(12),
            td:nth-child(13) { text-align: right; }
            tfoot td { text-align: right; font-weight: bold; padding-top: 8px; }
            footer { text-align: center; font-size: 10px; color: #555; margin-top: 10px; border-top: 1px solid #ccc; padding-top: 5px; }
          </style>
        </head>
        <body>
          <header>
            <img src="${window.location.origin}/assets/LOGO_EMPRESA.png" class="logo" alt="Logo Empresa">
            <h1>LISTADO DE INFORMACIÓN FINANCIERA</h1>
            <p style="font-size:11px; margin:0;">ERP ASSIP Solidaria y Financiera — Módulo Hoja de Vida</p>
          </header>

          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Documento</th>
                <th>Nombre Persona</th>
                <th>Salario</th>
                <th>Pensión</th>
                <th>Arriendo</th>
                <th>Comisiones</th>
                <th>Otros Ingresos</th>
                <th>Total Ingresos</th>
                <th>Total Egresos</th>
                <th>Total Activos</th>
                <th>Total Pasivos</th>
                <th>Patrimonio Neto</th>
                <th>Relación Financiera</th>
              </tr>
            </thead>
            <tbody>${filas}</tbody>
            <tfoot>
              <tr><td colspan="14">Total registros: ${total}</td></tr>
            </tfoot>
          </table>

          <footer>Impreso el ${new Date().toLocaleString()}</footer>
        </body>
      </html>
    `;

    // ==========================================================
    // 🖨️ Abrir ventana y ejecutar impresión
    // ==========================================================
    const ventana = window.open('', '_blank', 'width=1200,height=800');
    if (!ventana) {
      alert('⚠️ Bloqueador de ventanas emergentes activo.');
      return;
    }
    ventana.document.open();
    ventana.document.write(html);
    ventana.document.close();
    ventana.onload = () => ventana.print();
  }

  /**
   * 🔢 Formatea valores numéricos con separador de miles.
   */
  private format(value?: number): string {
    return (value ?? 0).toLocaleString('es-CO', { minimumFractionDigits: 0 });
  }
}
