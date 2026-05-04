import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class Regla002ExporterService {

  exportarListado(lista: any[]): void {

    if (!lista || lista.length === 0) {
      console.warn('⚠ No hay datos para exportar');
      return;
    }

    // ==========================================================
    // 🎯 Mapeo EXACTO de los nombres del backend
    // ==========================================================
    const map = (x: any) => ({
      Documento: x.documento,
      TipoDocumento: x.tipoDocumento,
      Nombre: x.nombreCompleto,
      Edad: x.edad,
      Zona: x.nombreZona,
      Subzona: x.nombreSubZona,
      SaldoAportes: x.saldoAportes,
      FechaAperturaCuenta: x.fechaAperturaCuenta,

      // 📞 Contacto
      Telefono: x.telefono,
      Celular1: x.celularUno,
      Celular2: x.celularDos,
      CorreoPersonal: x.correoPersonal,

      // 🟣 Permisos Especiales
      RecibeLlamadas: x.recibeLlamadas,
      RecibeMSM: x.recibeMsm,
      RecibeEmails: x.recibeEmails,
      RecibeCartas: x.recibeCartas,
      RecibeRedesSociales: x.recibeRedesSociales
    });

    const rows = lista.map(map);

    // ==========================================================
    // 📘 Crear workbook
    // ==========================================================
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(rows);
    XLSX.utils.book_append_sheet(wb, ws, 'Regla_002');

    const filename = `informe_regla002.xlsx`;
    XLSX.writeFile(wb, filename);
  }
}
