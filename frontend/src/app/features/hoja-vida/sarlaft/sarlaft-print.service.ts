import { Injectable } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Sarlaft } from './sarlaft.api';
import { CatalogosApi, CodigoNombreDTO } from '../../../shared/catalogos/catalogos.api';

/**
 * 🖨️ Servicio de impresión — Listado SARLAFT
 * ------------------------------------------------------------
 * Decodifica catálogos (PEPS y Parentescos) y genera informe
 * en formato Legal horizontal con diseño uniforme ERP ASSIP.
 */
@Injectable({ providedIn: 'root' })
export class SarlaftPrintService {
  constructor(private catalogos: CatalogosApi) {}

  imprimir(
    registros: (Sarlaft & {
      documento?: string;
      nombrePersona?: string;
    })[]
  ): void {
    if (!registros || registros.length === 0) {
      alert('⚠️ No hay registros SARLAFT para imprimir.');
      return;
    }

    forkJoin({
      tiposPeps: this.catalogos.listarTiposPeps().pipe(catchError(() => of([] as CodigoNombreDTO[]))),
      parentescos: this.catalogos.listarParentescos().pipe(catchError(() => of([] as CodigoNombreDTO[]))),
    }).subscribe({
      next: (cat) => {
        const mapTiposPeps = new Map<string | number, string>(
          (cat.tiposPeps ?? []).map((t: CodigoNombreDTO) => [t.codigo, t.nombre])
        );
        const mapParentescos = new Map<string | number, string>(
          (cat.parentescos ?? []).map((p: CodigoNombreDTO) => [p.codigo, p.nombre])
        );

        const registrosDecod = registros.map(r => ({
          ...r,
          nombreTipoPeps: mapTiposPeps.get(r.tipoPeps ?? '') ?? '',
          nombreTipoFamiliaPeps: mapTiposPeps.get(r.tipoFamiliaPeps ?? '') ?? '',
          nombreParentesco: mapParentescos.get(r.codigoParentesco ?? '') ?? ''
        }));

        this.generarHTML(registrosDecod);
      },
      error: (err) => {
        console.error('Error cargando catálogos para impresión:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }
    });
  }

  private generarHTML(
    registros: (Sarlaft & {
      documento?: string;
      nombrePersona?: string;
      nombreTipoPeps?: string;
      nombreTipoFamiliaPeps?: string;
      nombreParentesco?: string;
    })[]
  ): void {
    const filas = registros
      .map(
        (r, i) => `
        <tr>
          <td style="text-align:center;">${i + 1}</td>
          <td>${r.documento ?? ''}</td>
          <td>${r.nombrePersona ?? ''}</td>
          <td>${r.exoneracionUiaf ? 'Sí' : 'No'}</td>
          <td>${r.fechaExoneracion ?? ''}</td>
          <td>${r.asociadoPeps ? 'Sí' : 'No'}</td>
          <td>${r.nombreTipoPeps ?? ''}</td>
          <td>${r.observacionesPeps ?? ''}</td>
          <td>${r.fechaInicialPeps ?? ''}</td>
          <td>${r.fechaFinalPeps ?? ''}</td>
          <td>${r.familiaPeps ? 'Sí' : 'No'}</td>
          <td>${r.nombreTipoFamiliaPeps ?? ''}</td>
          <td>${r.nombreParentesco ?? ''}</td>
          <td>${r.cedulaFamiliaPeps ?? ''}</td>
          <td>${r.nombreFamiliaPeps ?? ''}</td>
          <td>${r.monedaExtranjera ? 'Sí' : 'No'}</td>
          <td>${r.observacionMonedaExtranjera ?? ''}</td>
          <td>${r.cuentaExtranjero ? 'Sí' : 'No'}</td>
          <td>${r.tipoMonedaExtranjera ?? ''}</td>
          <td>${r.numeroCuentaExtranjero ?? ''}</td>
          <td>${r.nombreBancoExtranjero ?? ''}</td>
          <td>${r.ciudadCuentaExtranjero ?? ''}</td>
          <td>${r.paisCuentaExtranjero ?? ''}</td>
        </tr>`
      )
      .join('');

    const total = registros.length;

    const html = `
      <html>
        <head>
          <meta charset="utf-8">
          <title>Listado SARLAFT</title>
          <style>
            @page { size: Legal landscape; margin: 18mm; }
            body {
              font-family: Arial, sans-serif;
              color: #222;
              margin: 0;
              font-size: 12px;
            }
            header {
              text-align: center;
              margin-bottom: 10px;
            }
            img.logo {
              height: 60px;
              display: block;
              margin: 0 auto 5px auto;
            }
            h1 {
              font-size: 18px;
              margin: 4px 0;
            }
            table {
              width: 100%;
              border-collapse: collapse;
              border-spacing: 0;
            }
            th, td {
              padding: 5px 6px;
              border-bottom: 0.5px solid #ccc;
              vertical-align: middle;
            }
            th {
              background-color: #f3f3f3;
              text-align: left;
            }
            tbody tr:nth-child(even) {
              background-color: #fafafa;
            }
            tfoot td {
              text-align: right;
              font-weight: bold;
              padding-top: 8px;
            }
            footer {
              text-align: center;
              font-size: 10px;
              color: #555;
              margin-top: 10px;
              border-top: 1px solid #ccc;
              padding-top: 5px;
            }
          </style>
        </head>
        <body>
          <header>
            <img src="${window.location.origin}/assets/LOGO_EMPRESA.png" class="logo" alt="Logo Empresa">
            <h1>LISTADO SARLAFT</h1>
            <p style="font-size:11px; margin:0;">ERP ASSIP Solidaria y Financiera — Módulo Hoja de Vida</p>
          </header>

          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Documento</th>
                <th>Nombre Persona</th>
                <th>Exonerado UIAF</th>
                <th>Fecha Exoneración</th>
                <th>Es PEPS</th>
                <th>Tipo PEPS</th>
                <th>Observaciones</th>
                <th>Fecha Inicial</th>
                <th>Fecha Final</th>
                <th>Familiares PEPS</th>
                <th>Tipo PEPS Familiar</th>
                <th>Parentesco</th>
                <th>Cédula Familiar</th>
                <th>Nombre Familiar</th>
                <th>Moneda Extranjera</th>
                <th>Observación</th>
                <th>Cuenta Extranjero</th>
                <th>Tipo Moneda</th>
                <th>Número Cuenta</th>
                <th>Banco Extranjero</th>
                <th>Ciudad</th>
                <th>País</th>
              </tr>
            </thead>
            <tbody>${filas}</tbody>
            <tfoot>
              <tr>
                <td colspan="23">Total registros: ${total}</td>
              </tr>
            </tfoot>
          </table>

          <footer>
            Impreso el ${new Date().toLocaleString()}
          </footer>
        </body>
      </html>
    `;

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
}
