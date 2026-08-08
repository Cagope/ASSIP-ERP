import { CommonModule, Location } from '@angular/common';

import {
  Component,
  OnInit
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';

import {
  ExpedienteAsociadoApi
} from './expediente-asociado.api';

import {
  ExpedienteAsociado
} from './expediente-asociado.dto';

import {
  ExpedienteEncabezadoComponent
} from './encabezado/expediente-encabezado.component';

import {
  ExpedienteIndicadoresComponent
} from './indicadores/expediente-indicadores.component';

import {
  ExpedienteResumenComponent
} from './resumen/expediente-resumen.component';

import {
  ExpedienteAfiliacionComponent
} from './afiliacion/expediente-afiliacion.component';

import {
  ExpedienteContactoComponent
} from './contacto/expediente-contacto.component';

import {
  ExpedienteFinancieroComponent
} from './financiero/expediente-financiero.component';

import {
  ExpedienteSarlaftComponent
} from './sarlaft/expediente-sarlaft.component';

import {
  ExpedienteAhorrosComponent
} from './ahorros/expediente-ahorros.component';

import {
  ExpedienteCdatsComponent
} from './cdats/expediente-cdats.component';

import {
  ExpedienteCreditosComponent
} from './creditos/expediente-creditos.component';

import {
  ExpedienteBienesComponent
} from './bienes/expediente-bienes.component';

import {
  ExpedienteAlertasComponent
} from './alertas/expediente-alertas.component';

type SeccionExpediente =
  | 'RESUMEN'
  | 'AFILIACION'
  | 'CONTACTO'
  | 'FINANCIERO'
  | 'SARLAFT'
  | 'AHORROS'
  | 'CDATS'
  | 'CREDITOS'
  | 'BIENES'
  | 'GARANTIAS'
  | 'ALERTAS';


@Component({
  selector: 'app-expediente-asociado',
  standalone: true,
  imports: [
    CommonModule,
    ExpedienteEncabezadoComponent,
    ExpedienteIndicadoresComponent,
    ExpedienteResumenComponent,
    ExpedienteAfiliacionComponent,
    ExpedienteContactoComponent,
    ExpedienteFinancieroComponent,
    ExpedienteSarlaftComponent,
    ExpedienteAhorrosComponent,
    ExpedienteCdatsComponent,
    ExpedienteCreditosComponent,
    ExpedienteBienesComponent,
    ExpedienteAlertasComponent
  ],
  templateUrl: './expediente-asociado.component.html',
  styleUrls: ['./expediente-asociado.component.scss']
})
export class ExpedienteAsociadoComponent implements OnInit {

  // ========================================================
  // EXPEDIENTE
  // ========================================================

  idDatosPersonal = 0;

  expediente: ExpedienteAsociado | null = null;

  cargando = false;

  error = '';

  seccionActiva: SeccionExpediente = 'RESUMEN';


  constructor(
    private readonly route: ActivatedRoute,
    private readonly expedienteApi: ExpedienteAsociadoApi,
    private readonly location: Location
  ) {
  }


  // ========================================================
  // INICIALIZACIÓN
  // ========================================================

  ngOnInit(): void {

    const parametro =
      this.route.snapshot.paramMap.get(
        'idDatosPersonal'
      );

    const id =
      Number(parametro);

    if (
      !Number.isInteger(id) ||
      id <= 0
    ) {

      this.error =
        'No se recibió un asociado válido.';

      return;
    }

    this.idDatosPersonal = id;

    this.consultar();
  }


  // ========================================================
  // CONSULTA DEL EXPEDIENTE
  // ========================================================

  consultar(): void {

    if (
      this.cargando ||
      this.idDatosPersonal <= 0
    ) {
      return;
    }

    this.cargando = true;

    this.error = '';

    this.expedienteApi
      .consultarPorIdDatosPersonal(
        this.idDatosPersonal
      )
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({

        next: respuesta => {

          if (!respuesta.encontrado) {

            this.expediente = null;

            this.error =
              respuesta.mensaje?.trim() ||
              'No se encontró información del asociado.';

            return;
          }

          this.expediente = respuesta;
        },

        error: error => {

          console.error(
            'Error consultando el expediente del asociado:',
            error
          );

          this.expediente = null;

          this.error =
            error?.error?.mensaje ??
            error?.error?.message ??
            'No fue posible consultar el expediente del asociado.';
        }
      });
  }


  // ========================================================
  // NAVEGACIÓN DEL EXPEDIENTE
  // ========================================================

  seleccionarSeccion(
    seccion: SeccionExpediente
  ): void {

    this.seccionActiva = seccion;
  }

  esSeccionActiva(
    seccion: SeccionExpediente
  ): boolean {

    return this.seccionActiva === seccion;
  }

  volverABuscador(): void {
    this.location.back();
  }

}
