import { Injectable } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DatosFamiliar } from '../../../shared/models/datos-familiar.model';
import { CatalogosApi, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';

/**
 * 🖨️ Servicio de impresión — Listado de Datos Familiares
 * ------------------------------------------------------------
 * Decodifica Departamento y Ciudad, y genera un informe
 * en formato Legal horizontal con diseño uniforme al ERP ASSIP.
 */
@Injectable({ providedIn: 'root' })
export class DatosFamiliaresPrintService {
  constructor(private catalogos: CatalogosApi) {}

  imprimir(
    familiares: (DatosFamiliar & {
      documentoPersona?: string;
      nombrePersona?: string;
    })[]
  ): void {
    if (!familiares || familiares.length === 0) {
      alert('⚠️ No hay registros familiares para imprimir.');
      return;
    }

    const idsDeptos = [...new Set(familiares.map(f => f.idDepartamento).filter((id): id is number => !!id))];
    const observablesCiudades =
      idsDeptos.length > 0
        ? idsDeptos.map(id =>
            this.catalogos
              .listarCiudadesPorDepartamento(id)
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

        const familiaresDecod = familiares.map(f => ({
          ...f,
          nombreDepartamento: mapDeptos.get(f.idDepartamento ?? 0) ?? '',
          nombreCiudad: mapCiudades.get(f.idCiudad ?? 0) ?? ''
        }));

        this.generarHTML(familiaresDecod);
      },
      error: (err) => {
        console.error('Error cargando catálogos para impresión:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }
    });
  }

  private generarHTML(
    familiares: (DatosFamiliar & {
      documentoPersona?: string;
      nombrePersona?: string;
      nombreDepartamento?: string;
      nombreCiudad?: string;
    })[]
  ): void {
    const filas = familiares
      .map(
        (f, i) => `
        <tr>
          <td style="text-align:center;">${i + 1}</td>
          <td>${f.documentoPersona ?? ''}</td>
          <td>${f.nombrePersona ?? ''}</td>
          <td>${f.nombreDatosFamiliar ?? ''}</td>
          <td>${f.codigoParentesco ?? ''}</td>
          <td>${f.documentoDatosFamiliar ?? ''}</td>
          <td>${f.direccionDatosFamiliar ?? ''}</td>
          <td>${f.telefonoDatosFamiliar ?? ''}</td>
          <td>${f.celularDatosFamiliar ?? ''}</td>
          <td>${f.nombreDepartamento ?? ''}</td>
          <td>${f.nombreCiudad ?? ''}</td>
          <td>${f.ingresosDatosFamiliar?.toLocaleString() ?? '0'}</td>
          <td>${f.egresosDatosFamiliar?.toLocaleString() ?? '0'}</td>
          <td>${f.referenciaFamiliar ? 'Sí' : 'No'}</td>
        </tr>
      `
      )
      .join('');

    const total = familiares.length;

    const html = `
      <html>
        <head>
          <meta charset="utf-8">
          <title>Listado de Datos Familiares</title>
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
            tfoot td { text-align: right; font-weight: bold; padding-top: 8px; }
            footer { text-align: center; font-size: 10px; color: #555; margin-top: 10px; border-top: 1px solid #ccc; padding-top: 5px; }
          </style>
        </head>
        <body>
          <header>
            <img src="${window.location.origin}/assets/LOGO_EMPRESA.png" class="logo" alt="Logo Empresa">
            <h1>LISTADO DE DATOS FAMILIARES</h1>
            <p style="font-size:11px; margin:0;">ERP ASSIP Solidaria y Financiera — Módulo Hoja de Vida</p>
          </header>

          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Documento Persona</th>
                <th>Nombre Persona</th>
                <th>Nombre Familiar</th>
                <th>Parentesco</th>
                <th>Documento Familiar</th>
                <th>Dirección</th>
                <th>Teléfono</th>
                <th>Celular</th>
                <th>Departamento</th>
                <th>Ciudad</th>
                <th>Ingresos</th>
                <th>Egresos</th>
                <th>Referencia</th>
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
