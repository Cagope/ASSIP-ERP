import { Injectable } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Ubicacion } from '../../../shared/models/ubicacion.model';
import { CatalogosApi, CodigoNombreDTO, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';
import { GeneralApi, SubZonaDTO } from '../../../shared/general/general.api';

/**
 * 🖨️ Servicio de impresión — Listado de Ubicaciones (Oficio horizontal)
 * Decodifica País, Departamento, Ciudad y Sub Zona sin errores de compilación.
 */
@Injectable({ providedIn: 'root' })
export class UbicacionesPrintService {
  constructor(
    private catalogos: CatalogosApi,
    private general: GeneralApi
  ) {}

  imprimir(
    ubicaciones: (Ubicacion & {
      documento?: string;
      nombrePersona?: string;
    })[]
  ): void {
    if (!ubicaciones || ubicaciones.length === 0) {
      alert('⚠️ No hay ubicaciones para imprimir.');
      return;
    }

    // ✅ Solo IDs válidos
    const idsDepartamentos = [...new Set(ubicaciones.map(u => u.idDepartamento).filter((id): id is number => !!id))];

    const observablesCiudades =
      idsDepartamentos.length > 0
        ? idsDepartamentos.map(id =>
            this.catalogos
              .listarCiudadesPorDepartamento(Number(id)) // 🔹 fuerza número
              .pipe(catchError(() => of([] as Ciudad[])))
          )
        : [of([] as Ciudad[])];

    forkJoin({
      paises: this.catalogos.listarPaises(),
      departamentos: this.catalogos.listarDepartamentos(),
      subZonas: this.general.listarSubZonas(),
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
        const mapSubZonas = new Map<string | number, string>(
          (cat.subZonas ?? []).map((s: SubZonaDTO) => [s.idSubZona, s.nombreSubZona])
        );

        const ubicacionesDecod = ubicaciones.map(u => ({
          ...u,
          nombrePais: mapPaises.get(u.idPais ?? '') ?? '',
          nombreDepartamento: mapDeptos.get(u.idDepartamento ?? 0) ?? '',
          nombreCiudad: mapCiudades.get(u.idCiudad ?? 0) ?? '', // ✅ decodificada
          nombreSubZona: mapSubZonas.get(u.idSubZona ?? '') ?? ''
        }));

        this.generarHTML(ubicacionesDecod);
      },
      error: (err) => {
        console.error('Error cargando catálogos para impresión:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }
    });
  }

  private generarHTML(
    ubicaciones: (Ubicacion & {
      documento?: string;
      nombrePersona?: string;
      nombrePais?: string;
      nombreDepartamento?: string;
      nombreCiudad?: string;
      nombreSubZona?: string;
    })[]
  ): void {
    const filas = ubicaciones
      .map(
        (u, i) => `
        <tr>
          <td style="text-align:center;">${i + 1}</td>
          <td>${u.documento ?? ''}</td>
          <td>${u.nombrePersona ?? ''}</td>
          <td>${u.direccion ?? ''}</td>
          <td>${u.barrio ?? ''}</td>
          <td>${u.telefono ?? ''}</td>
          <td>${u.celularUno ?? ''}</td>
          <td>${u.celularDos ?? ''}</td>
          <td>${u.correo ?? ''}</td>
          <td>${u.nombrePais ?? ''}</td>
          <td>${u.nombreDepartamento ?? ''}</td>
          <td>${u.nombreCiudad ?? ''}</td>
          <td>${u.nombreSubZona ?? ''}</td>
        </tr>
      `
      )
      .join('');

    const total = ubicaciones.length;

    const html = `
      <html>
        <head>
          <meta charset="utf-8">
          <title>Listado de Ubicaciones</title>
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
            <h1>LISTADO DE UBICACIONES</h1>
            <p style="font-size:11px; margin:0;">ERP ASSIP Solidaria y Financiera — Módulo Hoja de Vida</p>
          </header>

          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Documento</th>
                <th>Nombre Persona</th>
                <th>Dirección</th>
                <th>Barrio</th>
                <th>Teléfono</th>
                <th>Celular 1</th>
                <th>Celular 2</th>
                <th>Correo</th>
                <th>País</th>
                <th>Departamento</th>
                <th>Ciudad</th>
                <th>Sub Zona</th>
              </tr>
            </thead>
            <tbody>${filas}</tbody>
            <tfoot>
              <tr>
                <td colspan="13">Total registros: ${total}</td>
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
