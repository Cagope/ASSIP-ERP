import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

export interface ConsolidadoConceptosInformeDTO {
  codigoConcepto: string;
  nombreConcepto: string;
  tipoConcepto: string;

  cantidadRegistros: number;
  totalCantidad: number;
  totalValor: number;
}

@Injectable({ providedIn: 'root' })
export class ConsolidadoConceptosInformeApi {

  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/nomina/informes/consolidado-conceptos`;

  consultar(filtros: {
    codigoConcepto?: string | null;
    tipoConcepto?: string | null;
    fechaInicial?: string | null;
    fechaFinal?: string | null;
  }): Observable<ConsolidadoConceptosInformeDTO[]> {

    let params = new HttpParams();

    if (filtros.codigoConcepto) {
      params = params.set('codigoConcepto', filtros.codigoConcepto);
    }

    if (filtros.tipoConcepto) {
      params = params.set('tipoConcepto', filtros.tipoConcepto);
    }

    if (filtros.fechaInicial) {
      params = params.set('fechaInicial', filtros.fechaInicial);
    }

    if (filtros.fechaFinal) {
      params = params.set('fechaFinal', filtros.fechaFinal);
    }

    return this.http.get<ConsolidadoConceptosInformeDTO[]>(this.baseUrl, { params });
  }
}
