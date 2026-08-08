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
  ConsultaCreditoSeguro
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
  // INDICADORES
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

}
