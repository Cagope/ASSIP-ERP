import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

import {
  ExpedienteParticipacionInstitucional
} from '../expediente-asociado.dto';


@Component({
  selector: 'app-expediente-participacion-institucional',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl:
    './expediente-participacion-institucional.component.html',
  styleUrl:
    './expediente-participacion-institucional.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteParticipacionInstitucionalComponent {

  @Input()
  participaciones: ExpedienteParticipacionInstitucional[] = [];

  get listaOrdenada(): ExpedienteParticipacionInstitucional[] {
    return [...(this.participaciones ?? [])]
      .sort((a, b) => {

        const ordenA = a.orden ?? 999;
        const ordenB = b.orden ?? 999;

        if (ordenA !== ordenB) {
          return ordenA - ordenB;
        }

        return this.obtenerNombrePrincipal(a)
          .localeCompare(
            this.obtenerNombrePrincipal(b),
            'es',
            {
              sensitivity: 'base'
            }
          );
      });
  }

  get tieneParticipaciones(): boolean {
    return this.listaOrdenada.length > 0;
  }

  obtenerNombrePrincipal(
    participacion: ExpedienteParticipacionInstitucional
  ): string {

    switch (participacion.tipoParticipacion) {

      case 'DIRECTIVO':
        return participacion.nombreTipoDirectivo
          ?? 'Cargo directivo';

      case 'COMITE':
        return participacion.nombreComite
          ?? 'Comité institucional';

      case 'PRIVILEGIADO':
        return participacion.nombreTipoDirectivo
          ?? 'Relación privilegiada';

      case 'PERSONA_RELACIONADA':
        return participacion.nombreRelacionado
          ?? 'Persona relacionada';

      default:
        return participacion.nombreTipoDirectivo
          ?? participacion.nombreComite
          ?? participacion.nombreRelacionado
          ?? 'Participación institucional';
    }
  }

  obtenerDetallePrincipal(
    participacion: ExpedienteParticipacionInstitucional
  ): string {

    switch (participacion.tipoParticipacion) {

      case 'DIRECTIVO':
        return participacion.nombreCalidadDirectivo
          ?? '';

      case 'COMITE':
        return participacion.nombreCargoComite
          ?? '';

      case 'PRIVILEGIADO':
        return participacion.nombreParentesco
          ?? 'Relación privilegiada';

      case 'PERSONA_RELACIONADA':
        return this.obtenerDetallePersonaRelacionada(
          participacion
        );

      default:
        return '';
    }
  }

  obtenerDetallePersonaRelacionada(
    participacion: ExpedienteParticipacionInstitucional
  ): string {

    const partes: string[] = [];

    if (participacion.nombreParentesco) {
      partes.push(participacion.nombreParentesco);
    }

    if (participacion.nombreTipoDirectivo) {
      partes.push(
        `Relacionado con ${participacion.nombreTipoDirectivo}`
      );
    }

    return partes.join(' · ');
  }

  obtenerEtiquetaTipo(
    tipo: string | null
  ): string {

    switch (tipo) {

      case 'DIRECTIVO':
        return 'Directivo';

      case 'COMITE':
        return 'Comité';

      case 'PRIVILEGIADO':
        return 'Privilegiado';

      case 'PERSONA_RELACIONADA':
        return 'Persona relacionada';

      default:
        return tipo || 'Participación';
    }
  }

  obtenerClaseTipo(
    tipo: string | null
  ): string {

    switch (tipo) {

      case 'DIRECTIVO':
        return 'tipo-directivo';

      case 'COMITE':
        return 'tipo-comite';

      case 'PRIVILEGIADO':
        return 'tipo-privilegiado';

      case 'PERSONA_RELACIONADA':
        return 'tipo-relacionada';

      default:
        return 'tipo-general';
    }
  }

  mostrarEstadoActivo(
    participacion: ExpedienteParticipacionInstitucional
  ): boolean {

    const estado =
      participacion.nombreEstadoDirectivo
        ?.trim()
        .toUpperCase();

    return estado === 'ACTIVO'
      || estado === 'VIGENTE';
  }

  mostrarEstadoInactivo(
    participacion: ExpedienteParticipacionInstitucional
  ): boolean {

    return !!participacion.nombreEstadoDirectivo
      && !this.mostrarEstadoActivo(participacion);
  }

  trackByParticipacion(
    index: number,
    participacion: ExpedienteParticipacionInstitucional
  ): string {

    return [
      participacion.tipoParticipacion ?? 'PARTICIPACION',
      participacion.idDirectivo ?? '',
      participacion.idComiteDetalle ?? '',
      participacion.idPrivilegiado ?? '',
      participacion.idPersonaRelacionada ?? '',
      index
    ].join('-');
  }
}
