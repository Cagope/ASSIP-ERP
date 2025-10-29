import { Injectable } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Laboral } from '../../../shared/models/laboral.model';
import { CatalogosApi, CodigoNombreDTO, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';

/**
 * 🖨️ Servicio de impresión — Listado de Información Laboral
 * ------------------------------------------------------------
 * Decodifica País, Departamento y Ciudad, y genera un informe
 * en formato Legal horizontal con diseño uniforme al ERP ASSIP.
 */
@Injectable({ providedIn: 'root' })
export class LaboralesPrintService {
  constructor(private catalogos: CatalogosApi) {}

  imprimir(
    laborales: (Laboral & {
      documento?: string;
      nombrePersona?: string;
    })[]
  ): void {
    if (!laborales || laborales.length === 0) {
      alert('⚠️ No hay registros laborales para imprimir.');
      return;
    }

    const idsDeptos = [...new Set(laborales.map(l => l.idDepartamento).filter((id): id is number => !!id))];
    const observablesCiudades =
      idsDeptos.length > 0
        ? idsDeptos.map(id =>
            this.catalogos
              .listarCiudadesPorDepartamento(id)
              .pipe(catchError(() => of([] as Ciudad[])))
          )
        : [of([] as Ciudad[])];

    forkJoin({
      paises: this.catalogos.listarPaises(),
      departamentos: this.catalogos.listarDepartamentos(),
      ciudadesPorDepto: forkJoin(observablesCiudades)
    }).subscribe({
      next: (cat) => {
        const todasCiudades = (cat.ciudadesPorDepto ?? []).flat();

        const mapPaises = new Map<string | number, string>(
          (cat.paises ?? []).map((p: CodigoNombreDTO) => [p.codigo, p.nombre])
        );
        const mapDeptos = new Map<number, string>(
          (cat.departamentos ?? []).map((d: Departamento) => [d.idDepartamento, d.nombreDepartamento])
        );
        const mapCiudades = new Map<number, string>(
          todasCiudades.map((c: Ciudad) => [c.idCiudad, c.nombreCiudad])
        );

        const laboralesDecod = laborales.map(l => ({
          ...l,
          nombrePais: mapPaises.get(l.idPais ?? '') ?? '',
          nombreDepartamento: mapDeptos.get(l.idDepartamento ?? 0) ?? '',
          nombreCiudad: mapCiudades.get(l.idCiudad ?? 0) ?? ''
        }));

        this.generarHTML(laboralesDecod);
      },
      error: (err) => {
        console.error('Error cargando catálogos para impresión:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }
    });
  }

  private generarHTML(
    laborales: (Laboral & {
      documento?: string;
      nombrePersona?: string;
      nombrePais?: string;
      nombreDepartamento?: string;
      nombreCiudad?: string;
    })[]
  ): void {
    const filas = laborales
      .map(
        (l, i) => `
        <tr>
          <td style="text-align:center;">${i + 1}</td>
          <td>${l.documento ?? ''}</td>
          <td>${l.nombrePersona ?? ''}</td>
          <td>${l.nombreEmpresa ?? ''}</td>
          <td>${l.direccion ?? ''}</td>
          <td>${l.telefonoEmpresa ?? ''}</td>
          <td>${l.celularEmpresa ?? ''}</td>
          <td>${l.correoEmpresa ?? ''}</td>
          <td>${l.nombrePais ?? ''}</td>
          <td>${l.nombreDepartamento ?? ''}</td>
          <td>${l.nombreCiudad ?? ''}</td>
          <td>${l.codigoTipoEmpresa ?? ''}</td>
          <td>${l.codigoTipoContrato ?? ''}</td>
          <td>${l.codigoJornada ?? ''}</td>
          <td>${l.fechaVinculacion ?? ''}</td>
        </tr>
      `
      )
      .join('');

    const total = laborales.length;

    const html = `
      <html>
        <head>
          <meta charset="utf-8">
          <title>Listado de Información Laboral</title>
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
            <h1>LISTADO DE INFORMACIÓN LABORAL</h1>
            <p style="font-size:11px; margin:0;">ERP ASSIP Solidaria y Financiera — Módulo Hoja de Vida</p>
          </header>

          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Documento</th>
                <th>Nombre Persona</th>
                <th>Empresa / Actividad</th>
                <th>Dirección</th>
                <th>Teléfono</th>
                <th>Celular</th>
                <th>Correo</th>
                <th>País</th>
                <th>Departamento</th>
                <th>Ciudad</th>
                <th>Tipo Empresa</th>
                <th>Tipo Contrato</th>
                <th>Jornada</th>
                <th>Fecha Vinculación</th>
              </tr>
            </thead>
            <tbody>${filas}</tbody>
            <tfoot>
              <tr><td colspan="15">Total registros: ${total}</td></tr>
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
