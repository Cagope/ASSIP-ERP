import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class EmpleadoContratosExporterService {

  exportar(items: any[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const data = items.map(x => ({

      // =========================
      // CONTRATO
      // =========================

      'ID Contrato': x.idContrato,

      // =========================
      // EMPLEADO
      // =========================

      'ID Empleado': x.idEmpleado,
      'Documento': x.documentoEmpleado ?? '',
      'Nombre Empleado': x.nombreEmpleado ?? '',

      // =========================
      // FECHAS
      // =========================

      'Fecha Inicio': x.fechaInicio ?? '',
      'Fecha Fin': x.fechaFin ?? '',

      // =========================
      // TIPO CONTRATO
      // =========================

      'ID Tipo Contrato': x.idTipoContrato ?? '',
      'Tipo Contrato': x.tipoContratoNombre ?? '',

      // =========================
      // SECCIÓN / CARGO
      // =========================

      'ID Sección': x.idSeccion ?? '',
      'Sección': x.nombreSeccion ?? '',

      'ID Cargo': x.idCargo ?? '',
      'Cargo': x.nombreCargo ?? '',

      // =========================
      // VALORES
      // =========================

      'Salario Base': x.salarioBase ?? 0,
      'Periodo Pago': x.periodoPago ?? '',
      'Salario Integral': x.salarioIntegral ? 'SI' : 'NO',

      // =========================
      // AFILIACIONES
      // =========================

      'ID EPS': x.idEps ?? '',
      'EPS': x.nombreEps ?? '',

      'ID AFP': x.idAfp ?? '',
      'AFP': x.nombreAfp ?? '',

      'ID Cesantías': x.idCesantias ?? '',
      'Cesantías': x.nombreCesantias ?? '',

      'ID ARL': x.idArl ?? '',
      'ARL': x.nombreArl ?? '',

      'ID Caja': x.idCajaCompensacion ?? '',
      'Caja Compensación': x.nombreCajaCompensacion ?? '',

      // =========================
      // CUENTA NÓMINA
      // =========================

      'ID Cuenta Nómina': x.idCuentaAhorroNomina ?? '',
      'Cuenta Nómina': x.cuentaNominaDisplay ?? '',

      'ID Forma Ahorro': x.idFormaAhorroNomina ?? '',
      'Forma Ahorro': x.nombreFormaAhorroNomina ?? '',

      // =========================
      // ARL / RENOVACIÓN
      // =========================

      'Fecha Renovación': x.fechaEnvioNotaRenovacion ?? '',
      'Clase Riesgo ARL': x.claseRiesgoArl ?? '',
      '% ARL': x.porcentajeArl ?? 0,

      // =========================
      // ESTADO
      // =========================

      'Activo': x.activo ? 'SI' : 'NO',

    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'Contratos');
    XLSX.writeFile(wb, 'empleado_contratos.xlsx');
  }
}
