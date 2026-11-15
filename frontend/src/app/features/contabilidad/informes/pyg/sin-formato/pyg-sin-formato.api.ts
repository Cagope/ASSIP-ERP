
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import {
  PyGSinFormatoFiltro,
  PyGSinFormatoRespuesta
} from './pyg-sin-formato.models';

/**
 * API del Estado de Excedentes o Pérdidas sin formato.
 * Consulta resultados y descarga informes PDF y Excel.
 */
@Injectable({
  providedIn: 'root'
})
export class PyGSinFormatoApi {
  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/contabilidad/informes/pyg/sin-formato`;

  /**
   * Construye los parámetros de agencia, año y mes.
   * Reutiliza los mismos filtros en consulta y exportaciones.
   */
  private construirParametros(filtro: PyGSinFormatoFiltro): HttpParams {
    return new HttpParams()
      .set('idAgencia', String(filtro.idAgencia))
      .set('ano', String(filtro.ano))
      .set('mes', String(filtro.mes));
  }

  /**
   * Consulta el PyG mensual de la agencia seleccionada.
   * El backend valida permisos y alcance de agencia.
   */
  consultar(filtro: PyGSinFormatoFiltro): Observable<PyGSinFormatoRespuesta> {
    return this.http.get<PyGSinFormatoRespuesta>(
      this.baseUrl,
      { params: this.construirParametros(filtro) }
    );
  }

  /**
   * Descarga el informe PDF generado por el backend.
   * Incluye las firmas parametrizadas cuando se solicita.
   */
  exportarPdf(
    filtro: PyGSinFormatoFiltro,
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
   * Descarga el informe Excel con importes numéricos.
   * Incluye las firmas parametrizadas cuando se solicita.
   */
  exportarExcel(
    filtro: PyGSinFormatoFiltro,
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
