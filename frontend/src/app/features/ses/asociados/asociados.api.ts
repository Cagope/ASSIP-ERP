import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AsociadosApi {

  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/superintendencia/asociados`;

  consultar(fechaCorte: string) {
    return this.http.get<any[]>(`${this.base}?fechaCorte=${fechaCorte}`);
  }
}
