import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { SessionService } from '../../../core/auth/session.service';
import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';
import { ConsignacionesBancariasApi } from './consignaciones-bancarias.api';
import {
  CajaEfectivoDisponible,
  ChequeDisponible,
  ConsignacionBancariaPreviewResponse,
  ConsignacionBancariaRequest,
  ConsignacionBancariaResponse,
  CuentaBancariaConsignacion,
  EfectivoCajaRequest
} from './consignaciones-bancarias.models';

@Component({
  selector: 'app-consignaciones-bancarias',
  standalone: true,
  imports: [CommonModule, FormsModule, NumericFormatDirective],
  templateUrl: './consignaciones-bancarias.component.html',
  styleUrl: './consignaciones-bancarias.component.scss'
})
export class ConsignacionesBancariasComponent implements OnInit {

  idAgencia: number | null = null;
  codigoAgencia: string | null = null;
  nombreAgencia: string | null = null;

  fechaConsignacion = this.obtenerFechaActual();
  numeroConsignacion = '';
  observacion = '';
  idCuentaBancaria: number | null = null;

  cuentasBancarias: CuentaBancariaConsignacion[] = [];
  cheques: ChequeDisponible[] = [];
  cajas: CajaEfectivoDisponible[] = [];

  idsChequesSeleccionados = new Set<number>();

  valorEfectivo = 0;
  idCajaEfectivo: number | null = null;

  cargando = false;
  generandoPreview = false;
  procesando = false;

  mensajeError = '';

  preview: ConsignacionBancariaPreviewResponse | null = null;
  resultado: ConsignacionBancariaResponse | null = null;

  private cargasPendientes = 0;

  constructor(
    private readonly api: ConsignacionesBancariasApi,
    private readonly session: SessionService
  ) {
  }

  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    const agenciaActiva =
      this.session.getAgenciaActiva();

    if (!agenciaActiva || agenciaActiva.idAgencia === null) {
      this.mensajeError =
        'No hay una agencia activa en la sesión.';
      return;
    }

    this.idAgencia =
      agenciaActiva.idAgencia;

    this.codigoAgencia =
      agenciaActiva.codigoAgencia;

    this.nombreAgencia =
      agenciaActiva.nombreAgencia;

    this.cargarDatos();
  }

  // =========================================================
  // CAMBIOS DE CAPTURA
  // =========================================================

  cambiarFecha(): void {

    this.invalidarPreview();

    this.cajas = [];
    this.idCajaEfectivo = null;
    this.valorEfectivo = 0;

    if (this.idAgencia === null || !this.fechaConsignacion) {
      return;
    }

    this.cargarCajas();
  }

  cambiarNumeroConsignacion(): void {
    this.invalidarPreview();
  }

  cambiarCuentaBancaria(): void {
    this.invalidarPreview();
  }

  // =========================================================
  // CARGA DE DATOS
  // =========================================================

  cargarDatos(): void {

    if (this.idAgencia === null) {
      return;
    }

    this.mensajeError = '';

    this.cargarCuentasBancarias();
    this.cargarCheques();
    this.cargarCajas();
  }

  private cargarCuentasBancarias(): void {

    if (this.idAgencia === null) {
      return;
    }

    this.iniciarCarga();

    this.api.listarCuentasBancarias(
      this.idAgencia
    )
      .pipe(
        finalize(() => this.finalizarCarga())
      )
      .subscribe({

        next: cuentas => {
          this.cuentasBancarias =
            cuentas ?? [];
        },

        error: error => {

          this.cuentasBancarias = [];

          this.registrarErrorCarga(
            this.obtenerMensajeError(
              error,
              'No fue posible consultar las cuentas bancarias disponibles.'
            )
          );
        }
      });
  }

  private cargarCheques(): void {

    if (this.idAgencia === null) {
      return;
    }

    this.iniciarCarga();

    this.api.listarCheques(
      this.idAgencia
    )
      .pipe(
        finalize(() => this.finalizarCarga())
      )
      .subscribe({

        next: cheques => {

          this.cheques =
            cheques ?? [];

          this.depurarSeleccionCheques();
        },

        error: error => {

          this.cheques = [];

          this.idsChequesSeleccionados.clear();

          this.invalidarPreview();

          this.registrarErrorCarga(
            this.obtenerMensajeError(
              error,
              'No fue posible consultar los cheques disponibles.'
            )
          );
        }
      });
  }

  private cargarCajas(): void {

    if (this.idAgencia === null || !this.fechaConsignacion) {
      return;
    }

    this.iniciarCarga();

    this.api.listarCajas(
      this.idAgencia,
      this.fechaConsignacion
    )
      .pipe(
        finalize(() => this.finalizarCarga())
      )
      .subscribe({

        next: cajas => {

          this.cajas =
            cajas ?? [];

          this.depurarCajaEfectivo();
        },

        error: error => {

          this.cajas = [];
          this.idCajaEfectivo = null;

          this.invalidarPreview();

          this.registrarErrorCarga(
            this.obtenerMensajeError(
              error,
              'No fue posible consultar las cajas disponibles.'
            )
          );
        }
      });
  }

  // =========================================================
  // SELECCIÓN DE CHEQUES
  // =========================================================

  chequeSeleccionado(
    idChequeRecibido: number
  ): boolean {

    return this.idsChequesSeleccionados.has(
      idChequeRecibido
    );
  }

  cambiarSeleccionCheque(
    cheque: ChequeDisponible,
    seleccionado: boolean
  ): void {

    if (seleccionado) {

      this.idsChequesSeleccionados.add(
        cheque.idChequeRecibido
      );

    } else {

      this.idsChequesSeleccionados.delete(
        cheque.idChequeRecibido
      );
    }

    this.invalidarPreview();
  }

  seleccionarTodosCheques(
    seleccionado: boolean
  ): void {

    this.idsChequesSeleccionados.clear();

    if (seleccionado) {

      for (const cheque of this.cheques) {

        this.idsChequesSeleccionados.add(
          cheque.idChequeRecibido
        );
      }
    }

    this.invalidarPreview();
  }

  get todosChequesSeleccionados(): boolean {

    return this.cheques.length > 0 &&
      this.idsChequesSeleccionados.size ===
      this.cheques.length;
  }

  // =========================================================
  // EFECTIVO
  // =========================================================

  cambiarValorEfectivo(
    valor: number | null
  ): void {

    const normalizado =
      Number(valor ?? 0);

    if (!Number.isFinite(normalizado) || normalizado <= 0) {

      this.valorEfectivo = 0;
      this.idCajaEfectivo = null;

      this.invalidarPreview();

      return;
    }

    this.valorEfectivo =
      normalizado;

    if (
      this.cajas.length === 1 &&
      this.idCajaEfectivo === null
    ) {

      this.idCajaEfectivo =
        this.cajas[0].idCaja;
    }

    this.invalidarPreview();
  }

  cambiarCajaEfectivo(): void {
    this.invalidarPreview();
  }

  // =========================================================
  // TOTALES
  // =========================================================

  get cantidadChequesSeleccionados(): number {

    return this.idsChequesSeleccionados.size;
  }

  get valorChequesSeleccionados(): number {

    return this.cheques
      .filter(
        cheque =>
          this.idsChequesSeleccionados.has(
            cheque.idChequeRecibido
          )
      )
      .reduce(
        (total, cheque) =>
          total + Number(
            cheque.valorCheque ?? 0
          ),
        0
      );
  }

  get valorEfectivoTotal(): number {

    return Number(
      this.valorEfectivo ?? 0
    );
  }

  get valorTotalConsignacion(): number {

    return this.valorChequesSeleccionados +
      this.valorEfectivoTotal;
  }

  // =========================================================
  // CUENTA BANCARIA
  // =========================================================

  get cuentaBancariaSeleccionada():
    CuentaBancariaConsignacion | null {

    if (this.idCuentaBancaria === null) {
      return null;
    }

    return this.cuentasBancarias.find(
      cuenta =>
        cuenta.idCuentaBancaria ===
        this.idCuentaBancaria
    ) ?? null;
  }

  // =========================================================
  // CONCEPTO
  // =========================================================

  get conceptoSugerido(): string {

    const numero =
      this.numeroConsignacion.trim();

    const cuenta =
      this.cuentaBancariaSeleccionada;

    if (
      !numero &&
      !cuenta &&
      this.valorTotalConsignacion <= 0
    ) {

      return '';
    }

    const partes: string[] = [
      'Consignación bancaria'
    ];

    if (numero) {
      partes.push(
        `No. ${numero}`
      );
    }

    if (cuenta) {

      partes.push(
        cuenta.nombreBanco
      );

      partes.push(
        `Cuenta ${cuenta.numeroCuenta}`
      );
    }

    partes.push(
      `${this.cantidadChequesSeleccionados} cheque(s) ` +
      `${this.formatearMoneda(this.valorChequesSeleccionados)}`
    );

    partes.push(
      `Efectivo ${this.formatearMoneda(this.valorEfectivoTotal)}`
    );

    partes.push(
      `Total ${this.formatearMoneda(this.valorTotalConsignacion)}`
    );

    return partes
      .join(' - ')
      .substring(0, 250);
  }

  // =========================================================
  // PREVIEW CONTABLE
  // =========================================================

  generarPreview(): void {

    this.mensajeError = '';
    this.resultado = null;
    this.preview = null;

    if (this.generandoPreview || this.procesando) {
      return;
    }

    const request =
      this.construirRequest();

    if (request === null) {
      return;
    }

    this.generandoPreview = true;

    this.api.previewContable(
      request
    )
      .pipe(
        finalize(
          () => this.generandoPreview = false
        )
      )
      .subscribe({

        next: preview => {

          this.preview =
            preview;
        },

        error: error => {

          this.preview = null;

          this.mensajeError =
            this.obtenerMensajeError(
              error,
              'No fue posible generar el preview contable de la consignación.'
            );
        }
      });
  }

  invalidarPreview(): void {
    this.preview = null;
    this.resultado = null;
  }

  get previewValido(): boolean {

    return this.preview !== null &&
      this.preview.cuadrado === true &&
      Number(this.preview.diferencia ?? 0) === 0;
  }

  // =========================================================
  // APLICAR
  // =========================================================

  aplicar(): void {

    this.mensajeError = '';
    this.resultado = null;

    if (this.procesando) {
      return;
    }

    if (!this.previewValido) {

      this.mensajeError =
        'Debe generar un preview contable válido y cuadrado antes de registrar la consignación.';

      return;
    }

    const request =
      this.construirRequest();

    if (request === null) {
      return;
    }

    this.procesando = true;

    this.api.aplicar(
      request
    )
      .pipe(
        finalize(
          () => this.procesando = false
        )
      )
      .subscribe({

        next: resultado => {

          this.resultado =
            resultado;

          this.preview = null;

          this.numeroConsignacion = '';
          this.idCuentaBancaria = null;

          this.idsChequesSeleccionados.clear();

          this.valorEfectivo = 0;
          this.idCajaEfectivo = null;

          this.recargarDespuesDeAplicar();
        },

        error: error => {

          this.mensajeError =
            this.obtenerMensajeError(
              error,
              'No fue posible registrar la consignación bancaria.'
            );
        }
      });
  }

  // =========================================================
  // REQUEST
  // =========================================================

  private construirRequest():
    ConsignacionBancariaRequest | null {

    if (this.idAgencia === null) {

      this.mensajeError =
        'No hay una agencia activa en la sesión.';

      return null;
    }

    if (this.idCuentaBancaria === null) {

      this.mensajeError =
        'Debe seleccionar la cuenta bancaria destino.';

      return null;
    }

    if (!this.fechaConsignacion) {

      this.mensajeError =
        'Debe indicar la fecha de consignación.';

      return null;
    }

    const numeroConsignacion =
      this.numeroConsignacion.trim();

    if (!numeroConsignacion) {

      this.mensajeError =
        'Debe indicar el número de la consignación bancaria.';

      return null;
    }

    if (numeroConsignacion.length > 20) {

      this.mensajeError =
        'El número de consignación no puede superar 20 caracteres.';

      return null;
    }

    if (this.valorTotalConsignacion <= 0) {

      this.mensajeError =
        'Debe seleccionar al menos un cheque o registrar un valor en efectivo.';

      return null;
    }

    if (
      this.valorEfectivoTotal > 0 &&
      this.idCajaEfectivo === null
    ) {

      this.mensajeError =
        'Debe seleccionar la caja origen del efectivo.';

      return null;
    }

    const efectivos: EfectivoCajaRequest[] = [];

    if (
      this.valorEfectivoTotal > 0 &&
      this.idCajaEfectivo !== null
    ) {

      efectivos.push({
        idCaja: this.idCajaEfectivo,
        valor: this.valorEfectivoTotal
      });
    }

    const idsCheques =
      Array.from(
        this.idsChequesSeleccionados
      )
        .sort(
          (a, b) => a - b
        );

    return {
      idAgencia: this.idAgencia,
      idCuentaBancaria: this.idCuentaBancaria,
      fechaConsignacion: this.fechaConsignacion,
      numeroConsignacion,
      observacion: this.observacion.trim() || this.conceptoSugerido,
      idsCheques,
      efectivos
    };
  }

  // =========================================================
  // RECARGA
  // =========================================================

  private recargarDespuesDeAplicar(): void {

    if (this.idAgencia === null) {
      return;
    }

    this.cargarCheques();
    this.cargarCajas();
  }

  // =========================================================
  // DEPURACIÓN
  // =========================================================

  private depurarSeleccionCheques(): void {

    const idsDisponibles =
      new Set(
        this.cheques.map(
          cheque =>
            cheque.idChequeRecibido
        )
      );

    let cambio = false;

    for (
      const idCheque of
      Array.from(this.idsChequesSeleccionados)
    ) {

      if (!idsDisponibles.has(idCheque)) {

        this.idsChequesSeleccionados.delete(
          idCheque
        );

        cambio = true;
      }
    }

    if (cambio) {
      this.invalidarPreview();
    }
  }

  private depurarCajaEfectivo(): void {

    if (this.idCajaEfectivo === null) {
      return;
    }

    const existe =
      this.cajas.some(
        caja =>
          caja.idCaja ===
          this.idCajaEfectivo
      );

    if (!existe) {

      this.idCajaEfectivo = null;

      this.invalidarPreview();
    }
  }

  // =========================================================
  // ESTADO DE CARGA
  // =========================================================

  private iniciarCarga(): void {

    this.cargasPendientes++;

    this.cargando = true;
  }

  private finalizarCarga(): void {

    this.cargasPendientes =
      Math.max(
        0,
        this.cargasPendientes - 1
      );

    this.cargando =
      this.cargasPendientes > 0;
  }

  private registrarErrorCarga(
    mensaje: string
  ): void {

    if (!this.mensajeError) {

      this.mensajeError =
        mensaje;

      return;
    }

    if (!this.mensajeError.includes(mensaje)) {

      this.mensajeError =
        `${this.mensajeError} ${mensaje}`;
    }
  }

  // =========================================================
  // UTILIDADES
  // =========================================================

  private obtenerFechaActual(): string {

    const ahora =
      new Date();

    const anio =
      ahora.getFullYear();

    const mes =
      String(
        ahora.getMonth() + 1
      ).padStart(
        2,
        '0'
      );

    const dia =
      String(
        ahora.getDate()
      ).padStart(
        2,
        '0'
      );

    return `${anio}-${mes}-${dia}`;
  }

  private formatearMoneda(
    valor: number
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        style: 'currency',
        currency: 'COP',
        maximumFractionDigits: 0
      }
    ).format(
      valor
    );
  }

  private obtenerMensajeError(
    error: any,
    mensajeDefecto: string
  ): string {

    return error?.error?.message ??
      error?.error?.mensaje ??
      error?.error?.error ??
      mensajeDefecto;
  }
}
