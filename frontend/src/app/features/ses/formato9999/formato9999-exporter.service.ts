
import { inject, Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  AsociadoSES
} from './formato9999.api';

interface EstadisticaAsociados {
  masculino: number;
  femenino: number;
  juridica: number;
  otro: number;
  total: number;
}

@Injectable({
  providedIn: 'root'
})
export class Formato9999ExporterService {

  private readonly excelExport = inject(ExcelExportService);

  /**
   * Orden de campos del reporte SES.
   */
  private readonly columnas: string[] = [
    'CODENTIDAD',
    'FECHACORTE',
    'TipoIdentificacion',
    'NumeroIdentificacion',
    'PrimerApellido',
    'SegundoApellido',
    'Nombres',
    'FechaIngreso',
    'Telefono',
    'Direccion',
    'Asociado',
    'Activo',
    'ActividadEconomica',
    'CodigoMunicipio',
    'Email',
    'Genero',
    'Empleado',
    'TipoContrato',
    'NivelEscolaridad',
    'Estrato',
    'NivelIngresos',
    'FechaNacimiento',
    'EstadoCivil',
    'MujerCabezaFamilia',
    'Ocupacion',
    'SectorEconomico',
    'JornadaLaboral',
    'FechaRetiro',
    'AsistioUltAsamblea',
    'Celular'
  ];

  /**
   * Genera el archivo plano TXT para la SES.
   *
   * El nombre utiliza:
   * SES-CODENTIDAD-FECHACORTE-CODIGOFORMATO.txt
   */
  exportarPlano(
    lista: AsociadoSES[],
    codigoFormato: string
  ): void {

    this.validarDatos(lista);

    const nombreArchivo = this.construirNombreArchivo(
      lista,
      codigoFormato,
      'txt'
    );

    const filas = lista.map(
      asociado => this.construirFila(asociado, false)
    );

    const lineas = [
      this.columnas.join(';'),
      ...filas.map(
        fila => fila.map(
          valor => this.valorPlano(valor)
        ).join(';')
      )
    ];

    const contenido = lineas.join('\r\n') + '\r\n';

    const archivo = new Blob(
      ['\uFEFF', contenido],
      {
        type: 'text/plain;charset=utf-8'
      }
    );

    this.descargarArchivo(
      archivo,
      nombreArchivo
    );
  }

  /**
   * Genera el Excel de revisión.
   *
   * Hoja 1: detalle completo SES.
   * Hoja 2: estadísticas por género.
   */
  exportar(
    lista: AsociadoSES[],
    estadistica: EstadisticaAsociados | null,
    codigoFormato: string
  ): void {

    this.validarDatos(lista);

    const estad = estadistica ??
      this.calcularEstadisticas(lista);

    const fechaCorte = lista[0].fechaCorte;
    const codigoEntidad = lista[0].codigoEntidad;

    const nombreArchivo = this.construirNombreArchivo(
      lista,
      codigoFormato,
      'xlsx'
    );

    const filas = lista.map(
      asociado => this.construirFila(asociado, true)
    );

    const porcentaje = (cantidad: number): number => {

      if (estad.total === 0) {
        return 0;
      }

      return Number(
        ((cantidad / estad.total) * 100).toFixed(2)
      );
    };

    this.excelExport.exportar({
      nombreArchivo,

      hojas: [
        {
          nombreHoja: 'Asociados SES',

          titulo:
            'Superintendencia de la Economía Solidaria',

          subtitulo:
            'Reporte de Asociados',

          filtros: [
            ['Código entidad', codigoEntidad],
            ['Fecha de corte', fechaCorte],
            ['Código formato', codigoFormato]
          ],

          resumen: [
            ['Total asociados', lista.length],
            [
              'Saldo total de aportes',
              lista.reduce(
                (total, asociado) =>
                  total + Number(asociado.saldoAportes ?? 0),
                0
              )
            ]
          ],

          columnas: this.columnas,

          filas,

          anchos: [
            16,
            16,
            20,
            24,
            24,
            24,
            42,
            18,
            20,
            45,
            14,
            12,
            20,
            18,
            42,
            12,
            14,
            18,
            20,
            14,
            18,
            18,
            18,
            22,
            18,
            20,
            18,
            18,
            20,
            20
          ]
        },

        {
          nombreHoja: 'Estadísticas',

          titulo:
            'Estadísticas de Asociados',

          filtros: [
            ['Código entidad', codigoEntidad],
            ['Fecha de corte', fechaCorte],
            ['Código formato', codigoFormato]
          ],

          columnas: [
            'Categoría',
            'Cantidad',
            'Porcentaje (%)'
          ],

          filas: [
            [
              'Masculino',
              estad.masculino,
              porcentaje(estad.masculino)
            ],
            [
              'Femenino',
              estad.femenino,
              porcentaje(estad.femenino)
            ],
            [
              'Persona Jurídica',
              estad.juridica,
              porcentaje(estad.juridica)
            ],
            [
              'Otro / Sin clasificar',
              estad.otro,
              porcentaje(estad.otro)
            ],
            [
              'TOTAL',
              estad.total,
              estad.total > 0 ? 100 : 0
            ]
          ],

          anchos: [
            30,
            18,
            20
          ]
        }
      ]
    });
  }

  /**
   * Construye el nombre de los archivos SES.
   *
   * El código de entidad procede del reporte.
   * El código del formato procede de la parametrización.
   * La fecha se convierte de ddMMyyyy a yyyyMMdd.
   */
  private construirNombreArchivo(
    lista: AsociadoSES[],
    codigoFormato: string,
    extension: 'txt' | 'xlsx'
  ): string {

    const codigoEntidad = String(
      lista[0].codigoEntidad ?? ''
    ).trim();

    const fechaCorte = String(
      lista[0].fechaCorte ?? ''
    ).trim();

    const formato = String(
      codigoFormato ?? ''
    ).trim();

    if (!/^\d+$/.test(codigoEntidad)) {
      throw new Error(
        'El código de entidad SES no es válido.'
      );
    }

    if (!/^\d+$/.test(formato)) {
      throw new Error(
        'El código del formato SES no es válido.'
      );
    }

    if (!/^\d{8}$/.test(fechaCorte)) {
      throw new Error(
        'La fecha de corte debe tener formato ddMMyyyy.'
      );
    }

    const dia = fechaCorte.substring(0, 2);
    const mes = fechaCorte.substring(2, 4);
    const anio = fechaCorte.substring(4, 8);

    const fechaNombre = `${anio}${mes}${dia}`;

    return (
      `SES-${codigoEntidad}-${fechaNombre}-` +
      `${formato}.${extension}`
    );
  }

  /**
   * Construye una fila con el orden exacto
   * de las columnas del reporte.
   *
   * En Excel conserva el formato NIT anterior.
   * En el plano mantiene el documento sin guiones.
   */
  private construirFila(
    item: AsociadoSES,
    formatoExcel: boolean
  ): Array<string | number> {

    const esJuridica =
      item.tipoIdentificacion === 'N';

    const documento = esJuridica && formatoExcel
      ? this.formatearNIT(
          item.numeroIdentificacion,
          item.digitoVerificacion ?? ''
        )
      : item.numeroIdentificacion;

    return [
      item.codigoEntidad ?? '',
      item.fechaCorte ?? '',

      item.tipoIdentificacion ?? '',
      documento ?? '',

      item.primerApellido ?? '',
      item.segundoApellido ?? '',
      item.nombres ?? '',

      item.fechaIngreso ?? '',

      item.telefono ?? '',
      item.direccion ?? '',

      item.rolAsociado ?? 0,
      item.activo ?? 0,

      item.actividadEconomica ?? '',
      item.codigoMunicipio ?? '',
      item.email ?? '',

      item.genero ?? '',

      esJuridica
        ? 0
        : item.empleado ?? 0,

      item.tipoContrato ?? 0,

      item.nivelEscolaridad ?? '',
      item.estrato ?? '',

      item.nivelIngresos ?? '',

      item.fechaNacimiento ?? '',

      esJuridica
        ? 0
        : item.estadoCivil ?? 0,

      esJuridica
        ? 0
        : item.mujerCabezaFamilia ?? 0,

      item.ocupacion ?? '',
      item.sectorEconomico ?? '',

      esJuridica
        ? 0
        : item.jornadaLaboral ?? 0,

      item.fechaRetiro ?? '',

      item.asistioAsamblea ?? 0,

      item.celular ?? ''
    ];
  }

  /**
   * Conserva el formato de NIT utilizado
   * en el Excel original.
   */
  private formatearNIT(
    numero: string,
    digitoVerificacion: string
  ): string {

    if (!numero) {
      return '';
    }

    const nit = numero.padStart(9, '0');

    return (
      `${nit.substring(0, 3)}-` +
      `${nit.substring(3, 6)}-` +
      `${nit.substring(6, 9)}-` +
      `${digitoVerificacion}`
    );
  }

  /**
   * Convierte un valor al formato del archivo plano.
   *
   * Elimina saltos de línea y separadores que
   * podrían alterar la estructura del TXT.
   */
  private valorPlano(
    valor: string | number
  ): string {

    if (valor === null || valor === undefined) {
      return '';
    }

    return String(valor)
      .replace(/[\r\n]+/g, ' ')
      .replace(/;/g, ',')
      .trim();
  }

  /**
   * Calcula las estadísticas cuando no
   * se reciben desde el componente.
   */
  private calcularEstadisticas(
    lista: AsociadoSES[]
  ): EstadisticaAsociados {

    const masculino = lista.filter(
      item => Number(item.genero) === 1
    ).length;

    const femenino = lista.filter(
      item => Number(item.genero) === 2
    ).length;

    const juridica = lista.filter(
      item => Number(item.genero) === 3
    ).length;

    const total = lista.length;

    return {
      masculino,
      femenino,
      juridica,
      otro: total - masculino - femenino - juridica,
      total
    };
  }

  /**
   * Verifica que existan registros y que
   * todos correspondan a la misma entidad
   * y fecha de corte.
   */
  private validarDatos(
    lista: AsociadoSES[]
  ): void {

    if (!lista || lista.length === 0) {
      throw new Error(
        'No existen asociados para exportar.'
      );
    }

    const codigoEntidad = lista[0].codigoEntidad;
    const fechaCorte = lista[0].fechaCorte;

    if (!codigoEntidad || !fechaCorte) {
      throw new Error(
        'El reporte no contiene código de entidad o fecha de corte.'
      );
    }

    if (!/^\d{8}$/.test(fechaCorte)) {
      throw new Error(
        'La fecha de corte debe tener formato ddMMyyyy.'
      );
    }

    const inconsistente = lista.some(
      item =>
        item.codigoEntidad !== codigoEntidad ||
        item.fechaCorte !== fechaCorte
    );

    if (inconsistente) {
      throw new Error(
        'Los asociados consultados tienen diferentes entidades o fechas de corte.'
      );
    }
  }

  /**
   * Descarga el archivo generado
   * sin requerir servicios HTTP adicionales.
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

    try {
      enlace.click();
    } finally {
      document.body.removeChild(enlace);

      setTimeout(() => {
        URL.revokeObjectURL(url);
      }, 1000);
    }
  }
}
