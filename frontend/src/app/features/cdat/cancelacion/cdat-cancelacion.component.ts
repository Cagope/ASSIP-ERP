import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CdatCancelacionApi, CdatCancelacionItemDTO } from './cdat-cancelacion.api';
import { CdatCancelacionValores } from './cdat-cancelacion.valores';

import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';

import {
  TiposComprobantesApi,
  TipoComprobante
} from '../../contabilidad/tipos-comprobantes/tipos-comprobantes.api';

@Component({
  selector: 'app-cdat-cancelacion',
  standalone: true,
  imports: [CommonModule, FormsModule, NumericFormatDirective],
  templateUrl: './cdat-cancelacion.component.html',
  styleUrls: ['./cdat-cancelacion.component.scss']
})
export class CdatCancelacionComponent extends CdatCancelacionValores {

  tiposComprobantes: TipoComprobante[] = [];
  amortizaciones: any[] = [];

  proximoCodigoNuevoCdat = '';
  fechaVencimientoNuevoCdatCalculada = '';

  constructor(
    private api: CdatCancelacionApi,
    private tiposComprobantesApi: TiposComprobantesApi
  ) {
    super();
  }

  async buscarCdat(): Promise<void> {
    this.error = '';
    this.preview = null;
    this.cdat = null;
    this.resultados = [];

    const tieneFiltro = Object.values(this.filtros)
      .some(v => String(v ?? '').trim() !== '');

    if (!tieneFiltro) {
      this.error = 'Ingrese al menos un criterio de búsqueda.';
      return;
    }

    this.cargando = true;

    try {
      this.resultados = await this.api.buscar(this.filtros);

      if (!this.resultados.length) {
        this.error = 'No se encontraron CDATs activos.';
      }
    } catch (err: any) {
      this.error = err?.error?.message || 'No se pudo consultar la información.';
    } finally {
      this.cargando = false;
    }
  }

  seleccionarCdat(cdat: CdatCancelacionItemDTO): void {

    this.error = '';
    this.preview = null;
    this.cdat = cdat;

    this.model.idCuentaCdat = cdat.idCuentaCdat;
    this.model.idAgencia = cdat.idAgencia;
    this.model.idDatosPersonal = cdat.idDatosPersonal;

    this.model.fechaAperturaNuevoCdat = this.model.fechaProceso;
    this.model.idCuentaAhorroNuevoCdat = cdat.idCuentaAhorro;
    this.model.retencionFuenteNuevoCdat = cdat.retencionFuenteCdat;
    this.model.amortizacionDepositoNuevoCdat = cdat.amortizacionDeposito;

    this.model.observacionNuevoCdat =
      `CDAT ORIGEN ${cdat.codigoCdat}`;

    this.model.tipoComprobante = '';
    this.model.numeroComprobante = '';

    this.calcularVencimientoNuevoCdat();

    this.cargarCuentasAhorroDisponibles(cdat);
    this.cargarTiposComprobantes();
    this.cargarAmortizaciones();
  }

  private cargarCuentasAhorroDisponibles(
    cdat: CdatCancelacionItemDTO
  ): void {

    const req = {
      schema: 'depositos',
      view: 'vw_depositos_cuentas_ahorro_total',
      filters: {
        documento: cdat.documento,
        codigo_estado: 'A'
      }
    };

    this.api.buscarCuentas(req).subscribe({

      next: (res: any) => {

        const data = res?.data ?? [];

        this.cuentasAhorroDisponibles = data.filter((c: any) =>

          Number(c.id_datos_personal)
          === Number(cdat.idDatosPersonal)

          &&

          String(c.codigo_estado ?? '')
            .trim()
            .toUpperCase() === 'A'

          &&

          Number(c.id_forma_ahorro ?? c.idFormaAhorro) !== 1
        );
      },

      error: err => {

        this.error =
          err.error?.message ||
          'No se pudieron cargar las cuentas de ahorro.';
      }
    });
  }

  cargarTiposComprobantes(): void {
    this.tiposComprobantes = [];

    if (!this.model.idAgencia) {
      return;
    }

    this.tiposComprobantesApi
      .listarPorAgencia(this.model.idAgencia)
      .subscribe({
        next: data => {
          this.tiposComprobantes = data ?? [];
        },
        error: () => {
          this.error = 'No se pudieron cargar los tipos de comprobante.';
        }
      });
  }

  cargarAmortizaciones(): void {

    this.api.listarAmortizaciones().subscribe({

      next: (data: any[]) => {

        this.amortizaciones = data ?? [];

        if (
          !this.model.amortizacionDepositoNuevoCdat
          && this.amortizaciones.length
        ) {

          this.model.amortizacionDepositoNuevoCdat =
            this.amortizaciones[0].codigoAmortizacion;
        }
      },

      error: () => {
        this.error =
          'No se pudieron cargar las amortizaciones.';
      }
    });
  }

  cargarProximoCodigoNuevoCdat(): void {

    this.proximoCodigoNuevoCdat = '';

    this.api.obtenerProximoCodigoCdat()
      .subscribe({

        next: (codigo: string) => {
          this.proximoCodigoNuevoCdat = codigo ?? '';
        },

        error: (err: any) => {
          this.error =
            err?.error ||
            'No se pudo obtener el próximo número del CDAT.';
        }
      });
  }

  onChangeValorRenovacion(): void {

    if (Number(this.model.valorRenovacion || 0) <= 0) {

      this.proximoCodigoNuevoCdat = '';

      return;
    }

    if (!this.proximoCodigoNuevoCdat) {
      this.cargarProximoCodigoNuevoCdat();
    }
  }

  cargarCajasAbiertas(): void {

    this.cajasAbiertas = [];

    if (!this.model.idAgencia || !this.model.fechaProceso) {
      return;
    }

    this.api.obtenerCajasAbiertas(
      this.model.idAgencia,
      this.model.fechaProceso
    ).subscribe({

      next: (data: any[]) => {
        this.cajasAbiertas = data ?? [];
      },

      error: (err: any) => {
        this.error =
          err.error?.message ||
          'No se pudieron cargar las cajas abiertas.';
      }
    });
  }

  async cargarConsecutivoComprobante(): Promise<void> {
    this.model.numeroComprobante = '';

    if (!this.model.idAgencia || !this.model.tipoComprobante) {
      return;
    }

    try {
      const res = await this.api.obtenerProximoComprobante(
        this.model.idAgencia,
        this.model.tipoComprobante
      );

      this.model.numeroComprobante = res?.numeroComprobante ?? '';
    } catch (err: any) {
      this.error = err?.error?.message || 'No se pudo obtener el consecutivo.';
    }
  }

  onSelectCuentaDepositoEntrada(): void {

    const cuenta = this.cuentasAhorroDisponibles.find(
      (c: any) =>
        c.id_cuenta_ahorro ===
        this.nuevoDepositoEntrada.idCuentaAhorro
    );

    if (!cuenta) {

      this.nuevoDepositoEntrada.codigoCuenta = '';
      this.nuevoDepositoEntrada.saldoDisponible = 0;

      return;
    }

    this.nuevoDepositoEntrada.codigoCuenta =
      cuenta.codigo_cuenta;

    this.nuevoDepositoEntrada.saldoDisponible =
      Number(cuenta.saldo_actual_cuenta || 0);
  }

  onSelectCuentaDepositoSalida(): void {

    const cuenta = this.cuentasAhorroDisponibles.find(
      (c: any) =>
        c.id_cuenta_ahorro ===
        this.nuevoDepositoSalida.idCuentaAhorro
    );

    if (!cuenta) {

      this.nuevoDepositoSalida.codigoCuenta = '';
      this.nuevoDepositoSalida.saldoDisponible = 0;

      return;
    }

    this.nuevoDepositoSalida.codigoCuenta =
      cuenta.codigo_cuenta;

    this.nuevoDepositoSalida.saldoDisponible =
      Number(cuenta.saldo_actual_cuenta || 0);
  }

  agregarDepositoEntrada(): void {
    if (!this.nuevoDepositoEntrada.idCuentaAhorro || !this.nuevoDepositoEntrada.valorDebitar) {
      this.error = 'Debe seleccionar cuenta y valor de entrada.';
      return;
    }

    this.depositosEntrada.push({ ...this.nuevoDepositoEntrada });
    this.nuevoDepositoEntrada = this.initDeposito();
    this.prepararMediosPago();
  }

  agregarDepositoSalida(): void {
    if (!this.nuevoDepositoSalida.idCuentaAhorro || !this.nuevoDepositoSalida.valorDebitar) {
      this.error = 'Debe seleccionar cuenta y valor de salida.';
      return;
    }

    this.depositosSalida.push({ ...this.nuevoDepositoSalida });
    this.nuevoDepositoSalida = this.initDeposito();
    this.prepararMediosPago();
  }

  eliminarDepositoEntrada(index: number): void {
    this.depositosEntrada.splice(index, 1);
    this.prepararMediosPago();
  }

  eliminarDepositoSalida(index: number): void {
    this.depositosSalida.splice(index, 1);
    this.prepararMediosPago();
  }

  agregarChequeEntrada(): void {

    this.error = '';

    if (!this.nuevoChequeEntrada.codigoBanco?.trim()) {
      this.error = 'Debe ingresar el banco.';
      return;
    }

    if (!this.nuevoChequeEntrada.numeroCheque?.trim()) {
      this.error = 'Debe ingresar el número del cheque.';
      return;
    }

    if (!this.nuevoChequeEntrada.valorCheque
      || this.nuevoChequeEntrada.valorCheque <= 0) {

      this.error = 'El valor del cheque debe ser mayor a cero.';
      return;
    }

    this.chequesEntrada.push({
      ...this.nuevoChequeEntrada
    });

    this.nuevoChequeEntrada = this.initCheque();

    this.prepararMediosPago();
  }

  eliminarChequeEntrada(index: number): void {

    this.chequesEntrada.splice(index, 1);

    this.prepararMediosPago();
  }

  agregarChequeSalida(): void {

    this.error = '';

    if (!this.nuevoChequeSalida.codigoBanco?.trim()) {
      this.error = 'Debe ingresar el banco.';
      return;
    }

    if (!this.nuevoChequeSalida.numeroCheque?.trim()) {
      this.error = 'Debe ingresar el número del cheque.';
      return;
    }

    if (!this.nuevoChequeSalida.valorCheque
      || this.nuevoChequeSalida.valorCheque <= 0) {

      this.error = 'El valor del cheque debe ser mayor a cero.';
      return;
    }

    this.chequesSalida.push({
      ...this.nuevoChequeSalida
    });

    this.nuevoChequeSalida = this.initCheque();

    this.prepararMediosPago();
  }

  eliminarChequeSalida(index: number): void {

    this.chequesSalida.splice(index, 1);

    this.prepararMediosPago();
  }

  // ======================================================
  // BANCOS ENTRADA
  // ======================================================

  agregarBancoEntrada(): void {

    this.error = '';

    if (!this.nuevoBancoEntrada.codigoCuentaBanco?.trim()) {
      this.error = 'Debe seleccionar la cuenta banco.';
      return;
    }

    if (!this.nuevoBancoEntrada.valorBanco
      || this.nuevoBancoEntrada.valorBanco <= 0) {

      this.error = 'El valor banco debe ser mayor a cero.';
      return;
    }

    this.bancosEntrada.push({
      ...this.nuevoBancoEntrada
    });

    this.nuevoBancoEntrada = this.initBanco();

    this.prepararMediosPago();
  }

  eliminarBancoEntrada(index: number): void {

    this.bancosEntrada.splice(index, 1);

    this.prepararMediosPago();
  }

  // ======================================================
  // BANCOS SALIDA
  // ======================================================

  agregarBancoSalida(): void {

    this.error = '';

    if (!this.nuevoBancoSalida.codigoCuentaBanco?.trim()) {
      this.error = 'Debe seleccionar la cuenta banco.';
      return;
    }

    if (!this.nuevoBancoSalida.valorBanco
      || this.nuevoBancoSalida.valorBanco <= 0) {

      this.error = 'El valor banco debe ser mayor a cero.';
      return;
    }

    this.bancosSalida.push({
      ...this.nuevoBancoSalida
    });

    this.nuevoBancoSalida = this.initBanco();

    this.prepararMediosPago();
  }

  eliminarBancoSalida(index: number): void {

    this.bancosSalida.splice(index, 1);

    this.prepararMediosPago();
  }

  // ======================================================
  // TRASLADOS ENTRADA
  // ======================================================

  agregarTrasladoEntrada(): void {

    this.error = '';

    if (!this.nuevoTrasladoEntrada.codigoCuentaTraslado?.trim()) {
      this.error = 'Debe seleccionar la cuenta traslado.';
      return;
    }

    if (!this.nuevoTrasladoEntrada.valorTraslado
      || this.nuevoTrasladoEntrada.valorTraslado <= 0) {

      this.error = 'El valor traslado debe ser mayor a cero.';
      return;
    }

    this.trasladosEntrada.push({
      ...this.nuevoTrasladoEntrada
    });

    this.nuevoTrasladoEntrada = this.initTraslado();

    this.prepararMediosPago();
  }

  eliminarTrasladoEntrada(index: number): void {

    this.trasladosEntrada.splice(index, 1);

    this.prepararMediosPago();
  }

  // ======================================================
  // TRASLADOS SALIDA
  // ======================================================

  agregarTrasladoSalida(): void {

    this.error = '';

    if (!this.nuevoTrasladoSalida.codigoCuentaTraslado?.trim()) {
      this.error = 'Debe seleccionar la cuenta traslado.';
      return;
    }

    if (!this.nuevoTrasladoSalida.valorTraslado
      || this.nuevoTrasladoSalida.valorTraslado <= 0) {

      this.error = 'El valor traslado debe ser mayor a cero.';
      return;
    }

    this.trasladosSalida.push({
      ...this.nuevoTrasladoSalida
    });

    this.nuevoTrasladoSalida = this.initTraslado();

    this.prepararMediosPago();
  }

  eliminarTrasladoSalida(index: number): void {

    this.trasladosSalida.splice(index, 1);

    this.prepararMediosPago();
  }


  prepararMediosPago(): void {

    this.model.mediosPagoEntrada = {

      idCaja: this.idCajaEntrada,

      valorEfectivo: this.valorEfectivoEntrada || 0,
      valorCheques: this.totalChequesEntrada(),
      valorDepositos: this.totalDepositosEntrada(),
      valorBancos: this.totalBancosEntrada(),
      valorTrasladosAgencias: this.totalTrasladosEntrada(),

      depositos: this.depositosEntrada,
      cheques: this.chequesEntrada,
      bancos: this.bancosEntrada,
      trasladosAgencias: this.trasladosEntrada
    };

    this.model.mediosPagoSalida = {

      idCaja: this.idCajaSalida,

      valorEfectivo: this.valorEfectivoSalida || 0,
      valorCheques: this.totalChequesSalida(),
      valorDepositos: this.totalDepositosSalida(),
      valorBancos: this.totalBancosSalida(),
      valorTrasladosAgencias: this.totalTrasladosSalida(),

      depositos: this.depositosSalida,
      cheques: this.chequesSalida,
      bancos: this.bancosSalida,
      trasladosAgencias: this.trasladosSalida
    };
  }

  calcularTasaEfectivaNuevoCdat(): void {
    const na = Number(this.model.tasaNominalAnualNuevoCdat || 0);

    if (na <= 0) {
      this.model.tasaEfectivaAnualNuevoCdat = 0;
      return;
    }

    const tasaDecimal = na / 100;
    const efectiva = Math.pow(1 + (tasaDecimal / 12), 12) - 1;

    this.model.tasaEfectivaAnualNuevoCdat =
      Number((efectiva * 100).toFixed(6));
  }

  calcularVencimientoNuevoCdat(): void {

    if (
      !this.model.fechaProceso
      || !this.model.plazoMesesNuevoCdat
    ) {

      this.fechaVencimientoNuevoCdatCalculada = '';

      this.model.fechaVencimientoNuevoCdat = null;
      this.model.plazoDiasNuevoCdat = null;

      return;
    }

    const fechaApertura =
      new Date(this.model.fechaProceso + 'T00:00:00');

    const meses =
      Number(this.model.plazoMesesNuevoCdat);

    const fechaVencimiento =
      new Date(fechaApertura);

    fechaVencimiento.setMonth(
      fechaVencimiento.getMonth() + meses
    );

    this.fechaVencimientoNuevoCdatCalculada =
      fechaVencimiento
        .toISOString()
        .substring(0, 10);

    this.model.fechaVencimientoNuevoCdat =
      this.fechaVencimientoNuevoCdatCalculada;

    const diferenciaMs =
      fechaVencimiento.getTime()
      - fechaApertura.getTime();

    this.model.plazoDiasNuevoCdat =
      Math.round(
        diferenciaMs / (1000 * 60 * 60 * 24)
      );
  }

  async generarPreview(): Promise<void> {
    this.error = '';
    this.preview = null;
    this.prepararMediosPago();

    const validacion = this.validarBase();
    if (validacion) {
      this.error = validacion;
      return;
    }

    this.procesando = true;

    try {
      this.preview = await this.api.preview(this.model);
    } catch (err: any) {
      this.error = err?.error?.message || 'No se pudo generar el preview.';
    } finally {
      this.procesando = false;
    }
  }

  async aplicar(): Promise<void> {
    this.error = '';
    this.prepararMediosPago();

    const validacion = this.validarBase();
    if (validacion) {
      this.error = validacion;
      return;
    }

    if (!this.preview || !this.preview.cuadrado) {
      this.error = 'Debe generar un preview contable válido antes de aplicar.';
      return;
    }

    if (!confirm('¿Desea aplicar la cancelación / renovación del CDAT?')) {
      return;
    }

    this.procesando = true;

    try {
      await this.api.aplicar(this.model);
      alert('Proceso aplicado correctamente.');
      this.limpiarEstado();
    } catch (err: any) {
      this.error = err?.error?.message || 'No se pudo aplicar el proceso.';
    } finally {
      this.procesando = false;
    }
  }

  private validarBase(): string | null {
    if (!this.cdat) return 'Debe seleccionar primero el CDAT.';
    if (!this.model.fechaProceso) return 'La fecha del proceso es obligatoria.';
    if (!this.model.tipoComprobante) return 'Debe seleccionar el tipo de comprobante.';

    if (Number(this.model.valorRenovacion || 0) > 0) {

      if (!this.model.fechaAperturaNuevoCdat) {
        return 'Debe ingresar la fecha apertura del nuevo CDAT.';
      }

      if (!this.model.plazoMesesNuevoCdat) {
        return 'Debe ingresar el plazo meses del nuevo CDAT.';
      }

      if (!this.model.fechaVencimientoNuevoCdat) {
        return 'No se pudo calcular la fecha vencimiento.';
      }

      if (!this.model.tasaNominalAnualNuevoCdat
        || this.model.tasaNominalAnualNuevoCdat <= 0) {

        return 'Debe ingresar la tasa nominal anual.';
      }

      if (!this.model.amortizacionDepositoNuevoCdat) {
        return 'Debe seleccionar la amortización.';
      }
    }

    return null;
  }

  valorDiferenciaCalculada(): number {
    const disponible = Number(this.cdat?.valorDisponible || 0);
    const renovacion = Number(this.model.valorRenovacion || 0);
    return Math.abs(renovacion - disponible);
  }

  tipoOperacionDiferencia(): string {
    const disponible = Number(this.cdat?.valorDisponible || 0);
    const renovacion = Number(this.model.valorRenovacion || 0);

    if (renovacion > disponible) return 'ENTRADA';
    if (renovacion < disponible) return 'SALIDA';
    return 'NINGUNA';
  }
}
