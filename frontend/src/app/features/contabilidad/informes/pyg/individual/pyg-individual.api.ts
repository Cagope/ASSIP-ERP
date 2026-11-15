
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import {
  PyGIndividualFiltro,
  PyGIndividualRespuesta
} from './pyg-individual.models';

/**
 * API del Estado de Excedentes o Pérdidas con formato individual.
 * Centraliza la consulta y las exportaciones PDF y Excel.
 */
@Injectable({
  providedIn: 'root'
})
export class PyGIndividualApi {
  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/contabilidad/informes/pyg/individual`;

  /**
   * Construye los parámetros de agencia, período y detalle.
   * Reutiliza los mismos filtros en consulta y exportaciones.
   */
  private construirParametros(filtro: PyGIndividualFiltro): HttpParams {
    return new HttpParams()
      .set('idAgencia', String(filtro.idAgencia))
      .set('ano', String(filtro.ano))
      .set('mes', String(filtro.mes))
      .set('incluirDetalle', String(filtro.incluirDetalle));
  }

  /**
   * Consulta el PyG individual del período seleccionado.
   * El backend valida permisos y alcance de agencia.
   */
  consultar(filtro: PyGIndividualFiltro): Observable<PyGIndividualRespuesta> {
    return this.http.get<PyGIndividualRespuesta>(
      this.baseUrl,
      {
        params: this.construirParametros(filtro)
      }
    );
  }

  /**
   * Descarga el informe PDF con detalle opcional.
   * Incorpora firmas institucionales únicamente si se solicitan.
   */
  exportarPdf(
    filtro: PyGIndividualFiltro,
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
   * Descarga el Excel individual con importes numéricos.
   * No envía parámetros de firmas institucionales.
   */
  exportarExcel(filtro: PyGIndividualFiltro): Observable<Blob> {
    return this.http.get(
      `${this.baseUrl}/excel`,
      {
        params: this.construirParametros(filtro),
        responseType: 'blob'
      }
    );
  }
}
