import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { Agencia } from './agencia.api';

/**
 * 📦 Servicio para exportar el listado de agencias a Excel
 * Usa los datos ya cargados en el componente (sin pedir al backend).
 */
@Injectable({ providedIn: 'root' })
export class AgenciasExporterService {
  exportarExcel(agencias: Agencia[]): void {
    if (!agencias || agencias.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    // 🔹 Armar filas legibles
    const rows = agencias.map(a => ({
      'Código': a.codigoAgencia ?? '',
      'Nombre de Agencia': a.nombreAgencia ?? '',
      'Sigla': a.siglaAgencia ?? '',
      'Dirección': a.direccionAgencia ?? '',
      'Departamento': (a as any).nombreDepartamento ?? a.idDepartamento ?? '',
      'Ciudad': (a as any).nombreCiudad ?? a.idCiudad ?? '',
      'Correo': a.correoAgencia ?? '',
      'Celular': a.celularAgencia ?? '',
      'Teléfono': a.telefonoAgencia ?? '',
      'Fecha Creación': a.fechaCreacion ? new Date(a.fechaCreacion).toLocaleString() : '',
      'Fecha Edición': a.fechaEdicion ? new Date(a.fechaEdicion).toLocaleString() : '',
    }));

    // 🔹 Crear hoja y libro Excel
    const ws = XLSX.utils.json_to_sheet(rows);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Agencias');

    // 🔹 Ajuste de ancho de columnas
    (ws as any)['!cols'] = [
      { wch: 10 },
      { wch: 32 },
      { wch: 10 },
      { wch: 40 },
      { wch: 24 },
      { wch: 24 },
      { wch: 28 },
      { wch: 14 },
      { wch: 14 },
      { wch: 22 },
      { wch: 22 },
    ];

    // 🔹 Guardar archivo
    const d = new Date();
    const fecha = `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}`;
    XLSX.writeFile(wb, `agencias_${fecha}.xlsx`);
  }
}
