
import { inject, Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelFila
} from '../../../shared/services/excel-export.service';

import {
  Formato9013DTO
} from './formato9013.api';

@Injectable({
  providedIn: 'root'
})
export class Formato9013ExporterService {

  private readonly excelExport = inject(ExcelExportService);

  /**
   * Columnas oficiales del archivo plano SES.
   * Se conserva exactamente su orden.
   */
  private readonly columnasSES: string[] = [
    'CODENTIDAD',
    'FECHACORTE',
    'CONSECUTIVO',
    'TIPOIDEN',
    'NÚMERO IDENTIFICACION',
    'saldo',
    'VRAPORTEMENSUAL',
    'APORTEORDINARIO',
    'APORTEEXTRAORDINARIO',
    'VALORREVALORIZACION',
    'MONTOPROMEDIO',
    'ULTIMAFECHA',
    'CONTRIBUCIONES'
  ];

  /**
   * Genera el TXT oficial del Formato 9013.
   *
   * No agrega totales ni columnas adicionales.
   * Los valores numéricos nulos se exportan como cero.
   */
  exportarPlano(
    registros: Formato9013DTO[],
    fechaCorte: string
  ): void {

    this.validarRegistros(registros);

    const codigoEntidad =
      this.obtenerCodigoEntidad(registros);

    const filas = registros.map(registro =>
      this.construirFilaSES(registro)
    );

    const encabezado =
      'SUPERINTENDENCIA DE LA ECONOMIA SOLIDARIA' +
      ';'.repeat(this.columnasSES.length - 1);

    const lineas: string[] = [
      encabezado,
      this.columnasSES.join(';'),
      ...filas.map(fila =>
        fila.map(valor =>
          this.valorPlano(valor)
        ).join(';')
      )
    ];

    const contenido =
      lineas.join('\r\n') + '\r\n';

    const archivo = new Blob(
      ['\uFEFF', contenido],
      {
        type: 'text/plain;charset=utf-8'
      }
    );

    const nombreArchivo =
      this.nombreBase(codigoEntidad, fechaCorte) +
      '.txt';

    this.descargarArchivo(
      archivo,
      nombreArchivo
    );
  }

  /**
   * Genera el Excel de revisión del Formato 9013.
   *
   * Incluye identificación del asociado,
   * los campos oficiales y un resumen financiero.
   */
  exportarExcel(
    registros: Formato9013DTO[],
    fechaCorte: string
  ): void {

    this.validarRegistros(registros);

    const codigoEntidad =
      this.obtenerCodigoEntidad(registros);

    const filas: ExcelFila[] = registros.map(
      registro => [
        registro.codigoEntidad,
        this.formatearFechaSES(registro.fechaCorte),
        registro.consecutivo,
        registro.tipoIdentificacion,
        registro.numeroIdentificacion,
        this.nombreCompleto(registro),
        this.valorNumerico(registro.saldoAportes),
        this.valorNumerico(registro.valorAporteMensual),
        this.valorNumerico(registro.aportesOrdinarios),
        this.valorNumerico(registro.aportesExtraordinarios),
        this.valorNumerico(registro.valorRevalorizacion),
        this.valorNumerico(registro.montoPromedio),
        this.formatearFechaSES(registro.ultimaFecha),
        this.valorNumerico(registro.contribuciones)
      ]
    );

    const sumar = (
      obtener: (registro: Formato9013DTO) =>
        number | null
    ): number => {

      return registros.reduce(
        (total, registro) =>
          total + this.valorNumerico(obtener(registro)),
        0
      );
    };

    const resumen: ExcelFila[] = [
      ['Asociados', registros.length],
      [
        'Saldo de aportes',
        sumar(r => r.saldoAportes)
      ],
      [
        'Aporte mensual',
        sumar(r => r.valorAporteMensual)
      ],
      [
        'Aportes ordinarios',
        sumar(r => r.aportesOrdinarios)
      ],
      [
        'Aportes extraordinarios',
        sumar(r => r.aportesExtraordinarios)
      ],
      [
        'Revalorización',
        sumar(r => r.valorRevalorizacion)
      ],
      [
        'Monto promedio',
        sumar(r => r.montoPromedio)
      ],
      [
        'Contribuciones',
        sumar(r => r.contribuciones)
      ]
    ];

    this.excelExport.exportar({
      nombreArchivo:
        this.nombreBase(codigoEntidad, fechaCorte) +
        '.xlsx',

      hojas: [
        {
          nombreHoja: 'Aportes Sociales',
          titulo: 'FORMATO 9013 - APORTES SOCIALES',
          subtitulo:
            `Entidad: ${codigoEntidad} | ` +
            `Corte: ${fechaCorte}`,

          resumen,

          columnas: [
            'CODENTIDAD',
            'FECHACORTE',
            'CONSECUTIVO',
            'TIPOIDEN',
            'NÚMERO IDENTIFICACION',
            'NOMBRE COMPLETO',
            'SALDO',
            'VRAPORTEMENSUAL',
            'APORTEORDINARIO',
            'APORTEEXTRAORDINARIO',
            'VALORREVALORIZACION',
            'MONTOPROMEDIO',
            'ULTIMAFECHA',
            'CONTRIBUCIONES'
          ],

          filas,

          anchos: [
            16,
            16,
            14,
            14,
            23,
            42,
            20,
            22,
            22,
            24,
            24,
            22,
            17,
            20
          ]
        }
      ]
    });
  }

  /**
   * Construye las 13 columnas del archivo plano.
   * Los campos financieros siempre son numéricos.
   */
  private construirFilaSES(
    registro: Formato9013DTO
  ): Array<string | number | null> {

    return [
      registro.codigoEntidad,
      this.formatearFechaSES(registro.fechaCorte),
      registro.consecutivo,
      registro.tipoIdentificacion,
      registro.numeroIdentificacion,
      this.valorNumerico(registro.saldoAportes),
      this.valorNumerico(registro.valorAporteMensual),
      this.valorNumerico(registro.aportesOrdinarios),
      this.valorNumerico(registro.aportesExtraordinarios),
      this.valorNumerico(registro.valorRevalorizacion),
      this.valorNumerico(registro.montoPromedio),
      this.formatearFechaSES(registro.ultimaFecha),
      this.valorNumerico(registro.contribuciones)
    ];
  }

  /**
   * Convierte valores numéricos nulos o no finitos a cero.
   * Conserva los valores financieros válidos.
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
   * Convierte fechas yyyy-MM-dd al formato SES ddMMyyyy.
   * Conserva vacía una fecha no informada.
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
   * Prepara cada campo del TXT sin alterar
   * el número de columnas.
   */
  private valorPlano(
    valor: string | number | null
  ): string {

    if (valor === null || valor === undefined) {
      return '';
    }

    if (typeof valor === 'number') {
      return String(
        Number.isFinite(valor) ? valor : 0
      );
    }

    return String(valor)
      .replace(/[\r\n]+/g, ' ')
      .replace(/;/g, ',')
      .trim();
  }

  /**
   * Nombre del asociado para el Excel.
   */
  private nombreCompleto(
    registro: Formato9013DTO
  ): string {

    return [
      registro.primerApellido,
      registro.segundoApellido,
      registro.nombres
    ]
      .filter(valor => Boolean(valor?.trim()))
      .join(' ');
  }

  /**
   * Valida que existan registros exportables.
   */
  private validarRegistros(
    registros: Formato9013DTO[]
  ): void {

    if (!registros?.length) {
      throw new Error(
        'No existen resultados para exportar.'
      );
    }
  }

  /**
   * Obtiene el código de la entidad
   * desde los resultados consultados.
   */
  private obtenerCodigoEntidad(
    registros: Formato9013DTO[]
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
   * Nombre:
   * SES-CODENTIDAD-ddMMyyyy-9013
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

    return (
      `SES-${codigoEntidad}-${fechaSES}-9013`
    );
  }

  /**
   * Descarga el archivo generado.
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
