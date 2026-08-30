import {
  CommonModule
} from '@angular/common';

import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  ConsultaCreditoDetalle,
  ConsultaCreditoSeguro,
  ConsultaCreditoSeguroMovimiento
} from '../consulta-creditos.models';


@Component({
  selector: 'app-consulta-creditos-seguros',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl:
    './consulta-creditos-seguros.component.html',
  styleUrls: [
    './consulta-creditos-seguros.component.scss'
  ]
})
export class ConsultaCreditosSegurosComponent {

  // =========================================================
  // ENTRADAS
  // =========================================================

  @Input({
    required: true
  })
  credito!:
    ConsultaCreditoDetalle;

  @Input()
  seguros:
    ConsultaCreditoSeguro[] = [];


  // =========================================================
  // SALIDAS
  // =========================================================

  @Output()
  cerrar =
    new EventEmitter<void>();


  // =========================================================
  // INDICADORES DE CONFIGURACIONES
  // =========================================================

  get cantidadSeguros(): number {

    return (
      this.seguros?.length
      ?? 0
    );

  }

  get cantidadSegurosActivos(): number {

    return (
      this.seguros
        ?.filter(
          seguro =>
            seguro.seguroActivo === true
        )
        .length
      ?? 0
    );

  }

  get cantidadSegurosInactivos(): number {

    return (
      this.seguros
        ?.filter(
          seguro =>
            seguro.seguroActivo !== true
        )
        .length
      ?? 0
    );

  }

  get tieneSeguros(): boolean {

    return this.cantidadSeguros > 0;

  }

  get porcentajeTotalActivo(): number {

    return this.seguros
      ?.filter(
        seguro =>
          seguro.seguroActivo === true
      )
      .reduce(
        (
          total,
          seguro
        ) =>
          total
          + Number(
            seguro.porcentajeSeguro
            ?? 0
          ),
        0
      )
      ?? 0;

  }


  // =========================================================
  // INDICADORES DE MOVIMIENTOS
  // =========================================================

  get cantidadMovimientos(): number {

    return this.seguros
      ?.reduce(
        (
          total,
          seguro
        ) =>
          total
          + (
            seguro.movimientos?.length
            ?? 0
          ),
        0
      )
      ?? 0;

  }

  get tieneMovimientos(): boolean {

    return this.cantidadMovimientos > 0;

  }

  movimientosSeguro(
    seguro:
      ConsultaCreditoSeguro
  ): ConsultaCreditoSeguroMovimiento[] {

    return (
      seguro.movimientos
      ?? []
    );

  }

  tieneMovimientosSeguro(
    seguro:
      ConsultaCreditoSeguro
  ): boolean {

    return (
      seguro.movimientos?.length
      ?? 0
    ) > 0;

  }


  // =========================================================
  // ACCIONES
  // =========================================================

  cerrarDetalle(): void {

    this.cerrar.emit();

  }


  // =========================================================
  // DESCRIPCIONES
  // =========================================================

  descripcionEstado(
    seguro:
      ConsultaCreditoSeguro
  ): string {

    if (
      seguro.descripcionEstadoSeguro
    ) {
      return seguro.descripcionEstadoSeguro;
    }

    return seguro.seguroActivo === true
      ? 'Activo'
      : 'Inactivo';

  }

  descripcionTipoCobro(
    seguro:
      ConsultaCreditoSeguro
  ): string {

    return (
      seguro.descripcionTipoCobroSeguro
      ?? seguro.tipoCobroSeguro
      ?? ''
    );

  }

  descripcionVencimiento(
    seguro:
      ConsultaCreditoSeguro
  ): string {

    if (
      seguro.estadoVencimientoSeguro
    ) {
      return seguro.estadoVencimientoSeguro;
    }

    if (
      seguro.fechaSeguroVencida === true
    ) {
      return 'Vencido';
    }

    return 'Vigente';

  }

  descripcionEstadoMovimiento(
    movimiento:
      ConsultaCreditoSeguroMovimiento
  ): string {

    if (
      movimiento.movimientoActivo === true
    ) {
      return 'Activo';
    }

    return 'Inactivo';

  }

  descripcionComprobante(
    movimiento:
      ConsultaCreditoSeguroMovimiento
  ): string {

    if (
      movimiento.comprobanteCompleto
    ) {
      return movimiento.comprobanteCompleto;
    }

    if (
      movimiento.numeroComprobante
    ) {
      return movimiento.numeroComprobante;
    }

    return '';

  }


  // =========================================================
  // SELECCIÓN VISUAL
  // =========================================================

  esSeguroActivo(
    seguro:
      ConsultaCreditoSeguro
  ): boolean {

    return seguro.seguroActivo === true;

  }

  esSeguroVencido(
    seguro:
      ConsultaCreditoSeguro
  ): boolean {

    return seguro.fechaSeguroVencida === true;

  }

  esConfiguracionPrincipal(
    seguro:
      ConsultaCreditoSeguro
  ): boolean {

    return (
      seguro.configuracionSeguroPrincipal
      === true
    );

  }

  esMovimientoDebito(
    movimiento:
      ConsultaCreditoSeguroMovimiento
  ): boolean {

    return (
      Number(
        movimiento.valorDebito
        ?? 0
      ) > 0
    );

  }

  esMovimientoCredito(
    movimiento:
      ConsultaCreditoSeguroMovimiento
  ): boolean {

    return (
      Number(
        movimiento.valorCredito
        ?? 0
      ) > 0
    );

  }


  // =========================================================
  // TRACK BY
  // =========================================================

  trackBySeguro(
    indice: number,
    seguro:
      ConsultaCreditoSeguro
  ): number {

    return (
      seguro.idCreditoSeguro
      ?? indice
    );

  }

  trackByMovimiento(
    indice: number,
    movimiento:
      ConsultaCreditoSeguroMovimiento
  ): number {

    return (
      movimiento.idCreditoSeguroDetalle
      ?? indice
    );

  }

}
