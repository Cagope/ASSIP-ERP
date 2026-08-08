import {
  Injectable
} from '@angular/core';

import * as XLSX from 'xlsx';

import {
  EvaluacionCartera,
  EvaluacionCreditoResultado,
  EvaluacionCriterioResultado,
  EvaluacionResultadoHojaVida,
  EvaluacionResultadoMorosidad,
  ACCION_EVALUACION_DESCRIPCION
} from '../../evaluacion-cartera.models';


// =========================================================
// DETALLE COMPLETO PARA EXCEL
// =========================================================

export interface EvaluacionExcelDetalleCredito {

  credito:
    EvaluacionCreditoResultado;

  criterios:
    EvaluacionCriterioResultado[];
}


// =========================================================
// ESTRUCTURA INTERNA DE AGRUPACIÓN
// =========================================================

interface ResumenCantidadSaldo {

  cantidad: number;

  saldoActual: number;
}


@Injectable({
  providedIn: 'root'
})
export class EvaluacionResultadosExcelService {

  // =========================================================
  // EXPORTAR LIBRO COMPLETO
  //
  // El libro contiene:
  //
  // 1. RESULTADOS_COMPLETOS
  // 2. CRITERIOS_DETALLE
  // 3. CLASIFICACION_ACTUAL
  // 4. CLASIFICACION_X_CREDITO
  // 5. VARIABLES_TRABAJADAS
  // 6. ACCIONES_SUGERIDAS
  // 7. ACCIONES_X_CLASIFICACION
  // 8. RECALIFICADOS
  // 9. FOTO_HOJA_VIDA
  // 10. MOROSIDAD_EXTRACTO
  // =========================================================

  exportar(
    evaluacion:
      EvaluacionCartera,
    resultados:
      EvaluacionCreditoResultado[],
    detalles:
      EvaluacionExcelDetalleCredito[],
    fotoHojaVida:
      EvaluacionResultadoHojaVida[],
    morosidadExtracto:
      EvaluacionResultadoMorosidad[]
  ): void {

    if (!evaluacion) {

      throw new Error(
        'No existe información de la evaluación para exportar.'
      );
    }


    const creditos =
      Array.isArray(resultados)
        ? resultados
        : [];


    if (
      creditos.length === 0
    ) {

      throw new Error(
        'No existen resultados para exportar.'
      );
    }


    const detalleCompleto =
      Array.isArray(detalles)
        ? detalles
        : [];


    const hojaVida =
      Array.isArray(fotoHojaVida)
        ? fotoHojaVida
        : [];


    const morosidad =
      Array.isArray(morosidadExtracto)
        ? morosidadExtracto
        : [];


    const libro =
      XLSX.utils.book_new();


    // =======================================================
    // 1. RESULTADOS COMPLETOS
    // =======================================================

    const hojaResultados =
      this.crearHojaResultados(
        evaluacion,
        creditos
      );


    XLSX.utils.book_append_sheet(
      libro,
      hojaResultados,
      'RESULTADOS_COMPLETOS'
    );


    // =======================================================
    // 2. CRITERIOS DETALLE
    //
    // 1 crédito = 1 fila.
    // =======================================================

    const hojaCriterios =
      this.crearHojaCriterios(
        evaluacion,
        detalleCompleto
      );


    XLSX.utils.book_append_sheet(
      libro,
      hojaCriterios,
      'CRITERIOS_DETALLE'
    );


    // =======================================================
    // 3. CLASIFICACIÓN ACTUAL
    // =======================================================

    const hojaClasificacionActual =
      this.crearHojaClasificacionActual(
        creditos
      );


    XLSX.utils.book_append_sheet(
      libro,
      hojaClasificacionActual,
      'CLASIFICACION_ACTUAL'
    );


    // =======================================================
    // 4. CLASIFICACIÓN X CRÉDITO
    // =======================================================

    const hojaClasificacionCredito =
      this.crearHojaClasificacionCredito(
        creditos
      );


    XLSX.utils.book_append_sheet(
      libro,
      hojaClasificacionCredito,
      'CLASIFICACION_X_CREDITO'
    );


    // =======================================================
    // 5. VARIABLES TRABAJADAS
    // =======================================================

    const hojaVariables =
      this.crearHojaVariablesTrabajadas(
        detalleCompleto
      );


    XLSX.utils.book_append_sheet(
      libro,
      hojaVariables,
      'VARIABLES_TRABAJADAS'
    );


    // =======================================================
    // 6. ACCIONES SUGERIDAS
    // =======================================================

    const hojaAcciones =
      this.crearHojaAccionesSugeridas(
        creditos
      );


    XLSX.utils.book_append_sheet(
      libro,
      hojaAcciones,
      'ACCIONES_SUGERIDAS'
    );


    // =======================================================
    // 7. ACCIONES X CLASIFICACIÓN
    // =======================================================

    const hojaAccionesClasificacion =
      this.crearHojaAccionesClasificacion(
        creditos
      );


    XLSX.utils.book_append_sheet(
      libro,
      hojaAccionesClasificacion,
      'ACCIONES_X_CLASIFICACION'
    );


    // =======================================================
    // 8. RECALIFICADOS
    // =======================================================

    const hojaRecalificados =
      this.crearHojaRecalificados(
        creditos
      );


    XLSX.utils.book_append_sheet(
      libro,
      hojaRecalificados,
      'RECALIFICADOS'
    );


    // =======================================================
    // 9. FOTO HOJA DE VIDA
    //
    // 1 asociado = 1 fila.
    // =======================================================

    const hojaFotoHojaVida =
      this.crearHojaFotoHojaVida(
        hojaVida
      );


    XLSX.utils.book_append_sheet(
      libro,
      hojaFotoHojaVida,
      'FOTO_HOJA_VIDA'
    );


    // =======================================================
    // 10. MOROSIDAD EXTRACTO
    //
    // 1 comprobante consolidado = 1 fila.
    // =======================================================

    const hojaMorosidad =
      this.crearHojaMorosidadExtracto(
        morosidad
      );


    XLSX.utils.book_append_sheet(
      libro,
      hojaMorosidad,
      'MOROSIDAD_EXTRACTO'
    );


    // =======================================================
    // NOMBRE DEL ARCHIVO
    // =======================================================

    const fecha =
      this.normalizarFechaArchivo(
        evaluacion.fechaCorte
      );


    const nombreArchivo =
      `EVALUACION_CARTERA_${fecha}.xlsx`;


    XLSX.writeFile(
      libro,
      nombreArchivo,
      {
        compression: true
      }
    );
  }


  // =========================================================
  // HOJA 1
  // RESULTADOS COMPLETOS
  //
  // 1 fila = 1 crédito evaluado.
  // =========================================================

  private crearHojaResultados(
    evaluacion:
      EvaluacionCartera,
    resultados:
      EvaluacionCreditoResultado[]
  ): XLSX.WorkSheet {

    const filas =
      resultados.map(
        resultado => ({

          'ID EVALUACIÓN':
            resultado.idEvaluacionCartera,

          'FECHA CORTE':
            evaluacion.fechaCorte,

          'FECHA PROCESO':
            evaluacion.fechaEjecucion
            ?? '',

          'ID CRÉDITO EVALUADO':
            resultado.idEvaluacionCarteraCredito,

          'ID CRÉDITO':
            resultado.idCarteraCredito,

          'DOCUMENTO':
            resultado.documento,

          'NOMBRE':
            resultado.nombreCompleto,

          'CÓDIGO AGENCIA':
            resultado.codigoAgencia
            ?? '',

          'AGENCIA':
            resultado.nombreAgencia
            ?? '',

          'CÓDIGO LÍNEA':
            resultado.codigoLineaCredito
            ?? '',

          'LÍNEA DE CRÉDITO':
            resultado.nombreLineaCredito
            ?? '',

          'PAGARÉ':
            resultado.pagareCartera,

          'CLASIFICACIÓN':
            resultado.codigoClasificacionCredito,

          'SALDO INICIAL':
            Number(
              resultado.valorInicialCredito
              ?? 0
            ),

          'SALDO ACTUAL':
            Number(
              resultado.saldoActual
              ?? 0
            ),

          'EDAD MORA':
            resultado.edadMora
            ?? '',

          'RIESGO ANTERIOR':
            resultado.edadRiesgoAnterior
            ?? '',

          'RIESGO INICIAL':
            resultado.edadRiesgoInicial
            ?? '',

          'RIESGO CALCULADO':
            resultado.edadRiesgoCalculada
            ?? '',

          'RIESGO ARRASTRE':
            resultado.edadRiesgoArrastre
            ?? '',

          'RIESGO FINAL':
            resultado.edadRiesgoFinal
            ?? '',

          'PUNTAJE TOTAL':
            Number(
              resultado.puntajeTotal
              ?? 0
            ),

          'ACCIÓN':
            resultado.accionEvaluacion,

          'ACCIÓN SUGERIDA':
            this.descripcionAccion(
              resultado.accionEvaluacion
            ),

          'COMENTARIO':
            resultado.comentarioEvaluacion
            ?? ''

        })
      );


    const hoja =
      XLSX.utils.json_to_sheet(
        filas
      );


    this.configurarHojaResultados(
      hoja
    );


    return hoja;
  }


  // =========================================================
  // HOJA 2
  // CRITERIOS DETALLE
  //
  // REGLA:
  // 1 crédito = 1 fila.
  //
  // Cada criterio se convierte en columnas:
  // - resultado;
  // - puntaje.
  // =========================================================

  private crearHojaCriterios(
    evaluacion:
      EvaluacionCartera,
    detalles:
      EvaluacionExcelDetalleCredito[]
  ): XLSX.WorkSheet {

    const filas:
      Record<
        string,
        string | number | null
      >[] = [];


    for (
      const item
      of detalles
    ) {

      const credito =
        item.credito;


      const fila:
        Record<
          string,
          string | number | null
        > = {

          'ID EVALUACIÓN':
            credito.idEvaluacionCartera,

          'FECHA CORTE':
            evaluacion.fechaCorte,

          'ID CRÉDITO EVALUADO':
            credito.idEvaluacionCarteraCredito,

          'ID CRÉDITO':
            credito.idCarteraCredito,

          'DOCUMENTO':
            credito.documento,

          'NOMBRE':
            credito.nombreCompleto,

          'CÓDIGO AGENCIA':
            credito.codigoAgencia
            ?? '',

          'AGENCIA':
            credito.nombreAgencia
            ?? '',

          'CÓDIGO LÍNEA':
            credito.codigoLineaCredito
            ?? '',

          'LÍNEA':
            credito.nombreLineaCredito
            ?? '',

          'PAGARÉ':
            credito.pagareCartera,

          'CLASIFICACIÓN':
            credito.codigoClasificacionCredito,

          'SALDO INICIAL':
            Number(
              credito.valorInicialCredito
              ?? 0
            ),

          'SALDO ACTUAL':
            Number(
              credito.saldoActual
              ?? 0
            ),

          'EDAD MORA':
            credito.edadMora
            ?? '',

          'RIESGO ACTUAL':
            credito.edadRiesgoAnterior
            ?? '',

          'RIESGO INICIAL':
            credito.edadRiesgoInicial
            ?? '',

          'RIESGO CALCULADO':
            credito.edadRiesgoCalculada
            ?? '',

          'RIESGO ARRASTRE':
            credito.edadRiesgoArrastre
            ?? '',

          'RIESGO SUGERIDO':
            credito.edadRiesgoFinal
            ?? '',

          'PUNTAJE TOTAL':
            Number(
              credito.puntajeTotal
              ?? 0
            ),

          'ACCIÓN':
            this.descripcionAccion(
              credito.accionEvaluacion
            )

        };


      // =======================================================
      // CRITERIOS DEL CRÉDITO
      // =======================================================

      const criterios =
        Array.isArray(
          item.criterios
        )
          ? item.criterios
              .slice()
              .sort(
                (
                  a,
                  b
                ) =>
                  Number(
                    a.ordenEvaluacion
                    ?? 0
                  )
                  -
                  Number(
                    b.ordenEvaluacion
                    ?? 0
                  )
              )
          : [];


      for (
        const criterio
        of criterios
      ) {

        const codigo =
          String(
            criterio.codigoCriterio
            ?? ''
          )
            .trim();


        const nombre =
          String(
            criterio.nombreCriterio
            ?? ''
          )
            .trim();


        const prefijo =
          codigo
            ? `${codigo} ${nombre}`
            : nombre;


        fila[
          `${prefijo} - RESULTADO`
        ] =
          criterio.descripcionResultado
          ?? '';


        fila[
          `${prefijo} - PUNTAJE`
        ] =
          Number(
            criterio.puntajeObtenido
            ?? 0
          );
      }


      filas.push(
        fila
      );
    }


    const hoja =
      XLSX.utils.json_to_sheet(
        filas
      );


    this.aplicarAutofiltro(
      hoja
    );


    // =========================================================
    // ANCHOS
    // =========================================================

    const columnasBase:
      XLSX.ColInfo[] = [

        { wch: 12 }, // id evaluación
        { wch: 13 }, // fecha corte
        { wch: 18 }, // id crédito evaluado
        { wch: 14 }, // id crédito
        { wch: 16 }, // documento
        { wch: 38 }, // nombre
        { wch: 15 }, // código agencia
        { wch: 25 }, // agencia
        { wch: 15 }, // código línea
        { wch: 32 }, // línea
        { wch: 12 }, // pagaré
        { wch: 16 }, // clasificación
        { wch: 18 }, // saldo inicial
        { wch: 18 }, // saldo actual
        { wch: 12 }, // edad mora
        { wch: 15 }, // riesgo actual
        { wch: 15 }, // riesgo inicial
        { wch: 16 }, // riesgo calculado
        { wch: 16 }, // arrastre
        { wch: 16 }, // riesgo sugerido
        { wch: 15 }, // puntaje total
        { wch: 18 }  // acción

      ];


    // =========================================================
    // CONTAR CRITERIOS ÚNICOS
    // =========================================================

    const criteriosUnicos =
      new Map<
        string,
        EvaluacionCriterioResultado
      >();


    for (
      const item
      of detalles
    ) {

      for (
        const criterio
        of item.criterios
      ) {

        const codigo =
          String(
            criterio.codigoCriterio
            ?? ''
          )
            .trim();


        if (
          codigo
          && !criteriosUnicos.has(
            codigo
          )
        ) {

          criteriosUnicos.set(
            codigo,
            criterio
          );
        }
      }
    }


    const criteriosOrdenados =
      Array.from(
        criteriosUnicos.values()
      )
        .sort(
          (
            a,
            b
          ) =>
            Number(
              a.ordenEvaluacion
              ?? 0
            )
            -
            Number(
              b.ordenEvaluacion
              ?? 0
            )
        );


    const columnasCriterios:
      XLSX.ColInfo[] = [];


    for (
      const _criterio
      of criteriosOrdenados
    ) {

      // RESULTADO

      columnasCriterios.push({
        wch: 36
      });


      // PUNTAJE

      columnasCriterios.push({
        wch: 12
      });
    }


    hoja['!cols'] = [
      ...columnasBase,
      ...columnasCriterios
    ];


    return hoja;
  }


  // =========================================================
  // HOJA 3
  // CLASIFICACIÓN ACTUAL
  //
  // Edad de riesgo vigente antes de la evaluación.
  // =========================================================

  private crearHojaClasificacionActual(
    resultados:
      EvaluacionCreditoResultado[]
  ): XLSX.WorkSheet {

    const mapa =
      new Map<
        string,
        ResumenCantidadSaldo
      >();


    for (
      const resultado
      of resultados
    ) {

      const categoria =
        (
          resultado.edadRiesgoAnterior
          ?? ''
        )
          .trim()
          .toUpperCase();


      if (!categoria) {
        continue;
      }


      this.acumular(
        mapa,
        categoria,
        resultado.saldoActual
      );
    }


    const filas =
      Array.from(
        mapa.entries()
      )
        .sort(
          (
            [categoriaA],
            [categoriaB]
          ) =>
            categoriaA.localeCompare(
              categoriaB
            )
        )
        .map(
          ([
            categoria,
            resumen
          ]) => ({

            'CATEGORÍA ACTUAL':
              categoria,

            'OPERACIONES':
              resumen.cantidad,

            'SALDO ACTUAL':
              resumen.saldoActual

          })
        );


    filas.push({

      'CATEGORÍA ACTUAL':
        'TOTALES',

      'OPERACIONES':
        resultados.length,

      'SALDO ACTUAL':
        this.totalSaldo(
          resultados
        )

    });


    const hoja =
      XLSX.utils.json_to_sheet(
        filas
      );


    hoja['!cols'] = [
      { wch: 22 },
      { wch: 16 },
      { wch: 20 }
    ];


    return hoja;
  }


  // =========================================================
  // HOJA 4
  // CLASIFICACIÓN X CRÉDITO
  //
  // Clasificación S/C/M/P + edad de riesgo actual.
  // =========================================================

  private crearHojaClasificacionCredito(
    resultados:
      EvaluacionCreditoResultado[]
  ): XLSX.WorkSheet {

    const mapa =
      new Map<
        string,
        ResumenCantidadSaldo
      >();


    for (
      const resultado
      of resultados
    ) {

      const clasificacion =
        (
          resultado.codigoClasificacionCredito
          ?? ''
        )
          .trim()
          .toUpperCase();


      const categoria =
        (
          resultado.edadRiesgoAnterior
          ?? ''
        )
          .trim()
          .toUpperCase();


      const llave =
        `${clasificacion}|${categoria}`;


      this.acumular(
        mapa,
        llave,
        resultado.saldoActual
      );
    }


    const filas =
      Array.from(
        mapa.entries()
      )
        .map(
          ([
            llave,
            resumen
          ]) => {

            const [
              clasificacion,
              categoria
            ] =
              llave.split('|');


            return {

              'CLASIFICACIÓN':
                clasificacion,

              'CATEGORÍA':
                categoria,

              'CANTIDAD':
                resumen.cantidad,

              'SALDO ACTUAL':
                resumen.saldoActual

            };
          }
        )
        .sort(
          (
            a,
            b
          ) => {

            const clasificacion =
              String(
                a['CLASIFICACIÓN']
              )
                .localeCompare(
                  String(
                    b['CLASIFICACIÓN']
                  )
                );


            if (
              clasificacion !== 0
            ) {

              return clasificacion;
            }


            return String(
              a['CATEGORÍA']
            )
              .localeCompare(
                String(
                  b['CATEGORÍA']
                )
              );
          }
        );


    const hoja =
      XLSX.utils.json_to_sheet(
        filas
      );


    hoja['!cols'] = [
      { wch: 18 },
      { wch: 16 },
      { wch: 14 },
      { wch: 20 }
    ];


    return hoja;
  }


  // =========================================================
  // HOJA 5
  // VARIABLES TRABAJADAS
  //
  // Agrupa los resultados obtenidos por criterio y regla.
  // =========================================================

  private crearHojaVariablesTrabajadas(
    detalles:
      EvaluacionExcelDetalleCredito[]
  ): XLSX.WorkSheet {

    const mapa =
      new Map<
        string,
        {
          codigoCriterio: string;
          nombreCriterio: string;
          codigoRegla: string;
          nombreRegla: string;
          descripcionResultado: string;
          puntaje: number;
          cantidad: number;
          saldoActual: number;
        }
      >();


    for (
      const item
      of detalles
    ) {

      const criterios =
        Array.isArray(
          item.criterios
        )
          ? item.criterios
          : [];


      for (
        const criterio
        of criterios
      ) {

        const llave =
          [
            criterio.codigoCriterio,
            criterio.codigoRegla,
            criterio.descripcionResultado
          ].join('|');


        const actual =
          mapa.get(
            llave
          );


        if (!actual) {

          mapa.set(
            llave,
            {

              codigoCriterio:
                criterio.codigoCriterio,

              nombreCriterio:
                criterio.nombreCriterio,

              codigoRegla:
                criterio.codigoRegla,

              nombreRegla:
                criterio.nombreRegla,

              descripcionResultado:
                criterio.descripcionResultado,

              puntaje:
                Number(
                  criterio.puntajeObtenido
                  ?? 0
                ),

              cantidad:
                1,

              saldoActual:
                Number(
                  item.credito.saldoActual
                  ?? 0
                )

            }
          );

          continue;
        }


        actual.cantidad++;

        actual.saldoActual +=
          Number(
            item.credito.saldoActual
            ?? 0
          );
      }
    }


    const filas =
      Array.from(
        mapa.values()
      )
        .sort(
          (
            a,
            b
          ) => {

            const criterio =
              a.codigoCriterio.localeCompare(
                b.codigoCriterio
              );


            if (
              criterio !== 0
            ) {

              return criterio;
            }


            return a.codigoRegla.localeCompare(
              b.codigoRegla
            );
          }
        )
        .map(
          item => ({

            'CÓDIGO CRITERIO':
              item.codigoCriterio,

            'VARIABLE / CRITERIO':
              item.nombreCriterio,

            'CÓDIGO REGLA':
              item.codigoRegla,

            'REGLA':
              item.nombreRegla,

            'RESULTADO':
              item.descripcionResultado,

            'PUNTAJE':
              item.puntaje,

            'CANTIDAD':
              item.cantidad,

            'SALDO ACTUAL':
              item.saldoActual

          })
        );


    const hoja =
      XLSX.utils.json_to_sheet(
        filas
      );


    hoja['!cols'] = [
      { wch: 16 },
      { wch: 32 },
      { wch: 14 },
      { wch: 30 },
      { wch: 38 },
      { wch: 12 },
      { wch: 14 },
      { wch: 20 }
    ];


    return hoja;
  }


  // =========================================================
  // HOJA 6
  // ACCIONES SUGERIDAS
  //
  // R / H / M.
  // =========================================================

  private crearHojaAccionesSugeridas(
    resultados:
      EvaluacionCreditoResultado[]
  ): XLSX.WorkSheet {

    const mapa =
      new Map<
        string,
        ResumenCantidadSaldo
      >();


    for (
      const resultado
      of resultados
    ) {

      const accion =
        resultado.accionEvaluacion;


      this.acumular(
        mapa,
        accion,
        resultado.saldoActual
      );
    }


    const filas =
      Array.from(
        mapa.entries()
      )
        .sort(
          (
            [accionA],
            [accionB]
          ) =>
            accionA.localeCompare(
              accionB
            )
        )
        .map(
          ([
            accion,
            resumen
          ]) => ({

            'SUGERENCIA':
              this.descripcionAccion(
                accion
              ),

            'CÓDIGO':
              accion,

            'CANTIDAD':
              resumen.cantidad,

            'SALDO ACTUAL':
              resumen.saldoActual

          })
        );


    filas.push({

      'SUGERENCIA':
        'TOTALES',

      'CÓDIGO':
        '',

      'CANTIDAD':
        resultados.length,

      'SALDO ACTUAL':
        this.totalSaldo(
          resultados
        )

    });


    const hoja =
      XLSX.utils.json_to_sheet(
        filas
      );


    hoja['!cols'] = [
      { wch: 22 },
      { wch: 12 },
      { wch: 14 },
      { wch: 20 }
    ];


    return hoja;
  }


  // =========================================================
  // HOJA 7
  // ACCIONES X CLASIFICACIÓN
  // =========================================================

  private crearHojaAccionesClasificacion(
    resultados:
      EvaluacionCreditoResultado[]
  ): XLSX.WorkSheet {

    const mapa =
      new Map<
        string,
        ResumenCantidadSaldo
      >();


    for (
      const resultado
      of resultados
    ) {

      const accion =
        resultado.accionEvaluacion;


      const clasificacion =
        (
          resultado.codigoClasificacionCredito
          ?? ''
        )
          .trim()
          .toUpperCase();


      const llave =
        `${accion}|${clasificacion}`;


      this.acumular(
        mapa,
        llave,
        resultado.saldoActual
      );
    }


    const filas =
      Array.from(
        mapa.entries()
      )
        .map(
          ([
            llave,
            resumen
          ]) => {

            const [
              accion,
              clasificacion
            ] =
              llave.split('|');


            return {

              'SUGERENCIA':
                this.descripcionAccion(
                  accion
                ),

              'CLASIFICACIÓN':
                clasificacion,

              'CANTIDAD':
                resumen.cantidad,

              'SALDO ACTUAL':
                resumen.saldoActual

            };
          }
        )
        .sort(
          (
            a,
            b
          ) => {

            const accion =
              String(
                a['SUGERENCIA']
              )
                .localeCompare(
                  String(
                    b['SUGERENCIA']
                  )
                );


            if (
              accion !== 0
            ) {

              return accion;
            }


            return String(
              a['CLASIFICACIÓN']
            )
              .localeCompare(
                String(
                  b['CLASIFICACIÓN']
                )
              );
          }
        );


    const hoja =
      XLSX.utils.json_to_sheet(
        filas
      );


    hoja['!cols'] = [
      { wch: 22 },
      { wch: 18 },
      { wch: 14 },
      { wch: 20 }
    ];


    return hoja;
  }


  // =========================================================
  // HOJA 8
  // RECALIFICADOS
  //
  // Solo acción R.
  // =========================================================

  private crearHojaRecalificados(
    resultados:
      EvaluacionCreditoResultado[]
  ): XLSX.WorkSheet {

    const mapa =
      new Map<
        string,
        ResumenCantidadSaldo
      >();


    const recalificados =
      resultados.filter(
        resultado =>
          resultado.accionEvaluacion
          === 'R'
      );


    for (
      const resultado
      of recalificados
    ) {

      const actual =
        (
          resultado.edadRiesgoAnterior
          ?? ''
        )
          .trim()
          .toUpperCase();


      const nueva =
        (
          resultado.edadRiesgoFinal
          ?? ''
        )
          .trim()
          .toUpperCase();


      const llave =
        `${actual}|${nueva}`;


      this.acumular(
        mapa,
        llave,
        resultado.saldoActual
      );
    }


    const filas =
      Array.from(
        mapa.entries()
      )
        .map(
          ([
            llave,
            resumen
          ]) => {

            const [
              actual,
              nueva
            ] =
              llave.split('|');


            return {

              'SUGERENCIA':
                'Reclasificar',

              'CATEGORÍA ACTUAL':
                actual,

              'CATEGORÍA NUEVA':
                nueva,

              'CANTIDAD':
                resumen.cantidad,

              'SALDO ACTUAL':
                resumen.saldoActual

            };
          }
        )
        .sort(
          (
            a,
            b
          ) => {

            const actual =
              String(
                a['CATEGORÍA ACTUAL']
              )
                .localeCompare(
                  String(
                    b['CATEGORÍA ACTUAL']
                  )
                );


            if (
              actual !== 0
            ) {

              return actual;
            }


            return String(
              a['CATEGORÍA NUEVA']
            )
              .localeCompare(
                String(
                  b['CATEGORÍA NUEVA']
                )
              );
          }
        );


    const hoja =
      XLSX.utils.json_to_sheet(
        filas
      );


    hoja['!cols'] = [
      { wch: 20 },
      { wch: 20 },
      { wch: 20 },
      { wch: 14 },
      { wch: 20 }
    ];


    return hoja;
  }


  // =========================================================
  // HOJA 9
  // FOTO HOJA DE VIDA
  //
  // Fotografía histórica utilizada por la evaluación.
  //
  // 1 fila = 1 asociado.
  // =========================================================

  private crearHojaFotoHojaVida(
    datos:
      EvaluacionResultadoHojaVida[]
  ): XLSX.WorkSheet {

    const filas =
      datos.map(
        item => ({

          // ===================================================
          // IDENTIFICACIÓN
          // ===================================================

          'ID CIERRE HOJA VIDA PERSONA':
            item.idCierreHojaVidaPersona,

          'ID CIERRE HOJA VIDA':
            item.idCierreHojaVida,

          'ID DATOS PERSONAL':
            item.idDatosPersonal,

          'TIPO DOCUMENTO':
            item.tipoDocumento
            ?? '',

          'DOCUMENTO':
            item.documento
            ?? '',

          'TIPO PERSONA':
            item.tipoPersona
            ?? '',

          'TIENE RUT':
            item.tieneRut === true
              ? 'SI'
              : item.tieneRut === false
                ? 'NO'
                : '',

          'DÍGITO VERIFICACIÓN':
            item.digitoVerificacion
            ?? '',

          'NOMBRE':
            item.nombreCompleto
            ?? '',


          // ===================================================
          // FECHAS
          // ===================================================

          'FECHA NACIMIENTO':
            item.fechaNacimiento
            ?? '',

          'FECHA APERTURA':
            item.fechaApertura
            ?? '',

          'FECHA ACTUALIZACIÓN':
            item.fechaActualizacion
            ?? '',


          // ===================================================
          // INFORMACIÓN PERSONAL
          // ===================================================

          'GÉNERO':
            item.codigoGenero
            ?? '',

          'ESTADO CIVIL':
            item.codigoEstadoCivil
            ?? '',

          'ESCOLARIDAD':
            item.codigoEscolaridad
            ?? '',

          'CABEZA FAMILIA':
            item.cabezaFamilia
            ?? '',

          'ESTRATO':
            item.estratoSocial
            ?? '',

          'TIPO VIVIENDA':
            item.codigoTipoVivienda
            ?? '',

          'NÚMERO HIJOS':
            item.numeroHijos
            ?? 0,

          'OCUPACIÓN':
            item.codigoOcupacion
            ?? '',

          'SECTOR ECONÓMICO':
            item.codigoSectorEconomico
            ?? '',

          'ACTIVIDAD SES':
            item.codigoActividadSes
            ?? '',

          'ACTIVIDAD DIAN':
            item.codigoActividadDian
            ?? '',


          // ===================================================
          // UBICACIÓN
          // ===================================================

          'DIRECCIÓN':
            item.direccion
            ?? '',

          'BARRIO':
            item.barrio
            ?? '',

          'TELÉFONO':
            item.telefono
            ?? '',

          'CELULAR 1':
            item.celularUno
            ?? '',

          'CELULAR 2':
            item.celularDos
            ?? '',

          'CORREO':
            item.correo
            ?? '',

          'ID PAÍS':
            item.idPais
            ?? '',

          'ID DEPARTAMENTO':
            item.idDepartamento
            ?? '',

          'ID CIUDAD':
            item.idCiudad
            ?? '',

          'ID ZONA':
            item.idZona
            ?? '',

          'ID SUBZONA':
            item.idSubZona
            ?? '',


          // ===================================================
          // INGRESOS
          // ===================================================

          'SALARIO':
            Number(
              item.valorSalario
              ?? 0
            ),

          'PENSIÓN':
            Number(
              item.valorPension
              ?? 0
            ),

          'INGRESOS ARRIENDO':
            Number(
              item.ingresosArriendo
              ?? 0
            ),

          'INGRESOS COMISIONES':
            Number(
              item.ingresosComisiones
              ?? 0
            ),

          'OTROS INGRESOS':
            Number(
              item.otrosIngresos
              ?? 0
            ),

          'INGRESOS TOTALES':
            Number(
              item.ingresosTotales
              ?? 0
            ),


          // ===================================================
          // EGRESOS
          // ===================================================

          'EGRESOS FAMILIARES':
            Number(
              item.egresosFamiliares
              ?? 0
            ),

          'EGRESOS ARRIENDO':
            Number(
              item.egresosArriendo
              ?? 0
            ),

          'EGRESOS CRÉDITO':
            Number(
              item.egresosCredito
              ?? 0
            ),

          'OTROS EGRESOS':
            Number(
              item.otrosEgresos
              ?? 0
            ),

          'EGRESOS TOTALES':
            Number(
              item.egresosTotales
              ?? 0
            ),


          // ===================================================
          // PATRIMONIO
          // ===================================================

          'TOTAL ACTIVOS':
            Number(
              item.totalActivos
              ?? 0
            ),

          'TOTAL PASIVOS':
            Number(
              item.totalPasivos
              ?? 0
            ),

          'PATRIMONIO':
            Number(
              item.patrimonioTotal
              ?? 0
            ),


          // ===================================================
          // FINANCIERA COMPLEMENTARIA
          // ===================================================

          'DEUDA RELACIÓN FINANCIERA':
            Number(
              item.deudaRelacionFinanciera
              ?? 0
            ),

          'ORIGEN FONDOS':
            item.origenFondos
            ?? '',

          'RELACIÓN FINANCIERA':
            item.relacionFinanciera
            ?? '',

          'INGRESO DISPONIBLE':
            Number(
              item.ingresoDisponible
              ?? 0
            )

        })
      );


    const hoja =
      XLSX.utils.json_to_sheet(
        filas
      );


    this.aplicarAutofiltro(
      hoja
    );


    hoja['!cols'] = [

      { wch: 24 },
      { wch: 20 },
      { wch: 18 },

      { wch: 16 },
      { wch: 18 },
      { wch: 14 },
      { wch: 12 },
      { wch: 18 },
      { wch: 38 },

      { wch: 16 },
      { wch: 16 },
      { wch: 18 },

      { wch: 12 },
      { wch: 16 },
      { wch: 16 },
      { wch: 16 },
      { wch: 12 },
      { wch: 16 },
      { wch: 14 },
      { wch: 16 },
      { wch: 20 },
      { wch: 18 },
      { wch: 18 },

      { wch: 35 },
      { wch: 22 },
      { wch: 18 },
      { wch: 18 },
      { wch: 18 },
      { wch: 35 },

      { wch: 12 },
      { wch: 18 },
      { wch: 14 },
      { wch: 12 },
      { wch: 12 },

      { wch: 18 },
      { wch: 18 },
      { wch: 20 },
      { wch: 22 },
      { wch: 18 },
      { wch: 20 },

      { wch: 20 },
      { wch: 20 },
      { wch: 20 },
      { wch: 18 },
      { wch: 20 },

      { wch: 20 },
      { wch: 20 },
      { wch: 20 },

      { wch: 26 },
      { wch: 35 },
      { wch: 35 },
      { wch: 22 }

    ];


    return hoja;
  }


  // =========================================================
  // HOJA 10
  // MOROSIDAD EXTRACTO
  //
  // Representa el insumo utilizado por el criterio 401.
  //
  // 1 fila = 1 comprobante consolidado por:
  //
  // id_cartera_credito
  // + tipo_comprobante
  // + numero_comprobante
  // =========================================================

  private crearHojaMorosidadExtracto(
    datos:
      EvaluacionResultadoMorosidad[]
  ): XLSX.WorkSheet {

    const filas =
      datos.map(
        item => ({

          'ID CIERRE CRÉDITO':
            item.idCierreCarteraCredito,

          'ID CRÉDITO':
            item.idCarteraCredito,

          'ID DATOS PERSONAL':
            item.idDatosPersonal,

          'DOCUMENTO':
            item.documento
            ?? '',

          'NOMBRE':
            item.nombreCompleto
            ?? '',

          'ID AGENCIA':
            item.idAgencia
            ?? '',

          'CÓDIGO AGENCIA':
            item.codigoAgencia
            ?? '',

          'AGENCIA':
            item.nombreAgencia
            ?? '',

          'ID LÍNEA':
            item.idLineaCredito
            ?? '',

          'CÓDIGO LÍNEA':
            item.codigoLineaCredito
            ?? '',

          'LÍNEA DE CRÉDITO':
            item.nombreLineaCredito
            ?? '',

          'PAGARÉ':
            item.pagareCartera
            ?? '',

          'TIPO COMPROBANTE':
            item.tipoComprobante
            ?? '',

          'NÚMERO COMPROBANTE':
            item.numeroComprobante
            ?? '',

          'FECHA CONTABLE DESDE':
            item.fechaContableDesde
            ?? '',

          'FECHA CONTABLE HASTA':
            item.fechaContableHasta
            ?? '',

          'VALOR CAPITAL':
            Number(
              item.valorCapital
              ?? 0
            ),

          'VALOR MORA':
            Number(
              item.valorMora
              ?? 0
            ),

          'DÍAS MORA':
            item.diasMora
            ?? ''

        })
      );


    const hoja =
      XLSX.utils.json_to_sheet(
        filas
      );


    this.aplicarAutofiltro(
      hoja
    );


    hoja['!cols'] = [
      { wch: 20 },
      { wch: 14 },
      { wch: 18 },
      { wch: 18 },
      { wch: 38 },
      { wch: 14 },
      { wch: 15 },
      { wch: 25 },
      { wch: 14 },
      { wch: 15 },
      { wch: 32 },
      { wch: 14 },
      { wch: 20 },
      { wch: 22 },
      { wch: 20 },
      { wch: 20 },
      { wch: 18 },
      { wch: 18 },
      { wch: 14 }
    ];


    return hoja;
  }


  // =========================================================
  // ACUMULAR CANTIDAD + SALDO
  // =========================================================

  private acumular(
    mapa:
      Map<
        string,
        ResumenCantidadSaldo
      >,
    llave:
      string,
    saldo:
      number
      | null
      | undefined
  ): void {

    const actual =
      mapa.get(
        llave
      );


    if (!actual) {

      mapa.set(
        llave,
        {

          cantidad:
            1,

          saldoActual:
            Number(
              saldo
              ?? 0
            )

        }
      );

      return;
    }


    actual.cantidad++;


    actual.saldoActual +=
      Number(
        saldo
        ?? 0
      );
  }


  // =========================================================
  // TOTAL SALDO
  // =========================================================

  private totalSaldo(
    resultados:
      EvaluacionCreditoResultado[]
  ): number {

    return resultados.reduce(
      (
        total,
        resultado
      ) =>
        total
        + Number(
          resultado.saldoActual
          ?? 0
        ),
      0
    );
  }


  // =========================================================
  // DESCRIPCIÓN ACCIÓN
  // =========================================================

  private descripcionAccion(
    accion:
      string
      | null
      | undefined
  ): string {

    if (!accion) {
      return '';
    }


    const codigo =
      accion
        .trim()
        .toUpperCase();


    if (
      codigo === 'R'
      || codigo === 'H'
      || codigo === 'M'
    ) {

      return (
        ACCION_EVALUACION_DESCRIPCION[
          codigo
        ]
        ?? codigo
      );
    }


    return codigo;
  }


  // =========================================================
  // CONFIGURAR HOJA PRINCIPAL
  // =========================================================

  private configurarHojaResultados(
    hoja:
      XLSX.WorkSheet
  ): void {

    this.aplicarAutofiltro(
      hoja
    );


    hoja['!cols'] = [
      { wch: 12 },
      { wch: 13 },
      { wch: 15 },
      { wch: 20 },
      { wch: 14 },
      { wch: 16 },
      { wch: 40 },
      { wch: 15 },
      { wch: 25 },
      { wch: 15 },
      { wch: 32 },
      { wch: 12 },
      { wch: 16 },
      { wch: 18 },
      { wch: 18 },
      { wch: 12 },
      { wch: 16 },
      { wch: 16 },
      { wch: 18 },
      { wch: 18 },
      { wch: 16 },
      { wch: 16 },
      { wch: 12 },
      { wch: 20 },
      { wch: 55 }
    ];
  }


  // =========================================================
  // APLICAR AUTOFILTRO
  // =========================================================

  private aplicarAutofiltro(
    hoja:
      XLSX.WorkSheet
  ): void {

    if (
      !hoja['!ref']
    ) {
      return;
    }


    hoja['!autofilter'] = {
      ref:
        hoja['!ref']
    };
  }


  // =========================================================
  // FECHA PARA NOMBRE DE ARCHIVO
  // =========================================================

  private normalizarFechaArchivo(
    fecha:
      string
      | null
      | undefined
  ): string {

    if (!fecha) {
      return 'SIN_FECHA';
    }


    return String(
      fecha
    )
      .substring(
        0,
        10
      )
      .replace(
        /-/g,
        ''
      );
  }
}
