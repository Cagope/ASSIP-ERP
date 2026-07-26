import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

import {
  ExpedienteCdat
} from '../expediente-asociado.dto';

@Component({
  selector: 'app-expediente-cdats',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-cdats.component.html',
  styleUrls: ['./expediente-cdats.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteCdatsComponent {

  @Input()
  cdats: ExpedienteCdat[] | null = [];

  // ========================================================
  // LISTA SEGURA
  // ========================================================

  get lista(): ExpedienteCdat[] {
    return Array.isArray(this.cdats)
      ? this.cdats
      : [];
  }

  get tieneCdats(): boolean {
    return this.lista.length > 0;
  }

  // ========================================================
  // CANTIDADES
  // ========================================================

  get cantidadCdats(): number {
    return this.lista.length;
  }

  get cantidadActivos(): number {
    return this.lista.filter(
      cdat =>
        cdat.activo === true &&
        !this.esCancelado(cdat)
    ).length;
  }

  get cantidadCancelados(): number {
    return this.lista.filter(
      cdat => this.esCancelado(cdat)
    ).length;
  }

  get cantidadVencidos(): number {
    return this.lista.filter(
      cdat =>
        !this.esCancelado(cdat) &&
        cdat.activo === true &&
        cdat.vencido === true
    ).length;
  }

  get cantidadProximosVencer(): number {
    return this.lista.filter(
      cdat =>
        !this.esCancelado(cdat) &&
        cdat.activo === true &&
        cdat.proximoVencer === true
    ).length;
  }

  get cantidadConjuntos(): number {
    return this.lista.filter(
      cdat => cdat.conjunto === true
    ).length;
  }

  get cantidadConAlertas(): number {
    return this.lista.filter(
      cdat => this.numero(cdat.cantidadAlertas) > 0
    ).length;
  }

  get cantidadPagosInteresesTotal(): number {
    return this.lista.reduce(
      (
        total,
        cdat
      ) =>
        total +
        this.entero(
          cdat.cantidadPagosIntereses
        ),
      0
    );
  }

  get cantidadMovimientosTotal(): number {
    return this.lista.reduce(
      (
        total,
        cdat
      ) =>
        total +
        this.entero(
          cdat.cantidadMovimientos
        ),
      0
    );
  }

  // ========================================================
  // CAPITAL
  // ========================================================

  get capitalInicialTotal(): number {
    return this.sumar(
      cdat => cdat.capitalInicial
    );
  }

  get capitalHistoricoInvertidoTotal(): number {
    return this.sumar(
      cdat => cdat.capitalHistoricoInvertido
    );
  }

  get capitalVigenteTotal(): number {
    return this.sumar(
      cdat => cdat.capitalVigente
    );
  }

  /**
   * Se conserva como apoyo temporal mientras el HTML anterior
   * todavía pueda estar usando saldoCapitalTotal.
   */
  get saldoCapitalTotal(): number {
    return this.sumar(
      cdat => cdat.saldoCapital
    );
  }

  /**
   * Se conserva como apoyo temporal mientras el HTML anterior
   * todavía pueda estar usando saldoTotal.
   */
  get saldoTotal(): number {
    return this.sumar(
      cdat => cdat.saldoTotal
    );
  }

  // ========================================================
  // RENDIMIENTO HISTÓRICO
  // ========================================================

  get interesesLiquidadosTotal(): number {
    return this.sumar(
      cdat => cdat.interesesLiquidados
    );
  }

  get retencionFuenteTotal(): number {
    return this.sumar(
      cdat => cdat.retencionFuente
    );
  }

  get rendimientoHistoricoTotal(): number {
    return this.sumar(
      cdat => cdat.rendimientoHistorico
    );
  }

  // ========================================================
  // PRESENTACIÓN
  // ========================================================

  numeroCdat(
    cdat: ExpedienteCdat
  ): string {

    return this.texto(
      cdat.numeroCdat,
      'Sin número'
    );
  }

  estadoCdat(
    cdat: ExpedienteCdat
  ): string {

    if (this.esCancelado(cdat)) {
      return this.texto(
        cdat.nombreEstado,
        'Cancelado'
      );
    }

    if (cdat.vencido === true) {
      return 'Vencido';
    }

    if (cdat.proximoVencer === true) {
      return this.texto(
        cdat.nombreEstado,
        'Próximo a vencer'
      );
    }

    return this.texto(
      cdat.nombreEstado ??
      cdat.codigoEstado,
      'Sin estado'
    );
  }

  claseEstado(
    cdat: ExpedienteCdat
  ): string {

    if (this.esCancelado(cdat)) {
      return 'estado--cancelado';
    }

    if (
      cdat.activo === true &&
      cdat.vencido === true
    ) {
      return 'estado--vencido';
    }

    if (cdat.activo === true) {
      return 'estado--activo';
    }

    return 'estado--neutral';
  }

  claseAlerta(
    cdat: ExpedienteCdat
  ): string {

    const nivel =
      this.normalizar(
        cdat.nivelAlerta
      );

    if (
      this.numero(cdat.alertasCriticas) > 0 ||
      nivel === 'CRITICA' ||
      nivel === 'CRÍTICA'
    ) {
      return 'alerta--critica';
    }

    if (
      this.numero(cdat.alertasAdvertencia) > 0 ||
      nivel === 'ADVERTENCIA'
    ) {
      return 'alerta--advertencia';
    }

    if (
      this.numero(cdat.alertasInformativas) > 0 ||
      nivel === 'INFORMATIVA'
    ) {
      return 'alerta--informativa';
    }

    return 'alerta--normal';
  }

  requiereAtencion(
    cdat: ExpedienteCdat
  ): boolean {

    if (this.esCancelado(cdat)) {
      return false;
    }

    return (
      cdat.vencido === true ||
      cdat.proximoVencer === true ||
      this.numero(cdat.cantidadAlertas) > 0
    );
  }

  tieneInformacionHistorica(
    cdat: ExpedienteCdat
  ): boolean {

    return (
      this.numero(
        cdat.capitalHistoricoInvertido
      ) !== 0 ||
      this.numero(
        cdat.interesesLiquidados
      ) !== 0 ||
      this.numero(
        cdat.retencionFuente
      ) !== 0 ||
      this.numero(
        cdat.rendimientoHistorico
      ) !== 0 ||
      this.entero(
        cdat.cantidadMovimientos
      ) > 0
    );
  }

  tieneTitularidadConjunta(
    cdat: ExpedienteCdat
  ): boolean {

    return (
      cdat.conjunto === true ||
      this.entero(cdat.numeroTitulares) > 1
    );
  }

  // ========================================================
  // VENCIMIENTO
  // ========================================================

  diasVencimientoTexto(
    cdat: ExpedienteCdat
  ): string {

    if (this.esCancelado(cdat)) {
      return 'Cancelado';
    }

    if (
      cdat.diasParaVencimiento === null ||
      cdat.diasParaVencimiento === undefined
    ) {
      return 'Sin calcular';
    }

    const dias =
      this.numero(
        cdat.diasParaVencimiento
      );

    if (dias === 0) {
      return 'Vence hoy';
    }

    if (dias > 0) {
      return `${dias} ${
        dias === 1
          ? 'día'
          : 'días'
      }`;
    }

    const diasVencidos =
      Math.abs(dias);

    return `${diasVencidos} ${
      diasVencidos === 1
        ? 'día vencido'
        : 'días vencidos'
    }`;
  }

  claseDiasVencimiento(
    cdat: ExpedienteCdat
  ): string {

    if (this.esCancelado(cdat)) {
      return 'vencimiento--neutral';
    }

    if (
      cdat.activo === true &&
      cdat.vencido === true
    ) {
      return 'vencimiento--critico';
    }

    if (
      cdat.activo === true &&
      cdat.proximoVencer === true
    ) {
      return 'vencimiento--advertencia';
    }

    return 'vencimiento--normal';
  }

  // ========================================================
  // VALORES PARA PLANTILLA
  // ========================================================

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

  // ========================================================
  // TRACK BY
  // ========================================================

  trackByCdat(
    indice: number,
    cdat: ExpedienteCdat
  ): number | string {

    return cdat.idCdat ??
      cdat.numeroCdat ??
      indice;
  }

  // ========================================================
  // UTILIDADES
  // ========================================================

  private esCancelado(
    cdat: ExpedienteCdat
  ): boolean {

    return (
      cdat.cancelado === true ||
      this.normalizar(
        cdat.codigoEstado
      ) === 'C'
    );
  }

  private sumar(
    selector: (
      cdat: ExpedienteCdat
    ) => unknown
  ): number {

    return this.lista.reduce(
      (
        total,
        cdat
      ) =>
        total +
        this.numero(
          selector(cdat)
        ),
      0
    );
  }

  private texto(
    valor: unknown,
    predeterminado = ''
  ): string {

    const resultado =
      String(
        valor ?? ''
      ).trim();

    return resultado ||
      predeterminado;
  }

  private numero(
    valor: unknown
  ): number {

    const resultado =
      Number(
        valor ?? 0
      );

    return Number.isFinite(resultado)
      ? resultado
      : 0;
  }

  private entero(
    valor: unknown
  ): number {

    const resultado =
      Math.trunc(
        this.numero(valor)
      );

    return resultado > 0
      ? resultado
      : 0;
  }

  private normalizar(
    valor: unknown
  ): string {

    return this.texto(valor)
      .toUpperCase();
  }
}
