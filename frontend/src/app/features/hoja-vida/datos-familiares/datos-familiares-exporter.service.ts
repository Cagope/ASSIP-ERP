import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { DatosFamiliar } from '../../../shared/models/datos-familiar.model';
import { CatalogosApi, CodigoNombreDTO, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';

/**
 * 📦 Servicio de exportación a Excel — Listado de Datos Familiares
 * ---------------------------------------------------------------
 * Decodifica Departamento y Ciudad antes de exportar.
 * Incluye documento y nombre del titular de hoja de vida.
 */
@Injectable({ providedIn: 'root' })
export class DatosFamiliaresExporterService {
  constructor(private catalogos: CatalogosApi) {}

  exportarExcel(
    familiares: (DatosFamiliar & { documento?: string; nombrePersona?: string })[]
  ): void {
    if (!familiares || familiares.length === 0) {
      alert('⚠️ No hay registros familiares para exportar.');
      return;
    }

    // 🔹 Reunir IDs de departamentos únicos
    const idsDepartamentos = [
      ...new Set(familiares.map(f => f.idDepartamento).filter((id): id is number => !!id))
    ];

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

        // 🧩 Construir registros decodificados
        const familiaresDecod = familiares.map(f => ({
          'Documento Titular': f.documento ?? '',
          'Nombre Titular': f.nombrePersona ?? '',
          'Nombre Familiar': f.nombreDatosFamiliar ?? '',
          Parentesco: f.codigoParentesco ?? '',
          'Documento Familiar': f.documentoDatosFamiliar ?? '',
          Dirección: f.direccionDatosFamiliar ?? '',
          Teléfono: f.telefonoDatosFamiliar ?? '',
          Celular: f.celularDatosFamiliar ?? '',
          Departamento: mapDeptos.get(f.idDepartamento ?? 0) ?? '',
          Ciudad: mapCiudades.get(f.idCiudad ?? 0) ?? '',
          'Ingresos Mensuales': f.ingresosDatosFamiliar ?? 0,
          'Egresos Mensuales': f.egresosDatosFamiliar ?? 0,
          'Referencia Familiar': f.referenciaFamiliar ? 'Sí' : 'No',
          'Fecha Creación': f.fechaCreacion ? new Date(f.fechaCreacion).toLocaleString() : '',
          'Fecha Edición': f.fechaEdicion ? new Date(f.fechaEdicion).toLocaleString() : ''
        }));

        // 📊 Crear hoja y libro Excel
        const ws = XLSX.utils.json_to_sheet(familiaresDecod);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'DatosFamiliares');

        // 🧾 Ajustar anchos de columna
        (ws as any)['!cols'] = [
          { wch: 16 }, // Documento Titular
          { wch: 30 }, // Nombre Titular
          { wch: 30 }, // Nombre Familiar
          { wch: 14 }, // Parentesco
          { wch: 18 }, // Documento Familiar
          { wch: 32 }, // Dirección
          { wch: 12 }, // Teléfono
          { wch: 12 }, // Celular
          { wch: 22 }, // Departamento
          { wch: 22 }, // Ciudad
          { wch: 18 }, // Ingresos
          { wch: 18 }, // Egresos
          { wch: 18 }, // Referencia Familiar
          { wch: 22 }, // Fecha Creación
          { wch: 22 }  // Fecha Edición
        ];

        // 🗓️ Nombre del archivo
        const fecha = new Date();
        const sufijo = `${fecha.getFullYear()}${String(fecha.getMonth() + 1).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;
        XLSX.writeFile(wb, `datos_familiares_${sufijo}.xlsx`);
      },
      error: (err) => {
        console.error('❌ Error exportando información familiar:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }
    });
  }
}
