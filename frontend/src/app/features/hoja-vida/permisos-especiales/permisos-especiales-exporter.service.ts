import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

import { PermisoEspecial } from './permisos-especiales.api';

/**
 * 📦 Servicio de exportación a Excel — Permisos Especiales
 * ------------------------------------------------------------
 * Genera un archivo Excel con los permisos de comunicación
 * otorgados o no por cada persona del esquema Hoja de Vida.
 *
 * Mantiene el formato estándar del ERP ASSIP.
 */
@Injectable({ providedIn: 'root' })
export class PermisosEspecialesExporterService {

  exportarExcel(
    registros: (PermisoEspecial & { documento?: string; nombrePersona?: string })[]
  ): void {
    if (!registros || registros.length === 0) {
      alert('⚠️ No hay registros de Permisos Especiales para exportar.');
      return;
    }

    const dataExport = registros.map(p => ({
      Documento: p.documento ?? '',
      'Nombre Persona': p.nombrePersona ?? '',

      // 🟩 Permisos de contacto
      'Recibe Llamadas Telefónicas': p.recibeLlamadas ? 'Sí' : 'No',
      'Fecha Autorización Llamadas': p.fechaLlamadas
        ? new Date(p.fechaLlamadas).toLocaleDateString()
        : '',

      'Recibe Mensajes SMS': p.recibeMsm ? 'Sí' : 'No',
      'Fecha Autorización SMS': p.fechaSms
        ? new Date(p.fechaSms).toLocaleDateString()
        : '',

      'Recibe Correos Electrónicos': p.recibeEmails ? 'Sí' : 'No',
      'Fecha Autorización Emails': p.fechaEmails
        ? new Date(p.fechaEmails).toLocaleDateString()
        : '',

      'Recibe Correspondencia Física (Cartas)': p.recibeCartas ? 'Sí' : 'No',
      'Fecha Autorización Cartas': p.fechaCartas
        ? new Date(p.fechaCartas).toLocaleDateString()
        : '',

      'Recibe Información por Redes Sociales': p.recibeRedesSociales ? 'Sí' : 'No',
      'Fecha Autorización Redes Sociales': p.fechaRedesSociales
        ? new Date(p.fechaRedesSociales).toLocaleDateString()
        : '',

      // Auditoría
      'Fecha Creación': p.fechaCreacion ? new Date(p.fechaCreacion).toLocaleString() : '',
      'Fecha Edición': p.fechaEdicion ? new Date(p.fechaEdicion).toLocaleString() : '',
    }));

    // 📊 Crear hoja Excel
    const ws = XLSX.utils.json_to_sheet(dataExport);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'PermisosEspeciales');

    // 📏 Ajustar columnas
    (ws as any)['!cols'] = [
      { wch: 14 }, { wch: 28 },
      { wch: 28 }, { wch: 22 },
      { wch: 26 }, { wch: 22 },
      { wch: 30 }, { wch: 22 },
      { wch: 34 }, { wch: 22 },
      { wch: 34 }, { wch: 22 },
      { wch: 20 }, { wch: 20 },
    ];

    const fecha = new Date();
    const sufijo = `${fecha.getFullYear()}${String(fecha.getMonth() + 1).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;
    XLSX.writeFile(wb, `permisos_especiales_${sufijo}.xlsx`);
  }
}
