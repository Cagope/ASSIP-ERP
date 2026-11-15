import { inject, Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelFila
} from '../../../shared/services/excel-export.service';

import {
  Formato9026DTO
} from './formato9026.api';

@Injectable({
  providedIn: 'root'
})
export class Formato9026ExporterService {

  private readonly excelExport = inject(ExcelExportService);

  /**
   * Columnas oficiales del Formato SES 9026.
   *
   * El orden y la cantidad de columnas no deben
   * modificarse al generar el archivo plano.
   */
  private readonly columnasSES: string[] = [
    'CODENTIDAD',
    'FECHACORTE',
    'CONSECUTIVO',
    'TIPOIDEN',
    'NIT',
    'CODIGOCONTABLE',
    'NOMBREDEPOSITO',
    'TIPOAHORRO',
    'AMORTIZACION',
    'FECHAPERTURA',
    'PLAZO',
    'FECHAVENCIMIENTO',
    'MODALIDAD',
    'TASAINTERESNOMINAL',
    'TASAINTERESEFECTIVA',
    'INTERESESCAUSADOS',
    'SALDO',
    'DEPOSITOINICIAL',
    'NUMEROCUENTA',
    'EXENTAGMF',
    'FECHAACEPTACIONGMF',
    'ESTADO',
    'CTABAJOMONTO',
    'COTITULARES',
    'CONJUNTACOLECTIVO',
    'PASO'
  ];

  /**
   * Columnas del TXT: excluye PASO, conservado en Excel.
   */
  private readonly columnasTXT: string[] = this.columnasSES.slice(0, -1);

  /**
   * Genera el archivo TXT oficial del Formato 9026.
   *
   * Características:
   * - Exactamente 25 columnas.
   * - Separador punto y coma.
   * - Fechas en formato ddMMyyyy.
   * - Sin separadores de miles.
   * - Sin columnas complementarias.
   * - Consecutivos validados.
   * - Codificación UTF-8 con BOM.
   *
   * No permite exportar registros con campos
   * regulatorios obligatorios incompletos.
   */
  exportarPlano(
    registros: Formato9026DTO[],
    fechaCorte: string
  ): void {

    this.validarRegistros(registros);
    this.validarDatosSES(registros, fechaCorte);

    const codigoEntidad =
      this.obtenerCodigoEntidad(registros);

    const encabezado =
      'SUPERINTENDENCIA DE LA ECONOMIA SOLIDARIA' +
      ';'.repeat(this.columnasTXT.length - 1);

    const lineas: string[] = [
      encabezado,
      this.columnasTXT.join(';')
    ];

    for (const registro of registros) {

      const fila = this.construirFilaSES(registro).slice(0, this.columnasTXT.length);

      if (fila.length !== this.columnasTXT.length) {
        throw new Error(
          'El Formato 9026 debe contener exactamente 25 columnas.'
        );
      }

      lineas.push(
        fila.map(valor => this.valorPlano(valor)).join(';')
      );
    }

    const contenido =
      lineas.join('\r\n') + '\r\n';

    const archivo = new Blob(
      ['\uFEFF', contenido],
      {
        type: 'text/plain;charset=utf-8'
      }
    );

    this.descargarArchivo(
      archivo,
      this.nombreBase(codigoEntidad, fechaCorte) + '.txt'
    );
  }

  /**
   * Genera el Excel de revisión del Formato 9026.
   *
   * Hoja 1:
   * - Campos oficiales.
   * - Nombre del titular.
   * - Agencia y módulo de origen.
   *
   * Hoja 2:
   * - Cantidades por módulo.
   * - Saldos por módulo.
   * - Totales consolidados.
   *
   * Permite revisar registros pendientes de
   * completar antes de generar el TXT oficial.
   */
  exportarExcel(
    registros: Formato9026DTO[],
    fechaCorte: string
  ): void {

    this.validarRegistros(registros);

    const codigoEntidad =
      this.obtenerCodigoEntidad(registros);

    const filas: ExcelFila[] = registros.map(
      registro => [
        ...this.construirFilaSES(registro),
        registro.nombreTitular,
        registro.idAgencia,
        registro.moduloOrigen
      ]
    );

    const depositos = registros.filter(
      registro => registro.moduloOrigen === 'DEPOSITOS'
    );

    const cdats = registros.filter(
      registro => registro.moduloOrigen === 'CDAT'
    );

    const totalDepositos =
      this.sumarSaldos(depositos);

    const totalCdats =
      this.sumarSaldos(cdats);

    const totalGeneral =
      this.sumarSaldos(registros);

    const resumen: ExcelFila[] = [
      ['Cuentas de ahorro', depositos.length],
      ['CDAT', cdats.length],
      ['Total registros', registros.length],
      ['Saldo Depósitos', totalDepositos],
      ['Saldo CDAT', totalCdats],
      ['Saldo consolidado', totalGeneral]
    ];

    const filasResumen: ExcelFila[] = [
      [
        'DEPOSITOS',
        depositos.length,
        totalDepositos
      ],
      [
        'CDAT',
        cdats.length,
        totalCdats
      ],
      [
        'TOTAL',
        registros.length,
        totalGeneral
      ]
    ];

    this.excelExport.exportar({
      nombreArchivo:
        this.nombreBase(codigoEntidad, fechaCorte) +
        '.xlsx',

      hojas: [
        {
          nombreHoja: 'Detalle Ahorros',
          titulo: 'FORMATO 9026 - AHORROS',
          subtitulo:
            `Entidad: ${codigoEntidad} | ` +
            `Corte: ${fechaCorte}`,

          resumen,

          columnas: [
            ...this.columnasSES,
            'NOMBRE TITULAR',
            'AGENCIA',
            'MODULO ORIGEN'
          ],

          filas,

          anchos: [
            16, 16, 14, 14, 22,
            20, 28, 16, 18, 18,
            14, 18, 16, 22, 22,
            22, 22, 22, 24, 16,
            22, 14, 18, 16, 22,
            14, 42, 14, 18
          ]
        },
        {
          nombreHoja: 'Resumen',
          titulo: 'FORMATO 9026 - RESUMEN FINANCIERO',
          subtitulo:
            `Entidad: ${codigoEntidad} | ` +
            `Corte: ${fechaCorte}`,

          columnas: [
            'MODULO',
            'CANTIDAD CUENTAS',
            'SALDO'
          ],

          filas: filasResumen,

          anchos: [
            24,
            24,
            26
          ]
        }
      ]
    });
  }

  /**
   * Construye los 26 campos oficiales de un registro.
   *
   * Las fechas se normalizan al formato SES.
   * Los números se mantienen sin formato visual.
   * Los campos de texto conservan ceros iniciales.
   */
  private construirFilaSES(
    registro: Formato9026DTO
  ): ExcelFila {

    return [
      registro.codigoEntidad,
      this.formatearFechaSES(registro.fechaCorte),
      registro.consecutivo,
      registro.tipoIdentificacion,
      registro.nit,
      registro.codigoContable,
      registro.nombreDeposito,
      registro.tipoAhorro,
      registro.amortizacion,
      this.formatearFechaSES(registro.fechaApertura),
      registro.plazo,
      this.formatearFechaSES(registro.fechaVencimiento),
      registro.modalidad,
      this.valorNumerico(registro.tasaInteresNominal),
      this.valorNumerico(registro.tasaInteresEfectiva),
      this.valorNumerico(registro.interesesCausados),
      this.valorNumerico(registro.saldo),
      this.valorNumerico(registro.depositoInicial),
      registro.numeroCuenta,
      registro.exentaGmf,
      this.formatearFechaSES(registro.fechaAceptacionGmf),
      registro.estado,
      registro.ctaBajoMonto,
      registro.cotitulares,
      registro.conjuntaColectivo,
      registro.paso
    ];
  }

  /**
   * Suma los saldos de los registros recibidos.
   *
   * No recalcula saldos financieros; únicamente
   * totaliza los valores entregados por el backend.
   */
  private sumarSaldos(
    registros: Formato9026DTO[]
  ): number {

    return registros.reduce(
      (total, registro) =>
        total + this.valorNumerico(registro.saldo),
      0
    );
  }

  /**
   * Convierte importes y tasas a valores numéricos.
   *
   * Los valores ausentes se presentan como cero
   * únicamente para la revisión del informe.
   */
  private valorNumerico(
    valor: number | null | undefined
  ): number {

    if (
      valor === null ||
      valor === undefined ||
      !Number.isFinite(valor)
    ) {
      return 0;
    }

    return valor;
  }

  /**
   * Convierte fechas yyyy-MM-dd a ddMMyyyy.
   *
   * Si la fecha ya viene en formato SES,
   * conserva su representación original.
   */
  private formatearFechaSES(
    fecha: string | null | undefined
  ): string {

    if (!fecha) {
      return '';
    }

    const valor = fecha.trim();

    const coincidencia = valor.match(
      /^(\d{4})-(\d{2})-(\d{2})$/
    );

    if (coincidencia) {
      return (
        coincidencia[3] +
        coincidencia[2] +
        coincidencia[1]
      );
    }

    if (/^\d{8}$/.test(valor)) {
      return valor;
    }

    throw new Error(
      `La fecha "${valor}" no tiene un formato válido para SES.`
    );
  }

  /**
   * Prepara un valor para el archivo plano.
   *
   * Elimina saltos de línea y separadores
   * que podrían alterar la estructura de columnas.
   */
  private valorPlano(
    valor: string | number | boolean | Date | null | undefined
  ): string {

    if (valor === null || valor === undefined) {
      return '';
    }

    if (typeof valor === 'number') {
      return Number.isFinite(valor)
        ? String(valor)
        : '0';
    }

    return String(valor)
      .replace(/[\r\n]+/g, ' ')
      .replace(/;/g, ',')
      .trim();
  }

  /**
   * Verifica que existan registros para exportar.
   */
  private validarRegistros(
    registros: Formato9026DTO[]
  ): void {

    if (!registros?.length) {
      throw new Error(
        'No existen resultados del Formato 9026 para exportar.'
      );
    }
  }

  /**
   * Valida la información necesaria para el TXT.
   *
   * Evita generar un archivo oficial cuando
   * existen códigos regulatorios incompletos.
   *
   * La validación de campos obligatorios deberá
   * mantenerse alineada con el instructivo SES.
   */
  private validarDatosSES(
    registros: Formato9026DTO[],
    fechaCorte: string
  ): void {

    const fechaEsperada =
      this.formatearFechaSES(fechaCorte);

    const camposObligatorios: Array<
      keyof Formato9026DTO
    > = [
      'codigoEntidad',
      'fechaCorte',
      'consecutivo',
      'tipoIdentificacion',
      'nit',
      'codigoContable',
      'nombreDeposito',
      'tipoAhorro',
      'fechaApertura',
      'saldo',
      'numeroCuenta',
      'exentaGmf',
      'estado',
      'ctaBajoMonto',
      'cotitulares'
    ];

    registros.forEach((registro, indice) => {

      for (const campo of camposObligatorios) {

        const valor = registro[campo];

        if (
          valor === null ||
          valor === undefined ||
          String(valor).trim() === ''
        ) {
          throw new Error(
            `Registro ${indice + 1}: ` +
            `el campo ${campo} está pendiente de completar. ` +
            'No se puede generar el TXT oficial.'
          );
        }
      }

      // Tipo cotitular: solo obligatorio si existen cotitulares.
      const cotitulares = String(registro.cotitulares ?? '').trim();
      const tipoCotitular = String(registro.conjuntaColectivo ?? '').trim();

      if (cotitulares !== '0' && cotitulares !== '1') {
        throw new Error(
          `Registro ${indice + 1}: COTITULARES debe ser 0 o 1.`
        );
      }

      if (cotitulares === '1' && !['1', '2'].includes(tipoCotitular)) {
        throw new Error(
          `Registro ${indice + 1}: CONJUNTACOLECTIVO debe ser 1 o 2 cuando existen cotitulares.`
        );
      }

      if (cotitulares === '0' && tipoCotitular !== '') {
        throw new Error(
          `Registro ${indice + 1}: CONJUNTACOLECTIVO debe estar vacío cuando no existen cotitulares.`
        );
      }

      if (registro.consecutivo !== indice + 1) {
        throw new Error(
          `Registro ${indice + 1}: ` +
          'el consecutivo no corresponde al orden del informe.'
        );
      }

      if (
        this.formatearFechaSES(registro.fechaCorte) !==
        fechaEsperada
      ) {
        throw new Error(
          `Registro ${indice + 1}: ` +
          'la fecha de corte no coincide con el período solicitado.'
        );
      }

      if (
        registro.tipoAhorro < 1 ||
        registro.tipoAhorro > 5
      ) {
        throw new Error(
          `Registro ${indice + 1}: ` +
          'el tipo de ahorro no corresponde a los códigos SES.'
        );
      }

      const camposNumericos: Array<
        keyof Formato9026DTO
      > = [
        'tasaInteresNominal',
        'tasaInteresEfectiva',
        'interesesCausados',
        'saldo',
        'depositoInicial'
      ];

      for (const campo of camposNumericos) {

        const valor = registro[campo];

        if (
          typeof valor !== 'number' ||
          !Number.isFinite(valor)
        ) {
          throw new Error(
            `Registro ${indice + 1}: ` +
            `el campo financiero ${campo} no es válido.`
          );
        }
      }
    });
  }

  /**
   * Obtiene el código SES de la entidad.
   *
   * Se utiliza para identificar los archivos
   * TXT y Excel generados.
   */
  private obtenerCodigoEntidad(
    registros: Formato9026DTO[]
  ): string {

    const codigo =
      registros[0]?.codigoEntidad?.trim();

    if (!codigo) {
      throw new Error(
        'Los resultados no contienen el código de la entidad SES.'
      );
    }

    return codigo;
  }

  /**
   * Construye el nombre del archivo.
   *
   * Convención:
   * SES-CODENTIDAD-ddMMyyyy-9026
   */
  private nombreBase(
    codigoEntidad: string,
    fechaCorte: string
  ): string {

    const fechaSES =
      this.formatearFechaSES(fechaCorte);

    if (!fechaSES) {
      throw new Error(
        'La fecha de corte no tiene un formato válido.'
      );
    }

    return `SES-${codigoEntidad}-${fechaSES}-9026`;
  }

  /**
   * Descarga el archivo TXT generado.
   *
   * Utiliza un enlace temporal y libera
   * posteriormente la URL del objeto.
   */
  private descargarArchivo(
    archivo: Blob,
    nombreArchivo: string
  ): void {

    const url = URL.createObjectURL(archivo);

    const enlace = document.createElement('a');

    enlace.href = url;
    enlace.download = nombreArchivo;

    document.body.appendChild(enlace);

    enlace.click();
    enlace.remove();

    URL.revokeObjectURL(url);
  }
}
