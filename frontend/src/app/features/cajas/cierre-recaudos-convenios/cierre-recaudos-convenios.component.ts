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
  CierreRecaudosConveniosApi,
  CierreRecaudosConveniosPreview,
  CierreRecaudosConveniosRequest
} from './cierre-recaudos-convenios.api';

import {
  RecaudoConvenioConvenio,
  RecaudosConveniosApi
} from '../recaudos-convenios/recaudos-convenios.api';

import {
  VincularCajaApi,
  CajaProvisionActivaDTO
} from '../provisiones/vincular-caja/vincular-caja.api';

@Component({
  selector: 'app-cierre-recaudos-convenios',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './cierre-recaudos-convenios.component.html',
  styleUrls: ['./cierre-recaudos-convenios.component.scss']
})
export class CierreRecaudosConveniosComponent implements OnInit {

  private readonly api =
    inject(CierreRecaudosConveniosApi);

  private readonly recaudosApi =
    inject(RecaudosConveniosApi);

  private readonly cajaApi =
    inject(VincularCajaApi);

  provisionActiva: CajaProvisionActivaDTO | null = null;

  convenios: RecaudoConvenioConvenio[] = [];

  fechaContable =
    new Date().toISOString().substring(0, 10);

  idConvenio: number | null = null;
  documentoSoporte = '';

  previewData: CierreRecaudosConveniosPreview | null = null;

  cargando = false;
  consultando = false;
  aplicando = false;

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

          if (data?.fechaContable) {
            this.fechaContable = data.fechaContable;
          }

          if (data?.idAgencia) {
            this.cargarConvenios(data.idAgencia);
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

    this.recaudosApi.listarConveniosActivos(idAgencia)
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

  consultar(): void {

    this.error = '';
    this.mensaje = '';
    this.previewData = null;

    const request =
      this.construirRequest();

    if (!request) {
      return;
    }

    this.consultando = true;

    this.api.preview(request)
      .pipe(
        finalize(() => this.consultando = false)
      )
      .subscribe({
        next: data => {

          this.previewData = data;

          if (data.errores && data.errores.length > 0) {
            this.error = data.errores.join(' ');
          } else {
            this.mensaje =
              data.mensaje || 'Consulta realizada correctamente.';
          }
        },
        error: err => {

          console.error(err);

          this.error =
            err?.error?.message
            || 'No fue posible consultar el cierre.';
        }
      });
  }

  aplicar(): void {

    this.error = '';
    this.mensaje = '';

    const request =
      this.construirRequest();

    if (!request) {
      return;
    }

    if (!this.previewData?.permiteAplicar) {
      this.error =
        'Debe consultar un cierre válido antes de aplicar.';
      return;
    }

    const ok = confirm(
      '¿Aplicar el cierre de recaudos del convenio seleccionado?'
    );

    if (!ok) {
      return;
    }

    this.aplicando = true;

    this.api.aplicar(request)
      .pipe(
        finalize(() => this.aplicando = false)
      )
      .subscribe({
        next: resp => {

          this.mensaje =
            resp.mensaje || 'Cierre aplicado correctamente.';

          this.previewData = null;
          this.documentoSoporte = '';
        },
        error: err => {

          console.error(err);

          this.error =
            err?.error?.message
            || 'No fue posible aplicar el cierre.';
        }
      });
  }

  construirRequest(): CierreRecaudosConveniosRequest | null {

    if (!this.provisionActiva?.idCaja) {
      this.error = 'No hay caja activa.';
      return null;
    }

    if (!this.fechaContable) {
      this.error = 'Digite la fecha contable.';
      return null;
    }

    if (!this.idConvenio) {
      this.error = 'Seleccione un convenio.';
      return null;
    }

    if (!this.documentoSoporte.trim()) {
      this.error = 'Digite el documento soporte.';
      return null;
    }

    const documento =
      this.documentoSoporte.trim();

    if (!/^\d+$/.test(documento)) {
      this.error = 'El documento soporte debe ser numérico.';
      return null;
    }

    if (documento.length > 7) {
      this.error =
        'El documento soporte no puede tener más de 7 dígitos.';
      return null;
    }

    return {
      idCaja: this.provisionActiva.idCaja,
      fechaContable: this.fechaContable,
      idConvenio: Number(this.idConvenio),
      documentoSoporte: documento
    };
  }

  totalPreview(): number {
    return Number(this.previewData?.valorTotal || 0);
  }
}
