import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class ActualizacionExporterService {

  exportarListado(fecha: string, lista: any[]): void {

    if (!lista || lista.length === 0) {
      console.warn('⚠ No hay datos para exportar');
      return;
    }

    const actualizados = lista.filter(x => x.estado === 'ACTUALIZADO');
    const desactualizados = lista.filter(x => x.estado === 'DESACTUALIZADO');

    // ==========================================================
    // 🎯 Mapeo EXACTO de los nombres del backend
    // ==========================================================
    const map = (x: any) => ({
      Documento: x.documento,
      Nombre: x.nombreCompleto,                         // ✔ nombres reales
      FechaActualización: x.fechaActualizacion,
      DíasDesactualizados: x.diasDesactualizado,
      SaldoAportes: x.saldoAportes,
      FechaAperturaCuenta: x.fechaAperturaCuenta,

      // Contacto
      Teléfono: x.telefono,
      Celular1: x.celularUno,
      Celular2: x.celularDos,
      CorreoPersonal: x.correoPersonal,
      Zona: x.nombreZona,
      Subzona: x.nombreSubZona,

      // Permisos Especiales
      RecibeLlamadas: x.recibeLlamadas,
      RecibeMSM: x.recibeMsm,
      RecibeEmails: x.recibeEmails,
      RecibeCartas: x.recibeCartas,
      RecibeRedesSociales: x.recibeRedesSociales
    });

    const hojaActualizados = actualizados.map(map);
    const hojaDesactualizados = desactualizados.map(map);

    // ==========================================================
    // 📘 Crear workbook
    // ==========================================================
    const wb = XLSX.utils.book_new();

    const ws1 = XLSX.utils.json_to_sheet(hojaActualizados);
    XLSX.utils.book_append_sheet(wb, ws1, 'Actualizados');

    const ws2 = XLSX.utils.json_to_sheet(hojaDesactualizados);
    XLSX.utils.book_append_sheet(wb, ws2, 'Desactualizados');

    const filename = `informe_actualizacion_${fecha}.xlsx`;
    XLSX.writeFile(wb, filename);
  }
}
