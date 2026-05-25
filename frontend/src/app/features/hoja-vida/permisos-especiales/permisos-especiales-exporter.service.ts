import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  PermisoEspecial
} from './permisos-especiales.api';

@Injectable({
  providedIn: 'root'
})
export class PermisosEspecialesExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    registros: (PermisoEspecial & {
      documento?: string;
      nombrePersona?: string;
    })[]
  ): void {

    if (!registros || registros.length === 0) {
      alert('No hay registros de Permisos Especiales para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `permisos_especiales_${this.fechaArchivo()}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'PermisosEspeciales',

          titulo:
            'PERMISOS ESPECIALES',

          columnas: [
            'Documento',
            'Nombre Persona',
            'Recibe Llamadas Telefónicas',
            'Fecha Autorización Llamadas',
            'Recibe Mensajes SMS',
            'Fecha Autorización SMS',
            'Recibe Correos Electrónicos',
            'Fecha Autorización Emails',
            'Recibe Correspondencia Física (Cartas)',
            'Fecha Autorización Cartas',
            'Recibe Información por Redes Sociales',
            'Fecha Autorización Redes Sociales',
            'Fecha Creación',
            'Fecha Edición'
          ],

          filas: registros.map(p => [
            p.documento || '',
            p.nombrePersona || '',
            p.recibeLlamadas ? 'Sí' : 'No',
            p.fechaLlamadas
              ? new Date(p.fechaLlamadas).toLocaleDateString()
              : '',
            p.recibeMsm ? 'Sí' : 'No',
            p.fechaSms
              ? new Date(p.fechaSms).toLocaleDateString()
              : '',
            p.recibeEmails ? 'Sí' : 'No',
            p.fechaEmails
              ? new Date(p.fechaEmails).toLocaleDateString()
              : '',
            p.recibeCartas ? 'Sí' : 'No',
            p.fechaCartas
              ? new Date(p.fechaCartas).toLocaleDateString()
              : '',
            p.recibeRedesSociales ? 'Sí' : 'No',
            p.fechaRedesSociales
              ? new Date(p.fechaRedesSociales).toLocaleDateString()
              : '',
            p.fechaCreacion
              ? new Date(p.fechaCreacion).toLocaleString()
              : '',
            p.fechaEdicion
              ? new Date(p.fechaEdicion).toLocaleString()
              : ''
          ]),

          anchos: [
            14,
            28,
            28,
            22,
            26,
            22,
            30,
            22,
            34,
            22,
            34,
            22,
            20,
            20
          ]
        }

      ]

    });
  }

  private fechaArchivo(): string {

    const fecha =
      new Date();

    return `${fecha.getFullYear()}${String(fecha.getMonth() + 1).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;
  }
}
