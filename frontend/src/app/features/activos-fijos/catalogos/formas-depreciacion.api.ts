import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface FormaDepreciacionDTO {
  idFormaDepreciacion: number;
  nombreFormaDepreciacion: string;
}

@Injectable({ providedIn: 'root' })
export class FormasDepreciacionApi {

  private readonly base =
    `${environment.apiUrl}/activos-fijos/formas-depreciacion`;

  constructor(private http: HttpClient) {}

  listar(): Observable<FormaDepreciacionDTO[]> {
    return this.http.get<FormaDepreciacionDTO[]>(this.base);
  }
}
