import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  forkJoin
} from 'rxjs';

import {
  HeaderActionsComponent
} from '../../../../../../shared/header-actions/header-actions.component';

import {
  EvaluacionCarteraApi
} from '../../evaluacion-cartera.api';

import {
  EvaluacionCarteraResultadosApi
} from '../evaluacion-cartera-resultados.api';

import {
  EvaluacionResultadoPrintService
} from '../impresion/evaluacion-resultado-print.service';

import {
  AccionEvaluacionCartera,
  ACCION_EVALUACION_DESCRIPCION,
  EvaluacionCartera,
  EvaluacionCreditoResultado,
  EvaluacionCriterioResultado
} from '../../evaluacion-cartera.models';


@Component({
  selector: 'app-evaluacion-resultado-detalle',

  standalone: true,

  imports: [
    CommonModule,
    HeaderActionsComponent
  ],

  templateUrl:
    './evaluacion-resultado-detalle.component.html',

  styleUrl:
    './evaluacion-resultado-detalle.component.scss'
})
export class EvaluacionResultadoDetalleComponent
implements OnInit {

  // =========================================================
  // SERVICIOS
  // =========================================================

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly evaluacionApi =
    inject(EvaluacionCarteraApi);

  private readonly api =
    inject(EvaluacionCarteraResultadosApi);

  private readonly printService =
    inject(EvaluacionResultadoPrintService);


  // =========================================================
  // IDENTIFICACIÓN
  // =========================================================

  idEvaluacionCartera:
    number | null = null;

  idEvaluacionCarteraCredito:
    number | null = null;


  // =========================================================
  // EVALUACIÓN
  // =========================================================

  evaluacion:
    EvaluacionCartera | null = null;


  // =========================================================
  // RESULTADO
  // =========================================================

  resultado:
    EvaluacionCreditoResultado | null = null;

  criterios:
    EvaluacionCriterioResultado[] = [];


  // =========================================================
  // ESTADO
  // =========================================================

  cargando = false;

  imprimiendo = false;

  error = '';


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    const idEvaluacion =
      Number(
        this.route.snapshot.paramMap.get(
          'idEvaluacionCartera'
        )
      );

    const idResultado =
      Number(
        this.route.snapshot.paramMap.get(
          'idEvaluacionCarteraCredito'
        )
      );


    if (
      !Number.isInteger(idEvaluacion)
      || idEvaluacion <= 0
    ) {

      this.error =
        'El identificador de la evaluación no es válido.';

      return;
    }


    if (
      !Number.isInteger(idResultado)
      || idResultado <= 0
    ) {

      this.error =
        'El identificador del crédito evaluado no es válido.';

      return;
    }


    this.idEvaluacionCartera =
      idEvaluacion;

    this.idEvaluacionCarteraCredito =
      idResultado;

    this.cargar();
  }


  // =========================================================
  // CARGAR
  // =========================================================

  cargar(): void {

    if (
      this.idEvaluacionCartera === null
      || this.idEvaluacionCarteraCredito === null
      || this.cargando
    ) {
      return;
    }


    this.cargando = true;

    this.error = '';


    forkJoin({

      evaluacion:
        this.evaluacionApi.buscarPorId(
          this.idEvaluacionCartera
        ),

      resultado:
        this.api.buscarResultadoPorId(
          this.idEvaluacionCartera,
          this.idEvaluacionCarteraCredito
        ),

      criterios:
        this.api.listarDetalle(
          this.idEvaluacionCartera,
          this.idEvaluacionCarteraCredito
        )

    }).subscribe({

      next: ({
        evaluacion,
        resultado,
        criterios
      }) => {

        this.evaluacion =
          evaluacion;

        this.resultado =
          resultado;

        this.criterios =
          Array.isArray(criterios)
            ? criterios
            : [];

        this.cargando = false;
      },


      error: (error: unknown) => {

        console.error(
          'Error consultando el detalle de la evaluación:',
          error
        );

        this.evaluacion = null;

        this.resultado = null;

        this.criterios = [];

        this.error =
          this.obtenerMensajeError(
            error,
            'No fue posible consultar el detalle del crédito evaluado.'
          );

        this.cargando = false;
      }

    });
  }


  // =========================================================
  // DESCRIPCIÓN DE ACCIÓN
  // =========================================================

  descripcionAccion(
    accion:
      AccionEvaluacionCartera
  ): string {

    return (
      ACCION_EVALUACION_DESCRIPCION[
        accion
      ]
      ?? accion
    );
  }


  // =========================================================
  // TOTAL PUNTAJE DE CRITERIOS
  // =========================================================

  get puntajeDetalle(): number {

    return this.criterios.reduce(
      (
        total,
        criterio
      ) =>
        total
        + Number(
          criterio.puntajeObtenido
          ?? 0
        ),
      0
    );
  }


  // =========================================================
  // TOTAL PUNTAJE MÁXIMO
  // =========================================================

  get puntajeMaximo(): number {

    return this.criterios.reduce(
      (
        total,
        criterio
      ) =>
        total
        + Number(
          criterio.puntajeMaximo
          ?? 0
        ),
      0
    );
  }


  // =========================================================
  // VOLVER
  // =========================================================

  volver(): void {

    if (
      this.idEvaluacionCartera === null
    ) {

      this.router.navigate([
        '/cartera/evaluacion/evaluaciones'
      ]);

      return;
    }


    this.router.navigate([
      '/cartera/evaluacion/evaluaciones',
      this.idEvaluacionCartera,
      'resultados'
    ]);
  }


  // =========================================================
  // IMPRIMIR FORMATO INDIVIDUAL
  //
  // No navega a otra pantalla.
  //
  // Utiliza:
  // - cabecera de evaluación ya cargada;
  // - resultado del crédito ya cargado;
  // - criterios ya cargados.
  //
  // El PrintService genera un iframe invisible y abre
  // directamente el diálogo de impresión del navegador.
  // =========================================================

  imprimirFormato(): void {

    if (
      !this.evaluacion
      || !this.resultado
      || this.imprimiendo
    ) {
      return;
    }


    this.error = '';

    this.imprimiendo = true;


    this.printService
      .imprimirIndividual(
        this.evaluacion,
        this.resultado,
        this.criterios
      )
      .subscribe({

        next: () => {

          this.imprimiendo = false;
        },


        error: (error: unknown) => {

          console.error(
            'Error preparando la impresión individual:',
            error
          );

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible preparar el formato para impresión.'
            );

          this.imprimiendo = false;
        }

      });
  }


  // =========================================================
  // TRACK BY
  // =========================================================

  trackByCriterio(
    index: number,
    criterio:
      EvaluacionCriterioResultado
  ): number {

    return (
      criterio.idEvaluacionCarteraCreditoDetalle
      ?? index
    );
  }


  // =========================================================
  // MENSAJE DE ERROR
  // =========================================================

  private obtenerMensajeError(
    error: unknown,
    mensajePredeterminado: string
  ): string {

    if (
      !error
      || typeof error !== 'object'
    ) {
      return mensajePredeterminado;
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
      ?? mensajePredeterminado
    );
  }
}
