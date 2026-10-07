import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

import {
  ExpedienteSarlaft
} from '../expediente-asociado.dto';

@Component({
  selector: 'app-expediente-sarlaft',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-sarlaft.component.html',
  styleUrls: ['./expediente-sarlaft.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteSarlaftComponent {

  @Input()
  sarlaft: ExpedienteSarlaft | null = null;

  // =========================================================
  // Identificación
  // =========================================================

  get nombreCompleto(): string {
    return this.texto(
      this.sarlaft?.nombreCompleto,
      'Sin nombre registrado'
    );
  }

  get documentoCompleto(): string {
    const tipoDocumento = this.texto(
      this.sarlaft?.nombreTipoDocumento
      ?? this.sarlaft?.tipoDocumento
    );

    const documento = this.texto(
      this.sarlaft?.documento
    );

    return this.unir(
      [
        tipoDocumento,
        documento
      ],
      ' ',
      'Sin documento'
    );
  }

  get tipoPersona(): string {
    return this.texto(
      this.sarlaft?.tipoPersona,
      'Sin definir'
    );
  }

  // =========================================================
  // Información económica
  // =========================================================

  get ocupacion(): string {
    return this.texto(
      this.sarlaft?.nombreOcupacion,
      'Sin ocupación registrada'
    );
  }

  get sectorEconomico(): string {
    return this.texto(
      this.sarlaft?.nombreSectorEconomico,
      'Sin sector económico'
    );
  }

  get actividadSes(): string {
    return this.texto(
      this.sarlaft?.nombreActividadSes,
      'Sin actividad SES'
    );
  }

  get actividadDian(): string {
    return this.texto(
      this.sarlaft?.nombreActividadDian,
      'Sin actividad DIAN'
    );
  }

  get origenFondos(): string {
    return this.texto(
      this.sarlaft?.origenFondos,
      'Sin origen de fondos registrado'
    );
  }

  // =========================================================
  // PEP
  // =========================================================

  get esPep(): boolean {
    return this.sarlaft?.asociadoPeps === true;
  }

  get tieneFamiliarPep(): boolean {
    return this.sarlaft?.familiaPeps === true;
  }

  get descripcionPep(): string {
    return this.unir([
      this.sarlaft?.nombreTipoPeps,
      this.sarlaft?.tipoPeps,
      this.sarlaft?.observacionesPeps
    ]);
  }

  get periodoPep(): string {
    return this.unir(
      [
        this.formatearFecha(
          this.sarlaft?.fechaInicialPeps
        ),
        this.formatearFecha(
          this.sarlaft?.fechaFinalPeps
        )
      ],
      ' a ',
      'Sin fechas registradas'
    );
  }

  get familiarPep(): string {
    return this.unir([
      this.sarlaft?.nombreFamiliaPeps,
      this.sarlaft?.cedulaFamiliaPeps,
      this.sarlaft?.nombreParentesco
    ]);
  }

  // =========================================================
  // Operaciones internacionales
  // =========================================================

  get realizaMonedaExtranjera(): boolean {
    return this.sarlaft?.monedaExtranjera === true;
  }

  get poseeCuentaExterior(): boolean {
    return this.sarlaft?.cuentaExtranjero === true;
  }

  get cuentaExterior(): string {
    return this.unir([
      this.sarlaft?.nombreBancoExtranjero,
      this.sarlaft?.numeroCuentaExtranjero,
      this.sarlaft?.tipoMonedaExtranjera,
      this.sarlaft?.ciudadCuentaExtranjero,
      this.sarlaft?.paisCuentaExtranjero
    ]);
  }

  // =========================================================
  // Residencia fiscal
  // =========================================================

  get tieneResidenciaFiscal(): boolean {
    return (
      this.sarlaft?.tieneInformacionResidenciaFiscal === true
      || this.sarlaft?.ciudadanoEstadosUnidos === true
      || this.sarlaft?.residenteFiscalEstadosUnidos === true
      || this.sarlaft?.residenteFiscalExterior === true
    );
  }

  get residenciaFiscal(): string {
    return this.unir([
      this.sarlaft?.paisResidenciaFiscal,
      this.sarlaft?.ciudadResidenciaFiscal,
      this.sarlaft?.direccionResidenciaFiscal
    ]);
  }

  get identificacionFiscal(): string {
    return this.unir([
      this.sarlaft?.tipoIdentificacionFiscal,
      this.sarlaft?.numeroIdentificacionFiscal
    ]);
  }

  // =========================================================
  // Condiciones de protección
  // =========================================================

  get tieneCondicionesProteccion(): boolean {
    return (
      this.sarlaft?.tieneInformacionCondicionesProteccion === true
      || this.sarlaft?.administraRecursosPublicos === true
      || this.sarlaft?.grupoProteccionEspecialConstitucional === true
      || this.sarlaft?.personaMayor60Anos === true
      || this.sarlaft?.discapacidadFisica === true
      || this.sarlaft?.victimaConflictoArmado === true
      || this.sarlaft?.pobrezaExtrema === true
      || this.sarlaft?.poblacionIndigena === true
      || this.sarlaft?.poblacionAfrodescendiente === true
      || this.sarlaft?.poblacionLgbtiqMas === true
      || this.sarlaft?.perteneceGrupoProteccionConstitucional === true
    );
  }

  // =========================================================
  // Actualización
  // =========================================================

  get fechaActualizacion(): string {
    return this.formatearFecha(
      this.sarlaft?.fechaActualizacion,
      'Sin fecha de actualización'
    );
  }

  get fechaCreacionDatos(): string {
    return this.formatearFecha(
      this.sarlaft?.fechaCreacionDatos,
      'Sin fecha de creación'
    );
  }

  get fechaEdicionDatos(): string {
    return this.formatearFecha(
      this.sarlaft?.fechaEdicionDatos,
      'Sin fecha de edición'
    );
  }

  // =========================================================
  // Utilidades para plantilla
  // =========================================================

  valorTexto(
    valor: unknown,
    predeterminado = 'Sin información'
  ): string {
    return this.texto(
      valor,
      predeterminado
    );
  }

  siNo(
    valor: boolean | null | undefined
  ): string {
    if (valor === true) {
      return 'Sí';
    }

    if (valor === false) {
      return 'No';
    }

    return 'Sin definir';
  }

  claseBooleano(
    valor: boolean | null | undefined
  ): string {
    if (valor === true) {
      return 'estado--si';
    }

    if (valor === false) {
      return 'estado--no';
    }

    return 'estado--indefinido';
  }

  formatearFecha(
    valor: string | null | undefined,
    predeterminado = ''
  ): string {
    if (!valor) {
      return predeterminado;
    }

    const fecha = new Date(valor);

    if (Number.isNaN(fecha.getTime())) {
      return valor;
    }

    return new Intl.DateTimeFormat(
      'es-CO',
      {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      }
    ).format(fecha);
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
}
