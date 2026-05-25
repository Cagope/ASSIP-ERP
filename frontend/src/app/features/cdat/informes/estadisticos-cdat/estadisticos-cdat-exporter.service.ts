import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

import {
  EstadisticosCdatBloqueCompleto,
  EstadisticosCdatDetalle,
  EstadisticosCdatGrupo,
  EstadisticosCdatResumen,
  EstadisticosCdatTasa
} from './estadisticos-cdat.models';

@Injectable({
  providedIn: 'root'
})
export class EstadisticosCdatExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    fechaCorteActual: string,
    fechaCorteAnterior: string,
    resumen: EstadisticosCdatResumen | null,
    rangos: EstadisticosCdatGrupo[],
    amortizacion: EstadisticosCdatGrupo[],
    plazos: EstadisticosCdatGrupo[],
    plazosDetalle: EstadisticosCdatGrupo[],
    tasas: EstadisticosCdatTasa[],
    tasasDetalle: EstadisticosCdatTasa[]
  ): void {

    const hojas: ExcelSheetOptions[] = [
      this.crearHojaResumen(
        fechaCorteActual,
        fechaCorteAnterior,
        resumen
      ),
      this.crearHojaGrupo(
        'Rangos',
        rangos,
        'Rango'
      ),
      this.crearHojaGrupo(
        'Amortización',
        amortizacion,
        'Amortización'
      ),
      this.crearHojaGrupo(
        'Plazos',
        plazos,
        'Plazo'
      ),
      this.crearHojaGrupo(
        'Plazo exacto',
        plazosDetalle,
        'Plazo'
      ),
      this.crearHojaTasas(
        'Tasas',
        tasas
      ),
      this.crearHojaTasas(
        'Tasa exacta',
        tasasDetalle
      )
    ];

    this.excelExport.exportar({
      nombreArchivo:
        `estadisticos-cdat-${fechaCorteActual || 'corte'}.xlsx`,
      hojas
    });
  }

  exportarBloque(
    bloque: EstadisticosCdatBloqueCompleto<any>
  ): void {

    const hojas: ExcelSheetOptions[] = [
      {
        nombreHoja: 'Resumen bloque',
        titulo: bloque.titulo,
        filtros: [
          ['Fecha corte', bloque.fechaCorte || '']
        ],
        columnas: [
          'Concepto',
          'Cantidad',
          'Valor total',
          'Promedio tasa',
          '% participación'
        ],
        filas: bloque.grupos.map(g => {

          const r: any = g.resumen;

          return [
            r.concepto || r.tasa || '',
            Number(r.cantidad || 0),
            Number(r.valorTotal || 0),
            Number(r.promedioTasa || 0),
            Number(r.participacion || 0)
          ];
        }),
        anchos: [
          28,
          12,
          18,
          16,
          18
        ]
      }
    ];

    bloque.grupos.forEach((g, index) => {
      hojas.push(
        {
          nombreHoja:
            this.nombreHojaSegura(
              g.concepto || `Grupo ${index + 1}`
            ),
          columnas: [
            'CDAT',
            'Documento',
            'Asociado',
            'Agencia',
            'Fecha apertura',
            'Fecha vencimiento',
            'Plazo',
            'Tasa',
            'Valor',
            'Estado'
          ],
          filas:
            this.filasDetalle(g.detalle || []),
          anchos: [
            14,
            16,
            36,
            26,
            14,
            14,
            10,
            10,
            18,
            14
          ]
        }
      );
    });

    this.excelExport.exportar({
      nombreArchivo:
        `${this.nombreArchivoSeguro(bloque.titulo)}-${bloque.fechaCorte || 'corte'}.xlsx`,
      hojas
    });
  }

  private crearHojaResumen(
    fechaCorteActual: string,
    fechaCorteAnterior: string,
    resumen: EstadisticosCdatResumen | null
  ): ExcelSheetOptions {

    return {
      nombreHoja: 'Resumen',
      titulo: 'ESTADÍSTICOS CDAT',
      filtros: [
        ['Fecha corte actual', fechaCorteActual || ''],
        ['Fecha corte anterior', fechaCorteAnterior || '']
      ],
      columnas: [
        'Indicador',
        'Valor'
      ],
      filas: [
        [
          'Total CDAT',
          Number(resumen?.totalCdats || 0)
        ],
        [
          'Valor captado',
          Number(resumen?.valorTotalCaptado || 0)
        ],
        [
          'Promedio tasa',
          Number(resumen?.promedioTasa || 0)
        ],
        [
          'Promedio plazo',
          Number(resumen?.promedioPlazo || 0)
        ],
        [
          'Vencen 30 días',
          Number(resumen?.vencen30Dias || 0)
        ]
      ],
      anchos: [
        28,
        20
      ]
    };
  }

  private crearHojaGrupo(
    nombreHoja: string,
    datos: EstadisticosCdatGrupo[],
    tituloConcepto: string
  ): ExcelSheetOptions {

    return {
      nombreHoja,
      columnas: [
        tituloConcepto,
        'Cantidad',
        'Valor total',
        'Promedio tasa',
        '% participación'
      ],
      filas: (datos || []).map(r => [
        r.concepto || '',
        Number(r.cantidad || 0),
        Number(r.valorTotal || 0),
        Number(r.promedioTasa || 0),
        Number(r.participacion || 0)
      ]),
      anchos: [
        28,
        12,
        18,
        16,
        18
      ]
    };
  }

  private crearHojaTasas(
    nombreHoja: string,
    datos: EstadisticosCdatTasa[]
  ): ExcelSheetOptions {

    return {
      nombreHoja,
      columnas: [
        'Tasa',
        'Cantidad',
        'Valor total',
        '% participación'
      ],
      filas: (datos || []).map(r => [
        r.tasa || '',
        Number(r.cantidad || 0),
        Number(r.valorTotal || 0),
        Number(r.participacion || 0)
      ]),
      anchos: [
        18,
        12,
        18,
        18
      ]
    };
  }

  private filasDetalle(
    detalle: EstadisticosCdatDetalle[]
  ): any[][] {

    return (detalle || []).map(d => [
      d.codigoCdat || '',
      d.documento || '',
      d.nombreCompleto || '',
      d.agencia || '',
      d.fechaAperturaCdat || '',
      d.fechaVencimientoCdat || '',
      Number(d.plazoMeses || 0),
      Number(d.tasaNominalAnual || 0),
      Number(d.saldoActualCdat || 0),
      d.estadoCdat || ''
    ]);
  }

  private nombreHojaSegura(
    nombre: string
  ): string {

    return String(nombre || 'Detalle')
      .replace(/[\\/?*[\]:]/g, '')
      .substring(0, 31);
  }

  private nombreArchivoSeguro(
    nombre: string
  ): string {

    return String(nombre || 'estadisticos-cdat')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  }

}
