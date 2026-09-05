import {
  Injectable
} from '@angular/core';

import * as XLSX from 'xlsx';

import {
  DetalleAnexo1
} from './anexo1-cierre.api';


@Injectable({
  providedIn: 'root'
})
export class Anexo1CierreExporterService {


  // =========================================================
  // EXPORTAR
  //
  // Un archivo por agencia.
  //
  // Hojas:
  // - RESUMEN
  // - VIG_<CLASIFICACION>_<P|R>
  // - VENC_<CLASIFICACION>_<P|R>
  //
  // Solo se crean hojas que tengan registros.
  // =========================================================

  exportar(
    detalle: DetalleAnexo1[],
    fechaCorte: string
  ): void {

    if (!detalle?.length) {
      return;
    }

    const agencias =
      this.agruparPorAgencia(detalle);

    for (
      const [codigoAgencia, creditosAgencia]
      of agencias.entries()
    ) {

      this.exportarAgencia(
        codigoAgencia,
        creditosAgencia,
        fechaCorte
      );
    }
  }


  // =========================================================
  // ARCHIVO POR AGENCIA
  // =========================================================

  private exportarAgencia(
    codigoAgencia: string,
    detalle: DetalleAnexo1[],
    fechaCorte: string
  ): void {

    const wb =
      XLSX.utils.book_new();

    this.agregarResumen(
      wb,
      detalle,
      fechaCorte
    );

    const grupos =
      this.agruparParaHojas(detalle);

    for (
      const [clave, creditos]
      of grupos.entries()
    ) {

      this.agregarDetalle(
        wb,
        clave,
        creditos
      );
    }

    const codigo =
      this.textoSeguroArchivo(
        codigoAgencia || 'SIN_AGENCIA'
      );

    XLSX.writeFile(
      wb,
      `ANEXO1_${codigo}_${this.periodo(fechaCorte)}.xlsx`
    );
  }


  // =========================================================
  // RESUMEN GENERAL DE LA AGENCIA
  // =========================================================

  private agregarResumen(
    wb: XLSX.WorkBook,
    detalle: DetalleAnexo1[],
    fechaCorte: string
  ): void {

    const primero =
      detalle[0];

    const vigentes =
      detalle.filter(
        credito =>
          this.esVigente(credito)
      );

    const vencidos =
      detalle.filter(
        credito =>
          !this.esVigente(credito)
      );

    const filas: any[][] = [];

    filas.push(
      [
        'ANEXO 1 - CAUSACIÓN Y DETERIORO'
      ],
      [],
      [
        'Fecha de corte',
        fechaCorte || ''
      ],
      [
        'Código agencia',
        primero?.codigoAgencia || ''
      ],
      [
        'Agencia',
        primero?.nombreAgencia || ''
      ],
      [],
      [
        'Total créditos',
        detalle.length
      ],
      [
        'Créditos vigentes',
        vigentes.length
      ],
      [
        'Créditos vencidos',
        vencidos.length
      ],
      [],
      [
        'Saldo capital',
        this.sumar(
          detalle,
          'saldoCapital'
        )
      ],
      [
        'Intereses causados mes',
        this.sumar(
          detalle,
          'valorInteresesCausadosMes'
        )
      ],
      [
        'Saldo intereses causados',
        this.sumar(
          detalle,
          'saldoInteresesCausados'
        )
      ],
      [
        'Intereses contingentes mes',
        this.sumar(
          detalle,
          'valorInteresesContingentesMes'
        )
      ],
      [
        'Saldo intereses contingentes',
        this.sumar(
          detalle,
          'saldoInteresesContingentes'
        )
      ],
      [
        'Aportes prorrateados',
        this.sumar(
          detalle,
          'valorAportesCredito'
        )
      ],
      [
        'Garantías prorrateadas',
        this.sumar(
          detalle,
          'valorGarantiasCredito'
        )
      ],
      [
        'Garantías calculadas',
        this.sumar(
          detalle,
          'valorGarantiaReconocida'
        )
      ],
      [
        'Base deterioro capital',
        this.sumar(
          detalle,
          'baseDeterioroCapital'
        )
      ],
      [
        'Deterioro capital',
        this.sumar(
          detalle,
          'deterioroCapital'
        )
      ],
      [
        'Deterioro intereses',
        this.sumar(
          detalle,
          'deterioroIntereses'
        )
      ],
      [
        'Deterioro total',
        this.sumar(
          detalle,
          'deterioroCapital'
        ) +
        this.sumar(
          detalle,
          'deterioroIntereses'
        )
      ]
    );

    const ws =
      XLSX.utils.aoa_to_sheet(
        filas
      );

    ws['!cols'] = [
      {
        wch: 34
      },
      {
        wch: 28
      }
    ];

    XLSX.utils.book_append_sheet(
      wb,
      ws,
      'RESUMEN'
    );
  }


  // =========================================================
  // DETALLE
  //
  // Se utiliza la misma estructura para todas las hojas.
  // Así podemos comparar VIGENTE / VENCIDA sin perder
  // información de clasificación o garantía.
  // =========================================================

  private agregarDetalle(
    wb: XLSX.WorkBook,
    claveGrupo: string,
    detalle: DetalleAnexo1[]
  ): void {

    const filas: any[][] = [];

    filas.push([
      'Código agencia',
      'Agencia',

      'Código línea',
      'Línea',
      'Pagaré',

      'Tipo documento',
      'Documento',
      'Asociado',

      'Código clasificación',
      'Clasificación',

      'Tipo persona',

      'Código garantía',
      'Garantía',
      'Tipo garantía',

      'Saldo capital',
      'Tasa nominal anual',

      'Días mora',
      'Edad inicial',
      'Edad mora',
      'Edad riesgo',
      'Edad contable',

      'Interés causado mes',
      'Saldo intereses causados',

      'Contingente mes',
      'Saldo contingentes',

      'Saldo aportes al corte',
      '% aporte crédito',
      'Aportes prorrateados',

      'Cantidad bienes',
      'Valor garantías total',
      '% garantía crédito',
      'Garantías prorrateadas',

      '% aplicación garantía',
      'Garantías calculadas',

      '% deterioro capital',
      'Base deterioro capital',
      'Deterioro capital',

      '% deterioro intereses',
      'Deterioro intereses',

      'Deterioro total',

      'Método'
    ]);

    detalle.forEach(
      credito => {

        filas.push([
          credito.codigoAgencia || '',
          credito.nombreAgencia || '',

          credito.codigoLineaCredito || '',
          credito.nombreLineaCredito || '',
          credito.pagareCartera || '',

          credito.tipoDocumento || '',
          credito.documento || '',
          credito.nombreCompleto || '',

          credito.codigoClasificacionCredito || '',
          credito.descripcionClasificacionCredito || '',

          credito.tipoPersona || '',

          credito.codigoGarantiaCredito || '',
          credito.descripcionGarantiaCredito || '',
          this.descripcionTipoGarantia(
            credito.tipoGarantia
          ),

          this.numero(
            credito.saldoCapital
          ),

          this.numero(
            credito.tasaNominalAnual
          ),

          this.numero(
            credito.diasMora
          ),

          credito.edadRiesgoInicial || '',
          credito.edadDeMora || '',
          credito.edadDeRiesgo || '',
          credito.edadContable || '',

          this.numero(
            credito.valorInteresesCausadosMes
          ),

          this.numero(
            credito.saldoInteresesCausados
          ),

          this.numero(
            credito.valorInteresesContingentesMes
          ),

          this.numero(
            credito.saldoInteresesContingentes
          ),

          this.numero(
            credito.saldoAportesFechaCorte
          ),

          this.numero(
            credito.porcentajeAportesCredito
          ),

          this.numero(
            credito.valorAportesCredito
          ),

          this.numero(
            credito.cantidadBienesGarantia
          ),

          this.numero(
            credito.valorGarantiasTotal
          ),

          this.numero(
            credito.porcentajeGarantiasCredito
          ),

          this.numero(
            credito.valorGarantiasCredito
          ),

          this.numero(
            credito.porcentajeAplicacionGarantia
          ),

          this.numero(
            credito.valorGarantiaReconocida
          ),

          this.numero(
            credito.porcentajeDeterioroCapital
          ),

          this.numero(
            credito.baseDeterioroCapital
          ),

          this.numero(
            credito.deterioroCapital
          ),

          this.numero(
            credito.porcentajeDeterioroIntereses
          ),

          this.numero(
            credito.deterioroIntereses
          ),

          this.numero(
            credito.deterioroCapital
          ) +
          this.numero(
            credito.deterioroIntereses
          ),

          credito.codigoMetodoCalculo || ''
        ]);
      }
    );

    const ws =
      XLSX.utils.aoa_to_sheet(
        filas
      );

    ws['!cols'] = [
      { wch: 15 },
      { wch: 28 },

      { wch: 14 },
      { wch: 30 },
      { wch: 16 },

      { wch: 16 },
      { wch: 18 },
      { wch: 38 },

      { wch: 18 },
      { wch: 26 },

      { wch: 14 },

      { wch: 16 },
      { wch: 34 },
      { wch: 18 },

      { wch: 20 },
      { wch: 18 },

      { wch: 12 },
      { wch: 14 },
      { wch: 14 },
      { wch: 14 },
      { wch: 14 },

      { wch: 22 },
      { wch: 24 },

      { wch: 22 },
      { wch: 22 },

      { wch: 22 },
      { wch: 18 },
      { wch: 22 },

      { wch: 18 },
      { wch: 22 },
      { wch: 18 },
      { wch: 22 },

      { wch: 22 },
      { wch: 22 },

      { wch: 20 },
      { wch: 22 },
      { wch: 22 },

      { wch: 22 },
      { wch: 22 },

      { wch: 22 },

      { wch: 14 }
    ];

    if (detalle.length > 0) {

      ws['!autofilter'] = {
        ref:
          `A1:AO${detalle.length + 1}`
      };
    }

    XLSX.utils.book_append_sheet(
      wb,
      ws,
      this.nombreHoja(
        claveGrupo,
        wb.SheetNames
      )
    );
  }


  // =========================================================
  // AGRUPAR POR AGENCIA
  //
  // IMPORTANTE:
  // Se agrupa por codigoAgencia, no por idAgencia.
  // =========================================================

  private agruparPorAgencia(
    detalle: DetalleAnexo1[]
  ): Map<string, DetalleAnexo1[]> {

    const grupos =
      new Map<string, DetalleAnexo1[]>();

    detalle.forEach(
      credito => {

        const codigo =
          (
            credito.codigoAgencia ||
            'SIN_AGENCIA'
          )
            .trim();

        if (!grupos.has(codigo)) {

          grupos.set(
            codigo,
            []
          );
        }

        grupos.get(codigo)!
          .push(credito);
      }
    );

    return new Map(
      [...grupos.entries()]
        .sort(
          (a, b) =>
            a[0].localeCompare(
              b[0],
              'es',
              {
                numeric: true
              }
            )
        )
    );
  }


  // =========================================================
  // AGRUPAR HOJAS
  //
  // Clave:
  //
  // VIG_<CLASIFICACION>_PERSONAL
  // VIG_<CLASIFICACION>_REAL
  //
  // VENC_<CLASIFICACION>_PERSONAL
  // VENC_<CLASIFICACION>_REAL
  // =========================================================

  private agruparParaHojas(
    detalle: DetalleAnexo1[]
  ): Map<string, DetalleAnexo1[]> {

    const grupos =
      new Map<string, DetalleAnexo1[]>();

    detalle.forEach(
      credito => {

        const estado =
          this.esVigente(credito)
            ? 'VIG'
            : 'VENC';

        const clasificacion =
          this.codigoClasificacionHoja(
            credito
          );

        const garantia =
          this.codigoTipoGarantiaHoja(
            credito.tipoGarantia
          );

        const clave =
          `${estado}_${clasificacion}_${garantia}`;

        if (!grupos.has(clave)) {

          grupos.set(
            clave,
            []
          );
        }

        grupos.get(clave)!
          .push(credito);
      }
    );

    return new Map(
      [...grupos.entries()]
        .sort(
          (a, b) =>
            a[0].localeCompare(
              b[0],
              'es'
            )
        )
    );
  }


  // =========================================================
  // CLASIFICACIÓN PARA NOMBRE DE HOJA
  // =========================================================

  private codigoClasificacionHoja(
    credito: DetalleAnexo1
  ): string {

    const descripcion =
      (
        credito.descripcionClasificacionCredito ||
        credito.codigoClasificacionCredito ||
        'SIN_CLASIFICAR'
      )
        .trim()
        .toUpperCase();

    return this.textoSeguroHoja(
      descripcion
    );
  }


  // =========================================================
  // GARANTÍA PARA NOMBRE DE HOJA
  //
  // El tipo viene de cartera.garantias_creditos:
  //
  // P = PERSONAL
  // R = REAL
  // =========================================================

  private codigoTipoGarantiaHoja(
    tipoGarantia: string
  ): string {

    const tipo =
      (
        tipoGarantia ||
        ''
      )
        .trim()
        .toUpperCase();

    if (tipo === 'R') {
      return 'REAL';
    }

    if (tipo === 'P') {
      return 'PERSONAL';
    }

    return 'SIN_GARANTIA';
  }


  // =========================================================
  // DESCRIPCIÓN TIPO GARANTÍA
  // =========================================================

  private descripcionTipoGarantia(
    tipoGarantia: string
  ): string {

    const tipo =
      (
        tipoGarantia ||
        ''
      )
        .trim()
        .toUpperCase();

    if (tipo === 'R') {
      return 'REAL';
    }

    if (tipo === 'P') {
      return 'PERSONAL';
    }

    return tipo;
  }


  // =========================================================
  // VIGENTE / VENCIDO
  //
  // Se conserva la regla actual:
  // edad contable A = vigente.
  // =========================================================

  private esVigente(
    credito: DetalleAnexo1
  ): boolean {

    return (
      credito.edadContable ||
      ''
    )
      .trim()
      .toUpperCase() === 'A';
  }


  // =========================================================
  // SUMAR
  // =========================================================

  private sumar(
    detalle: DetalleAnexo1[],
    campo: keyof DetalleAnexo1
  ): number {

    return detalle.reduce(
      (total, item) =>
        total +
        this.numero(
          item[campo]
        ),
      0
    );
  }


  // =========================================================
  // NÚMERO
  // =========================================================

  private numero(
    valor: unknown
  ): number {

    if (
      valor === null ||
      valor === undefined ||
      valor === ''
    ) {

      return 0;
    }

    const numero =
      Number(valor);

    return Number.isFinite(numero)
      ? numero
      : 0;
  }


  // =========================================================
  // PERÍODO AAAAMM
  // =========================================================

  private periodo(
    fechaCorte: string
  ): string {

    if (!fechaCorte) {
      return 'PROCESO';
    }

    return fechaCorte
      .replace(
        /-/g,
        ''
      )
      .substring(
        0,
        6
      );
  }


  // =========================================================
  // TEXTO SEGURO PARA ARCHIVO
  // =========================================================

  private textoSeguroArchivo(
    valor: string
  ): string {

    return (
      valor ||
      'SIN_AGENCIA'
    )
      .trim()
      .replace(
        /[\\/:*?"<>|]/g,
        '_'
      )
      .replace(
        /\s+/g,
        '_'
      );
  }


  // =========================================================
  // TEXTO SEGURO PARA HOJA
  // =========================================================

  private textoSeguroHoja(
    valor: string
  ): string {

    return (
      valor ||
      'SIN_CLASIFICAR'
    )
      .replace(
        /[\\/?*\[\]:]/g,
        ' '
      )
      .replace(
        /\s+/g,
        '_'
      )
      .trim();
  }


  // =========================================================
  // NOMBRE SEGURO Y ÚNICO PARA HOJA
  // =========================================================

  private nombreHoja(
    nombre: string,
    existentes: string[]
  ): string {

    let base =
      (
        nombre ||
        'DETALLE'
      )
        .replace(
          /[\\/?*\[\]:]/g,
          ' '
        )
        .replace(
          /\s+/g,
          '_'
        )
        .trim()
        .substring(
          0,
          31
        );

    if (!base) {
      base = 'DETALLE';
    }

    let candidato =
      base;

    let consecutivo =
      2;

    while (
      existentes.includes(
        candidato
      )
    ) {

      const sufijo =
        `_${consecutivo}`;

      candidato =
        base.substring(
          0,
          31 - sufijo.length
        ) +
        sufijo;

      consecutivo++;
    }

    return candidato;
  }

}
