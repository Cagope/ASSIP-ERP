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
  Router
} from '@angular/router';

import {
  HeaderActionsComponent
} from '../../../../../shared/header-actions/header-actions.component';

import {
  EvaluacionCarteraApi
} from '../evaluacion-cartera.api';

import {
  EvaluacionCartera,
  EvaluacionCarteraGuardar,
  EstadoEvaluacionCartera,
  ESTADO_EVALUACION_DESCRIPCION
} from '../evaluacion-cartera.models';


@Component({
  selector: 'app-evaluacion-cartera-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './evaluacion-cartera-list.component.html',
  styleUrl: './evaluacion-cartera-list.component.scss'
})
export class EvaluacionCarteraListComponent implements OnInit {

  private readonly api =
    inject(EvaluacionCarteraApi);

  private readonly router =
    inject(Router);

  // =========================================================
  // DATOS
  // =========================================================

  evaluaciones:
    EvaluacionCartera[] = [];

  evaluacionesFiltradas:
    EvaluacionCartera[] = [];

  cargando = false;

  guardando = false;

  error = '';

  mensaje = '';

  // =========================================================
  // NUEVA EVALUACIÓN
  // =========================================================

  mostrarNuevaEvaluacion = false;

  nuevaEvaluacion:
    EvaluacionCarteraGuardar =
      this.crearModeloNuevaEvaluacion();

  // =========================================================
  // FILTROS
  // =========================================================

  filtroFechaCorte = '';

  filtroEstado = '';

  // =========================================================
  // PAGINACIÓN
  // =========================================================

  paginaActual = 1;

  tamanioPagina = 20;

  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    this.cargar();
  }

  // =========================================================
  // CARGAR
  // =========================================================

  cargar(): void {

    this.cargando = true;

    this.error = '';

    this.mensaje = '';

    this.api
      .listar()
      .subscribe({

        next: (
          data:
            EvaluacionCartera[]
        ) => {

          this.evaluaciones =
            data ?? [];

          this.aplicarFiltros();

          this.cargando = false;
        },

        error: (err) => {

          console.error(
            'Error cargando evaluaciones de cartera',
            err
          );

          this.evaluaciones = [];

          this.evaluacionesFiltradas = [];

          this.error =
            err?.error?.message
            || 'No fue posible cargar las evaluaciones de cartera.';

          this.cargando = false;
        }
      });
  }

  // =========================================================
  // NUEVO
  // =========================================================

  nuevo(): void {

    if (
      this.cargando
      || this.guardando
    ) {
      return;
    }

    this.error = '';

    this.mensaje = '';

    this.nuevaEvaluacion =
      this.crearModeloNuevaEvaluacion();

    this.mostrarNuevaEvaluacion =
      true;
  }

  // =========================================================
  // CANCELAR NUEVO
  // =========================================================

  cancelarNuevo(): void {

    if (
      this.guardando
    ) {
      return;
    }

    this.error = '';

    this.mensaje = '';

    this.mostrarNuevaEvaluacion =
      false;

    this.nuevaEvaluacion =
      this.crearModeloNuevaEvaluacion();
  }

  // =========================================================
  // CREAR EVALUACIÓN
  // =========================================================

  crearEvaluacion(): void {

    if (
      this.guardando
    ) {
      return;
    }

    this.error = '';

    this.mensaje = '';

    const validacion =
      this.validarNuevaEvaluacion();

    if (
      validacion
    ) {

      this.error =
        validacion;

      return;
    }

    const dto:
      EvaluacionCarteraGuardar = {

        fechaCorte:
          this.nuevaEvaluacion
            .fechaCorte,

        /*
         * La evaluación utiliza las reglas activas
         * al momento de ejecutar el proceso.
         *
         * Este valor se conserva únicamente porque
         * actualmente la tabla y el DTO lo requieren.
         */
        versionMetodologia:
          'VIGENTE',

        fechaComiteRiesgos:
          null,

        numeroActaRiesgos:
          null,

        fechaConsejo:
          null,

        numeroActaConsejo:
          null,

        observaciones:
          null
      };

    this.guardando = true;

    this.api
      .crear(
        dto
      )
      .subscribe({

        next: (
          evaluacion:
            EvaluacionCartera
        ) => {

          this.guardando = false;

          this.mostrarNuevaEvaluacion =
            false;

          this.nuevaEvaluacion =
            this.crearModeloNuevaEvaluacion();

          this.router.navigate([
            '/cartera/evaluacion/evaluaciones',
            evaluacion.idEvaluacionCartera,
            'gestionar'
          ]);
        },

        error: (err) => {

          console.error(
            'Error creando evaluación de cartera',
            err
          );

          this.error =
            err?.error?.message
            || 'No fue posible iniciar la evaluación de cartera.';

          this.guardando = false;
        }
      });
  }

  // =========================================================
  // BUSCAR
  // =========================================================

  buscar(): void {

    this.paginaActual = 1;

    this.aplicarFiltros();
  }

  // =========================================================
  // LIMPIAR
  // =========================================================

  limpiar(): void {

    this.filtroFechaCorte = '';

    this.filtroEstado = '';

    this.paginaActual = 1;

    this.aplicarFiltros();
  }

  // =========================================================
  // APLICAR FILTROS
  // =========================================================

  private aplicarFiltros(): void {

    const fecha =
      this.filtroFechaCorte
        .trim();

    const estado =
      this.filtroEstado
        .trim();

    this.evaluacionesFiltradas =
      this.evaluaciones
        .filter(
          evaluacion => {

            const cumpleFecha =
              !fecha
              || evaluacion.fechaCorte
              === fecha;

            const cumpleEstado =
              !estado
              || evaluacion.estado
              === estado;

            return (
              cumpleFecha
              && cumpleEstado
            );
          }
        );

    this.ajustarPaginaActual();
  }

  // =========================================================
  // REGISTROS DE LA PÁGINA
  // =========================================================

  get evaluacionesPagina():
    EvaluacionCartera[] {

    const inicio =
      (
        this.paginaActual
        - 1
      )
      * this.tamanioPagina;

    const fin =
      inicio
      + this.tamanioPagina;

    return (
      this.evaluacionesFiltradas
        .slice(
          inicio,
          fin
        )
    );
  }

  // =========================================================
  // TOTAL PÁGINAS
  // =========================================================

  get totalPaginas(): number {

    if (
      this.evaluacionesFiltradas
        .length === 0
    ) {
      return 1;
    }

    return Math.ceil(
      this.evaluacionesFiltradas
        .length
      / this.tamanioPagina
    );
  }

  // =========================================================
  // TOTAL REGISTROS
  // =========================================================

  get totalRegistros(): number {

    return (
      this.evaluacionesFiltradas
        .length
    );
  }

  // =========================================================
  // PÁGINA ANTERIOR
  // =========================================================

  paginaAnterior(): void {

    if (
      this.paginaActual
      <= 1
    ) {
      return;
    }

    this.paginaActual--;
  }

  // =========================================================
  // PÁGINA SIGUIENTE
  // =========================================================

  paginaSiguiente(): void {

    if (
      this.paginaActual
      >= this.totalPaginas
    ) {
      return;
    }

    this.paginaActual++;
  }

  // =========================================================
  // AJUSTAR PÁGINA
  // =========================================================

  private ajustarPaginaActual(): void {

    if (
      this.paginaActual
      > this.totalPaginas
    ) {

      this.paginaActual =
        this.totalPaginas;
    }

    if (
      this.paginaActual
      < 1
    ) {

      this.paginaActual =
        1;
    }
  }

  // =========================================================
  // DESCRIPCIÓN ESTADO
  // =========================================================

  descripcionEstado(
    estado:
      EstadoEvaluacionCartera
  ): string {

    return (
      ESTADO_EVALUACION_DESCRIPCION[
        estado
      ]
      ?? estado
    );
  }

  // =========================================================
  // GESTIONAR
  // =========================================================

  gestionar(
    evaluacion:
      EvaluacionCartera
  ): void {

    this.router.navigate([
      '/cartera/evaluacion/evaluaciones',
      evaluacion.idEvaluacionCartera,
      'gestionar'
    ]);
  }

  // =========================================================
  // VALIDAR NUEVA EVALUACIÓN
  // =========================================================

  private validarNuevaEvaluacion():
    string | null {

    if (
      !this.nuevaEvaluacion
        .fechaCorte
    ) {

      return (
        'Debe seleccionar la fecha de corte.'
      );
    }

    const fecha =
      new Date(
        `${this.nuevaEvaluacion.fechaCorte}T00:00:00`
      );

    if (
      Number.isNaN(
        fecha.getTime()
      )
    ) {

      return (
        'La fecha de corte no es válida.'
      );
    }

    const ultimoDiaMes =
      new Date(
        fecha.getFullYear(),
        fecha.getMonth() + 1,
        0
      );

    if (
      fecha.getDate()
      !== ultimoDiaMes.getDate()
    ) {

      return (
        'La fecha de corte debe corresponder al último día del mes.'
      );
    }

    const yaExiste =
      this.evaluaciones
        .some(
          evaluacion =>
            evaluacion.fechaCorte
            === this.nuevaEvaluacion
              .fechaCorte
        );

    if (
      yaExiste
    ) {

      return (
        'Ya existe una evaluación de cartera para la fecha de corte seleccionada.'
      );
    }

    return null;
  }

  // =========================================================
  // MODELO NUEVO
  // =========================================================

  private crearModeloNuevaEvaluacion():
    EvaluacionCarteraGuardar {

    return {

      fechaCorte:
        '',

      /*
       * Valor interno.
       *
       * No se presenta al usuario.
       * Las reglas utilizadas serán las reglas
       * activas cuando se ejecute la evaluación.
       */
      versionMetodologia:
        'VIGENTE',

      fechaComiteRiesgos:
        null,

      numeroActaRiesgos:
        null,

      fechaConsejo:
        null,

      numeroActaConsejo:
        null,

      observaciones:
        null
    };
  }
}
