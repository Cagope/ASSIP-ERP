import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { OriginacionFormalizacionApi } from
  '../originacion-formalizacion.api';

import {
  SolicitudFormalizacionBandeja
} from '../originacion-formalizacion.models';

@Component({
  selector: 'app-originacion-formalizacion-list',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './originacion-formalizacion-list.component.html',
  styleUrl: './originacion-formalizacion-list.component.scss'
})
export class OriginacionFormalizacionListComponent
  implements OnInit {

  private readonly api =
    inject(OriginacionFormalizacionApi);

  private readonly router =
    inject(Router);

  solicitudes: SolicitudFormalizacionBandeja[] = [];

  cargando = false;
  error = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {

    this.cargando = true;
    this.error = '';

    this.api.listarBandeja().subscribe({

      next: solicitudes => {

        this.solicitudes = solicitudes;
        this.cargando = false;
      },

      error: () => {

        this.solicitudes = [];
        this.error =
          'No fue posible consultar las solicitudes pendientes de formalización.';

        this.cargando = false;
      }

    });
  }

  abrirFormalizacion(
    solicitud: SolicitudFormalizacionBandeja
  ): void {

    this.router.navigate([
      '/cartera/originacion/formalizacion',
      solicitud.idSolicitudCredito
    ]);
  }

  descripcionEstado(
    estado: string
  ): string {

    switch (estado) {

      case 'PAGARE_GENERADO':
        return 'Pagaré generado';

      case 'CONDICIONES_GUARDADAS':
        return 'Condiciones guardadas';

      default:
        return 'Pendiente';
    }
  }

}
