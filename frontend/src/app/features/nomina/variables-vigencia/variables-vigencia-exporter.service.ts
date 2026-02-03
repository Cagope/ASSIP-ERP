import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { VariablesVigenciaListDTO, VariablesVigenciaFormDTO } from './variables-vigencia.api';

type ExportDTO = VariablesVigenciaListDTO & Partial<VariablesVigenciaFormDTO>;

@Injectable({ providedIn: 'root' })
export class VariablesVigenciaExporterService {

  exportar(items: ExportDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(x => ({
      'ID': x.idVariable ?? '',
      'Fecha inicial': x.fechaInicial ?? '',
      'Fecha final': x.fechaFinal ?? '',

      'SMMLV': (x as any).smmlv ?? '',
      'Aux transporte': (x as any).auxTransporte ?? '',

      '% Salud empleado': (x as any).porcSaludEmpleado ?? '',
      '% Salud empleador': (x as any).porcSaludEmpleador ?? '',

      '% Pensión empleado': (x as any).porcPensionEmpleado ?? '',
      '% Pensión empleador': (x as any).porcPensionEmpleador ?? '',

      '% Caja compensación': (x as any).porcCajaCompensacion ?? '',
      '% SENA': (x as any).porcSena ?? '',
      '% ICBF': (x as any).porcIcbf ?? '',

      '% Prov. prima': (x as any).porProvisionPrima ?? '',
      '% Prov. vacaciones': (x as any).porProvisionVacaciones ?? '',
      '% Prov. cesantías': (x as any).porProvisionCesantias ?? '',
      '% Prov. interés cesantías': (x as any).porProvisionInteresCesantias ?? '',

      'Tope IBC mínimo (SMMLV)': (x as any).topeIbcMinSmmlv ?? '',
      'Tope IBC máximo (SMMLV)': (x as any).topeIbcMaxSmmlv ?? '',

      'Exonerado salud': ((x as any).exoneradoSalud ? 'SI' : 'NO'),
      'Exonerado parafiscales': ((x as any).exoneradoParafiscales ? 'SI' : 'NO'),

      'Activo': (x.activo ? 'SI' : 'NO'),
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'VariablesVigencia');
    XLSX.writeFile(wb, 'variables_vigencia.xlsx');
  }
}
