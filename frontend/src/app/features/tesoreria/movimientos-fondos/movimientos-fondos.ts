import {
  Component,
  DestroyRef,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MovimientosFondosApi } from './movimientos-fondos.api';
import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';

import {
  CajaConProvision,
  CajaControlRetiro,
  CajaMovimiento,
  ChequeDisponible,
  CuentaBancariaMovimiento,
  CuentaBancariaRetiro,
  CuentaBancariaTraslado,
  EfectivoCaja,
  ModalidadTrasladoCaja,
  MedioTrasladoBancario,
  MovimientoFondoPreview,
  MovimientoFondoRequest,
  TipoMovimientoFondo
} from './movimientos-fondos.models';

import {
  AgenciaSesion,
  SessionService
} from '../../../core/auth/session.service';

@Component({
  selector: 'app-movimientos-fondos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NumericFormatDirective
  ],
  templateUrl: './movimientos-fondos.html',
  styleUrl: './movimientos-fondos.scss'
})
export class MovimientosFondosComponent implements OnInit {

  private readonly api = inject(MovimientosFondosApi);
  private readonly destroyRef = inject(DestroyRef);
  private readonly session = inject(SessionService);

  // =========================================================
  // ESTADO
  // =========================================================

  cargando = false;
  consultando = false;
  mensajeError = '';
  mensajeInformativo = '';

  preview: MovimientoFondoPreview | null = null;
  aplicando = false;
  movimientoAplicado = false;
  numeroComprobanteGenerado = '';
  private solicitudPrevisualizada = '';

  idComprobantePendiente: number | null = null;
  retiroPreparado = false;
  trasladoBancarioPreparado = false;

  // =========================================================
  // CATÁLOGOS
  // =========================================================

  readonly tiposMovimiento: {
    valor: TipoMovimientoFondo;
    etiqueta: string;
    disponible: boolean;
  }[] = [
    {
      valor: 'CONSIGNACION',
      etiqueta: 'Consignación bancaria',
      disponible: true
    },
    {
      valor: 'RETIRO',
      etiqueta: 'Retiro bancario',
      disponible: true
    },
    {
      valor: 'TRASLADO_CAJAS',
      etiqueta: 'Movimiento entre cajas',
      disponible: true
    },
    {
      valor: 'TRASLADO_BANCARIO',
      etiqueta: 'Traslado bancario',
      disponible: true
    }
  ];

  readonly modalidadesTraslado: {
    valor: ModalidadTrasladoCaja;
    etiqueta: string;
  }[] = [
    {
      valor: 'PROVISION',
      etiqueta: 'Provisión a caja operativa'
    },
    {
      valor: 'REEMBOLSO',
      etiqueta: 'Reembolso a caja control'
    }
  ];

  cuentasBancarias: CuentaBancariaMovimiento[] = [];
  chequesDisponibles: ChequeDisponible[] = [];
  cajasMovimiento: CajaMovimiento[] = [];
  cajasConProvision: CajaConProvision[] = [];
  cajasTrasladoAbiertas: CajaConProvision[] = [];
  agencias: AgenciaSesion[] = [];

  cuentasRetiro: CuentaBancariaRetiro[] = [];
  cajasControlRetiro: CajaControlRetiro[] = [];
  cuentasOrigenTraslado: CuentaBancariaTraslado[] = [];
  cuentasDestinoTraslado: CuentaBancariaTraslado[] = [];

  readonly mediosTrasladoBancario: { valor: MedioTrasladoBancario; etiqueta: string }[] = [
    { valor: 'EFECTIVO', etiqueta: 'Físico (efectivo entre bancos)' },
    { valor: 'BRE_B', etiqueta: 'Bre-B' },
    { valor: 'TRANSFERENCIA_NORMAL', etiqueta: 'Transferencia bancaria normal' }
  ];

  // =========================================================
  // FORMULARIO
  // =========================================================

  idAgencia: number | null = null;

  tipoMovimiento: TipoMovimientoFondo = 'TRASLADO_CAJAS';
  modalidadTrasladoCaja: ModalidadTrasladoCaja = 'PROVISION';
  medioTrasladoBancario: MedioTrasladoBancario = 'TRANSFERENCIA_NORMAL';

  fechaMovimiento = this.fechaLocal();

  numeroSoporte = '';
  observacion = '';

  valor: number | null = null;

  idCuentaBancariaDestino: number | null = null;
  idCuentaBancariaOrigen: number | null = null;

  idCajaOrigen: number | null = null;
  idCajaDestino: number | null = null;

  idsChequesSeleccionados: number[] = [];
  efectivos: EfectivoCaja[] = [];

  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    this.agencias = this.session.getAgencias();

    const agenciaActiva = this.session.getAgenciaActiva();

    if (
      agenciaActiva?.idAgencia != null &&
      this.agencias.some(
        agencia => agencia.idAgencia === agenciaActiva.idAgencia
      )
    ) {
      this.idAgencia = agenciaActiva.idAgencia;
      this.cargarCatalogos();
    }
  }

  // =========================================================
  // PROPIEDADES DE PRESENTACIÓN
  // =========================================================

  get esConsignacion(): boolean {
    return this.tipoMovimiento === 'CONSIGNACION';
  }

  get esRetiro(): boolean {
    return this.tipoMovimiento === 'RETIRO';
  }

  get esTrasladoCajas(): boolean {
    return this.tipoMovimiento === 'TRASLADO_CAJAS';
  }

  get esTrasladoBancario(): boolean {
    return this.tipoMovimiento === 'TRASLADO_BANCARIO';
  }

  provisionesConsultadas = false;
  consultandoProvisiones = false;

  cajaHabilitada(idCaja: number): boolean {
    const cajas = this.esTrasladoCajas
      ? this.cajasTrasladoAbiertas
      : this.cajasConProvision;

    return this.provisionesConsultadas && cajas.some(
      caja => caja.idCaja === idCaja
    );
  }

  private seleccionarCajasUnicas(): void {
    if (!this.esTrasladoCajas || !this.provisionesConsultadas) {
      return;
    }

    const origen = this.cajasOrigen;
    const destino = this.cajasDestino;

    this.idCajaOrigen =
      origen.length === 1 ? origen[0].idCaja : null;

    this.idCajaDestino =
      destino.length === 1 ? destino[0].idCaja : null;
  }

  get cajasSinProvisionAbierta(): CajaMovimiento[] {
    return this.cajasMovimiento.filter(
      caja => !this.cajaHabilitada(caja.idCaja)
    );
  }

  get cajasControl(): CajaMovimiento[] {
    return this.cajasMovimiento.filter(
      caja =>
        caja.tipoCaja?.toUpperCase() === 'CONTROL' &&
        this.cajaHabilitada(caja.idCaja)
    );
  }

  get cajasOperativas(): CajaMovimiento[] {
    return this.cajasMovimiento.filter(
      caja =>
        caja.tipoCaja?.toUpperCase() === 'OPERATIVA' &&
        this.cajaHabilitada(caja.idCaja)
    );
  }

  get cajasOrigen(): CajaMovimiento[] {
    return this.modalidadTrasladoCaja === 'PROVISION'
      ? this.cajasControl
      : this.cajasOperativas;
  }

  get cajasDestino(): CajaMovimiento[] {
    return this.modalidadTrasladoCaja === 'PROVISION'
      ? this.cajasOperativas
      : this.cajasControl;
  }

  get valorChequesSeleccionados(): number {
    return this.chequesDisponibles
      .filter(cheque =>
        this.idsChequesSeleccionados.includes(
          cheque.idChequeRecibido
        )
      )
      .reduce(
        (total, cheque) =>
          total + Number(cheque.valorCheque || 0),
        0
      );
  }

  get valorEfectivoConsignacion(): number {
    return this.efectivos.reduce(
      (total, item) => total + Number(item.valor || 0),
      0
    );
  }

  get valorTotalConsignacion(): number {
    return this.valorChequesSeleccionados +
      this.valorEfectivoConsignacion;
  }

  // =========================================================
  // CAMBIOS DEL FORMULARIO
  // =========================================================

  cambiarTipoMovimiento(): void {

    this.limpiarResultado();

    this.idCuentaBancariaDestino = null;
    this.idCuentaBancariaOrigen = null;

    this.cuentasRetiro = [];
    this.cajasControlRetiro = [];
    this.cuentasOrigenTraslado = [];
    this.cuentasDestinoTraslado = [];
    this.medioTrasladoBancario = 'TRANSFERENCIA_NORMAL';

    this.idCajaOrigen = null;
    this.idCajaDestino = null;

    this.idsChequesSeleccionados = [];
    this.efectivos = [];
    this.valor = null;

    if (this.esTrasladoCajas) {
      this.modalidadTrasladoCaja = 'PROVISION';
    }

    if (this.idAgencia) {
      this.cargarCatalogos();
    }
  }

  cambiarModalidadTraslado(): void {

    this.idCajaOrigen = null;
    this.idCajaDestino = null;

    this.seleccionarCajasUnicas();
    this.limpiarResultado();
  }

  cambiarDatos(): void {
    this.limpiarResultado();
  }

  cambiarAgencia(): void {

    this.limpiarResultado();

    this.cuentasBancarias = [];
    this.cuentasRetiro = [];
    this.cajasControlRetiro = [];
    this.cuentasOrigenTraslado = [];
    this.cuentasDestinoTraslado = [];

    this.chequesDisponibles = [];
    this.cajasMovimiento = [];
    this.cajasConProvision = [];
    this.cajasTrasladoAbiertas = [];

    this.provisionesConsultadas = false;

    this.idCuentaBancariaDestino = null;
    this.idCuentaBancariaOrigen = null;

    this.idCajaOrigen = null;
    this.idCajaDestino = null;

    this.idsChequesSeleccionados = [];
    this.efectivos = [];

    if (!this.idAgencia || this.idAgencia <= 0) {
      return;
    }

    this.cargarCatalogos();
  }

  cambiarFecha(): void {

    this.limpiarResultado();

    this.cajasConProvision = [];
    this.cajasTrasladoAbiertas = [];
    this.provisionesConsultadas = false;
    this.efectivos = [];

    if (!this.esRetiro && !this.esTrasladoBancario) {
      this.idCajaOrigen = null;
      this.idCajaDestino = null;

      if (this.idAgencia) {
        this.cargarCajasConProvision();
      }
    }
  }

  // =========================================================
  // CONSULTAS
  // =========================================================

  cargarCatalogos(): void {

    if (!this.idAgencia || this.idAgencia <= 0) {
      this.mensajeError = 'Seleccione una agencia válida.';
      return;
    }

    if (this.esRetiro) {
      this.cargarCatalogosRetiro();
      return;
    }

    if (this.esTrasladoBancario) {
      this.cargarCatalogosTrasladoBancario();
      return;
    }

    const idAgencia = this.idAgencia;

    this.consultando = true;
    this.mensajeError = '';

    this.api.listarCajasMovimiento(idAgencia)
      .pipe(
        finalize(() => this.consultando = false),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: cajas => {
          if (this.idAgencia !== idAgencia) {
            return;
          }

          this.cajasMovimiento = cajas;
          this.seleccionarCajasUnicas();
        },
        error: error => this.mostrarError(error)
      });

    this.api.listarCuentasBancarias(idAgencia)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: cuentas => {
          if (this.idAgencia === idAgencia) {
            this.cuentasBancarias = cuentas;
          }
        },
        error: error => this.mostrarError(error)
      });

    this.api.listarCheques(idAgencia)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: cheques => {
          if (this.idAgencia === idAgencia) {
            this.chequesDisponibles = cheques;
          }
        },
        error: error => this.mostrarError(error)
      });

    this.cargarCajasConProvision();
  }

  /**
   * Consulta las cuentas bancarias disponibles para retiro
   * y las cajas CONTROL de la agencia.
   */
  cargarCatalogosRetiro(): void {

    if (!this.idAgencia || this.idAgencia <= 0) {
      return;
    }

    const idAgencia = this.idAgencia;

    this.cuentasRetiro = [];
    this.cajasControlRetiro = [];

    this.consultando = true;
    this.mensajeError = '';

    this.api.listarCuentasRetiro(idAgencia)
      .pipe(
        finalize(() => this.consultando = false),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: cuentas => {
          if (
            this.idAgencia === idAgencia &&
            this.esRetiro
          ) {
            this.cuentasRetiro = cuentas;
          }
        },
        error: error => this.mostrarError(error)
      });

    this.api.listarCajasControlRetiro(idAgencia)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: cajas => {
          if (
            this.idAgencia === idAgencia &&
            this.esRetiro
          ) {
            this.cajasControlRetiro = cajas;

            this.idCajaDestino =
              cajas.length === 1
                ? cajas[0].idCaja
                : null;
          }
        },
        error: error => this.mostrarError(error)
      });
  }

  // =========================================================
  // CATÁLOGOS PARA TRASLADOS BANCARIOS
  // =========================================================

  cargarCatalogosTrasladoBancario(): void {
    if (!this.idAgencia || this.idAgencia <= 0) {
      return;
    }

    const idAgencia = this.idAgencia;
    this.cuentasOrigenTraslado = [];
    this.cuentasDestinoTraslado = [];
    this.consultando = true;
    this.mensajeError = '';

    this.api.listarCuentasOrigenTrasladoBancario(idAgencia)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: cuentas => {
          if (this.idAgencia === idAgencia && this.esTrasladoBancario) {
            this.cuentasOrigenTraslado = cuentas;
          }
          this.consultando = false;
        },
        error: error => {
          this.consultando = false;
          if (this.idAgencia === idAgencia && this.esTrasladoBancario) {
            this.mostrarError(error);
          }
        }
      });

    this.api.listarCuentasDestinoTrasladoBancario(idAgencia)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: cuentas => {
          if (this.idAgencia === idAgencia && this.esTrasladoBancario) {
            this.cuentasDestinoTraslado = cuentas;
          }
        },
        error: error => {
          if (this.idAgencia === idAgencia && this.esTrasladoBancario) {
            this.mostrarError(error);
          }
        }
      });
  }

  cargarCajasConProvision(): void {

    if (!this.idAgencia || !this.fechaMovimiento) {
      return;
    }

    const idAgencia = this.idAgencia;
    const fecha = this.fechaMovimiento;

    this.provisionesConsultadas = false;
    this.consultandoProvisiones = true;

    this.cajasConProvision = [];
    this.cajasTrasladoAbiertas = [];

    this.idCajaOrigen = null;
    this.idCajaDestino = null;

    this.efectivos = [];
    this.limpiarResultado();

    const tipoConsultado = this.tipoMovimiento;

    const consulta = tipoConsultado === 'TRASLADO_CAJAS'
      ? this.api.listarCajasTrasladoAbiertas(idAgencia, fecha)
      : this.api.listarCajasConProvision(idAgencia, fecha);

    consulta
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: cajas => {
          if (
            this.idAgencia === idAgencia &&
            this.fechaMovimiento === fecha &&
            this.tipoMovimiento === tipoConsultado
          ) {
            if (tipoConsultado === 'TRASLADO_CAJAS') {
              this.cajasTrasladoAbiertas = cajas;
            } else {
              this.cajasConProvision = cajas;
            }

            this.provisionesConsultadas = true;
            this.consultandoProvisiones = false;
            this.seleccionarCajasUnicas();
          }
        },
        error: error => {
          if (
            this.idAgencia === idAgencia &&
            this.fechaMovimiento === fecha &&
            this.tipoMovimiento === tipoConsultado
          ) {
            this.consultandoProvisiones = false;
            this.provisionesConsultadas = false;
            this.mostrarError(error);
          }
        }
      });
  }

  // =========================================================
  // CHEQUES
  // =========================================================

  chequeSeleccionado(idCheque: number): boolean {
    return this.idsChequesSeleccionados.includes(idCheque);
  }

  cambiarCheque(
    idCheque: number,
    seleccionado: boolean
  ): void {

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
  }

  // =========================================================
  // EFECTIVO DE CONSIGNACIÓN
  // =========================================================

  obtenerValorEfectivo(idCaja: number): number | null {
    return this.efectivos.find(
      item => item.idCaja === idCaja
    )?.valor ?? null;
  }

  cambiarValorEfectivo(
    idCaja: number,
    valor: number | null
  ): void {

    this.limpiarResultado();

    this.efectivos = this.efectivos.filter(
      item => item.idCaja !== idCaja
    );

    const valorIngresado = Number(valor ?? 0);

    if (
      !Number.isFinite(valorIngresado) ||
      valorIngresado <= 0
    ) {
      return;
    }

    this.efectivos.push({
      idCaja,
      valor: valorIngresado
    });

    const caja = this.cajasConProvision.find(
      item => item.idCaja === idCaja
    );

    if (!caja) {
      this.mensajeError =
        'La caja seleccionada no tiene una provisión abierta.';
      return;
    }

    const disponible = Number(
      caja.efectivoDisponible ?? 0
    );

    if (valorIngresado > disponible) {
      this.mensajeError =
        `La caja ${caja.codigoCaja} tiene ` +
        `$${disponible.toLocaleString('es-CO')} disponibles. ` +
        `No puede consignar $${valorIngresado.toLocaleString('es-CO')}.`;
    }
  }


  // =========================================================
  // VISTA PREVIA CONTABLE
  // =========================================================

  /**
   * Valida la información del movimiento y solicita al backend
   * la vista previa de sus registros contables.
   *
   * La consulta no genera comprobantes ni movimientos de caja.
   */
  consultarPreview(): void {

    if (this.cargando || this.aplicando) {
      return;
    }

    this.limpiarResultado();

    const errorValidacion = this.validarFormulario();

    if (errorValidacion) {
      this.mensajeError = errorValidacion;
      return;
    }

    const request = this.construirRequest();

    this.cargando = true;

    this.api.previewContable(request)
      .pipe(
        finalize(() => this.cargando = false),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: resultado => {

          this.preview = resultado;
          this.solicitudPrevisualizada =
            JSON.stringify(request);

          this.mensajeInformativo =
            'Vista previa generada. ' +
            'No se ha registrado ningún movimiento.';
        },
        error: error => this.mostrarError(error)
      });
  }

  // =========================================================
  // REGISTRO DEFINITIVO
  // CONSIGNACIONES Y TRASLADOS ENTRE CAJAS
  // =========================================================

  /**
   * Registra definitivamente una consignación bancaria
   * o un traslado entre cajas.
   *
   * Los retiros bancarios no utilizan este método porque
   * requieren preparación y autorización posterior.
   */
  aplicarMovimiento(): void {

    if (this.esRetiro || this.esTrasladoBancario) {
      this.mensajeError =
        'Los retiros y traslados bancarios requieren preparación ' +
        'y autorización del comprobante.';
      return;
    }

    if (
      this.cargando ||
      this.aplicando ||
      this.movimientoAplicado ||
      !this.preview?.cuadrado
    ) {
      return;
    }

    const error = this.validarFormulario();

    if (error) {
      this.mensajeError = error;
      return;
    }

    const request = this.construirRequest();

    if (
      JSON.stringify(request) !==
      this.solicitudPrevisualizada
    ) {
      this.limpiarResultado();
      this.mensajeError =
        'Los datos cambiaron. ' +
        'Genere nuevamente la vista previa.';
      return;
    }

    const confirmado = window.confirm(
      `¿Confirma el registro definitivo de ` +
      `${request.tipoMovimiento} por ` +
      `$${this.preview.valorTotal.toLocaleString('es-CO')}?\n` +
      'Esta acción genera movimientos de caja ' +
      'y un comprobante contable.'
    );

    if (!confirmado) {
      return;
    }

    this.aplicando = true;
    this.mensajeError = '';

    this.api.aplicarMovimiento(request)
      .pipe(
        finalize(() => this.aplicando = false),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: resultado => {

          this.movimientoAplicado = true;

          this.numeroComprobanteGenerado =
            resultado.numeroComprobante;

          this.preview = null;

          this.mensajeInformativo =
            `Movimiento registrado correctamente. ` +
            `Comprobante ${resultado.tipoComprobante} ` +
            `${resultado.numeroComprobante}.`;

          this.cargarCatalogos();
        },
        error: error => this.mostrarError(error)
      });
  }

  // =========================================================
  // PREPARACIÓN DE RETIRO BANCARIO
  // =========================================================

  /**
   * Prepara un retiro bancario y genera su comprobante
   * contable en estado PENDIENTE.
   *
   * No contabiliza definitivamente el retiro.
   * No incrementa la provisión de la caja CONTROL.
   *
   * La autorización posterior es responsabilidad
   * del proceso de autorización de comprobantes.
   */
  prepararRetiro(): void {

    if (!this.esRetiro) {
      this.mensajeError =
        'La operación seleccionada no es un retiro bancario.';
      return;
    }

    if (
      this.cargando ||
      this.aplicando ||
      this.retiroPreparado ||
      !this.preview?.cuadrado
    ) {
      return;
    }

    const error = this.validarFormulario();

    if (error) {
      this.mensajeError = error;
      return;
    }

    const request = this.construirRequest();

    if (
      JSON.stringify(request) !==
      this.solicitudPrevisualizada
    ) {
      this.limpiarResultado();

      this.mensajeError =
        'Los datos del retiro cambiaron. ' +
        'Genere nuevamente la vista previa.';

      return;
    }

    const valorRetiro = Number(this.preview.valorTotal);

    const confirmado = window.confirm(
      `¿Confirma la preparación del retiro bancario ` +
      `por $${valorRetiro.toLocaleString('es-CO')}?\n\n` +
      'Se generará un comprobante PENDIENTE de autorización.\n' +
      'El retiro todavía no será contabilizado definitivamente.'
    );

    if (!confirmado) {
      return;
    }

    this.aplicando = true;
    this.mensajeError = '';

    this.api.prepararRetiro(request)
      .pipe(
        finalize(() => this.aplicando = false),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: resultado => {

          this.retiroPreparado = true;
          this.movimientoAplicado = true;

          this.idComprobantePendiente =
            resultado.idComprobante;

          this.preview = null;
          this.solicitudPrevisualizada = '';

          this.mensajeInformativo =
            resultado.mensaje ||
            `Retiro bancario preparado correctamente. ` +
            `Comprobante ${resultado.idComprobante} ` +
            `en estado ${resultado.estado}. ` +
            `Pendiente de autorización.`;
        },
        error: error => this.mostrarError(error)
      });
  }

  // =========================================================
  // PREPARACIÓN DE TRASLADO BANCARIO
  // =========================================================

  prepararTrasladoBancario(): void {
    if (!this.esTrasladoBancario) {
      this.mensajeError = 'La operación seleccionada no es un traslado bancario.';
      return;
    }

    if (this.cargando || this.aplicando || this.trasladoBancarioPreparado ||
        !this.preview?.cuadrado) {
      return;
    }

    const error = this.validarFormulario();
    if (error) {
      this.mensajeError = error;
      return;
    }

    const request = this.construirRequest();
    if (JSON.stringify(request) !== this.solicitudPrevisualizada) {
      this.limpiarResultado();
      this.mensajeError = 'Los datos cambiaron. Genere nuevamente la vista previa.';
      return;
    }

    const valor = Number(this.preview.valorTotal);
    if (!window.confirm(
      `¿Confirma la preparación del traslado bancario por ` +
      `$${valor.toLocaleString('es-CO')}?\n\n` +
      'Se generará un comprobante PENDIENTE de autorización.\n' +
      'No se contabilizará ni registrará movimiento de caja todavía.'
    )) {
      return;
    }

    this.aplicando = true;
    this.mensajeError = '';

    this.api.prepararTrasladoBancario(request)
      .pipe(
        finalize(() => this.aplicando = false),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: resultado => {
          this.trasladoBancarioPreparado = true;
          this.movimientoAplicado = true;
          this.idComprobantePendiente = resultado.idComprobante;
          this.preview = null;
          this.solicitudPrevisualizada = '';
          this.mensajeInformativo = resultado.mensaje ||
            `Traslado bancario preparado. Comprobante ${resultado.idComprobante} ` +
            `en estado ${resultado.estado}. Pendiente de autorización.`;
        },
        error: error => this.mostrarError(error)
      });
  }

  // =========================================================
  // VALIDACIONES
  // =========================================================

  /**
   * Valida los datos requeridos según el tipo de movimiento.
   *
   * Las consignaciones y los traslados conservan la validación
   * de provisiones abiertas.
   *
   * La preparación de un retiro no exige una provisión
   * abierta; esa validación corresponde a la autorización.
   */
  private validarFormulario(): string | null {

    if (!this.idAgencia || this.idAgencia <= 0) {
      return 'Seleccione una agencia.';
    }

    if (!this.fechaMovimiento) {
      return 'Seleccione la fecha del movimiento.';
    }

    // -------------------------------------------------------
    // RETIRO BANCARIO
    // -------------------------------------------------------

    if (this.esRetiro) {

      if (!this.idCuentaBancariaOrigen) {
        return 'Seleccione la cuenta bancaria de origen.';
      }

      const cuentaValida = this.cuentasRetiro.some(
        cuenta =>
          cuenta.idCuentaBancaria ===
          this.idCuentaBancariaOrigen
      );

      if (!cuentaValida) {
        return 'La cuenta bancaria seleccionada no está ' +
          'habilitada para retiros en esta agencia.';
      }

      if (!this.idCajaDestino) {
        return 'Seleccione la caja CONTROL de destino.';
      }

      const cajaValida = this.cajasControlRetiro.some(
        caja => caja.idCaja === this.idCajaDestino
      );

      if (!cajaValida) {
        return 'La caja seleccionada no corresponde ' +
          'a una caja CONTROL disponible en la agencia.';
      }

      const valorRetiro = Number(this.valor);

      if (
        !Number.isFinite(valorRetiro) ||
        valorRetiro <= 0
      ) {
        return 'Digite un valor de retiro mayor a cero.';
      }

      if (!this.numeroSoporte.trim()) {
        return 'Digite el número de soporte del retiro.';
      }

      if (this.numeroSoporte.trim().length > 20) {
        return 'El número de soporte no puede ' +
          'superar 20 caracteres.';
      }

      return null;
    }

    // -------------------------------------------------------
    // TRASLADO BANCARIO (SIN PROVISIONES DE CAJA)
    // -------------------------------------------------------

    if (this.esTrasladoBancario) {
      if (!this.idCuentaBancariaOrigen || !this.idCuentaBancariaDestino) {
        return 'Seleccione las cuentas bancarias de origen y destino.';
      }
      if (this.idCuentaBancariaOrigen === this.idCuentaBancariaDestino) {
        return 'Las cuentas bancarias de origen y destino deben ser diferentes.';
      }
      if (!this.cuentasOrigenTraslado.some(
        c => c.idCuentaBancaria === this.idCuentaBancariaOrigen
      )) {
        return 'La cuenta de origen no está habilitada para traslados.';
      }
      if (!this.cuentasDestinoTraslado.some(
        c => c.idCuentaBancaria === this.idCuentaBancariaDestino
      )) {
        return 'La cuenta de destino no está habilitada para recibir traslados.';
      }
      if (!this.mediosTrasladoBancario.some(
        m => m.valor === this.medioTrasladoBancario
      )) {
        return 'Seleccione un medio de traslado válido.';
      }
      if (!Number.isFinite(Number(this.valor)) || Number(this.valor) <= 0) {
        return 'Digite un valor de traslado mayor a cero.';
      }
      if (!this.numeroSoporte.trim()) {
        return 'Digite el número de soporte del traslado.';
      }
      if (this.numeroSoporte.trim().length > 20) {
        return 'El número de soporte no puede superar 20 caracteres.';
      }
      return null;
    }

    // -------------------------------------------------------
    // CONSIGNACIÓN BANCARIA
    // -------------------------------------------------------

    if (this.esConsignacion) {

      if (!this.numeroSoporte.trim()) {
        return 'Digite el número de soporte.';
      }

      if (this.numeroSoporte.trim().length > 20) {
        return 'El número de soporte no puede ' +
          'superar 20 caracteres.';
      }
    }

    // -------------------------------------------------------
    // PROVISIONES: CONSIGNACIÓN Y TRASLADO
    // -------------------------------------------------------

    if (
      !this.provisionesConsultadas ||
      this.consultandoProvisiones
    ) {
      return 'Debe verificarse la provisión abierta ' +
        'de las cajas para la fecha seleccionada.';
    }

    // -------------------------------------------------------
    // TRASLADO ENTRE CAJAS
    // -------------------------------------------------------

    if (this.esTrasladoCajas) {

      if (!this.idCajaOrigen || !this.idCajaDestino) {
        return 'Seleccione las cajas de origen y destino.';
      }

      if (this.idCajaOrigen === this.idCajaDestino) {
        return 'La caja origen y destino deben ser diferentes.';
      }

      if (!this.valor || this.valor <= 0) {
        return 'Digite un valor mayor a cero.';
      }

      const origenValido = this.cajasOrigen.some(
        caja => caja.idCaja === this.idCajaOrigen
      );

      const destinoValido = this.cajasDestino.some(
        caja => caja.idCaja === this.idCajaDestino
      );

      if (!origenValido || !destinoValido) {
        return 'Las cajas seleccionadas no corresponden ' +
          'a la modalidad o no tienen provisión ABIERTA ' +
          'para la fecha.';
      }

      return null;
    }

    // -------------------------------------------------------
    // CONSIGNACIÓN
    // -------------------------------------------------------

    if (this.esConsignacion) {

      if (!this.idCuentaBancariaDestino) {
        return 'Seleccione la cuenta bancaria destino.';
      }

      if (
        this.efectivos.some(
          item => !this.cajaHabilitada(item.idCaja)
        )
      ) {
        return 'Una caja con efectivo no tiene provisión ' +
          'ABIERTA para la fecha.';
      }

      for (const efectivo of this.efectivos) {

        const caja = this.cajasConProvision.find(
          item => item.idCaja === efectivo.idCaja
        );

        if (!caja) {
          return 'No fue posible determinar el efectivo ' +
            'disponible de la caja.';
        }

        const disponible = Number(
          caja.efectivoDisponible ?? 0
        );

        const solicitado = Number(
          efectivo.valor ?? 0
        );

        if (
          !Number.isFinite(solicitado) ||
          solicitado < 0
        ) {
          return `El valor ingresado para la caja ` +
            `${caja.codigoCaja} no es válido.`;
        }

        if (solicitado > disponible) {
          return `La caja ${caja.codigoCaja} tiene ` +
            `$${disponible.toLocaleString('es-CO')} ` +
            `disponibles. No puede consignar ` +
            `$${solicitado.toLocaleString('es-CO')}.`;
        }
      }

      if (this.valorTotalConsignacion <= 0) {
        return 'Seleccione cheques o registre efectivo ' +
          'para consignar.';
      }

      return null;
    }

    return 'Esta operación todavía no está habilitada.';
  }

  // =========================================================
  // CONSTRUIR SOLICITUD
  // =========================================================

  /**
   * Construye la solicitud correspondiente a la operación
   * seleccionada, utilizando el contrato compartido
   * con el backend.
   */
  private construirRequest(): MovimientoFondoRequest {

    const request: MovimientoFondoRequest = {
      tipoMovimiento: this.tipoMovimiento,
      idAgencia: this.idAgencia!,
      fechaMovimiento: this.fechaMovimiento,
      numeroSoporte: this.esTrasladoCajas
        ? ''
        : this.numeroSoporte.trim(),
      observacion: this.observacion.trim() || null
    };

    // -------------------------------------------------------
    // TRASLADO ENTRE CAJAS
    // -------------------------------------------------------

    if (this.esTrasladoCajas) {

      request.modalidadTrasladoCaja =
        this.modalidadTrasladoCaja;

      request.idCajaOrigen = this.idCajaOrigen;
      request.idCajaDestino = this.idCajaDestino;
      request.valor = Number(this.valor);

    // -------------------------------------------------------
    // CONSIGNACIÓN
    // -------------------------------------------------------

    } else if (this.esConsignacion) {

      request.idCuentaBancariaDestino =
        this.idCuentaBancariaDestino;

      request.idsCheques = [
        ...this.idsChequesSeleccionados
      ];

      request.efectivos = this.efectivos.map(
        item => ({ ...item })
      );

    // -------------------------------------------------------
    // RETIRO BANCARIO
    // -------------------------------------------------------

    } else if (this.esRetiro) {

      request.idCuentaBancariaOrigen =
        this.idCuentaBancariaOrigen;

      request.idCajaDestino =
        this.idCajaDestino;

      request.valor = Number(this.valor);

    // -------------------------------------------------------
    // TRASLADO BANCARIO
    // -------------------------------------------------------

    } else if (this.esTrasladoBancario) {
      request.idCuentaBancariaOrigen = this.idCuentaBancariaOrigen;
      request.idCuentaBancariaDestino = this.idCuentaBancariaDestino;
      request.medioTrasladoBancario = this.medioTrasladoBancario;
      request.valor = Number(this.valor);
    }

    return request;
  }

  // =========================================================
  // UTILIDADES
  // =========================================================

  /**
   * Limpia los datos capturados para iniciar un nuevo
   * movimiento sin modificar la operación seleccionada.
   */
  limpiarFormulario(): void {

    this.numeroSoporte = '';
    this.observacion = '';
    this.valor = null;

    this.idCuentaBancariaDestino = null;
    this.idCuentaBancariaOrigen = null;

    this.idCajaOrigen = null;
    this.idCajaDestino = null;

    this.idsChequesSeleccionados = [];
    this.efectivos = [];

    if (this.esRetiro) {

      if (this.cajasControlRetiro.length === 1) {
        this.idCajaDestino =
          this.cajasControlRetiro[0].idCaja;
      }

    } else {
      this.seleccionarCajasUnicas();
    }

    this.limpiarResultado();
  }

  /**
   * Invalida la vista previa cuando cambia un dato
   * y limpia el estado del último movimiento.
   */
  private limpiarResultado(): void {

    this.preview = null;

    this.movimientoAplicado = false;
    this.numeroComprobanteGenerado = '';

    this.idComprobantePendiente = null;
    this.retiroPreparado = false;
    this.trasladoBancarioPreparado = false;

    this.solicitudPrevisualizada = '';

    this.mensajeError = '';
    this.mensajeInformativo = '';
  }

  /**
   * Obtiene el mensaje de error del backend o,
   * en su defecto, muestra un mensaje genérico.
   */
  private mostrarError(error: unknown): void {

    const respuesta = error as {
      error?: {
        message?: string;
        mensaje?: string;
        error?: string;
      };
      message?: string;
    };

    this.mensajeError =
      respuesta?.error?.message ||
      respuesta?.error?.mensaje ||
      respuesta?.error?.error ||
      respuesta?.message ||
      'No fue posible completar la consulta.';
  }

  /**
   * Obtiene la fecha local en formato yyyy-MM-dd.
   */
  private fechaLocal(): string {

    const fecha = new Date();

    const anio = fecha.getFullYear();
    const mes = String(
      fecha.getMonth() + 1
    ).padStart(2, '0');

    const dia = String(
      fecha.getDate()
    ).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }

}
