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

  evaluaciones: EvaluacionCartera[] = [];

  evaluacionesFiltradas: EvaluacionCartera[] = [];

  cargando = false;

  error = '';

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

    this.api.listar().subscribe({

      next: (data) => {

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
          'No fue posible cargar las evaluaciones de cartera.';

        this.cargando = false;
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
      this.filtroFechaCorte.trim();

    const estado =
      this.filtroEstado.trim();

    this.evaluacionesFiltradas =
      this.evaluaciones.filter(
        evaluacion => {

          const cumpleFecha =
            !fecha
            || evaluacion.fechaCorte === fecha;

          const cumpleEstado =
            !estado
            || evaluacion.estado === estado;

          return cumpleFecha
            && cumpleEstado;
        }
      );

    this.ajustarPaginaActual();
  }

  // =========================================================
  // REGISTROS DE LA PÁGINA
  // =========================================================

  get evaluacionesPagina(): EvaluacionCartera[] {

    const inicio =
      (this.paginaActual - 1)
      * this.tamanioPagina;

    const fin =
      inicio + this.tamanioPagina;

    return this.evaluacionesFiltradas.slice(
      inicio,
      fin
    );
  }

  // =========================================================
  // TOTAL PÁGINAS
  // =========================================================

  get totalPaginas(): number {

    if (
      this.evaluacionesFiltradas.length === 0
    ) {
      return 1;
    }

    return Math.ceil(
      this.evaluacionesFiltradas.length
      / this.tamanioPagina
    );
  }

  // =========================================================
  // TOTAL REGISTROS
  // =========================================================

  get totalRegistros(): number {
    return this.evaluacionesFiltradas.length;
  }

  // =========================================================
  // PÁGINA ANTERIOR
  // =========================================================

  paginaAnterior(): void {

    if (this.paginaActual <= 1) {
      return;
    }

    this.paginaActual--;
  }

  // =========================================================
  // PÁGINA SIGUIENTE
  // =========================================================

  paginaSiguiente(): void {

    if (
      this.paginaActual >= this.totalPaginas
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
      this.paginaActual > this.totalPaginas
    ) {
      this.paginaActual =
        this.totalPaginas;
    }

    if (this.paginaActual < 1) {
      this.paginaActual = 1;
    }
  }

  // =========================================================
  // DESCRIPCIÓN ESTADO
  // =========================================================

  descripcionEstado(
    estado: EstadoEvaluacionCartera
  ): string {

    return ESTADO_EVALUACION_DESCRIPCION[
      estado
    ] ?? estado;
  }

  // =========================================================
  // GESTIONAR
  // =========================================================

  gestionar(
    evaluacion: EvaluacionCartera
  ): void {

    this.router.navigate([
      '/cartera/evaluacion/evaluaciones',
      evaluacion.idEvaluacionCartera,
      'gestionar'
    ]);
  }
}
