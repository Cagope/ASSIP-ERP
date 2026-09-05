import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import * as XLSX from 'xlsx';

import {
  GarantiasCoberturaBien,
  GarantiasCoberturaCredito,
  GarantiasCoberturaDetalle,
  GarantiasCoberturaResumen
} from './garantias-cobertura.models';

import { GarantiasCoberturaService } from './garantias-cobertura.service';

@Component({
  selector: 'app-garantias-cobertura',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './garantias-cobertura.component.html',
  styleUrls: ['./garantias-cobertura.component.scss']
})
export class GarantiasCoberturaComponent implements OnInit {

  cortes: string[] = [];
  fechaCorte = '';

  resumen: GarantiasCoberturaResumen | null = null;

  bienes: GarantiasCoberturaBien[] = [];

  creditosInsuficientes: GarantiasCoberturaCredito[] = [];

  detalle: GarantiasCoberturaDetalle[] = [];

  tituloDetalle = '';
  mostrarDetalle = false;

  filtroBienes = '';

  cargando = false;
  cargandoDetalle = false;
  exportando = false;

  error = '';

  constructor(
    private readonly service: GarantiasCoberturaService
  ) {}

  // =========================================================
  // INICIO
  // =========================================================

  ngOnInit(): void {
    this.cargarCortes();
  }

  // =========================================================
  // CORTES
  // =========================================================

  cargarCortes(): void {
    this.cargando = true;
    this.error = '';

    this.service.listarCortes().subscribe({
      next: cortes => {
        this.cortes = cortes ?? [];
        this.fechaCorte = this.cortes[0] ?? '';

        if (this.fechaCorte) {
          this.cargarAnalisis();
        } else {
          this.cargando = false;
        }
      },
      error: err => this.manejarError(err)
    });
  }

  cambiarCorte(): void {

    if (!this.fechaCorte) {
      return;
    }

    this.cerrarDetalle();

    this.cargarAnalisis();
  }

  // =========================================================
  // ANÁLISIS
  // =========================================================

  cargarAnalisis(): void {

    this.cargando = true;
    this.error = '';

    forkJoin({

      resumen:
        this.service.obtenerResumen(
          this.fechaCorte
        ),

      bienes:
        this.service.listarBienes(
          this.fechaCorte
        ),

      insuficientes:
        this.service.listarCreditosInsuficientes(
          this.fechaCorte
        )

    }).subscribe({

      next: r => {

        this.resumen =
          r.resumen;

        this.bienes =
          r.bienes ?? [];

        this.creditosInsuficientes =
          r.insuficientes ?? [];

        this.cargando = false;

      },

      error: err =>
        this.manejarError(err)

    });
  }

  // =========================================================
  // FILTRO DE BIENES
  // =========================================================

  get bienesFiltrados(): GarantiasCoberturaBien[] {

    const q =
      this.filtroBienes
        .trim()
        .toLowerCase();

    if (!q) {
      return this.bienes;
    }

    return this.bienes.filter(
      b =>

        String(
          b.idBien
        ).includes(q)

        ||

        (
          b.identificacionBien ?? ''
        )
          .toLowerCase()
          .includes(q)

        ||

        (
          b.tipoBien ?? ''
        )
          .toLowerCase()
          .includes(q)

        ||

        (
          b.descripcionBien ?? ''
        )
          .toLowerCase()
          .includes(q)

        ||

        (
          b.estadoCobertura ?? ''
        )
          .toLowerCase()
          .includes(q)

    );
  }

  // =========================================================
  // DETALLE POR BIEN
  // =========================================================

  verDetalleBien(
    bien: GarantiasCoberturaBien
  ): void {

    this.cargandoDetalle = true;

    this.detalle = [];

    this.tituloDetalle =
      `Bien ${
        bien.identificacionBien ||
        bien.idBien
      }`;

    this.service
      .listarDetallePorBien(
        this.fechaCorte,
        bien.idBien
      )
      .subscribe({

        next: detalle => {

          this.detalle =
            detalle ?? [];

          this.mostrarDetalle = true;

          this.cargandoDetalle = false;

        },

        error: err => {

          this.cargandoDetalle = false;

          this.manejarError(err);

        }

      });
  }

  // =========================================================
  // DETALLE POR CRÉDITO
  // =========================================================

  verDetalleCredito(
    credito: GarantiasCoberturaCredito
  ): void {

    this.cargandoDetalle = true;

    this.detalle = [];

    this.tituloDetalle =
      `Crédito ${credito.pagareCartera}`;

    this.service
      .listarDetallePorCredito(
        this.fechaCorte,
        credito.idCarteraCredito
      )
      .subscribe({

        next: detalle => {

          this.detalle =
            detalle ?? [];

          this.mostrarDetalle = true;

          this.cargandoDetalle = false;

        },

        error: err => {

          this.cargandoDetalle = false;

          this.manejarError(err);

        }

      });
  }

  cerrarDetalle(): void {

    this.mostrarDetalle = false;

    this.detalle = [];

    this.tituloDetalle = '';
  }

  // =========================================================
  // ESTADOS VISUALES
  // =========================================================

  claseEstado(
    estado: string | null | undefined
  ): string {

    return estado === 'INSUFICIENTE'
      ? 'estado estado--alerta'
      : 'estado estado--ok';
  }

  claseAlerta(
    alerta: string | null | undefined
  ): string {

    return alerta &&
           alerta !== 'SIN ALERTA'

      ? 'estado estado--alerta'
      : 'estado estado--ok';
  }

  // =========================================================
  // EXPORTAR EXCEL
  // =========================================================

  exportarExcel(): void {

    if (!this.fechaCorte) {
      alert(
        'Debe seleccionar una fecha de corte.'
      );
      return;
    }

    if (!this.resumen) {
      alert(
        'No existe información para exportar.'
      );
      return;
    }

    this.exportando = true;
    this.error = '';

    /*
     * El detalle se consulta nuevamente porque el modal
     * solamente contiene el bien o crédito seleccionado.
     *
     * El Excel debe contener TODO el detalle auditable
     * del corte.
     */
    this.service
      .listarDetalle(
        this.fechaCorte
      )
      .subscribe({

        next: detalleCompleto => {

          try {

            this.generarArchivoExcel(
              detalleCompleto ?? []
            );

          } finally {

            this.exportando = false;

          }

        },

        error: err => {

          this.exportando = false;

          this.manejarError(err);

        }

      });
  }

  // =========================================================
  // CONSTRUIR LIBRO EXCEL
  // =========================================================

  private generarArchivoExcel(
    detalleCompleto: GarantiasCoberturaDetalle[]
  ): void {

    if (!this.resumen) {
      return;
    }

    const workbook =
      XLSX.utils.book_new();

    // =====================================================
    // HOJA 1 - RESUMEN
    // =====================================================

    const filasResumen: any[][] = [

      [
        'ANÁLISIS DE GARANTÍAS Y COBERTURA'
      ],

      [],

      [
        'Fecha de corte',
        this.fechaCorte
      ],

      [],

      [
        'Indicador',
        'Valor'
      ],

      [
        'Cantidad de bienes',
        Number(
          this.resumen.cantidadBienes || 0
        )
      ],

      [
        'Valor total de los bienes',
        Number(
          this.resumen.valorTotalBienes || 0
        )
      ],

      [
        'Bienes suficientes',
        Number(
          this.resumen.bienesSuficientes || 0
        )
      ],

      [
        'Bienes insuficientes',
        Number(
          this.resumen.bienesInsuficientes || 0
        )
      ],

      [
        'Margen total de bienes',
        Number(
          this.resumen.margenTotalBienes || 0
        )
      ],

      [
        'Déficit total por bienes',
        Number(
          this.resumen.deficitTotalBienes || 0
        )
      ],

      [
        'Créditos respaldados por bienes',
        Number(
          this.resumen.cantidadCreditosConBien || 0
        )
      ],

      [
        'Créditos con múltiples bienes',
        Number(
          this.resumen.creditosConMultiplesBienes || 0
        )
      ],

      [
        'Saldo de créditos respaldados',
        Number(
          this.resumen.saldoCreditosConBien || 0
        )
      ],

      [
        'Valor de garantías asignadas',
        Number(
          this.resumen.valorGarantiasAsignadas || 0
        )
      ],

      [
        'Cobertura efectiva',
        Number(
          this.resumen.coberturaEfectivaCreditos || 0
        )
      ],

      [
        'Exposición no cubierta',
        Number(
          this.resumen.exposicionNoCubierta || 0
        )
      ],

      [
        'Créditos con cobertura suficiente',
        Number(
          this.resumen.creditosCoberturaSuficiente || 0
        )
      ],

      [
        'Créditos con cobertura insuficiente',
        Number(
          this.resumen.creditosCoberturaInsuficiente || 0
        )
      ],

      [
        'Porcentaje cobertura efectiva',
        Number(
          this.resumen.porcentajeCoberturaEfectiva || 0
        )
      ],

      [
        'Porcentaje exposición no cubierta',
        Number(
          this.resumen.porcentajeExposicionNoCubierta || 0
        )
      ]

    ];

    const wsResumen =
      XLSX.utils.aoa_to_sheet(
        filasResumen
      );

    wsResumen['!cols'] = [
      { wch: 42 },
      { wch: 24 }
    ];

    XLSX.utils.book_append_sheet(
      workbook,
      wsResumen,
      'Resumen'
    );

    // =====================================================
    // HOJA 2 - BIENES
    // =====================================================

    const filasBienes =
      this.bienes.map(
        b => ({

          'ID Bien':
            b.idBien,

          'Identificación':
            b.identificacionBien ?? '',

          'Tipo de bien':
            b.tipoBien ?? '',

          'Descripción':
            b.descripcionBien ?? '',

          'Valor bien al corte':
            Number(
              b.valorBienFechaCorte || 0
            ),

          'Cantidad créditos':
            Number(
              b.cantidadCreditosBien || 0
            ),

          'Saldo créditos respaldados':
            Number(
              b.saldoTotalCreditosBien || 0
            ),

          'Valor garantía distribuida':
            Number(
              b.valorGarantiaDistribuida || 0
            ),

          'Cobertura efectiva':
            Number(
              b.coberturaEfectivaBien || 0
            ),

          '% cobertura':
            Number(
              b.porcentajeCoberturaBien || 0
            ),

          'Margen cobertura':
            Number(
              b.margenCobertura || 0
            ),

          'Déficit cobertura':
            Number(
              b.deficitCobertura || 0
            ),

          'Estado':
            b.estadoCobertura ?? ''

        })
      );

    const wsBienes =
      XLSX.utils.json_to_sheet(
        filasBienes
      );

    wsBienes['!cols'] = [
      { wch: 10 },
      { wch: 18 },
      { wch: 16 },
      { wch: 45 },
      { wch: 20 },
      { wch: 16 },
      { wch: 24 },
      { wch: 24 },
      { wch: 20 },
      { wch: 16 },
      { wch: 20 },
      { wch: 20 },
      { wch: 16 }
    ];

    XLSX.utils.book_append_sheet(
      workbook,
      wsBienes,
      'Bienes'
    );

    // =====================================================
    // HOJA 3 - CRÉDITOS INSUFICIENTES
    // =====================================================

    const filasInsuficientes =
      this.creditosInsuficientes.map(
        c => ({

          'ID Crédito':
            c.idCarteraCredito,

          'Pagaré':
            c.pagareCartera ?? '',

          'Documento':
            c.documento ?? '',

          'Asociado':
            c.nombreCompleto ?? '',

          'Línea':
            c.nombreLineaCredito ?? '',

          'Saldo crédito':
            Number(
              c.saldoCredito || 0
            ),

          'Cantidad bienes':
            Number(
              c.cantidadBienes || 0
            ),

          'Garantías asignadas':
            Number(
              c.valorGarantiasAsignadas || 0
            ),

          'Cobertura efectiva':
            Number(
              c.coberturaEfectivaCredito || 0
            ),

          'Exposición no cubierta':
            Number(
              c.exposicionNoCubierta || 0
            ),

          '% cobertura':
            Number(
              c.porcentajeCoberturaCredito || 0
            ),

          'Estado cobertura':
            c.estadoCoberturaCredito ?? '',

          'Días mora':
            Number(
              c.diasMora || 0
            ),

          'Edad contable':
            c.edadContableResultado ?? '',

          'Deterioro capital':
            Number(
              c.deterioroCapital || 0
            ),

          'Deterioro intereses':
            Number(
              c.deterioroIntereses || 0
            ),

          'Deterioro otros':
            Number(
              c.deterioroOtros || 0
            ),

          'Deterioro total':
            Number(
              c.deterioroTotal || 0
            ),

          'Cantidad codeudores actuales':
            Number(
              c.cantidadCodeudoresActual || 0
            ),

          'Tiene codeudor actual':
            c.tieneCodeudorActual
              ? 'SI'
              : 'NO'

        })
      );

    const wsInsuficientes =
      XLSX.utils.json_to_sheet(
        filasInsuficientes
      );

    wsInsuficientes['!cols'] = [
      { wch: 12 },
      { wch: 14 },
      { wch: 18 },
      { wch: 38 },
      { wch: 28 },
      { wch: 18 },
      { wch: 16 },
      { wch: 20 },
      { wch: 20 },
      { wch: 22 },
      { wch: 16 },
      { wch: 20 },
      { wch: 12 },
      { wch: 14 },
      { wch: 18 },
      { wch: 18 },
      { wch: 18 },
      { wch: 18 },
      { wch: 24 },
      { wch: 22 }
    ];

    XLSX.utils.book_append_sheet(
      workbook,
      wsInsuficientes,
      'Creditos insuficientes'
    );

    // =====================================================
    // HOJA 4 - DETALLE AUDITABLE
    // =====================================================

    const filasDetalle =
      detalleCompleto.map(
        d => ({

          'Fecha corte':
            d.fechaCorte ?? '',

          'ID Bien':
            d.idBien,

          'Identificación bien':
            d.identificacionBien ?? '',

          'Tipo bien':
            d.tipoBien ?? '',

          'Descripción bien':
            d.descripcionBien ?? '',

          'Valor bien al corte':
            Number(
              d.valorBienFechaCorte || 0
            ),

          'Cantidad créditos bien':
            Number(
              d.cantidadCreditosBien || 0
            ),

          'Saldo total créditos bien':
            Number(
              d.saldoTotalCreditosBien || 0
            ),

          '% cobertura bien':
            Number(
              d.porcentajeCoberturaBien || 0
            ),

          'Margen cobertura bien':
            Number(
              d.margenCobertura || 0
            ),

          'Déficit cobertura bien':
            Number(
              d.deficitCobertura || 0
            ),

          'Estado bien':
            d.estadoCobertura ?? '',

          'ID Crédito':
            d.idCarteraCredito,

          'Pagaré':
            d.pagareCartera ?? '',

          'Documento':
            d.documento ?? '',

          'Asociado':
            d.nombreCompleto ?? '',

          'Código línea':
            d.codigoLineaCredito ?? '',

          'Línea':
            d.nombreLineaCredito ?? '',

          'Clasificación':
            d.descripcionClasificacionCredito ?? '',

          'Garantía crédito':
            d.descripcionGarantiaCredito ?? '',

          'Destino económico':
            d.descripcionDestinoEconomico ?? '',

          'Saldo crédito':
            Number(
              d.saldoCredito || 0
            ),

          '% participación crédito/bien':
            Number(
              d.porcentajeCreditoBien || 0
            ),

          'Valor garantía asignado':
            Number(
              d.valorGarantiaCreditoBien || 0
            ),

          'Días mora':
            Number(
              d.diasMora || 0
            ),

          'Edad contable':
            d.edadContableResultado ?? '',

          'Deterioro capital':
            Number(
              d.deterioroCapital || 0
            ),

          'Deterioro intereses':
            Number(
              d.deterioroIntereses || 0
            ),

          'Deterioro otros':
            Number(
              d.deterioroOtros || 0
            ),

          'Deterioro total':
            Number(
              d.deterioroTotal || 0
            ),

          'Cantidad codeudores actuales':
            Number(
              d.cantidadCodeudoresActual || 0
            ),

          'Tiene codeudor actual':
            d.tieneCodeudorActual
              ? 'SI'
              : 'NO',

          'Alerta':
            d.alertaCobertura ?? ''

        })
      );

    const wsDetalle =
      XLSX.utils.json_to_sheet(
        filasDetalle
      );

    wsDetalle['!cols'] = [
      { wch: 14 },
      { wch: 10 },
      { wch: 20 },
      { wch: 16 },
      { wch: 42 },
      { wch: 20 },
      { wch: 18 },
      { wch: 24 },
      { wch: 18 },
      { wch: 20 },
      { wch: 20 },
      { wch: 18 },
      { wch: 12 },
      { wch: 14 },
      { wch: 18 },
      { wch: 38 },
      { wch: 14 },
      { wch: 30 },
      { wch: 28 },
      { wch: 24 },
      { wch: 30 },
      { wch: 18 },
      { wch: 24 },
      { wch: 22 },
      { wch: 12 },
      { wch: 14 },
      { wch: 18 },
      { wch: 18 },
      { wch: 18 },
      { wch: 18 },
      { wch: 24 },
      { wch: 22 },
      { wch: 42 }
    ];

    XLSX.utils.book_append_sheet(
      workbook,
      wsDetalle,
      'Detalle auditable'
    );

    // =====================================================
    // GENERAR ARCHIVO
    // =====================================================

    XLSX.writeFile(
      workbook,
      `garantias-cobertura-${this.fechaCorte}.xlsx`
    );
  }

  // =========================================================
  // ERROR
  // =========================================================

  private manejarError(
    err: any
  ): void {

    console.error(err);

    this.error =
      err?.error?.message
      ||
      err?.error
      ||
      'No fue posible cargar el análisis de garantías y cobertura.';

    this.cargando = false;
  }

}
