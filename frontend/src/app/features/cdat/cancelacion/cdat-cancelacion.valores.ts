import {
  CdatCancelacionEntradaDTO,
  CdatCancelacionFiltroDTO,
  CdatCancelacionItemDTO
} from './cdat-cancelacion.api';

export abstract class CdatCancelacionValores {

  error = '';
  cargando = false;
  procesando = false;

  filtros: CdatCancelacionFiltroDTO = this.initFiltros();
  resultados: CdatCancelacionItemDTO[] = [];
  cuentasAhorroDisponibles: any[] = [];

  cdat: CdatCancelacionItemDTO | null = null;
  preview: any = null;

  // =============================
  // EFECTIVO / CAJA
  // =============================

  valorEfectivoEntrada = 0;
  valorEfectivoSalida = 0;

  idCajaEntrada: number | null = null;
  idCajaSalida: number | null = null;

  cajasAbiertas: any[] = [];

  // =============================
  // DEPÓSITOS
  // =============================

  mostrarDepositosEntrada = false;
  mostrarDepositosSalida = false;

  depositosEntrada: any[] = [];
  depositosSalida: any[] = [];

  nuevoDepositoEntrada: any = this.initDeposito();
  nuevoDepositoSalida: any = this.initDeposito();

  // =============================
  // CHEQUES
  // =============================

  chequesEntrada: any[] = [];
  chequesSalida: any[] = [];

  nuevoChequeEntrada = this.initCheque();
  nuevoChequeSalida = this.initCheque();

  // =============================
  // BANCOS
  // =============================

  bancosEntrada: any[] = [];
  bancosSalida: any[] = [];

  nuevoBancoEntrada = this.initBanco();
  nuevoBancoSalida = this.initBanco();

  // =============================
  // TRASLADOS
  // =============================

  trasladosEntrada: any[] = [];
  trasladosSalida: any[] = [];

  nuevoTrasladoEntrada = this.initTraslado();
  nuevoTrasladoSalida = this.initTraslado();

  model: CdatCancelacionEntradaDTO = this.initModel();

  initFiltros(): CdatCancelacionFiltroDTO {
    return {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: '',
      codigoCdat: ''
    };
  }

  initDeposito(): any {
    return {
      idCuentaAhorro: null,
      codigoCuenta: '',
      saldoDisponible: 0,
      valorDebitar: 0
    };
  }

  initCheque(): any {
    return {
      codigoBanco: '',
      numeroCheque: '',
      valorCheque: 0
    };
  }

  initBanco(): any {
    return {
      idCatalogoCuentaBanco: null,
      codigoCuentaBanco: '',
      nombreCuentaBanco: '',
      valorBanco: 0
    };
  }

  initTraslado(): any {
    return {
      idCatalogoCuentaTraslado: null,
      codigoCuentaTraslado: '',
      nombreCuentaTraslado: '',
      valorTraslado: 0
    };
  }

  initModel(): CdatCancelacionEntradaDTO {
    return {

      idCuentaCdat: 0,

      idAgencia: 0,
      idDatosPersonal: 0,

      fechaProceso: new Date().toISOString().substring(0, 10),
      fechaLiquidacion: new Date().toISOString().substring(0, 10),

      tipoComprobante: '',
      numeroComprobante: '',

      valorRenovacion: 0,

      mediosPagoEntrada: null,
      mediosPagoSalida: null,

      observacion: '',

      // =========================
      // NUEVO CDAT RENOVACIÓN
      // =========================

      fechaAperturaNuevoCdat: null,
      fechaVencimientoNuevoCdat: null,

      plazoMesesNuevoCdat: null,
      plazoDiasNuevoCdat: null,

      tasaNominalAnualNuevoCdat: null,
      tasaEfectivaAnualNuevoCdat: null,

      amortizacionDepositoNuevoCdat: null,
      retencionFuenteNuevoCdat: true,

      idCuentaAhorroNuevoCdat: null,

      observacionNuevoCdat: null
    };
  }

  requiereCajaEntrada(): boolean {
    return Number(this.valorEfectivoEntrada || 0) > 0 ||
      this.totalChequesEntrada() > 0;
  }

  requiereCajaSalida(): boolean {
    return Number(this.valorEfectivoSalida || 0) > 0 ||
      this.totalChequesSalida() > 0;
  }

  totalChequesEntrada(): number {
    return this.chequesEntrada
      .reduce((total, ch) => total + Number(ch.valorCheque || 0), 0);
  }

  totalChequesSalida(): number {
    return this.chequesSalida
      .reduce((total, ch) => total + Number(ch.valorCheque || 0), 0);
  }

  totalDepositosEntrada(): number {
    return this.depositosEntrada
      .reduce((total, d) => total + Number(d.valorDebitar || 0), 0);
  }

  totalDepositosSalida(): number {
    return this.depositosSalida
      .reduce((total, d) => total + Number(d.valorDebitar || 0), 0);
  }

  totalBancosEntrada(): number {
    return this.bancosEntrada
      .reduce((total, b) => total + Number(b.valorBanco || 0), 0);
  }

  totalBancosSalida(): number {
    return this.bancosSalida
      .reduce((total, b) => total + Number(b.valorBanco || 0), 0);
  }

  totalTrasladosEntrada(): number {
    return this.trasladosEntrada
      .reduce((total, t) => total + Number(t.valorTraslado || 0), 0);
  }

  totalTrasladosSalida(): number {
    return this.trasladosSalida
      .reduce((total, t) => total + Number(t.valorTraslado || 0), 0);
  }

  totalMediosEntrada(): number {
    return Number(this.valorEfectivoEntrada || 0)
      + this.totalChequesEntrada()
      + this.totalDepositosEntrada()
      + this.totalBancosEntrada()
      + this.totalTrasladosEntrada();
  }

  totalMediosSalida(): number {
    return Number(this.valorEfectivoSalida || 0)
      + this.totalChequesSalida()
      + this.totalDepositosSalida()
      + this.totalBancosSalida()
      + this.totalTrasladosSalida();
  }

  limpiarEstado(): void {
    this.error = '';
    this.cdat = null;
    this.preview = null;
    this.resultados = [];
    this.cuentasAhorroDisponibles = [];
    this.filtros = this.initFiltros();
    this.model = this.initModel();

    this.valorEfectivoEntrada = 0;
    this.valorEfectivoSalida = 0;
    this.idCajaEntrada = null;
    this.idCajaSalida = null;
    this.cajasAbiertas = [];

    this.depositosEntrada = [];
    this.depositosSalida = [];
    this.nuevoDepositoEntrada = this.initDeposito();
    this.nuevoDepositoSalida = this.initDeposito();

    this.chequesEntrada = [];
    this.chequesSalida = [];
    this.nuevoChequeEntrada = this.initCheque();
    this.nuevoChequeSalida = this.initCheque();

    this.bancosEntrada = [];
    this.bancosSalida = [];
    this.nuevoBancoEntrada = this.initBanco();
    this.nuevoBancoSalida = this.initBanco();

    this.trasladosEntrada = [];
    this.trasladosSalida = [];
    this.nuevoTrasladoEntrada = this.initTraslado();
    this.nuevoTrasladoSalida = this.initTraslado();
  }

  // =====================================================
  // SUGERENCIAS AUTOMÁTICAS
  // =====================================================

  valorEsperadoEntrada(): number {

    if (!this.cdat) {
      return 0;
    }

    const disponible = Number(this.cdat.valorDisponible || 0);
    const renovacion = Number(this.model.valorRenovacion || 0);

    return renovacion > disponible
      ? renovacion - disponible
      : 0;
  }

  valorEsperadoSalida(): number {

    if (!this.cdat) {
      return 0;
    }

    const disponible = Number(this.cdat.valorDisponible || 0);
    const renovacion = Number(this.model.valorRenovacion || 0);

    return disponible > renovacion
      ? disponible - renovacion
      : 0;
  }

  saldoPendienteEntrada(): number {

    const pendiente =
      this.valorEsperadoEntrada()
      - this.totalMediosEntrada();

    return pendiente > 0 ? pendiente : 0;
  }

  saldoPendienteSalida(): number {

    const pendiente =
      this.valorEsperadoSalida()
      - this.totalMediosSalida();

    return pendiente > 0 ? pendiente : 0;
  }

  sugerirEfectivoEntrada(): void {

    if (this.valorEfectivoEntrada > 0) {
      return;
    }

    this.valorEfectivoEntrada =
      this.valorEsperadoEntrada();
  }

  sugerirEfectivoSalida(): void {

    if (this.valorEfectivoSalida > 0) {
      return;
    }

    this.valorEfectivoSalida =
      this.valorEsperadoSalida();
  }

  sugerirChequeEntrada(): void {

    if (this.nuevoChequeEntrada.valorCheque > 0) {
      return;
    }

    this.nuevoChequeEntrada.valorCheque =
      this.saldoPendienteEntrada();
  }

  sugerirChequeSalida(): void {

    if (this.nuevoChequeSalida.valorCheque > 0) {
      return;
    }

    this.nuevoChequeSalida.valorCheque =
      this.saldoPendienteSalida();
  }

  sugerirBancoEntrada(): void {

    if (this.nuevoBancoEntrada.valorBanco > 0) {
      return;
    }

    this.nuevoBancoEntrada.valorBanco =
      this.saldoPendienteEntrada();
  }

  sugerirBancoSalida(): void {

    if (this.nuevoBancoSalida.valorBanco > 0) {
      return;
    }

    this.nuevoBancoSalida.valorBanco =
      this.saldoPendienteSalida();
  }

  sugerirDepositoEntrada(): void {

    if (this.nuevoDepositoEntrada.valorDebitar > 0) {
      return;
    }

    this.nuevoDepositoEntrada.valorDebitar =
      this.saldoPendienteEntrada();
  }

  sugerirDepositoSalida(): void {

    if (this.nuevoDepositoSalida.valorDebitar > 0) {
      return;
    }

    this.nuevoDepositoSalida.valorDebitar =
      this.saldoPendienteSalida();
  }

  sugerirTrasladoEntrada(): void {

    if (this.nuevoTrasladoEntrada.valorTraslado > 0) {
      return;
    }

    this.nuevoTrasladoEntrada.valorTraslado =
      this.saldoPendienteEntrada();
  }

  sugerirTrasladoSalida(): void {

    if (this.nuevoTrasladoSalida.valorTraslado > 0) {
      return;
    }

    this.nuevoTrasladoSalida.valorTraslado =
      this.saldoPendienteSalida();
  }

}
