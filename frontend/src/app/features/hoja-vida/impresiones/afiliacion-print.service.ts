import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { printDirect } from '../../../shared/print/print-base.template';
import { DatosPersonales } from '../datos-personales/datos-personales.api';

@Injectable({ providedIn: 'root' })
export class AfiliacionPrintService {

  /** 🌐 Endpoint base para los resúmenes de afiliación */
  private readonly baseUrl = `${environment.apiUrl}/hoja-vida/afiliaciones`;

  constructor(private http: HttpClient) {}

  // ============================================================
  // 🖨️ IMPRESIÓN PROFESIONAL — SIN DUPLICAR ENCABEZADO
  // ============================================================
  imprimirDesdeVista(): void {
    const sheet = document.querySelector('.sheet') as HTMLElement;
    if (!sheet) {
      alert('⚠️ No se encontró el formulario (.sheet) para imprimir.');
      return;
    }

    const clone = sheet.cloneNode(true) as HTMLElement;

    const iframe = document.createElement('iframe');
    iframe.style.position = 'fixed';
    iframe.style.width = '0';
    iframe.style.height = '0';
    iframe.style.border = 'none';
    document.body.appendChild(iframe);

    const doc = iframe.contentDocument || iframe.contentWindow?.document;
    if (!doc) return;

    doc.open();
    doc.write(`
      <html>
        <head>
          <meta charset="utf-8" />
          <title>Formulario de Afiliación</title>
          ${document.head.innerHTML}
          <style>
            /* 🔹 Definir tamaño Carta */
            @page {
              size: Letter portrait;
              margin: 10mm 12mm 12mm 12mm;
            }

            @media print {
              html, body {
                width: 215.9mm; /* 8.5 in */
                height: 279.4mm; /* 11 in */
                margin: 0;
                padding: 0;
                background: #fff !important;
              }

              .sheet {
                box-shadow: none !important;
                margin: 0 auto !important;
                page-break-after: always;
                width: 100%;
                max-width: 190mm;
                font-size: 11px;
              }

              .no-print, .actions-top {
                display: none !important;
              }

              img {
                print-color-adjust: exact;
                image-rendering: crisp-edges;
                max-height: 90px;
              }
            }
          </style>
        </head>
        <body></body>
      </html>
    `);
    doc.close();

    doc.body.appendChild(clone);

    setTimeout(() => {
      iframe.contentWindow?.focus();
      iframe.contentWindow?.print();
      setTimeout(() => iframe.remove(), 1000);
    }, 400);
  }

  // ============================================================
  // 🖨️ IMPRESIÓN INDIVIDUAL
  // ============================================================
  imprimirIndividual(persona: DatosPersonales): void {
    const html = this.generarFormulario(persona);
    printDirect(html, { title: 'Formulario de Afiliación' });
  }

  // ============================================================
  // 🖨️ IMPRESIÓN DE LISTADO
  // ============================================================
  imprimirListado(lista: DatosPersonales[]): void {
    const html = `
      <div style="font-family:Arial, sans-serif; padding:20px;">
        <h2 style="text-align:center; margin-bottom:12px;">Listado de Afiliaciones</h2>
        <table border="1" cellspacing="0" cellpadding="4" width="100%">
          <thead style="background:#f9fafb; font-weight:600;">
            <tr>
              <th>Documento</th>
              <th>Nombre completo</th>
              <th>Tipo persona</th>
              <th>Fecha actualización</th>
            </tr>
          </thead>
          <tbody>
            ${lista.map(p => `
              <tr>
                <td>${p.tipoDocumento ?? ''} ${p.documento ?? ''}</td>
                <td>${p.nombres ?? ''} ${p.primerApellido ?? ''} ${p.segundoApellido ?? ''}</td>
                <td>${p.tipoPersona === '1' ? 'Natural' : 'Jurídica'}</td>
                <td>${p.fechaActualizacion ?? ''}</td>
              </tr>`).join('')}
          </tbody>
        </table>
      </div>
    `;
    printDirect(html, { title: 'Listado de Afiliaciones' });
  }

  // ============================================================
  // 📄 FORMULARIO INDIVIDUAL (Versión básica)
  // ============================================================
  private generarFormulario(p: DatosPersonales): string {
    return `
      <div style="font-family:Arial, sans-serif; padding:20px;">
        <h2 style="text-align:center; margin-bottom:14px;">Formulario de Afiliación</h2>
        <table style="width:100%; border-collapse:collapse; font-size:12px;">
          <tbody>
            <tr>
              <td style="width:30%; font-weight:600;">Documento:</td>
              <td>${p.tipoDocumento ?? ''} ${p.documento ?? ''}</td>
            </tr>
            <tr>
              <td style="font-weight:600;">Nombre completo:</td>
              <td>${p.nombres ?? ''} ${p.primerApellido ?? ''} ${p.segundoApellido ?? ''}</td>
            </tr>
            <tr>
              <td style="font-weight:600;">Tipo persona:</td>
              <td>${p.tipoPersona === '1' ? 'Natural' : 'Jurídica'}</td>
            </tr>
            <tr>
              <td style="font-weight:600;">Fecha actualización:</td>
              <td>${p.fechaActualizacion ?? ''}</td>
            </tr>
          </tbody>
        </table>
      </div>
    `;
  }

  // ============================================================
  // 📡 OBTENER RESUMEN DESDE BACKEND
  // ============================================================
  obtenerResumen(id: number) {
    return this.http.get<any>(`${this.baseUrl}/${id}/resumen`);
  }
}
