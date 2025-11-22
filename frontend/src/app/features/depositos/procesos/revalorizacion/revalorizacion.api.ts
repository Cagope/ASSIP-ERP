import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class RevalorizacionApi {

  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/depositos/revalorizacion`;

  ejecutar(body: any): Promise<any> {
    return this.http.post<any>(`${this.base}/ejecutar`, body).toPromise();
  }
}
