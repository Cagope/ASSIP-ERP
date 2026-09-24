import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { SolicitudListApi } from './solicitud-list.api';
import {
  SolicitudListado,
  SolicitudListadoFiltros
} from './solicitud-list.models';


@Component({
  selector: 'app-solicitud-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './solicitud-list.component.html',
  styleUrl: './solicitud-list.component.scss'
})
export class SolicitudListComponent implements OnInit {

  private readonly api = inject(SolicitudListApi);
  private readonly router = inject(Router);


  // =========================================================
  // CONSTANTES
  // =========================================================

  private readonly ID_RESULTADO_EN_CURSO = 1;


  // =========================================================
  // ESTADO
  // =========================================================

  solicitudes: SolicitudListado[] = [];

  cargando = false;

  error = '';

  filtros: SolicitudListadoFiltros =
    this.crearFiltrosIniciales();


  // =========================================================
  // CICLO DE VIDA
  // =========================================================

  ngOnInit(): void {
    this.buscar();
  }


  // =========================================================
  // CONSULTA
  // =========================================================

  buscar(): void {

    if (this.cargando) {
      return;
    }

    this.cargando = true;
    this.error = '';

    this.api.listar(this.filtros)
      .subscribe({

        next: (solicitudes) => {

          this.solicitudes =
            solicitudes ?? [];

          this.cargando = false;
        },

        error: (error) => {

          console.error(
            'Error consultando solicitudes de originación',
            error
          );

          this.solicitudes = [];

          this.error =
            this.obtenerMensajeError(error);

          this.cargando = false;
        }
      });
  }


  // =========================================================
  // FILTROS
  // =========================================================

  limpiarFiltros(): void {

    this.filtros =
      this.crearFiltrosIniciales();

    this.buscar();
  }


  private crearFiltrosIniciales():
    SolicitudListadoFiltros {

    return {
      idAgencia: null,
      numeroSolicitud: '',
      documento: '',
      nombreSolicitante: '',
      idAsesor: null,
      idSolicitudProceso: null,

      // La bandeja inicia mostrando solicitudes pendientes.
      idSolicitudResultado:
        this.ID_RESULTADO_EN_CURSO
    };
  }


  // =========================================================
  // NAVEGACIÓN
  // =========================================================

  nuevaSolicitud(): void {

    this.router.navigate([
      '/cartera/originacion/solicitud'
    ]);
  }


  // =========================================================
  // NAVEGACIÓN SEGÚN EL PROCESO DE LA SOLICITUD
  // =========================================================

  abrirSolicitud(
    solicitud: SolicitudListado
  ): void {

    if (!solicitud?.idSolicitudCredito) {
      return;
    }

    // =======================================================
    // FORMALIZACIÓN - PROCESO 4
    // =======================================================

    if (solicitud.idSolicitudProceso === 4) {

      void this.router.navigate([
        '/cartera/originacion/formalizacion',
        solicitud.idSolicitudCredito
      ]);

      return;
    }

    // =======================================================
    // SOLICITUD Y DEMÁS PROCESOS
    // Conservamos la navegación existente.
    // =======================================================

    if (
      !solicitud.idDatosPersonal
      || !solicitud.idAgencia
    ) {
      return;
    }

    void this.router.navigate(
      [
        '/cartera/originacion/solicitud'
      ],
      {
        queryParams: {
          idSolicitudCredito:
            solicitud.idSolicitudCredito,

          idDatosPersonal:
            solicitud.idDatosPersonal,

          idAgencia:
            solicitud.idAgencia
        }
      }
    );
  }

  // =========================================================
  // NAVEGACIÓN A FORMALIZACIÓN
  // =========================================================

  abrirFormalizacion(
    solicitud: SolicitudListado
  ): void {

    if (
      solicitud.idSolicitudProceso !== 4
      || !solicitud.idSolicitudCredito
    ) {
      return;
    }

    void this.router.navigate([
      '/cartera/originacion/formalizacion',
      solicitud.idSolicitudCredito
    ]);
  }

  // =========================================================
  // EVENTOS
  // =========================================================

  alPresionarEnter(
    event: Event
  ): void {

    event.preventDefault();
    this.buscar();
  }


  // =========================================================
  // PRESENTACIÓN
  // =========================================================

  trackBySolicitud(
    _index: number,
    solicitud: SolicitudListado
  ): number {

    return solicitud.idSolicitudCredito;
  }


  // =========================================================
  // ERRORES
  // =========================================================

  private obtenerMensajeError(
    error: any
  ): string {

    const mensajeBackend =
      error?.error?.message
      ?? error?.error?.mensaje
      ?? error?.error?.error;

    if (
      typeof mensajeBackend === 'string'
      && mensajeBackend.trim()
    ) {
      return mensajeBackend.trim();
    }

    return 'No fue posible consultar las solicitudes de crédito.';
  }
}
