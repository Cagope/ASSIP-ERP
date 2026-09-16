import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { OriginacionSolicitudStateService } from '../solicitud/originacion-solicitud-state.service';
import { OriginacionBienesApi } from './originacion-bienes.api';
import {
  SolicitudBien,
  SolicitudBienCreditoRespaldado,
  SolicitudBienSeleccionRequest
} from './originacion-bienes.models';

@Component({
  selector: 'app-originacion-bienes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './originacion-bienes.component.html',
  styleUrl: './originacion-bienes.component.scss'
})
export class OriginacionBienesComponent implements OnInit {
  idSolicitudCredito: number | null = null;
  bienes: SolicitudBien[] = [];

  cargando = false;
  guardandoIdBienPersona: number | null = null;
  validando = false;
  error = '';
  mensaje = '';

  bienDetalle: SolicitudBien | null = null;
  creditosRespaldados: SolicitudBienCreditoRespaldado[] = [];
  cargandoCreditos = false;
  errorCreditos = '';

  constructor(
    private readonly api: OriginacionBienesApi,
    private readonly state: OriginacionSolicitudStateService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.idSolicitudCredito = this.state.getIdSolicitudCredito();

    if (!this.idSolicitudCredito) {
      this.error = 'No se encontró una solicitud de crédito activa.';
      return;
    }

    this.cargar();
  }

  cargar(): void {
    if (!this.idSolicitudCredito) {
      return;
    }

    this.cargando = true;
    this.error = '';
    this.mensaje = '';

    this.api.listarPorSolicitud(this.idSolicitudCredito).subscribe({
      next: bienes => {
        this.bienes = bienes ?? [];
        this.cargando = false;
      },
      error: err => {
        this.cargando = false;
        this.error = this.obtenerMensajeError(
          err,
          'No fue posible consultar los bienes de los participantes.'
        );
      }
    });
  }

  cambiarSeleccion(bien: SolicitudBien, seleccionado: boolean): void {
    if (this.guardandoIdBienPersona !== null) {
      return;
    }

    this.error = '';
    this.mensaje = '';
    this.guardandoIdBienPersona = bien.idBienPersona;

    const request: SolicitudBienSeleccionRequest = {
      idSolicitudDeudor: bien.idSolicitudDeudor,
      idBienPersona: bien.idBienPersona,
      seleccionado,
      observacion: bien.observacion?.trim() || null
    };

    this.api.seleccionar(request).subscribe({
      next: actualizado => {
        this.guardandoIdBienPersona = null;
        this.reemplazarBien(actualizado);
        this.mensaje = seleccionado
          ? 'Bien seleccionado para la solicitud.'
          : 'Bien retirado de la solicitud.';
      },
      error: err => {
        this.guardandoIdBienPersona = null;
        this.error = this.obtenerMensajeError(
          err,
          'No fue posible actualizar la selección del bien.'
        );
      }
    });
  }

  guardarObservacion(bien: SolicitudBien): void {
    if (!bien.seleccionado) {
      return;
    }

    this.cambiarSeleccion(bien, true);
  }

  verCreditosRespaldados(bien: SolicitudBien): void {
    this.bienDetalle = bien;
    this.creditosRespaldados = [];
    this.errorCreditos = '';
    this.cargandoCreditos = true;

    this.api.listarCreditosRespaldados(bien.idBien).subscribe({
      next: creditos => {
        this.creditosRespaldados = creditos ?? [];
        this.cargandoCreditos = false;
      },
      error: err => {
        this.cargandoCreditos = false;
        this.errorCreditos = this.obtenerMensajeError(
          err,
          'No fue posible consultar los créditos respaldados por el bien.'
        );
      }
    });
  }

  cerrarCreditosRespaldados(): void {
    this.bienDetalle = null;
    this.creditosRespaldados = [];
    this.errorCreditos = '';
    this.cargandoCreditos = false;
  }

  continuar(): void {
    if (!this.idSolicitudCredito || this.validando) {
      return;
    }

    this.validando = true;
    this.error = '';
    this.mensaje = '';

    this.api.validarParaContinuar(this.idSolicitudCredito).subscribe({
      next: resultado => {
        this.validando = false;

        if (!resultado?.puedeContinuar) {
          this.error = 'No es posible continuar con la información actual de bienes.';
          return;
        }

        this.router.navigate([
          '/cartera/originacion/financiero'
        ]);
      },
      error: err => {
        this.validando = false;
        this.error = this.obtenerMensajeError(
          err,
          'No es posible continuar. Revise los bienes seleccionados para la solicitud.'
        );
      }
    });
  }

  volver(): void {
    this.router.navigate([
      '/cartera/originacion/deudores'
    ]);
  }

  get cantidadSeleccionados(): number {
    return this.bienes.filter(bien => bien.seleccionado).length;
  }

  get valorComercialTotal(): number {
    return this.sumar(this.bienes.map(bien => bien.valorComercial));
  }

  get valorNetoTotal(): number {
    return this.sumar(this.bienes.map(bien => bien.valorNetoPropiedad));
  }

  get valorCreditosRespaldadosTotal(): number {
    return this.sumar(this.bienes.map(bien => bien.valorCreditosRespaldados));
  }

  etiquetaTipoDeudor(tipo: string | null): string {
    if (tipo === 'PRINCIPAL') {
      return 'Principal';
    }
    if (tipo === 'CODEUDOR') {
      return 'Codeudor';
    }
    return tipo || 'Participante';
  }

  etiquetaTipoGarantia(tipo: string | null): string {
    if (tipo === 'R') {
      return 'Real';
    }
    if (tipo === 'P') {
      return 'Personal';
    }
    return tipo || '-';
  }

  trackByBienPersona(_: number, bien: SolicitudBien): number {
    return bien.idBienPersona;
  }

  trackByCredito(_: number, credito: SolicitudBienCreditoRespaldado): number {
    return credito.idCarteraCredito;
  }

  private reemplazarBien(actualizado: SolicitudBien): void {
    const indice = this.bienes.findIndex(
      bien => bien.idSolicitudDeudor === actualizado.idSolicitudDeudor
        && bien.idBienPersona === actualizado.idBienPersona
    );

    if (indice < 0) {
      return;
    }

    this.bienes = this.bienes.map((bien, posicion) =>
      posicion === indice ? actualizado : bien
    );
  }

  private sumar(valores: Array<number | null | undefined>): number {
    return valores.reduce<number>(
      (total, valor) => total + Number(valor ?? 0),
      0
    );
  }

  private obtenerMensajeError(respuesta: any, predeterminado: string): string {
    return (
      respuesta?.error?.mensaje
      ?? respuesta?.error?.message
      ?? respuesta?.error?.error
      ?? respuesta?.message
      ?? predeterminado
    );
  }
}
