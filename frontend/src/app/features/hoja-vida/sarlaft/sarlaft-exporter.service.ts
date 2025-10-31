import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { Sarlaft } from './sarlaft.api';
import { CatalogosApi, CodigoNombreDTO } from '../../../shared/catalogos/catalogos.api';

/**
 * 📦 Servicio de exportación a Excel — SARLAFT
 * Decodifica catálogos de PEPS y Parentescos.
 * Mantiene el formato uniforme del ERP ASSIP.
 */
@Injectable({ providedIn: 'root' })
export class SarlaftExporterService {
  constructor(private catalogos: CatalogosApi) {}

  exportarExcel(
    registros: (Sarlaft & { documento?: string; nombrePersona?: string })[]
  ): void {
    if (!registros || registros.length === 0) {
      alert('⚠️ No hay registros SARLAFT para exportar.');
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

        const sarlaftDecod = registros.map(s => ({
          Documento: s.documento ?? '',
          'Nombre Persona': s.nombrePersona ?? '',

          // 🟩 Exoneración UIAF
          'Exonerado UIAF': s.exoneracionUiaf ? 'Sí' : 'No',
          'Fecha Exoneración': s.fechaExoneracion ? new Date(s.fechaExoneracion).toLocaleDateString() : '',

          // 🟦 PEPS
          'Es PEPS': s.asociadoPeps ? 'Sí' : 'No',
          'Tipo PEPS': mapTiposPeps.get(s.tipoPeps ?? '') ?? '',
          'Observaciones PEPS': s.observacionesPeps ?? '',
          'Fecha Inicial PEPS': s.fechaInicialPeps ? new Date(s.fechaInicialPeps).toLocaleDateString() : '',
          'Fecha Final PEPS': s.fechaFinalPeps ? new Date(s.fechaFinalPeps).toLocaleDateString() : '',

          // 🟧 Familiares PEPS
          'Tiene Familiares PEPS': s.familiaPeps ? 'Sí' : 'No',
          'Tipo PEPS Familiar': mapTiposPeps.get(s.tipoFamiliaPeps ?? '') ?? '',
          'Parentesco': mapParentescos.get(s.codigoParentesco ?? '') ?? '',
          'Cédula Familiar': s.cedulaFamiliaPeps ?? '',
          'Nombre Familiar': s.nombreFamiliaPeps ?? '',

          // 🟨 Moneda Extranjera
          'Transacciones en Moneda Extranjera': s.monedaExtranjera ? 'Sí' : 'No',
          'Observación Moneda Extranjera': s.observacionMonedaExtranjera ?? '',

          // 🟫 Cuenta en el Extranjero
          'Cuenta en el Extranjero': s.cuentaExtranjero ? 'Sí' : 'No',
          'Tipo Moneda Extranjera': s.tipoMonedaExtranjera ?? '',
          'Número de Cuenta': s.numeroCuentaExtranjero ?? '',
          'Banco Extranjero': s.nombreBancoExtranjero ?? '',
          'Ciudad Cuenta Extranjero': s.ciudadCuentaExtranjero ?? '',
          'País Cuenta Extranjero': s.paisCuentaExtranjero ?? '',

          'Fecha Creación': s.fechaCreacion ? new Date(s.fechaCreacion).toLocaleString() : '',
          'Fecha Edición': s.fechaEdicion ? new Date(s.fechaEdicion).toLocaleString() : ''
        }));

        // 📊 Generar hoja Excel
        const ws = XLSX.utils.json_to_sheet(sarlaftDecod);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'SARLAFT');

        // 📏 Ajustar columnas
        (ws as any)['!cols'] = [
          { wch: 14 }, { wch: 28 }, { wch: 16 }, { wch: 18 },
          { wch: 14 }, { wch: 22 }, { wch: 30 }, { wch: 16 },
          { wch: 16 }, { wch: 18 }, { wch: 26 }, { wch: 24 },
          { wch: 28 }, { wch: 30 }, { wch: 26 }, { wch: 26 },
          { wch: 20 }, { wch: 22 }, { wch: 22 }, { wch: 22 },
          { wch: 22 }, { wch: 20 }, { wch: 20 }, { wch: 22 },
        ];

        const fecha = new Date();
        const sufijo = `${fecha.getFullYear()}${String(fecha.getMonth() + 1).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;
        XLSX.writeFile(wb, `sarlaft_${sufijo}.xlsx`);
      },
      error: (err) => {
        console.error('❌ Error exportando SARLAFT:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }
    });
  }
}
