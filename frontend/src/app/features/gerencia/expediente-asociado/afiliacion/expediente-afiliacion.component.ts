import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

import {
  ExpedienteAfiliacion,
  ExpedienteParticipacionInstitucional
} from '../expediente-asociado.dto';

@Component({
  selector: 'app-expediente-afiliacion',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-afiliacion.component.html',
  styleUrls: ['./expediente-afiliacion.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteAfiliacionComponent {

  @Input()
  afiliacion: ExpedienteAfiliacion | null = null;

  @Input()
  participacionInstitucional:
    ExpedienteParticipacionInstitucional[] = [];

  // =========================================================
  // Afiliación
  // =========================================================

  get codigoAportes(): string {
    return this.texto(
      this.afiliacion?.codigoCuentaAportes ??
      this.afiliacion?.codigoAsociado ??
      this.afiliacion?.numeroAfiliacion,
      'Sin código de aportes'
    );
  }

  get estadoAfiliacion(): string {
    return this.texto(
      this.afiliacion?.nombreEstadoAfiliacion ??
      this.afiliacion?.nombreEstadoAsociado ??
      this.afiliacion?.nombreEstadoCuentaAportes,
      this.afiliacion?.afiliacionActiva === true
        ? 'ACTIVO'
        : 'Sin estado definido'
    );
  }

  get claseEstadoAfiliacion(): string {
    if (this.afiliacion?.retirado === true) {
      return 'estado--retirado';
    }

    if (
      this.afiliacion?.afiliacionActiva === true ||
      this.afiliacion?.cuentaAportesActiva === true
    ) {
      return 'estado--activo';
    }

    const estado = this.normalizar(
      this.afiliacion?.codigoEstadoAfiliacion ??
      this.afiliacion?.codigoEstadoAsociado ??
      this.afiliacion?.codigoEstadoCuentaAportes ??
      this.afiliacion?.nombreEstadoAfiliacion ??
      this.afiliacion?.nombreEstadoAsociado ??
      this.afiliacion?.nombreEstadoCuentaAportes
    );

    if (
      estado === 'A' ||
      estado === 'ACTIVO' ||
      estado === 'ACTIVA' ||
      estado === 'VIGENTE'
    ) {
      return 'estado--activo';
    }

    if (
      estado === 'R' ||
      estado === 'RETIRADO' ||
      estado === 'RETIRADA'
    ) {
      return 'estado--retirado';
    }

    if (
      estado === 'I' ||
      estado === 'INACTIVO' ||
      estado === 'INACTIVA'
    ) {
      return 'estado--inactivo';
    }

    return 'estado--neutral';
  }

  get fechaAfiliacion(): string | null {
    return (
      this.afiliacion?.fechaAfiliacion ??
      this.afiliacion?.fechaIngreso ??
      this.afiliacion?.fechaAntiguedad ??
      null
    );
  }

  get antiguedadTexto(): string {
    const anios = this.numero(
      this.afiliacion?.antiguedadAnios
    );

    const mesesTotales = this.numero(
      this.afiliacion?.antiguedadMeses
    );

    const diasTotales = this.numero(
      this.afiliacion?.antiguedadDias
    );

    if (anios > 0) {
      const mesesRestantes = Math.max(
        0,
        mesesTotales - (anios * 12)
      );

      const textoAnios =
        `${anios} ${anios === 1 ? 'año' : 'años'}`;

      if (mesesRestantes > 0) {
        return (
          `${textoAnios} y ` +
          `${mesesRestantes} ` +
          `${mesesRestantes === 1 ? 'mes' : 'meses'}`
        );
      }

      return textoAnios;
    }

    if (mesesTotales > 0) {
      return (
        `${mesesTotales} ` +
        `${mesesTotales === 1 ? 'mes' : 'meses'}`
      );
    }

    if (diasTotales > 0) {
      return (
        `${diasTotales} ` +
        `${diasTotales === 1 ? 'día' : 'días'}`
      );
    }

    return 'Sin antigüedad calculada';
  }

  // =========================================================
  // Cuenta de aportes
  // =========================================================

  get codigoCuentaAportes(): string {
    return this.texto(
      this.afiliacion?.codigoCuentaAportes,
      'Sin cuenta'
    );
  }

  get nombreFormaAportes(): string {
    return this.texto(
      this.afiliacion?.nombreFormaAportes,
      'Sin forma de aportes'
    );
  }

  get estadoCuentaAportes(): string {
    return this.texto(
      this.afiliacion?.nombreEstadoCuentaAportes,
      this.afiliacion?.cuentaAportesActiva === true
        ? 'ACTIVO'
        : 'Sin estado'
    );
  }

  get claseEstadoCuentaAportes(): string {
    if (this.afiliacion?.cuentaAportesActiva === true) {
      return 'cuenta-estado--activa';
    }

    const estado = this.normalizar(
      this.afiliacion?.codigoEstadoCuentaAportes ??
      this.afiliacion?.nombreEstadoCuentaAportes
    );

    if (
      estado === 'A' ||
      estado === 'ACTIVO' ||
      estado === 'ACTIVA' ||
      estado === 'VIGENTE'
    ) {
      return 'cuenta-estado--activa';
    }

    return 'cuenta-estado--inactiva';
  }

  get fechaAperturaAportes(): string | null {
    return (
      this.afiliacion?.fechaAperturaAportes ??
      this.afiliacion?.fechaAfiliacion ??
      null
    );
  }

  get fechaUltimoMovimientoAportes(): string | null {
    return (
      this.afiliacion?.fechaUltimoMovimientoAportes ??
      this.afiliacion?.fechaUltimoAporte ??
      null
    );
  }

  get saldoAportes(): number {
    return this.numero(
      this.afiliacion?.saldoAportes
    );
  }

  get saldoDisponibleAportes(): number {
    return this.numero(
      this.afiliacion?.saldoDisponibleAportes
    );
  }

  get cuotaAportes(): number {
    return this.numero(
      this.afiliacion?.cuotaAportes
    );
  }

  // =========================================================
  // Participación institucional
  // =========================================================

  get directivos():
    ExpedienteParticipacionInstitucional[] {

    return (this.participacionInstitucional ?? [])
      .filter(item =>
        this.normalizar(item.tipoParticipacion) ===
        'DIRECTIVO'
      );
  }

  get comites():
    ExpedienteParticipacionInstitucional[] {

    return (this.participacionInstitucional ?? [])
      .filter(item =>
        this.normalizar(item.tipoParticipacion) ===
        'COMITE'
      );
  }

  get personasRelacionadas():
    ExpedienteParticipacionInstitucional[] {

    return (this.participacionInstitucional ?? [])
      .filter(item =>
        this.normalizar(item.tipoParticipacion) ===
        'PERSONA_RELACIONADA'
      );
  }

  get tieneParticipacionInstitucional(): boolean {
    return (
      this.directivos.length > 0 ||
      this.comites.length > 0 ||
      this.personasRelacionadas.length > 0
    );
  }

  get cantidadDirectivos(): number {
    return this.directivos.length;
  }

  get cantidadComites(): number {
    return this.comites.length;
  }

  get cantidadPersonasRelacionadas(): number {
    return this.personasRelacionadas.length;
  }

  nombreDirectivo(
    item: ExpedienteParticipacionInstitucional
  ): string {
    return this.texto(
      item.nombreTipoDirectivo,
      'Cargo directivo'
    );
  }

  calidadDirectivo(
    item: ExpedienteParticipacionInstitucional
  ): string {
    return this.texto(
      item.nombreCalidadDirectivo,
      'Sin calidad definida'
    );
  }

  estadoDirectivo(
    item: ExpedienteParticipacionInstitucional
  ): string {
    return this.texto(
      item.nombreEstadoDirectivo,
      'Sin estado'
    );
  }

  claseEstadoDirectivo(
    item: ExpedienteParticipacionInstitucional
  ): string {
    const estado = this.normalizar(
      item.estadoDirectivo ??
      item.nombreEstadoDirectivo
    );

    if (
      estado === '1' ||
      estado === 'A' ||
      estado === 'ACTIVO' ||
      estado === 'ACTIVA' ||
      estado === 'VIGENTE'
    ) {
      return 'participacion-estado--activo';
    }

    return 'participacion-estado--inactivo';
  }

  nombreComite(
    item: ExpedienteParticipacionInstitucional
  ): string {
    return this.texto(
      item.nombreComite,
      'Comité sin nombre'
    );
  }

  cargoComite(
    item: ExpedienteParticipacionInstitucional
  ): string {
    return this.texto(
      item.nombreCargoComite,
      'Sin cargo definido'
    );
  }

  nombrePersonaRelacionada(
    item: ExpedienteParticipacionInstitucional
  ): string {

    return this.texto(
      item.nombreRelacionado,
      'Sin nombre'
    );
  }

  documentoPersonaRelacionada(
    item: ExpedienteParticipacionInstitucional
  ): string {

    return this.texto(
      item.documentoRelacionado,
      'Sin documento'
    );
  }

  parentescoPersonaRelacionada(
    item: ExpedienteParticipacionInstitucional
  ): string {

    return this.texto(
      item.nombreParentesco,
      'Sin parentesco'
    );
  }

  cargoDirectivoRelacionado(
    item: ExpedienteParticipacionInstitucional
  ): string {

    return this.texto(
      item.nombreTipoDirectivo,
      'Cargo directivo'
    );
  }

  trackByDirectivo(
    index: number,
    item: ExpedienteParticipacionInstitucional
  ): number | string {
    return (
      item.idDirectivo ??
      `${item.tipoParticipacion}-${index}`
    );
  }

  trackByComite(
    index: number,
    item: ExpedienteParticipacionInstitucional
  ): number | string {
    return (
      item.idComiteDetalle ??
      item.idComite ??
      `${item.tipoParticipacion}-${index}`
    );
  }

  trackByPersonaRelacionada(
    index: number,
    item: ExpedienteParticipacionInstitucional
  ): number | string {

    return (
      item.idPrivilegiado ??
      `${item.tipoParticipacion}-${index}`
    );
  }

  // =========================================================
  // Retiro
  // =========================================================

  get nombreMotivoRetiro(): string {
    return this.texto(
      this.afiliacion?.nombreMotivoRetiro,
      'Sin motivo registrado'
    );
  }

  // =========================================================
  // Actualización
  // =========================================================

  get diasSinActualizar(): number {
    return this.numero(
      this.afiliacion?.diasSinActualizar
    );
  }

  get claseActualizacion(): string {
    if (
      this.afiliacion?.informacionActualizada === true
    ) {
      return 'actualizacion--vigente';
    }

    if (!this.afiliacion?.fechaUltimaActualizacion) {
      return 'actualizacion--sin-fecha';
    }

    if (this.diasSinActualizar <= 365) {
      return 'actualizacion--vigente';
    }

    if (this.diasSinActualizar <= 730) {
      return 'actualizacion--advertencia';
    }

    return 'actualizacion--critica';
  }

  get textoActualizacion(): string {
    if (
      this.afiliacion?.informacionActualizada === true
    ) {
      return 'Información actualizada';
    }

    if (!this.afiliacion?.fechaUltimaActualizacion) {
      return 'Sin fecha de actualización';
    }

    if (this.diasSinActualizar <= 365) {
      return 'Información vigente';
    }

    if (this.diasSinActualizar <= 730) {
      return 'Requiere actualización';
    }

    return 'Información desactualizada';
  }

  // =========================================================
  // Utilidades
  // =========================================================

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
    valor: number | null | undefined
  ): number {
    const resultado = Number(
      valor ?? 0
    );

    return Number.isFinite(resultado)
      ? resultado
      : 0;
  }

  private normalizar(
    valor: unknown
  ): string {
    return this.texto(valor)
      .toUpperCase()
      .trim();
  }
}
