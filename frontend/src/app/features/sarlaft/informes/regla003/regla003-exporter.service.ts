import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({
  providedIn: 'root'
})
export class Regla003ExporterService {

  exportarListado(registros: any[]) {

    const data = registros.map(r => ({
      Documento: r.documento,
      TipoDocumento: r.tipoDocumento,
      Nombre: r.nombreCompleto,
      Edad: r.edad,
      Zona: r.nombreZona,
      Subzona: r.nombreSubZona,

      // 🟦 Datos de aportes
      SaldoAportes: r.saldoAportes,
      FechaAperturaCuenta: r.fechaAperturaCuenta,

      // 🟧 Datos específicos de la regla
      FormaAhorro: r.formaAhorro,
      EdadPermitida: r.edadPermitida,
      Motivo: r.motivo,

      // 🟩 Contacto
      Telefono: r.telefono,
      Celular1: r.celularUno,
      Celular2: r.celularDos,
      CorreoPersonal: r.correoPersonal,

      // 🟪 Permisos especiales
      RecibeLlamadas: r.recibeLlamadas ? 'SI' : 'NO',
      RecibeMSM: r.recibeMsm ? 'SI' : 'NO',
      RecibeEmails: r.recibeEmails ? 'SI' : 'NO',
      RecibeCartas: r.recibeCartas ? 'SI' : 'NO',
      RecibeRedesSociales: r.recibeRedesSociales ? 'SI' : 'NO'
    }));

    const worksheet = XLSX.utils.json_to_sheet(data);
    const workbook = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(workbook, worksheet, 'Regla003');
    XLSX.writeFile(workbook, 'regla003_forma_prohibida.xlsx');
  }
}
