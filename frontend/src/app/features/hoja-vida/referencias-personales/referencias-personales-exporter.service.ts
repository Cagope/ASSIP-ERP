import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ReferenciaPersonal } from '../../../shared/models/referencia-personal.model';
import { CatalogosApi, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';

/**
 * 📦 Servicio de exportación a Excel — Referencias Personales
 * Decodifica Departamento y Ciudad antes de exportar.
 */
@Injectable({ providedIn: 'root' })
export class ReferenciasPersonalesExporterService {
  constructor(private catalogos: CatalogosApi) {}

  exportarExcel(
    referencias: (ReferenciaPersonal & { documento?: string; nombrePersona?: string })[]
  ): void {
    if (!referencias || referencias.length === 0) {
      alert('⚠️ No hay referencias personales para exportar.');
      return;
    }

    // 🔹 Reunir IDs de departamentos usados
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

        // 🗺️ Mapas de referencia
        const mapDeptos = new Map<number, string>(
          (cat.departamentos ?? []).map((d: Departamento) => [d.idDepartamento, d.nombreDepartamento])
        );
        const mapCiudades = new Map<number, string>(
          todasCiudades.map((c: Ciudad) => [c.idCiudad, c.nombreCiudad])
        );

        // 🧩 Decodificar campos
        const referenciasDecod = referencias.map(r => ({
          Documento: r.documento ?? '',
          'Nombre Persona': r.nombrePersona ?? '',
          'Nombre Referencia': r.nombreReferenciaPersonal ?? '',
          Dirección: r.direccionReferenciaPersonal ?? '',
          Departamento: mapDeptos.get(r.idDepartamento ?? 0) ?? '',
          Ciudad: mapCiudades.get(r.idCiudad ?? 0) ?? '',
          Teléfono: r.telefonoReferenciaPersonal ?? '',
          Celular: r.celularReferenciaPersonal ?? '',
          'Fecha Creación': r.fechaCreacion ? new Date(r.fechaCreacion).toLocaleString() : '',
          'Fecha Edición': r.fechaEdicion ? new Date(r.fechaEdicion).toLocaleString() : ''
        }));

        // 📊 Generar hoja y libro
        const ws = XLSX.utils.json_to_sheet(referenciasDecod);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'ReferenciasPersonales');

        // 🧾 Ajuste de anchos de columna
        (ws as any)['!cols'] = [
          { wch: 14 }, // Documento
          { wch: 30 }, // Nombre Persona
          { wch: 30 }, // Nombre Referencia
          { wch: 40 }, // Dirección
          { wch: 22 }, // Departamento
          { wch: 22 }, // Ciudad
          { wch: 14 }, // Teléfono
          { wch: 14 }, // Celular
          { wch: 22 }, // Fecha Creación
          { wch: 22 }  // Fecha Edición
        ];

        // 🗓️ Nombre del archivo
        const fecha = new Date();
        const sufijo = `${fecha.getFullYear()}${String(fecha.getMonth() + 1).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;

        XLSX.writeFile(wb, `referencias_personales_${sufijo}.xlsx`);
      },
      error: (err) => {
        console.error('Error exportando referencias personales:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }
    });
  }
}
