import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { EmpleadoContratoListDTO } from './empleado-contratos.api';
import { EmpleadoListDTO } from '../empleados/empleados.api';
import { PersonaBusquedaDTO } from '../../../shared/personas/personas.api';

@Injectable({ providedIn: 'root' })
export class EmpleadoContratosExporterService {

  exportar(
    items: EmpleadoContratoListDTO[],
    empleadosMap: Map<number, EmpleadoListDTO>,
    personasMap: Map<number, PersonaBusquedaDTO>
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(x => {
      const emp = empleadosMap.get(x.idEmpleado);
      const per = emp?.idDatosPersonal ? personasMap.get(emp.idDatosPersonal) : null;

      return {
        'ID Contrato': x.idContrato,
        'ID Empleado': x.idEmpleado,
        'Documento': per?.documento ?? '',
        'Nombre': per?.nombreCompleto ?? '',
        'Fecha Inicio': x.fechaInicio,
        'Fecha Fin': x.fechaFin ?? '',
        'Periodo': x.periodoPago,
        'Salario Base': x.salarioBase,
        'Activo': x.activo ? 'SI' : 'NO',
      };
    });

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Contratos');
    XLSX.writeFile(wb, 'empleado_contratos.xlsx');
  }
}
