import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class EmpleadoContratosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: any[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        'empleado_contratos.xlsx',

      hojas: [

        {
          nombreHoja:
            'Contratos',

          titulo:
            'CONTRATOS DE EMPLEADOS',

          columnas: [
            'ID Contrato',
            'ID Empleado',
            'Documento',
            'Nombre Empleado',
            'Fecha Inicio',
            'Fecha Fin',
            'ID Tipo Contrato',
            'Tipo Contrato',
            'ID Sección',
            'Sección',
            'ID Cargo',
            'Cargo',
            'Salario Base',
            'Periodo Pago',
            'Salario Integral',
            'ID EPS',
            'EPS',
            'ID AFP',
            'AFP',
            'ID Cesantías',
            'Cesantías',
            'ID ARL',
            'ARL',
            'ID Caja',
            'Caja Compensación',
            'ID Cuenta Nómina',
            'Cuenta Nómina',
            'ID Forma Ahorro',
            'Forma Ahorro',
            'Fecha Renovación',
            'Clase Riesgo ARL',
            '% ARL',
            'Activo'
          ],

          filas: items.map(x => [
            x.idContrato ?? '',
            x.idEmpleado ?? '',
            x.documentoEmpleado || '',
            x.nombreEmpleado || '',
            x.fechaInicio || '',
            x.fechaFin || '',
            x.idTipoContrato ?? '',
            x.tipoContratoNombre || '',
            x.idSeccion ?? '',
            x.nombreSeccion || '',
            x.idCargo ?? '',
            x.nombreCargo || '',
            Number(x.salarioBase || 0),
            x.periodoPago || '',
            x.salarioIntegral ? 'SI' : 'NO',
            x.idEps ?? '',
            x.nombreEps || '',
            x.idAfp ?? '',
            x.nombreAfp || '',
            x.idCesantias ?? '',
            x.nombreCesantias || '',
            x.idArl ?? '',
            x.nombreArl || '',
            x.idCajaCompensacion ?? '',
            x.nombreCajaCompensacion || '',
            x.idCuentaAhorroNomina ?? '',
            x.cuentaNominaDisplay || '',
            x.idFormaAhorroNomina ?? '',
            x.nombreFormaAhorroNomina || '',
            x.fechaEnvioNotaRenovacion || '',
            x.claseRiesgoArl || '',
            Number(x.porcentajeArl || 0),
            x.activo ? 'SI' : 'NO'
          ]),

          anchos: [
            14,
            14,
            18,
            38,
            16,
            16,
            18,
            26,
            14,
            28,
            14,
            28,
            18,
            16,
            18,
            12,
            30,
            12,
            30,
            16,
            30,
            12,
            30,
            12,
            34,
            18,
            28,
            18,
            28,
            18,
            18,
            12,
            12
          ]
        }

      ]

    });
  }
}
