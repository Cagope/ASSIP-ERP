import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';

import {
  finalize
} from 'rxjs';

import {
  MovimientosInteragenciaApi,
  MovimientoInteragenciaCheque,
  MovimientoInteragenciaCuenta,
  MovimientoInteragenciaPreview,
  MovimientoInteragenciaRequest
} from './movimientos-interagencia.api';

import {
  VincularCajaApi,
  CajaProvisionActivaDTO
} from '../provisiones/vincular-caja/vincular-caja.api';

import {
  NumericFormatDirective
} from '../../../shared/utils/numeric-format.directive';

@Component({
  selector: 'app-movimientos-interagencia',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NumericFormatDirective
  ],
  templateUrl: './movimientos-interagencia.component.html',
  styleUrls: ['./movimientos-interagencia.component.scss']
})
export class MovimientosInteragenciaComponent implements OnInit {

  private readonly api =
    inject(MovimientosInteragenciaApi);

  private readonly cajaApi =
    inject(VincularCajaApi);

  provisionActiva: CajaProvisionActivaDTO | null = null;

  documento = '';
  nombres = '';
  primerApellido = '';
  segundoApellido = '';

  cuentas: MovimientoInteragenciaCuenta[] = [];
  cuentaSeleccionada: MovimientoInteragenciaCuenta | null = null;

  idCuentaAhorro: number | null = null;

  tipoMovimiento = '001';
  codigoOperacion = 'CONSIGNACION_AHORRO';

  numeroComprobante = '';

  valorEfectivo = 0;
  valorCheques = 0;

  cheques: MovimientoInteragenciaCheque[] = [
    {
      codigoBanco: '',
      numeroCheque: '',
      valorCheque: 0,
      observacion: ''
    }
  ];

  mostrarCheques = false;

  previewData: MovimientoInteragenciaPreview | null = null;

  cargando = false;
  buscando = false;
  consultando = false;
  aplicando = false;

  error = '';
  mensaje = '';

  ngOnInit(): void {
    this.cargarProvisionActiva();
  }

  cargarProvisionActiva(): void {

    this.cargando = true;
    this.error = '';

    this.cajaApi.obtenerProvisionActivaUsuario()
      .pipe(
        finalize(() => this.cargando = false)
      )
      .subscribe({
        next: data => {
          this.provisionActiva = data;
        },
        error: err => {

          console.error(err);

          this.error =
            err?.error?.message
            || 'El usuario no tiene una caja/provisión activa.';
        }
      });
  }

  buscarCuentas(): void {

    this.error = '';
    this.mensaje = '';
    this.previewData = null;
    this.cuentas = [];
    this.cuentaSeleccionada = null;
    this.idCuentaAhorro = null;

    if (!this.provisionActiva?.idAgencia) {
      this.error = 'No existe una agencia activa.';
      return;
    }

    if (
      !this.documento.trim()
      && !this.nombres.trim()
      && !this.primerApellido.trim()
      && !this.segundoApellido.trim()
    ) {
      this.error =
        'Debe ingresar al menos un criterio de búsqueda.';
      return;
    }

    this.buscando = true;

    this.api.buscarCuentas(
      this.provisionActiva.idAgencia,
      this.documento,
      this.nombres,
      this.primerApellido,
      this.segundoApellido
    )
      .pipe(
        finalize(() => this.buscando = false)
      )
      .subscribe({
        next: data => {

          this.cuentas = data || [];

          if (this.cuentas.length === 0) {
            this.error =
              'No se encontraron cuentas para los criterios ingresados.';
          }
        },
        error: err => {

          console.error(err);

          this.error =
            err?.error?.message
            || 'No fue posible buscar cuentas.';
        }
      });
  }

  seleccionarCuentaDirecta(
    cuenta: MovimientoInteragenciaCuenta
  ): void {

    this.cuentaSeleccionada = cuenta;
    this.idCuentaAhorro = cuenta.idCuentaAhorro ?? null;
    this.previewData = null;
    this.error = '';
    this.mensaje = '';
  }

  cambiarTipoMovimiento(): void {

    this.previewData = null;

    if (this.tipoMovimiento === '551') {
      this.codigoOperacion = 'RETIRO_AHORRO';
      this.valorCheques = 0;
      this.cheques = [
        {
          codigoBanco: '',
          numeroCheque: '',
          valorCheque: 0,
          observacion: ''
        }
      ];
      this.mostrarCheques = false;
      return;
    }

    this.codigoOperacion = 'CONSIGNACION_AHORRO';
  }

  consultar(): void {

    this.error = '';
    this.mensaje = '';
    this.previewData = null;

    const request =
      this.construirRequest();

    if (!request) {
      return;
    }

    this.consultando = true;

    this.api.preview(request)
      .pipe(
        finalize(() => this.consultando = false)
      )
      .subscribe({
        next: data => {

          this.previewData = data;

          if (data.errores && data.errores.length > 0) {
            this.error = data.errores.join(' ');
          } else {
            this.mensaje =
              data.mensaje || 'Movimiento listo para aplicar.';
          }
        },
        error: err => {

          console.error(err);

          this.error =
            err?.error?.message
            || 'No fue posible consultar el movimiento.';
        }
      });
  }

  aplicar(): void {

    this.error = '';
    this.mensaje = '';

    const request =
      this.construirRequest();

    if (!request) {
      return;
    }

    if (!this.previewData?.permiteAplicar) {
      this.error =
        'Debe consultar un movimiento válido antes de aplicar.';
      return;
    }

    const ok = confirm(
      '¿Aplicar el movimiento interagencia?'
    );

    if (!ok) {
      return;
    }

    this.aplicando = true;

    this.api.aplicar(request)
      .pipe(
        finalize(() => this.aplicando = false)
      )
      .subscribe({
        next: resp => {

          this.mensaje =
            resp.mensaje || 'Movimiento aplicado correctamente.';

          this.limpiarTodo();
        },
        error: err => {

          console.error(err);

          this.error =
            err?.error?.message
            || 'No fue posible aplicar el movimiento.';
        }
      });
  }

  construirRequest(): MovimientoInteragenciaRequest | null {

    if (!this.provisionActiva?.idCaja) {
      this.error = 'No hay caja activa.';
      return null;
    }

    if (!this.provisionActiva?.idProvision) {
      this.error = 'No hay provisión activa.';
      return null;
    }

    if (!this.provisionActiva?.idAgencia) {
      this.error = 'No hay agencia activa.';
      return null;
    }

    if (!this.provisionActiva?.fechaContable) {
      this.error = 'No hay fecha contable activa.';
      return null;
    }

    if (!this.idCuentaAhorro) {
      this.error = 'Seleccione una cuenta.';
      return null;
    }

    if (!this.tipoMovimiento) {
      this.error = 'Seleccione el tipo de movimiento.';
      return null;
    }

    if (!this.numeroComprobante.trim()) {
      this.error = 'Digite el número de comprobante.';
      return null;
    }

    if (this.totalMovimiento() <= 0) {
      this.error = 'Digite un valor mayor a cero.';
      return null;
    }

    if (
      this.esRetiro()
      && Number(this.valorCheques || 0) > 0
    ) {
      this.error =
        'Los retiros interagencia solo pueden realizarse en efectivo.';
      return null;
    }

    return {
      idCaja: this.provisionActiva.idCaja,
      idProvision: this.provisionActiva.idProvision,
      idAgenciaCaja: this.provisionActiva.idAgencia,
      fechaContable: this.provisionActiva.fechaContable,

      idCuentaAhorro: Number(this.idCuentaAhorro),

      codigoOperacion: this.codigoOperacion,
      tipoMovimiento: this.tipoMovimiento,
      tipoComprobante: 'CJ',
      numeroComprobante: this.numeroComprobante.trim(),

      valorEfectivo: Number(this.valorEfectivo || 0),
      valorCheques: Number(this.valorCheques || 0),

      cheques: Number(this.valorCheques || 0) > 0
        ? this.cheques
        : []
    };
  }

  agregarCheque(): void {
    this.cheques.push({
      codigoBanco: '',
      numeroCheque: '',
      valorCheque: 0,
      observacion: ''
    });
  }

  eliminarCheque(
    index: number
  ): void {

    if (this.cheques.length === 1) {
      return;
    }

    this.cheques.splice(index, 1);
    this.valorCheques = this.totalCheques();
    this.previewData = null;
  }

  totalCheques(): number {
    return this.cheques.reduce(
      (total, item) =>
        total + Number(item.valorCheque || 0),
      0
    );
  }

  sincronizarTotalCheques(): void {
    this.valorCheques = this.totalCheques();
    this.previewData = null;
  }

  totalMovimiento(): number {
    return Number(this.valorEfectivo || 0)
      + Number(this.valorCheques || 0);
  }

  saldoFinalEstimado(): number {

    const saldo =
      Number(this.cuentaSeleccionada?.saldoActual || 0);

    const valor =
      this.totalMovimiento();

    return this.esRetiro()
      ? saldo - valor
      : saldo + valor;
  }

  esIngreso(): boolean {
    return this.codigoOperacion === 'CONSIGNACION_AHORRO';
  }

  esRetiro(): boolean {
    return this.codigoOperacion === 'RETIRO_AHORRO';
  }

  limpiarMovimiento(): void {
    this.previewData = null;
    this.numeroComprobante = '';
    this.valorEfectivo = 0;
    this.valorCheques = 0;
    this.cheques = [
      {
        codigoBanco: '',
        numeroCheque: '',
        valorCheque: 0,
      }
    ];
    this.mostrarCheques = false;
  }

  limpiarTodo(): void {
    this.documento = '';
    this.nombres = '';
    this.primerApellido = '';
    this.segundoApellido = '';

    this.cuentas = [];
    this.cuentaSeleccionada = null;
    this.idCuentaAhorro = null;

    this.tipoMovimiento = '001';
    this.codigoOperacion = 'CONSIGNACION_AHORRO';

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

    this.mostrarCheques = false;

    this.previewData = null;
    this.error = '';
    this.mensaje = '';
  }

  naturalezaTexto(): string {
    return this.esRetiro()
      ? 'Retiro'
      : 'Consignación';
  }

  generarPreview(): void {

    this.previewData = null;

    if (
      !this.cuentaSeleccionada
      || !this.idCuentaAhorro
      || !this.provisionActiva?.idCaja
      || !this.provisionActiva?.idAgencia
      || !this.tipoMovimiento
      || !this.numeroComprobante.trim()
      || this.totalMovimiento() <= 0
    ) {
      this.error = '';
      return;
    }

    const request =
      this.construirRequest();

    if (!request) {
      return;
    }

    this.consultando = true;

    this.api.preview(request).subscribe({
      next: data => {
        this.previewData = data;
        this.error = '';
        this.mensaje = data.mensaje || '';
        this.consultando = false;
      },
      error: err => {
        this.previewData = null;
        this.error =
          err?.error?.message
          || 'No fue posible generar el preview.';
        this.consultando = false;
      }
    });
  }

}
