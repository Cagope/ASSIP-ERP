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
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  forkJoin
} from 'rxjs';

import {
  HeaderActionsComponent
} from '../../../../../shared/header-actions/header-actions.component';

import {
  EvaluacionCarteraApi
} from '../evaluacion-cartera.api';

import {
  EvaluacionCarteraResultadosApi
} from './evaluacion-cartera-resultados.api';

import {
  EvaluacionResultadoPrintService,
  EvaluacionFormatoImpresion
} from './impresion/evaluacion-resultado-print.service';

import {
  AccionEvaluacionCartera,
  ACCION_EVALUACION_DESCRIPCION,
  EvaluacionCartera,
  EvaluacionCreditoResultado,
  EvaluacionCriterioResultado
} from '../evaluacion-cartera.models';

import {
  EvaluacionResultadosExcelService,
  EvaluacionExcelDetalleCredito
} from './excel/evaluacion-resultados-excel.service';

@Component({
  selector: 'app-evaluacion-cartera-resultados',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],

  templateUrl:
    './evaluacion-cartera-resultados.component.html',

  styleUrl:
    './evaluacion-cartera-resultados.component.scss'
})
export class EvaluacionCarteraResultadosComponent
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

  private readonly resultadosApi =
    inject(EvaluacionCarteraResultadosApi);

  private readonly printService =
    inject(EvaluacionResultadoPrintService);

  private readonly excelService =
    inject(EvaluacionResultadosExcelService);

  // =========================================================
  // IDENTIFICACIÓN
  // =========================================================

  idEvaluacionCartera:
    number | null = null;


  // =========================================================
  // EVALUACIÓN
  // =========================================================

  evaluacion:
    EvaluacionCartera | null = null;


  // =========================================================
  // RESULTADOS
  // =========================================================

  resultados:
    EvaluacionCreditoResultado[] = [];

  resultadosFiltrados:
    EvaluacionCreditoResultado[] = [];


  // =========================================================
  // ESTADO
  // =========================================================

  cargando = false;

  imprimiendo = false;

  error = '';


  // =========================================================
  // FILTROS
  // =========================================================

  filtroDocumento = '';

  filtroNombre = '';

  filtroPagare = '';

  filtroClasificacion = '';

  filtroAccion:
    AccionEvaluacionCartera | '' = '';


  // =========================================================
  // PAGINACIÓN
  // =========================================================

  paginaActual = 1;

  tamanioPagina = 20;


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    const id =
      Number(
        this.route.snapshot.paramMap.get(
          'idEvaluacionCartera'
        )
      );


    if (
      !Number.isInteger(id)
      || id <= 0
    ) {

      this.error =
        'El identificador de la evaluación no es válido.';

      return;
    }


    this.idEvaluacionCartera =
      id;

    this.cargar();
  }


  // =========================================================
  // CARGAR
  // =========================================================

  cargar(): void {

    if (
      this.idEvaluacionCartera === null
      || this.cargando
    ) {
      return;
    }


    this.cargando = true;

    this.error = '';


    this.evaluacionApi
      .buscarPorId(
        this.idEvaluacionCartera
      )
      .subscribe({

        next: (
          evaluacion:
            EvaluacionCartera
        ) => {

          this.evaluacion =
            evaluacion;

          this.cargarResultados();
        },


        error: (error: unknown) => {

          console.error(
            'Error consultando la evaluación:',
            error
          );

          this.evaluacion = null;

          this.resultados = [];

          this.resultadosFiltrados = [];

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar la evaluación de cartera.'
            );

          this.cargando = false;
        }

      });
  }


  // =========================================================
  // CARGAR RESULTADOS
  // =========================================================

  private cargarResultados(): void {

    if (
      this.idEvaluacionCartera === null
    ) {
      return;
    }


    this.resultadosApi
      .listarResultados(
        this.idEvaluacionCartera
      )
      .subscribe({

        next: (
          resultados:
            EvaluacionCreditoResultado[]
        ) => {

          this.resultados =
            Array.isArray(resultados)
              ? resultados
              : [];

          this.paginaActual = 1;

          this.aplicarFiltros();

          this.cargando = false;
        },


        error: (error: unknown) => {

          console.error(
            'Error consultando resultados de la evaluación:',
            error
          );

          this.resultados = [];

          this.resultadosFiltrados = [];

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar los resultados de la evaluación.'
            );

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

    this.filtroDocumento = '';

    this.filtroNombre = '';

    this.filtroPagare = '';

    this.filtroClasificacion = '';

    this.filtroAccion = '';

    this.paginaActual = 1;

    this.aplicarFiltros();
  }


  // =========================================================
  // APLICAR FILTROS
  // =========================================================

  private aplicarFiltros(): void {

    const documento =
      this.normalizarTexto(
        this.filtroDocumento
      );


    const nombre =
      this.normalizarTexto(
        this.filtroNombre
      );


    const pagare =
      this.normalizarTexto(
        this.filtroPagare
      );


    const clasificacion =
      this.normalizarTexto(
        this.filtroClasificacion
      );


    const accion =
      this.filtroAccion;


    this.resultadosFiltrados =
      this.resultados.filter(
        resultado => {

          const cumpleDocumento =
            !documento
            || this
              .normalizarTexto(
                resultado.documento
              )
              .includes(
                documento
              );


          const cumpleNombre =
            !nombre
            || this
              .normalizarTexto(
                resultado.nombreCompleto
              )
              .includes(
                nombre
              );


          const cumplePagare =
            !pagare
            || this
              .normalizarTexto(
                resultado.pagareCartera
              )
              .includes(
                pagare
              );


          const cumpleClasificacion =
            !clasificacion
            || this
              .normalizarTexto(
                resultado.codigoClasificacionCredito
              )
              === clasificacion;


          const cumpleAccion =
            !accion
            || resultado.accionEvaluacion
            === accion;


          return (
            cumpleDocumento
            && cumpleNombre
            && cumplePagare
            && cumpleClasificacion
            && cumpleAccion
          );
        }
      );


    this.ajustarPaginaActual();
  }


  // =========================================================
  // RESULTADOS DE LA PÁGINA
  // =========================================================

  get resultadosPagina():
    EvaluacionCreditoResultado[] {

    const inicio =
      (
        this.paginaActual - 1
      )
      * this.tamanioPagina;


    const fin =
      inicio
      + this.tamanioPagina;


    return this.resultadosFiltrados.slice(
      inicio,
      fin
    );
  }


  // =========================================================
  // TOTAL DE PÁGINAS
  // =========================================================

  get totalPaginas(): number {

    if (
      this.resultadosFiltrados.length === 0
    ) {
      return 1;
    }


    return Math.ceil(
      this.resultadosFiltrados.length
      / this.tamanioPagina
    );
  }


  // =========================================================
  // TOTAL DE RESULTADOS
  // =========================================================

  get totalResultados(): number {

    return this.resultadosFiltrados.length;
  }


  // =========================================================
  // TOTALES POR ACCIÓN
  // =========================================================

  get totalReclasificar(): number {

    return this.resultados.filter(
      resultado =>
        resultado.accionEvaluacion === 'R'
    ).length;
  }


  get totalHabilitar(): number {

    return this.resultados.filter(
      resultado =>
        resultado.accionEvaluacion === 'H'
    ).length;
  }


  get totalMantener(): number {

    return this.resultados.filter(
      resultado =>
        resultado.accionEvaluacion === 'M'
    ).length;
  }


  // =========================================================
  // IMPRIMIR POR ACCIÓN
  //
  // Imprime TODOS los créditos de la acción indicada.
  //
  // R = Reclasificar
  // H = Habilitar
  // M = Mantener
  //
  // IMPORTANTE:
  //
  // El detalle completo se obtiene mediante UNA SOLA
  // solicitud HTTP.
  //
  // Luego se agrupan los criterios por
  // idEvaluacionCarteraCredito.
  //
  // Cada crédito se imprime en una hoja independiente.
  // =========================================================

  imprimirPorAccion(
    accion:
      AccionEvaluacionCartera
  ): void {

    if (
      this.idEvaluacionCartera === null
      || !this.evaluacion
      || this.imprimiendo
    ) {
      return;
    }


    const seleccionados =
      this.resultados.filter(
        resultado =>
          resultado.accionEvaluacion
          === accion
      );


    if (
      seleccionados.length === 0
    ) {

      this.error =
        'No existen créditos con la recomendación '
        + this.descripcionAccion(accion)
        + ' para imprimir.';

      return;
    }


    this.error = '';

    this.imprimiendo = true;


    // =======================================================
    // UNA SOLA CONSULTA PARA TODOS LOS DETALLES
    // =======================================================

    this.resultadosApi
      .listarDetallePorAccion(
        this.idEvaluacionCartera,
        accion
      )
      .subscribe({

        next: (
          detalles:
            EvaluacionCriterioResultado[]
        ) => {

          // =================================================
          // AGRUPAR CRITERIOS POR CRÉDITO EVALUADO
          // =================================================

          const detallesPorCredito =
            new Map<
              number,
              EvaluacionCriterioResultado[]
            >();


          for (
            const detalle
            of detalles
          ) {

            const idCredito =
              detalle.idEvaluacionCarteraCredito;


            if (
              !idCredito
            ) {
              continue;
            }


            const criteriosCredito =
              detallesPorCredito.get(
                idCredito
              )
              ?? [];


            criteriosCredito.push(
              detalle
            );


            detallesPorCredito.set(
              idCredito,
              criteriosCredito
            );
          }


          // =================================================
          // CONSTRUIR FORMATOS
          // =================================================

          const formatos:
            EvaluacionFormatoImpresion[] =
            seleccionados.map(
              credito => ({

                credito,

                criterios:
                  detallesPorCredito.get(
                    credito.idEvaluacionCarteraCredito
                  )
                  ?? []

              })
            );


          // =================================================
          // IMPRIMIR
          // =================================================

          this.printService
            .imprimirMasivo(
              this.evaluacion!,
              formatos
            )
            .subscribe({

              next: () => {

                this.imprimiendo =
                  false;
              },


              error: (
                error:
                  unknown
              ) => {

                console.error(
                  'Error imprimiendo formatos de evaluación:',
                  error
                );


                this.error =
                  this.obtenerMensajeError(
                    error,
                    'No fue posible imprimir los formatos seleccionados.'
                  );


                this.imprimiendo =
                  false;
              }

            });
        },


        error: (
          error:
            unknown
        ) => {

          console.error(
            'Error consultando los criterios para impresión masiva:',
            error
          );


          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible preparar los formatos para impresión.'
            );


          this.imprimiendo =
            false;
        }

      });
  }


  // =========================================================
  // PÁGINA ANTERIOR
  // =========================================================

  paginaAnterior(): void {

    if (
      this.paginaActual <= 1
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
  // CAMBIAR PÁGINA
  // =========================================================

  cambiarPagina(
    pagina:
      number
  ): void {

    if (
      pagina < 1
      || pagina > this.totalPaginas
    ) {
      return;
    }


    this.paginaActual =
      pagina;
  }


  // =========================================================
  // AJUSTAR PÁGINA ACTUAL
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
      this.paginaActual < 1
    ) {

      this.paginaActual = 1;
    }
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
  // VER DETALLE
  // =========================================================

  verDetalle(
    resultado:
      EvaluacionCreditoResultado
  ): void {

    if (
      this.idEvaluacionCartera === null
      || !resultado
      || !resultado.idEvaluacionCarteraCredito
    ) {
      return;
    }


    this.router.navigate([
      '/cartera/evaluacion/evaluaciones',
      this.idEvaluacionCartera,
      'resultados',
      resultado.idEvaluacionCarteraCredito
    ]);
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
      'gestionar'
    ]);
  }


  // =========================================================
  // EXPORTAR EVALUACIÓN COMPLETA A EXCEL
  //
  // Exporta TODOS los créditos.
  //
  // No depende de:
  // - filtros visibles;
  // - paginación;
  // - página actual.
  //
  // Obtiene los criterios mediante solamente 3 llamadas:
  // R / H / M.
  // =========================================================

  exportarExcel(): void {

    if (
      this.idEvaluacionCartera === null
      || !this.evaluacion
      || this.resultados.length === 0
      || this.imprimiendo
    ) {
      return;
    }


    this.error = '';

    this.imprimiendo = true;


    forkJoin({

      recalificar:
        this.resultadosApi.listarDetallePorAccion(
          this.idEvaluacionCartera,
          'R'
        ),

      habilitar:
        this.resultadosApi.listarDetallePorAccion(
          this.idEvaluacionCartera,
          'H'
        ),

      mantener:
        this.resultadosApi.listarDetallePorAccion(
          this.idEvaluacionCartera,
          'M'
        ),

      fotoHojaVida:
        this.resultadosApi.listarFotoHojaVida(
          this.idEvaluacionCartera
        ),

      morosidadExtracto:
        this.resultadosApi.listarMorosidadExtracto(
          this.idEvaluacionCartera
        )

    }).subscribe({

      next: ({
        recalificar,
        habilitar,
        mantener,
        fotoHojaVida,
        morosidadExtracto
      }) => {

        // =====================================================
        // CONSOLIDAR TODOS LOS CRITERIOS
        // =====================================================

        const todosDetalles = [

          ...(
            Array.isArray(recalificar)
              ? recalificar
              : []
          ),

          ...(
            Array.isArray(habilitar)
              ? habilitar
              : []
          ),

          ...(
            Array.isArray(mantener)
              ? mantener
              : []
          )

        ];


        // =====================================================
        // AGRUPAR CRITERIOS POR CRÉDITO
        // =====================================================

        const detallesPorCredito =
          new Map<
            number,
            EvaluacionCriterioResultado[]
          >();


        for (
          const detalle
          of todosDetalles
        ) {

          const idCredito =
            detalle.idEvaluacionCarteraCredito;


          if (!idCredito) {
            continue;
          }


          const criterios =
            detallesPorCredito.get(
              idCredito
            )
            ?? [];


          criterios.push(
            detalle
          );


          detallesPorCredito.set(
            idCredito,
            criterios
          );
        }


        // =====================================================
        // ARMAR ESTRUCTURA COMPLETA PARA EXCEL
        //
        // 1 crédito = 1 elemento.
        //
        // Cada elemento contiene:
        // - resultado consolidado;
        // - criterios aplicados.
        // =====================================================

        const detalleExcel:
          EvaluacionExcelDetalleCredito[] =
          this.resultados.map(
            credito => ({

              credito,

              criterios:
                detallesPorCredito.get(
                  credito.idEvaluacionCarteraCredito
                )
                ?? []

            })
          );


        // =====================================================
        // NORMALIZAR INSUMOS
        // =====================================================

        const fotoHojaVidaExcel =
          Array.isArray(
            fotoHojaVida
          )
            ? fotoHojaVida
            : [];


        const morosidadExtractoExcel =
          Array.isArray(
            morosidadExtracto
          )
            ? morosidadExtracto
            : [];


        // =====================================================
        // GENERAR ARCHIVO EXCEL
        // =====================================================

        try {

          this.excelService.exportar(
            this.evaluacion!,
            this.resultados,
            detalleExcel,
            fotoHojaVidaExcel,
            morosidadExtractoExcel
          );


          this.imprimiendo =
            false;

        } catch (error) {

          console.error(
            'Error generando el archivo Excel de evaluación:',
            error
          );


          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible generar el archivo Excel.'
            );


          this.imprimiendo =
            false;
        }
      },


      error: (
        error:
          unknown
      ) => {

        console.error(
          'Error consultando información para exportación:',
          error
        );


        this.error =
          this.obtenerMensajeError(
            error,
            'No fue posible consultar la información completa para exportar.'
          );


        this.imprimiendo =
          false;
      }

    });
  }


  // =========================================================
  // TRACK BY
  // =========================================================

  trackByResultado(
    index:
      number,
    resultado:
      EvaluacionCreditoResultado
  ): number {

    return (
      resultado.idEvaluacionCarteraCredito
      ?? index
    );
  }


  // =========================================================
  // NORMALIZACIÓN
  // =========================================================

  private normalizarTexto(
    valor:
      string
      | null
      | undefined
  ): string {

    return (
      valor
      ?? ''
    )
      .trim()
      .toUpperCase();
  }


  // =========================================================
  // MENSAJE DE ERROR
  // =========================================================

  private obtenerMensajeError(
    error:
      unknown,
    mensajePredeterminado:
      string
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
