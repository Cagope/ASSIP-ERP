import { Injectable } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ReferenciaPersonal } from '../../../shared/models/referencia-personal.model';
import { CatalogosApi, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';

/**
 * 🖨️ Servicio de impresión — Listado de Referencias Personales (Carta horizontal)
 * Decodifica Departamento y Ciudad antes de imprimir.
 */
@Injectable({ providedIn: 'root' })
export class ReferenciasPersonalesPrintService {
  constructor(private catalogos: CatalogosApi) {}

  imprimir(
    referencias: (ReferenciaPersonal & {
      documento?: string;
      nombrePersona?: string;
    })[]
  ): void {
    if (!referencias || referencias.length === 0) {
      alert('⚠️ No hay referencias personales para imprimir.');
      return;
    }

    // ✅ Solo IDs válidos
    const idsDepartamentos = [...new Set(referencias.map(r => r.idDepartamento).filter((id): id is number => !!id))];

    const observablesCiudades =
      idsDepartamentos.length > 0
        ? idsDepartamentos.map(id =>
            this.catalogos
              .listarCiudadesPorDepartamento(Number(id))
              .pipe(catchError(() => of([] as Ciudad[])))
          )
        : [of([] as Ciudad[])];

    forkJoin({
      departamentos: this.catalogos.listarDepartamentos(),
      ciudadesPorDepto: forkJoin(observablesCiudades)
    }).subscribe({
      next: (cat) => {
        const todasCiudades = (cat.ciudadesPorDepto ?? []).flat();

        const mapDeptos = new Map<number, string>(
          (cat.departamentos ?? []).map((d: Departamento) => [d.idDepartamento, d.nombreDepartamento])
        );
        const mapCiudades = new Map<number, string>(
          todasCiudades.map((c: Ciudad) => [c.idCiudad, c.nombreCiudad])
        );

        const referenciasDecod = referencias.map(r => ({
          ...r,
          nombreDepartamento: mapDeptos.get(r.idDepartamento ?? 0) ?? '',
          nombreCiudad: mapCiudades.get(r.idCiudad ?? 0) ?? ''
        }));

        this.generarHTML(referenciasDecod);
      },
      error: (err) => {
        console.error('Error cargando catálogos para impresión:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }
    });
  }

  private generarHTML(
    referencias: (ReferenciaPersonal & {
      documento?: string;
      nombrePersona?: string;
      nombreDepartamento?: string;
      nombreCiudad?: string;
    })[]
  ): void {
    const filas = referencias
      .map(
        (r, i) => `
        <tr>
          <td style="text-align:center;">${i + 1}</td>
          <td>${r.documento ?? ''}</td>
          <td>${r.nombrePersona ?? ''}</td>
          <td>${r.nombreReferenciaPersonal ?? ''}</td>
          <td>${r.direccionReferenciaPersonal ?? ''}</td>
          <td>${r.nombreDepartamento ?? ''}</td>
          <td>${r.nombreCiudad ?? ''}</td>
          <td>${r.telefonoReferenciaPersonal ?? ''}</td>
          <td>${r.celularReferenciaPersonal ?? ''}</td>
        </tr>
      `
      )
      .join('');

    const total = referencias.length;

    const html = `
      <html>
        <head>
          <meta charset="utf-8">
          <title>Listado de Referencias Personales</title>
          <style>
            @page { size: Letter landscape; margin: 18mm; }
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
            <h1>LISTADO DE REFERENCIAS PERSONALES</h1>
            <p style="font-size:11px; margin:0;">ERP ASSIP Solidaria y Financiera — Módulo Hoja de Vida</p>
          </header>

          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Documento</th>
                <th>Nombre Persona</th>
                <th>Nombre Referencia</th>
                <th>Dirección</th>
                <th>Departamento</th>
                <th>Ciudad</th>
                <th>Teléfono</th>
                <th>Celular</th>
              </tr>
            </thead>
            <tbody>${filas}</tbody>
            <tfoot>
              <tr>
                <td colspan="9">Total registros: ${total}</td>
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
