import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/** Los nombres y valores corresponden exactamente a las 62 columnas del backend. */
export type Formato9027DTO = Record<string, string>;

@Injectable({ providedIn: 'root' })
export class Formato9027Api {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/ses/formatos/9027`;

  consultar(fechaCorte: string): Observable<Formato9027DTO[]> {
    return this.http.get<Formato9027DTO[]>(this.url, {
      params: new HttpParams().set('fechaCorte', fechaCorte)
    });
  }
}
