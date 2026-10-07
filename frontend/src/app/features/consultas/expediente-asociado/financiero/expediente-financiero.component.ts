import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

import {
  ExpedienteInformacionFinanciera
} from '../expediente-asociado.dto';

@Component({
  selector: 'app-expediente-financiero',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-financiero.component.html',
  styleUrls: ['./expediente-financiero.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteFinancieroComponent {

  @Input()
  informacion: ExpedienteInformacionFinanciera | null = null;

  get nombreCompleto(): string {
    return this.texto(
      this.campo('nombreCompleto'),
      'Sin nombre registrado'
    );
  }

  get documentoCompleto(): string {
    const tipo = this.texto(
      this.campo('nombreTipoDocumento') ??
      this.campo('tipoDocumento')
    );

    const documento = this.texto(
      this.campo('documento')
    );

    return this.unir(
      [
        tipo,
        documento
      ],
      ' ',
      'Sin documento'
    );
  }

  get ingresosLaborales(): number {
    return this.numero(
      this.campo('ingresosLaborales') ??
      this.campo('ingresosMensuales') ??
      this.campo('salario')
    );
  }

  get ingresosComisiones(): number {
    return this.numero(
      this.campo('ingresosComisiones')
    );
  }

  get otrosIngresos(): number {
    return this.numero(
      this.campo('otrosIngresos')
    );
  }

  get totalIngresos(): number {
    const totalBackend = this.numero(
      this.campo('totalIngresos')
    );

    if (totalBackend !== 0) {
      return totalBackend;
    }

    return (
      this.ingresosLaborales +
      this.ingresosComisiones +
      this.otrosIngresos
    );
  }

  get egresosFamiliares(): number {
    return this.numero(
      this.campo('egresosFamiliares')
    );
  }

  get egresosArriendo(): number {
    return this.numero(
      this.campo('egresosArriendo')
    );
  }

  get egresosCredito(): number {
    return this.numero(
      this.campo('egresosCredito') ??
      this.campo('cuotasCreditos')
    );
  }

  get otrosEgresos(): number {
    return this.numero(
      this.campo('otrosEgresos')
    );
  }

  get totalEgresos(): number {
    const totalBackend = this.numero(
      this.campo('totalEgresos') ??
      this.campo('egresosMensuales')
    );

    if (totalBackend !== 0) {
      return totalBackend;
    }

    return (
      this.egresosFamiliares +
      this.egresosArriendo +
      this.egresosCredito +
      this.otrosEgresos
    );
  }

  get disponibleMensual(): number {
    const valorBackend = this.campo(
      'disponibleMensual'
    );

    if (
      valorBackend !== null &&
      valorBackend !== undefined
    ) {
      return this.numero(valorBackend);
    }

    return this.totalIngresos - this.totalEgresos;
  }

  get totalActivos(): number {
    return this.numero(
      this.campo('totalActivos') ??
      this.campo('activos')
    );
  }

  get totalPasivos(): number {
    return this.numero(
      this.campo('totalPasivos') ??
      this.campo('pasivos')
    );
  }

  get patrimonio(): number {
    const valorBackend = this.campo(
      'patrimonio'
    );

    if (
      valorBackend !== null &&
      valorBackend !== undefined
    ) {
      return this.numero(valorBackend);
    }

    return this.totalActivos - this.totalPasivos;
  }

  get porcentajeEndeudamiento(): number | null {
    const valorBackend = this.campo(
      'porcentajeEndeudamiento'
    );

    if (
      valorBackend !== null &&
      valorBackend !== undefined
    ) {
      return this.numero(valorBackend);
    }

    if (this.totalActivos <= 0) {
      return null;
    }

    return (
      this.totalPasivos /
      this.totalActivos
    ) * 100;
  }

  get porcentajeCompromisoIngresos(): number | null {
    const valorBackend = this.campo(
      'porcentajeCompromisoIngresos'
    );

    if (
      valorBackend !== null &&
      valorBackend !== undefined
    ) {
      return this.numero(valorBackend);
    }

    if (this.totalIngresos <= 0) {
      return null;
    }

    return (
      this.totalEgresos /
      this.totalIngresos
    ) * 100;
  }

  get capacidadPago(): number {
    return this.numero(
      this.campo('capacidadPago') ??
      this.disponibleMensual
    );
  }

  get nivelCapacidadPago(): string {
    const nivel = this.texto(
      this.campo('nivelCapacidadPago')
    );

    if (nivel) {
      return this.formatearCodigo(nivel);
    }

    if (this.totalIngresos <= 0) {
      return 'Sin evaluar';
    }

    const porcentajeDisponible =
      (
        this.disponibleMensual /
        this.totalIngresos
      ) * 100;

    if (porcentajeDisponible >= 40) {
      return 'Alta';
    }

    if (porcentajeDisponible >= 20) {
      return 'Media';
    }

    if (porcentajeDisponible >= 0) {
      return 'Baja';
    }

    return 'Crítica';
  }

  get claseCapacidadPago(): string {
    const nivel = this.normalizar(
      this.nivelCapacidadPago
    );

    if (
      nivel === 'ALTA' ||
      nivel === 'BUENA'
    ) {
      return 'capacidad--alta';
    }

    if (
      nivel === 'MEDIA' ||
      nivel === 'MODERADA'
    ) {
      return 'capacidad--media';
    }

    if (
      nivel === 'BAJA'
    ) {
      return 'capacidad--baja';
    }

    if (
      nivel === 'CRITICA' ||
      nivel === 'CRÍTICA'
    ) {
      return 'capacidad--critica';
    }

    return 'capacidad--neutral';
  }

  get actividadEconomica(): string {
    return this.texto(
      this.campo('nombreActividadEconomica') ??
      this.campo('actividadEconomica'),
      'Sin actividad económica'
    );
  }

  get sectorEconomico(): string {
    return this.texto(
      this.campo('nombreSectorEconomico') ??
      this.campo('sectorEconomico'),
      'Sin sector económico'
    );
  }

  get ocupacion(): string {
    return this.texto(
      this.campo('ocupacion'),
      'Sin ocupación'
    );
  }

  get empresa(): string {
    return this.texto(
      this.campo('empresa') ??
      this.campo('nombreEmpresa'),
      'Sin empresa'
    );
  }

  get cargo(): string {
    return this.texto(
      this.campo('cargo'),
      'Sin cargo'
    );
  }

  get conceptoOtrosIngresos(): string {
    return this.texto(
      this.campo('conceptoOtrosIngresos'),
      'Sin detalle'
    );
  }

  get informacionCompleta(): boolean {
    return (
      this.booleano(
        this.campo('informacionFinancieraCompleta')
      ) ||
      this.booleano(
        this.campo('informacionCompleta')
      )
    );
  }

  get porcentajeCompletitud(): number {
    const valorBackend = this.campo(
      'porcentajeCompletitud'
    );

    if (
      valorBackend !== null &&
      valorBackend !== undefined
    ) {
      return this.limitarPorcentaje(
        this.numero(valorBackend)
      );
    }

    const validaciones = [
      this.totalIngresos > 0,
      this.totalEgresos >= 0,
      this.totalActivos > 0,
      this.totalPasivos >= 0,
      this.actividadEconomica !==
        'Sin actividad económica',
      this.ocupacion !==
        'Sin ocupación'
    ];

    const completas = validaciones
      .filter(Boolean)
      .length;

    return Math.round(
      (
        completas /
        validaciones.length
      ) * 100
    );
  }

  get claseCompletitud(): string {
    if (
      this.informacionCompleta ||
      this.porcentajeCompletitud === 100
    ) {
      return 'completitud--completa';
    }

    if (this.porcentajeCompletitud >= 70) {
      return 'completitud--alta';
    }

    if (this.porcentajeCompletitud >= 40) {
      return 'completitud--media';
    }

    return 'completitud--baja';
  }

  get fechaActualizacion(): string | null {
    const valor = this.informacion?.fechaActualizacion;

    if (
      valor === null ||
      valor === undefined ||
      String(valor).trim() === ''
    ) {
      return null;
    }

    return String(valor);
  }

  get diasSinActualizar(): number {
    return this.numero(
      this.campo('diasSinActualizar')
    );
  }

  get claseActualizacion(): string {
    if (!this.fechaActualizacion) {
      return 'actualizacion--critica';
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
    if (!this.fechaActualizacion) {
      return 'Sin fecha de actualización';
    }

    if (this.diasSinActualizar <= 365) {
      return 'Información financiera vigente';
    }

    if (this.diasSinActualizar <= 730) {
      return 'Requiere actualización';
    }

    return 'Información financiera desactualizada';
  }

  get observaciones(): string {
    return this.texto(
      this.campo('observaciones')
    );
  }

  get presentaNovedades(): boolean {
    return this.booleano(
      this.campo('presentaNovedades')
    );
  }

  get cantidadNovedades(): number {
    return this.numero(
      this.campo('cantidadNovedades')
    );
  }

  get resumenNovedades(): string {
    return this.texto(
      this.campo('resumenNovedades') ??
      this.campo('nivelNovedad'),
      'Novedades pendientes de revisión'
    );
  }

  private campo(
    nombre: string
  ): unknown {
    if (!this.informacion) {
      return null;
    }

    const registro =
      this.informacion as unknown as Record<string, unknown>;

    return registro[nombre] ?? null;
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

  private booleano(
    valor: unknown
  ): boolean {
    return valor === true;
  }

  private unir(
    valores: unknown[],
    separador = ' · ',
    predeterminado = 'Sin información'
  ): string {
    const resultado = valores
      .map(valor => this.texto(valor))
      .filter(valor => valor.length > 0)
      .join(separador);

    return resultado || predeterminado;
  }

  private normalizar(
    valor: unknown
  ): string {
    return this.texto(valor)
      .toUpperCase();
  }

  private limitarPorcentaje(
    valor: number
  ): number {
    return Math.min(
      100,
      Math.max(0, valor)
    );
  }

  private formatearCodigo(
    valor: string
  ): string {
    const texto = valor
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
