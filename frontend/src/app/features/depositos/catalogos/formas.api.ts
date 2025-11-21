import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FormasAhorroApi {

  private readonly http = inject(HttpClient);
  // 🔹 OJO: en tu environment solo existe apiUrl
  private readonly base = `${environment.apiUrl}/depositos/catalogos/formas`;

  listar() {
    return this.http.get<any[]>(this.base);
  }

}
