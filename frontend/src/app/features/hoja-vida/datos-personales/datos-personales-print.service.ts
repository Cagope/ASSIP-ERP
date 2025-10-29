import { Injectable } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CatalogosApi, CodigoNombreDTO, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';
import { DatosPersonales } from './datos-personales.api';

/**
 * 🖨️ Servicio de impresión — Listado de Datos Personales
 * ------------------------------------------------------------
 * Decodifica país, departamento, ciudad, género y estado civil.
 * Usa el mismo patrón que UbicacionesPrintService (formato Legal horizontal).
 */
@Injectable({ providedIn: 'root' })
export class DatosPersonalesPrintService {
  constructor(private catalogos: CatalogosApi) {}

  imprimir(personas: DatosPersonales[]): void {
    if (!personas || personas.length === 0) {
      alert('⚠️ No hay registros de datos personales para imprimir.');
      return;
    }

    // ✅ IDs de departamentos de nacimiento (los únicos disponibles en esta entidad)
    const idsDeptos = [
      ...new Set(personas.map(p => p.idDepartamentoNacimiento).filter((id): id is number => !!id))
    ];

    const observablesCiudades =
      idsDeptos.length > 0
        ? idsDeptos.map(id =>
            this.catalogos
              .listarCiudadesPorDepartamento(Number(id))
              .pipe(catchError(() => of([] as Ciudad[])))
          )
        : [of([] as Ciudad[])];

    forkJoin({
      paises: this.catalogos.listarPaises(),
      departamentos: this.catalogos.listarDepartamentos(),
      ciudadesPorDepto: forkJoin(observablesCiudades),
      generos: this.catalogos.listarGeneros(),
      estadosCiviles: this.catalogos.listarEstadosCiviles()
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
        const mapGeneros = new Map<string | number, string>(
          (cat.generos ?? []).map((g: CodigoNombreDTO) => [g.codigo, g.nombre])
        );
        const mapEstados = new Map<string | number, string>(
          (cat.estadosCiviles ?? []).map((e: CodigoNombreDTO) => [e.codigo, e.nombre])
        );

        // 🔹 Construimos el listado con nombres decodificados
        const personasDecod = personas.map(p => ({
          ...p,
          nombrePais: mapPaises.get(p.idPaisNacimiento ?? '') ?? '',
          nombreDepartamento: mapDeptos.get(p.idDepartamentoNacimiento ?? 0) ?? '',
          nombreCiudad: mapCiudades.get(p.idCiudadNacimiento ?? 0) ?? '',
          nombreGenero: mapGeneros.get(p.codigoGenero ?? '') ?? '',
          nombreEstadoCivil: mapEstados.get(p.codigoEstadoCivil ?? '') ?? ''
        }));

        this.generarHTML(personasDecod);
      },
      error: (err) => {
        console.error('Error cargando catálogos para impresión:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }
    });
  }

  // ------------------------------------------------------------
  // 🧾 Genera el documento HTML (formato Legal landscape)
  // ------------------------------------------------------------
  private generarHTML(
    personas: (DatosPersonales & {
      nombreGenero?: string;
      nombreEstadoCivil?: string;
      nombrePais?: string;
      nombreDepartamento?: string;
      nombreCiudad?: string;
    })[]
  ): void {
    const filas = personas
      .map(
        (p, i) => `
        <tr>
          <td style="text-align:center;">${i + 1}</td>
          <td>${p.tipoDocumento ?? ''} ${p.documento ?? ''}</td>
          <td>${p.nombres ?? ''} ${p.primerApellido ?? ''} ${p.segundoApellido ?? ''}</td>
          <td>${p.nombreGenero ?? ''}</td>
          <td>${p.nombreEstadoCivil ?? ''}</td>
          <td>${p.fechaNacimiento ?? ''}</td>
          <td>${p.nombrePais ?? ''}</td>
          <td>${p.nombreDepartamento ?? ''}</td>
          <td>${p.nombreCiudad ?? ''}</td>
        </tr>
      `
      )
      .join('');

    const total = personas.length;

    const html = `
      <html>
        <head>
          <meta charset="utf-8">
          <title>Listado de Datos Personales</title>
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
            <h1>LISTADO DE DATOS PERSONALES</h1>
            <p style="font-size:11px; margin:0;">ERP ASSIP Solidaria y Financiera — Módulo Hoja de Vida</p>
          </header>

          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Documento</th>
                <th>Nombre Completo</th>
                <th>Género</th>
                <th>Estado Civil</th>
                <th>Fecha Nacimiento</th>
                <th>País</th>
                <th>Departamento</th>
                <th>Ciudad</th>
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
