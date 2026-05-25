import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class ActualizacionExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarListado(
    fecha: string,
    lista: any[]
  ): void {

    if (!lista || lista.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const actualizados =
      lista.filter(x => x.estado === 'ACTUALIZADO');

    const desactualizados =
      lista.filter(x => x.estado === 'DESACTUALIZADO');

    const hojas: ExcelSheetOptions[] = [
      this.crearHoja(
        'Actualizados',
        actualizados,
        fecha
      ),
      this.crearHoja(
        'Desactualizados',
        desactualizados,
        fecha
      )
    ];

    this.excelExport.exportar({

      nombreArchivo:
        `informe_actualizacion_${fecha}.xlsx`,

      hojas

    });
  }

  private crearHoja(
    nombreHoja: string,
    items: any[],
    fecha: string
  ): ExcelSheetOptions {

    return {

      nombreHoja,

      titulo:
        `INFORME ACTUALIZACIÓN - ${nombreHoja.toUpperCase()}`,

      filtros: [
        ['Fecha corte', fecha || '']
      ],

      columnas: [
        'Documento',
        'Nombre',
        'Fecha actualización',
        'Días desactualizados',
        'Saldo aportes',
        'Fecha apertura cuenta',
        'Teléfono',
        'Celular 1',
        'Celular 2',
        'Correo personal',
        'Zona',
        'Subzona',
        'Recibe llamadas',
        'Recibe MSM',
        'Recibe emails',
        'Recibe cartas',
        'Recibe redes sociales'
      ],

      filas: (items || []).map(x => [
        x.documento || '',
        x.nombreCompleto || '',
        x.fechaActualizacion || '',
        Number(x.diasDesactualizado || 0),
        Number(x.saldoAportes || 0),
        x.fechaAperturaCuenta || '',
        x.telefono || '',
        x.celularUno || '',
        x.celularDos || '',
        x.correoPersonal || '',
        x.nombreZona || '',
        x.nombreSubZona || '',
        this.siNo(x.recibeLlamadas),
        this.siNo(x.recibeMsm),
        this.siNo(x.recibeEmails),
        this.siNo(x.recibeCartas),
        this.siNo(x.recibeRedesSociales)
      ]),

      anchos: [
        18,
        42,
        20,
        20,
        18,
        22,
        16,
        16,
        16,
        34,
        22,
        22,
        18,
        16,
        16,
        16,
        22
      ]
    };
  }

  private siNo(
    value: any
  ): string {

    return value ? 'SI' : 'NO';
  }
}
