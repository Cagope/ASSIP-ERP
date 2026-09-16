import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import {
  TablaAmortizacionRequest,
  TablaAmortizacionResponse
} from './tabla-amortizacion.models';

@Injectable({
  providedIn: 'root'
})
export class TablaAmortizacionApi {

  private readonly url =
    `${environment.apiUrl}/shared/financiero/tabla-amortizacion`;

  constructor(
    private readonly http: HttpClient
  ) {}

  calcular(
    request: TablaAmortizacionRequest
  ): Observable<TablaAmortizacionResponse> {

    return this.http.post<TablaAmortizacionResponse>(
      `${this.url}/calcular`,
      request
    );
  }
}
