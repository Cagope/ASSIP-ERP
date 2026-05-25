import { Injectable } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  Sarlaft
} from './sarlaft.api';

import {
  CatalogosApi,
  CodigoNombreDTO
} from '../../../shared/catalogos/catalogos.api';

@Injectable({
  providedIn: 'root'
})
export class SarlaftExporterService {

  constructor(
    private catalogos: CatalogosApi,
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    registros: (Sarlaft & {
      documento?: string;
      nombrePersona?: string;
    })[]
  ): void {

    if (!registros || registros.length === 0) {
      alert('No hay registros SARLAFT para exportar.');
      return;
    }

    forkJoin({
      tiposPeps:
        this.catalogos
          .listarTiposPeps()
          .pipe(catchError(() => of([] as CodigoNombreDTO[]))),

      parentescos:
        this.catalogos
          .listarParentescos()
          .pipe(catchError(() => of([] as CodigoNombreDTO[])))
    }).subscribe({

      next: cat => {

        const mapTiposPeps =
          new Map<string | number, string>(
            (cat.tiposPeps || []).map((t: CodigoNombreDTO) => [
              t.codigo,
              t.nombre
            ])
          );

        const mapParentescos =
          new Map<string | number, string>(
            (cat.parentescos || []).map((p: CodigoNombreDTO) => [
              p.codigo,
              p.nombre
            ])
          );

        this.excelExport.exportar({

          nombreArchivo:
            `sarlaft_${this.fechaArchivo()}.xlsx`,

          hojas: [

            {
              nombreHoja:
                'SARLAFT',

              titulo:
                'SARLAFT',

              columnas: [
                'Documento',
                'Nombre Persona',
                'Exonerado UIAF',
                'Fecha Exoneración',
                'Es PEPS',
                'Tipo PEPS',
                'Observaciones PEPS',
                'Fecha Inicial PEPS',
                'Fecha Final PEPS',
                'Tiene Familiares PEPS',
                'Tipo PEPS Familiar',
                'Parentesco',
                'Cédula Familiar',
                'Nombre Familiar',
                'Transacciones en Moneda Extranjera',
                'Observación Moneda Extranjera',
                'Cuenta en el Extranjero',
                'Tipo Moneda Extranjera',
                'Número de Cuenta',
                'Banco Extranjero',
                'Ciudad Cuenta Extranjero',
                'País Cuenta Extranjero',
                'Fecha Creación',
                'Fecha Edición'
              ],

              filas: registros.map(s => [
                s.documento || '',
                s.nombrePersona || '',
                s.exoneracionUiaf ? 'Sí' : 'No',
                s.fechaExoneracion
                  ? new Date(s.fechaExoneracion).toLocaleDateString()
                  : '',
                s.asociadoPeps ? 'Sí' : 'No',
                mapTiposPeps.get(s.tipoPeps || '') || '',
                s.observacionesPeps || '',
                s.fechaInicialPeps
                  ? new Date(s.fechaInicialPeps).toLocaleDateString()
                  : '',
                s.fechaFinalPeps
                  ? new Date(s.fechaFinalPeps).toLocaleDateString()
                  : '',
                s.familiaPeps ? 'Sí' : 'No',
                mapTiposPeps.get(s.tipoFamiliaPeps || '') || '',
                mapParentescos.get(s.codigoParentesco || '') || '',
                s.cedulaFamiliaPeps || '',
                s.nombreFamiliaPeps || '',
                s.monedaExtranjera ? 'Sí' : 'No',
                s.observacionMonedaExtranjera || '',
                s.cuentaExtranjero ? 'Sí' : 'No',
                s.tipoMonedaExtranjera || '',
                s.numeroCuentaExtranjero || '',
                s.nombreBancoExtranjero || '',
                s.ciudadCuentaExtranjero || '',
                s.paisCuentaExtranjero || '',
                s.fechaCreacion
                  ? new Date(s.fechaCreacion).toLocaleString()
                  : '',
                s.fechaEdicion
                  ? new Date(s.fechaEdicion).toLocaleString()
                  : ''
              ]),

              anchos: [
                14,
                28,
                16,
                18,
                14,
                22,
                30,
                16,
                16,
                18,
                26,
                24,
                28,
                30,
                26,
                26,
                20,
                22,
                22,
                22,
                22,
                20,
                20,
                22
              ]
            }

          ]

        });

      },

      error: err => {
        console.error('Error exportando SARLAFT:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }

    });
  }

  private fechaArchivo(): string {

    const fecha =
      new Date();

    return `${fecha.getFullYear()}${String(fecha.getMonth() + 1).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;
  }
}
