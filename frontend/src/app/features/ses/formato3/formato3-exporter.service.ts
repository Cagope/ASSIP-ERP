
import { Injectable, inject } from '@angular/core';

import {
  ExcelExportService,
  ExcelFila,
  ExcelSheetOptions
} from '../../../shared/services/excel-export.service';

interface RegistroFormato3 {
  codigoEntidad: string;
  fechaCorte: string;
  codigoRenglon: string;
  descripcion: string;
  saldo: number;
  c2: string;
  c3: string;
  c4: string;
  c5: string;
}

@Injectable({
  providedIn: 'root'
})
export class Formato3ExporterService {

  private readonly excel = inject(ExcelExportService);

  /**
   * Exporta a Excel el contenido del plano oficial.
   *
   * Recibe el nombre oficial desde el componente,
   * obtenido de la respuesta del backend.
   *
   * No consulta nuevamente los saldos ni realiza
   * cálculos contables adicionales.
   */
  exportar(
    contenido: string,
    ano: number,
    mes: number,
    nombreArchivo: string
  ): void {

    const registros = this.interpretarPlano(contenido);

    if (registros.length === 0) {
      throw new Error(
        'El Formato 3 no contiene registros para exportar.'
      );
    }

    if (
      !/^SES-[A-Za-z0-9_-]+-\d{8}-[A-Za-z0-9_-]+\.xlsx$/i.test(
        nombreArchivo
      )
    ) {
      throw new Error(
        'El nombre oficial del archivo Excel no es válido.'
      );
    }

    const filas: ExcelFila[] = registros.map(registro => [
      registro.codigoEntidad,
      registro.fechaCorte,
      registro.codigoRenglon,
      registro.descripcion,
      registro.saldo,
      registro.c2,
      registro.c3,
      registro.c4,
      registro.c5
    ]);

    const codigoEntidad = registros[0].codigoEntidad;
    const fechaCorte = registros[0].fechaCorte;

    const hoja: ExcelSheetOptions = {

      nombreHoja: 'Formato 3',

      titulo: 'FORMATO 3 - SALDOS CONTABLES',

      subtitulo:
        'SUPERINTENDENCIA DE LA ECONOMÍA SOLIDARIA',

      filtros: [
        ['Código entidad', codigoEntidad],
        ['Año', ano],
        ['Mes', this.nombreMes(mes)],
        ['Fecha de corte', fechaCorte]
      ],

      resumen: [
        ['Cantidad de renglones', registros.length]
      ],

      columnas: [
        'CODENTIDAD',
        'FECHACORTE',
        'CODRENGLON',
        'DESCRIPCIÓN',
        'SALDO',
        'C2',
        'C3',
        'C4',
        'C5'
      ],

      filas,

      anchos: [
        18,
        18,
        20,
        65,
        24,
        14,
        14,
        14,
        14
      ]
    };

    this.excel.exportar({
      nombreArchivo,
      hojas: [hoja]
    });
  }

  /**
   * Interpreta el archivo plano delimitado
   * por punto y coma.
   *
   * Primera línea: encabezado institucional.
   * Segunda línea: nombres de columnas.
   * Líneas siguientes: registros contables.
   */
  private interpretarPlano(
    contenido: string
  ): RegistroFormato3[] {

    const lineas = contenido
      .replace(/^\uFEFF/, '')
      .split(/\r?\n/)
      .map(linea => linea.trim())
      .filter(linea => linea.length > 0);

    if (lineas.length < 2) {
      throw new Error(
        'El archivo plano no tiene la estructura esperada.'
      );
    }

    const columnasEsperadas = [
      'CODENTIDAD',
      'FECHACORTE',
      'CODRENGLON',
      'DESCRIPCIÓN',
      'SALDO',
      'C2',
      'C3',
      'C4',
      'C5'
    ];

    const encabezado = lineas[1]
      .split(';')
      .map(columna => columna.trim().toUpperCase());

    const encabezadoValido =
      encabezado.length === columnasEsperadas.length &&
      columnasEsperadas.every(
        (columna, indice) => encabezado[indice] === columna
      );

    if (!encabezadoValido) {
      throw new Error(
        'Las columnas del archivo no corresponden al Formato 3.'
      );
    }

    const registros: RegistroFormato3[] = [];

    for (let indice = 2; indice < lineas.length; indice++) {

      const columnas = lineas[indice].split(';');

      if (columnas.length !== 9) {
        throw new Error(
          `La línea ${indice + 1} no contiene las nueve columnas requeridas.`
        );
      }

      const codigoEntidad = columnas[0].trim();
      const fechaCorte = columnas[1].trim();
      const codigoRenglon = columnas[2].trim();
      const descripcion = columnas[3].trim();
      const saldoTexto = columnas[4].trim();

      if (!/^\d+$/.test(codigoEntidad)) {
        throw new Error(
          `Código de entidad inválido en la línea ${indice + 1}.`
        );
      }

      if (!/^\d{8}$/.test(fechaCorte)) {
        throw new Error(
          `Fecha de corte inválida en la línea ${indice + 1}.`
        );
      }

      if (!/^\d{6}$/.test(codigoRenglon)) {
        throw new Error(
          `Código de renglón inválido en la línea ${indice + 1}.`
        );
      }

      if (!/^-?\d+(\.\d+)?$/.test(saldoTexto)) {
        throw new Error(
          `Saldo inválido en la línea ${indice + 1}.`
        );
      }

      const saldo = Number(saldoTexto);

      if (!Number.isFinite(saldo)) {
        throw new Error(
          `El saldo de la línea ${indice + 1} no es válido.`
        );
      }

      registros.push({
        codigoEntidad,
        fechaCorte,
        codigoRenglon,
        descripcion,
        saldo,
        c2: columnas[5].trim(),
        c3: columnas[6].trim(),
        c4: columnas[7].trim(),
        c5: columnas[8].trim()
      });
    }

    return registros;
  }

  /**
   * Obtiene el nombre del mes para el encabezado.
   */
  private nombreMes(mes: number): string {

    const meses = [
      'Enero',
      'Febrero',
      'Marzo',
      'Abril',
      'Mayo',
      'Junio',
      'Julio',
      'Agosto',
      'Septiembre',
      'Octubre',
      'Noviembre',
      'Diciembre'
    ];

    if (
      !Number.isInteger(mes) ||
      mes < 1 ||
      mes > 12
    ) {
      throw new Error(
        'El mes seleccionado no es válido.'
      );
    }

    return meses[mes - 1];
  }
}
