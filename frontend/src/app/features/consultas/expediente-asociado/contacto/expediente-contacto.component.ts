import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

import {
  ExpedienteContacto
} from '../expediente-asociado.dto';

@Component({
  selector: 'app-expediente-contacto',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-contacto.component.html',
  styleUrls: ['./expediente-contacto.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteContactoComponent {

  @Input()
  contacto: ExpedienteContacto | null = null;

  // =========================================================
  // IDENTIFICACIÓN
  // =========================================================

  get nombreCompleto(): string {
    return this.texto(
      this.contacto?.nombreCompleto,
      'Sin nombre registrado'
    );
  }

  get documentoCompleto(): string {
    const tipoDocumento = this.texto(
      this.contacto?.nombreTipoDocumento ??
      this.contacto?.tipoDocumento
    );

    const documento = this.texto(
      this.contacto?.documento
    );

    return this.unir(
      [
        tipoDocumento,
        documento
      ],
      ' ',
      'Sin documento registrado'
    );
  }

  // =========================================================
  // DIRECCIÓN PRINCIPAL
  // =========================================================

  get direccionPrincipal(): string {
    return this.unir([
      this.contacto?.direccionPrincipal,
      this.contacto?.complementoDireccion
    ]);
  }

  get ubicacionPrincipal(): string {
    return this.unir([
      this.contacto?.nombreCiudad,
      this.contacto?.nombreDepartamento,
      this.contacto?.nombrePais
    ]);
  }

  // =========================================================
  // CORREO PRINCIPAL
  // =========================================================

  get correoPrincipal(): string {
    return this.texto(
      this.contacto?.correoPrincipal,
      'Sin correo registrado'
    );
  }

  // =========================================================
  // AUTORIZACIONES
  // =========================================================

  autorizacionTexto(
    valor: boolean | null | undefined
  ): string {
    if (valor === true) {
      return 'Autorizado';
    }

    if (valor === false) {
      return 'No autorizado';
    }

    return 'Sin definir';
  }

  autorizacionClase(
    valor: boolean | null | undefined
  ): string {
    if (valor === true) {
      return 'autorizacion--permitida';
    }

    if (valor === false) {
      return 'autorizacion--denegada';
    }

    return 'autorizacion--neutral';
  }

  // =========================================================
  // UTILIDADES DE PRESENTACIÓN
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
