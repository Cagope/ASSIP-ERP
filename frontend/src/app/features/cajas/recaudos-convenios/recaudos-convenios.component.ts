import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';

import {
  finalize
} from 'rxjs';

import {
  RecaudoConvenio,
  RecaudoConvenioConvenio,
  RecaudosConveniosApi
} from './recaudos-convenios.api';

import {
  VincularCajaApi,
  CajaProvisionActivaDTO
} from '../provisiones/vincular-caja/vincular-caja.api';

import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';

@Component({
  selector: 'app-recaudos-convenios',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NumericFormatDirective
  ],
  templateUrl: './recaudos-convenios.component.html',
  styleUrls: ['./recaudos-convenios.component.scss']
})
export class RecaudosConveniosComponent implements OnInit {

  private readonly api =
    inject(RecaudosConveniosApi);

  private readonly cajaApi =
    inject(VincularCajaApi);

  provisionActiva: CajaProvisionActivaDTO | null = null;

  convenios: RecaudoConvenioConvenio[] = [];
  recaudos: RecaudoConvenio[] = [];

  idConvenio: number | null = null;
  documentoSoporte = '';
  valorRecaudo = 0;

  convenioSeleccionado: RecaudoConvenioConvenio | null = null;

  cargando = false;
  guardando = false;

  error = '';
  mensaje = '';

  ngOnInit(): void {
    this.cargarProvisionActiva();
  }

  cargarProvisionActiva(): void {

    this.cargando = true;
    this.error = '';

    this.cajaApi.obtenerProvisionActivaUsuario()
      .pipe(
        finalize(() => this.cargando = false)
      )
      .subscribe({
        next: data => {

          this.provisionActiva = data;

          if (data?.idAgencia) {
            this.cargarConvenios(data.idAgencia);
          }

          if (data?.idProvision) {
            this.cargarRecaudos(data.idProvision);
          }
        },
        error: err => {

          console.error(err);

          this.error =
            err?.error?.message
            || 'El usuario no tiene una caja/provisión activa.';
        }
      });
  }

  cargarConvenios(
    idAgencia: number
  ): void {

    this.api.listarConveniosActivos(idAgencia)
      .subscribe({
        next: data => {
          this.convenios = data || [];
        },
        error: err => {

          console.error(err);

          this.error =
            err?.error?.message
            || 'No fue posible cargar los convenios activos.';
        }
      });
  }

  cargarRecaudos(
    idProvision: number
  ): void {

    this.api.listarPorProvision(idProvision)
      .subscribe({
        next: data => {
          this.recaudos = data || [];
        },
        error: err => {

          console.error(err);

          this.error =
            err?.error?.message
            || 'No fue posible cargar los recaudos.';
        }
      });
  }

  seleccionarConvenio(): void {

    const id =
      Number(this.idConvenio || 0);

    this.convenioSeleccionado =
      this.convenios.find(
        c => Number(c.idConvenio) === id
      ) || null;
  }

  aplicar(): void {

    this.error = '';
    this.mensaje = '';

    if (!this.provisionActiva?.idProvision) {
      this.error = 'No hay una provisión activa.';
      return;
    }

    if (!this.idConvenio) {
      this.error = 'Seleccione un convenio.';
      return;
    }

    if (!this.documentoSoporte.trim()) {
      this.error = 'Digite el documento soporte.';
      return;
    }

    if (!this.valorRecaudo || Number(this.valorRecaudo) <= 0) {
      this.error = 'Digite un valor mayor a cero.';
      return;
    }

    this.guardando = true;

    this.api.aplicar({
      idProvision: this.provisionActiva.idProvision,
      idConvenio: Number(this.idConvenio),
      documentoSoporte: this.documentoSoporte.trim(),
      valorRecaudo: Number(this.valorRecaudo)
    }).pipe(
      finalize(() => this.guardando = false)
    ).subscribe({
      next: () => {

        this.mensaje =
          'Recaudo registrado correctamente.';

        this.limpiarFormulario();

        if (this.provisionActiva?.idProvision) {
          this.cargarRecaudos(
            this.provisionActiva.idProvision
          );
        }
      },
      error: err => {

        console.error(err);

        this.error =
          err?.error?.message
          || 'No fue posible registrar el recaudo.';
      }
    });
  }

  limpiarFormulario(): void {
    this.idConvenio = null;
    this.convenioSeleccionado = null;
    this.documentoSoporte = '';
    this.valorRecaudo = 0;
  }

  totalRecaudos(): number {
    return this.recaudos.reduce(
      (acc, item) => acc + Number(item.valorRecaudo || 0),
      0
    );
  }
}
