import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { PlanCuenta } from './plan-cuentas.api';

@Injectable({ providedIn: 'root' })
export class PlanCuentasExporterService {

  exportar(items: PlanCuenta[]): void {

    const data = items.map(i => ({
      Agencia: i.idAgencia,
      Código: i.codigoCuenta,
      Nombre: i.nombre,
      Naturaleza: i.naturaleza === 'D' ? 'Débito' : 'Crédito',
      Nivel: i.nivel,
      Operable: i.operable ? 'SI' : 'NO',
      'Control Entrada/Salida': i.controlEntradaSalida ? 'SI' : 'NO',
      'Tipo Especial': i.tipoEspecial ?? ''
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Plan de Cuentas');
    XLSX.writeFile(wb, 'plan_cuentas.xlsx');
  }
}
