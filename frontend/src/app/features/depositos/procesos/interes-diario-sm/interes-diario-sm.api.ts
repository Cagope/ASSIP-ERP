import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { firstValueFrom } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class InteresDiarioSmApi {

  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/depositos/interes-diario-sm`;

  ejecutar(body: any): Promise<any> {
    return firstValueFrom(
      this.http.post<any>(`${this.base}/ejecutar`, body)
    );
  }
}
