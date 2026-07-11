import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import {
  HeaderActionsComponent
} from '../../../shared/header-actions/header-actions.component';

import {
  DatosPersonalesApi,
  DatosPersonales
} from '../datos-personales/datos-personales.api';

import {
  AperturaCuentasApi
} from './apertura-cuentas.api';

import {
  SessionService
} from '../../../core/auth/session.service';

@Component({
  selector: 'app-apertura-cuentas-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './apertura-cuentas-list.component.html',
  styleUrls: ['./apertura-cuentas-list.component.scss']
})
export class AperturaCuentasListComponent
  implements OnInit {

  private readonly dpApi =
    inject(DatosPersonalesApi);

  private readonly api =
    inject(AperturaCuentasApi);

  private readonly router =
    inject(Router);

  private readonly session =
    inject(SessionService);

  personas: DatosPersonales[] = [];
  filtradas: DatosPersonales[] = [];

  cargando = false;
  validando = false;
  error = '';

  filtros = {
    documento: '',
    nombres: '',
    primerApellido: '',
    segundoApellido: ''
  };

  pagina = 1;
  tamanoPagina = 10;

  ngOnInit(): void {
    const agencia =
      this.session.getAgenciaActiva();

    if (!agencia) {
      this.error =
        'No se detectó la agencia activa del usuario.';

      return;
    }

    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';

    this.dpApi.listar()
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({
        next: lista => {
          this.personas = (lista ?? [])
            .sort((a, b) => {
              const fechaA =
                a.fechaActualizacion
                  ? Date.parse(a.fechaActualizacion)
                  : 0;

              const fechaB =
                b.fechaActualizacion
                  ? Date.parse(b.fechaActualizacion)
                  : 0;

              return fechaB - fechaA;
            });

          this.buscar();
        },
        error: err => {
          console.error(
            'Error cargando datos personales:',
            err
          );

          this.error =
            'No fue posible cargar los asociados.';

          this.personas = [];
          this.filtradas = [];
          this.pagina = 1;
        }
      });
  }

  buscar(): void {
    const documento =
      this.normalizarTexto(
        this.filtros.documento
      );

    const nombres =
      this.normalizarTexto(
        this.filtros.nombres
      );

    const primerApellido =
      this.normalizarTexto(
        this.filtros.primerApellido
      );

    const segundoApellido =
      this.normalizarTexto(
        this.filtros.segundoApellido
      );

    this.filtradas = this.personas.filter(
      persona => {
        const documentoPersona =
          this.normalizarTexto(
            persona.documento
          );

        const nombresPersona =
          this.normalizarTexto(
            persona.nombres
          );

        const primerApellidoPersona =
          this.normalizarTexto(
            persona.primerApellido
          );

        const segundoApellidoPersona =
          this.normalizarTexto(
            persona.segundoApellido
          );

        return (
          (
            !documento
            || documentoPersona.includes(
              documento
            )
          )
          &&
          (
            !nombres
            || nombresPersona.includes(
              nombres
            )
          )
          &&
          (
            !primerApellido
            || primerApellidoPersona.includes(
              primerApellido
            )
          )
          &&
          (
            !segundoApellido
            || segundoApellidoPersona.includes(
              segundoApellido
            )
          )
        );
      }
    );

    this.pagina = 1;
    this.error = '';
  }

  limpiar(): void {
    this.filtros = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };

    this.filtradas = [...this.personas];
    this.pagina = 1;
    this.error = '';
  }

  get paginadas(): DatosPersonales[] {
    const inicio =
      (this.pagina - 1) * this.tamanoPagina;

    return this.filtradas.slice(
      inicio,
      inicio + this.tamanoPagina
    );
  }

  totalPaginas(): number {
    return Math.max(
      1,
      Math.ceil(
        this.filtradas.length
        / this.tamanoPagina
      )
    );
  }

  cambiarPagina(pagina: number): void {
    if (
      pagina < 1
      || pagina > this.totalPaginas()
    ) {
      return;
    }

    this.pagina = pagina;
  }

  gestionar(
    persona: DatosPersonales
  ): void {

    this.error = '';

    if (!persona.idDatosPersonal) {
      this.error =
        'Asociado inválido: no tiene idDatosPersonal definido.';

      return;
    }

    const agencia =
      this.session.agenciaActivaSig();

    const idAgencia =
      agencia?.idAgencia
      ?? agencia?.id_agencia
      ?? null;

    if (!idAgencia) {
      this.error =
        'No se detectó la agencia activa del usuario.';

      return;
    }

    this.validando = true;

    this.api.validar(
      persona.idDatosPersonal,
      idAgencia
    )
      .pipe(
        finalize(() => {
          this.validando = false;
        })
      )
      .subscribe({
        next: respuesta => {
          if (
            !respuesta
            || respuesta.ok === false
          ) {
            this.error =
              respuesta?.mensaje
              ?? 'El asociado no cumple las reglas para abrir cuentas.';

            return;
          }

          this.router.navigate(
            [
              '/hoja-vida/apertura-cuentas/nuevo'
            ],
            {
              queryParams: {
                idDatosPersonal:
                  persona.idDatosPersonal,
                idAgencia
              }
            }
          );
        },
        error: err => {
          console.error(
            'Error validando apertura de cuentas:',
            err
          );

          this.error =
            'No fue posible validar la apertura de cuentas.';
        }
      });
  }

  private normalizarTexto(
    valor: string | number | null | undefined
  ): string {

    return String(valor ?? '')
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }
}
