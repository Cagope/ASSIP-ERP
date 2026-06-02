import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CapturaDepositosApi } from './captura-depositos.api';
import {
  CajaCapturaDepositosCuenta,
  CajaCapturaDepositosPreview,
  CajaCapturaDepositosRequest,
  CajaCapturaDepositosResponse,
  TipoMovimientoDeposito
} from './captura-depositos.models';

import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';

import {
  ExtractoCuentaSharedComponent
} from '../../../shared/cuentas-ahorro/extracto-cuenta/extracto-cuenta-shared.component';

import { environment } from '../../../../environments/environment';

import {
  VincularCajaApi,
  CajaProvisionActivaDTO
} from '../provisiones/vincular-caja/vincular-caja.api';

import {
  LavadoActivosModalComponent
} from '../../../shared/lavado-activos-modal/lavado-activos-modal.component';

@Component({
  selector: 'app-captura-depositos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NumericFormatDirective,
    ExtractoCuentaSharedComponent,
    LavadoActivosModalComponent
  ],
  templateUrl: './captura-depositos.component.html',
  styleUrl: './captura-depositos.component.scss'
})
export class CapturaDepositosComponent {

  cargando = false;
  aplicando = false;

  error = '';
  mensaje = '';
  mostrarExtracto = false;

  filtros = {
    documento: '',
    nombres: '',
    primerApellido: '',
    segundoApellido: ''
  };

  cuentas: CajaCapturaDepositosCuenta[] = [];
  tiposMovimiento: TipoMovimientoDeposito[] = [];



  idCaja = 0;
  idAgencia = 0;
  fechaContable = new Date().toISOString().substring(0, 10);

  idCuentaAhorro: number | null = null;

  tipoMovimiento = '';
  numeroComprobante = '';

  valorEfectivo = 0;
  valorCheques = 0;

  cheques = [
    {
      codigoBanco: '',
      numeroCheque: '',
      valorCheque: 0,
      observacion: ''
    }
  ];

  mostrarCheques = true;

  provisionActiva: CajaProvisionActivaDTO | null = null;

  cuenta: CajaCapturaDepositosCuenta | null = null;
  previewData: CajaCapturaDepositosPreview | null = null;
  respuesta: CajaCapturaDepositosResponse | null = null;
  firmaAmpliada: string | null = null;

  mostrarLavadoActivos = false;
  lavadoActivosData: any = null;

  constructor(
    private api: CapturaDepositosApi,
    private cajaApi: VincularCajaApi
  ) {
    this.cargarCajaActiva();
    this.cargarTiposMovimiento();
  }

  cargarTiposMovimiento(): void {
    this.api.listarTiposMovimiento().subscribe({
      next: data => {
        this.tiposMovimiento = data || [];
      },
      error: err => {
        this.error = err?.error?.message || 'No fue posible cargar los tipos de movimiento.';
      }
    });
  }

  cargarCajaActiva(): void {
    this.cargando = true;

    this.cajaApi.obtenerProvisionActivaUsuario().subscribe({
      next: data => {
        this.provisionActiva = data;
        this.idCaja = data.idCaja;
        this.idAgencia = data.idAgencia;
        this.fechaContable = data.fechaContable;
        this.cargando = false;
      },
      error: err => {
        this.error = err?.error?.message || 'El usuario no tiene una caja/provisión abierta.';
        this.cargando = false;
      }
    });
  }

  buscarCuentas(): void {
    this.limpiarMensajes();
    this.cuentas = [];
    this.cuenta = null;
    this.previewData = null;
    this.respuesta = null;

    if (!this.idAgencia || this.idAgencia <= 0) {
      this.error = 'La agencia es obligatoria para buscar cuentas.';
      return;
    }

    const tieneFiltro =
      this.filtros.documento.trim()
      || this.filtros.nombres.trim()
      || this.filtros.primerApellido.trim()
      || this.filtros.segundoApellido.trim();

    if (!tieneFiltro) {
      this.error = 'Digite al menos un criterio de búsqueda.';
      return;
    }

    this.cargando = true;

    this.api.buscarCuentas({
      idAgencia: this.idAgencia,
      documento: this.filtros.documento,
      nombres: this.filtros.nombres,
      primerApellido: this.filtros.primerApellido,
      segundoApellido: this.filtros.segundoApellido
    }).subscribe({
      next: data => {
        this.cuentas = data || [];
        this.cargando = false;
      },
      error: err => {
        this.error = err?.error?.message || 'No fue posible buscar las cuentas.';
        this.cargando = false;
      }
    });
  }

  seleccionarCuenta(item: CajaCapturaDepositosCuenta): void {
    this.cuenta = item;
    this.idCuentaAhorro = item.idCuentaAhorro;
    this.previewData = null;
    this.respuesta = null;
    this.mensaje = '';
    this.error = '';
  }

  limpiarBusqueda(): void {
    this.filtros = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };

    this.cuentas = [];
  }

  totalCheques(): number {
    return this.cheques.reduce(
      (total, item) => total + Number(item.valorCheque || 0),
      0
    );
  }

  totalMovimiento(): number {
    return Number(this.valorEfectivo || 0)
      + this.totalCheques();
  }

  documentoSoporteTexto(): string {

    if (!this.cuenta?.tipoDocumentoSoporte) {
      return 'Sin documento activo';
    }

    return (
      this.cuenta.tipoDocumentoSoporte
      + ' / '
      + (this.cuenta.numeroInicialLibreta || '')
      + ' - '
      + (this.cuenta.numeroFinalLibreta || '')
    );
  }

  verMovimientos(): void {

    if (!this.cuenta) {
      return;
    }

    this.mostrarExtracto =
      !this.mostrarExtracto;
  }

  tieneCuentaConjunta(): boolean {

    return !!this.cuenta?.cuentaConjuntaReal;
  }

  tieneApoderado(): boolean {

    return !!(
      this.cuenta?.nombrePoder
      && this.cuenta?.documentoPoder
    );
  }

  agregarCheque(): void {
    this.cheques.push({
      codigoBanco: '',
      numeroCheque: '',
      valorCheque: 0,
      observacion: ''
    });
  }

  eliminarCheque(index: number): void {
    this.cheques.splice(index, 1);

    if (this.cheques.length === 0) {
      this.agregarCheque();
    }
  }

  generarPreview(): void {
    this.respuesta = null;

    if (
      !this.cuenta
      || !this.idCuentaAhorro
      || !this.idCaja
      || !this.idAgencia
      || !this.tipoMovimiento
      || !this.numeroComprobante
      || this.totalMovimiento() <= 0
    ) {
      this.error = '';
      return;
    }

    const request = this.construirRequest();

    if (!request) {
      return;
    }

    this.cargando = true;

    this.api.preview(request).subscribe({
      next: data => {
        this.previewData = data;
        this.error = '';
        this.cargando = false;
      },
      error: err => {
        this.previewData = null;
        this.error = err?.error?.message || 'No fue posible generar el preview.';
        this.cargando = false;
      }
    });
  }

  aplicar(): void {
    this.limpiarMensajes();

    const request = this.construirRequest();

    if (!request) {
      return;
    }

    const cuentaActual = this.cuenta;
    const numeroComprobanteActual = this.numeroComprobante;
    const esRetiroActual = this.esRetiro();

    this.aplicando = true;

    this.api.aplicar(request).subscribe({
      next: data => {

        const mensajeOk =
          data.mensaje || 'Movimiento aplicado correctamente.';

        if (data.requiereFormatoLavadoActivos && cuentaActual) {

          this.lavadoActivosData = {
            modulo: '05',
            proceso: 'CAPTURA_DEPOSITOS',
            idOrigen: data.movimientosCaja?.[0] || null,

            fechaTransaccion: data.fechaContable,
            fechaContable: data.fechaContable,

            tipoTransaccion: esRetiroActual ? 'EGRESO' : 'INGRESO',

            valorTransaccion: data.valorTotal,

            idAgencia: this.idAgencia,
            codigoAgencia: cuentaActual.codigoAgencia,
            nombreAgencia: cuentaActual.nombreAgencia,

            idDatosPersonal: cuentaActual.idDatosPersonal,
            tipoDocumento: cuentaActual.tipoDocumento,
            documento: data.documento,
            nombreCompleto: data.nombreAsociado,

            codigoProducto: cuentaActual.codigoForma,
            descripcionProducto: cuentaActual.nombreForma,

            numeroProducto:
              (cuentaActual.codigoCuenta || '').trim(),
            numeroComprobante: numeroComprobanteActual,

            documentoRealiza: data.documento,
            nombreBeneficiario: data.nombreAsociado
          };

          this.mostrarLavadoActivos = true;

          this.mensaje =
            `${mensajeOk} Debe diligenciar el formato de lavado de activos.`;

          this.aplicando = false;

          return;
        }

        this.limpiar();

        this.mensaje = mensajeOk;
        this.aplicando = false;
      },
      error: err => {
        this.error =
          err?.error?.message
          || 'No fue posible aplicar el movimiento.';

        this.aplicando = false;
      }
    });
  }

  private construirRequest(): CajaCapturaDepositosRequest | null {
    if (!this.cuenta || !this.idCuentaAhorro) {
      this.error = 'Debe seleccionar una cuenta.';
      return null;
    }

    if (!this.idCaja || this.idCaja <= 0) {
      this.error = 'La caja es obligatoria.';
      return null;
    }

    if (!this.idAgencia || this.idAgencia <= 0) {
      this.error = 'La agencia es obligatoria.';
      return null;
    }

    if (!this.tipoMovimiento) {
      this.error = 'Seleccione el tipo de movimiento.';
      return null;
    }

    if (!this.numeroComprobante || !/^\d+$/.test(this.numeroComprobante.trim())) {
      this.error = 'El número del desprendible/libreta u orden debe ser numérico.';
      return null;
    }

    const tipo = this.tiposMovimiento.find(
      item => item.codigoMovimiento === this.tipoMovimiento
    );

    if (!tipo) {
      this.error = 'El tipo de movimiento seleccionado no es válido.';
      return null;
    }

    const codigoOperacion =
      tipo.accionMovimiento === 'S'
        ? 'CONSIGNACION_AHORRO'
        : 'RETIRO_AHORRO';

    const valorChequesDigitado = Number(this.valorCheques || 0);
    const totalDetalleCheques = this.totalCheques();

    if (valorChequesDigitado > 0 && totalDetalleCheques !== valorChequesDigitado) {
      this.error = 'El total del detalle de cheques debe coincidir con el valor digitado en cheques.';
      return null;
    }

    if (this.totalMovimiento() <= 0) {
      this.error = 'El valor total del movimiento debe ser mayor a cero.';
      return null;
    }

    const chequesValidos = valorChequesDigitado > 0
      ? this.cheques.filter(item =>
          item.codigoBanco?.trim()
          || item.numeroCheque?.trim()
          || Number(item.valorCheque || 0) > 0
        )
      : [];

    return {
      idCaja: this.idCaja,
      idAgencia: this.idAgencia,
      fechaContable: this.fechaContable,
      idCuentaAhorro: this.idCuentaAhorro,
      codigoOperacion,
      tipoMovimiento: this.tipoMovimiento,
      tipoComprobante: 'CJ',
      numeroComprobante: this.numeroComprobante.trim(),
      valorEfectivo: Number(this.valorEfectivo || 0),
      valorCheques: valorChequesDigitado,
      valorTransferencias: 0,
      cheques: chequesValidos
    };
  }

  limpiar(): void {
    this.error = '';
    this.mensaje = '';

    this.idCuentaAhorro = null;
    this.tipoMovimiento = '';
    this.numeroComprobante = '';

    this.valorEfectivo = 0;
    this.valorCheques = 0;

    this.cheques = [
      {
        codigoBanco: '',
        numeroCheque: '',
        valorCheque: 0,
        observacion: ''
      }
    ];

    this.cuenta = null;
    this.previewData = null;
    this.respuesta = null;
  }

  private limpiarMensajes(): void {
    this.error = '';
    this.mensaje = '';
  }

  urlFirma(nombre?: string | null): string {
    if (!nombre) {
      return '';
    }

    return `${environment.apiUrl}/shared/archivos/firmas/${nombre}`;
  }

  saldoFinalEstimado(): number {
    if (!this.cuenta) {
      return 0;
    }

    const tipo = this.tiposMovimiento.find(
      item => item.codigoMovimiento === this.tipoMovimiento
    );

    if (tipo?.accionMovimiento === 'R') {
      return Number(this.cuenta.saldoActual || 0) - this.totalMovimiento();
    }

    return Number(this.cuenta.saldoActual || 0) + this.totalMovimiento();
  }

  esRetiro(): boolean {
    const tipo = this.tiposMovimiento.find(
      item => item.codigoMovimiento === this.tipoMovimiento
    );

    return tipo?.accionMovimiento === 'R';
  }

  esIngreso(): boolean {
    const tipo = this.tiposMovimiento.find(
      item => item.codigoMovimiento === this.tipoMovimiento
    );

    return tipo?.accionMovimiento === 'S';
  }

  cerrarLavadoActivos(): void {
    this.mostrarLavadoActivos = false;
  }

  lavadoActivosGuardado(event: any): void {

    this.mostrarLavadoActivos = false;
    this.lavadoActivosData = null;

    this.limpiar();

    this.mensaje =
      event?.mensaje ||
      'Formato de lavado de activos guardado correctamente.';
  }

}
