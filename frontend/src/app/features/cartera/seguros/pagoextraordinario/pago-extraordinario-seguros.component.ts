import { CommonModule } from '@angular/common';
import { Component, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, Subject, takeUntil } from 'rxjs';
import { ProrrogasNumericFormatDirective } from '../../prorrogas_v1/prorrogas-numeric-format.directive';
import { ConsultaCreditosAsociado, ConsultaCreditosBuscadorComponent } from '../../consulta-creditos/buscador/consulta-creditos-buscador.component';
import { ConsultaCreditoResumen } from '../../consulta-creditos/consulta-creditos.models';
import { PagoCuotasCajaRecaudo } from '../../pago-cuotas_v1/pago-cuotas_v1.models';
import { PagoExtraordinarioSegurosApi } from './pago-extraordinario-seguros.api';
import { PagoSeguroCuentaAhorro, PagoSeguroMedio, PagoSeguroResultado, PagoSeguroSolicitud, PagoSeguroVistaPrevia } from './pago-extraordinario-seguros.models';

interface AhorroCaptura { idCuentaAhorro: number | null; valor: number; }

/** Captura cuatro formas combinables, revisa el CC y confirma una sola disminución de seguros. */
@Component({
  selector: 'app-pago-extraordinario-seguros',
  standalone: true,
  imports: [CommonModule, FormsModule, ConsultaCreditosBuscadorComponent, ProrrogasNumericFormatDirective],
  templateUrl: './pago-extraordinario-seguros.component.html',
  styleUrls: ['./pago-extraordinario-seguros.component.scss']
})
export class PagoExtraordinarioSegurosComponent implements OnDestroy {
  private readonly destruir$ = new Subject<void>();
  private version = 0;
  private solicitudPrevia: string | null = null;
  asociadoSeleccionado: ConsultaCreditosAsociado | null = null;
  creditos: ConsultaCreditoResumen[] = [];
  creditoSeleccionado: ConsultaCreditoResumen | null = null;
  idAgencia: number | null = null;
  saldoSeguro = 0;
  cajas: PagoCuotasCajaRecaudo[] = [];
  cuentasAhorro: PagoSeguroCuentaAhorro[] = [];
  idCaja: number | null = null;
  valorCaja = 0;
  valorBancos = 0;
  valorCorreccion = 0;
  ahorros: AhorroCaptura[] = [];
  fechaMovimiento = this.fechaLocal();
  justificacion = '';
  private conceptoEditado = false;
  vistaPrevia: PagoSeguroVistaPrevia | null = null;
  resultado: PagoSeguroResultado | null = null;
  cargando = false;
  procesando = false;
  error: string | null = null;

  constructor(private readonly api: PagoExtraordinarioSegurosApi) {}

  get totalAplicado(): number {
    return this.valorCaja + this.valorBancos + this.valorCorreccion
      + this.ahorros.reduce((s, a) => s + Number(a.valor || 0), 0);
  }
  get saldoRestante(): number { return this.saldoSeguro - this.totalAplicado; }
  get totalDebitos(): number { return (this.vistaPrevia?.lineas ?? []).reduce((s, l) => s + Number(l.debito), 0); }
  get totalCreditos(): number { return (this.vistaPrevia?.lineas ?? []).reduce((s, l) => s + Number(l.credito), 0); }

  /** Selecciona un asociado y obtiene sus créditos sin solicitar identificadores internos. */
  seleccionarAsociado(asociado: ConsultaCreditosAsociado): void {
    if (this.procesando) return;
    const version = ++this.version;
    this.asociadoSeleccionado = asociado;
    this.limpiarCredito();
    this.creditos = [];
    this.error = null;
    this.cargando = true;
    this.api.listarCreditosPorAsociado(asociado.idDatosPersonal).pipe(takeUntil(this.destruir$)).subscribe({
      next: creditos => {
        if (version !== this.version) return;
        this.creditos = (creditos ?? []).filter(c => c.codigoEstadoCartera === 'A' && Number(c.saldoActual ?? 0) > 0);
        this.cargando = false;
      },
      error: e => { if (version === this.version) { this.cargando = false; this.error = this.mensajeError(e); } }
    });
  }

  /** Carga saldo, cuentas de ahorros y cajas de la agencia del crédito. */
  seleccionarCredito(credito: ConsultaCreditoResumen): void {
    if (this.procesando || !credito.idCarteraCredito) return;
    const version = ++this.version;
    this.limpiarCredito();
    this.creditoSeleccionado = credito;
    this.actualizarConceptoSugerido();
    this.error = null;
    this.cargando = true;
    const id = credito.idCarteraCredito;
    forkJoin({
      detalle: this.api.consultarDetalleCredito(id),
      saldo: this.api.consultarSaldo(id),
      ahorros: this.api.consultarCuentasAhorro(id)
    }).pipe(takeUntil(this.destruir$)).subscribe({
      next: datos => {
        if (version !== this.version) return;
        this.saldoSeguro = Number(datos.saldo);
        this.cuentasAhorro = datos.ahorros ?? [];
        this.idAgencia = datos.detalle.idAgencia;
        if (!this.idAgencia) {
          this.cargando = false;
          this.error = 'No fue posible determinar la agencia del crédito.';
          return;
        }
        this.api.listarCajasRecaudo(this.idAgencia).pipe(takeUntil(this.destruir$)).subscribe({
          next: cajas => { if (version === this.version) { this.cajas = cajas ?? []; this.cargando = false; } },
          error: e => { if (version === this.version) { this.cargando = false; this.error = this.mensajeError(e); } }
        });
      },
      error: e => { if (version === this.version) { this.cargando = false; this.error = this.mensajeError(e); } }
    });
  }

  /** Restablece solo el crédito seleccionado y permite escoger otro. */
  cambiarCredito(): void {
    if (this.procesando) return;
    ++this.version;
    this.limpiarCredito();
    this.cargando = false;
    this.error = null;
  }
  limpiarBusqueda(): void {
    if (this.procesando) return;
    ++this.version;
    this.asociadoSeleccionado = null;
    this.creditos = [];
    this.limpiarCredito();
    this.cargando = false;
    this.error = null;
  }
  private limpiarCredito(): void {
    this.creditoSeleccionado = null;
    this.idAgencia = null;
    this.saldoSeguro = 0;
    this.cajas = [];
    this.cuentasAhorro = [];
    this.idCaja = null;
    this.valorCaja = 0;
    this.valorBancos = 0;
    this.valorCorreccion = 0;
    this.ahorros = [];
    this.fechaMovimiento = this.fechaLocal();
    this.justificacion = '';
    this.conceptoEditado = false;
    this.invalidarVistaPrevia();
    this.resultado = null;
  }
  /** Evita confirmar una vista previa desactualizada después de editar importes. */
  invalidarVistaPrevia(): void {
    this.vistaPrevia = null;
    this.solicitudPrevia = null;
    this.resultado = null;
  }
  agregarAhorro(): void { this.ahorros.push({ idCuentaAhorro: null, valor: 0 }); this.cambiarMedios(); }
  quitarAhorro(index: number): void { this.ahorros.splice(index, 1); this.cambiarMedios(); }

  /** Sugiere el concepto al cambiar medios sin sobrescribir texto editado por el funcionario. */
  cambiarMedios(): void {
    this.actualizarConceptoSugerido();
    this.invalidarVistaPrevia();
  }

  /** Conserva las observaciones escritas manualmente y obliga a revisar las correcciones. */
  editarConcepto(valor: string): void {
    this.justificacion = valor;
    this.conceptoEditado = valor.trim().length > 0;
    if (!this.conceptoEditado) this.actualizarConceptoSugerido();
    this.invalidarVistaPrevia();
  }

  /** Construye un concepto uniforme con pagaré y asociado, limitado a 430 caracteres. */
  private actualizarConceptoSugerido(): void {
    if (this.conceptoEditado || !this.creditoSeleccionado) return;
    const pagare = String(this.creditoSeleccionado.pagareCartera || '').trim();
    const asociado = String(this.asociadoSeleccionado?.nombreCompleto || '').trim();
    const formas = [
      this.valorCaja > 0,
      this.valorBancos > 0,
      this.valorCorreccion > 0,
      this.ahorros.some(a => Number(a.valor) > 0)
    ].filter(Boolean).length;
    const descripcion = formas > 1
      ? 'Aplicación extraordinaria de seguros de cartera'
      : this.valorCorreccion > 0
        ? 'Corrección del saldo de seguros de cartera'
        : 'Pago extraordinario de seguros de cartera';
    this.justificacion = [descripcion, pagare ? `Pagaré ${pagare}` : '', asociado]
      .filter(Boolean).join(' - ').slice(0, 430);
  }


  /** Construye la solicitud numérica y verifica que no se supere el saldo. */
  private solicitudValida(): PagoSeguroSolicitud | null {
    this.error = null;
    if (!this.creditoSeleccionado?.idCarteraCredito || !this.fechaMovimiento) {
      this.error = 'Seleccione un crédito y una fecha válida.'; return null;
    }
    const medios: PagoSeguroMedio[] = [];
    const agregar = (forma: PagoSeguroMedio['forma'], valor: number, idCaja: number | null = null, idCuentaAhorro: number | null = null): boolean => {
      if (!Number.isFinite(valor) || valor < 0 || !Number.isInteger(valor)) {
        this.error = 'Los importes deben ser valores positivos en pesos enteros.'; return false;
      }
      if (valor > 0) medios.push({ forma, valor, idCaja, idCuentaAhorro });
      return true;
    };
    if (!agregar('CAJA', Number(this.valorCaja)) || !agregar('BANCOS', Number(this.valorBancos)) || !agregar('CORRECCION', Number(this.valorCorreccion))) return null;
    if (this.valorCaja > 0 && !this.cajas.some(c => c.idCaja === Number(this.idCaja))) {
      this.error = 'Seleccione una caja habilitada.'; return null;
    }
    const caja = medios.find(m => m.forma === 'CAJA');
    if (caja) caja.idCaja = this.idCaja;
    const usadas = new Set<number>();
    for (const ahorro of this.ahorros) {
      const valor = Number(ahorro.valor);
      if (!Number.isFinite(valor) || valor < 0 || !Number.isInteger(valor)) {
        this.error = 'Los importes de Depósitos deben ser valores positivos en pesos enteros.'; return null;
      }
      if (valor === 0) continue;
      const cuenta = this.cuentasAhorro.find(c => c.idCuentaAhorro === Number(ahorro.idCuentaAhorro));
      if (!cuenta || usadas.has(cuenta.idCuentaAhorro)) {
        this.error = 'Seleccione cuentas de ahorros diferentes y válidas.'; return null;
      }
      if (valor > Number(cuenta.saldoDisponible)) {
        this.error = `El valor supera el saldo disponible de la cuenta ${cuenta.codigoCuenta}.`; return null;
      }
      usadas.add(cuenta.idCuentaAhorro);
      if (!agregar('DEPOSITOS', valor, null, cuenta.idCuentaAhorro)) return null;
    }
    const total = medios.reduce((s, m) => s + m.valor, 0);
    if (total <= 0 || total > this.saldoSeguro) {
      this.error = 'El total debe ser mayor que cero y no superar el saldo pendiente del seguro.'; return null;
    }
    if (this.valorCorreccion > 0 && !this.conceptoEditado) {
      this.error = 'Para la corrección contra la cuenta 24, complemente el concepto con el motivo específico del ajuste.'; return null;
    }
    return { idCredito: this.creditoSeleccionado.idCarteraCredito, fechaMovimiento: this.fechaMovimiento, medios, justificacion: this.justificacion.trim() };
  }

  /** Solicita el comprobante CC sin aplicar movimientos. */
  previsualizar(): void {
    if (this.cargando || this.procesando) return;
    this.invalidarVistaPrevia();
    const solicitud = this.solicitudValida();
    if (!solicitud) return;
    this.procesando = true;
    this.api.previsualizar(solicitud).pipe(takeUntil(this.destruir$)).subscribe({
      next: vista => { this.vistaPrevia = vista; this.solicitudPrevia = JSON.stringify(solicitud); this.procesando = false; },
      error: e => { this.procesando = false; this.error = this.mensajeError(e); }
    });
  }
  /** Confirma únicamente la solicitud idéntica a la que se previsualizó. */
  confirmar(): void {
    if (!this.vistaPrevia || this.procesando) return;
    const solicitud = this.solicitudValida();
    if (!solicitud || JSON.stringify(solicitud) !== this.solicitudPrevia) {
      this.invalidarVistaPrevia();
      this.error = 'Los datos cambiaron. Genere nuevamente la vista previa.';
      return;
    }
    this.procesando = true;
    this.api.confirmar(solicitud).pipe(takeUntil(this.destruir$)).subscribe({
      next: resultado => {
        this.resultado = resultado;
        this.saldoSeguro = Number(resultado.saldoPosterior);
        this.vistaPrevia = null;
        this.solicitudPrevia = null;
        this.procesando = false;
      },
      error: e => { this.procesando = false; this.error = this.mensajeError(e); }
    });
  }
  private fechaLocal(): string {
    const hoy = new Date();
    const local = new Date(hoy.getTime() - hoy.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 10);
  }
  private mensajeError(error: any): string {
    return error?.error?.message || error?.error?.mensaje || error?.error?.detail || error?.message || 'No fue posible completar la operación.';
  }
  ngOnDestroy(): void { this.destruir$.next(); this.destruir$.complete(); }
}
