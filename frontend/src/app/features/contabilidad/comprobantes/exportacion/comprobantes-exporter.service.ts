import * as XLSX from 'xlsx';

import { Injectable, inject } from '@angular/core';
import { ExcelExportService, ExcelFila } from '../../../../shared/services/excel-export.service';
import { FiltrosComprobantes } from '../consulta/comprobantes-consulta.models';
import {
  CabeceraExportacion,
  ControlExportacion,
  DatosExportacion,
  MovimientoExportacion
} from './comprobantes-exportacion.api';

/** Genera los libros de comprobantes con controles integrados en cada movimiento. */
@Injectable({ providedIn: 'root' })
export class ComprobantesExporterService {
  private readonly excel = inject(ExcelExportService);

  /** Exporta todos los comprobantes que corresponden a los filtros aplicados. */
  exportarConsulta(datos: DatosExportacion, filtros: FiltrosComprobantes = {}): void {
    if (!datos.comprobantes?.length) {
      alert('No hay comprobantes para exportar.');
      return;
    }
    const agencia = this.etiquetaAgencia(datos.comprobantes);
    this.generar(
      datos,
      `Comprobantes_Consulta_${agencia}_${this.marcaTiempo()}.xlsx`,
      'CONSULTA DE COMPROBANTES CONTABLES',
      this.filtros(filtros)
    );
  }

  /** Exporta un comprobante individual con sus movimientos y controles. */
  exportarComprobante(datos: DatosExportacion): void {
    const comprobante = datos.comprobantes?.[0];
    if (!comprobante) {
      alert('No hay comprobante para exportar.');
      return;
    }
    const agencia = this.etiquetaAgencia([comprobante]);
    const tipo = this.segmentoArchivo(comprobante.tipoComprobante);
    const numero = this.segmentoArchivo(comprobante.numeroComprobante);
    this.generar(
      datos,
      `Comprobante_${tipo}_${numero}_${agencia}_${this.marcaTiempo()}.xlsx`,
      `COMPROBANTE ${comprobante.tipoComprobante} - ${comprobante.numeroComprobante}`,
      [['Agencia', `${comprobante.codigoAgencia ?? ''} - ${comprobante.nombreAgencia ?? ''}`]]
    );
  }

  /** Presenta cada comprobante como bloque independiente con sus soportes y totales. */
  exportarImpresionGeneral(datos: DatosExportacion, filtros: FiltrosComprobantes = {}): void {
    const comprobantes = datos.comprobantes ?? [];
    if (!comprobantes.length) {
      alert('No hay comprobantes para exportar.');
      return;
    }
    const controles = this.agruparControles(datos.controles ?? []);
    const movimientos = new Map<string, MovimientoExportacion[]>();
    const clave = (agencia: number, tipo: string, numero: string) =>
      JSON.stringify([agencia, tipo, numero]);
    for (const movimiento of datos.movimientos ?? []) {
      const llave = clave(movimiento.idAgencia, movimiento.tipoComprobante, movimiento.numeroComprobante);
      const grupo = movimientos.get(llave) ?? [];
      grupo.push(movimiento);
      movimientos.set(llave, grupo);
    }
    const filas: ExcelFila[] = [
      ['IMPRESIÓN GENERAL DE COMPROBANTES'],
      ['Fecha inicial', filtros.fechaDesde ?? 'Todas', 'Fecha final', filtros.fechaHasta ?? 'Todas'],
      ['Tipo de comprobante', filtros.tipoComprobante ?? 'Todos'],
      []
    ];
    const combinaciones: XLSX.Range[] = [];
    const combinar = (fila: number, desde: number, hasta: number) => {
      combinaciones.push({ s: { r: fila, c: desde }, e: { r: fila, c: hasta } });
    };
    combinar(0, 0, 7);
    let totalGeneralDebito = 0;
    let totalGeneralCredito = 0;
    for (const comprobante of comprobantes) {
      const inicio = filas.length;
      filas.push([`COMPROBANTE ${comprobante.tipoComprobante} - ${comprobante.numeroComprobante}`]);
      combinar(inicio, 0, 7);
      filas.push(['Agencia', `${comprobante.codigoAgencia ?? ''} - ${comprobante.nombreAgencia ?? ''}`,
        'Fecha', comprobante.fechaContable ?? '', 'Tipo', comprobante.nombreTipoComprobante ?? '']);
      filas.push(['Concepto', comprobante.conceptoComprobante ?? '']);
      combinar(filas.length - 1, 1, 7);
      filas.push(['Cuenta', 'Nombre de cuenta', 'Documento tercero', 'Nombre tercero',
        'Detalle / concepto', 'Documentos y soportes', 'Débito', 'Crédito']);
      const detalle = movimientos.get(clave(comprobante.idAgencia, comprobante.tipoComprobante,
        comprobante.numeroComprobante)) ?? [];
      let debito = 0;
      let credito = 0;
      for (const movimiento of detalle) {
        const documentos = [movimiento.documentoOrigen, movimiento.documentoSoporte];
        for (const control of controles.get(movimiento.idAuxiliarContable) ?? []) {
          const descripcion = control.descripcionTipoControl ?? 'Documento';
          if (control.numeroDocumento) documentos.push(`${descripcion}: ${control.numeroDocumento}`);
        }
        const valorDebito = movimiento.valorDebito ?? 0;
        const valorCredito = movimiento.valorCredito ?? 0;
        debito += valorDebito;
        credito += valorCredito;
        filas.push([movimiento.codigoCuenta ?? '', movimiento.nombreCuenta ?? '',
          movimiento.documentoTercero ?? '', movimiento.nombreTercero ?? '',
          movimiento.detalleMovimiento ?? '',
          [...new Set(documentos.filter((d): d is string => !!d?.trim()))].join(' | '),
          valorDebito, valorCredito]);
      }
      totalGeneralDebito += debito;
      totalGeneralCredito += credito;
      filas.push(['TOTALES DEL COMPROBANTE', '', '', '', '', '', debito, credito]);
      combinar(filas.length - 1, 0, 5);
      filas.push(['Diferencia débito - crédito', '', '', '', '', '', debito - credito]);
      combinar(filas.length - 1, 0, 5);
      filas.push([]);
      filas.push([]);
    }
    filas.push(['TOTALES GENERALES', '', '', '', '', '', totalGeneralDebito, totalGeneralCredito]);
    combinar(filas.length - 1, 0, 5);
    filas.push(['DIFERENCIA GENERAL', '', '', '', '', '', totalGeneralDebito - totalGeneralCredito]);
    combinar(filas.length - 1, 0, 5);
    const hoja = XLSX.utils.aoa_to_sheet(filas);
    hoja['!merges'] = combinaciones;
    hoja['!cols'] = [
      { wch: 20 }, { wch: 38 }, { wch: 21 }, { wch: 34 },
      { wch: 52 }, { wch: 52 }, { wch: 19 }, { wch: 19 }
    ];
    for (let fila = 0; fila < filas.length; fila++) {
      for (const columna of [6, 7]) {
        const celda = hoja[XLSX.utils.encode_cell({ r: fila, c: columna })];
        if (celda?.t === 'n') celda.z = '#,##0.00;[Red](#,##0.00)';
      }
    }
    const libro = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(libro, hoja, 'Impresión general');
    XLSX.writeFile(libro,
      `Impresion_General_${this.etiquetaAgencia(comprobantes)}_${this.marcaTiempo()}.xlsx`);
  }

  /** Genera dos hojas y conserva una sola fila por movimiento contable. */
  private generar(
    datos: DatosExportacion,
    nombreArchivo: string,
    titulo: string,
    filtros: ExcelFila[]
  ): void {
    const comprobantes = datos.comprobantes ?? [];
    const movimientos = datos.movimientos ?? [];
    const controles = datos.controles ?? [];
    const controlesPorMovimiento = this.agruparControles(controles);
    const debitos = comprobantes.reduce((total, c) => total + (c.totalDebito ?? 0), 0);
    const creditos = comprobantes.reduce((total, c) => total + (c.totalCredito ?? 0), 0);
    const resumen: ExcelFila[] = [
      ['Comprobantes', comprobantes.length],
      ['Movimientos', movimientos.length],
      ['Controles documentales', controles.length],
      ['Total débito', debitos],
      ['Total crédito', creditos],
      ['Diferencia débito - crédito', debitos - creditos]
    ];
    this.excel.exportar({
      nombreArchivo,
      hojas: [
        {
          nombreHoja: 'Comprobantes',
          titulo,
          filtros,
          resumen,
          columnas: [
            'ID origen comprobante', 'ID agencia', 'Código agencia', 'Nombre agencia',
            'Tipo comprobante', 'Nombre tipo', 'Número comprobante', 'Fecha contable',
            'Concepto', 'Origen tipo', 'Módulo origen', 'Proceso origen', 'Tabla origen',
            'ID origen', 'Estado origen', 'Total débito', 'Total crédito', 'Movimientos',
            'ID creador origen', 'Usuario creador origen', 'Nombre creador origen',
            'Fecha creación origen', 'ID editor origen', 'Usuario editor origen',
            'Nombre editor origen', 'Fecha edición origen', 'ID creador concepto',
            'Usuario creador concepto', 'Nombre creador concepto', 'Fecha creación concepto',
            'ID editor concepto', 'Usuario editor concepto', 'Nombre editor concepto',
            'Fecha edición concepto'
          ],
          filas: comprobantes.map(c => this.filaCabecera(c)),
          anchos: [
            20, 12, 16, 28, 17, 30, 20, 17, 48, 20, 22, 25,
            25, 16, 16, 18, 18, 15, 18, 22, 30, 22, 18, 22,
            30, 22, 18, 22, 30, 22, 18, 22, 30, 22
          ]
        },
        {
          nombreHoja: 'Movimientos',
          titulo: 'MOVIMIENTOS CONTABLES Y CONTROLES DOCUMENTALES',
          columnas: [
            'ID auxiliar', 'ID agencia', 'Tipo comprobante', 'Número comprobante',
            'ID cuenta', 'Código cuenta', 'Nombre cuenta', 'ID tercero',
            'Documento tercero', 'Nombre tercero', 'Fecha auxiliar',
            'Detalle movimiento', 'Estado movimiento', 'Valor débito',
            'Valor crédito', 'Valor base', 'Módulo origen', 'Documento origen',
            'Documento soporte',
            'Cantidad controles', 'ID controles', 'ID tipos control',
            'Tipos de control', 'Exige documento', 'Números de documento',
            'ID creadores control', 'Usuarios creadores control',
            'Nombres creadores control', 'Fechas creación control',
            'ID editores control', 'Usuarios editores control',
            'Nombres editores control', 'Fechas edición control',
            'ID creador movimiento', 'Usuario creador movimiento',
            'Nombre creador movimiento', 'Fecha creación movimiento',
            'ID editor movimiento', 'Usuario editor movimiento',
            'Nombre editor movimiento', 'Fecha edición movimiento'
          ],
          filas: movimientos.map(m =>
            this.filaMovimiento(
              m,
              controlesPorMovimiento.get(m.idAuxiliarContable) ?? []
            )
          ),
          anchos: [
            16, 12, 18, 20, 14, 18, 42, 15, 20, 35, 17,
            55, 18, 18, 18, 18, 22, 25, 25, 16, 20, 20,
            38, 20, 30, 20, 25, 30, 28, 20, 25, 30, 28,
            18, 22, 30, 22, 18, 22, 30, 22
          ]
        }
      ]
    });
  }

  /** Relaciona los controles con el auxiliar sin duplicar movimientos ni importes. */
  private agruparControles(
    controles: ControlExportacion[]
  ): Map<number, ControlExportacion[]> {
    const resultado = new Map<number, ControlExportacion[]>();
    for (const control of controles) {
      const grupo = resultado.get(control.idAuxiliarContable) ?? [];
      grupo.push(control);
      resultado.set(control.idAuxiliarContable, grupo);
    }
    return resultado;
  }

  /** Construye una celda con los valores de los controles de un movimiento. */
  private valoresControles(
    controles: ControlExportacion[],
    obtener: (control: ControlExportacion) => string | number | boolean | null | undefined
  ): string {
    return controles
      .map(control => {
        const valor = obtener(control);
        return valor === null || valor === undefined ? '' : String(valor);
      })
      .join(' | ');
  }

  /** Exporta la cabecera y su auditoría en el orden de columnas definido. */
  private filaCabecera(c: CabeceraExportacion): ExcelFila {
    return [
      c.idOrigenComprobante, c.idAgencia, c.codigoAgencia, c.nombreAgencia,
      c.tipoComprobante, c.nombreTipoComprobante, c.numeroComprobante,
      c.fechaContable, c.conceptoComprobante, c.origenTipo, c.moduloOrigen,
      c.procesoOrigen, c.tablaOrigen, c.idOrigen, c.estadoOrigen,
      c.totalDebito, c.totalCredito, c.cantidadMovimientos,
      c.idUsuarioCreacionOrigen, c.usuarioCreacionOrigen,
      c.nombreCreacionOrigen, c.fechaCreacionOrigen,
      c.idUsuarioEdicionOrigen, c.usuarioEdicionOrigen,
      c.nombreEdicionOrigen, c.fechaEdicionOrigen,
      c.idUsuarioCreacionConcepto, c.usuarioCreacionConcepto,
      c.nombreCreacionConcepto, c.fechaCreacionConcepto,
      c.idUsuarioEdicionConcepto, c.usuarioEdicionConcepto,
      c.nombreEdicionConcepto, c.fechaEdicionConcepto
    ];
  }

  /** Incorpora todos los controles en las columnas del movimiento correspondiente. */
  private filaMovimiento(
    m: MovimientoExportacion,
    controles: ControlExportacion[]
  ): ExcelFila {
    const valores = (
      obtener: (control: ControlExportacion) => string | number | boolean | null | undefined
    ) => this.valoresControles(controles, obtener);
    return [
      m.idAuxiliarContable, m.idAgencia, m.tipoComprobante,
      m.numeroComprobante, m.idCatalogoCuenta, m.codigoCuenta,
      m.nombreCuenta, m.idDatosPersonal, m.documentoTercero,
      m.nombreTercero, m.fechaAuxiliar, m.detalleMovimiento,
      m.estadoMovimiento, m.valorDebito, m.valorCredito,
      m.valorBase, m.origenModulo, m.documentoOrigen,
      m.documentoSoporte,
      controles.length,
      valores(c => c.idControlesAuxiliares),
      valores(c => c.idTiposControl),
      valores(c => c.descripcionTipoControl),
      valores(c => c.pideDocumento == null ? '' : c.pideDocumento ? 'SI' : 'NO'),
      valores(c => c.numeroDocumento),
      valores(c => c.idUsuarioCreacion),
      valores(c => c.usuarioCreacion),
      valores(c => c.nombreCreacion),
      valores(c => c.fechaCreacion),
      valores(c => c.idUsuarioEdicion),
      valores(c => c.usuarioEdicion),
      valores(c => c.nombreEdicion),
      valores(c => c.fechaEdicion),
      m.idUsuarioCreacion, m.usuarioCreacion,
      m.nombreCreacion, m.fechaCreacion,
      m.idUsuarioEdicion, m.usuarioEdicion,
      m.nombreEdicion, m.fechaEdicion
    ];
  }

  /** Documenta los filtros utilizados para generar la exportación. */
  private filtros(f: FiltrosComprobantes): ExcelFila[] {
    return [
      ['ID agencia', f.idAgencia ?? 'Todas las autorizadas'],
      ['Tipo comprobante', f.tipoComprobante ?? 'Todos'],
      ['Número comprobante', f.numeroComprobante ?? 'Todos'],
      ['Fecha desde', f.fechaDesde ?? 'Sin filtro'],
      ['Fecha hasta', f.fechaHasta ?? 'Sin filtro'],
      ['Concepto', f.concepto ?? 'Todos'],
      ['ID tercero', f.idDatosPersonal ?? 'Todos']
    ];
  }

  /** Identifica la agencia mediante su código o indica varias agencias. */
  private etiquetaAgencia(comprobantes: CabeceraExportacion[]): string {
    const codigos = [
      ...new Set(
        comprobantes
          .map(c => c.codigoAgencia)
          .filter((v): v is string => !!v)
      )
    ];
    return codigos.length === 1
      ? `Agencia_${this.segmentoArchivo(codigos[0])}`
      : 'Todas_Agencias';
  }

  /** Genera una marca local de fecha y hora para el nombre del archivo. */
  private marcaTiempo(): string {
    const fecha = new Date();
    const dos = (n: number) => String(n).padStart(2, '0');
    return `${fecha.getFullYear()}${dos(fecha.getMonth() + 1)}${dos(fecha.getDate())}_` +
      `${dos(fecha.getHours())}${dos(fecha.getMinutes())}${dos(fecha.getSeconds())}`;
  }

  /** Limpia los segmentos utilizados exclusivamente en nombres de archivo. */
  private segmentoArchivo(valor: string): string {
    return (valor || '').trim().replace(/[^a-zA-Z0-9_-]/g, '_');
  }
}
