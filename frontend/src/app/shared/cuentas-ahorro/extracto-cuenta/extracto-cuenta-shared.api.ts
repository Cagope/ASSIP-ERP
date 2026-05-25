import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import {
  ExtractoCuentaSharedRequest,
  ExtractoCuentaSharedResponse
} from './extracto-cuenta-shared.models';

@Injectable({
  providedIn: 'root'
})
export class ExtractoCuentaSharedApi {

  private readonly http = inject(HttpClient);

  private readonly url =
    `${environment.apiUrl}/shared/cuentas-ahorro/extracto`;

  consultar(
    request: ExtractoCuentaSharedRequest
  ): Observable<ExtractoCuentaSharedResponse> {

    return this.http.post<ExtractoCuentaSharedResponse>(
      this.url,
      {
        ...request,
        fechaInicial: request.fechaInicial?.trim(),
        fechaFinal: request.fechaFinal?.trim()
      }
    );
  }
}
