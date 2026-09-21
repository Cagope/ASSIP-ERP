import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { OriginacionFormalizacionApi } from './originacion-formalizacion.api';
import { SolicitudFormalizacionDetalle, SolicitudFormalizacionGuardarRequest } from './originacion-formalizacion.models';
import { FormaPago, ModalidadInteres, TipoCuota } from '../solicitud/originacion-solicitud.models';

@Component({
  selector: 'app-originacion-formalizacion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './originacion-formalizacion.component.html',
  styleUrl: './originacion-formalizacion.component.scss'
})
export class OriginacionFormalizacionComponent implements OnInit {
  private readonly api = inject(OriginacionFormalizacionApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  idBusqueda: number | null = null;
  detalle: SolicitudFormalizacionDetalle | null = null;
  formulario: SolicitudFormalizacionGuardarRequest = this.formularioVacio();
  formasPago: FormaPago[] = [];
  modalidadesInteres: ModalidadInteres[] = [];
  tiposCuota: TipoCuota[] = [];
  cargando = false;
  guardando = false;
  finalizando = false;
  error = '';
  mensaje = '';

  get editable(): boolean {
    return this.detalle?.idSolicitudProceso === 4
      && this.detalle?.idSolicitudResultado === 1
      && !this.detalle.fechaFinFormalizacion;
  }

  get modalidadSeleccionada(): ModalidadInteres | undefined {
    return this.modalidadesInteres.find(m =>
      m.periodoCodigo === this.formulario.periodoCodigoInteresFormalizado
      && m.tipoModalidad === this.formulario.tipoModalidadInteresFormalizado);
  }

  get cambiosPendientes(): boolean {
    const d = this.detalle;
    const f = this.formulario;
    if (!d) { return false; }
    return d.valorSolicitado !== f.valorFormalizado
      || d.plazoSolicitado !== f.plazoFormalizado
      || d.codigoFormaPago?.trim() !== f.codigoFormaPagoFormalizada
      || d.periodoCodigoInteres?.trim() !== f.periodoCodigoInteresFormalizado
      || d.tipoModalidadInteres?.trim() !== f.tipoModalidadInteresFormalizado
      || d.amortizacionCapital !== f.amortizacionCapitalFormalizada
      || d.codigoTipoCuota?.trim() !== f.codigoTipoCuotaFormalizada
      || d.tasaColocacionAplicada !== f.tasaNominalFormalizada;
  }

  get hayEdicionSinGuardar(): boolean {
    const d = this.detalle;
    const f = this.formulario;
    if (!d) { return false; }
    return d.valorFormalizado !== f.valorFormalizado
      || d.plazoFormalizado !== f.plazoFormalizado
      || d.codigoFormaPagoFormalizada !== f.codigoFormaPagoFormalizada
      || d.periodoCodigoInteresFormalizado !== f.periodoCodigoInteresFormalizado
      || d.tipoModalidadInteresFormalizado !== f.tipoModalidadInteresFormalizado
      || d.amortizacionCapitalFormalizada !== f.amortizacionCapitalFormalizada
      || d.codigoTipoCuotaFormalizada !== f.codigoTipoCuotaFormalizada
      || d.tasaNominalFormalizada !== f.tasaNominalFormalizada;
  }

  ngOnInit(): void {
    forkJoin({
      formasPago: this.api.listarFormasPago(),
      modalidadesInteres: this.api.listarModalidadesInteres(),
      tiposCuota: this.api.listarTiposCuota()
    }).subscribe({
      next: catalogos => {
        this.formasPago = catalogos.formasPago ?? [];
        this.modalidadesInteres = catalogos.modalidadesInteres ?? [];
        this.tiposCuota = catalogos.tiposCuota ?? [];
      },
      error: err => { this.error = this.mensajeError(err); }
    });
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('idSolicitudCredito'));
      if (Number.isInteger(id) && id > 0) {
        this.idBusqueda = id;
        this.cargar(id);
      } else {
        this.detalle = null;
      }
    });
  }

  buscar(): void {
    const id = Number(this.idBusqueda);
    if (!Number.isInteger(id) || id <= 0) {
      this.error = 'Ingrese un identificador de solicitud válido.';
      return;
    }
    void this.router.navigate(['/cartera/originacion/formalizacion', id]);
  }

  cargar(id: number): void {
    this.cargando = true;
    this.error = '';
    this.mensaje = '';
    this.detalle = null;
    this.api.consultar(id).subscribe({
      next: detalle => {
        this.detalle = detalle;
        this.formulario = this.desdeDetalle(detalle);
        this.cargando = false;
      },
      error: err => {
        this.cargando = false;
        this.error = this.mensajeError(err);
      }
    });
  }

  guardar(): void {
    if (!this.editable || this.guardando || this.finalizando || !this.detalle) { return; }
    if (!this.validarFormulario()) { return; }
    this.guardando = true;
    this.error = '';
    this.mensaje = '';
    const id = this.detalle.idSolicitudCredito;
    this.api.guardar(id, { ...this.formulario, mesesGraciaCapitalFormalizados: 0, mesesGraciaInteresFormalizados: 0 })
      .subscribe({
        next: detalle => {
          this.detalle = detalle;
          this.formulario = this.desdeDetalle(detalle);
          this.guardando = false;
          this.mensaje = 'Condiciones definitivas guardadas correctamente.';
        },
        error: err => {
          this.guardando = false;
          this.error = this.mensajeError(err);
        }
      });
  }

  finalizar(): void {
    if (!this.editable || this.guardando || this.finalizando || !this.detalle) { return; }
    if (this.detalle.valorFormalizado === null || this.hayEdicionSinGuardar) {
      this.error = 'Guarde las condiciones definitivas antes de finalizar.';
      return;
    }
    if (!window.confirm('¿Finalizar formalización y enviar la solicitud al proceso de desembolso?')) { return; }
    this.finalizando = true;
    this.error = '';
    this.mensaje = '';
    this.api.finalizar(this.detalle.idSolicitudCredito).subscribe({
      next: detalle => {
        this.detalle = detalle;
        this.formulario = this.desdeDetalle(detalle);
        this.finalizando = false;
        this.mensaje = 'Formalización finalizada. La solicitud pasó al proceso de desembolso.';
      },
      error: err => {
        this.finalizando = false;
        this.error = this.mensajeError(err);
      }
    });
  }

  volver(): void { void this.router.navigate(['/cartera/originacion']); }

  private validarFormulario(): boolean {
    const f = this.formulario;
    if (!(f.valorFormalizado > 0) || !Number.isInteger(f.plazoFormalizado) || f.plazoFormalizado <= 0
      || !Number.isInteger(f.amortizacionCapitalFormalizada) || f.amortizacionCapitalFormalizada <= 0
      || f.amortizacionCapitalFormalizada > f.plazoFormalizado || f.tasaNominalFormalizada < 0
      || !Number.isFinite(f.tasaNominalFormalizada) || !f.codigoFormaPagoFormalizada
      || !f.periodoCodigoInteresFormalizado || !f.tipoModalidadInteresFormalizado
      || !f.codigoTipoCuotaFormalizada) {
      this.error = 'Complete las condiciones definitivas con valores válidos.';
      return false;
    }
    if (f.codigoTipoCuotaFormalizada === '1'
      && this.modalidadSeleccionada
      && this.modalidadSeleccionada.periodoMeses !== f.amortizacionCapitalFormalizada) {
      this.error = 'Para cuota fija, la periodicidad de intereses debe coincidir con la amortización de capital.';
      return false;
    }
    return true;
  }

  private desdeDetalle(d: SolicitudFormalizacionDetalle): SolicitudFormalizacionGuardarRequest {
    return {
      valorFormalizado: d.valorFormalizado ?? d.valorSolicitado ?? 0,
      plazoFormalizado: d.plazoFormalizado ?? d.plazoSolicitado ?? 0,
      codigoFormaPagoFormalizada: d.codigoFormaPagoFormalizada ?? d.codigoFormaPago ?? '',
      periodoCodigoInteresFormalizado: d.periodoCodigoInteresFormalizado ?? d.periodoCodigoInteres ?? '',
      tipoModalidadInteresFormalizado: d.tipoModalidadInteresFormalizado ?? d.tipoModalidadInteres ?? '',
      amortizacionCapitalFormalizada: d.amortizacionCapitalFormalizada ?? d.amortizacionCapital ?? 0,
      codigoTipoCuotaFormalizada: d.codigoTipoCuotaFormalizada ?? d.codigoTipoCuota ?? '',
      mesesGraciaCapitalFormalizados: 0,
      mesesGraciaInteresFormalizados: 0,
      tasaNominalFormalizada: d.tasaNominalFormalizada ?? d.tasaColocacionAplicada ?? 0
    };
  }

  private formularioVacio(): SolicitudFormalizacionGuardarRequest {
    return {
      valorFormalizado: 0, plazoFormalizado: 0, codigoFormaPagoFormalizada: '',
      periodoCodigoInteresFormalizado: '', tipoModalidadInteresFormalizado: '',
      amortizacionCapitalFormalizada: 0, codigoTipoCuotaFormalizada: '',
      mesesGraciaCapitalFormalizados: 0, mesesGraciaInteresFormalizados: 0,
      tasaNominalFormalizada: 0
    };
  }

  private mensajeError(err: any): string {
    const mensaje = err?.error?.message ?? err?.error?.mensaje ?? err?.error?.error;
    return typeof mensaje === 'string' && mensaje.trim()
      ? mensaje.trim()
      : 'No fue posible completar la operación de formalización.';
  }
}
