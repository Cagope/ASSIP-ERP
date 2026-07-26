import { CommonModule } from '@angular/common';

import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  ExpedienteResumenGeneral
} from '../expediente-asociado.dto';

@Component({
  selector: 'app-expediente-resumen',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-resumen.component.html',
  styleUrls: ['./expediente-resumen.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteResumenComponent {

  @Input()
  resumen: ExpedienteResumenGeneral | null = null;

  @Output()
  readonly regresarBusqueda = new EventEmitter<void>();


  // ========================================================
  // IDENTIFICACIÓN
  // ========================================================

  get nombreCompleto(): string {
    return this.texto(
      this.resumen?.nombreCompleto,
      'Sin nombre registrado'
    );
  }


  get documentoCompleto(): string {

    const tipo = this.texto(
      this.resumen?.nombreTipoDocumento ??
      this.resumen?.tipoDocumento
    );

    const documento = this.texto(
      this.resumen?.documento
    );

    if (!tipo && !documento) {
      return 'Sin documento';
    }

    if (!tipo) {
      return documento;
    }

    if (!documento) {
      return tipo;
    }

    return `${tipo} ${documento}`;
  }


  get tipoPersona(): string {

    const codigo = this.normalizar(
      this.resumen?.tipoPersona
    );

    switch (codigo) {

      case '1':
        return 'Natural';

      case '2':
        return 'Jurídica';

      case '':
        return 'Sin información';

      default:
        return this.texto(
          this.resumen?.tipoPersona,
          'Sin información'
        );
    }
  }


  // ========================================================
  // ESTADO DEL ASOCIADO
  // Fuente: cuenta de aportes
  // ========================================================

  get estadoAsociado(): string {
    return this.texto(
      this.resumen?.nombreEstadoAsociado,
      'Sin estado'
    );
  }


  get claseEstado(): string {

    if (this.resumen?.activo === true) {
      return 'estado--activo';
    }

    const estado = this.normalizar(
      this.resumen?.codigoEstadoAsociado ??
      this.resumen?.nombreEstadoAsociado
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
      estado === 'I' ||
      estado === 'INACTIVO' ||
      estado === 'INACTIVA' ||
      estado === 'SUSPENDIDO' ||
      estado === 'SUSPENDIDA'
    ) {
      return 'estado--inactivo';
    }

    if (
      estado === 'R' ||
      estado === 'RETIRADO' ||
      estado === 'RETIRADA' ||
      estado === 'CANCELADO' ||
      estado === 'CANCELADA'
    ) {
      return 'estado--retirado';
    }

    return 'estado--neutral';
  }


  // ========================================================
  // INFORMACIÓN PERSONAL
  // ========================================================

  get escolaridad(): string {
    return this.texto(
      this.resumen?.nombreEscolaridad,
      'Sin información'
    );
  }


  get ocupacion(): string {
    return this.texto(
      this.resumen?.ocupacion,
      'Sin información'
    );
  }


  get lugarNacimiento(): string {
    return this.unir([
      this.resumen?.ciudadNacimiento,
      this.resumen?.departamentoNacimiento,
      this.resumen?.paisNacimiento
    ]);
  }


  // ========================================================
  // INFORMACIÓN FAMILIAR
  // ========================================================

  get cabezaFamilia(): string {

    const valor = this.normalizar(
      this.resumen?.cabezaFamilia
    );

    if (
      valor === '1' ||
      valor === 'S' ||
      valor === 'SI' ||
      valor === 'SÍ' ||
      valor === 'TRUE'
    ) {
      return 'Sí';
    }

    if (
      valor === '0' ||
      valor === 'N' ||
      valor === 'NO' ||
      valor === 'FALSE'
    ) {
      return 'No';
    }

    return 'Sin información';
  }


  get numeroHijos(): string {

    const valor =
      this.resumen?.numeroHijos;

    if (
      valor === null ||
      valor === undefined
    ) {
      return 'Sin información';
    }

    return String(valor);
  }


  get estratoSocial(): string {

    const valor =
      this.resumen?.estratoSocial;

    if (
      valor === null ||
      valor === undefined
    ) {
      return 'Sin información';
    }

    return String(valor);
  }


  get tipoVivienda(): string {
    return this.texto(
      this.resumen?.nombreTipoVivienda,
      'Sin información'
    );
  }


  // ========================================================
  // PERFIL ECONÓMICO
  // ========================================================

  get sectorEconomico(): string {
    return this.texto(
      this.resumen?.nombreSectorEconomico,
      'Sin información'
    );
  }


  get actividadSes(): string {
    return this.texto(
      this.resumen?.nombreActividadSes,
      'Sin información'
    );
  }


  get actividadDian(): string {
    return this.texto(
      this.resumen?.nombreActividadDian,
      'Sin información'
    );
  }


  // ========================================================
  // CONTACTO
  // ========================================================

  get ubicacionCompleta(): string {
    return this.unir([
      this.resumen?.direccion,
      this.resumen?.barrio,
      this.resumen?.ciudad,
      this.resumen?.departamento,
      this.resumen?.pais
    ]);
  }


  get telefonoPrincipal(): string {
    return this.texto(
      this.resumen?.celular ??
      this.resumen?.telefono,
      'Sin teléfono'
    );
  }


  get correoElectronico(): string {
    return this.texto(
      this.resumen?.correoElectronico,
      'Sin correo electrónico'
    );
  }


  // ========================================================
  // INFORMACIÓN LABORAL
  // ========================================================

  get actividadLaboral(): string {
    return this.texto(
      this.resumen?.ocupacion,
      'Sin información'
    );
  }


  get empresa(): string {
    return this.texto(
      this.resumen?.empresa,
      'Sin información'
    );
  }


  // ========================================================
  // INFORMACIÓN FINANCIERA
  // ========================================================

  get ingresosMensuales(): number {
    return this.numero(
      this.resumen?.ingresosMensuales
    );
  }


  get egresosMensuales(): number {
    return this.numero(
      this.resumen?.egresosMensuales
    );
  }


  get disponibleMensual(): number {
    return (
      this.ingresosMensuales -
      this.egresosMensuales
    );
  }


  get activos(): number {
    return this.numero(
      this.resumen?.activos
    );
  }


  get pasivos(): number {
    return this.numero(
      this.resumen?.pasivos
    );
  }


  get patrimonio(): number {
    return this.numero(
      this.resumen?.patrimonio
    );
  }


  get patrimonioEstimado(): number {
    return this.numero(
      this.resumen?.patrimonioEstimado
    );
  }


  // ========================================================
  // PRODUCTOS Y SALDOS
  // ========================================================

  get saldoAportes(): number {
    return this.numero(
      this.resumen?.saldoAportes
    );
  }


  get saldoAhorros(): number {
    return this.numero(
      this.resumen?.saldoAhorros
    );
  }


  get saldoCdats(): number {
    return this.numero(
      this.resumen?.saldoCdats
    );
  }


  get saldoCartera(): number {
    return (
      this.numero(
        this.resumen?.saldoCapitalCartera
      ) +
      this.numero(
        this.resumen?.saldoInteresesCartera
      )
    );
  }


  get valorBienes(): number {
    return this.numero(
      this.resumen?.valorBienes
    );
  }


  get valorGarantias(): number {
    return this.numero(
      this.resumen?.valorGarantias
    );
  }


  get numeroCuentasAhorro(): number {
    return this.numero(
      this.resumen?.numeroCuentasAhorro
    );
  }


  get numeroCdats(): number {
    return this.numero(
      this.resumen?.numeroCdats
    );
  }


  get numeroCreditos(): number {
    return this.numero(
      this.resumen?.numeroCreditos
    );
  }


  get numeroBienes(): number {
    return this.numero(
      this.resumen?.numeroBienes
    );
  }


  // ========================================================
  // ACTUALIZACIÓN
  // ========================================================

  get diasSinActualizar(): number {
    return this.numero(
      this.resumen?.diasSinActualizar
    );
  }


  get claseActualizacion(): string {

    if (!this.resumen?.fechaActualizacionHojaVida) {
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


  get estadoActualizacion(): string {

    if (!this.resumen?.fechaActualizacionHojaVida) {
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


  // ========================================================
  // ALERTAS
  // ========================================================

  get tieneAlertaEspecial(): boolean {
    return (
      this.resumen?.pep === true ||
      this.resumen?.familiarPep === true ||
      this.resumen?.monedaExtranjera === true ||
      this.resumen?.cuentaExterior === true
    );
  }


  // ========================================================
  // ACCIONES
  // ========================================================

  regresarABusqueda(): void {
    this.regresarBusqueda.emit();
  }


  // ========================================================
  // UTILIDADES
  // ========================================================

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
      .toUpperCase();
  }


  private unir(
    valores: Array<unknown>
  ): string {

    const resultado = valores
      .map(valor => this.texto(valor))
      .filter(valor => valor.length > 0)
      .join(' · ');

    return resultado || 'Sin información';
  }
}
