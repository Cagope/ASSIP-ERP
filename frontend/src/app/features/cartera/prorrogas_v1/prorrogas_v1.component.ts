import { CommonModule } from '@angular/common';
import { Component, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProrrogasNumericFormatDirective } from './prorrogas-numeric-format.directive';
import { Subject, takeUntil } from 'rxjs';
import { ConsultaCreditosAsociado, ConsultaCreditosBuscadorComponent } from '../consulta-creditos/buscador/consulta-creditos-buscador.component';
import { ProrrogasV1Api } from './prorrogas_v1.api';
import {
  ProrrogasCaja, ProrrogasCreditoDetalle, ProrrogasCreditoResumen,
  ProrrogasCuentaAhorro, ProrrogasMedioRecaudo, ProrrogasSolicitud,
  ProrrogasLiquidacion, ProrrogasMovimientoContable, ProrrogasResultado
} from './prorrogas_v1.models';

/** Liquida, previsualiza y confirma prórrogas con validación del backend. */
@Component({
  selector: 'app-prorrogas-v1',
  standalone: true,
  imports: [CommonModule, FormsModule, ProrrogasNumericFormatDirective, ConsultaCreditosBuscadorComponent],
  templateUrl: './prorrogas_v1.component.html',
  styleUrls: ['./prorrogas_v1.component.scss']
})
export class ProrrogasV1Component implements OnDestroy {
  private readonly destruir$ = new Subject<void>();
  asociado: ConsultaCreditosAsociado | null = null;
  creditos: ProrrogasCreditoResumen[] = [];
  credito: ProrrogasCreditoDetalle | null = null;
  idCreditoSeleccionado: number | null = null;
  mesesProrroga: 1 | 2 | 3 = 1;
  interesCorriente = 0;
  interesMora = 0;
  capital = 0;
  seguro = 0;
  aportes = 0;
  fondoGarantia = 0;
  otrosConceptos = 0;
  observacion = '';
  cajas: ProrrogasCaja[] = [];
  cuentasAhorro: ProrrogasCuentaAhorro[] = [];
  idCaja: number | null = null;
  efectivo = 0;
  ahorros: { idCuentaAhorro: number | null; valor: number }[] = [];
  transferencia = 0;
  private idCuentaCxc: number | null = null;
  codigoCuentaCxc: string | null = null;
  private errorCuentaCxc: string | null = null;
  fechaContable = (() => { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`; })();
  liquidacion: ProrrogasLiquidacion | null = null;
  movimientos: ProrrogasMovimientoContable[] = [];
  resultado: ProrrogasResultado | null = null;
  vistaPreviaVigente = false;
  private solicitudPrevisualizada: string | null = null;
  cargando = false;
  error: string | null = null;
  aviso: string | null = null;

  constructor(private readonly api: ProrrogasV1Api) {}

  /** Reinicia los datos del crédito al cambiar de asociado. */
  seleccionarAsociado(asociado: ConsultaCreditosAsociado): void {
    this.asociado = asociado;
    this.creditos = [];
    this.limpiarCredito();
    this.error = null;
    this.cargando = true;
    this.api.listarCreditosPorAsociado(asociado.idDatosPersonal)
      .pipe(takeUntil(this.destruir$)).subscribe({
        next: creditos => {
          // Prórrogas admite únicamente créditos activos con saldo de capital.
          this.creditos = (creditos ?? []).filter(credito =>
            credito.codigoEstadoCartera === 'A' &&
            Number(credito.saldoActual ?? 0) > 0
          );
          this.cargando = false;
        },
        error: () => { this.error = 'No fue posible consultar los créditos del asociado.'; this.cargando = false; }
      });
  }

  /** Sugiere un concepto descriptivo sin exigir que el funcionario lo modifique. */
  private conceptoSugerido(): string {
    if (!this.credito) return '';
    const pagare = this.credito.pagareCartera || 'SIN PAGARÉ';
    return `PRÓRROGA DE ${this.diasProrroga} DÍAS - PAGARÉ ${pagare} - FECHA CONTABLE ${this.fechaContable}`;
  }

  /** Actualiza el concepto sugerido al cambiar la duración, sin sobrescribir ediciones. */
  cambiarPlazoProrroga(): void {
    if (this.credito && (!this.observacion.trim() || this.observacion.startsWith('PRÓRROGA DE '))) {
      this.observacion = this.conceptoSugerido();
    }
    this.importeModificado();
  }

  /** Consulta el crédito y los medios permitidos desde servicios ya existentes. */
  seleccionarCredito(id: number | null): void {
    this.limpiarCredito();
    if (id == null) return;
    this.idCreditoSeleccionado = id;
    this.cargando = true;
    this.api.consultarCredito(id).pipe(takeUntil(this.destruir$)).subscribe({
      next: credito => {
        this.credito = credito;
        this.observacion = this.conceptoSugerido();
        this.cargando = false;
        this.cargarMedios(credito);
        this.liquidar();
      },
      error: () => { this.error = 'No fue posible consultar el detalle del crédito.'; this.cargando = false; }
    });
  }

  private cargarMedios(credito: ProrrogasCreditoDetalle): void {
    this.idCuentaCxc = null;
    this.codigoCuentaCxc = null;
    this.errorCuentaCxc = null;
    this.api.consultarCuentaCxc(credito.idCarteraCredito)
      .pipe(takeUntil(this.destruir$)).subscribe({
        next: cuenta => {
          if (this.credito?.idCarteraCredito !== credito.idCarteraCredito) return;
          this.idCuentaCxc = cuenta.idCuentaPorPagar;
          this.codigoCuentaCxc = cuenta.codigoCuenta;
          this.importeModificado();
        },
        error: e => {
          if (this.credito?.idCarteraCredito !== credito.idCarteraCredito) return;
          this.errorCuentaCxc = e?.error?.message ||
            'No existe una cuenta CxC activa para el recaudo bancario de esta agencia.';
          this.importeModificado();
        }
      });
    this.api.listarCuentasAhorro(credito.idCarteraCredito)
      .pipe(takeUntil(this.destruir$)).subscribe({
        next: cuentas => this.cuentasAhorro = cuentas,
        error: () => this.aviso = 'No se pudieron consultar las cuentas de Ahorros.'
      });
    this.api.listarCajas(credito.idAgencia)
      .pipe(takeUntil(this.destruir$)).subscribe({
        next: cajas => this.cajas = cajas,
        error: () => this.aviso = 'No se pudieron consultar las cajas disponibles.'
      });
  }

  /** Limpia selección, valores y medios para evitar reutilizar un recaudo anterior. */
  limpiarCredito(): void {
    this.credito = null;
    this.liquidacion = null;
    this.solicitudPrevisualizada = null;
    this.movimientos = [];
    this.resultado = null;
    this.vistaPreviaVigente = false;
    this.idCreditoSeleccionado = null;
    this.cajas = [];
    this.cuentasAhorro = [];
    this.idCaja = null;
    this.efectivo = 0;
    this.ahorros = [];
    this.transferencia = 0;
    this.idCuentaCxc = null;
    this.codigoCuentaCxc = null;
    this.errorCuentaCxc = null;
    this.interesCorriente = 0;
    this.interesMora = 0;
    this.capital = 0;
    this.seguro = 0;
    this.aportes = 0;
    this.fondoGarantia = 0;
    this.otrosConceptos = 0;
    this.mesesProrroga = 1;
    this.observacion = '';
    this.error = null;
    this.aviso = null;
  }

  limpiarBusqueda(): void {
    this.asociado = null;
    this.creditos = [];
    this.limpiarCredito();
  }

  /** Obtiene del servidor los importes obligatorios; nunca los calcula el navegador. */
  liquidar(): void {
    if (!this.credito || !this.fechaContable) return;
    this.cargando = true;
    this.error = null;
    this.liquidacion = null;
    this.solicitudPrevisualizada = null;
    this.movimientos = [];
    this.vistaPreviaVigente = false;
    this.api.liquidar(this.credito.idCarteraCredito, this.fechaContable)
      .pipe(takeUntil(this.destruir$)).subscribe({
        next: l => {
          this.liquidacion = l;
          this.interesCorriente = l.valorInteresCorriente;
          this.interesMora = l.valorInteresMora;
          this.seguro = l.valorSeguro;
          this.capital = 0;
          this.aportes = l.valorAportes;
          this.fondoGarantia = l.valorFondoGarantia;
          this.otrosConceptos = l.valorOtrosConceptos;
          this.efectivo = this.idCaja != null ? this.totalConceptos : 0;
          this.actualizarRestante();
          this.cargando = false;
        },
        error: e => { this.error = e?.error?.message || 'No fue posible liquidar la prórroga.'; this.cargando = false; }
      });
  }

  /** Previsualiza y valida el recaudo sin aplicar ningún movimiento financiero. */
  previsualizar(): void {
    const error = this.validacionLocal;
    if (error) { this.error = error; return; }
    const solicitud = this.construirSolicitud();
    if (!solicitud) return;
    this.cargando = true;
    this.error = null;
    this.vistaPreviaVigente = false;
    const firma = JSON.stringify({ solicitud, fechaContable: this.fechaContable });
    this.api.previsualizar(solicitud, this.fechaContable)
      .pipe(takeUntil(this.destruir$)).subscribe({
        next: movimientos => {
          this.movimientos = movimientos;
          this.solicitudPrevisualizada = firma;
          this.vistaPreviaVigente = true;
          this.cargando = false;
        },
        error: e => { this.error = e?.error?.message || 'No fue posible previsualizar el CC.'; this.cargando = false; }
      });
  }

  /** Confirma únicamente después de una previsualización; el backend vuelve a validar. */
  confirmar(): void {
    if (!this.vistaPreviaVigente || this.validacionLocal || this.cargando) return;
    const solicitud = this.construirSolicitud();
    if (!solicitud || JSON.stringify({ solicitud, fechaContable: this.fechaContable }) !== this.solicitudPrevisualizada) {
      this.error = 'Los datos cambiaron. Genere nuevamente la vista previa del comprobante.';
      this.vistaPreviaVigente = false;
      return;
    }
    this.cargando = true;
    this.error = null;
    this.api.confirmar(solicitud, this.fechaContable)
      .pipe(takeUntil(this.destruir$)).subscribe({
        next: resultado => {
          this.resultado = resultado;
          this.vistaPreviaVigente = false;
          this.cargando = false;
          this.aviso = `Prórroga aplicada. Comprobante ${resultado.tipoComprobante}-${resultado.numeroComprobante}.`;
        },
        error: e => { this.error = e?.error?.message || 'No fue posible confirmar la prórroga.'; this.cargando = false; }
      });
  }

  /** Reparte el importe restante sin modificar otros débitos capturados. */
  private actualizarRestante(): void {
    this.transferencia = Math.max(0, this.totalConceptos - this.efectivo - this.totalAhorros);
    this.importeModificado();
  }

  get totalAhorros(): number {
    return this.ahorros.reduce((suma, a) => suma + Number(a.valor || 0), 0);
  }

  get diferenciaRecaudo(): number {
    return this.totalConceptos - this.totalMedios;
  }

  /** Cambiar la caja no debe perder los importes ya capturados. */
  cambiarCaja(): void {
    if (this.idCaja == null) this.efectivo = 0;
    else if (this.efectivo === 0) this.efectivo = Math.max(0, this.totalConceptos - this.totalAhorros);
    this.actualizarRestante();
  }

  cambiarEfectivo(): void {
    this.actualizarRestante();
  }

  /** Los conceptos cambian el total y obligan a recalcular el remanente. */
  actualizarConceptos(): void {
    this.actualizarRestante();
  }

  /** Sugiere solo el saldo no asignado a Caja ni a otras cuentas, sin alterar sus importes. */
  cambiarCuentaAhorro(indice: number): void {
    const fila = this.ahorros[indice];
    if (!fila) return;
    const cuenta = this.cuentasAhorro.find(c => c.idCuentaAhorro === fila.idCuentaAhorro);
    const otros = this.ahorros.reduce((s, a, i) => s + (i === indice ? 0 : Number(a.valor || 0)), 0);
    const disponible = Math.max(0, this.totalConceptos - Number(this.efectivo || 0) - otros);
    fila.valor = cuenta ? Math.min(disponible, Math.max(0, Number(cuenta.saldoActual))) : 0;
    this.actualizarRestante();
  }

  cambiarValorAhorro(): void {
    this.actualizarRestante();
  }

  cuentasDisponiblesParaFila(indice: number): ProrrogasCuentaAhorro[] {
    const usadas = new Set(this.ahorros.filter((_, i) => i !== indice)
      .map(a => a.idCuentaAhorro).filter((id): id is number => id != null));
    return this.cuentasAhorro.filter(c => !usadas.has(c.idCuentaAhorro));
  }

  agregarCuenta(): void {
    if (this.ahorros.length >= this.cuentasAhorro.length) return;
    this.ahorros.push({ idCuentaAhorro: null, valor: 0 });
    this.actualizarRestante();
  }

  quitarCuenta(indice: number): void {
    this.ahorros.splice(indice, 1);
    this.actualizarRestante();
  }

  get totalConceptos(): number {
    return [this.interesCorriente, this.interesMora, this.capital, this.seguro,
      this.aportes, this.fondoGarantia, this.otrosConceptos]
      .reduce((total, valor) => total + (Number(valor) || 0), 0);
  }

  get totalMedios(): number {
    return (Number(this.efectivo) || 0) + (Number(this.transferencia) || 0)
      + this.ahorros.reduce((total, cuenta) => total + (Number(cuenta.valor) || 0), 0);
  }

  /** Formato de pesos colombianos; el modelo conserva números enteros. */
  formatoPesos(valor: number): string {
    return Number.isSafeInteger(valor) && valor >= 0
      ? new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 }).format(valor)
      : '';
  }

  /** Invalida la previsualización cuando cambia cualquier importe del recaudo. */
  importeModificado(): void {
    this.vistaPreviaVigente = false;
    this.solicitudPrevisualizada = null;
    this.movimientos = [];
    this.error = null;
  }

  get diasProrroga(): number { return this.mesesProrroga * 30; }

  /** Proyección informativa de fechas; solo el backend determina la fecha definitiva. */
  fechaProyectada(fecha: string | null): string | null {
    if (!fecha) return null;
    const partes = fecha.split('-').map(Number);
    if (partes.length !== 3 || partes.some(n => !Number.isFinite(n))) return null;
    const fechaCalculada = new Date(Date.UTC(partes[0], partes[1] - 1, partes[2]));
    fechaCalculada.setUTCDate(fechaCalculada.getUTCDate() + this.diasProrroga);
    return fechaCalculada.toISOString().slice(0, 10);
  }

  /** Estructura exacta del request del servicio Java, aún sin endpoint público de prórrogas. */
  construirSolicitud(): ProrrogasSolicitud | null {
    if (!this.credito) return null;
    const medios: ProrrogasMedioRecaudo[] = [];
    if (this.efectivo > 0 && this.idCaja != null) {
      medios.push({ tipoMedio: 'CAJA', valor: this.efectivo, idCaja: this.idCaja,
        idCuentaAhorro: null, idCuentaPorPagar: null });
    }
    for (const cuenta of this.ahorros) {
      if (cuenta.valor > 0 && cuenta.idCuentaAhorro != null) {
        medios.push({ tipoMedio: 'AHORROS', valor: cuenta.valor, idCaja: null,
          idCuentaAhorro: cuenta.idCuentaAhorro, idCuentaPorPagar: null });
      }
    }
    if (this.transferencia > 0 && this.idCuentaCxc != null) {
      medios.push({ tipoMedio: 'CXP', valor: this.transferencia, idCaja: null,
        idCuentaAhorro: null, idCuentaPorPagar: this.idCuentaCxc });
    }
    return {
      idCarteraCredito: this.credito.idCarteraCredito,
      mesesProrroga: this.mesesProrroga,
      valorInteresCorriente: Number(this.interesCorriente) || 0,
      valorInteresMora: Number(this.interesMora) || 0,
      valorCapital: Number(this.capital) || 0,
      valorSeguro: Number(this.seguro) || 0,
      valorAportes: Number(this.aportes) || 0,
      valorFondoGarantia: Number(this.fondoGarantia) || 0,
      valorOtrosConceptos: Number(this.otrosConceptos) || 0,
      mediosRecaudo: medios,
      observacion: this.observacion.trim()
    };
  }

  get validacionLocal(): string | null {
    if (!this.credito) return 'Seleccione un crédito.';
    if (!this.liquidacion) return 'Primero liquide el crédito a la fecha contable.';
    if (this.credito.codigoEstadoCartera !== 'A' || (this.credito.saldoActual ?? 0) <= 0)
      return 'El crédito debe estar activo y tener saldo.';
    const importes = [this.interesCorriente, this.interesMora, this.capital, this.seguro,
      this.aportes, this.fondoGarantia, this.otrosConceptos, this.efectivo, this.transferencia,
      ...this.ahorros.map(a => a.valor)];
    if (importes.some(v => !Number.isSafeInteger(Number(v)) || Number(v) < 0))
      return 'Todos los valores deben ser pesos enteros no negativos.';
    if (this.interesCorriente !== this.liquidacion.valorInteresCorriente || this.interesMora !== this.liquidacion.valorInteresMora)
      return 'Los intereses no coinciden con la liquidación del servidor.';
    if (this.capital >= (this.credito.saldoActual ?? 0))
      return 'El abono a capital debe ser inferior al saldo.';
    if (this.efectivo + this.totalAhorros > this.totalConceptos)
      return 'La suma de Caja y Ahorros supera el valor de la prórroga.';
    if (this.totalConceptos !== this.totalMedios)
      return 'El total de medios debe coincidir exactamente con los conceptos.';
    if (this.efectivo > 0 && this.idCaja == null) return 'Seleccione una Caja.';
    if (this.ahorros.some(a => a.valor > 0 && a.idCuentaAhorro == null))
      return 'Seleccione las cuentas de Ahorros para cada abono.';
    const ids = this.ahorros.filter(a => a.valor > 0).map(a => a.idCuentaAhorro);
    if (new Set(ids).size !== ids.length) return 'No repita la misma cuenta de Ahorros.';
    if (this.ahorros.some(a => a.valor > 0 && a.valor > (this.cuentasAhorro.find(c => c.idCuentaAhorro === a.idCuentaAhorro)?.saldoActual ?? 0)))
      return 'Un débito supera el saldo de la cuenta de Ahorros.';
    if (this.transferencia > 0 && this.idCuentaCxc == null)
      return this.errorCuentaCxc || 'Consultando la cuenta CxC parametrizada para esta agencia.';
    if (this.aportes > 0 || this.fondoGarantia > 0 || this.otrosConceptos > 0)
      return 'Falta parametrización contable para aportes, fondo de garantía u otros conceptos.';
    return null;
  }

  /** Libera las suscripciones al destruir la pantalla. */
  ngOnDestroy(): void {
    this.destruir$.next();
    this.destruir$.complete();
  }
}
