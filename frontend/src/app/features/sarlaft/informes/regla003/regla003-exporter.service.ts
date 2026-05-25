import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class Regla003ExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarListado(
    registros: any[]
  ): void {

    if (!registros || registros.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        'regla003_forma_prohibida.xlsx',

      hojas: [

        {
          nombreHoja:
            'Regla003',

          titulo:
            'INFORME REGLA 003 - FORMA PROHIBIDA',

          columnas: [
            'Documento',
            'Tipo Documento',
            'Nombre',
            'Edad',
            'Zona',
            'Subzona',
            'Saldo Aportes',
            'Fecha Apertura Cuenta',
            'Forma Ahorro',
            'Edad Permitida',
            'Motivo',
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

          filas: registros.map(r => [
            r.documento || '',
            r.tipoDocumento || '',
            r.nombreCompleto || '',
            Number(r.edad || 0),
            r.nombreZona || '',
            r.nombreSubZona || '',
            Number(r.saldoAportes || 0),
            r.fechaAperturaCuenta || '',
            r.formaAhorro || '',
            Number(r.edadPermitida || 0),
            r.motivo || '',
            r.telefono || '',
            r.celularUno || '',
            r.celularDos || '',
            r.correoPersonal || '',
            this.siNo(r.recibeLlamadas),
            this.siNo(r.recibeMsm),
            this.siNo(r.recibeEmails),
            this.siNo(r.recibeCartas),
            this.siNo(r.recibeRedesSociales)
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
            28,
            18,
            40,
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
