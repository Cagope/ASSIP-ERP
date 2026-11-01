
import { Injectable, inject } from '@angular/core';
import {
  ExcelExportService,
  ExcelFila,
  ExcelSheetOptions
} from '../../../../../shared/services/excel-export.service';
import {
  AuxiliarCuenta,
  AuxiliarCuentaMovimiento,
  AuxiliarCuentasResponse
} from './auxiliar-cuentas.models';

/** Información legible de la agencia que se incluirá en el libro Excel. */
export interface AuxiliarCuentasAgenciaExportacion {
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Genera el auxiliar contable en modalidad básica o completa.
 * La auditoría se incluye únicamente cuando existe autorización.
 */
@Injectable({ providedIn: 'root' })
export class AuxiliarCuentasExporterService {
  private readonly excel = inject(ExcelExportService);

  /**
   * Exporta el informe con las hojas permitidas.
   * La modalidad básica nunca incorpora la hoja de auditoría.
   */
  exportar(
    informe: AuxiliarCuentasResponse,
    agencia: AuxiliarCuentasAgenciaExportacion,
    usuarioSolicitante: string,
    incluirAuditoria: boolean
  ): void {
    if (!informe.cuentas.length) {
      return;
    }

    const generado = new Date();
    const etiquetaAgencia =
      `${agencia.codigoAgencia} - ${agencia.nombreAgencia}`;

    const filtros: ExcelFila[] = [
      ['Agencia', etiquetaAgencia],
      ['Cuenta inicial', informe.cuentaInicial],
      ['Cuenta final', informe.cuentaFinal],
      ['Fecha inicial', informe.fechaInicial],
      ['Fecha final', informe.fechaFinal]
    ];

    const hojas: ExcelSheetOptions[] = [
      this.construirAuxiliar(informe, filtros)
    ];

    if (incluirAuditoria) {
      hojas.push(this.construirAuditoria(informe, filtros));
    }

    hojas.push(
      this.construirControl(
        informe,
        etiquetaAgencia,
        usuarioSolicitante,
        generado,
        incluirAuditoria
      )
    );

    const modalidad = incluirAuditoria ? 'Completo' : 'Basico';

    this.excel.exportar({
      nombreArchivo:
        `Auxiliar_Cuentas_${modalidad}_` +
        `${this.segmentoArchivo(agencia.codigoAgencia)}_` +
        `${this.marcaTiempo(generado)}.xlsx`,
      hojas
    });
  }

  /**
   * Construye los saldos y movimientos de cada cuenta.
   * Incluye el concepto del movimiento en ambas modalidades.
   */
  private construirAuxiliar(
    informe: AuxiliarCuentasResponse,
    filtros: ExcelFila[]
  ): ExcelSheetOptions {
    const filas: ExcelFila[] = [];

    for (const cuenta of informe.cuentas) {
      filas.push([
        cuenta.codigoCuenta,
        cuenta.nombreCuenta,
        cuenta.naturalezaCuenta,
        'SALDO INICIAL',
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        cuenta.saldoInicial
      ]);

      for (const movimiento of cuenta.movimientos) {
        filas.push([
          cuenta.codigoCuenta,
          cuenta.nombreCuenta,
          cuenta.naturalezaCuenta,
          'MOVIMIENTO',
          movimiento.fechaAuxiliar,
          movimiento.tipoComprobante,
          movimiento.numeroComprobante,
          movimiento.detalleMovimiento,
          movimiento.documentoTercero,
          movimiento.nombreTercero,
          movimiento.valorDebito,
          movimiento.valorCredito,
          movimiento.saldoAcumulado
        ]);
      }

      filas.push([
        cuenta.codigoCuenta,
        cuenta.nombreCuenta,
        cuenta.naturalezaCuenta,
        'TOTAL PERÍODO',
        null,
        null,
        null,
        null,
        null,
        null,
        cuenta.totalDebitos,
        cuenta.totalCreditos,
        null
      ]);

      filas.push([
        cuenta.codigoCuenta,
        cuenta.nombreCuenta,
        cuenta.naturalezaCuenta,
        'SALDO FINAL',
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        cuenta.saldoFinal
      ]);
    }

    return {
      nombreHoja: 'Auxiliar por cuentas',
      titulo: 'AUXILIAR CONTABLE POR CUENTAS',
      filtros,
      columnas: [
        'Código cuenta',
        'Nombre cuenta',
        'Naturaleza',
        'Tipo registro',
        'Fecha movimiento',
        'Tipo comprobante',
        'Número comprobante',
        'Concepto movimiento',
        'Documento tercero',
        'Nombre tercero',
        'Débito',
        'Crédito',
        'Saldo'
      ],
      filas,
      anchos: [
        18, 44, 14, 20, 19, 19, 22,
        55, 22, 38, 19, 19, 19
      ]
    };
  }

  /** Construye la hoja de auditoría exclusiva de la modalidad completa. */
  private construirAuditoria(
    informe: AuxiliarCuentasResponse,
    filtros: ExcelFila[]
  ): ExcelSheetOptions {
    const filas: ExcelFila[] = [];

    for (const cuenta of informe.cuentas) {
      for (const movimiento of cuenta.movimientos) {
        filas.push(this.filaAuditoria(cuenta, movimiento));
      }
    }

    return {
      nombreHoja: 'Detalle de auditoria',
      titulo: 'DETALLE DE AUDITORÍA DE MOVIMIENTOS CONTABLES',
      filtros,
      columnas: [
        'ID auxiliar',
        'ID cuenta',
        'Código cuenta',
        'Nombre cuenta',
        'Naturaleza',
        'Fecha auxiliar',
        'Tipo comprobante',
        'Número comprobante',
        'Detalle movimiento',
        'Estado movimiento',
        'ID tercero',
        'Documento tercero',
        'Nombre tercero',
        'Valor débito',
        'Valor crédito',
        'Valor base',
        'Saldo acumulado',
        'Módulo origen',
        'Documento origen',
        'Documento soporte',
        'ID usuario creación',
        'Usuario creación',
        'Nombre usuario creación',
        'Fecha creación',
        'ID usuario edición',
        'Usuario edición',
        'Nombre usuario edición',
        'Fecha edición'
      ],
      filas,
      anchos: [
        17, 15, 18, 44, 14, 18, 20,
        22, 55, 20, 16, 22, 38, 19,
        19, 19, 19, 25, 28, 28, 20,
        24, 35, 24, 20, 24, 35, 24
      ]
    };
  }

  /** Mantiene una fila por movimiento con su trazabilidad completa. */
  private filaAuditoria(
    cuenta: AuxiliarCuenta,
    movimiento: AuxiliarCuentaMovimiento
  ): ExcelFila {
    return [
      movimiento.idAuxiliarContable,
      cuenta.idCatalogoCuenta,
      cuenta.codigoCuenta,
      cuenta.nombreCuenta,
      cuenta.naturalezaCuenta,
      movimiento.fechaAuxiliar,
      movimiento.tipoComprobante,
      movimiento.numeroComprobante,
      movimiento.detalleMovimiento,
      movimiento.estadoMovimiento,
      movimiento.idDatosPersonal,
      movimiento.documentoTercero,
      movimiento.nombreTercero,
      movimiento.valorDebito,
      movimiento.valorCredito,
      movimiento.valorBase,
      movimiento.saldoAcumulado,
      movimiento.origenModulo,
      movimiento.documentoOrigen,
      movimiento.documentoSoporte,
      movimiento.fkSeguridadCreacion,
      movimiento.usernameCreacion,
      movimiento.nombreUsuarioCreacion,
      movimiento.fechaCreacion,
      movimiento.fechaEdicion
        ? movimiento.fkSeguridadEdicion
        : null,
      movimiento.fechaEdicion
        ? movimiento.usernameEdicion
        : null,
      movimiento.fechaEdicion
        ? movimiento.nombreUsuarioEdicion
        : null,
      movimiento.fechaEdicion
    ];
  }

  /**
   * Documenta parámetros, modalidad y controles numéricos.
   * Conserva las verificaciones del informe original.
   */
  private construirControl(
    informe: AuxiliarCuentasResponse,
    agencia: string,
    usuarioSolicitante: string,
    generado: Date,
    incluirAuditoria: boolean
  ): ExcelSheetOptions {
    const cuentas = informe.cuentas;

    const cantidadMovimientos = cuentas.reduce(
      (total, cuenta) => total + cuenta.movimientos.length,
      0
    );

    const totalDebitos = cuentas.reduce(
      (total, cuenta) => total + cuenta.totalDebitos,
      0
    );

    const totalCreditos = cuentas.reduce(
      (total, cuenta) => total + cuenta.totalCreditos,
      0
    );

    const totalSaldosIniciales = cuentas.reduce(
      (total, cuenta) => total + cuenta.saldoInicial,
      0
    );

    const totalSaldosFinales = cuentas.reduce(
      (total, cuenta) => total + cuenta.saldoFinal,
      0
    );

    const diferencia =
      totalSaldosFinales - totalSaldosIniciales;

    const variacionEsperada = cuentas.reduce(
      (total, cuenta) => total + (
        cuenta.naturalezaCuenta === 'D'
          ? cuenta.totalDebitos - cuenta.totalCreditos
          : cuenta.totalCreditos - cuenta.totalDebitos
      ),
      0
    );

    const filas: ExcelFila[] = [
      ['Agencia', agencia],
      ['Cuenta inicial', informe.cuentaInicial],
      ['Cuenta final', informe.cuentaFinal],
      ['Fecha inicial', informe.fechaInicial],
      ['Fecha final', informe.fechaFinal],
      ['Fecha y hora de generación', this.fechaHora(generado)],
      ['Usuario solicitante', usuarioSolicitante],
      [
        'Modalidad de exportación',
        incluirAuditoria ? 'COMPLETO' : 'BÁSICO'
      ],
      ['Cuentas incluidas', cuentas.length],
      ['Movimientos incluidos', cantidadMovimientos],
      ['Total débitos del período', totalDebitos],
      ['Total créditos del período', totalCreditos],
      [
        'Diferencia débitos - créditos',
        totalDebitos - totalCreditos
      ],
      [
        'Suma de saldos iniciales por naturaleza',
        totalSaldosIniciales
      ],
      [
        'Suma de saldos finales por naturaleza',
        totalSaldosFinales
      ],
      ['Variación de saldos', diferencia],
      [
        'Variación esperada según naturaleza',
        variacionEsperada
      ],
      [
        'Diferencia de control de saldos',
        diferencia - variacionEsperada
      ],
      ['Estados incluidos', 'Movimientos activos'],
      ['Alcance', 'Cuentas operables del rango consultado']
    ];

    return {
      nombreHoja: 'Control del informe',
      titulo: 'CONTROL DEL AUXILIAR CONTABLE',
      subtitulo:
        'Parámetros, identificación y verificación de resultados',
      columnas: ['Concepto', 'Valor'],
      filas,
      anchos: [46, 65]
    };
  }

  /** Convierte la fecha local a un valor legible para el control. */
  private fechaHora(fecha: Date): string {
    const dos = (valor: number) =>
      String(valor).padStart(2, '0');

    return (
      `${dos(fecha.getDate())}/` +
      `${dos(fecha.getMonth() + 1)}/` +
      `${fecha.getFullYear()} ` +
      `${dos(fecha.getHours())}:` +
      `${dos(fecha.getMinutes())}:` +
      `${dos(fecha.getSeconds())}`
    );
  }

  /** Genera una marca temporal local para identificar la exportación. */
  private marcaTiempo(fecha: Date): string {
    const dos = (valor: number) =>
      String(valor).padStart(2, '0');

    return (
      `${fecha.getFullYear()}` +
      `${dos(fecha.getMonth() + 1)}` +
      `${dos(fecha.getDate())}_` +
      `${dos(fecha.getHours())}` +
      `${dos(fecha.getMinutes())}` +
      `${dos(fecha.getSeconds())}`
    );
  }

  /** Evita caracteres no permitidos en el segmento de agencia. */
  private segmentoArchivo(valor: string): string {
    return (valor || 'Agencia')
      .trim()
      .replace(/[^a-zA-Z0-9_-]/g, '_');
  }
}
