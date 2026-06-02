import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';

import { SessionService } from '../../../../core/auth/session.service';

import {
  CajaEstadoDTO,
  CajaProvisionVincularRequest,
  VincularCajaApi
} from './vincular-caja.api';

@Component({
  selector: 'app-vincular-caja',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    NumericFormatDirective
  ],
  templateUrl: './vincular-caja.component.html',
  styleUrls: ['./vincular-caja.component.scss']
})
export class VincularCajaComponent implements OnInit {

  fechaContable =
    new Date()
      .toISOString()
      .substring(0, 10);

  idAgencia = 0;

  idCaja: number | null = null;

  efectivoInicio = 0;
  chequesInicio = 0;

  cajasEstado: CajaEstadoDTO[] = [];

  cargando = false;

  mensaje = '';
  error = '';

  idProvisionActiva: number | null = null;
  cajaActivaTexto = '';

  constructor(
    private api: VincularCajaApi,
    private session: SessionService
  ) {
  }

  ngOnInit(): void {

    const agencia =
      this.session.getAgenciaActiva();

    this.idAgencia =
      agencia?.idAgencia ?? 0;

    this.cargarEstadoCajas();

  }

  cargarEstadoCajas(): void {

    this.cajasEstado = [];

    if (!this.idAgencia) {
      return;
    }

    this.api.listarEstadoCajas(
      this.idAgencia,
      this.fechaContable
    ).subscribe({

      next: data => {

        this.cajasEstado =
          data || [];

      },

      error: err => {

        console.error(err);

        this.error =
          err?.error?.message ||
          JSON.stringify(err?.error) ||
          'No fue posible cargar el estado de cajas.';

      }

    });

  }

  onChangeCaja(): void {

    this.efectivoInicio = 0;
    this.chequesInicio = 0;

  }

  seleccionarCajaDisponible(item: CajaEstadoDTO): void {

    if (!item.disponible) {
      this.error = 'Esta caja ya está vinculada a otro usuario.';
      return;
    }

    this.idCaja =
      item.idCaja;

    this.onChangeCaja();

  }

  vincular(): void {

    this.error = '';
    this.mensaje = '';

    if (!this.idCaja) {

      this.error =
        'Debe seleccionar una caja.';

      return;

    }

    const caja =
      this.cajasEstado.find(c => c.idCaja === this.idCaja);

    if (caja && !caja.disponible) {

      this.error =
        'Esta caja ya está vinculada a otro usuario.';

      return;

    }

    if (
      this.efectivoInicio < 0 ||
      this.chequesInicio < 0
    ) {

      this.error =
        'Los valores iniciales no pueden ser negativos.';

      return;

    }

    const request: CajaProvisionVincularRequest = {

      idCaja:
        this.idCaja,

      fechaContable:
        this.fechaContable,

      efectivoInicio:
        Number(this.efectivoInicio || 0),

      chequesInicio:
        Number(this.chequesInicio || 0)

    };

    this.cargando = true;

    this.api.vincular(request)
      .subscribe({

        next: idProvision => {

          localStorage.setItem(
            'cajaActivaIdProvision',
            String(idProvision)
          );

          localStorage.setItem(
            'cajaActivaIdCaja',
            String(this.idCaja)
          );

          localStorage.setItem(
            'cajaActivaFecha',
            this.fechaContable
          );

          this.idProvisionActiva =
            Number(idProvision);

          this.cajaActivaTexto =
            caja
              ? `${caja.codigoCaja} - ${caja.descripcionCaja}`
              : `Caja ${this.idCaja}`;

          this.mensaje =
            'Caja vinculada correctamente.';

          this.cargando = false;

          this.cargarEstadoCajas();

        },

        error: err => {

          this.error =
            err?.error?.message ||
            err?.error ||
            'No fue posible vincular la caja.';

          this.cargando = false;

        }

      });

  }

  limpiar(): void {

    this.idCaja = null;

    this.efectivoInicio = 0;
    this.chequesInicio = 0;

    this.error = '';
    this.mensaje = '';

  }

}
