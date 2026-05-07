import { FormControl } from '@angular/forms';
import { CdatChequeDTO, CdatFormDTO } from './cdats.api';
import { CuentaAutocompleteDTO } from '../../../shared/cuentas/cuentas.api';

export abstract class CdatsUpsertValores {

  model: CdatFormDTO = this.initModel();

  cuentaBancoCtrl = new FormControl('');
  cuentasBanco: CuentaAutocompleteDTO[] = [];

  cuentaTrasladoCtrl = new FormControl('');
  cuentasTraslado: CuentaAutocompleteDTO[] = [];

  depositos: any[] = [];
  cheques: CdatChequeDTO[] = [];
  bancos: any[] = [];
  trasladosAgencias: any[] = [];
  beneficiarios: any[] = [];

  nuevoDeposito = this.initNuevoDeposito();
  nuevoCheque: CdatChequeDTO = this.initNuevoCheque();
  nuevoBanco = this.initNuevoBanco();
  nuevoTraslado = this.initNuevoTraslado();
  nuevoBeneficiario = this.initNuevoBeneficiario();

  error = '';

  protected abstract cargarCajasAbiertas(): void;

  protected initModel(): CdatFormDTO {
    return {
      idCuentaCdat: null,
      idAgencia: null,
      idProductoCdat: null,
      codigoCdat: null,

      idDatosPersonal: null,
      idDatosPersonalCotitular: null,

      fechaAperturaCdat: '',
      fechaVencimientoCdat: null,

      plazoMeses: null,
      plazoDias: null,

      valorAperturaCdat: 0,
      saldoActualCdat: 0,

      tasaNominalAnual: 0,
      tasaEfectivaAnual: 0,
      tasaNominalMensual: 0,
      tasaEfectivaMensual: 0,

      retencionFuenteCdat: false,

      amortizacionDeposito: 'M',
      modalidadCdat: 'V',

      fechaUltimaLiquidacion: null,
      fechaProximaLiquidacion: null,
      fechaUltimoTrasladoInteres: null,
      fechaProximoTrasladoInteres: null,

      idCuentaAportes: null,
      idCuentaAhorro: null,

      cuentaConjunta: 'N',
      accionConjunta: null,

      origenCdat: 'N',
      idCuentaCdatOrigen: null,

      estadoCdat: null,

      tipoComprobante: null as any,
      numeroComprobante: '',
      fechaComprobante: null,
      idCaja: null,

      valorEfectivo: 0,
      valorCheque: 0,
      valorDepositos: 0,
      valorBanco: 0,
      valorTrasladosAgencias: 0,

      observacion: ''
    };
  }

  protected initNuevoDeposito() {
    return {
      idCuentaAhorro: null,
      codigoCuenta: '',
      saldoDisponible: 0,
      valorDebitar: 0
    };
  }

  protected initNuevoCheque(): CdatChequeDTO {
    return {
      codigoBanco: '',
      numeroCheque: '',
      valorCheque: 0
    };
  }

  protected initNuevoBanco() {
    return {
      idCatalogoCuentaBanco: null as number | null,
      codigoCuentaBanco: '',
      nombreCuentaBanco: '',
      valorBanco: 0
    };
  }

  protected initNuevoTraslado() {
    return {
      idCatalogoCuentaTraslado: null as number | null,
      codigoCuentaTraslado: '',
      nombreCuentaTraslado: '',
      valorTraslado: 0
    };
  }

  protected initNuevoBeneficiario() {
    return {
      documento: '',
      nombre: '',
      telefono: '',
      tipoParentesco: ''
    };
  }

  calcularValorCdat(): void {
    const total =
      Number(this.model.valorEfectivo || 0) +
      Number(this.model.valorCheque || 0) +
      Number(this.model.valorDepositos || 0) +
      Number(this.model.valorBanco || 0) +
      Number(this.model.valorTrasladosAgencias || 0);

    this.model.valorAperturaCdat = total;

    if (this.requiereCaja()) {
      this.cargarCajasAbiertas();
    } else {
      this.model.idCaja = null;
    }
  }

  requiereCaja(): boolean {
    return Number(this.model.valorEfectivo || 0) > 0 ||
      Number(this.model.valorCheque || 0) > 0;
  }

  agregarBeneficiario(): void {
    this.error = '';

    if (!this.nuevoBeneficiario.documento || !this.nuevoBeneficiario.nombre) {
      this.error = 'Documento y nombre del beneficiario son obligatorios.';
      return;
    }

    this.beneficiarios.push({ ...this.nuevoBeneficiario });
    this.nuevoBeneficiario = this.initNuevoBeneficiario();
  }

  eliminarBeneficiario(index: number): void {
    this.beneficiarios.splice(index, 1);
  }

  agregarCheque(): void {
    this.error = '';

    if (!this.nuevoCheque.codigoBanco?.trim()) {
      this.error = 'Debe ingresar el código del banco.';
      return;
    }

    if (!this.nuevoCheque.numeroCheque?.trim()) {
      this.error = 'Debe ingresar el número del cheque.';
      return;
    }

    if (!this.nuevoCheque.valorCheque || this.nuevoCheque.valorCheque <= 0) {
      this.error = 'El valor del cheque debe ser mayor a cero.';
      return;
    }

    this.cheques.push({ ...this.nuevoCheque });
    this.nuevoCheque = this.initNuevoCheque();
    this.recalcularTotalCheques();
  }

  eliminarCheque(index: number): void {
    this.cheques.splice(index, 1);
    this.recalcularTotalCheques();
  }

  recalcularTotalCheques(): void {
    this.model.valorCheque = this.cheques.reduce(
      (acc, x) => acc + (Number(x.valorCheque) || 0),
      0
    );

    this.calcularValorCdat();
  }

  agregarDeposito(): void {
    this.error = '';

    if (!this.nuevoDeposito.idCuentaAhorro) {
      this.error = 'Debe seleccionar la cuenta.';
      return;
    }

    if (!this.nuevoDeposito.valorDebitar || this.nuevoDeposito.valorDebitar <= 0) {
      this.error = 'Valor inválido.';
      return;
    }

    if (this.nuevoDeposito.valorDebitar > this.nuevoDeposito.saldoDisponible) {
      this.error = 'El valor supera el saldo disponible.';
      return;
    }

    this.depositos.push({ ...this.nuevoDeposito });
    this.nuevoDeposito = this.initNuevoDeposito();
    this.recalcularDepositos();
  }

  eliminarDeposito(i: number): void {
    this.depositos.splice(i, 1);
    this.recalcularDepositos();
  }

  recalcularDepositos(): void {
    this.model.valorDepositos = this.depositos.reduce(
      (sum, d) => sum + (d.valorDebitar || 0),
      0
    );

    this.calcularValorCdat();
  }

  agregarBanco(): void {
    this.error = '';

    if (!this.nuevoBanco.idCatalogoCuentaBanco) {
      this.error = 'Debe seleccionar la cuenta contable de banco.';
      return;
    }

    if (!this.nuevoBanco.valorBanco || this.nuevoBanco.valorBanco <= 0) {
      this.error = 'El valor del banco debe ser mayor a cero.';
      return;
    }

    this.bancos.push({ ...this.nuevoBanco });
    this.nuevoBanco = this.initNuevoBanco();
    this.recalcularBancos();
  }

  eliminarBanco(index: number): void {
    this.bancos.splice(index, 1);
    this.recalcularBancos();
  }

  recalcularBancos(): void {
    this.model.valorBanco = this.bancos.reduce(
      (sum, b) => sum + (Number(b.valorBanco) || 0),
      0
    );

    this.calcularValorCdat();
  }

  seleccionarCuentaBanco(c: CuentaAutocompleteDTO): void {
    const label = `${c.codigoCuenta} — ${c.nombreCuenta}`;

    this.nuevoBanco.idCatalogoCuentaBanco = c.idCuenta;
    this.nuevoBanco.codigoCuentaBanco = c.codigoCuenta;
    this.nuevoBanco.nombreCuentaBanco = c.nombreCuenta;

    this.cuentaBancoCtrl.setValue(label, { emitEvent: false });
    this.cuentasBanco = [];
  }

  agregarTraslado(): void {
    this.error = '';

    if (!this.nuevoTraslado.idCatalogoCuentaTraslado) {
      this.error = 'Debe seleccionar la cuenta de traslado.';
      return;
    }

    if (!this.nuevoTraslado.valorTraslado || this.nuevoTraslado.valorTraslado <= 0) {
      this.error = 'El valor del traslado debe ser mayor a cero.';
      return;
    }

    this.trasladosAgencias.push({ ...this.nuevoTraslado });

    this.nuevoTraslado = this.initNuevoTraslado();
    this.cuentaTrasladoCtrl.setValue('', { emitEvent: false });
    this.cuentasTraslado = [];

    this.recalcularTraslados();
  }

  eliminarTraslado(index: number): void {
    this.trasladosAgencias.splice(index, 1);
    this.recalcularTraslados();
  }

  recalcularTraslados(): void {
    this.model.valorTrasladosAgencias = this.trasladosAgencias.reduce(
      (sum, t) => sum + (Number(t.valorTraslado) || 0),
      0
    );

    this.calcularValorCdat();
  }

  seleccionarCuentaTraslado(c: CuentaAutocompleteDTO): void {
    const label = `${c.codigoCuenta} — ${c.nombreCuenta}`;

    this.nuevoTraslado.idCatalogoCuentaTraslado = c.idCuenta;
    this.nuevoTraslado.codigoCuentaTraslado = c.codigoCuenta;
    this.nuevoTraslado.nombreCuentaTraslado = c.nombreCuenta;

    this.cuentaTrasladoCtrl.setValue(label, { emitEvent: false });
    this.cuentasTraslado = [];
  }
}
