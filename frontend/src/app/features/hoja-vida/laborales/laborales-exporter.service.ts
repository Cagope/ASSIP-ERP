import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { Laboral } from '../../../shared/models/laboral.model';
import { CatalogosApi, CodigoNombreDTO, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';

/**
 * 📦 Servicio de exportación a Excel — Listado de Información Laboral
 * Decodifica País, Departamento y Ciudad antes de exportar.
 * Mantiene el formato uniforme del ERP ASSIP.
 */
@Injectable({ providedIn: 'root' })
export class LaboralesExporterService {
  constructor(private catalogos: CatalogosApi) {}

  exportarExcel(
    laborales: (Laboral & { documento?: string; nombrePersona?: string })[]
  ): void {
    if (!laborales || laborales.length === 0) {
      alert('⚠️ No hay registros laborales para exportar.');
      return;
    }

    const idsDepartamentos = [
      ...new Set(laborales.map(l => l.idDepartamento).filter((id): id is number => !!id))
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
          Documento: l.documento ?? '',
          'Nombre Persona': l.nombrePersona ?? '',
          'Nombre Empresa / Actividad': l.nombreEmpresa ?? '',
          Dirección: l.direccion ?? '',
          'Teléfono Empresa': l.telefonoEmpresa ?? '',
          'Celular Empresa': l.celularEmpresa ?? '',
          'Correo Empresa': l.correoEmpresa ?? '',
          País: mapPaises.get(l.idPais ?? '') ?? '',
          Departamento: mapDeptos.get(l.idDepartamento ?? 0) ?? '',
          Ciudad: mapCiudades.get(l.idCiudad ?? 0) ?? '',
          'Tipo Empresa': l.codigoTipoEmpresa ?? '',
          'Tipo Contrato': l.codigoTipoContrato ?? '',
          Jornada: l.codigoJornada ?? '',
          'Fecha Vinculación': l.fechaVinculacion ? new Date(l.fechaVinculacion).toLocaleDateString() : '',
          'Fecha Creación': l.fechaCreacion ? new Date(l.fechaCreacion).toLocaleString() : '',
          'Fecha Edición': l.fechaEdicion ? new Date(l.fechaEdicion).toLocaleString() : ''
        }));

        const ws = XLSX.utils.json_to_sheet(laboralesDecod);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Laborales');

        (ws as any)['!cols'] = [
          { wch: 14 }, { wch: 28 }, { wch: 38 }, { wch: 28 },
          { wch: 14 }, { wch: 14 }, { wch: 28 }, { wch: 18 },
          { wch: 22 }, { wch: 24 }, { wch: 16 }, { wch: 16 },
          { wch: 14 }, { wch: 18 }, { wch: 20 }, { wch: 20 }
        ];

        const fecha = new Date();
        const sufijo = `${fecha.getFullYear()}${String(fecha.getMonth() + 1).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;
        XLSX.writeFile(wb, `laborales_${sufijo}.xlsx`);
      },
      error: (err) => {
        console.error('❌ Error exportando información laboral:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }
    });
  }
}
