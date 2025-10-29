import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { Ubicacion } from '../../../shared/models/ubicacion.model';
import { CatalogosApi, CodigoNombreDTO, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';
import { GeneralApi, SubZonaDTO } from '../../../shared/general/general.api';

/**
 * 📦 Servicio de exportación a Excel — Listado de Ubicaciones
 * Decodifica País, Departamento, Ciudad y Sub Zona antes de exportar.
 */
@Injectable({ providedIn: 'root' })
export class UbicacionesExporterService {
  constructor(
    private catalogos: CatalogosApi,
    private general: GeneralApi
  ) {}

  exportarExcel(
    ubicaciones: (Ubicacion & { documento?: string; nombrePersona?: string })[]
  ): void {
    if (!ubicaciones || ubicaciones.length === 0) {
      alert('⚠️ No hay ubicaciones para exportar.');
      return;
    }

    // 🔹 Reunir IDs de departamentos usados
    const idsDepartamentos = [...new Set(ubicaciones.map(u => u.idDepartamento).filter((id): id is number => !!id))];

    const observablesCiudades =
      idsDepartamentos.length > 0
        ? idsDepartamentos.map(id =>
            this.catalogos
              .listarCiudadesPorDepartamento(Number(id))
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

        // 🗺️ Mapas de referencia
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

        // 🧩 Decodificar campos
        const ubicacionesDecod = ubicaciones.map(u => ({
          Documento: u.documento ?? '',
          'Nombre Persona': u.nombrePersona ?? '',
          Dirección: u.direccion ?? '',
          Barrio: u.barrio ?? '',
          Teléfono: u.telefono ?? '',
          'Celular 1': u.celularUno ?? '',
          'Celular 2': u.celularDos ?? '',
          Correo: u.correo ?? '',
          País: mapPaises.get(u.idPais ?? '') ?? '',
          Departamento: mapDeptos.get(u.idDepartamento ?? 0) ?? '',
          Ciudad: mapCiudades.get(u.idCiudad ?? 0) ?? '',
          'Sub Zona': mapSubZonas.get(u.idSubZona ?? '') ?? '',
          'Fecha Creación': u.fechaCreacion ? new Date(u.fechaCreacion).toLocaleString() : '',
          'Fecha Edición': u.fechaEdicion ? new Date(u.fechaEdicion).toLocaleString() : ''
        }));

        // 📊 Generar hoja y libro
        const ws = XLSX.utils.json_to_sheet(ubicacionesDecod);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Ubicaciones');

        // 🧾 Ajuste de anchos de columna
        (ws as any)['!cols'] = [
          { wch: 14 }, // Documento
          { wch: 30 }, // Nombre
          { wch: 40 }, // Dirección
          { wch: 22 }, // Barrio
          { wch: 12 }, // Teléfono
          { wch: 14 }, // Celular 1
          { wch: 14 }, // Celular 2
          { wch: 30 }, // Correo
          { wch: 20 }, // País
          { wch: 22 }, // Departamento
          { wch: 24 }, // Ciudad
          { wch: 22 }, // Sub Zona
          { wch: 22 }, // Fecha Creación
          { wch: 22 }  // Fecha Edición
        ];

        // 🗓️ Nombre del archivo
        const fecha = new Date();
        const sufijo = `${fecha.getFullYear()}${String(fecha.getMonth() + 1).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;

        XLSX.writeFile(wb, `ubicaciones_${sufijo}.xlsx`);
      },
      error: (err) => {
        console.error('Error exportando ubicaciones:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }
    });
  }
}
