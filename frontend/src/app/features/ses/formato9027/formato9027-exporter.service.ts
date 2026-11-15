import { Injectable, inject } from '@angular/core';
import { ExcelExportService } from '../../../shared/services/excel-export.service';
import { Formato9027DTO } from './formato9027.api';

/** Orden oficial inmutable: coincide con la inserción de campos del backend. */
export const CAMPOS_SES_9027: readonly string[] = [
  'CODENTIDAD',
  'FECHACORTE',
  'CONSECUTIVO',
  'TIPOIDEN',
  'NIT',
  'CODIGOCONTABLE',
  'MODIFICACIONESALCREDITO',
  'NROCREDITO',
  'FECHADESEMBOLSOINICIAL',
  'FECHAVENCIMIENTO',
  'MOROSIDAD',
  'TIPOCUOTA',
  'ALTURACUOTA',
  'AMORTIZACION',
  'MODALIDAD',
  'TASAINTERESEFECTIVA',
  'VALORPRESTAMO',
  'VALORCUOTAFIJA',
  'SALDOCAPITAL',
  'SALDOINTERESES',
  'PAGOPORCUENTAASOCIADOS',
  'GARANTIA',
  'FECHAAVALUO',
  'DETERIORO',
  'DETERIOROINTERES',
  'CONTINGENCIA',
  'VALORCUOTAEXTRA',
  'MESESCUOTAEXTRA',
  'FECHAULTIMOPAGO',
  'CLASEGARANTIA',
  'DESTINOCREDITO',
  'CODOFICINA',
  'AMORTICAPITAL',
  'VALORMORA',
  'TIPOVIVIENDA',
  'VIS',
  'RANGOTIPO',
  'ENTIDADREDESCUENTO',
  'MARGENREDESCUENTO',
  'SUBSIDIO',
  'DESEMBOLSO',
  'MONEDA',
  'APORTESSOCIALES',
  'LINEACREDENTIDAD',
  'NUMMODIFICACIONES',
  'ESTADOCREDITO',
  'NITPATRONAL',
  'NOMBREPATRONAL',
  'TARJCREDCUPROT',
  'ENTOTORGARANT',
  'FECHAREESTRUCTURACION',
  'CATEGORIAREESTR',
  'CLASIFICACION',
  'PLAZOCREDITO',
  'TASAINTERESNOMINAL',
  'CATEGORIA',
  'PERIOGRACIA',
  'CALIFANTEMODCE1120',
  'MODCREDCE1120',
  'TIPOMODCE1120',
  'FECHAMODCE1120',
  'MODCREDCE1720'
];

@Injectable({ providedIn: 'root' })
export class Formato9027ExporterService {
  private readonly excel = inject(ExcelExportService);

  /** Excel de revisión: incluye los 62 campos y la totalidad de registros. */
  exportarExcel(registros: Formato9027DTO[], fechaCorte: string): void {
    if (!registros.length) {
      throw new Error('No hay registros para exportar.');
    }

    const numericos = new Set([
      'VALORPRESTAMO', 'VALORCUOTAFIJA', 'SALDOCAPITAL',
      'SALDOINTERESES', 'PAGOPORCUENTAASOCIADOS', 'DETERIORO',
      'DETERIOROINTERES', 'CONTINGENCIA', 'VALORCUOTAEXTRA',
      'VALORMORA', 'APORTESSOCIALES'
    ]);
    const columnas = [...CAMPOS_SES_9027];
    const filas = registros.map(registro => columnas.map(campo => {
      const valor = registro[campo] ?? '';
      if (numericos.has(campo) && /^-?\d+(?:\.\d+)?$/.test(valor.trim())) {
        return Number(valor);
      }
      return valor;
    }));

    this.excel.exportar({
      nombreArchivo: `SES-3402-${fechaCorte.replace(/-/g, '')}-9027.xlsx`,
      hojas: [{
        nombreHoja: 'Cartera individual',
        titulo: 'Formato SES 9027 - Cartera individual',
        subtitulo: 'Exportación para revisión - no reemplaza el CSV regulatorio',
        filtros: [['Fecha de corte', fechaCorte]],
        resumen: [
          ['Créditos reportados', registros.length],
          ['Columnas SES', columnas.length],
          ['Saldo capital', this.sumar(registros, 'SALDOCAPITAL')],
          ['Saldo intereses', this.sumar(registros, 'SALDOINTERESES')]
        ],
        columnas,
        filas,
        anchos: columnas.map(campo => Math.max(15, Math.min(28, campo.length + 3)))
      }]
    });
  }

  private sumar(registros: Formato9027DTO[], campo: string): number {
    return registros.reduce((total, registro) => {
      const valor = registro[campo] ?? '';
      return total + (/^-?\d+(?:\.\d+)?$/.test(valor.trim()) ? Number(valor) : 0);
    }, 0);
  }

  exportarCsv(registros: Formato9027DTO[], fechaCorte: string): void {
    if (!registros.length) {
      throw new Error('No hay registros para exportar.');
    }

    const filas = registros.map((registro) => {
      const valores = CAMPOS_SES_9027.map((campo) => {
        const valor = registro[campo] ?? '';
        if (/[;"\r\n]/.test(valor)) {
          return `"${valor.replace(/"/g, '""')}"`;
        }
        return valor;
      });
      return valores.join(';');
    });

    // El formato regulatorio no incluye encabezados.
    const contenido = filas.join('\r\n') + '\r\n';
    const archivo = new Blob(['\uFEFF', contenido], {
      type: 'text/csv;charset=utf-8'
    });
    const enlace = document.createElement('a');
    const url = URL.createObjectURL(archivo);
    const fecha = fechaCorte.replace(/-/g, '');
    enlace.href = url;
    enlace.download = `SES-3402-${fecha}-9027.csv`;
    document.body.appendChild(enlace);
    enlace.click();
    enlace.remove();
    URL.revokeObjectURL(url);
  }
}
