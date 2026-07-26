import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

import {
  ExpedienteCuentaAhorro,
  ExpedienteIndicadores
} from '../expediente-asociado.dto';

@Component({
  selector: 'app-expediente-indicadores',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-indicadores.component.html',
  styleUrls: ['./expediente-indicadores.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteIndicadoresComponent {

  @Input()
  indicadores: ExpedienteIndicadores | null = null;

  @Input()
  cuentasAhorro: ExpedienteCuentaAhorro[] = [];

  get totalAportes(): number {
    return this.valorNumero(
      this.indicadores?.totalAportes
    );
  }

  get totalAhorros(): number {
    return this.valorNumero(
      this.indicadores?.totalAhorros
    );
  }

  get totalCdats(): number {
    return this.valorNumero(
      this.indicadores?.totalCdats
    );
  }

  get saldoTotalCredito(): number {
    return this.valorNumero(
      this.indicadores?.saldoTotalCredito
    );
  }

  get totalBienes(): number {
    return this.valorNumero(
      this.indicadores?.totalBienes
    );
  }

  get patrimonioNeto(): number {
    return this.valorNumero(
      this.indicadores?.patrimonioNeto
    );
  }

  get cantidadCuentasAhorro(): number {

    return this.cuentasAhorro
      .filter(cuenta => !this.esCuentaAportes(cuenta))
      .length;
  }

  get cantidadCdats(): number {
    return this.valorNumero(
      this.indicadores?.cantidadCdats
    );
  }

  get cantidadCreditos(): number {
    return this.valorNumero(
      this.indicadores?.cantidadCreditos
    );
  }

  get cantidadBienes(): number {
    return this.valorNumero(
      this.indicadores?.cantidadBienes
    );
  }

  get creditosEnMora(): number {
    return this.valorNumero(
      this.indicadores?.creditosEnMora
    );
  }

  get diasMayorMora(): number {
    return this.valorNumero(
      this.indicadores?.diasMayorMora
    );
  }

  get cantidadAlertas(): number {
    return (
      this.valorNumero(
        this.indicadores?.cantidadAlertasCriticas
      ) +
      this.valorNumero(
        this.indicadores?.cantidadAlertasAdvertencia
      ) +
      this.valorNumero(
        this.indicadores?.cantidadAlertasInformativas
      )
    );
  }

  get nivelRiesgo(): string {
    return (
      this.indicadores?.nivelRiesgo ??
      'NO DEFINIDO'
    );
  }

  get estadoGeneral(): string {
    return (
      this.indicadores?.estadoGeneral ??
      'SIN EVALUAR'
    );
  }

  get resumenEjecutivo(): string {
    return (
      this.indicadores?.resumenEjecutivo ??
      'No se encuentra disponible el resumen ejecutivo del asociado.'
    );
  }

  get claseRiesgo(): string {
    const riesgo = this.normalizarTexto(
      this.indicadores?.nivelRiesgo
    );

    const color = this.normalizarTexto(
      this.indicadores?.colorRiesgo
    );

    if (
      riesgo === 'BAJO' ||
      color === 'VERDE'
    ) {
      return 'riesgo--bajo';
    }

    if (
      riesgo === 'MEDIO' ||
      color === 'AMARILLO'
    ) {
      return 'riesgo--medio';
    }

    if (
      riesgo === 'ALTO' ||
      color === 'ROJO'
    ) {
      return 'riesgo--alto';
    }

    return 'riesgo--neutral';
  }

  get claseEstadoGeneral(): string {
    const estado = this.normalizarTexto(
      this.indicadores?.estadoGeneral
    );

    if (
      estado === 'COMPLETO' ||
      estado === 'VIGENTE' ||
      estado === 'NORMAL'
    ) {
      return 'estado-general--correcto';
    }

    if (
      estado === 'REQUIERE_ACTUALIZACION' ||
      estado === 'REQUIERE ACTUALIZACION' ||
      estado === 'INCOMPLETO'
    ) {
      return 'estado-general--advertencia';
    }

    if (
      estado === 'CRITICO' ||
      estado === 'BLOQUEADO' ||
      estado === 'REQUIERE_ATENCION' ||
      estado === 'REQUIERE ATENCION'
    ) {
      return 'estado-general--critico';
    }

    return 'estado-general--neutral';
  }

  get estadoGeneralPresentacion(): string {
    return this.formatearCodigo(
      this.estadoGeneral
    );
  }

  get sarlaftVigente(): boolean {
    return this.indicadores?.sarlaftVigente === true;
  }

  get contactoCompleto(): boolean {
    return this.indicadores?.contactoCompleto === true;
  }

  get informacionFinancieraCompleta(): boolean {
    return (
      this.indicadores?.informacionFinancieraCompleta ===
      true
    );
  }

  get documentacionCompleta(): boolean {
    return (
      this.indicadores?.documentacionCompleta === true
    );
  }

  private esCuentaAportes(
    cuenta: ExpedienteCuentaAhorro
  ): boolean {

    return this.normalizarTexto(
      cuenta.codigoTipoCaptacion
    ) === '1';
  }

  private valorNumero(
    valor: number | null | undefined
  ): number {
    const numero = Number(valor ?? 0);

    return Number.isFinite(numero)
      ? numero
      : 0;
  }

  private normalizarTexto(
    valor: string | null | undefined
  ): string {
    return String(valor ?? '')
      .trim()
      .toUpperCase();
  }

  private formatearCodigo(
    valor: string
  ): string {
    const texto = String(valor ?? '')
      .trim()
      .replaceAll('_', ' ')
      .toLocaleLowerCase('es-CO');

    if (!texto) {
      return 'Sin evaluar';
    }

    return texto.charAt(0).toUpperCase() +
      texto.slice(1);
  }
}
