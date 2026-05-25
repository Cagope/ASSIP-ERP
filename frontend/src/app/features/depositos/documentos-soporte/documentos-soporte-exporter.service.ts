import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  DocumentosSoporteCuenta,
  DocumentosSoporteHistorico
} from './documentos-soporte.api';

@Injectable({
  providedIn: 'root'
})
export class DocumentosSoporteExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    cuenta: DocumentosSoporteCuenta | null,
    historico: DocumentosSoporteHistorico[]
  ): void {

    if (!historico || historico.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const cuentaCodigo =
      cuenta?.codigoCuenta || 'cuenta';

    this.excelExport.exportar({

      nombreArchivo:
        `documentos_soporte_${cuentaCodigo}.xlsx`,

      hojas: [

        {
          nombreHoja: 'Documentos Soporte',

          titulo: 'DOCUMENTOS SOPORTE',

          filtros: [
            ['Cuenta', cuenta?.codigoCuenta || ''],
            ['Documento', cuenta?.documento || ''],
            ['Asociado', cuenta?.nombreCompleto || ''],
            ['Forma ahorro', cuenta?.nombreForma || '']
          ],

          columnas: [
            'Tipo soporte',
            'Número inicial',
            'Número final',
            'Cantidad',
            'Estado',
            'Fecha entrega',
            'Fecha estado',
            'Usuario creación',
            'Fecha creación'
          ],

          filas: historico.map(x => [

            x.tipoDocumentoSoporte || '',

            x.numeroInicial || '',

            x.numeroFinal || '',

            (
              Number(x.numeroFinal || 0)
              - Number(x.numeroInicial || 0)
              + 1
            ),

            x.estadoDocumento || '',

            x.fechaEntrega || '',

            x.fechaEstado || '',

            x.usuarioCreacion || '',

            x.fechaCreacion || ''

          ]),

          anchos: [
            18,
            18,
            18,
            12,
            14,
            18,
            18,
            22,
            22
          ]
        }

      ]

    });
  }
}
