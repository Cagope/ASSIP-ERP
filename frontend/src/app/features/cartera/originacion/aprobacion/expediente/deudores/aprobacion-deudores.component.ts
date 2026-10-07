import {
  CommonModule
} from '@angular/common';

import {
  Component,
  Input
} from '@angular/core';


@Component({
  selector: 'app-aprobacion-deudores',
  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl:
    './aprobacion-deudores.component.html',

  styleUrl:
    './aprobacion-deudores.component.scss'
})
export class AprobacionDeudoresComponent {

  // =========================================================
  // ENTRADAS
  // =========================================================

  @Input()
  foto:
    Record<string, unknown> |
    Record<string, unknown>[] |
    null = null;

  @Input()
  deudores:
    Record<string, unknown>[] |
    null = null;


  // =========================================================
  // DATOS DE LA FOTOGRAFÍA
  // =========================================================

  get items():
    Record<string, unknown>[] {

    return Array.isArray(
      this.foto
    )
      ? this.foto
      : [];
  }


  get actual():
    Record<string, unknown> {

    return (
      !Array.isArray(this.foto) &&
      this.foto
    )
      ? this.foto
      : {};
  }


  // =========================================================
  // VALORES
  // =========================================================

  valor(
    objeto: Record<string, unknown>,
    campo: string
  ): string {

    const valor =
      objeto[campo];

    if (
      valor === null ||
      valor === undefined ||
      valor === ''
    ) {
      return 'No registrado';
    }

    return String(valor);
  }


  texto(
    objeto: Record<string, unknown>,
    campo: string
  ): string {

    return this.valor(
      objeto,
      campo
    ).replaceAll(
      '_',
      ' '
    );
  }


  numero(
    objeto: Record<string, unknown>,
    campo: string
  ): number | null {

    const valor =
      objeto[campo];

    if (
      valor === null ||
      valor === undefined ||
      valor === ''
    ) {
      return null;
    }

    const numero =
      Number(valor);

    return Number.isFinite(numero)
      ? numero
      : null;
  }


  // =========================================================
  // FORMATO
  // =========================================================

  dinero(
    objeto: Record<string, unknown>,
    campo: string
  ): string {

    const numero =
      this.numero(
        objeto,
        campo
      );

    if (numero === null) {
      return 'No registrado';
    }

    return new Intl.NumberFormat(
      'es-CO',
      {
        style: 'currency',
        currency: 'COP',
        maximumFractionDigits: 0
      }
    ).format(numero);
  }


  porcentaje(
    objeto: Record<string, unknown>,
    campo: string,
    decimales = 2
  ): string {

    const numero =
      this.numero(
        objeto,
        campo
      );

    if (numero === null) {
      return 'No registrado';
    }

    return (
      new Intl.NumberFormat(
        'es-CO',
        {
          maximumFractionDigits:
            decimales
        }
      ).format(numero)
      + ' %'
    );
  }


  fecha(
    objeto: Record<string, unknown>,
    campo: string
  ): string {

    const valor =
      objeto[campo];

    if (
      typeof valor !== 'string' ||
      !valor
    ) {
      return 'No registrada';
    }

    const partes =
      valor
        .slice(
          0,
          10
        )
        .split('-');

    if (partes.length !== 3) {
      return 'No registrada';
    }

    return (
      `${partes[2]}/${partes[1]}/${partes[0]}`
    );
  }


  // =========================================================
  // ESTADOS
  // =========================================================

  estado(
    objeto: Record<string, unknown>,
    campo: string
  ): string {

    const valor =
      objeto[campo];

    if (valor === true) {
      return 'Cumple';
    }

    if (valor === false) {
      return 'No cumple';
    }

    return 'No evaluado';
  }


  // =========================================================
  // DEUDORES
  // =========================================================

  nombre(
    idSolicitudDeudor: unknown
  ): string {

    const deudor =
      (this.deudores ?? [])
        .find(
          item =>
            item[
              'id_solicitud_deudor'
            ] === idSolicitudDeudor
        );

    if (!deudor) {
      return (
        'Nombre no disponible en fotografía'
      );
    }

    return this.valor(
      deudor,
      'nombre_completo'
    );
  }


  rol(
    objeto: Record<string, unknown>
  ): string {

    const tipoDeudor =
      this.texto(
        objeto,
        'tipo_deudor'
      );

    if (tipoDeudor === 'PRINCIPAL') {
      return 'Titular';
    }

    if (tipoDeudor === 'CODEUDOR') {
      return 'Codeudor';
    }

    return tipoDeudor;
  }
}
