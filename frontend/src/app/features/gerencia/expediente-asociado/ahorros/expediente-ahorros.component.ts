import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

import {
  ExpedienteCuentaAhorro
} from '../expediente-asociado.dto';

@Component({
  selector: 'app-expediente-ahorros',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-ahorros.component.html',
  styleUrls: ['./expediente-ahorros.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteAhorrosComponent {

  @Input()
  cuentas: ExpedienteCuentaAhorro[] | null = [];

  // =========================================================
  // LISTAS
  // =========================================================

  /**
   * Todas las cuentas recibidas desde el expediente.
   *
   * Incluye aportes sociales y demás productos de captación.
   */
  get listaCompleta(): ExpedienteCuentaAhorro[] {
    return Array.isArray(this.cuentas)
      ? this.cuentas
      : [];
  }

  /**
   * Productos que corresponden realmente a cuentas de ahorro.
   *
   * El código de tipo de captación 1 identifica aportes sociales,
   * por lo cual se excluye de este componente.
   */
  get lista(): ExpedienteCuentaAhorro[] {
    return this.listaCompleta.filter(
      cuenta => !this.esCuentaAportes(cuenta)
    );
  }

  get tieneCuentas(): boolean {
    return this.cantidadCuentas > 0;
  }

  get cantidadCuentas(): number {
    return this.lista.length;
  }

  // =========================================================
  // ESTADOS DE LAS CUENTAS
  // =========================================================

  get cantidadActivas(): number {
    return this.lista.filter(
      cuenta => cuenta.activa === true
    ).length;
  }

  get cantidadCanceladas(): number {
    return this.lista.filter(
      cuenta => this.esCuentaCancelada(cuenta)
    ).length;
  }

  get cantidadBloqueadas(): number {
    return this.lista.filter(
      cuenta => cuenta.bloqueada === true
    ).length;
  }

  get cantidadEmbargadas(): number {
    return this.lista.filter(
      cuenta => cuenta.embargada === true
    ).length;
  }

  get cantidadInactivas(): number {
    return this.lista.filter(
      cuenta => cuenta.cuentaInactiva === true
    ).length;
  }

  get cantidadConRevision(): number {
    return this.lista.filter(
      cuenta => cuenta.requiereRevision === true
    ).length;
  }

  get cantidadConAlertas(): number {
    return this.lista.filter(
      cuenta => this.numero(cuenta.cantidadAlertas) > 0
    ).length;
  }

  get cantidadConMovimientosInusuales(): number {
    return this.lista.filter(
      cuenta => cuenta.movimientosInusuales === true
    ).length;
  }

  // =========================================================
  // SALDOS CONSOLIDADOS
  // =========================================================

  get saldoDisponibleTotal(): number {
    return this.sumar(
      cuenta => cuenta.saldoDisponible
    );
  }

  get saldoCanjeTotal(): number {
    return this.sumar(
      cuenta => cuenta.saldoCanje
    );
  }

  get saldoTotal(): number {
    return this.sumar(
      cuenta => cuenta.saldoTotal
    );
  }

  // =========================================================
  // MOVIMIENTO HISTÓRICO CONSOLIDADO
  // =========================================================

  get totalConsignaciones(): number {
    return this.sumar(
      cuenta => cuenta.totalConsignaciones
    );
  }

  get totalRetiros(): number {
    return this.sumar(
      cuenta => cuenta.totalRetiros
    );
  }

  get totalIntereses(): number {
    return this.sumar(
      cuenta => cuenta.totalIntereses
    );
  }

  // =========================================================
  // ENTRADAS DEL MES
  // =========================================================

  get cantidadEntradasMes(): number {
    return this.sumar(
      cuenta => cuenta.cantidadEntradasMes
    );
  }

  get valorEntradasMes(): number {
    return this.sumar(
      cuenta => cuenta.valorEntradasMes
    );
  }

  // =========================================================
  // SALIDAS DEL MES
  // =========================================================

  get cantidadSalidasMes(): number {
    return this.sumar(
      cuenta => cuenta.cantidadSalidasMes
    );
  }

  get valorSalidasMes(): number {
    return this.sumar(
      cuenta => cuenta.valorSalidasMes
    );
  }

  // =========================================================
  // MOVIMIENTO TOTAL DEL MES
  // =========================================================

  get cantidadMovimientosMes(): number {
    return this.lista.reduce(
      (total, cuenta) =>
        total + this.cantidadMovimientosCuenta(cuenta),
      0
    );
  }

  get valorMovimientosMes(): number {
    return this.lista.reduce(
      (total, cuenta) =>
        total + this.valorMovimientosCuenta(cuenta),
      0
    );
  }

  /**
   * Devuelve la cantidad mensual consolidada de una cuenta.
   *
   * Si el backend entrega cantidadMovimientosMes, se utiliza ese
   * valor. En caso contrario, se calcula con entradas y salidas.
   */
  cantidadMovimientosCuenta(
    cuenta: ExpedienteCuentaAhorro
  ): number {

    if (this.tieneValorNumerico(
      cuenta.cantidadMovimientosMes
    )) {
      return this.numero(
        cuenta.cantidadMovimientosMes
      );
    }

    return (
      this.numero(cuenta.cantidadEntradasMes) +
      this.numero(cuenta.cantidadSalidasMes)
    );
  }

  /**
   * Devuelve el valor total movilizado durante el mes.
   *
   * El valor representa la suma absoluta de entradas y salidas,
   * no el movimiento neto.
   */
  valorMovimientosCuenta(
    cuenta: ExpedienteCuentaAhorro
  ): number {

    if (this.tieneValorNumerico(
      cuenta.valorMovimientosMes
    )) {
      return this.numero(
        cuenta.valorMovimientosMes
      );
    }

    return (
      this.numero(cuenta.valorEntradasMes) +
      this.numero(cuenta.valorSalidasMes)
    );
  }

  /**
   * Movimiento neto mensual:
   *
   * entradas - salidas
   */
  movimientoNetoCuenta(
    cuenta: ExpedienteCuentaAhorro
  ): number {
    return (
      this.numero(cuenta.valorEntradasMes) -
      this.numero(cuenta.valorSalidasMes)
    );
  }

  get movimientoNetoMes(): number {
    return (
      this.valorEntradasMes -
      this.valorSalidasMes
    );
  }

  // =========================================================
  // CHEQUES EN CANJE
  // =========================================================

  get cantidadChequesCanje(): number {
    return this.sumar(
      cuenta => cuenta.cantidadChequesCanje
    );
  }

  get valorChequesCanje(): number {
    return this.sumar(
      cuenta => cuenta.valorChequesCanje
    );
  }

  // =========================================================
  // RELACIONES ESPECIALES
  // =========================================================

  get cantidadBeneficiarios(): number {
    return this.lista.filter(
      cuenta => cuenta.tieneBeneficiarios === true
    ).length;
  }

  get cantidadApoderados(): number {
    return this.lista.filter(
      cuenta => cuenta.tieneApoderados === true
    ).length;
  }

  get cantidadConjuntas(): number {
    return this.lista.filter(
      cuenta => cuenta.cuentaConjunta === true
    ).length;
  }

  // =========================================================
  // INFORMACIÓN VISIBLE DE LA CUENTA
  // =========================================================

  nombreForma(
    cuenta: ExpedienteCuentaAhorro
  ): string {
    return this.texto(
      cuenta.nombreFormaAhorro ??
      cuenta.codigoFormaAhorro,
      'Sin forma de ahorro'
    );
  }

  tipoCaptacion(
    cuenta: ExpedienteCuentaAhorro
  ): string {
    return this.texto(
      cuenta.nombreTipoCaptacion ??
      cuenta.codigoTipoCaptacion,
      'Sin tipo de captación'
    );
  }

  numeroCuenta(
    cuenta: ExpedienteCuentaAhorro
  ): string {
    return this.texto(
      cuenta.numeroCuenta,
      'Sin número'
    );
  }

  estadoCuenta(
    cuenta: ExpedienteCuentaAhorro
  ): string {
    return this.texto(
      cuenta.nombreEstadoCuenta ??
      cuenta.codigoEstadoCuenta,
      'Sin estado'
    );
  }

  // =========================================================
  // CLASES VISUALES
  // =========================================================

  claseEstado(
    cuenta: ExpedienteCuentaAhorro
  ): string {

    if (cuenta.embargada === true) {
      return 'estado--embargada';
    }

    if (cuenta.bloqueada === true) {
      return 'estado--bloqueada';
    }

    if (this.esCuentaCancelada(cuenta)) {
      return 'estado--cancelada';
    }

    if (cuenta.activa === true) {
      return 'estado--activa';
    }

    return 'estado--neutral';
  }

  claseAlerta(
    cuenta: ExpedienteCuentaAhorro
  ): string {

    const nivel = this.normalizar(
      cuenta.nivelAlerta
    );

    if (
      this.numero(cuenta.alertasCriticas) > 0 ||
      nivel === 'CRITICA' ||
      nivel === 'CRÍTICA'
    ) {
      return 'alerta--critica';
    }

    if (
      this.numero(cuenta.alertasAdvertencia) > 0 ||
      nivel === 'ADVERTENCIA'
    ) {
      return 'alerta--advertencia';
    }

    if (
      this.numero(cuenta.alertasInformativas) > 0 ||
      nivel === 'INFORMATIVA'
    ) {
      return 'alerta--informativa';
    }

    return 'alerta--normal';
  }

  // =========================================================
  // VALIDACIONES DE LA CUENTA
  // =========================================================

  requiereAtencion(
    cuenta: ExpedienteCuentaAhorro
  ): boolean {
    return (
      cuenta.requiereRevision === true ||
      cuenta.movimientosInusuales === true ||
      cuenta.saldoNegativo === true ||
      cuenta.embargada === true ||
      cuenta.bloqueada === true ||
      this.numero(cuenta.cantidadAlertas) > 0
    );
  }

  tieneCondicionesEspeciales(
    cuenta: ExpedienteCuentaAhorro
  ): boolean {
    return (
      cuenta.cuentaConjunta === true ||
      cuenta.tieneBeneficiarios === true ||
      cuenta.tieneApoderados === true ||
      cuenta.exentaGMF === true ||
      cuenta.generaIntereses === true
    );
  }

  esCuentaAportes(
    cuenta: ExpedienteCuentaAhorro
  ): boolean {
    return this.normalizar(
      cuenta.codigoTipoCaptacion
    ) === '1';
  }

  esCuentaCancelada(
    cuenta: ExpedienteCuentaAhorro
  ): boolean {

    if (cuenta.cancelada === true) {
      return true;
    }

    const codigoEstado = this.normalizar(
      cuenta.codigoEstadoCuenta
    );

    const nombreEstado = this.normalizar(
      cuenta.nombreEstadoCuenta
    );

    return (
      codigoEstado === 'C' ||
      nombreEstado === 'CANCELADA' ||
      nombreEstado === 'CANCELADO'
    );
  }

  // =========================================================
  // FUNCIONES PARA EL TEMPLATE
  // =========================================================

  valor(
    dato: unknown
  ): number {
    return this.numero(dato);
  }

  textoValor(
    dato: unknown,
    predeterminado = 'Sin información'
  ): string {
    return this.texto(
      dato,
      predeterminado
    );
  }

  // =========================================================
  // FUNCIONES PRIVADAS
  // =========================================================

  private sumar(
    selector: (
      cuenta: ExpedienteCuentaAhorro
    ) => unknown
  ): number {
    return this.lista.reduce(
      (total, cuenta) =>
        total + this.numero(selector(cuenta)),
      0
    );
  }

  private texto(
    valor: unknown,
    predeterminado = ''
  ): string {

    const resultado = String(
      valor ?? ''
    ).trim();

    return resultado || predeterminado;
  }

  private numero(
    valor: unknown
  ): number {

    const resultado = Number(
      valor ?? 0
    );

    return Number.isFinite(resultado)
      ? resultado
      : 0;
  }

  private tieneValorNumerico(
    valor: unknown
  ): boolean {

    if (
      valor === null ||
      valor === undefined ||
      valor === ''
    ) {
      return false;
    }

    return Number.isFinite(
      Number(valor)
    );
  }

  private normalizar(
    valor: unknown
  ): string {
    return this.texto(valor)
      .toUpperCase();
  }
}
