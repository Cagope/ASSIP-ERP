import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class Regla002ExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarListado(
    lista: any[]
  ): void {

    if (!lista || lista.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        'informe_regla002.xlsx',

      hojas: [

        {
          nombreHoja:
            'Regla_002',

          titulo:
            'INFORME REGLA 002',

          columnas: [
            'Documento',
            'Tipo Documento',
            'Nombre',
            'Edad',
            'Zona',
            'Subzona',
            'Saldo Aportes',
            'Fecha Apertura Cuenta',
            'Teléfono',
            'Celular 1',
            'Celular 2',
            'Correo Personal',
            'Recibe Llamadas',
            'Recibe MSM',
            'Recibe Emails',
            'Recibe Cartas',
            'Recibe Redes Sociales'
          ],

          filas: lista.map(x => [
            x.documento || '',
            x.tipoDocumento || '',
            x.nombreCompleto || '',
            Number(x.edad || 0),
            x.nombreZona || '',
            x.nombreSubZona || '',
            Number(x.saldoAportes || 0),
            x.fechaAperturaCuenta || '',
            x.telefono || '',
            x.celularUno || '',
            x.celularDos || '',
            x.correoPersonal || '',
            this.siNo(x.recibeLlamadas),
            this.siNo(x.recibeMsm),
            this.siNo(x.recibeEmails),
            this.siNo(x.recibeCartas),
            this.siNo(x.recibeRedesSociales)
          ]),

          anchos: [
            18,
            18,
            42,
            10,
            22,
            22,
            18,
            22,
            16,
            16,
            16,
            34,
            18,
            16,
            16,
            16,
            22
          ]
        }

      ]

    });
  }

  private siNo(
    value: any
  ): string {

    return value ? 'SI' : 'NO';
  }
}
