
import {
  Component,
  DestroyRef,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  finalize,
  switchMap,
  tap
} from 'rxjs';

import {
  takeUntilDestroyed
} from '@angular/core/rxjs-interop';

import {
  CajasSalidaChequesApi,
  CajaVinculadaUsuario
} from './cajas-salida-cheques.api';

import {
  CajasSalidaChequesCheque,
  CajasSalidaChequesPreview,
  CajasSalidaChequesRequest,
  CajasSalidaChequesResponse
} from './cajas-salida-cheques.models';

@Component({
  selector: 'app-cajas-salida-cheques',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './cajas-salida-cheques.component.html',
  styleUrls: ['./cajas-salida-cheques.component.scss']
})
export class CajasSalidaChequesComponent implements OnInit {

  private readonly api = inject(CajasSalidaChequesApi);
  private readonly destroyRef = inject(DestroyRef);

  // =========================================================
  // VINCULACIÓN DEL USUARIO
  // =========================================================

  vinculacion: CajaVinculadaUsuario | null = null;

  idAgencia: number | null = null;
  idCajaOrigen: number | null = null;

  fechaContable = '';

  // =========================================================
  // FORMULARIO
  // =========================================================

  numeroDocumento = '';
  observacion = '';

  private observacionAutomatica = '';

  cheques: CajasSalidaChequesCheque[] = [];
  idsChequesSeleccionados: number[] = [];

  consultando = false;
  consultandoPreview = false;
  ejecutando = false;

  mensajeError = '';
  mensajeExito = '';

  resultado: CajasSalidaChequesResponse | null = null;

  // =========================================================
  // VISTA PREVIA DEL RC
  // =========================================================

  preview: CajasSalidaChequesPreview | null = null;

  private firmaPreview: string | null = null;

  // Identifica respuestas de consultas anteriores.
  private versionPreview = 0;

  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {
    this.cargarDatos();
  }

  // =========================================================
  // CARGAR CAJA VINCULADA Y CHEQUES
  // =========================================================

  cargarDatos(): void {

    if (
      this.consultando ||
      this.consultandoPreview ||
      this.ejecutando
    ) {
      return;
    }

    this.consultando = true;

    this.vinculacion = null;
    this.idAgencia = null;
    this.idCajaOrigen = null;
    this.fechaContable = '';

    this.cheques = [];
    this.idsChequesSeleccionados = [];

    this.limpiarResultado();

    this.api.obtenerCajaVinculada()
      .pipe(
        tap(vinculacion => {

          if (!vinculacion) {
            throw new Error(
              'El usuario no tiene una caja vinculada activa.'
            );
          }

          if (
            !vinculacion.idCaja ||
            !vinculacion.idAgencia ||
            !vinculacion.fechaJornada
          ) {
            throw new Error(
              'La vinculación activa no contiene los datos necesarios.'
            );
          }

          this.vinculacion = vinculacion;
          this.idAgencia = vinculacion.idAgencia;
          this.idCajaOrigen = vinculacion.idCaja;
          this.fechaContable = vinculacion.fechaJornada;
        }),

        // El backend identifica la caja del usuario.
        switchMap(() => this.api.listarCheques()),

        finalize(() => {
          this.consultando = false;
        }),

        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: cheques => {

          // Validación adicional de presentación.
          // La autorización real corresponde al backend.
          this.cheques = cheques.filter(
            cheque => cheque.idCaja === this.idCajaOrigen
          );
        },
        error: error => {
          this.mostrarError(error);
        }
      });
  }

  // =========================================================
  // CHEQUES DISPONIBLES
  // =========================================================

  get chequesDisponibles(): CajasSalidaChequesCheque[] {
    return this.cheques;
  }

  chequeSeleccionado(idCheque: number): boolean {
    return this.idsChequesSeleccionados.includes(idCheque);
  }

  cambiarCheque(
    idCheque: number,
    seleccionado: boolean
  ): void {

    if (
      this.consultando ||
      this.consultandoPreview ||
      this.ejecutando ||
      this.resultado
    ) {
      return;
    }

    const existe = this.chequesDisponibles.some(
      cheque => cheque.idChequeRecibido === idCheque
    );

    if (!existe) {
      return;
    }

    if (seleccionado) {

      if (!this.idsChequesSeleccionados.includes(idCheque)) {
        this.idsChequesSeleccionados = [
          ...this.idsChequesSeleccionados,
          idCheque
        ];
      }

    } else {

      this.idsChequesSeleccionados =
        this.idsChequesSeleccionados.filter(
          id => id !== idCheque
        );
    }
    this.limpiarResultado();
    this.generarObservacion();
  }

  seleccionarTodos(): void {

    if (
      this.consultando ||
      this.consultandoPreview ||
      this.ejecutando ||
      this.resultado
    ) {
      return;
    }

    this.idsChequesSeleccionados =
      this.chequesDisponibles.map(
        cheque => cheque.idChequeRecibido
      );

    this.limpiarResultado();
    this.generarObservacion();
  }

  desmarcarTodos(): void {

    if (
      this.consultando ||
      this.consultandoPreview ||
      this.ejecutando ||
      this.resultado
    ) {
      return;
    }

    this.idsChequesSeleccionados = [];
    this.limpiarResultado();
    this.generarObservacion();
  }

  get cantidadSeleccionados(): number {
    return this.idsChequesSeleccionados.length;
  }

  get valorTotalSeleccionado(): number {

    const seleccionados = new Set(
      this.idsChequesSeleccionados
    );

    return this.chequesDisponibles
      .filter(cheque =>
        seleccionados.has(cheque.idChequeRecibido)
      )
      .reduce(
        (total, cheque) =>
          total + Number(cheque.valorCheque || 0),
        0
      );
  }

  // =========================================================
  // CONSTRUCCIÓN DE LA SOLICITUD
  // =========================================================

  private construirRequest(): CajasSalidaChequesRequest {

    if (this.idCajaOrigen === null) {
      throw new Error(
        'El usuario no tiene una caja origen disponible.'
      );
    }

    return {
      idCajaOrigen: this.idCajaOrigen,
      fechaContable: this.fechaContable,
      numeroDocumento: this.numeroDocumento.trim() || null,
      observacion: this.observacion.trim() || null,
      idsCheques: [...this.idsChequesSeleccionados]
    };
  }

  // =========================================================
  // IDENTIFICACIÓN DE LA OPERACIÓN REVISADA
  // =========================================================

  private obtenerFirma(
    request: CajasSalidaChequesRequest
  ): string {

    return JSON.stringify({
      idCajaOrigen: request.idCajaOrigen,
      fechaContable: request.fechaContable,
      numeroDocumento: request.numeroDocumento,
      observacion: request.observacion,
      idsCheques: [...request.idsCheques].sort(
        (a, b) => a - b
      )
    });
  }

  get previewVigente(): CajasSalidaChequesPreview | null {

    if (
      !this.preview ||
      !this.firmaPreview ||
      this.idCajaOrigen === null
    ) {
      return null;
    }

    const firmaActual = this.obtenerFirma(
      this.construirRequest()
    );

    return firmaActual === this.firmaPreview
      ? this.preview
      : null;
  }

  // =========================================================
  // VALIDACIÓN PARA CONSULTAR VISTA PREVIA
  // =========================================================

  get puedeConsultarPreview(): boolean {

    return !this.consultando
      && !this.consultandoPreview
      && !this.ejecutando
      && this.resultado === null
      && this.vinculacion !== null
      && this.idAgencia !== null
      && this.idCajaOrigen !== null
      && this.fechaContable.length === 10
      && this.cantidadSeleccionados > 0
      && this.valorTotalSeleccionado > 0;
  }

  // =========================================================
  // CONSULTAR VISTA PREVIA CONTABLE RC
  // =========================================================

  consultarPreview(): void {

    this.mensajeError = '';
    this.mensajeExito = '';

    if (!this.puedeConsultarPreview) {

      this.mensajeError =
        'Seleccione al menos un cheque y verifique ' +
        'los datos de la operación.';

      return;
    }

    const request = this.construirRequest();
    const firma = this.obtenerFirma(request);

    this.invalidarPreview();

    const versionConsulta = this.versionPreview;

    this.consultandoPreview = true;

    this.api.previewContable(request)
      .pipe(
        finalize(() => {
          this.consultandoPreview = false;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: preview => {

          // No aceptar una respuesta de una operación
          // distinta de la que está actualmente seleccionada.
          if (
            versionConsulta !== this.versionPreview ||
            this.obtenerFirma(
              this.construirRequest()
            ) !== firma
          ) {
            return;
          }

          this.preview = preview;
          this.firmaPreview = firma;

          if (!preview.cuadrado) {
            this.mensajeError =
              'El comprobante RC presenta una diferencia ' +
              'contable. No se puede confirmar la entrega.';
          }
        },
        error: error => {

          if (versionConsulta !== this.versionPreview) {
            return;
          }

          this.invalidarPreview();
          this.mostrarError(error);
        }
      });
  }

  // =========================================================
  // VALIDACIÓN PARA EJECUTAR
  // =========================================================

  get puedeEjecutar(): boolean {

    const preview = this.previewVigente;

    return !this.consultando
      && !this.consultandoPreview
      && !this.ejecutando
      && this.resultado === null
      && preview !== null
      && preview.cuadrado === true
      && Number(preview.diferencia) === 0
      && preview.cantidadCheques === this.cantidadSeleccionados
      && Number(preview.valorTotal) === this.valorTotalSeleccionado
      && Number(preview.totalDebito) === Number(preview.totalCredito);
  }

  // =========================================================
  // EJECUTAR TRASLADO
  // =========================================================

  ejecutar(): void {

    this.mensajeError = '';
    this.mensajeExito = '';

    if (!this.puedeEjecutar) {

      this.mensajeError =
        'Debe consultar una vista previa RC válida y ' +
        'cuadrada antes de confirmar la entrega.';

      return;
    }

    const request = this.construirRequest();

    const confirmado = window.confirm(
      `¿Confirma la entrega de ${this.cantidadSeleccionados} ` +
      `cheque(s) por ${this.formatearMoneda(this.valorTotalSeleccionado)} ` +
      `desde ${this.vinculacion?.descripcionCaja ?? 'su caja'} ` +
      `hacia la caja CONTROL?`
    );

    if (!confirmado) {
      return;
    }

    // Verificar nuevamente la vista previa después
    // de la confirmación del usuario.
    if (!this.puedeEjecutar) {
      this.mensajeError =
        'Los datos cambiaron. Consulte nuevamente el RC.';
      return;
    }

    this.ejecutando = true;

    this.api.ejecutar(request)
      .pipe(
        finalize(() => {
          this.ejecutando = false;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: resultado => {

          this.resultado = resultado;

          this.mensajeExito =
            resultado.mensaje ||
            'La entrega de cheques se registró correctamente.';

          this.mensajeError = '';

          const idsTrasladados = new Set(
            request.idsCheques
          );

          this.cheques = this.cheques.filter(
            cheque => !idsTrasladados.has(
              cheque.idChequeRecibido
            )
          );

          this.idsChequesSeleccionados = [];
          this.invalidarPreview();
        },
        error: error => {
          this.invalidarPreview();
          this.mostrarError(error);
        }
      });
  }

  // =========================================================
  // NUEVA OPERACIÓN
  // =========================================================

  nuevaOperacion(): void {

    if (
      this.consultando ||
      this.consultandoPreview ||
      this.ejecutando
    ) {
      return;
    }

    this.numeroDocumento = '';
    this.observacion = '';
    this.observacionAutomatica = '';
    this.idsChequesSeleccionados = [];

    this.cargarDatos();
  }

  // =========================================================
  // UTILIDADES
  // =========================================================

  private invalidarPreview(): void {

    this.versionPreview++;
    this.preview = null;
    this.firmaPreview = null;
  }

  private limpiarResultado(): void {

    this.invalidarPreview();

    this.resultado = null;
    this.mensajeError = '';
    this.mensajeExito = '';
  }

  private mostrarError(error: any): void {

    this.mensajeError =
      error?.error?.message ||
      error?.error?.mensaje ||
      error?.error?.error ||
      error?.message ||
      'No fue posible completar la operación.';
  }

  formatearMoneda(valor: number): string {

    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      maximumFractionDigits: 0
    }).format(valor);
  }


  private generarObservacion(): void {

    // Si el usuario modificó el texto manualmente,
    // no sobrescribimos sus observaciones.
    if (
      this.observacion.trim() !== '' &&
      this.observacion !== this.observacionAutomatica
    ) {
      return;
    }

    if (
      this.cantidadSeleccionados === 0 ||
      !this.vinculacion
    ) {
      this.observacion = '';
      this.observacionAutomatica = '';
      return;
    }

    const cantidad = this.cantidadSeleccionados;

    const descripcionCheques =
      cantidad === 1 ? 'cheque' : 'cheques';

    const texto =
      `Entrega de ${cantidad} ${descripcionCheques} ` +
      `por valor de ${this.formatearMoneda(this.valorTotalSeleccionado)} ` +
      `desde la caja ${this.vinculacion.codigoCaja} ` +
      `hacia la caja CONTROL de la agencia.`;

    this.observacion = texto;
    this.observacionAutomatica = texto;
  }

}
