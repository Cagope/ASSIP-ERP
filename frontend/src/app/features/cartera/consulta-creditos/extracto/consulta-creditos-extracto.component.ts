import {
  CommonModule
} from '@angular/common';

import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  finalize
} from 'rxjs/operators';

import {
  ConsultaCreditosApi
} from '../consulta-creditos.api';

import {
  ConsultaCreditoDetalle,
  ConsultaCreditoExtracto,
  ConsultaCreditoInteres
} from '../consulta-creditos.models';


@Component({
  selector: 'app-consulta-creditos-extracto',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl:
    './consulta-creditos-extracto.component.html',
  styleUrls: [
    './consulta-creditos-extracto.component.scss'
  ]
})
export class ConsultaCreditosExtractoComponent {

  // =========================================================
  // ENTRADAS
  // =========================================================

  @Input({
    required: true
  })
  credito!:
    ConsultaCreditoDetalle;

  @Input()
  movimientos:
    ConsultaCreditoExtracto[] = [];

  @Input()
  interesesCausados:
    ConsultaCreditoInteres[] = [];


  // =========================================================
  // SALIDAS
  // =========================================================

  @Output()
  cerrar =
    new EventEmitter<void>();


  // =========================================================
  // ESTADO DEL PDF
  // =========================================================

  generandoPdf = false;

  errorPdf:
    string | null = null;


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly api:
      ConsultaCreditosApi
  ) {}


  // =========================================================
  // INDICADORES DEL EXTRACTO
  // =========================================================

  get tieneMovimientos(): boolean {

    return this.movimientos.length > 0;

  }

  get cantidadMovimientos(): number {

    return this.movimientos.length;

  }

  get totalCapital(): number {

    return this.sumarMovimientos(
      movimiento =>
        movimiento.valorCapital
    );

  }

  get totalIntereses(): number {

    return this.sumarMovimientos(
      movimiento =>
        movimiento.totalInteresesRegistrados
    );

  }

  get totalMora(): number {

    return this.sumarMovimientos(
      movimiento =>
        movimiento.valorInteresMora
    );

  }

  get totalSeguro(): number {

    return this.sumarMovimientos(
      movimiento =>
        movimiento.valorSeguro
    );

  }

  get totalFondoGarantia(): number {

    return this.sumarMovimientos(
      movimiento =>
        movimiento.valorFondoGarantia
    );

  }

  get totalOtrosConceptos(): number {

    return this.sumarMovimientos(
      movimiento =>
        movimiento.totalOtrosConceptos
    );

  }

  get totalAplicado(): number {

    return this.sumarMovimientos(
      movimiento =>
        movimiento.totalComponentesRegistrados
    );

  }


  // =========================================================
  // ÚLTIMA CAUSACIÓN
  // =========================================================

  get ultimaCausacion():
    ConsultaCreditoInteres | null {

    const registros =
      [
        ...this.interesesCausados
      ];

    if (
      registros.length === 0
    ) {
      return null;
    }

    registros.sort(
      (
        a,
        b
      ) => {

        const fechaA =
          this.fechaComparable(
            a.fechaProceso
            ?? a.periodoFinal
            ?? a.fechaCreacion
          );

        const fechaB =
          this.fechaComparable(
            b.fechaProceso
            ?? b.periodoFinal
            ?? b.fechaCreacion
          );

        if (
          fechaA !== fechaB
        ) {
          return fechaB - fechaA;
        }

        return (
          Number(
            b.idInteresCausado
            ?? 0
          )
          -
          Number(
            a.idInteresCausado
            ?? 0
          )
        );

      }
    );

    return registros[0];

  }


  // =========================================================
  // SALDOS ACTUALES
  // =========================================================

  get saldoCapitalActual(): number {

    return Number(
      this.ultimaCausacion
        ?.saldoCapital
      ?? this.credito
        .saldoActual
      ?? 0
    );

  }

  get saldoInteresActual(): number {

    return Number(
      this.ultimaCausacion
        ?.saldoInteresCorriente
      ?? 0
    );

  }

  get saldoMoraActual(): number {

    return Number(
      this.ultimaCausacion
        ?.saldoInteresMora
      ?? 0
    );

  }

  get saldoSeguroActual(): number {

    return Number(
      this.ultimaCausacion
        ?.saldoSeguro
      ?? 0
    );

  }

  get saldoFondoActual(): number {

    return Number(
      this.ultimaCausacion
        ?.saldoFondoGarantia
      ?? 0
    );

  }

  get saldoOtrosActual(): number {

    return Number(
      this.ultimaCausacion
        ?.saldoOtrosConceptos
      ?? 0
    );

  }

  get saldoTotalActual(): number {

    const saldoCausacion =
      this.ultimaCausacion
        ?.saldoPendiente;

    if (
      saldoCausacion !== null
      && saldoCausacion !== undefined
    ) {

      return Number(
        saldoCausacion
      );

    }

    return (
      this.saldoCapitalActual
      + this.saldoInteresActual
      + this.saldoMoraActual
      + this.saldoSeguroActual
      + this.saldoFondoActual
      + this.saldoOtrosActual
    );

  }

  get tieneSaldosCausados(): boolean {

    return this.ultimaCausacion !== null;

  }


  // =========================================================
  // PDF DEL EXTRACTO
  // =========================================================

  generarPdf(): void {

    if (
      this.generandoPdf
    ) {
      return;
    }

    const idCarteraCredito =
      Number(
        this.credito
          ?.idCarteraCredito
      );

    if (
      !Number.isInteger(
        idCarteraCredito
      )
      || idCarteraCredito <= 0
    ) {

      this.errorPdf =
        'El crédito seleccionado no tiene un identificador válido.';

      return;

    }

    this.errorPdf = null;

    /*
     * Se abre primero una pestaña vacía dentro del evento
     * del usuario. Esto evita que el navegador bloquee
     * posteriormente la apertura asincrónica del PDF.
     */
    const ventanaPdf =
      window.open(
        '',
        '_blank'
      );

    if (ventanaPdf) {

      ventanaPdf.document.title =
        'Generando extracto PDF';

      ventanaPdf.document.body.innerHTML = `
        <div style="
          font-family: Arial, sans-serif;
          padding: 32px;
          color: #334155;
        ">
          Generando extracto de crédito...
        </div>
      `;

    }

    this.generandoPdf = true;

    this.api
      .generarExtractoPdf(
        idCarteraCredito
      )
      .pipe(
        finalize(
          () => {

            this.generandoPdf = false;

          }
        )
      )
      .subscribe({

        next: (
          archivoPdf:
            Blob
        ) => {

          if (
            !archivoPdf
            || archivoPdf.size <= 0
          ) {

            ventanaPdf?.close();

            this.errorPdf =
              'El servidor no devolvió un archivo PDF válido.';

            return;

          }

          const pdf =
            archivoPdf.type ===
            'application/pdf'
              ? archivoPdf
              : new Blob(
                  [
                    archivoPdf
                  ],
                  {
                    type:
                      'application/pdf'
                  }
                );

          const urlPdf =
            URL.createObjectURL(
              pdf
            );

          if (ventanaPdf) {

            ventanaPdf.location.href =
              urlPdf;

          } else {

            /*
             * Respaldo para navegadores que bloqueen
             * completamente la nueva pestaña.
             */
            const enlace =
              document.createElement(
                'a'
              );

            enlace.href =
              urlPdf;

            enlace.target =
              '_blank';

            enlace.rel =
              'noopener noreferrer';

            document.body.appendChild(
              enlace
            );

            enlace.click();

            enlace.remove();

          }

          /*
           * Se conserva temporalmente la URL para que el visor
           * tenga tiempo suficiente de cargar el documento.
           */
          window.setTimeout(
            () => {

              URL.revokeObjectURL(
                urlPdf
              );

            },
            60000
          );

        },

        error: (
          error: unknown
        ) => {

          console.error(
            'Error generando el PDF del extracto:',
            error
          );

          ventanaPdf?.close();

          this.errorPdf =
            this.obtenerMensajeErrorPdf(
              error
            );

        }

      });

  }


  // =========================================================
  // ACCIONES
  // =========================================================

  cerrarDetalle(): void {

    if (
      this.generandoPdf
    ) {
      return;
    }

    this.errorPdf = null;

    this.cerrar.emit();

  }


  // =========================================================
  // DESCRIPCIONES
  // =========================================================

  comprobante(
    movimiento:
      ConsultaCreditoExtracto
  ): string {

    if (
      movimiento.comprobanteCompleto
    ) {
      return movimiento.comprobanteCompleto;
    }

    return [
      movimiento.tipoComprobante,
      movimiento.numeroComprobante
    ]
      .filter(
        valor =>
          Boolean(
            String(
              valor ?? ''
            ).trim()
          )
      )
      .join(
        ' '
      );

  }

  estadoMovimiento(
    movimiento:
      ConsultaCreditoExtracto
  ): string {

    if (
      movimiento.estado
    ) {
      return movimiento.estado;
    }

    if (
      movimiento.movimientoActivo
      === true
    ) {
      return 'Activo';
    }

    if (
      movimiento.movimientoActivo
      === false
    ) {
      return 'Inactivo';
    }

    return '';

  }


  // =========================================================
  // VALIDACIONES VISUALES
  // =========================================================

  esMovimientoActivo(
    movimiento:
      ConsultaCreditoExtracto
  ): boolean {

    return (
      movimiento.movimientoActivo
      === true
    );

  }

  esMovimientoInactivo(
    movimiento:
      ConsultaCreditoExtracto
  ): boolean {

    return (
      movimiento.movimientoActivo
      === false
    );

  }

  tieneMora(
    movimiento:
      ConsultaCreditoExtracto
  ): boolean {

    return (
      Number(
        movimiento.valorInteresMora
        ?? 0
      ) > 0
      ||
      Number(
        movimiento.diasMora
        ?? 0
      ) > 0
    );

  }


  // =========================================================
  // TRACK BY
  // =========================================================

  trackByMovimiento(
    indice: number,
    movimiento:
      ConsultaCreditoExtracto
  ): number {

    return (
      movimiento.idExtractoCartera
      ?? indice
    );

  }


  // =========================================================
  // UTILIDADES
  // =========================================================

  private sumarMovimientos(
    selector: (
      movimiento:
        ConsultaCreditoExtracto
    ) =>
      number
      | null
      | undefined
  ): number {

    return this.movimientos.reduce(
      (
        total,
        movimiento
      ) =>
        total
        +
        Number(
          selector(
            movimiento
          )
          ?? 0
        ),
      0
    );

  }

  private fechaComparable(
    fecha:
      | string
      | null
      | undefined
  ): number {

    if (
      !fecha
    ) {
      return 0;
    }

    const valor =
      Date.parse(
        fecha
      );

    return Number.isNaN(
      valor
    )
      ? 0
      : valor;

  }

  private obtenerMensajeErrorPdf(
    error: unknown
  ): string {

    if (
      !error
      || typeof error !== 'object'
    ) {

      return (
        'No fue posible generar el PDF del extracto.'
      );

    }

    const respuesta =
      error as {
        status?: number;
        message?: string;
      };

    if (
      respuesta.status === 401
      || respuesta.status === 403
    ) {

      return (
        'La sesión no está autorizada o ha expirado. Ingrese nuevamente.'
      );

    }

    if (
      respuesta.status === 404
    ) {

      return (
        'No se encontró el crédito solicitado.'
      );

    }

    if (
      respuesta.status === 0
    ) {

      return (
        'No fue posible conectar con el servidor.'
      );

    }

    return (
      respuesta.message
      || 'No fue posible generar el PDF del extracto.'
    );

  }

}
