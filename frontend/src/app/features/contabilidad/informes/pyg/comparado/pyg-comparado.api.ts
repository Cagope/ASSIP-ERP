
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import {
  PyGComparadoFiltro,
  PyGComparadoRespuesta
} from './pyg-comparado.models';

/**
 * API del Estado de Excedentes o Pérdidas con formato comparado.
 * Centraliza la consulta y las exportaciones PDF y Excel.
 */
@Injectable({
  providedIn: 'root'
})
export class PyGComparadoApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl =
    `${environment.apiUrl}/contabilidad/informes/pyg/comparado`;

  /**
   * Construye los parámetros de agencia y ambos períodos.
   * Garantiza que consulta y exportaciones utilicen los mismos filtros.
   */
  private construirParametros(filtro: PyGComparadoFiltro): HttpParams {
    return new HttpParams()
      .set('idAgencia', String(filtro.idAgencia))
      .set('anoPeriodo1', String(filtro.anoPeriodo1))
      .set('mesPeriodo1', String(filtro.mesPeriodo1))
      .set('anoPeriodo2', String(filtro.anoPeriodo2))
      .set('mesPeriodo2', String(filtro.mesPeriodo2));
  }

  /**
   * Consulta el PyG de los dos períodos seleccionados.
   * El backend valida el permiso y el alcance de agencia.
   */
  consultar(filtro: PyGComparadoFiltro): Observable<PyGComparadoRespuesta> {
    return this.http.get<PyGComparadoRespuesta>(
      this.baseUrl,
      { params: this.construirParametros(filtro) }
    );
  }

  /**
   * Descarga el informe PDF con ambos períodos y sus variaciones.
   * Incluye las firmas institucionales cuando se solicita.
   */
  exportarPdf(
    filtro: PyGComparadoFiltro,
    incluirFirmas: boolean
  ): Observable<Blob> {
    const params = this.construirParametros(filtro)
      .set('incluirFirmas', String(incluirFirmas));

    return this.http.get(
      `${this.baseUrl}/pdf`,
      {
        params,
        responseType: 'blob'
      }
    );
  }

  /**
   * Descarga el Excel comparativo con importes numéricos.
   * Incluye las firmas institucionales cuando se solicita.
   */
  exportarExcel(
    filtro: PyGComparadoFiltro,
    incluirFirmas: boolean
  ): Observable<Blob> {
    const params = this.construirParametros(filtro)
      .set('incluirFirmas', String(incluirFirmas));

    return this.http.get(
      `${this.baseUrl}/excel`,
      {
        params,
        responseType: 'blob'
      }
    );
  }
}
