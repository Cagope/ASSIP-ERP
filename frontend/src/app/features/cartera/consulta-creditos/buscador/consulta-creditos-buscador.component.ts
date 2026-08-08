import {
  CommonModule
} from '@angular/common';

import {
  Component,
  EventEmitter,
  Input,
  Output,
  inject
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  ConsultaCreditosApi
} from '../consulta-creditos.api';

import {
  CarteraAsociadoBusqueda
} from '../consulta-creditos.models';


// =========================================================
// FILTROS
// =========================================================

export interface ConsultaCreditosFiltros {
  documento: string;
  nombres: string;
  primerApellido: string;
  segundoApellido: string;
}


// =========================================================
// TIPO EXPUESTO AL COMPONENTE PRINCIPAL
// =========================================================

export type ConsultaCreditosAsociado =
  CarteraAsociadoBusqueda;


// =========================================================
// COMPONENTE
// =========================================================

@Component({
  selector: 'app-consulta-creditos-buscador',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl:
    './consulta-creditos-buscador.component.html',
  styleUrls: [
    './consulta-creditos-buscador.component.scss'
  ]
})
export class ConsultaCreditosBuscadorComponent {

  // =========================================================
  // SERVICIOS
  // =========================================================

  private readonly api =
    inject(ConsultaCreditosApi);


  // =========================================================
  // ENTRADAS
  // =========================================================

  @Input()
  bloqueado = false;


  // =========================================================
  // SALIDAS
  // =========================================================

  @Output()
  asociadoSeleccionado =
    new EventEmitter<ConsultaCreditosAsociado>();

  @Output()
  busquedaLimpiada =
    new EventEmitter<void>();


  // =========================================================
  // FILTROS
  // =========================================================

  filtros:
    ConsultaCreditosFiltros = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };


  // =========================================================
  // RESULTADOS
  // =========================================================

  resultados:
    ConsultaCreditosAsociado[] = [];

  seleccionado:
    ConsultaCreditosAsociado | null = null;


  // =========================================================
  // ESTADO
  // =========================================================

  cargando = false;

  busquedaRealizada = false;

  error = '';


  // =========================================================
  // BÚSQUEDA
  // =========================================================

  buscar(): void {

    if (
      this.cargando
      || this.bloqueado
    ) {
      return;
    }

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

    if (
      !documento
      && !nombres
      && !primerApellido
      && !segundoApellido
    ) {

      this.resultados = [];

      this.seleccionado = null;

      this.busquedaRealizada = false;

      this.error =
        'Ingrese al menos un criterio de búsqueda.';

      return;
    }

    this.filtros = {
      documento,
      nombres,
      primerApellido,
      segundoApellido
    };

    this.cargando = true;

    this.busquedaRealizada = true;

    this.error = '';

    this.resultados = [];

    this.seleccionado = null;

    this.api
      .buscarAsociados(
        documento,
        nombres,
        primerApellido,
        segundoApellido
      )
      .subscribe({

        next: (
          respuesta:
            CarteraAsociadoBusqueda[]
        ) => {

          this.resultados =
            Array.isArray(respuesta)
              ? respuesta
              : [];

          this.cargando = false;

          if (
            this.resultados.length === 0
          ) {
            this.error =
              'No se encontraron asociados con créditos para los criterios ingresados.';
          }

        },

        error: (
          error: unknown
        ) => {

          console.error(
            'Error buscando asociados de cartera:',
            error
          );

          this.resultados = [];

          this.seleccionado = null;

          this.cargando = false;

          this.error =
            this.obtenerMensajeError(
              error
            );

        }

      });

  }


  // =========================================================
  // SELECCIÓN
  // =========================================================

  seleccionar(
    asociado:
      ConsultaCreditosAsociado
  ): void {

    if (
      this.cargando
      || this.bloqueado
    ) {
      return;
    }

    const idDatosPersonal =
      Number(
        asociado.idDatosPersonal
      );

    if (
      !Number.isInteger(
        idDatosPersonal
      )
      || idDatosPersonal <= 0
    ) {

      this.error =
        'El asociado seleccionado no tiene un identificador válido.';

      return;
    }

    this.error = '';

    this.seleccionado =
      asociado;

    this.asociadoSeleccionado.emit(
      asociado
    );

  }


  // =========================================================
  // LIMPIAR
  // =========================================================

  limpiar(): void {

    if (
      this.cargando
      || this.bloqueado
    ) {
      return;
    }

    this.filtros = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };

    this.resultados = [];

    this.seleccionado = null;

    this.busquedaRealizada = false;

    this.error = '';

    this.busquedaLimpiada.emit();

  }


  // =========================================================
  // INDICADORES
  // =========================================================

  get tieneCriterios(): boolean {

    return Boolean(
      this.normalizarTexto(
        this.filtros.documento
      )
      || this.normalizarTexto(
        this.filtros.nombres
      )
      || this.normalizarTexto(
        this.filtros.primerApellido
      )
      || this.normalizarTexto(
        this.filtros.segundoApellido
      )
    );

  }

  get tieneResultados(): boolean {

    return this.resultados.length > 0;

  }

  esSeleccionado(
    asociado:
      ConsultaCreditosAsociado
  ): boolean {

    return (
      this.seleccionado
        ?.idDatosPersonal
      === asociado.idDatosPersonal
    );

  }


  // =========================================================
  // TRACK BY
  // =========================================================

  trackByAsociado(
    index: number,
    asociado:
      ConsultaCreditosAsociado
  ): number {

    return (
      asociado.idDatosPersonal
      ?? index
    );

  }


  // =========================================================
  // UTILIDADES
  // =========================================================

  private normalizarTexto(
    valor:
      | string
      | null
      | undefined
  ): string {

    return String(
      valor ?? ''
    )
      .trim()
      .replace(
        /\s+/g,
        ' '
      );

  }

  private obtenerMensajeError(
    error: unknown
  ): string {

    if (
      !error
      || typeof error !== 'object'
    ) {
      return 'No fue posible consultar los asociados.';
    }

    const respuesta =
      error as {
        message?: string;
        error?: {
          mensaje?: string;
          message?: string;
        };
      };

    return (
      respuesta.error?.mensaje
      ?? respuesta.error?.message
      ?? respuesta.message
      ?? 'No fue posible consultar los asociados.'
    );

  }

}
