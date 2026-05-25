import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment }
  from '../../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DocumentosSoporteApi {

  private readonly http =
    inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/documentos-soporte`;

  // ======================================================
  // 🔍 Consulta principal
  // ======================================================
  consultar(filtros: {
    agencia: string;
    fechaDesde: string;
    fechaHasta: string;
  }) {

    return this.http
      .post<any>(
        `${this.base}`,
        filtros
      )
      .toPromise();

  }

  // ======================================================
  // 📊 Resumen agencia / forma
  // ======================================================
  resumenPorAgenciaForma(filtros: {
    agencia: string;
    fechaDesde: string;
    fechaHasta: string;
  }) {

    return this.http
      .post<any[]>(
        `${this.base}/resumen-agencia-forma`,
        filtros
      )
      .toPromise();

  }

}
