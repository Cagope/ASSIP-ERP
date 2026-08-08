import {
  Injectable,
  inject
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  environment
} from '../../../../../../environments/environment';

import {
  AccionEvaluacionCartera,
  EvaluacionCreditoResultado,
  EvaluacionCriterioResultado,
  EvaluacionResultadoHojaVida,
  EvaluacionResultadoMorosidad
} from '../evaluacion-cartera.models';

@Injectable({
  providedIn: 'root'
})
export class EvaluacionCarteraResultadosApi {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/evaluaciones`;


  // =========================================================
  // LISTAR RESULTADOS DE LA EVALUACIÓN
  //
  // Base para:
  // - pantalla de resultados;
  // - filtros;
  // - exportación general a Excel.
  // =========================================================

  listarResultados(
    idEvaluacionCartera: number
  ): Observable<EvaluacionCreditoResultado[]> {

    return this.http.get<EvaluacionCreditoResultado[]>(
      `${this.baseUrl}/${idEvaluacionCartera}/resultados`
    );
  }


  // =========================================================
  // BUSCAR RESULTADO INDIVIDUAL
  //
  // Base para:
  // - consulta individual;
  // - impresión individual.
  // =========================================================

  buscarResultadoPorId(
    idEvaluacionCartera: number,
    idEvaluacionCarteraCredito: number
  ): Observable<EvaluacionCreditoResultado> {

    return this.http.get<EvaluacionCreditoResultado>(
      `${this.baseUrl}/${idEvaluacionCartera}/resultados/${idEvaluacionCarteraCredito}`
    );
  }


  // =========================================================
  // LISTAR DETALLE DE CRITERIOS
  //
  // Devuelve los criterios aplicados a un crédito.
  // =========================================================

  listarDetalle(
    idEvaluacionCartera: number,
    idEvaluacionCarteraCredito: number
  ): Observable<EvaluacionCriterioResultado[]> {

    return this.http.get<EvaluacionCriterioResultado[]>(
      `${this.baseUrl}/${idEvaluacionCartera}/resultados/${idEvaluacionCarteraCredito}/detalle`
    );
  }


  // =========================================================
  // LISTAR DETALLE MASIVO POR ACCIÓN
  //
  // R = Reclasificar
  // H = Habilitar
  // M = Mantener
  //
  // IMPORTANTE:
  //
  // Este método sustituye cientos o miles de llamadas
  // individuales al endpoint /detalle.
  //
  // Todo el detalle de la acción se obtiene mediante
  // una sola solicitud HTTP.
  // =========================================================

  listarDetallePorAccion(
    idEvaluacionCartera: number,
    accionEvaluacion: AccionEvaluacionCartera
  ): Observable<EvaluacionCriterioResultado[]> {

    return this.http.get<EvaluacionCriterioResultado[]>(
      `${this.baseUrl}/${idEvaluacionCartera}/resultados/detalle/accion/${accionEvaluacion}`
    );
  }

  // =========================================================
  // LISTAR INSUMO DE HOJA DE VIDA
  //
  // Devuelve una fila por asociado incluido en la evaluación.
  //
  // La información corresponde a la fotografía histórica
  // utilizada por el motor.
  // =========================================================

  listarFotoHojaVida(
    idEvaluacionCartera: number
  ): Observable<EvaluacionResultadoHojaVida[]> {

    return this.http.get<EvaluacionResultadoHojaVida[]>(
      `${this.baseUrl}/${idEvaluacionCartera}/resultados/insumos/hoja-vida`
    );
  }


  // =========================================================
  // LISTAR INSUMO DE MOROSIDAD
  //
  // Devuelve los comprobantes consolidados utilizados por
  // el criterio 401 - Servicio de la deuda.
  // =========================================================

  listarMorosidadExtracto(
    idEvaluacionCartera: number
  ): Observable<EvaluacionResultadoMorosidad[]> {

    return this.http.get<EvaluacionResultadoMorosidad[]>(
      `${this.baseUrl}/${idEvaluacionCartera}/resultados/insumos/morosidad`
    );
  }

}
